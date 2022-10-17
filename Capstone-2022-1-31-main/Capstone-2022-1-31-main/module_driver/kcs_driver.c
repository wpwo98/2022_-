#include <stdio.h>
#include <stdarg.h>
#include <string.h>

#include "opendefs.h"
#include "kcs_driver.h"
#include "openserial.h"
#include "scheduler.h"
#include "wisun_neighbors.h"
#include "idmanager.h"
#include "openqueue.h"
#include "openbridge.h"
#include "leds.h"
#include "wisun_schedule.h"
#include "uart.h"
#include "opentimers.h"
#include "openhdlc.h"
#include "icmpv6rpl.h"
#include "icmpv6echo.h"
#include "debugpins.h"
#include "wisun_machigh.h"
#include "flash.h"
#include "memoryblock.h"
#include "msetting.h"
#include "topology.h"

//============================ defines ========================================

#define DEBUGPRINT_PERIOD 500 // in ms //현재 결과 출력주기

#define KCS_CHANNELMASK_BUFFER_SIZE              129 //채널마스크 버퍼
#define KCS_WISUNOPTION_INPUT_BUFFER_SIZE        20 //wi-sun option 버퍼

static char KCS_wrong_command_msg[] = "Wrong Command\n";

//=========================== variables =======================================

KCS_vars_t KCS_vars;

//=========================== prototypes ======================================


// printing
owerror_t KCS_printResendRequest(void);

// command handlers
void KCS_handleRxFrame(void);

// misc
void KCS_debugPrint_timer_cb(opentimers_id_t id);

// HDLC output
void _outputHdlcOpen(void);
void _outputHdlcWrite(uint8_t b);
void _outputHdlcClose(void);

// HDLC input
void _inputHdlcOpen(void);
void _inputHdlcWrite(uint8_t b);
void _inputHdlcClose(void);

// task
 void _task_printWrongCRCInput(void);
 void _task_printInputBufferOverflow(void);

//=========================== public ==========================================

//===== admin
void KCS_init(void) {
    // reset variable

    memset(&KCS_vars,0,sizeof(KCS_vars_t));

    // admin
    KCS_vars.fInhibited         = FALSE;
    KCS_vars.ctsStateChanged    = FALSE;
    KCS_vars.debugPrintCounter  = 0;

    // input
    KCS_vars.hdlcBusyReceiving  = FALSE;
    KCS_vars.hdlcInputEscaping  = FALSE;
    KCS_vars.inputBufFillLevel  = 0;

    // ouput
    KCS_vars.outputBufIdxR      = 0;
    KCS_vars.outputBufIdxW      = 0;
    KCS_vars.fBusyFlushing      = FALSE;
    KCS_vars.lastSentDataIdx    = 0;

    KCS_vars.reset_timerId      = opentimers_create(TIMER_GENERAL_PURPOSE, TASKPRIO_KCS);
    KCS_vars.debugPrint_timerId = opentimers_create(TIMER_GENERAL_PURPOSE, TASKPRIO_KCS);
    opentimers_scheduleIn(
        KCS_vars.debugPrint_timerId,
        DEBUGPRINT_PERIOD,
        TIME_MS,
        TIMER_PERIODIC,
        KCS_debugPrint_timer_cb
    );

    // UART
    uart1_setCallbacks(
        isr_KCS_tx,
        isr_KCS_rx
    );
    uart1_enableInterrupts();
}

//===== transmitting

owerror_t KCS_printResendRequest(void) {
    _outputHdlcOpen();
    _outputHdlcWrite(SERFRAME_MOTE2PC_RESEND_REQUEST);
    _outputHdlcClose();

    // start TX'ing
    KCS_flush();
}

//===== retrieving inputBuffer

void task_KCS_debugPrint(void) {
    uint8_t debugPrintCounter;
    INTERRUPT_DECLARATION();

    //<<<<<<<<<<<<<<<<<<<<<<<
    DISABLE_INTERRUPTS();
    debugPrintCounter = KCS_vars.debugPrintCounter;
    ENABLE_INTERRUPTS();
    //>>>>>>>>>>>>>>>>>>>>>>>

    if (KCS_vars.outputBufIdxW!=KCS_vars.outputBufIdxR) {
        return;
    }

    switch (debugPrintCounter) {
        case STATUS_ISSYNC:
            if (debugPrint_wisun_maclow_isSync()==TRUE) {
                break;
            }
        case STATUS_ID:
            if (debugPrint_id()==TRUE) {
               break;
            }
        case STATUS_DAGRANK:
            if (debugPrint_wisun_machigh_myDAGrank()==TRUE) {
                break;
            }
        case STATUS_OUTBUFFERINDEXES:
            if (debugPrint_outBufferIndexes()==TRUE) {
                break;
            }
        case STATUS_JOINSTATE:
            if (debugPrint_wisun_machigh_joinState()==TRUE) {
                break;
            }
        case STATUS_SCHEDULE:
            if(debugPrint_schedule()==TRUE) {
                break;
            }
        case STATUS_PHY:
            if(debugPrint_phy()==TRUE) {
                break;
            }
        case STATUS_QUEUE:
            if(debugPrint_queue()==TRUE) {
                break;
            }
        case STATUS_NEIGHBORS:
            if (debugPrint_wisun_neighbors()==TRUE) {
                break;
            }
        case STATUS_NETNAME:
            if (debugPrint_netname()==TRUE) {
                break;
            }
        case STATUS_FORCETOPOLOGY:
            if (debugPrint_forcetopology()==TRUE) {
                break;
            }
        default:
            debugPrintCounter = STATUS_MAX;
    }

    debugPrintCounter++;
    if (debugPrintCounter >= STATUS_MAX) {
       debugPrintCounter = 0;
    }

    //<<<<<<<<<<<<<<<<<<<<<<<
    DISABLE_INTERRUPTS();
    KCS_vars.debugPrintCounter = debugPrintCounter;
    ENABLE_INTERRUPTS();
    //>>>>>>>>>>>>>>>>>>>>>>>
}

//===== receiving -> data read

uint16_t KCS_getInputBufferFillLevel() {
    uint16_t inputBufFillLevel;
    INTERRUPT_DECLARATION();

    //<<<<<<<<<<<<<<<<<<<<<<<
    DISABLE_INTERRUPTS();
    inputBufFillLevel = KCS_vars.inputBufFillLevel;
    ENABLE_INTERRUPTS();
    //>>>>>>>>>>>>>>>>>>>>>>>

    return inputBufFillLevel-1; // removing the command byte
}

uint16_t KCS_getInputBuffer(uint8_t* bufferToWrite, uint8_t maxNumBytes) {
    uint16_t numBytesWritten;
    uint16_t inputBufFillLevel;
    INTERRUPT_DECLARATION();

    // //<<<<<<<<<<<<<<<<<<<<<<<
    // DISABLE_INTERRUPTS();
    // inputBufFillLevel = KCS_vars.inputBufFillLevel;
    // ENABLE_INTERRUPTS();
    // //>>>>>>>>>>>>>>>>>>>>>>>
    // openserial_printError(
    //         COMPONENT_OPENSERIAL,
    //         222,
    //         (errorparameter_t)maxNumBytes,
    //         (errorparameter_t)inputBufFillLevel-1
    //     );

    // if (maxNumBytes<inputBufFillLevel-1) {
    //     openserial_printError(
    //         COMPONENT_OPENSERIAL,
    //         ERR_GETDATA_ASKS_TOO_FEW_BYTES,
    //         (errorparameter_t)maxNumBytes,
    //         (errorparameter_t)inputBufFillLevel-1
    //     );
    //     numBytesWritten = 0;
    // } else {
    //     numBytesWritten = inputBufFillLevel-1;
    //     //<<<<<<<<<<<<<<<<<<<<<<<
    //     DISABLE_INTERRUPTS();
    //     memcpy(bufferToWrite,&(KCS_vars.inputBuf[1]),numBytesWritten);
    //     ENABLE_INTERRUPTS();
    //     //>>>>>>>>>>>>>>>>>>>>>>>
    // }
    //---------------------------

    numBytesWritten = 6;
    
    //가져와지는지 디버그
    // openserial_printError(
    //     COMPONENT_UECHO,
    //     252,
    //     (errorparameter_t)inputBufFillLevel,
    //     (errorparameter_t)"G"
    // );
    DISABLE_INTERRUPTS();
    memcpy(bufferToWrite,&(KCS_vars.inputBuf[1]),numBytesWritten);
    //memcpy(bufferToWrite,&(test_buffer),numBytesWritten);
    ENABLE_INTERRUPTS();
    //---------------------------
    inputBufFillLevel = KCS_vars.inputBufFillLevel;
    numBytesWritten = inputBufFillLevel;

    return numBytesWritten;
}

//===== scheduling

void KCS_flush(void) {//buffer보내는 부분
    INTERRUPT_DECLARATION();

    //<<<<<<<<<<<<<<<<<<<<<<<
    DISABLE_INTERRUPTS();
    if (KCS_vars.fBusyFlushing==FALSE) {
        if (KCS_vars.ctsStateChanged==TRUE) {
            // send CTS
#ifdef FASTSIM
#else
#endif
            KCS_vars.ctsStateChanged = FALSE;
        } else {
            if (KCS_vars.fInhibited==TRUE) {
                // currently inhibited
            } else {
                // not inhibited
                if (KCS_vars.outputBufIdxW!=KCS_vars.outputBufIdxR) {
                    // I have some bytes to transmit

#ifdef FASTSIM
                    uart_writeCircularBuffer_FASTSIM(
                        KCS_vars.outputBuf,
                        &KCS_vars.outputBufIdxR,
                        &KCS_vars.outputBufIdxW
                    );
#else
                    uart1_writeByte(KCS_vars.outputBuf[OUTPUT_BUFFER_MASK & (KCS_vars.outputBufIdxR++)]);
                    KCS_vars.fBusyFlushing = TRUE;
#endif
                }
            }
        }
    }
    ENABLE_INTERRUPTS();
    //>>>>>>>>>>>>>>>>>>>>>>>
}

//===== command handlers

// executed in ISR
void KCS_handleRxFrame() {
    uint8_t cmdByte;

    cmdByte = KCS_vars.inputBuf[0];
    // call registered commands
    if (KCS_vars.registeredCmd!=NULL && KCS_vars.registeredCmd->cmdId==cmdByte) {
        KCS_vars.registeredCmd->cb();
    }
}

//===== misc

void KCS_debugPrint_timer_cb(opentimers_id_t id){
    // calling the task directly as the timer_cb function is executed in
    // task mode by opentimer already
    task_KCS_debugPrint();
}

//===== hdlc (output)

/**
\brief Start an HDLC frame in the output buffer.
*/
port_INLINE void _outputHdlcOpen(void) {
    INTERRUPT_DECLARATION();

    //<<<<<<<<<<<<<<<<<<<<<<<
    DISABLE_INTERRUPTS();

    // initialize the value of the CRC

    KCS_vars.hdlcOutputCrc                                    = HDLC_CRCINIT;

    // write the opening HDLC flag
    KCS_vars.outputBuf[OUTPUT_BUFFER_MASK & (KCS_vars.outputBufIdxW++)]       = HDLC_FLAG;

    ENABLE_INTERRUPTS();
    //>>>>>>>>>>>>>>>>>>>>>>>
}
/**
\brief Add a byte to the outgoing HDLC frame being built.
*/
port_INLINE void _outputHdlcWrite(uint8_t b) {
    INTERRUPT_DECLARATION();

    //<<<<<<<<<<<<<<<<<<<<<<<
    DISABLE_INTERRUPTS();

    // iterate through CRC calculator
    KCS_vars.hdlcOutputCrc = crcIteration(KCS_vars.hdlcOutputCrc,b);

    // add byte to buffer
    if (b==HDLC_FLAG || b==HDLC_ESCAPE) {
        KCS_vars.outputBuf[OUTPUT_BUFFER_MASK & (KCS_vars.outputBufIdxW++)]   = HDLC_ESCAPE;
        b                                                            = b^HDLC_ESCAPE_MASK;
    }
    KCS_vars.outputBuf[OUTPUT_BUFFER_MASK & (KCS_vars.outputBufIdxW++)]       = b;

    ENABLE_INTERRUPTS();
    //>>>>>>>>>>>>>>>>>>>>>>>
}
/**
\brief Finalize the outgoing HDLC frame.
*/
port_INLINE void _outputHdlcClose(void) {
    uint16_t   finalCrc;
    INTERRUPT_DECLARATION();

    //<<<<<<<<<<<<<<<<<<<<<<<
    DISABLE_INTERRUPTS();

    // finalize the calculation of the CRC
    finalCrc   = ~KCS_vars.hdlcOutputCrc;

    // write the CRC value
    _outputHdlcWrite((finalCrc>>0)&0xff);
    _outputHdlcWrite((finalCrc>>8)&0xff);

    // write the closing HDLC flag
    KCS_vars.outputBuf[OUTPUT_BUFFER_MASK & (KCS_vars.outputBufIdxW++)]       = HDLC_FLAG;

    ENABLE_INTERRUPTS();
    //>>>>>>>>>>>>>>>>>>>>>>>
}

//===== hdlc (input)

/**
\brief Start an HDLC frame in the input buffer.
*/
port_INLINE void _inputHdlcOpen(void) {
    // reset the input buffer index
    KCS_vars.inputBufFillLevel                                = 0;

    // initialize the value of the CRC
    KCS_vars.hdlcInputCrc                                     = HDLC_CRCINIT;
}
/**
\brief Add a byte to the incoming HDLC frame.
*/
port_INLINE void _inputHdlcWrite(uint8_t b) {
    if (b==HDLC_ESCAPE) {
        KCS_vars.hdlcInputEscaping = TRUE;
    } else {
        if (KCS_vars.hdlcInputEscaping==TRUE) {
            b                             = b^HDLC_ESCAPE_MASK;
            KCS_vars.hdlcInputEscaping = FALSE;
        }

        // add byte to input buffer
        KCS_vars.inputBuf[KCS_vars.inputBufFillLevel] = b;
        KCS_vars.inputBufFillLevel++;

        // iterate through CRC calculator
        KCS_vars.hdlcInputCrc = crcIteration(KCS_vars.hdlcInputCrc,b);
    }
}
/**
\brief Finalize the incoming HDLC frame.
*/
port_INLINE void _inputHdlcClose(void) {

    // verify the validity of the frame
    if (KCS_vars.hdlcInputCrc==HDLC_CRCGOOD) {
        // the CRC is correct

        // remove the CRC from the input buffer
        KCS_vars.inputBufFillLevel    -= 2;
    } else {
        // the CRC is incorrect

        // drop the incoming frame
        KCS_vars.inputBufFillLevel     = 0;
    }
}

//=========================== task ============================================

void _task_printInputBufferOverflow(void){

}

void _task_printWrongCRCInput(void){
    // 재전송 요청
    KCS_printResendRequest();
}

// void task_printSentData(void) {
//     // start TX'ing
//     KCS_flush();
// }

//=========================== interrupt handlers ==============================

// executed in ISR, called from scheduler.c
void isr_KCS_tx(void) {
    //불리는지 디버그
    // openserial_printError(
    //     COMPONENT_UECHO,
    //     250,
    //     (errorparameter_t)"A",
    //     (errorparameter_t)"B"
    // );
    if (KCS_vars.ctsStateChanged==TRUE) {
        KCS_vars.ctsStateChanged = FALSE;
    } else if (KCS_vars.fInhibited==TRUE) {
        // currently inhibited

        KCS_vars.fBusyFlushing = FALSE;
    } else {
        // not inhibited

        if (KCS_vars.outputBufIdxW!=KCS_vars.outputBufIdxR) {
            // I have some bytes to transmit

            if (
                KCS_vars.outputBuf[OUTPUT_BUFFER_MASK & (KCS_vars.outputBufIdxR - 1)] == HDLC_FLAG &&
                KCS_vars.outputBuf[OUTPUT_BUFFER_MASK & (KCS_vars.outputBufIdxR)] != HDLC_FLAG
            ) {
                // 보낸 바이트가 HDLC_FLAG인데 다음 보낼 바이트는 HDLC_FLAG가 아님 == 보낸 바이트는 전송 데이터의 시작 바이트
                // 전송 데이터의 시작 바이트 위치 저장
                KCS_vars.lastSentDataIdx = KCS_vars.outputBufIdxR - 1;
            }

            uart1_writeByte(KCS_vars.outputBuf[OUTPUT_BUFFER_MASK & (KCS_vars.outputBufIdxR++)]);
            KCS_vars.fBusyFlushing = TRUE;
        } else {
            // I'm done sending bytes

            KCS_vars.fBusyFlushing = FALSE;
        }
    }
}

/**
\pre executed in ISR, called from scheduler.c

\returns 1 if don't receiving frame, 0 if not
*/

uint8_t isr_KCS_rx(void) {
    uint8_t rxbyte;
    uint8_t returnVal;

    returnVal = 0;

    // read byte just received
    rxbyte = uart1_readByte();
    leds_debug_toggle();
    if (
        KCS_vars.hdlcBusyReceiving==FALSE  &&
        KCS_vars.hdlcLastRxByte==HDLC_FLAG &&
        rxbyte!=HDLC_FLAG
    ) {
        // start of frame

        // I'm now receiving
        KCS_vars.hdlcBusyReceiving         = TRUE;

        // create the HDLC frame
        _inputHdlcOpen();

        // add the byte just received
        _inputHdlcWrite(rxbyte);
    } else if (
        KCS_vars.hdlcBusyReceiving==TRUE   &&
        rxbyte!=HDLC_FLAG
    ) {
        // middle of frame

        // add the byte just received
        _inputHdlcWrite(rxbyte);
        if (KCS_vars.inputBufFillLevel+1>SERIAL_INPUT_BUFFER_SIZE){
            // push task
            scheduler_push_task(_task_printInputBufferOverflow,TASKPRIO_KCS);
            KCS_vars.inputBufFillLevel      = 0;
            KCS_vars.hdlcBusyReceiving      = FALSE;
        }
    } else if (
        KCS_vars.hdlcBusyReceiving==TRUE   &&
        rxbyte==HDLC_FLAG
    ) {
        // end of frame

        // finalize the HDLC frame
        _inputHdlcClose();
        KCS_vars.hdlcBusyReceiving      = FALSE;

        if (KCS_vars.inputBufFillLevel==0){
            // push task
            scheduler_push_task(_task_printWrongCRCInput,TASKPRIO_KCS);
        } else {
            KCS_handleRxFrame();
            KCS_vars.inputBufFillLevel = 0;
            returnVal = 1;
        }
    }

    KCS_vars.hdlcLastRxByte = rxbyte;

    return returnVal;
}
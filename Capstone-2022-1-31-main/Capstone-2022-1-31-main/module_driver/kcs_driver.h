/**
\brief Declaration of the "KCS" driver.

\author Fabien Chraim <chraim@eecs.berkeley.edu>, March 2012.
\author Thomas Watteyne <thomas.watteyne@inria.fr>, August 2016.
*/

#ifndef __KCS_H
#define __KCS_H

#include <stdio.h>
#include <stdarg.h>
#include <string.h>

/**
\addtogroup drivers
\{
\addtogroup KCS
\{
*/

//=========================== define ==========================================

// for msetting
#define OPENSERIAL_SETTING_BUFFER_MAX_LEN 70

/**
\brief Number of bytes of the serial output buffer, in bytes.

\warning should be exactly 256 so wrap-around on the index does not require
         the use of a slow modulo operator.
*/
#define SERIAL_OUTPUT_BUFFER_SIZE 1024 // leave at 256!
#define OUTPUT_BUFFER_MASK       0x3FF

/**
\brief Number of bytes of the serial input buffer, in bytes.

\warning Do not pick a number greater than 255, since its filling level is
         encoded by a single byte in the code.
*/
#define SERIAL_INPUT_BUFFER_SIZE  1023

//=========================== typedef =========================================


//=========================== variables =======================================

//=========================== prototypes ======================================

typedef void (*KCS_cbt)(void);
typedef void (*KCS_callback_t)(void);
typedef void (*KCS_udpcallback_t)(open_addr_t* destAddr, uint16_t destPort, uint16_t srcPort, uint16_t payloadLen);

typedef struct _KCS_rsvpt {
    uint8_t                cmdId; ///< serial command (e.g. 'B')
    KCS_cbt                cb;    ///< handler of that command
    struct _KCS_rsvpt*     next;  ///< pointer to the next registered command
} KCS_rsvpt;

typedef struct {
    // admin
    uint8_t             fInhibited;
    uint8_t             ctsStateChanged;
    uint8_t             debugPrintCounter;
    KCS_rsvpt*          registeredCmd;
    uint8_t             reset_timerId;
    uint8_t             debugPrint_timerId;
    uint8_t             settingReq[OPENSERIAL_SETTING_BUFFER_MAX_LEN];
    uint16_t            settingReqLen;
    // input
    uint8_t             inputBuf[SERIAL_INPUT_BUFFER_SIZE];
    uint16_t            inputBufFillLevel;
    uint8_t             hdlcLastRxByte;
    bool                hdlcBusyReceiving;
    uint16_t            hdlcInputCrc;
    bool                hdlcInputEscaping;
    // output
    uint8_t             outputBuf[SERIAL_OUTPUT_BUFFER_SIZE];
    uint16_t            outputBufIdxW;
    uint16_t            outputBufIdxR;
    uint16_t            lastSentDataIdx;    // 재전송을 위한 마지막 전송한 데이터 시작 인덱스
    bool                fBusyFlushing;
    uint16_t            hdlcOutputCrc;
    KCS_callback_t gvcCallback;
    // for testing EDFE
    KCS_callback_t edfeTriggerCallback;
    // for ufantest
    KCS_udpcallback_t udpTriggerCallback;
} KCS_vars_t;

// admin
void KCS_init(void);

void      task_KCS_debugPrint(void);

// receiving
uint16_t  KCS_getInputBufferFillLevel(void);
uint16_t  KCS_getInputBuffer(uint8_t* bufferToWrite, uint8_t maxNumBytes);

// scheduling
void      KCS_flush(void);

// interrupt handlers
uint8_t   isr_KCS_rx(void);
void      isr_KCS_tx(void);

/**
\}
\}
*/

#endif

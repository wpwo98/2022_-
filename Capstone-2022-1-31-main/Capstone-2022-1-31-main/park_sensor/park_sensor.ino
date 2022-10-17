#define SERIAL_OUTPUT_BUFFER_SIZE  1024
#define OUTPUT_BUFFER_MASK         0x3FF
#define HDLC_FLAG                  0x7e
#define HDLC_ESCAPE                0x7d
#define HDLC_ESCAPE_MASK           0x20
#define HDLC_CRCINIT               0xffff
#define HDLC_CRCGOOD               0xf0b8

#define SERFRAME_MOTE2PC_DATA      ((uint8_t)'D')

typedef struct{
  uint8_t             ctsStateChanged;
  uint8_t             fInhibited;
  
  uint8_t             outputBuf[SERIAL_OUTPUT_BUFFER_SIZE];
  uint16_t            outputBufIdxW;
  uint16_t            outputBufIdxR;
  uint16_t            lastSentDataIdx; // 재전송을 위한 마지막 전송한 데이터 시작 인덱스
  bool                fBusyFlushing;
  uint16_t            hdlcOutputCrc;
}hdlc_vars_t;

hdlc_vars_t hdlc_vars;

void hdlc_init(void) {
    // reset variable
    memset(&hdlc_vars,0,sizeof(hdlc_vars_t));

    hdlc_vars.ctsStateChanged    = false;
    hdlc_vars.fInhibited         = false;    
    // ouput
    hdlc_vars.outputBufIdxR      = 0;
    hdlc_vars.outputBufIdxW      = 0;
    hdlc_vars.fBusyFlushing      = false;
    hdlc_vars.lastSentDataIdx    = 0;
}

static const uint16_t fcstab[256] = {
   0x0000, 0x1189, 0x2312, 0x329b, 0x4624, 0x57ad, 0x6536, 0x74bf,
   0x8c48, 0x9dc1, 0xaf5a, 0xbed3, 0xca6c, 0xdbe5, 0xe97e, 0xf8f7,
   0x1081, 0x0108, 0x3393, 0x221a, 0x56a5, 0x472c, 0x75b7, 0x643e,
   0x9cc9, 0x8d40, 0xbfdb, 0xae52, 0xdaed, 0xcb64, 0xf9ff, 0xe876,
   0x2102, 0x308b, 0x0210, 0x1399, 0x6726, 0x76af, 0x4434, 0x55bd,
   0xad4a, 0xbcc3, 0x8e58, 0x9fd1, 0xeb6e, 0xfae7, 0xc87c, 0xd9f5,
   0x3183, 0x200a, 0x1291, 0x0318, 0x77a7, 0x662e, 0x54b5, 0x453c,
   0xbdcb, 0xac42, 0x9ed9, 0x8f50, 0xfbef, 0xea66, 0xd8fd, 0xc974,
   0x4204, 0x538d, 0x6116, 0x709f, 0x0420, 0x15a9, 0x2732, 0x36bb,
   0xce4c, 0xdfc5, 0xed5e, 0xfcd7, 0x8868, 0x99e1, 0xab7a, 0xbaf3,
   0x5285, 0x430c, 0x7197, 0x601e, 0x14a1, 0x0528, 0x37b3, 0x263a,
   0xdecd, 0xcf44, 0xfddf, 0xec56, 0x98e9, 0x8960, 0xbbfb, 0xaa72,
   0x6306, 0x728f, 0x4014, 0x519d, 0x2522, 0x34ab, 0x0630, 0x17b9,
   0xef4e, 0xfec7, 0xcc5c, 0xddd5, 0xa96a, 0xb8e3, 0x8a78, 0x9bf1,
   0x7387, 0x620e, 0x5095, 0x411c, 0x35a3, 0x242a, 0x16b1, 0x0738,
   0xffcf, 0xee46, 0xdcdd, 0xcd54, 0xb9eb, 0xa862, 0x9af9, 0x8b70,
   0x8408, 0x9581, 0xa71a, 0xb693, 0xc22c, 0xd3a5, 0xe13e, 0xf0b7,
   0x0840, 0x19c9, 0x2b52, 0x3adb, 0x4e64, 0x5fed, 0x6d76, 0x7cff,
   0x9489, 0x8500, 0xb79b, 0xa612, 0xd2ad, 0xc324, 0xf1bf, 0xe036,
   0x18c1, 0x0948, 0x3bd3, 0x2a5a, 0x5ee5, 0x4f6c, 0x7df7, 0x6c7e,
   0xa50a, 0xb483, 0x8618, 0x9791, 0xe32e, 0xf2a7, 0xc03c, 0xd1b5,
   0x2942, 0x38cb, 0x0a50, 0x1bd9, 0x6f66, 0x7eef, 0x4c74, 0x5dfd,
   0xb58b, 0xa402, 0x9699, 0x8710, 0xf3af, 0xe226, 0xd0bd, 0xc134,
   0x39c3, 0x284a, 0x1ad1, 0x0b58, 0x7fe7, 0x6e6e, 0x5cf5, 0x4d7c,
   0xc60c, 0xd785, 0xe51e, 0xf497, 0x8028, 0x91a1, 0xa33a, 0xb2b3,
   0x4a44, 0x5bcd, 0x6956, 0x78df, 0x0c60, 0x1de9, 0x2f72, 0x3efb,
   0xd68d, 0xc704, 0xf59f, 0xe416, 0x90a9, 0x8120, 0xb3bb, 0xa232,
   0x5ac5, 0x4b4c, 0x79d7, 0x685e, 0x1ce1, 0x0d68, 0x3ff3, 0x2e7a,
   0xe70e, 0xf687, 0xc41c, 0xd595, 0xa12a, 0xb0a3, 0x8238, 0x93b1,
   0x6b46, 0x7acf, 0x4854, 0x59dd, 0x2d62, 0x3ceb, 0x0e70, 0x1ff9,
   0xf78f, 0xe606, 0xd49d, 0xc514, 0xb1ab, 0xa022, 0x92b9, 0x8330,
   0x7bc7, 0x6a4e, 0x58d5, 0x495c, 0x3de3, 0x2c6a, 0x1ef1, 0x0f78
};

uint16_t crcIteration(uint16_t crc, uint8_t byte) {
   return (crc >> 8) ^ fcstab[(crc ^ byte) & 0xff];
}

void outputHdlcOpen(void) {
    // initialize the value of the CRC
    hdlc_vars.hdlcOutputCrc = HDLC_CRCINIT;

    // write the opening HDLC flag
    hdlc_vars.outputBuf[OUTPUT_BUFFER_MASK & (hdlc_vars.outputBufIdxW++)] = HDLC_FLAG;
}

void outputHdlcWrite(uint8_t b) {
    // iterate through CRC calculator
    hdlc_vars.hdlcOutputCrc = crcIteration(hdlc_vars.hdlcOutputCrc,b);

    // add byte to buffer
    if (b==HDLC_FLAG || b==HDLC_ESCAPE) {
        hdlc_vars.outputBuf[OUTPUT_BUFFER_MASK & (hdlc_vars.outputBufIdxW++)] = HDLC_ESCAPE;
        b = b^HDLC_ESCAPE_MASK;
    }
    hdlc_vars.outputBuf[OUTPUT_BUFFER_MASK & (hdlc_vars.outputBufIdxW++)] = b;
}

void outputHdlcClose(void) {
    uint16_t   finalCrc;
    // finalize the calculation of the CRC
    finalCrc   = ~hdlc_vars.hdlcOutputCrc;

    // write the CRC value
    outputHdlcWrite((finalCrc>>0)&0xff);
    outputHdlcWrite((finalCrc>>8)&0xff);

    // write the closing HDLC flag
    hdlc_vars.outputBuf[OUTPUT_BUFFER_MASK & (hdlc_vars.outputBufIdxW++)] = HDLC_FLAG;
}

void isr_hdlc_tx(void) {
    if (hdlc_vars.ctsStateChanged==true) {
        hdlc_vars.ctsStateChanged = false;
    } else if (hdlc_vars.fInhibited==true) {
        hdlc_vars.fBusyFlushing = false;
    } else {
        if (hdlc_vars.outputBufIdxW!=hdlc_vars.outputBufIdxR) {
            // I have some bytes to transmit
            if (
                hdlc_vars.outputBuf[OUTPUT_BUFFER_MASK & (hdlc_vars.outputBufIdxR - 1)] == HDLC_FLAG &&
                hdlc_vars.outputBuf[OUTPUT_BUFFER_MASK & (hdlc_vars.outputBufIdxR)] != HDLC_FLAG
            ) {
                // 보낸 바이트가 HDLC_FLAG인데 다음 보낼 바이트는 HDLC_FLAG가 아님 == 보낸 바이트는 전송 데이터의 시작 바이트
                // 전송 데이터의 시작 바이트 위치 저장
                hdlc_vars.lastSentDataIdx = hdlc_vars.outputBufIdxR - 1;
            }
            //전송
            //uart_writeByte(hdlc_vars.outputBuf[OUTPUT_BUFFER_MASK & (hdlc_vars.outputBufIdxR++)]);
            Serial1.write(hdlc_vars.outputBuf[OUTPUT_BUFFER_MASK & (hdlc_vars.outputBufIdxR++)]);
            //Serial.println("Send in isr!");
            hdlc_vars.fBusyFlushing = true;
        } else {
            // I'm done sending bytes
            hdlc_vars.fBusyFlushing = false;
        }
    }
}

//sensor
#define Sen4  13
#define Sen3  12
#define Sen2  11
#define Sen1  10
#define TRIG4 9 //TRIG 핀 설정 (초음파 보내는 핀)
#define ECHO4 8 //ECHO 핀 설정 (초음파 받는 핀)
#define TRIG3 7
#define ECHO3 6
#define TRIG2 5
#define ECHO2 4
#define TRIG1 3
#define ECHO1 2
int temp_count = 0;

void setup() {
  //Serial.begin(9600);
  Serial1.begin(9600);
  //Serial.println("Start");
  hdlc_init();
  
  //pin setting
  pinMode(TRIG1, OUTPUT);
  pinMode(ECHO1, INPUT);
  pinMode(TRIG2, OUTPUT);
  pinMode(ECHO2, INPUT);
  pinMode(TRIG3, OUTPUT);
  pinMode(ECHO3, INPUT);
  pinMode(TRIG4, OUTPUT);
  pinMode(ECHO4, INPUT);
  pinMode(Sen1, INPUT);
  pinMode(Sen2, INPUT);
  pinMode(Sen3, INPUT);
  pinMode(Sen4, INPUT);
}

void loop() {
  long duration, distance; //초음파 센서
  int readValue; //조도 센서
  int val = 0;
  char p1= '0',p2 = '0', p3 = '0', p4 = '0';
  
  digitalWrite(TRIG1, LOW);
  delayMicroseconds(2);
  digitalWrite(TRIG1, HIGH);
  delayMicroseconds(10);
  digitalWrite(TRIG1, LOW);
  duration = pulseIn (ECHO1, HIGH); //물체에 반사되어돌아온 초음파의 시간
  distance = duration * 17 / 1000;

  val = digitalRead(Sen1);
  readValue = analogRead(A0);
  
  if(distance < 15 && val == HIGH) p1 = '2';
  else if(readValue > 120) p1 = '1';

  digitalWrite(TRIG2, LOW);
  delayMicroseconds(2);
  digitalWrite(TRIG2, HIGH);
  delayMicroseconds(10);
  digitalWrite(TRIG2, LOW);
  duration = pulseIn (ECHO2, HIGH); //물체에 반사되어돌아온 초음파의 시간
  distance = duration * 17 / 1000;

  val = digitalRead(Sen2);
  readValue = analogRead(A1);
  
  if(distance < 15 && val == HIGH) p2 = '2';
  else if(readValue > 120) p2 = '1';
  
  digitalWrite(TRIG3, LOW);
  delayMicroseconds(2);
  digitalWrite(TRIG3, HIGH);
  delayMicroseconds(10);
  digitalWrite(TRIG3, LOW);
  duration = pulseIn (ECHO3, HIGH); //물체에 반사되어돌아온 초음파의 시간
  distance = duration * 17 / 1000;

  val = digitalRead(Sen3);
  readValue = analogRead(A2);
  
  if(distance < 15 && val == HIGH) p3 = '2';
  else if(readValue > 120) p3 = '1';

  digitalWrite(TRIG4, LOW);
  delayMicroseconds(2);
  digitalWrite(TRIG4, HIGH);
  delayMicroseconds(10);
  digitalWrite(TRIG4, LOW);
  duration = pulseIn (ECHO4, HIGH); //물체에 반사되어돌아온 초음파의 시간
  distance = duration * 17 / 1000;

  val = digitalRead(Sen4);
  readValue = analogRead(A3);
  
  if(distance < 15 && val == HIGH) p4 = '2';
  else if(readValue > 120) p4 = '1';
  
  //send
  outputHdlcOpen();
  outputHdlcWrite(SERFRAME_MOTE2PC_DATA);
  outputHdlcWrite((uint8_t)'1');//주차장 고유번호
  outputHdlcWrite((uint8_t)'2');//주차장 줄 고유번호
  outputHdlcWrite((uint8_t)p1);
  outputHdlcWrite((uint8_t)p2);
  outputHdlcWrite((uint8_t)p3);
  outputHdlcWrite((uint8_t)p4);
  outputHdlcClose();

  while(hdlc_vars.outputBufIdxW!=hdlc_vars.outputBufIdxR){
    //hdlc_flush();
    isr_hdlc_tx();
  }
  delay(5000);
}

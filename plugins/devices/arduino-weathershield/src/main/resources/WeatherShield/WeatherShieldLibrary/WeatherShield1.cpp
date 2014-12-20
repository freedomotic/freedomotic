/* ------------------------------------------------------------------------------------- 
         - Weather Shield 1 communication library for Arduino - 
                                         - www.EtherMania.com - Marco Signorini - 
   -------------------------------------------------------------------------------------- */


#include "Arduino.h"
#include "WeatherShield1.h"
#include "string.h"

#define RXCOMMANDPOS		3
#define RXPAR1POS		2
#define RXPAR2POS		1
#define RXPAR3POS		0

#define RXBUFFERLENGTH          4
#define WEATHERSHIELD_DEFAULTADDRESS   0x01
#define WEATHERSHIELD_DEFAULTIODATA_PIN    2
#define WEATHERSHIELD_DEFAULTCLOCK_PIN     7


WeatherShield1::WeatherShield1() {
	
	m_clockPin = WEATHERSHIELD_DEFAULTCLOCK_PIN;
	m_dataPin = WEATHERSHIELD_DEFAULTIODATA_PIN;
	m_deviceAddress = WEATHERSHIELD_DEFAULTADDRESS;
	
	/* Start with a reset */
	resetConnection();
}

/* ----------------------------------------------------------------- */

WeatherShield1::WeatherShield1(unsigned char clockpin, unsigned char datapin, unsigned char deviceaddress) {
	
	m_clockPin = clockpin;
	m_dataPin = datapin;
	m_deviceAddress = deviceaddress;
	
	/* Start with a reset */  
	resetConnection();
}

/* ----------------------------------------------------------------- */

/* Send a specific command to the weather shield and return the related
answer. The answer will be stored in the provided buffer. 
This function returns true if the operation successfully terminates */
bool WeatherShield1::sendCommand(unsigned char ucCommand, unsigned char ucParameter, unsigned char *pucBuffer) {

	sendCommand(ucCommand, ucParameter);
	delayMicroseconds(15000);
	delayMicroseconds(15000);
	delayMicroseconds(15000);
	delayMicroseconds(15000);
	delayMicroseconds(15000);
	delayMicroseconds(15000);
	bool bResult = readAnswer(ucCommand, pucBuffer);
	
	return bResult;
}

/* ----------------------------------------------------------------- */

/* Decode the float value stored in the buffer */
float WeatherShield1::decodeFloatValue(unsigned char *pucBuffer) {
  
  char cMSD = (char) pucBuffer[RXPAR1POS];
  char cLSD = (char) pucBuffer[RXPAR2POS];

  float fVal = cMSD + (((float)cLSD) / 100.0f);
  
  return fVal;
}

/* ----------------------------------------------------------------- */

/* Decode an short value stored in the buffer */
unsigned short WeatherShield1::decodeShortValue(unsigned char *pucBuffer) {
	
  unsigned char ucMSD = pucBuffer[RXPAR1POS];
  unsigned char ucLSD = pucBuffer[RXPAR2POS];
	
  unsigned short shResult = (ucMSD<<8) | ucLSD;
	
  return shResult;
}

/* ----------------------------------------------------------------- */

void WeatherShield1::decodeFloatAsString(unsigned char *pucBuffer, char *chString) {
	
  char cMSD = (char)pucBuffer[RXPAR1POS];
  char cLSD = (char)pucBuffer[RXPAR2POS];
	
  if (cLSD < 0) {
	  cLSD = -cLSD;
	  
	  if (cMSD < 0)
		  cMSD = -cMSD;

	  sprintf(chString,"-%d.%d", cMSD, cLSD);
  } else
	sprintf(chString,"%d.%d", cMSD, cLSD);
}

/* ----------------------------------------------------------------- */

/* Send a series of low level bits in order to reset
the communication channel between the Arduino and the 
Weather Shield 1 */
void WeatherShield1::resetConnection() {
  
  /* Clock is always an output pin */
  pinMode(m_clockPin, OUTPUT);
  digitalWrite(m_clockPin, LOW);  

  /* Set data pin in output mode */
  pinMode(m_dataPin, OUTPUT);
  
  /* We start sending the first high level bit */
  digitalWrite(m_dataPin, HIGH);
  pulseClockPin();
  
  /* Send a sequence of "fake" low level bits */
  for (unsigned char ucN = 0; ucN < 200; ucN++) {

    digitalWrite(m_dataPin, LOW);
    pulseClockPin();
  }
}

/* ----------------------------------------------------------------- */

/* Generate a clock pulse */
void WeatherShield1::pulseClockPin() {
  
  digitalWrite(m_clockPin, HIGH);
  delayMicroseconds(5000);
  digitalWrite(m_clockPin, LOW);  
  delayMicroseconds(5000);	
}

/* ----------------------------------------------------------------- */

/* Send a byte through the communication bus (MSb first) */
void WeatherShield1::sendByte(unsigned char ucData) {
  
  for (unsigned char ucN = 0; ucN < 8; ucN++) {
    
    if (ucData & 0x80)
      digitalWrite(m_dataPin, HIGH);
    else
      digitalWrite(m_dataPin, LOW);
      
    pulseClockPin();
    ucData = ucData << 1;
  }
}

/* ----------------------------------------------------------------- */

unsigned char WeatherShield1::readByte() {
  
  unsigned char ucResult = 0;
  
  for (unsigned char ucN = 0; ucN < 8; ucN++) {
    
	digitalWrite(m_clockPin, HIGH);
	delayMicroseconds(5000);

	ucResult <<= 1;
	unsigned char ucIn = digitalRead(m_dataPin);
	if (ucIn != 0)
		ucResult |= 1;


	digitalWrite(m_clockPin, LOW);
	delayMicroseconds(5000);
  }
  
  return ucResult;
}

/* ----------------------------------------------------------------- */

/* Send a command request to the Weather Shield 1 */
void WeatherShield1::sendCommand(unsigned char ucCommand, unsigned char ucParameter) {
  
  /* Set data pin in output mode */
  pinMode(m_dataPin, OUTPUT);
  
  /* We start sending the first high level bit */
  digitalWrite(m_dataPin, HIGH);
  pulseClockPin();
  
  /* The first byte is always 0xAA... */
  sendByte(0xAA);
	
  /* ... then is the address... */
  sendByte(m_deviceAddress);
	
  /* ... then is the command ... */
  sendByte(ucCommand);
 
  /* ... and the parameter ... */
  sendByte(ucParameter);
  
  /* And this is the last low level bit required by the protocol */
  digitalWrite(m_dataPin, LOW);
  pulseClockPin();
}

/* ----------------------------------------------------------------- */

/* Read the answer back from the Weather Shield 1 and fill the provided
buffer with the result. Depending on the type of command associated
to this answer the buffer contents should be properly decoded.
The function returns true if the read answer contain the expected 
command */
bool WeatherShield1::readAnswer(unsigned char ucCommand, unsigned char *pucBuffer) {
  
  /* Set data pin in input mode */
  pinMode(m_dataPin, INPUT);
  
  /* Read RXBUFFERLENGTH bytes from the Weather Shield 1 */
    for (unsigned char ucN = RXBUFFERLENGTH; ucN > 0; ucN--) 
    pucBuffer[ucN-1] = readByte();
  
  /* Set data pin in output mode */
  pinMode(m_dataPin, OUTPUT);
  
  return (pucBuffer[RXCOMMANDPOS] == ucCommand);
}

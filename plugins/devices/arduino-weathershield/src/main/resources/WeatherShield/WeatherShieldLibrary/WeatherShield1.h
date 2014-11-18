/* ------------------------------------------------------------------------------------- 
         - Weather Shield 1 communication library for Arduino - 
                                         - www.EtherMania.com - Marco Signorini - 
   -------------------------------------------------------------------------------------- */

#ifndef _WEATHER_SHIELD_1_H_
#define _WEATHER_SHIELD_1_H_

/* These are the available command definitions */
#define CMD_UNKNOWN 		0x00
#define CMD_SETADDRESS		0x01
#define CMD_ECHO_PAR		0x02
#define CMD_SET_SAMPLETIME	0x03
#define CMD_GETTEMP_C_AVG	0x04
#define CMD_GETTEMP_C_RAW	0x05
#define CMD_GETPRESS_AVG		0x06
#define CMD_GETPRESS_RAW	0x07
#define CMD_GETHUM_AVG		0x08
#define CMD_GETHUM_RAW		0x09

#define PAR_GET_LAST_SAMPLE	0x80
#define PAR_GET_AVG_SAMPLE	0x81


class WeatherShield1 {
	
	public:
		WeatherShield1();
		WeatherShield1(unsigned char clockpin, unsigned char datapin, unsigned char deviceaddress);

		void resetConnection();	
		bool sendCommand(unsigned char ucCommand, unsigned char ucParameter, unsigned char *pucBuffer);
		float decodeFloatValue(unsigned char *pucBuffer);
		unsigned short decodeShortValue(unsigned char *pucBuffer);
		void decodeFloatAsString(unsigned char *pucBuffer, char *chString);
	
	private:
		void pulseClockPin();
		void sendByte(unsigned char ucData);
		unsigned char readByte();
		void sendCommand(unsigned char ucCommand, unsigned char ucParameter);
		bool readAnswer(unsigned char ucCommand, unsigned char *pucBuffer);
		
	private:
		unsigned char m_clockPin;
		unsigned char m_dataPin;
		unsigned char m_deviceAddress;
};

#endif /* _WEATHER_SHIELD_1_H_ */


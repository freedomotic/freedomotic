/*
  Arduino WeatherShield
  by Mauro Cicolella  - www.emmecilab.net
 
  Hardware:
  * Arduino UNO 
  * Ethernet shield 
  * WeatherShield by www.ethermania.com
 
  Web server original code by A. Mellis e Tom Igoe
 
*/

#include <String.h>
#include <SPI.h>
#include <Ethernet.h>
#include <WeatherShield1.h>

// Set Ethernet Shield MAC and IP addresses
byte mac[] = { 0xDE, 0xAD, 0xBE, 0xEF, 0xFE, 0xED };
byte ip[] = { 192,168,0,150 };

#define RXBUFFERLENGTH 4
WeatherShield1 weatherShield;

/* Class for sensors reading */
class WeatherData {

  private:
       float fTemperature;
       unsigned short shTemperature;
       float fPressure;
       unsigned short shPressure;
       float fHumidity;
       unsigned short shHumidity;

  public:
    WeatherData() { bReady = false; } // constructor
    
  // get methods
    float getAvgTemperature() { return fTemperature; }
    float getInstTemperature() { return ((float)shTemperature/16); }
    float getAvgPressure() { return fPressure; }
    float getInstPressure() { 
          float value ;
          value = (((float)shPressure/1024)+0.095)/0.009;
          return value;
        }
    float getAvgHumidity() { return fHumidity; }
    float getInstHumidity() { 
          float value;
          value = (((float)shHumidity/1024)-0.1515)/0.00636; // without compensation
          // compensation relative humidity with read temperature
          value = value/(1.0546-0.00216*getInstTemperature());
          return value;
       
      }
  
  // set methods
    void setAvgTemperature(float Temperature) { fTemperature=Temperature; }
    void setInstTemperature(unsigned short Temperature) { shTemperature=Temperature; }
    void setAvgPressure(float Pressure) { fPressure=Pressure; }
    void setInstPressure(unsigned short Pressure) { shPressure=Pressure; }
    void setAvgHumidity(float Humidity) { fHumidity=Humidity; }
    void setInstHumidity(unsigned short Humidity) { shHumidity=Humidity; }
    
  public:
    boolean bReady;
};

// server listening on port 80:
EthernetServer server(80);
String readString;
float temp;
WeatherData weatherData;
boolean currentLineIsBlank = true;


void setup()
{
  // start connection and server
  Ethernet.begin(mac, ip);
  server.begin();
}

void loop()
{
  // waiting for client requests
  EthernetClient client = server.available();
  if (client) {
    // blank line at the end of an HTTP request 
    readString="";
    while (client.connected()) {
      if (client.available()) {
        char c = client.read();
        readString.concat(c); 
        if (c == '\n') { break; }
       }
    }

    if(readValues(weatherShield)) 
       // send status code
       client.println("HTTP/1.1 200 OK");
       // set datatype
       client.println("Content-Type: text/html");
       client.println();
       // send html code
       client.print(weatherData.getInstTemperature());
       client.print(":"); 
       client.print(weatherData.getInstPressure());
       client.print(":"); 
       client.print(weatherData.getInstHumidity());
    // delay for data acquiring
    delay(1);
    // close connection
    client.stop();
  }
}


/* this function reads sensors values and stores them in a WeatherData object
   return value = false if there are some problems with Weather Shield.
*/

boolean readValues(WeatherShield1 &weatherShield) {
  
  /* buffer */
  unsigned char ucBuffer[RXBUFFERLENGTH];
  
  /* connection check */
  if (weatherShield.sendCommand(CMD_ECHO_PAR, 100, ucBuffer)) {
    
    /* temperature */
    if (weatherShield.sendCommand(CMD_GETTEMP_C_AVG, 0, ucBuffer))
      weatherData.setAvgTemperature(weatherShield.decodeFloatValue(ucBuffer));
    if (weatherShield.sendCommand(CMD_GETTEMP_C_RAW, PAR_GET_LAST_SAMPLE, ucBuffer))
      weatherData.setInstTemperature(weatherShield.decodeShortValue(ucBuffer));
    
    /* pressure */
    if (weatherShield.sendCommand(CMD_GETPRESS_AVG, 0, ucBuffer))
       weatherData.setAvgPressure(weatherShield.decodeFloatValue(ucBuffer));
    if (weatherShield.sendCommand(CMD_GETPRESS_RAW, PAR_GET_LAST_SAMPLE, ucBuffer))
       weatherData.setInstPressure(weatherShield.decodeShortValue(ucBuffer));
        
    /* humidity  */
    if (weatherShield.sendCommand(CMD_GETHUM_AVG, 0, ucBuffer))
      weatherData.setAvgHumidity(weatherShield.decodeFloatValue(ucBuffer));
    if (weatherShield.sendCommand(CMD_GETHUM_RAW, PAR_GET_LAST_SAMPLE, ucBuffer))
      weatherData.setInstHumidity(weatherShield.decodeShortValue(ucBuffer));

    return true;
  }  
  
  return false;
}




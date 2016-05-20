// Freedomotic Remote Controller Plugin
// by Mauro Cicolella
// www.freedomotic.com

#include <IRremote.h> 
#include <String.h> 
#include <SPI.h> 
#include <Ethernet.h>
#include <EthernetUdp.h>

int pinIRreceiver = 11;
IRrecv IRreceiver(pinIRreceiver);
decode_results receivedSignal;

byte mac[] = { 0xDE, 0xAD, 0xBE, 0xEF, 0xFE, 0xED }; // mac address  
byte ip[] = { 192, 168, 0, 2}; // ip arduino 
byte subnet[] = { 255, 255, 255, 0 }; //subnet mask 
byte gateway[] = { 192, 168, 0, 1}; // ip gateway 
EthernetServer server(80); // server web listening on port 80 

/* UDP configuration */
unsigned int UDPport = 7878;
EthernetUDP Udp;
IPAddress ipServerUDP(192, 168, 0, 20);
unsigned int ServerUDPport = 8000;

void setup()
{
  Serial.begin(9600); // serial monitor
  IRreceiver.enableIRIn(); 
  Ethernet.begin(mac, ip, gateway, subnet); 
  Udp.begin(UDPport);
}


void loop()
{
  if (IRreceiver.decode(&receivedSignal)) // received IR signal
  {
    IRreceiver.resume(); // ready to receive the next signal
    
    switch (receivedSignal.value) { 
        // button 1
        case 0xC03FA05F:  
        Serial.println("Pressed 1 button");
        sendUDPpacket('1');
        break;
        
        // button 2
        case 0xC03F609F: 
        Serial.println("Pressed button 2");
        sendUDPpacket('2');
        break;
     
        // button 3
        case 0xC03FE01F: 
        Serial.println("Pressed button 3");
        sendUDPpacket('3');
        break;
     
        // button 4
        case 0xC03F906F:
        Serial.println("Pressed button 4");
        sendUDPpacket('4'); 
        break;
    
        // button 5
        case 0xC03F50AF: 
        Serial.println("Pressed button 5");
        sendUDPpacket('5');
        break; 
    }
   
  }
}

void sendUDPpacket(char button)
{
  Udp.beginPacket(ipServerUDP, ServerUDPport);
  Udp.write("AN1:");
  Udp.write(button);
  Udp.endPacket();
}

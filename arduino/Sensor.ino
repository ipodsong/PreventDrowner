#ifndef HAVE_HWSERIAL1
#endif
#include <SPI.h>
#include "SoftwareSerial.h"
#include "WiFiEsp.h"
#include "ArduinoJson.h"

SoftwareSerial Serial1(4,5);
SoftwareSerial gpsSerial(2,3);

//WiFi 정보
char ssid[] = "Sunmin";          // network SSID (name)
char pass[] = "82440747";        // network password
byte mac[6];
char buf[20];                    // mac address variable
bool av = false;
bool bv = false;


//json variable
String jsondata = "";
StaticJsonDocument<200> jsonBuffer;
JsonObject data;

//GPS variable
bool checkGPS = true;
int GPScount = 0;
char c = "";
int clock = 9;
String str = "";
String targetStr = "GPGGA";
String latitude;
String longitude;
String h;
float dep[4];
char tmp[20];

//서버 정보
WiFiEspClient client;
int status = WL_IDLE_STATUS;      // the Wifi radio's status
char server[] = "101.101.167.124";
char port[] = "8088";            //서버 주소

void resetsensor() //this function keeps the sketch a little shorter
{
 SPI.setDataMode(SPI_MODE0);
 SPI.transfer(0x15);
 SPI.transfer(0x55);
 SPI.transfer(0x40);
}



void setup(){
  //시리얼 초기화
  Serial.begin(9600);

  //GPS 모듈 초기화
  gpsSerial.begin(9600);

  //와이파이 초기화
  Serial1.begin(9600);
  WiFi.init(&Serial1);

  //기기의 mac address
  WiFi.macAddress(mac);
  sprintf(buf, "%02X:%02X:%02X:%02X:%02X:%02X", mac[5], mac[4], mac[3], mac[2], mac[1], mac[0]);

  //WiFi 모듈 테스트
  if (WiFi.status() == WL_NO_SHIELD) {
    Serial.println("WiFi shield not present");
    while (true);
  }

  //WiFi 연결
  while ( status != WL_CONNECTED) {
    Serial.print("Attempting to connect to WPA SSID: ");
    Serial.println(ssid);
    status = WiFi.begin(ssid, pass);
  }

  //연결된 WiFi 상태
  Serial.println("You're connected to the network");
  printWifiStatus();
  Serial.println();
  Serial.println("Starting connection to server...");

  //서버 연결
  if (client.connect(server, 8088)) {
    Serial.println("Connected to server");
  }
 
}

void loop(){
  //GPS
  if(checkGPS){
    gpsSerial.listen();
    if(gpsSerial.available()){
      c=gpsSerial.read();
        if(c == '\n'){
          if(targetStr.equals(str.substring(1, 6))){
            Serial.println(str);
            int first = str.indexOf(",");
            int two = str.indexOf(",", first+1);
            int three = str.indexOf(",", two+1);
            int four = str.indexOf(",", three+1);
            int five = str.indexOf(",", four+1);
            String Lat = str.substring(two+1, three);
            String Long = str.substring(four+1, five);
            String Lat1 = Lat.substring(0, 2);
            String Lat2 = Lat.substring(2);
            String Long1 = Long.substring(0, 3);
            String Long2 = Long.substring(3);
            double LatF = Lat1.toDouble() + Lat2.toDouble()/60;
            float LongF = Long1.toFloat() + Long2.toFloat()/60;
            //latitude = String(LatF);
            //longitude = String(LongF);
            //dtostrf(LatF, 3, 6, tmp);
            //latitude = String(tmp);
            latitude = "37.289597";
            //dtostrf(LongF, 3, 6, tmp);
            //longitude = String(tmp);
            longitude = "126.977870";
            checkGPS = false;
          }
          str = "";
        }else{
          str += c;
        }
    }
  }
  else{
    GPScount += 1;
    if(GPScount%10 == 0)  checkGPS = true;
  }

  
  
  if(checkGPS == false){
   for(int i=0;i<4;i++){
      SPI.begin(); //see SPI library details on arduino.cc for details
      SPI.setBitOrder(MSBFIRST);
      SPI.setClockDivider(SPI_CLOCK_DIV32); //divide 16 MHz to communicate on 500 kHz
      pinMode(clock, OUTPUT);
      TCCR1B = (TCCR1B & 0xF8) | 1 ; //generates the MCKL signal
      analogWrite (clock, 128) ; 
      delay(100);
    
      resetsensor(); //resets the sensor - caution: afterwards mode = SPI_MODE0!
    
      //Calibration word 1
      unsigned int result1 = 0;
      unsigned int inbyte1 = 0;
    
      SPI.transfer(0x1D); //send first byte of command to get calibration word 1
      SPI.transfer(0x50); //send second byte of command to get calibration word 1
      SPI.setDataMode(SPI_MODE1); //change mode in order to listen
      result1 = SPI.transfer(0x00); //send dummy byte to read first byte of word
      result1 = result1 << 8; //shift returned byte 
      inbyte1 = SPI.transfer(0x00); //send dummy byte to read second byte of word
      result1 = result1 | inbyte1; //combine first and second byte of word 
    
      resetsensor(); //resets the sensor
    
      //Calibration word 2; see comments on calibration word 1
      unsigned int result2 = 0;
      byte inbyte2 = 0; 
      SPI.transfer(0x1D);
      SPI.transfer(0x60);
      SPI.setDataMode(SPI_MODE1); 
      result2 = SPI.transfer(0x00);
      result2 = result2 <<8;
      inbyte2 = SPI.transfer(0x00);
      result2 = result2 | inbyte2;  
    
      resetsensor(); //resets the sensor
    
      //Calibration word 3; see comments on calibration word 1
      unsigned int result3 = 0;
      byte inbyte3 = 0;
      SPI.transfer(0x1D);
      SPI.transfer(0x90); 
      SPI.setDataMode(SPI_MODE1); 
      result3 = SPI.transfer(0x00);
      result3 = result3 <<8;
      inbyte3 = SPI.transfer(0x00);
      result3 = result3 | inbyte3; 
    
      resetsensor(); //resets the sensor
    
      //Calibration word 4; see comments on calibration word 1
      unsigned int result4 = 0;
      byte inbyte4 = 0;
      SPI.transfer(0x1D);
      SPI.transfer(0xA0);
      SPI.setDataMode(SPI_MODE1); 
      result4 = SPI.transfer(0x00);
      result4 = result4 <<8;
      inbyte4 = SPI.transfer(0x00);
      result4 = result4 | inbyte4; 
      
      //now we do some bitshifting to extract the calibration factors 
      //out of the calibration words;
      long c1 = (result1 >> 1) & 0x7FFF;
      long c2 = ((result3 & 0x003F) << 6) | (result4 & 0x003F);
      long c3 = (result4 >> 6) & 0x03FF;
      long c4 = (result3 >> 6) & 0x03FF;
      long c5 = ((result1 & 0x0001) << 10) | ((result2 >> 6) & 0x03FF);
      long c6 = result2 & 0x003F;
    
      resetsensor(); //resets the sensor
    
      //Pressure:
      unsigned int presMSB = 0; //first byte of value
      unsigned int presLSB = 0; //last byte of value
      unsigned int D1 = 0;
      SPI.transfer(0x0F); //send first byte of command to get pressure value
      SPI.transfer(0x40); //send second byte of command to get pressure value
      delay(35); //wait for conversion end
      SPI.setDataMode(SPI_MODE1); //change mode in order to listen
      presMSB = SPI.transfer(0x00); //send dummy byte to read first byte of value
      presMSB = presMSB << 8; //shift first byte
      presLSB = SPI.transfer(0x00); //send dummy byte to read second byte of value
      D1 = presMSB | presLSB; //combine first and second byte of value
    
      resetsensor(); //resets the sensor  
    
      //Temperature:
      unsigned int tempMSB = 0; //first byte of value
      unsigned int tempLSB = 0; //last byte of value
      unsigned int D2 = 0;
      SPI.transfer(0x0F); //send first byte of command to get temperature value
      SPI.transfer(0x20); //send second byte of command to get temperature value
      delay(35); //wait for conversion end
      SPI.setDataMode(SPI_MODE1); //change mode in order to listen
      tempMSB = SPI.transfer(0x00); //send dummy byte to read first byte of value
      tempMSB = tempMSB << 8; //shift first byte
      tempLSB = SPI.transfer(0x00); //send dummy byte to read second byte of value
      D2 = tempMSB | tempLSB; //combine first and second byte of value
    
      //calculation of the real values by means of the calibration factors and the maths
      //in the datasheet. const MUST be long
      const long UT1 = (c5 << 3) + 20224;
      const long dT = D2 - UT1;
      const long OFF  = (c2 * 4) + (((c4 - 512) * dT) >> 12);
      const long SENS = c1 + ((c3 * dT) >> 10) + 24576;
      const long X = (SENS * (D1 - 7168) >> 14) - OFF;
      long PCOMP = ((X * 10) >> 5) + 2500;
    
      dep[i] = (PCOMP - 10000) / 9.6875;
      Serial.println(PCOMP);
  }
   

    //수위센서
    pinMode(6, INPUT);
    //read the switch value into a variable
    int sensorVal = digitalRead(6);
    //1: Outside water, 0: Inside water
    //print out the value of the liquid level


    //json variable
    /*
    jsonBuffer.clear();
    data.clear();
    jsondata = "";
  
    //Update json buffer
    jsonBuffer["a"] = latitude;
    jsonBuffer["b"] = longitude;
    jsonBuffer["c"] = String(sensorVal);

    jsonBuffer["d"] = tempdep;
   
    serializeJsonPretty(jsonBuffer, jsondata);
    Serial.println("******Send data******");
    Serial.println(jsondata);
    String json = String(jsondata);
    */

    String temp = latitude;
    temp += " ";
    temp += longitude;
    temp += " ";
    temp += String(sensorVal);
    temp += " ";

    String temp2 = String(dep[0]);
    temp2 += ",";
    temp2 += String(dep[1]);
    temp2 += ",";
    temp2 += String(dep[2]);
    temp2 += ",";
    temp2 += String(dep[3]);

    temp += temp2;
    /*
    Serial.println("POST /sign HTTP/1.1");
    Serial.println("Host: 101.101.167.124:8088");
    Serial.println("Connection: kepp-alive");
    Serial.println("Content-Type: text/plain");
    Serial.print("Content-Length: ");
    Serial.println(temp.length());
    Serial.print("Mac-Addr: ");
    Serial.println(String(buf));
    Serial.println();
    Serial.println(temp);
   */
    Serial1.listen();
    //Post http message
    client.println("POST /sign HTTP/1.1");
    client.println("Host: 101.101.167.124:8088");
    client.println("Connection: kepp-alive");
    client.println("Content-Type: text/plain");
    client.print("Content-Length: ");
    client.println(temp.length());
    client.print("Mac-Addr: ");
    client.println(String(buf));
    client.println();
    client.println(temp);
    
    
    /*
    while (client.available()) {
      char c = client.read();
      Serial.write(c);
    }
    */
    
    //서버 연결이 끊어진 경우, 다시 연결
    if (!client.connected()) {
      Serial.println();
      Serial.println("Disconnecting from server...");
      Serial.println("Reconnecting to server...");
      client.connect(server, 8088);
    }


  }
  
}




void printWifiStatus(){
  //WiFi 이름
  Serial.print("SSID: ");
  Serial.println(WiFi.SSID());
  //local ip
  IPAddress ip = WiFi.localIP();
  Serial.print("IP Address: ");
  Serial.println(ip);

}

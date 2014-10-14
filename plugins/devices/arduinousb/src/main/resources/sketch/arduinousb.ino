void setup(){
  Serial.begin(9600);

  //Set all the pins we need to output pins

  pinMode(13, OUTPUT);
}

void loop (){
  if (Serial.available()) {

    //read serial as a character
    char ser = Serial.read();

    //NOTE because the serial is read as "char" and not "int", the read value must be compared to character numbers
    //hence the quotes around the numbers in the case statement
    switch (ser) {
        case 'a':
        pinON(13);
        Serial.println("13;on");
        break;
        case 'b':
        pinOFF(13);
        Serial.println("13;off");
        break;
    }
  }
}

void pinON(int pin){
  digitalWrite(pin, HIGH);
}

void pinOFF(int pin){
  digitalWrite(pin, LOW);
}

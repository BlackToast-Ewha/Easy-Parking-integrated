#include <Wire.h>
#include <LiquidCrystal_I2C.h>
#include <stdio.h>

LiquidCrystal_I2C lcd(0x3f, 16, 2);

// arrow right => 0
byte arrowRight[8] = {
  B00000,
  B00100,
  B00010,
  B11111,
  B00010,
  B00100,
  B00000,
  B00000
};

// arrow left => 1
byte arrowLeft[8] = {
  B00000,
  B00100,
  B01000,
  B11111,
  B01000,
  B00100,
  B00000,
  B00000
};

// arrow up => 2
byte arrowUp[8] = {
  B00100,
  B01010,
  B10001,
  B10101,
  B00100,
  B00100,
  B00000,
  B00000
};

void setup() {
  lcd.init();
  lcd.backlight();
  lcd.clear();

  Serial.begin(9600); // 9600 보드레이트

  while (!Serial) {
    ; // wait for serial port to connect
  }
  lcd.setCursor(0, 0);
  lcd.createChar(0, arrowRight);
  lcd.createChar(1, arrowLeft);
  lcd.createChar(2, arrowUp);
}

String javaInput = "";
String result = "";

int loop_idx = 0;

void loop() {
  if (Serial.available() > 0) {
    byte incomingBytes = 0;
    incomingBytes = Serial.read(); // read the incoming byte:
    if (incomingBytes == byte(500)) {
      lcd.setCursor(0, 0);
      lcd.println("Destination!          ");
    }
    if (incomingBytes == byte(510)){
      lcd.setCursor(0, 0);
      lcd.println("Redirection!!!!!!");
  }
    if (incomingBytes != -1) {
      if (incomingBytes != 32) {
        String temp = String(incomingBytes);
        javaInput.concat(temp);
      }
    }
  }

  lcd.setCursor(0, 0);
  lcd.setCursor(0, 1);

  if (javaInput.length() == 5) {
    String carNum = javaInput.substring(0, 4);
    String dir = javaInput.substring(4);

    lcd.setCursor(0, 0);
    lcd.print("carNum " + carNum);
    lcd.setCursor(0, 1);
    lcd.write(dir.toInt());

  }

}

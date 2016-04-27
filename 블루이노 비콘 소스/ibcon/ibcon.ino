#include <RFduinoBLE.h>
#include <SoftwareSerial.h>
#include "LedControl.h"

// pin 3 on the RGB shield is the green led
//int led = 4;
int bluetoothTx = 3;  // TX-O pin of bluetooth mate, Arduino D2
int bluetoothRx = 2;  // RX-I pin of bluetooth mate, Arduino D3
int buzzerPin = 1;
SoftwareSerial bluetooth(bluetoothTx, bluetoothRx);
LedControl lc = LedControl(4, 5, 6, 1);

int modFlag = 1;
int recvCount = 0;
int emoticonArray[3][8];
int i = 0, j = 0;

int postInput = -1;

void setup() {
  // led used to indicate that iBeacon has started
  //pinMode(led, OUTPUT);

  // do iBeacon advertising
  RFduinoBLE.iBeacon = true;


  // start the BLE stack
  RFduinoBLE.begin();

  Serial.begin(9600);  // Begin the serial monitor at 9600bps
  bluetooth.begin(9600);  // Start bluetooth serial at 9600

  lc.shutdown(0, false);
  lc.setIntensity(0, 8);
  lc.clearDisplay(0);

  pinMode(buzzerPin, OUTPUT);
}


void loop() {
  // switch to lower power mode
  //    bluetooth.print("hello");

  if (modFlag == 1)
  {
    if (bluetooth.available())
    {
      int data = (int)bluetooth.read();
      Serial.print(String(i) + " " + String(j) + ":"); Serial.println(data);
      if (i < 3 && j < 8)
      {
        emoticonArray[i][j] = data;
        //lc.setRow(0, j, data);
        j++;
        if (j == 8)
        {
          j = 0;
          i++;
          if (i == 3)
          {
            i = 0;
            modFlag = 2;
            Serial.println("mode2 start");
          }
        }
      }


    }
  }
  else if (modFlag == 2)
  {
    Serial.println(postInput);
    if (bluetooth.available())
    {
      int data = (int)bluetooth.read();
      Serial.println(data);
      if (data != postInput)
      {
        switch (data)
        {
          case 0:
            for (int i = 0; i < 8; i++)
            {
              lc.setRow(0, i, emoticonArray[0][i]);
            }
            break;
          case 1:
            for (int i = 0; i < 8; i++)
            {
              lc.setRow(0, i, emoticonArray[1][i]);
            }
            break;
          case 2:
            for (int i = 0; i < 8; i++)
            {
              lc.setRow(0, i, emoticonArray[2][i]);
            }
            break;
          case 4:
            for (int i = 0; i < 8; i++)
            {
              lc.setRow(0, i, false);
            }
            break;
        }
      }
      postInput = data;
    }
    if (postInput == 2)
    {
      analogWrite(buzzerPin, HIGH);           // PWM 100% 적용
    }
    else
    {
      analogWrite(buzzerPin, LOW);           // PWM 100% 적용
    }
  }

}
void RFduinoBLE_onReceive()
{
}
void RFduinoBLE_onConnect()
{
  //digitalWrite(led, HIGH);
}
void RFduinoBLE_onDisconnect()
{
  //digitalWrite(led, LOW);
}
void RFduinoBLE_onAdvertisement(bool start)
{
  // turn the green led on if we start advertisement, and turn it
  // off if we stop advertisement
  if (start)
    //digitalWrite(led, HIGH);
  {
  }
  else
    //digitalWrite(led, LOW);
  {
  }
}

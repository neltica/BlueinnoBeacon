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

int sendData(String str)
{
  int sendNum = 0;
  String recv;
  while (true)
  {
    bluetooth.flush();
    sendNum = bluetooth.print(str);
    if (bluetooth.available())
    {
      recv = String(bluetooth.read());
      int count = 0;
      for (int i = 0; i < recv.length() - 1; i++)
      {
        if (48 > recv[i] || recv[i] > 57)
        {
          count++;
        }
      }
      if (count == 0)
      {
        break;
      }
    }
    delay(500);

  }
  return atoi(recv.c_str());

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
            modFlag = 3;
            Serial.println("mode2 start");
          }
        }
      }


    }
  }
  else if (modFlag == 2)
  {
    for (int i = 0; i < 3; i++)
    {
      for (int j = 0; j < 8; j++)
      {
        lc.setRow(0, j, emoticonArray[i][j]);
      }
      delay(1000);
    }

  }
  else if (modFlag == 3)
  {
    if (bluetooth.available())
    {
      int data = (int)bluetooth.read();
      Serial.println(data);
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
        default:
          for (int i = 0; i < 8; i++)
          {
            lc.setRow(0, i, false);
          }
          break;
      }
    }
  }
  else if(modFlag==4)
  {
    analogWrite(buzzerPin, 64);           // PWM 25% 적용
    delay(1000);                       // 1초 대기
    analogWrite(buzzerPin, 128);          // PWM 50% 적용
    delay(1000);                       // 1초 대기
    analogWrite(buzzerPin, 256);           // PWM 100% 적용
    delay(1000);                       // 1초 대기
  }


}

void setLED(int emoNum)
{
  for (int i = 0; i < 8; i++)
  {
    lc.setRow(0, i, emoticonArray[emoNum][i]);
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

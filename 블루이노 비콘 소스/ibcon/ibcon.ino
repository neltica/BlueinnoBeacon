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

int modFlag = 1;           //몇번모드로 갈것인지 지정 1번은 이모티콘 초기화 2번은 데이터 수신 및 이모티콘 변경
int recvCount = 0;
int emoticonArray[3][8];       //이모티콘들 저장해놓는 변수
int i = 0, j = 0;

int postInput = -1;                     //이전에 들어온값이 뭔지 저장하는 변수

void setup() {
  // led used to indicate that iBeacon has started
  //pinMode(led, OUTPUT);

  // do iBeacon advertising
  RFduinoBLE.iBeacon = true;                     //비콘 사용 설정


  // start the BLE stack
  RFduinoBLE.begin();               //비콘 초기화

  Serial.begin(9600);  // Begin the serial monitor at 9600bps   //시리얼 초기화
  bluetooth.begin(9600);  // Start bluetooth serial at 9600      //블루투스(ble)초기화

  lc.shutdown(0, false);                              //도트매트릭스 초기화
  lc.setIntensity(0, 8);
  lc.clearDisplay(0);

  pinMode(buzzerPin, OUTPUT);                  //부저
}


void loop() {
  // switch to lower power mode
  //    bluetooth.print("hello");

  if (modFlag == 1)
  {
    if (bluetooth.available())
    {
      int data = (int)bluetooth.read();             //수신가능할시 읽어서
      Serial.print(String(i) + " " + String(j) + ":"); Serial.println(data);
      if (i < 3 && j < 8)
      {
        emoticonArray[i][j] = data;    //이모티콘에 저장
        //lc.setRow(0, j, data);
        j++;
        if (j == 8)
        {
          j = 0;
          i++;
          if (i == 3)
          {
            i = 0;
            modFlag = 2;                            //모두 다 수신하였을 경우 모드 2로 넘어감
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
      int data = (int)bluetooth.read();          //수신할 경우
      Serial.println(data);
      if (data != postInput)                 //이전 수신값과 비교하여 다르면
      {
        switch (data)
        {
          case 0:                          //수신값이 0이면 첫번째 이모티콘 화면에 출력
            for (int i = 0; i < 8; i++)
            {
              lc.setRow(0, i,emoticonArray[0][i]);
            }
            break;
          case 1:                                  //수신값이 1이면 두번째 이모티콘 출력
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
          case 4:                                        //4면 화면 끄기
            for (int i = 0; i < 8; i++)
            {
              lc.setRow(0, i, false);
            }
            break;
        }
      }
      postInput = data;      //이전값을 현재값으로 세팅 다음번에는 현재값이 이전값이됨
    }
    if (postInput == 2)            //부저는 이전값이 2이면 계속 울리게 해놓음
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

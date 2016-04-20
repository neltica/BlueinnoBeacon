#include <RFduinoBLE.h>
#include <SoftwareSerial.h>

// pin 3 on the RGB shield is the green led
//int led = 4;
int bluetoothTx = 4;  // TX-O pin of bluetooth mate, Arduino D2
int bluetoothRx = 2;  // RX-I pin of bluetooth mate, Arduino D3
SoftwareSerial bluetooth(bluetoothTx, bluetoothRx);

void setup() {
  // led used to indicate that iBeacon has started
  //pinMode(led, OUTPUT);

  // do iBeacon advertising
  RFduinoBLE.iBeacon = true;


  // start the BLE stack
  RFduinoBLE.begin();

  Serial.begin(9600);  // Begin the serial monitor at 9600bps
  bluetooth.begin(9600);  // Start bluetooth serial at 9600
}

void loop() {
  // switch to lower power mode

  if (bluetooth.available()) // If the bluetooth sent any characters
  {
    // Send any characters the bluetooth prints to the serial monitor
    Serial.print((char)bluetooth.read());
  }
  if (Serial.available()) // If stuff was typed in the serial monitor
  {
    // Send any characters the Serial monitor prints to the bluetooth
    bluetooth.print((char)Serial.read());
  }

  //RFduino_ULPDelay(delayValue);
  //RFduino_ULPDelay(/*INFINITE*/1);
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

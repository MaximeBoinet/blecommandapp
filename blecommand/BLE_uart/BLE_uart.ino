#include <BLEDevice.h>
#include <BLEServer.h>
#include <BLEUtils.h>
#include <BLE2902.h>

BLEServer *pServer = NULL;
BLECharacteristic * pTxCharacteristic;
bool deviceConnected = false;
bool oldDeviceConnected = false;
uint8_t txValue = 0;
uint8_t pinConnected = 19;
uint8_t pinDisconected = 18;
uint8_t openTalk = 25;
uint8_t dataTransit = 23;
uint8_t next = 14;
uint8_t previous = 12;
uint8_t manageLed = 5;
uint8_t askForMic = 21;
bool ledactivated = true;
bool micOpen = false;
unsigned long lastDebounceTime = 0;
unsigned long debounceDelay = 500;

#define SERVICE_UUID           "6E400001-B5A3-F393-E0A9-E50E24DCCA9E"
#define CHARACTERISTIC_UUID_RX "6E400002-B5A3-F393-E0A9-E50E24DCCA9E"
#define CHARACTERISTIC_UUID_TX "6E400003-B5A3-F393-E0A9-E50E24DCCA9E"


class MyServerCallbacks: public BLEServerCallbacks {
    void onConnect(BLEServer* pServer) {
      if (ledactivated)
        digitalWrite(pinConnected, HIGH);
      digitalWrite(pinDisconected, LOW);
      deviceConnected = true;
    };

    void onDisconnect(BLEServer* pServer) {
      if (ledactivated)
        digitalWrite(pinDisconected, HIGH);
      digitalWrite(pinConnected, LOW);
      deviceConnected = false;
    }
};

class MyCallbacks: public BLECharacteristicCallbacks {
    void onWrite(BLECharacteristic *pCharacteristic) {
      std::string rxValue = pCharacteristic->getValue();

      if (ledactivated)
        digitalWrite(dataTransit, HIGH);
      
      if (rxValue.length() > 0) {
        Serial.println("*********");
        Serial.print("Received Value: ");
        for (int i = 0; i < rxValue.length(); i++) {
          Serial.print("|");
          Serial.print(rxValue[i]);
          Serial.print("|");
        }
        Serial.println();
        Serial.println("*********");
        if (rxValue[0] != 1) {
          micOpen == false;
        } else {
          micOpen == true;
        }
        digitalWrite(openTalk, rxValue[0] == 1);
      }
      delay(200);
      digitalWrite(dataTransit, LOW);
    }
};


void setup() {
  Serial.begin(115200);

  pinMode(pinConnected, OUTPUT);
  pinMode(pinDisconected, OUTPUT);
  pinMode(openTalk, OUTPUT);
  pinMode(dataTransit, OUTPUT);
  pinMode(next, INPUT_PULLUP);
  pinMode(previous, INPUT_PULLUP);
  pinMode(askForMic, INPUT_PULLUP);
  pinMode(manageLed, INPUT_PULLUP);

  if (ledactivated)
    digitalWrite(pinDisconected, HIGH);
    
  BLEDevice::init("MyHologramCommand");
  
  pServer = BLEDevice::createServer();
  pServer->setCallbacks(new MyServerCallbacks());
  
  BLEService *pService = pServer->createService(SERVICE_UUID);

  pTxCharacteristic = pService->createCharacteristic(CHARACTERISTIC_UUID_TX, BLECharacteristic::PROPERTY_NOTIFY);                
  pTxCharacteristic->addDescriptor(new BLE2902());

  BLECharacteristic * pRxCharacteristic = pService->createCharacteristic(CHARACTERISTIC_UUID_RX, BLECharacteristic::PROPERTY_WRITE);
  pRxCharacteristic->setCallbacks(new MyCallbacks());
  
  pService->start();
  pServer->getAdvertising()->start();
  Serial.println("Waiting a client connection to notify...");
}

void loop() {

  if (digitalRead(manageLed) == LOW) {
    if (ledactivated) {
      turnOffLeds();
    } else {
      turnOnLeds();
    }
  }
  
  if (deviceConnected) {
    if (millis() - lastDebounceTime > debounceDelay) {
      lastDebounceTime = millis();
      manageMessageToSend();
    }
	}
 
  if (!deviceConnected && oldDeviceConnected) {
    delay(500);
    pServer->startAdvertising();
    Serial.println("start advertising");
    oldDeviceConnected = deviceConnected;
  }
  
  if (deviceConnected && !oldDeviceConnected) {
      oldDeviceConnected = deviceConnected;
  }
}

void manageMessageToSend() {
  
  if (digitalRead(next) == LOW) {
    
    if (ledactivated)
      digitalWrite(dataTransit, HIGH);
      
    txValue = 0;
    pTxCharacteristic->setValue(&txValue, 1);
    Serial.print("Sending value: ");
    Serial.println(txValue);
    pTxCharacteristic->notify();
    delay(25);
    digitalWrite(dataTransit, LOW);
  } else if (digitalRead(previous) == LOW) {
    
    if (ledactivated)
      digitalWrite(dataTransit, HIGH);
      
    txValue = 1;
    pTxCharacteristic->setValue(&txValue, 1);
    Serial.print("Sending value: ");
    Serial.println(txValue);
    pTxCharacteristic->notify();
    delay(25);
    digitalWrite(dataTransit, LOW);
  }

  if (digitalRead(askForMic) == LOW) {

    if (ledactivated)
      digitalWrite(dataTransit, HIGH);
      
    txValue = 2;
    pTxCharacteristic->setValue(&txValue, 1);
    Serial.print("Sending value: ");
    Serial.println(txValue);
    pTxCharacteristic->notify();
    delay(25);
    digitalWrite(dataTransit, LOW);
  }
}

void turnOffLeds() {
  digitalWrite(pinConnected, LOW);
  digitalWrite(pinDisconected, LOW);
  digitalWrite(openTalk, LOW);
  digitalWrite(dataTransit, LOW);
  ledactivated = false;
}

void turnOnLeds() {
  if (deviceConnected) {
    digitalWrite(pinConnected, HIGH);
  } else {
    digitalWrite(pinDisconected, HIGH);
  }
  if (micOpen) {
    digitalWrite(openTalk, HIGH);
  }
  ledactivated = true;
}

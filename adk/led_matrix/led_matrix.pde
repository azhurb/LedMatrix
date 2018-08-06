#include "ht1632.h"
#include <Usb.h>
#include <AndroidAccessory.h>

AndroidAccessory acc("Google, Inc.",
    "LedMatrix",
    "LedMatrix Arduino Board",
    "1.0",
    "https://github.com/azhurb/LedMatrix/blob/master/android/bin/LedMatrix.apk",
    "0000000012345678");

void setup () {
  
    ht1632_setup();

    Serial.begin(9600);
    Serial.print("\r\nStart");

    acc.powerOn();
}

void loop () {

    byte msg[128];

    if (acc.isConnected()) {
        int len = acc.read(msg, sizeof(msg), 1);
                
        Serial.print("\r\nMsg length: ");
        Serial.print(len, DEC);
                
        if (len > 0) {
            ht1632_sendframe(msg);
        }
    }

    delay(10);
}

void plot(int pos, int val){
    
    int x = pos / 16;
    int y = pos % 16;
    
    ht1632_plot(x, y, val);
}

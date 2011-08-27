#include "ht1632.h"
#include <Usb.h>
#include <AndroidAccessory.h>

AndroidAccessory acc("Google, Inc.",
		     "DemoKit",
		     "DemoKit Arduino Board",
		     "1.0",
		     "http://www.android.com",
		     "0000000012345678");

void setup () {
  
  ht1632_setup();
  
  Serial.begin(9600);
  
  acc.powerOn();
}

void loop () {

	byte msg[16];

	if (acc.isConnected()) {
		int len = acc.read(msg, sizeof(msg), 1);

		if (len > 0) {


		}
	}

	delay(10);
}

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

	byte msg[128];

	if (acc.isConnected()) {
		int len = acc.read(msg, sizeof(msg), 1);

		if (len > 0) {

			for (int i = 0; i < len; i++){
				plot(i * 4 + 0, (msg[i] >> 0) & 3);
				plot(i * 4 + 1, (msg[i] >> 2) & 3);
				plot(i * 4 + 2, (msg[i] >> 4) & 3);
				plot(i * 4 + 3, (msg[i] >> 6) & 3);
			}
		}
	}

	delay(10);
}

void plot(int pos, int val){
	
	int x = pos % 16;
	int y = pos / 16;

	Serial.print("\r\nx: ");
	Serial.print(x, DEC);
	Serial.print("; y: ");
	Serial.print(y, DEC);
	Serial.print("; color: ");
	Serial.print(val, DEC);
	
	ht1632_plot(x, y, val);
}
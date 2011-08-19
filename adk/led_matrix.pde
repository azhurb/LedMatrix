#include <HT1632.h>
#include <Usb.h>
#include <AndroidAccessory.h>

#define CS1_PIN		12
#define CS2_PIN		13
#define WR_PIN		10
#define DATA_PIN	9

AndroidAccessory acc("Google, Inc.",
		     "DemoKit",
		     "DemoKit Arduino Board",
		     "1.0",
		     "http://www.android.com",
		     "0000000012345678");

void setup () {
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

#include "ht1632.h"

void setup () {
  ht1632_setup();

  Serial.begin(115200);

  ht1632_clear();
}


void loop () {
  
  ht1632_plot(1, 1, GREEN);
  ht1632_plot(1, 2, RED);
  ht1632_plot(1, 3, ORANGE);

  delay(LONGDELAY);
}

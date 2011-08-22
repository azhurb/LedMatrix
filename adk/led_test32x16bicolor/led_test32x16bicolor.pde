#include "ht1632.h"

void setup () {
  ht1632_setup();

  Serial.begin(115200);

  ht1632_clear();
}

int color = 1;

void loop () {
  
  for (int i=0; i<32; i++){
    for (int j=0; j<16; j++){
      ht1632_plot(i, j, color);      
    }
  }
  
  if (color < 3){
    color++;
  }else{
    color = 1;
  }
  
  //ht1632_plot(1, 2, RED);
  //ht1632_plot(1, 3, ORANGE);

  //delay(1000);
}

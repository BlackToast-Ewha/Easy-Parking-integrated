const int pin_ir_in = 3; // IR sensor의 out단자와 연결된 mega의 pin
int prev_state =1; 
int curr_state = 1;
int counter = 0;

void setup() {
  // put your setup code here, to run once:
  pinMode(pin_ir_in, INPUT);
  Serial.begin(9600);
}

void loop() {
  // main code
  int curr_state = digitalRead(pin_ir_in);          // Check current state
  if (curr_state != prev_state)                     // Detect Rising or Falling Edge
  {
    if(curr_state == 0)                             // IF Obstacle Detected
    {
      //digitalWrite(pin_led_out, HIGH);              // LED ON
      counter++;                                    // Increment the Counter
      Serial.println("Obstacle Detected...!");
      Serial.println(counter); 
    }
    else
    {
      //digitalWrite(pin_led_out, LOW);               // LED OFF
    }
    prev_state = curr_state;                        // save prev state
  }
  delay(100);                                       // Wait for 100 mSec.
}

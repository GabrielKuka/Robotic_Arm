/*

                                                                    ******************************************************
                                                                    *                ~~~~~~~~~~~~~~~~~                   *
                                                                    *                   Gabriel Kuka                     *
                                                                    *                ~~~~~~~~~~~~~~~~~                   *
                                                                    ******************************************************

                                                                    ~`~`~`~`~`~`~`~`~`~`~`~`~`~`~`~`~`~`~`~`~`~`~`~`~`~`~`
                                                                             Arduino code to cotrol robotic arm
                                                                    ~`~`~`~`~`~`~`~`~`~`~`~`~`~`~`~`~`~`~`~`~`~`~`~`~`~`~`
Author: Gabriel Kuka
Robotic group: Robofultz
Version: 2.1

*/

//////// Imported libraries ////////
#include <Servo.h>

//////// Servo Objects ////////
Servo servoBase;
Servo servoShoulder;
Servo servoElbow;
Servo servoWrist;
Servo servoGripper;

//////// Temporary values ////////
int base_pos_temp = 90;
int shoulder_pos_temp = 90;
int elbow_pos_temp = 50;
int wrist_pos_temp = 140;
int gripper_pos_temp = 60;

//////// Other variables ////////
char data;
int pos;

void checkServoAndChangeDegree(){
  switch(data){
  case 'a': base_pos_temp += 10; break;
  case 'b': base_pos_temp -= 10; break;
  case 'c': shoulder_pos_temp += 10; break;
  case 'd': shoulder_pos_temp -= 10; break;
  case 'e': elbow_pos_temp += 10; break;
  case 'f': elbow_pos_temp -= 10; break;
  case 'g': wrist_pos_temp += 10; break;
  case 'h': wrist_pos_temp -= 10; break;
  }
}

void checkServoAndChangeDegreeReverse(){
  switch(data){
  case 'a': base_pos_temp -= 10; break;
  case 'b': base_pos_temp += 10; break;
  case 'c': shoulder_pos_temp -= 10; break;
  case 'd': shoulder_pos_temp += 10; break;
  case 'e': elbow_pos_temp -= 10; break;
  case 'f': elbow_pos_temp += 10; break;
  case 'g': wrist_pos_temp -= 10; break;
  case 'h': wrist_pos_temp += 10; break;
  }
}

int checkServo(){
  switch(data){
    case 'a': case 'b': return base_pos_temp; break;
    case 'c': case 'd': return shoulder_pos_temp; break;
    case 'e': case 'f': return elbow_pos_temp; break;
    case 'g': case 'h': return wrist_pos_temp; break;
  }
}

void setup() {
  initialize(); // <- initialize function
}

void initialize(){
  
   Serial.begin(9600); // <- speed of signal transmission

  //////// Attach servos to specific pins ////////
   servoShoulder.attach(2);
   servoElbow.attach(3);
   servoWrist.attach(4);
   servoGripper.attach(5);
   servoBase.attach(6);
  
  //////// Set servos to default positions ////////
   servoShoulder.write(shoulder_pos_temp);
   servoElbow.write(elbow_pos_temp);
   servoWrist.write(wrist_pos_temp);
   servoGripper.write(gripper_pos_temp);
   servoBase.write(base_pos_temp);
  
}

void loop() {
  checkCommand();
}

void checkCommand(){
  if (Serial.available() > 0){
  
    char bData = Serial.read(); 
    data = bData;
    switch(bData){
      case 'a' : rotateServo(servoBase, base_pos_temp, 10); break;
      case 'b' : rotateServo(servoBase, base_pos_temp, -10); break;
      case 'c' : rotateServo(servoShoulder, shoulder_pos_temp, 10); break;
      case 'd' : rotateServo(servoShoulder, shoulder_pos_temp, -10); break;
      case 'e' : rotateServo(servoElbow, elbow_pos_temp, 10); break;
      case 'f' : rotateServo(servoElbow, elbow_pos_temp, -10); break;
      case 'g' : rotateServo(servoWrist, wrist_pos_temp, 10); break;
      case 'h' : rotateServo(servoWrist, wrist_pos_temp, -10); break;
      case 'x' : rotateServo(servoGripper, gripper_pos_temp, 20); break;
      case 'z' : rotateServo(servoGripper, gripper_pos_temp, -20); break;
      default : Serial.println("Information not valid!");  // <- In case smth wrong happens nothing will go to the servo
      }
  } 
}



void rotateServo(Servo servo, int temporary_position, int side){
 if(data == 'z' || data == 'x'){
  if(side > 0){
    if(temporary_position < 60){
      for(pos = temporary_position; pos <= temporary_position + side; pos++){
        servo.write(pos);
        delay(20);
      }
      gripper_pos_temp += 20;
    }else { Serial.println("Limit!"); gripper_pos_temp -= 20; rotateServo(servo, gripper_pos_temp, side); }
  }else{
    if(temporary_position > 10){
      for(pos = temporary_position; pos >= temporary_position + side; pos--){
        servo.write(pos);
        delay(20);
      }
      gripper_pos_temp -= 20;
    }else{
      gripper_pos_temp += 20; rotateServo(servo, gripper_pos_temp, side); Serial.println("Limit!");
    }
  } 
 
 }else{
    if(side > 0){
      if(temporary_position < 170){
        for(pos = temporary_position; pos <= temporary_position + side; pos++){
          servo.write(pos);
          delay(20);
        }
        checkServoAndChangeDegree();
      }
      else{checkServoAndChangeDegreeReverse(); rotateServo(servo, checkServo(), side); Serial.println("Limit!");}
    }else{
      if(temporary_position > 10){
        for(pos = temporary_position; pos >= temporary_position + side; pos --){
          servo.write(pos);
          delay(20);
        }
        checkServoAndChangeDegree();
      }else{
        checkServoAndChangeDegreeReverse(); rotateServo(servo, checkServo(), side); Serial.println("Limit!");
       }
     }
    }
  }




/*
                                                                    ~`~`~`~`~`~`~`~`~`~`~`~`~`~`~`~`~`~`~`~`~`~`~`~`~`~`~`
                                                                                           End of code 
                                                                    ~`~`~`~`~`~`~`~`~`~`~`~`~`~`~`~`~`~`~`~`~`~`~`~`~`~`~`
*/

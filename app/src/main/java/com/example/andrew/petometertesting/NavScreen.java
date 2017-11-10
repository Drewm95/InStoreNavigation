package com.example.andrew.petometertesting;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Calendar;
import java.util.Date;

public class NavScreen extends Activity implements SensorEventListener {


    //declare the sensor manager
    private SensorManager sensorManager;
    private Sensor mAccel,mMag;
    private int stepCount, stepSense, targetDistance, turnCode, navStep;
    private Date lastUpdate;
    private float lastZ, newZ,newX, newY, lastx, lasty,stepDistRatio;
    private double azumuthStart, azimuth;
    private TextView stepCountView, instructionView;
    public static float[] mAccelerometer = null;
    public static float[] mGeomagnetic = null;
    private ImageView  arrow;
    private String instructionString;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);



        //set strideLength
        stepDistRatio = 24; //Stride length of 2 feet

        setContentView(R.layout.activity_nav_screen);

        //instatiate the sensor manager
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        //Get the textViews
        //lowAccuracyWarning = (TextView)findViewById(R.id.lowAccuracyWarning);
        stepCountView = (TextView)findViewById(R.id.NavStepCount);
        arrow = (ImageView)findViewById(R.id.arrowView);
        instructionView = (TextView)findViewById(R.id.InstructionView);


        //get the default accelerometer from the sm
        if (sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) != null) {
            //there is a accelerameter
            mAccel = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        } else {

        }

        //get the default magnetometer from the sm
        if (sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD) != null) {
            //there is a magnetometer
            mMag = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

            //DEBUG TEXT
            //mag1.setText("SENSOR FOUND");
        } else {
            // mag1.setText("SENSOR NOT PRESENT");
        }


        //set all of the required initial last acceleration values
        lastZ = 0; //TODO do this better
        lastx= 0;
        lasty = 0;
        stepSense = 2;
        //get the time, this is used to prevent counting a single step multiple times
        lastUpdate = Calendar.getInstance().getTime();

        demoSetup();

    }

    @Override
    protected void onResume(){
        super.onResume();
        sensorManager.registerListener(this, mAccel , SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(this, mMag, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    protected void onPause(){
        super.onPause();
        sensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        /*

        ACCELEROMETER SENSOR CHANGE

         */
        if(event.sensor == mAccel) {

            //assign the new event values to the accelerometer array for use with finding direction
            mAccelerometer = event.values;

            //get the latest value from the sensor

            newX = event.values[0];
            newY = event.values[1];
            newZ = event.values[2];
            //check if the new value is much different than the last one
            if (newZ + newX + newY > lastZ + lastx + lasty + stepSense) {

                //make sure it has been at least 10 milliseconds since the last step count
                //if not don't count it, people don't walk that fast
                if (Calendar.getInstance().getTimeInMillis() > lastUpdate.getTime() + 500) {
                    //A step was detected
                    stepCount++;
                    stepCountView.setText(Integer.toString(stepCount));
                    lastUpdate = Calendar.getInstance().getTime();
                    updateArrow();
                }
            }

            //assign value to lastxyz before moving on
            lastZ = newZ;
            lastx = newX;
            lasty = newY;
        }

        /*

        MAG FIELD SENSOR CHANGE

         */

        if (event.sensor == mMag) {

            //assign the new event values to the magnetometer array for use with finding direction
            mGeomagnetic = event.values;

        }


        /*

        GET DIRECTION USING THE MAGNETIC FIELD SENSOR

         */

        azimuth = 0;
        double pitch = 0;
        double roll = 0;

        if (mAccelerometer != null && mGeomagnetic != null) {
            float R[] = new float[9];
            float I[] = new float[9];
            boolean success = SensorManager.getRotationMatrix(R, I, mAccelerometer, mGeomagnetic);

            if (success) {
                float orientation[] = new float[3];
                SensorManager.getOrientation(R, orientation);
                // at this point, orientation contains the azimuth(direction), pitch and roll values.
                azimuth = 180 * orientation[0] / Math.PI;
                pitch = 180 * orientation[1] / Math.PI;
                roll = 180 * orientation[2] / Math.PI;
            }


        }

        /*
        UPDATE THE NAV SCREEN AND CHECK FOR UPDATED DIRECTIONS
         */

        updateArrow();

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }


    public static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        BigDecimal bd = new BigDecimal(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }


    private void updateArrow(){

        if(stepCount >= targetDistance / stepDistRatio && turnCode == 0) {
            arrow.setImageDrawable(getDrawable(R.drawable.arrowforwardfill));
            arrow.setRotation(0);
            navStep++;
            CheckNavigation();

        }
        else{
            arrow.setImageDrawable(getDrawable(R.drawable.arrowforward));
            arrow.setRotation(0);
        }

        if(turnCode == 1 && Math.abs(azimuth - azumuthStart) >= 90 ) { //left Turn //TODO this just detects a 90 degree turn, does not account for direction.
            navStep++;
            CheckNavigation();
            //arrow.setImageDrawable(getDrawable(R.drawable.arrowforwardfill));
            //arrow.setRotation(270);
        }
        else if(turnCode == 1){
            arrow.setImageDrawable(getDrawable(R.drawable.arrowforwardfill));
            arrow.setRotation(270);
        }
        if (turnCode == 2 && Math.abs(azimuth - azumuthStart) >= 90 ) { //right turn
            navStep++;
            CheckNavigation();
            //arrow.setImageDrawable(getDrawable(R.drawable.arrowforwardfill));
            //arrow.setRotation(90);
        }
        else if(turnCode == 2){
            arrow.setImageDrawable(getDrawable(R.drawable.arrowforwardfill));
            arrow.setRotation(90);
        }
    }

    private void demoSetup(){
        //This method is to set all the variables needed to run a demo without a map or db connection
        //first tell the user to walk forward 10 steps
        navStep = 0;
    }

    protected void CheckNavigation(){
        //forward 10 feet
        if (navStep == 0){
            updateNav(120, 0);
        }
        //turn right
        if (navStep == 1){
            updateNav(0, 2);
        }
        //forward 5 feet
        if (navStep == 2){
            updateNav(60, 0);
        }
        //turn left
        if (navStep == 3){
            updateNav(0, 1);
        }
        //forward 5 feet
        if (navStep == 4){
            updateNav(60, 0);
        }
    }

    protected void updateNav(int distance, int tcode){

        //create the instruction string
        if(turnCode == 0){
            instructionView.setText("Walk Forward " + distance / 12 + " feet.");
        }
        else if( turnCode == 1){
            instructionView.setText("Turn Left");

        }
        else if( turnCode == 2){
            instructionView.setText("Turn Right");

        }

        //Update the global variables and the arrow indicator

        targetDistance = distance;
        turnCode = tcode;
        azumuthStart = azimuth; //we need to know what angle the phone was at the start so we know when they complete the turn
    }



}


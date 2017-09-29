package com.example.andrew.petometertesting;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.util.Calendar;
import java.util.Date;

public class MainActivity extends Activity implements SensorEventListener {


    //declare the sensor manager
    private SensorManager sensorManager;
    private Sensor mAccel;
    private int stepCount, stepSense;
    private Date lastUpdate;
    private float lastZ, newZ, avgZ, count,zSum, newX, newY, lastx, lasty;
    private TextView stepDetected,lowAccuracyWarning,x,y,z,avgZText, stepCountView;

    /*

    set this to change the

    */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //instatiate the sensor manager
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        //Get the textViews
        stepDetected = (TextView)findViewById(R.id.stepDetectedText);
        lowAccuracyWarning = (TextView)findViewById(R.id.lowAccuracyWarning);
        x = (TextView)findViewById(R.id.xText);
        y = (TextView)findViewById(R.id.yText);
        z = (TextView)findViewById(R.id.zText);
        avgZText = (TextView)findViewById(R.id.zAverage);
        stepCountView = (TextView)findViewById(R.id.stepCount);


        //get the reset button reference
        final Button button = (Button) findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Code here executes on main thread after user presses button
                stepCount = 0;
                avgZ = 0;
                stepCountView.setText("0");
            }
        });

        final EditText stepSenseInput = (EditText)findViewById(R.id.editText);
        stepSenseInput.addTextChangedListener(new TextWatcher() {
                                                  @Override
                                                  public void beforeTextChanged(CharSequence s, int start, int count, int after){

                                                  }

                                                  @Override
                                                  public void onTextChanged(CharSequence s, int start, int before, int count) {

                                                  }

                                                  @Override
                                                  public void afterTextChanged(Editable s) {
                                                      try {
                                                          stepSense = Integer.parseInt(stepSenseInput.getText().toString());
                                                      }
                                                      catch (Exception exp){

                                                      }
                                                  }
                                              });

        //get the default accelerometer from the sm
        if (sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) != null) {

            mAccel = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

            //there is a accelerameter
            lastZ = 0; //TODO do this better
            lastx= 0;
            lasty = 0;
            zSum = 0;
            avgZ = 0;
            stepSense = 3;

        } else {

        }

        //get the time, this is used to prevent counting a single step multiple times
        lastUpdate = Calendar.getInstance().getTime();



    }

    @Override
    protected void onResume(){
        super.onResume();
        sensorManager.registerListener(this, mAccel , SensorManager.SENSOR_DELAY_UI);
    }

    @Override
    protected void onPause(){
        super.onPause();
        sensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        //get the latest value from the sensor

        newX = event.values[0];
        newY = event.values[1];
        newZ = event.values[2];
        //check if the new value is much different than the last one
        if(newZ + newX + newY > lastZ + lastx +lasty + stepSense) {

            //make sure it has been at least 10 milliseconds since the last step count
            //if not don't count it, people don't walk that fast
            if(Calendar.getInstance().getTimeInMillis() > lastUpdate.getTime() + 500) {
                //A step was detected
                stepDetected.setVisibility(View.VISIBLE);
                stepCount++;
                stepCountView.setText(Integer.toString(stepCount));
                lastUpdate = Calendar.getInstance().getTime();
            }
        }
        else {
            stepDetected.setVisibility(View.INVISIBLE);
        }

        //report other readings
        if(newX > lastx + 0.1 || newX < lastx - 0.1)
        x.setText(Float.toString(event.values[0]));
        if(newY > lasty+ 0.1 || newY < lasty - 0.1)
        y.setText(Float.toString(event.values[1]));
        if(newZ > lastZ + 0.1 || newZ < lastZ - 0.1)
        z.setText(Float.toString(event.values[2]));

        //get the average z
        count++;
        zSum +=  newZ;
        avgZ = zSum / count;
        avgZText.setText(Float.toString(avgZ));

        //assign value to lastxyz before moving on
        lastZ = newZ;
        lastx = newX;
        lasty = newY;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        if (accuracy < 3) {
            lowAccuracyWarning.setVisibility(View.VISIBLE);
    }
        else{
            lowAccuracyWarning.setVisibility(View.INVISIBLE);
        }
}




}

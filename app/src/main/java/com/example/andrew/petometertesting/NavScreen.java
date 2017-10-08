package com.example.andrew.petometertesting;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

public class NavScreen extends AppCompatActivity {

    private SensorManager sensorManager;
    private TextView stepCountView;
    private Sensor mStep;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nav_screen);


        //instatiate the sensor manager
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);


        //get the default step counter from the sensor manager
        if (sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR) != null) {
            //there is a step counter
            stepCountView.setText("STEP SENSOR FOUND");
            mStep = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR);

        } else {

            stepCountView.setText("STEP SENSOR NOT PRESENT");

        }

        //Set the progress bar



    }

}

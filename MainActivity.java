package com.example.assignment;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.view.View;
import android.widget.TextView;
import android.widget.Button;
import android.view.View.OnClickListener;

public class MainActivity extends AppCompatActivity implements SensorEventListener, StepListener  {
    private StepDetector simpleStepDetector;
    private SensorManager sensorManager;
    private Sensor accel, linearaccelerometer;
    private static final String TEXT_NUM_STEPS = "Number of Steps: ";
    private static final float NS2S = 1.0f / 1000000000.0f;
    private int numSteps;
    private long timeprev = 0;
    private float distance;
    TextView TvSteps, TvVelDis;
    Button BtnStart, BtnStop;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        // Get an instance of the SensorManager
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        accel = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        linearaccelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        simpleStepDetector = new StepDetector();
        simpleStepDetector.registerListener(this);

        TvSteps = (TextView) findViewById(R.id.tv_steps);
        TvVelDis = (TextView) findViewById(R.id.tv_veldis);
        BtnStart = (Button) findViewById(R.id.btn_start);
        BtnStop = (Button) findViewById(R.id.btn_stop);


        BtnStart.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {

                numSteps = 0;
                distance = 0;
                sensorManager.registerListener(MainActivity.this, accel, SensorManager.SENSOR_DELAY_FASTEST);
                sensorManager.registerListener(MainActivity.this, linearaccelerometer, SensorManager.SENSOR_DELAY_NORMAL);

            }
        });


        BtnStop.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {

                sensorManager.unregisterListener(MainActivity.this);

            }
        });



    }


    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            simpleStepDetector.updateAccel(
                    event.timestamp, event.values[0], event.values[1], event.values[2]);
        }

        if (event.sensor.getType() == Sensor.TYPE_LINEAR_ACCELERATION) {
            float dT = 0;
            if(timeprev != 0){
                dT = (event.timestamp - timeprev)*NS2S;
            }
            timeprev  = event.timestamp;

            float x, y, z;
            float vx, vy,vz;

            x = event.values[0];
            y = event.values[1];
            z = event.values[2];

            float normal = (float)Math.sqrt(x * x + y * y + z * z);

            if(normal < 1){
                //then it is random noise, ignore
                vx = 0;
                vy = 0;
                vz = 0;
            }
            else{
                vx = x*dT;
                vy = y*dT;
                vz = z*dT;
            }


            float speed = (float) (Math.sqrt(vx*vx + vy*vy + vz*vz)) ;
            distance += speed*dT;

            final String textData;
            textData = String.format("Speed=%.3fm/s Distance=%.3fm ",speed,distance);
            TvVelDis.setText(textData);

        }
    }

    @Override
    public void step(long timeNs) {
        numSteps++;
        TvSteps.setText(TEXT_NUM_STEPS + numSteps);
    }

    @Override
    public void velanddis(float vel_t, float dis_t) {
        final String textData;
        textData = String.format("Speed=%.3fm/s Distance=%.3fm",vel_t, dis_t);
        TvVelDis.setText(textData);
    }

}

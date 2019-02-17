package edu.uchicago.lorell;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.app.Activity;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.TextView;
import android.util.Log;

public class MainActivity extends AppCompatActivity implements SensorEventListener {


    private static final String TAG = "MainActivity";


    private SensorManager sensorManager;
    Sensor accelerometer;


    TextView xAccl, yAccl, zAccl;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        xAccl = (TextView) findViewById(R.id.xAccl);
        yAccl = (TextView) findViewById(R.id.yAccl);
        zAccl = (TextView) findViewById(R.id.zAccl);



        Log.d(TAG, "onCreate: Initializing Sensor Services.");
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);


        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorManager.registerListener(MainActivity.this, accelerometer, SensorManager.SENSOR_DELAY_UI);
        Log.d(TAG, "onCreate: Registered accelerometer listener.");

    }


    @Override
    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(MainActivity.this, accelerometer, SensorManager.SENSOR_DELAY_UI);
    }


    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(MainActivity.this);
    }


    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {


    }


    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {

        Log.d(TAG, "onSensorChanged: X: " + sensorEvent.values[0] + "  Y: " + sensorEvent.values[1] + "  Z: " + sensorEvent.values[2]);

        xAccl.setText("    xAccl: " + sensorEvent.values[0]);
        yAccl.setText("    yAccl: " + sensorEvent.values[1]);
        zAccl.setText("    zAccl: " + sensorEvent.values[2]);


    }
}

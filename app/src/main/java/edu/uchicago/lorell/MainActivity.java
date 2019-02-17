package edu.uchicago.lorell;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.support.v7.app.AppCompatActivity;
import android.app.Activity;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.TextView;
import android.util.Log;
import android.widget.ToggleButton;
import android.view.View;
import android.view.View.OnClickListener;
import java.io.File;
import java.io.FileOutputStream;

import org.w3c.dom.Text;

public class MainActivity extends AppCompatActivity implements SensorEventListener, OnClickListener {


    private static final String TAG = "MainActivity";


    private SensorManager sensorManager;
    Sensor accelerometer;


    TextView xAccl, yAccl, zAccl;
    ToggleButton toggle;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        toggle = (ToggleButton) findViewById(R.id.toggle);
        toggle.setOnClickListener(this);

        xAccl = (TextView) findViewById(R.id.xAccl);
        yAccl = (TextView) findViewById(R.id.yAccl);
        zAccl = (TextView) findViewById(R.id.zAccl);


        Log.d(TAG, "onCreate: Initializing Sensor Services.");
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);


        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorManager.registerListener(MainActivity.this, accelerometer, SensorManager.SENSOR_DELAY_UI);
        Log.d(TAG, "onCreate: Registered accelerometer listener.");

        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

    }


    @Override
    protected void onResume() {
        super.onResume();

        boolean off = toggle.isChecked();

        if(off == false) {
            sensorManager.registerListener(MainActivity.this, accelerometer, SensorManager.SENSOR_DELAY_UI);
        };
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

    @Override
    public void onClick(View v) {

        boolean off = toggle.isChecked();
        TextView text;

        if(off == false) {
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI);
        }else{
            sensorManager.unregisterListener(this,accelerometer);

            String filename = "myfile";
            String fileContents = "Why howdy!";
            FileOutputStream outputStream;

            try {
                outputStream = openFileOutput(filename, Context.MODE_PRIVATE);
                outputStream.write(fileContents.getBytes());
                outputStream.close();
            } catch (Exception e) {
                e.printStackTrace();
            }



            //Use this to view the files in the working directory.
            /*

            PackageManager m = getPackageManager();
            String s = getPackageName();
            String x = "";


            try {
                PackageInfo p = m.getPackageInfo(s, 0);
                s = p.applicationInfo.dataDir;
                File directory = new File(s);
                File[] files = directory.listFiles();

                for (int i=0; i < files.length; i++)
                {
                    x = x + "\n" + files[i].getName();
                }



            } catch (PackageManager.NameNotFoundException e) {
                Log.w("yourtag", "Error Package name not found ", e);
            }


            zAccl.setText(x);
            */

        }

    }


}

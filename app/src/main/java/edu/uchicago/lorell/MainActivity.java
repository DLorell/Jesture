package edu.uchicago.lorell;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;
import android.widget.ToggleButton;
import java.io.FileOutputStream;


public class MainActivity extends AppCompatActivity implements SensorEventListener, OnClickListener {


    private static final String TAG = "MainActivity";
    Double x = 0.0;
    Double y = 0.0;
    Double z = 0.0;
    String record = "[";
    Long startTime;
    Long time;


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

        xAccl.setText("xAccl: ----");
        yAccl.setText("yAccl: ----");
        zAccl.setText("zAccl: ----");


        Log.d(TAG, "onCreate: Initializing Sensor Services.");
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);


        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);

        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

    }

    /*
    @Override
    protected void onResume() {
        super.onResume();

        boolean on = toggle.isChecked();

        if(on) {
            sensorManager.registerListener(MainActivity.this, accelerometer, SensorManager.SENSOR_DELAY_UI);
        };
    }
    */


    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(MainActivity.this);

        xAccl.setText("xAccl: ----");
        yAccl.setText("yAccl: ----");
        zAccl.setText("zAccl: ----");

        toggle.setChecked(false);
    }


    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {


    }


    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {

        Log.d(TAG, "onSensorChanged: X: " + sensorEvent.values[0] + "  Y: " + sensorEvent.values[1] + "  Z: " + sensorEvent.values[2]);


        x = Math.floor(sensorEvent.values[0] * 100) / 100;
        y = Math.floor(sensorEvent.values[1] * 100) / 100;
        z = Math.floor(sensorEvent.values[2] * 100) / 100;
        time = (System.currentTimeMillis() - startTime);

        record = record + "{xAccl:" + Double.toString(x) + ", yAccl:"
                                           + Double.toString(y) + ", zAccl:"
                                           + Double.toString(z) + ", time:"
                                           + Long.toString(time) + "},\n";

        String xString = "xAccl: " + Double.toString(x);
        String yString = "yAccl: " + Double.toString(y);
        String zString = "zAccl: " + Double.toString(z);

        xAccl.setText(xString);
        yAccl.setText(yString);
        zAccl.setText(zString);


    }

    // This gets called every time you click the button.
    @Override
    public void onClick(View v) {

        boolean on = toggle.isChecked();
        TextView text;

        // "Hit record"
        if(on) {
            startTime = System.currentTimeMillis();
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI);
        // "Hit Stop Recording"
        }else{
            sensorManager.unregisterListener(this,accelerometer);


            xAccl.setText("xAccl: ----");
            yAccl.setText("yAccl: ----");
            zAccl.setText("zAccl: ----");

            String filename = "sensorData.txt";
            String fileContents = record + "]";
            FileOutputStream outputStream;

            try {
                outputStream = openFileOutput(filename, Context.MODE_PRIVATE);
                outputStream.write(fileContents.getBytes());
                outputStream.close();
                //deleteFile(filename);


            } catch (Exception e) {
                e.printStackTrace();
            }



            //Use this to view the files in the working directory.
            /*
            *
            *

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

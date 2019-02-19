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
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.BufferedWriter;
import android.content.pm.PackageManager;
import android.content.pm.PackageInfo;
import android.os.Environment;



public class MainActivity extends AppCompatActivity implements SensorEventListener, OnClickListener {


    private static final String TAG = "MainActivity";
    Double x = 0.0;
    Double y = 0.0;
    Double z = 0.0;
    String record = "[";
    Long startTime = 0L;
    Long curTime = 0L;
    Long lastTime = 0L;
    Float deltat = 0f;

    Double curSpeedx = 0.0;
    Double lastSpeedx = 0.0;
    Double curSpeedy = 0.0;
    Double lastSpeedy = 0.0;
    Double curSpeedz = 0.0;
    Double lastSpeedz = 0.0;

    Double xLocPrev = 0.0, yLocPrev = 0.0, zLocPrev = 0.0;



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


        x = Math.floor(sensorEvent.values[0] * 10) / 10;
        y = Math.floor(sensorEvent.values[1] * 10) / 10;
        z = Math.floor(sensorEvent.values[2] * 10) / 10;

        if (x <= 0.1 && x >= -0.1){
            x = 0d;
        }
        if (y <= 0.1 && y >= -0.1){
            y = 0d;
        }
        if (z <= 0.1 && z >= -0.1){
            z = 0d;
        }

        lastTime = curTime;
        curTime = System.currentTimeMillis() - startTime;

        deltat =  new Float(curTime - lastTime) / 1000;

        lastSpeedx = curSpeedx;
        curSpeedx = lastSpeedx + (x * deltat);
        lastSpeedy = curSpeedy;
        curSpeedy = lastSpeedy + (y * deltat);
        lastSpeedz = curSpeedz;
        curSpeedz = lastSpeedz + (z * deltat);

        Double xLoc, yLoc, zLoc, deltaXLoc, deltaYLoc, deltaZLoc, xSpeed;


        deltaXLoc = (lastSpeedx * deltat) + (x * deltat * deltat / 2);
        deltaYLoc = (lastSpeedy * deltat) + (y * deltat * deltat / 2);
        deltaZLoc = (lastSpeedz * deltat) + (z * deltat * deltat / 2);

        xLoc = xLocPrev + deltaXLoc;
        xLocPrev = xLoc;
        yLoc = yLocPrev + deltaYLoc;
        yLocPrev = yLoc;
        zLoc = zLocPrev + deltaZLoc;
        zLocPrev = zLoc;




        record = record + "{\"xAccl\":" + Double.toString(x) + ", \"yAccl\":"
                                           + Double.toString(y) + ", \"zAccl\":"
                                           + Double.toString(z) + ", \"xSpeed\":"
                                           + Double.toString(curSpeedx) + ", \"xLoc\":"
                                           + Double.toString(xLoc) + ", \"yLoc\":"
                                           + Double.toString(yLoc)+ ", \"zLoc\":"
                                           + Double.toString(zLoc)+ ", \"time\":"
                                           + Long.toString(curTime) + ", \"delta_t\":"
                                           + Float.toString(deltat) +
                                           "},\n";

        String xString = "xAccl: " + Double.toString(x);
        String yString = "yAccl: " + Double.toString(y);
        String zString = "zAccl: " + Double.toString(z);

        xAccl.setText(xString);
        yAccl.setText(yString);
        zAccl.setText(zString);


    }

    public File getPrivateStorageDir(Context context, String dirname) {
        // Get the directory for the app's private pictures directory.
        File file = new File(context.getExternalFilesDir(
                Environment.DIRECTORY_DOCUMENTS), dirname);
        if (!file.mkdirs()) {
            Log.e(TAG, "Directory not created");
        }
        return file;
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

            try {
                File filename = new File(this.getFilesDir(),"sensorData2.txt");
                FileWriter fw = new FileWriter(filename, false);
                fw.write(new String(new char[100000]).replace("\0", "      \n"));
                fw.flush();
            }catch(Exception e){
                e.printStackTrace();
            }


        // "Hit Stop Recording"
        }else{
            sensorManager.unregisterListener(this,accelerometer);


            xAccl.setText("xAccl: ----");
            yAccl.setText("yAccl: ----");
            zAccl.setText("zAccl: ----");


            File filename = new File(this.getFilesDir(),"sensorData2.txt");
            String fileContents = record + "]";
            FileOutputStream outputStream;



            try {


                FileWriter fw = new FileWriter(filename, false);
                zAccl.setText(filename.getAbsolutePath());
                fw.write(fileContents);
                fw.flush();

                /*
                outputStream = openFileOutput(filename, );
                outputStream.write(fileContents.getBytes());
                outputStream.close();*/



            } catch (Exception e) {
                e.printStackTrace();
            }




            //Use this to view the files in the working directory.
            /*


            PackageManager m = getPackageManager();
            String s = getPackageName();
            String x = "";
            String dir = "";
            String extdir = "";


            try {



                PackageInfo p = m.getPackageInfo(s, 0);
                dir = s = p.applicationInfo.dataDir;
                File directory = new File(s);
                File[] files = directory.listFiles();

                for (int i=0; i < files.length; i++)
                {
                    x = x + "\n" + files[i].getName();
                }

            } catch (PackageManager.NameNotFoundException e) {
                Log.w("yourtag", "Error Package name not found ", e);
            }

            x = x + "\n" + dir + "\n" + extdir;

            */



        }

    }



}

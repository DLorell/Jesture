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
import android.widget.Toast;
import android.content.ContentResolver;
import android.net.Uri;
import android.provider.MediaStore;



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

    private float[] rMatrix = new float[9];





    private SensorManager sensorManager;
    Sensor accelerometer;
    Sensor gravity;
    Sensor rotationVector;

    int gravInitialized;
    int rotVecInitialized;
    float[] initialRotVec = new float[3];

    Double[] accelVec = new Double[3];
    Double[] gravVec = new Double[3];
    float[] rotVec = new float[3];




    TextView xAccl, yAccl, zAccl, txtYaw, txtPitch, txtRoll, numTraces;
    ToggleButton toggle, testButton;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);



        toggle = (ToggleButton) findViewById(R.id.toggle);
        toggle.setOnClickListener(this);
        testButton = (ToggleButton) findViewById(R.id.testButton);
        testButton.setOnClickListener(this);


        xAccl = (TextView) findViewById(R.id.xAccl);
        yAccl = (TextView) findViewById(R.id.yAccl);
        zAccl = (TextView) findViewById(R.id.zAccl);
        txtYaw = (TextView) findViewById(R.id.Yaw);
        txtPitch = (TextView) findViewById(R.id.Pitch);
        txtRoll = (TextView) findViewById(R.id.Roll);
        numTraces = (TextView) findViewById(R.id.numTraces);

        xAccl.setText("xAccl: ----");
        yAccl.setText("yAccl: ----");
        zAccl.setText("zAccl: ----");
        txtYaw.setText("Yaw: ----");
        txtRoll.setText("Roll: ----");
        txtPitch.setText("Pitch: ----");
        numTraces.setText("Number of Saved Gesture Traces: " + Integer.toString(this.getFilesDir().listFiles().length - 1));



        Log.d(TAG, "onCreate: Initializing Sensor Services.");
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);


        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        gravity = sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY);
        rotationVector = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);

        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);


        //if (sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY) != null) {
        //    Toast.makeText(this, "Gravity AVAILABLE", Toast.LENGTH_SHORT).show();
        //} else {
        //    // Failure! No Gravity Sensor.
        //    Toast.makeText(this, "Failure! No Gravity Sensor", Toast.LENGTH_SHORT).show();
        //}

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
        txtYaw.setText("Yaw: ----");
        txtRoll.setText("Roll: ----");
        txtPitch.setText("Pitch: ----");
        numTraces.setText("Number of Saved Gesture Traces: " + Integer.toString(this.getFilesDir().listFiles().length - 1));

        toggle.setChecked(false);
    }


    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {


    }



    public void calculateAngles(float[] result, float[] rVector){
        //caculate rotation matrix from rotation vector first
        SensorManager.getRotationMatrixFromVector(rMatrix, rVector);

        //calculate Euler angles now
        SensorManager.getOrientation(rMatrix, result);

        //The results are in radians, need to convert it to degrees
        convertToDegrees(result);
    }

    private void convertToDegrees(float[] vector){
        for (int i = 0; i < vector.length; i++){
            vector[i] = -Math.round(Math.toDegrees(vector[i]));
        }
    }



    //Make sure accelerationVector is of dimensions 3X1
    public float[] normalizeAccelVecOrientation(float[] yrp, double[][] accelerationVector){
        double[][] orientation = new double[3][1];
        float[] finalVec = new float[3];


        //All in degrees
        // Yaw: [-180,180]. 0 is original (~north), 180/-180 is ~south, - is east, + is west
        float yaw = yrp[0];
        // Pitch [-180,180]. 0 is original (face up), 180/-180 is face down
        float pitch = yrp[2];
        // Roll: [-90,90]. 0 is original (flat,face up/down), 90 is vertical RightSideUp, -90 is vertical RSD
        float roll = yrp[1];

        //Turn positive
        if (yaw < 0) {
            yaw = 360 + yaw;
        }
        if(pitch < 0){
            pitch = 360 + pitch;
        }
        if(roll < 0){
            roll = 180 + roll;
        }

        //THESE MUST BE IN RADIANS. Also, these are the CORRECTIONS. So these are the angles to adjust by, not the raw YRP
        double yTheta, pTheta, rTheta;
        yTheta = Math.toRadians(-yaw);
        pTheta = Math.toRadians(-pitch);
        rTheta = Math.toRadians(-roll);



        //The Yaw rotation matrix
        double[][] Rz = {{Math.cos(yTheta), -Math.sin(yTheta), 0},
                         {Math.sin(yTheta), Math.cos(yTheta),  0},
                         {0,                0,                 1}};

        //The Pitch rotation
        double[][] Ry = {{Math.cos(pTheta), 0,  Math.sin(pTheta)},
                         {0,                1,                 0},
                         {-Math.sin(pTheta),0,  Math.cos(pTheta)}};
        //The Roll rotation
        double[][] Rx = {{1,                0,                 0},
                         {0, Math.cos(rTheta), -Math.sin(rTheta)},
                         {0, Math.sin(rTheta), Math.cos(rTheta)}};


        double[][] RMat = matMult(matMult(Rz, Ry), Rx);


        orientation = matMult(RMat, accelerationVector);


        for(int i = 0; i < orientation.length; i++){
            for(int j = 0; j < orientation[0].length; j++){
                finalVec[i] = (float) orientation[i][j];
            }
        }

        //DEBUGING -------------------------------






        // ---------------------------------------


        return(finalVec);
    }


    private double[][] matMult(double[][] mat1, double[][] mat2){
        int rows, columns;

        if (mat1 == null || mat2 == null){
            //Bad
            System.exit(1);
        }

        //check that the operation is legal

        if (mat1[0].length != mat2.length){
            //Illegal!
            System.exit(1);
        }


        rows = mat1.length;
        columns = mat2[0].length;
        double[][] newMat = new double[rows][columns];

        for (int i = 0; i < rows; i++){

            for (int j = 0; j < columns; j++){

                for (int k = 0; k < mat1[0].length; k++){

                    newMat[i][j] += mat1[i][k] * mat2[k][j];
                }
            }
        }

        return(newMat);
    }


    public static boolean delete(File path) {
        boolean result = true;
        if (path.exists()) {
            if (path.isDirectory()) {
                for (File child : path.listFiles()) {
                    result &= delete(child);
                }
                result &= path.delete(); // Delete empty directory.
            }
            if (path.isFile()) {
                result &= path.delete();
            }
            if (!result) {
                Log.e("Delete", "Delete failed;");
            }
            return result;
        } else {
            Log.e("Delete", "File does not exist.");
            return false;
        }
    }


    public static void deleteFileFromMediaStore(final ContentResolver contentResolver, final File file) {
        int sdk = android.os.Build.VERSION.SDK_INT;
        if (sdk >= android.os.Build.VERSION_CODES.HONEYCOMB) {
            String canonicalPath;
            try {
                canonicalPath = file.getCanonicalPath();
            } catch (IOException e) {
                canonicalPath = file.getAbsolutePath();
            }
            final Uri uri = MediaStore.Files.getContentUri("external");
            final int result = contentResolver.delete(uri,
                    MediaStore.Files.FileColumns.DATA + "=?", new String[]{canonicalPath});
            if (result == 0) {
                final String absolutePath = file.getAbsolutePath();
                if (!absolutePath.equals(canonicalPath)) {
                    contentResolver.delete(uri,
                            MediaStore.Files.FileColumns.DATA + "=?", new String[]{absolutePath});
                }
            }
        }
    }

    public void hardcoreDelete(File file){

        ContentResolver context = getContentResolver();

        delete(file);
        deleteFileFromMediaStore(context, file);
    }





    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {



        if(sensorEvent.sensor.getType() == Sensor.TYPE_ROTATION_VECTOR){

            calculateAngles(rotVec, sensorEvent.values);


            if (rotVecInitialized == 0){
                rotVecInitialized = 1;
                initialRotVec[0] = rotVec[0];
                initialRotVec[1] = rotVec[1];
                initialRotVec[2] = rotVec[2];
            }

            /*
            rotVec[0] -= initialRotVec[0];
            rotVec[1] -= initialRotVec[1];
            rotVec[2] -= initialRotVec[2];
            */


            String yStr = "Yaw: " + rotVec[0];
            String rStr = "Roll: " + rotVec[1];
            String pStr = "Pitch: " + rotVec[2];


            txtYaw.setText(yStr);
            txtRoll.setText(rStr);
            txtPitch.setText(pStr);


        }


        if(sensorEvent.sensor.getType() == Sensor.TYPE_GRAVITY) {

            if(gravInitialized == 0){
                gravInitialized = 1;
            }

            gravVec[0] = Math.floor(sensorEvent.values[0] * 10) / 10;
            gravVec[1] = Math.floor(sensorEvent.values[1] * 10) / 10;
            gravVec[2] = Math.floor(sensorEvent.values[2] * 10) / 10;

        }

        if(sensorEvent.sensor.getType() == Sensor.TYPE_ACCELEROMETER){



            double[][] linearAccl = new double[3][1];
            linearAccl[0][0] = (double) sensorEvent.values[0];
            linearAccl[1][0] = (double) sensorEvent.values[1];
            linearAccl[2][0] = (double) sensorEvent.values[2];

            float[] values = normalizeAccelVecOrientation(rotVec, linearAccl);


            Log.d(TAG, "onSensorChanged: X: " + values[0] + "  Y: " + values[1] + "  Z: " + values[2]);

            /*
            x = Math.floor(values[0] * 10) / 10;
            y = Math.floor(values[1] * 10) / 10;
            z = Math.floor(values[2] * 10) / 10;
            */


            if(gravInitialized == 1) {
                x = Math.floor((sensorEvent.values[0] - gravVec[0]) * 1000) / 1000;
                y = Math.floor((sensorEvent.values[1] - gravVec[1]) * 1000) / 1000;
                z = Math.floor((sensorEvent.values[2] - gravVec[2]) * 1000) / 1000;
            }else{
                x = 0d;
                y = 0d;
                z = 0d;
            }


            accelVec[0] = x;
            accelVec[1] = y;
            accelVec[2] = z;



            if (x <= 0.1 && x >= -0.1) {
                x = 0d;
            }
            if (y <= 0.1 && y >= -0.1) {
                y = 0d;
            }
            if (z <= 0.1 && z >= -0.1) {
                z = 0d;
            }

            lastTime = curTime;
            curTime = System.currentTimeMillis() - startTime;

            deltat = new Float(curTime - lastTime) / 1000;

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


            String initialRotString;
            if (rotVecInitialized == 1){

                initialRotString = "[" + Float.toString(initialRotVec[0]) + ", "
                                              + Float.toString(initialRotVec[1]) + ", "
                                              + Float.toString(initialRotVec[2]) + "]";
            }else{
                initialRotString = "N/A";
            }


            double x1 = Math.floor(sensorEvent.values[0] * 1000) / 1000;
            double y1 = Math.floor(sensorEvent.values[1] * 1000) / 1000;
            double z1 = Math.floor(sensorEvent.values[2] * 1000) / 1000;
            String rawAcclVecStr = "[" + Double.toString(x1) + ", " + Double.toString(y1) + ", " + Double.toString(z1) + "]";
            String acclVecStrWOGrav = "[" + Double.toString(x) + ", " + Double.toString(y) + ", " + Double.toString(z) + "]";
            String rotVecStr = "[" + Float.toString(rotVec[0]) + ", " + Float.toString(rotVec[1]) + ", " + Float.toString(rotVec[2]) + "]";



            record = record + "{\"xAccl\":" + Double.toString(x)
                            + ", \"yAccl\":" + Double.toString(y)
                            + ", \"zAccl\":" + Double.toString(z)
                            + ", \"yaw\":" + Double.toString(rotVec[0])
                            + ", \"roll\":" + Double.toString(rotVec[1])
                            + ", \"pitch\":" + Double.toString(rotVec[2])
                            + ", \"xLoc\":"  + Double.toString(xLoc)
                            + ", \"yLoc\":"  + Double.toString(yLoc)
                            + ", \"zLoc\":"  + Double.toString(zLoc)
                            + ", \"time\":"  + Long.toString(curTime)
                            + ", \"delta_t\":" + Float.toString(deltat)
                            + ", \"xVel\":"+ Double.toString(curSpeedx)
                            + ", \"yVel\":"+ Double.toString(curSpeedy)
                            + ", \"zVel\":"+ Double.toString(curSpeedz)
                            + ", \"rawAcclVec\": "+ rawAcclVecStr
                            + ", \"initRotVec\": "+ initialRotString
                            + "},\n";


            //record = record + "[" + ogacclVecStr + ", " + initialRotString + ", " + rotVecStr + ", " + acclVecStr + "]\n";

            String xString = "xAccl: " + Double.toString(x);
            String yString = "yAccl: " + Double.toString(y);
            String zString = "zAccl: " + Double.toString(z);


            xAccl.setText(xString);
            yAccl.setText(yString);
            zAccl.setText(zString);

        }




    }

    // This gets called every time you click a button.
    @Override
    public void onClick(View v) {

        switch (v.getId()){

            case R.id.toggle:

                boolean on = toggle.isChecked();
                TextView text;

                // "Hit record"
                if(on) {
                    startTime = System.currentTimeMillis();

                    rotVecInitialized = 0;
                    gravInitialized = 0;
                    initialRotVec = new float[3];
                    record = "[";


                    x = 0.0;
                    y = 0.0;
                    z = 0.0;
                    curTime = 0L;
                    lastTime = 0L;
                    deltat = 0f;

                    curSpeedx = 0.0;
                    lastSpeedx = 0.0;
                    curSpeedy = 0.0;
                    lastSpeedy = 0.0;
                    curSpeedz = 0.0;
                    lastSpeedz = 0.0;

                    xLocPrev = 0.0;
                    yLocPrev = 0.0;
                    zLocPrev = 0.0;


                    sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI);
                    sensorManager.registerListener(this, gravity, SensorManager.SENSOR_DELAY_UI);
                    sensorManager.registerListener(this, rotationVector, SensorManager.SENSOR_DELAY_UI);



                    // "Hit Stop Recording"
                }else{
                    sensorManager.unregisterListener(this,accelerometer);
                    sensorManager.unregisterListener(this,gravity);
                    sensorManager.unregisterListener(this,rotationVector);



                    //new File(this.getFilesDir(), "sensorData2.txt").delete();

                    File[] files = this.getFilesDir().listFiles();
                    String filename = "gestureTrace" + Integer.toString(files.length) + ".txt";

                    if(new File(this.getFilesDir(), filename).exists()){
                        new File(this.getFilesDir(), filename).delete();
                    }

                    String fileContents = record + "]";
                    FileOutputStream outputStream;

                    try {
                        outputStream = openFileOutput(filename, Context.MODE_PRIVATE);
                        outputStream.write(fileContents.getBytes());
                        outputStream.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    xAccl.setText("xAccl: ----");
                    yAccl.setText("yAccl: ----");
                    zAccl.setText("zAccl: ----");
                    txtYaw.setText("Yaw: ----");
                    txtRoll.setText("Roll: ----");
                    txtPitch.setText("Pitch: ----");
                    numTraces.setText("Number of Saved Gesture Traces: " + Integer.toString(this.getFilesDir().listFiles().length - 1));


                }


                break;

            case R.id.testButton:

                boolean onn = testButton.isChecked();

                if(onn){
                    sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI);
                    sensorManager.registerListener(this, gravity, SensorManager.SENSOR_DELAY_UI);
                    sensorManager.registerListener(this, rotationVector, SensorManager.SENSOR_DELAY_UI);
                }else{
                    sensorManager.unregisterListener(this,accelerometer);
                    sensorManager.unregisterListener(this,gravity);
                    sensorManager.unregisterListener(this,rotationVector);

                    xAccl.setText("xAccl: ----");
                    yAccl.setText("yAccl: ----");
                    zAccl.setText("zAccl: ----");
                    txtYaw.setText("Yaw: ----");
                    txtRoll.setText("Roll: ----");
                    txtPitch.setText("Pitch: ----");
                    numTraces.setText("Number of Saved Gesture Traces: " + Integer.toString(this.getFilesDir().listFiles().length - 1));
                }

                break;

        }



    }



}

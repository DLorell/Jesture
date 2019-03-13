package edu.uchicago.lorell;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.Toast;


import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

public class ConnectToServerAsyncTask extends AsyncTask<String,String,String> implements SensorEventListener {

    String gLabel = "";
    private static final String TAG = "ConnectToServerAsync";
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







    public ConnectToServerAsyncTask(Context ctx) {
        this.context = ctx;

        sensorManager = (SensorManager) ctx.getSystemService(Context.SENSOR_SERVICE);
        //Define the sensors
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        gravity = sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY);
        rotationVector = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);

    }



    // SOCKET CODE BELOW ------------------------------------------------------------------
    //-------------------------------------------------------------------------------------

    Context context;
    Socket socket;
    OutputStream socketOut;
    //boolean running = true;



    @Override
    protected String doInBackground(String... strings) {
        String IP = strings[0];
        String port = strings[1];
        InputStream is = null;
        String data = "Hello";
        try {

            startTime = System.currentTimeMillis();
            rotVecInitialized = 0;
            gravInitialized = 0;
            initialRotVec = new float[3];
            record = "";

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

            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_FASTEST);
            sensorManager.registerListener(this, gravity, SensorManager.SENSOR_DELAY_FASTEST);
            sensorManager.registerListener(this, rotationVector, SensorManager.SENSOR_DELAY_FASTEST);





            InetAddress inetIP = InetAddress.getByName(IP);
            socket = new Socket(inetIP, Integer.parseInt(port));
            socketOut = socket.getOutputStream();
            publishProgress("test");

            socketOut.write("[".getBytes("UTF-8"));
            while (!this.isCancelled()) {
                socketOut.write(record.getBytes("UTF-8"));
                record = "";
                Thread.sleep(1);
            }


        } catch (UnknownHostException e) {
            publishProgress("Exception");
            e.printStackTrace();
        } catch (IOException e) {
            publishProgress("Exception2");
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
         }
        return null;
    }

    @Override
    protected void onProgressUpdate(String... values) {
        super.onProgressUpdate(values);
        String message = values[0];
        Toast.makeText(this.context, message, Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onCancelled() {
        super.onCancelled();
        try {
            socketOut.write(record.getBytes("UTF-8"));
            socketOut.flush();
            socketOut.close();
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        sensorManager.unregisterListener(this, accelerometer);
        sensorManager.unregisterListener(this, gravity);
        sensorManager.unregisterListener(this, rotationVector);
        gravInitialized = 0;
        rotVecInitialized = 0;

    }


    // SOCKET CODE ABOVE ------------------------------------------------------------------
    //-------------------------------------------------------------------------------------



    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) { }


    @SuppressLint("UseValueOf")
    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {

        /*

        if(sensorEvent.sensor.getType() == Sensor.TYPE_ROTATION_VECTOR){


            calculateAngles(rotVec, sensorEvent.values);


            if (rotVecInitialized == 0){
                rotVecInitialized = 1;
                initialRotVec[0] = rotVec[0];
                initialRotVec[1] = rotVec[1];
                initialRotVec[2] = rotVec[2];
            }


            rotVec[0] -= initialRotVec[0];
            rotVec[1] -= initialRotVec[1];
            rotVec[2] -= initialRotVec[2];



            String yStr = "Yaw: " + rotVec[0];
            String rStr = "Roll: " + rotVec[1];
            String pStr = "Pitch: " + rotVec[2];


            txtYaw.setText(yStr);
            txtRoll.setText(rStr);
            txtPitch.setText(pStr);


        }*/


        if(sensorEvent.sensor.getType() == Sensor.TYPE_GRAVITY) {

            if(gravInitialized == 0){
                gravInitialized = 1;
            }

            gravVec[0] = Math.floor(sensorEvent.values[0] * 10) / 10;
            gravVec[1] = Math.floor(sensorEvent.values[1] * 10) / 10;
            gravVec[2] = Math.floor(sensorEvent.values[2] * 10) / 10;

        }

        if(sensorEvent.sensor.getType() == Sensor.TYPE_ACCELEROMETER){



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
                    + "}\n";


        }




    }




}

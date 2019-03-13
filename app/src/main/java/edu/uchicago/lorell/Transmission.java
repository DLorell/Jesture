package edu.uchicago.lorell;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.io.BufferedWriter;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;


// I've commented this pretty thoroughly. As you will see, there is a clear place for you to put
// the transmission code, it's in a method.


public class Transmission extends AppCompatActivity implements View.OnClickListener {

    //Global Variables
    EditText port, ipAdr;
    ToggleButton transmission;
    Boolean isTransmitting = Boolean.FALSE;


    public int PORT = 15000;
    private String serverIpAddress = "10.0.0.5";

    public class ClientThread implements Runnable {
        Socket socket;
        public void run() {
            try {
                PORT = Integer.parseInt(port.getText().toString());
                serverIpAddress=ipAdr.getText().toString();
                InetAddress serverAddr = InetAddress.getByName(serverIpAddress);
                socket = new Socket(serverAddr, PORT);
                PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);
                while (isTransmitting) {
                    out.printf("%10.2f\n", x);
                    out.flush();
                    Thread.sleep(2);
                }
            }
            catch (Exception e) {

            }
            finally{
                try {
                    isTransmitting=false;
                    transmission.setText("Start Streaming");
                    //out.close();
                    socket.close();
                }catch(Exception a){
                }
            }
        }
    };

    // Stuff that should happen as soon as we enter the "Transmission" activity. Initializations
    // and the like. Make sure not to put anything before the setContentView() method.
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transmission);

        // Initialize the "Transmit" toggle button. Should initialize as "inactive."
        transmission = (ToggleButton) findViewById(R.id.transmission);
        transmission.setOnClickListener(this);

        // Initialize the "port" and "ipAdr" text input fields. Defaults hardcoded here.
        // Can be edited, live, in-app.
        port = (EditText) findViewById(R.id.port);
        ipAdr = (EditText) findViewById(R.id.ipAdr);

        port.setText("8888");
        ipAdr.setText("192.168.1.42");
    }



    // Don't worry about this. This just calls the transmissionControl method when
    // the "Transmit" button is toggled.
    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.transmission){
            transmissionControl(v);
        }
    }



    // Here's the good stuff. Make the magic happen in here.
    public void transmissionControl(View view) {

        // isTransmitting is the boolean value of the state of the "Transmit" toggle button.
        // portStr/ipAdrStr are the strings that are showing in the editable Port and IP fields in
        // the activity.      E.x. portSrt == "8888" and ipAdrStr == "192.168.1.42"
        isTransmitting = transmission.isChecked();
        String portStr = port.getText().toString();
        String ipAdrStr = ipAdr.getText().toString();

        if(!isTransmitting){
            if (!serverIpAddress.equals("")) {
                Thread cThread = new Thread(new ClientThread());
                cThread.start();
            }
        }else{
            // "Transmit" button has been deactivated. Stop the transmission stuff?

        }
    }
}

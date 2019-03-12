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


// I've commented this pretty thoroughly. As you will see, there is a clear place for you to put
// the transmission code, it's in a method.


public class Transmission extends AppCompatActivity implements View.OnClickListener {

    //Global Variables
    EditText port, ipAdr;
    ToggleButton transmission;
    Boolean isTransmitting = Boolean.FALSE;



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

        if(isTransmitting){
            // "Transmit" button has been toggled. Do transmission stuff?
            // TODO
        }else{
            // "Transmit" button has been deactivated. Stop the transmission stuff?
            // TODO
        }
    }
}

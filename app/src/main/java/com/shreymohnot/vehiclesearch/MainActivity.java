package com.shreymohnot.vehiclesearch;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class MainActivity extends Activity {
    Button sendBtn;
    EditText txtRegN;
    String phoneNo;
    String message;
    ProgressDialog progress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sendBtn = (Button) findViewById(R.id.btnSend);
        txtRegN = (EditText) findViewById(R.id.txtRegN);

        sendBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                if (txtRegN.getText().toString().trim().length() >= 5)
                    checkMessage();
                else {
                    txtRegN.requestFocus();
                    Toast.makeText(getApplicationContext(), "Please Enter Valid Number", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    protected void checkMessage() {
        progress = new ProgressDialog(MainActivity.this);
        progress.setMax(100);
        progress.setMessage("Sending SMS...");
        progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progress.show();
        new Thread() {
            @Override
            public void run() {
                super.run();
                try {
                    sendSMSMessage();
                    Thread.sleep(400);
                    if (progress.isShowing()) {
                        progress.dismiss();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }

    public void onBackPressed() {
        final AlertDialog.Builder aobj = new AlertDialog.Builder(MainActivity.this);
        aobj.setTitle("Exit");
        aobj.setMessage("Are you sure?");
        aobj.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        });
        aobj.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        AlertDialog alertDialog = aobj.create();
        alertDialog.show();
    }

    protected void sendSMSMessage() {
        phoneNo = "7738299899";
        message = txtRegN.getText().toString().trim();
        message = "VAHAN " + message;

        String SMS_SENT = "SMS_SENT";
        String SMS_DELIVERED = "SMS_DELIVERED";
        PendingIntent sentPendingIntent = PendingIntent.getBroadcast(this, 0, new Intent(SMS_SENT), 0);
        PendingIntent deliveredPendingIntent = PendingIntent.getBroadcast(this, 0, new Intent(SMS_DELIVERED), 0);

        // For when the SMS has been sent
        registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                switch (getResultCode()) {
                    case Activity.RESULT_OK:
                        Toast.makeText(context, "SMS sent successfully", Toast.LENGTH_SHORT).show();
                        break;
                    case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
                        Toast.makeText(context, "Generic failure cause", Toast.LENGTH_SHORT).show();
                        break;
                    case SmsManager.RESULT_ERROR_NO_SERVICE:
                        Toast.makeText(context, "Service is currently unavailable", Toast.LENGTH_SHORT).show();
                        break;
                    case SmsManager.RESULT_ERROR_RADIO_OFF:
                        Toast.makeText(context, "Radio turned off", Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        }, new IntentFilter(SMS_SENT));

        // For when the SMS has been delivered
        registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                switch (getResultCode()) {
                    case Activity.RESULT_OK:
                        Toast.makeText(getBaseContext(), "SMS delivered", Toast.LENGTH_SHORT).show();
                        break;
                    case Activity.RESULT_CANCELED:
                        Toast.makeText(getBaseContext(), "SMS not delivered", Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        }, new IntentFilter(SMS_DELIVERED));

        // Get the default instance of SmsManager
        SmsManager smsManager = SmsManager.getDefault();
        // Send a text based SMS
        smsManager.sendTextMessage(phoneNo, null, message, sentPendingIntent, deliveredPendingIntent);
    }
}
package com.shreymohnot.vehiclesearch;

import android.Manifest;
import android.app.Activity;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.telephony.SmsManager;
import android.telephony.TelephonyManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;


public class MainActivity extends AppCompatActivity {
    public static final String SMS_SENT = "SMS_SENT";
    public static final String SMS_DELIVERED = "SMS_DELIVERED";
    private static final int MY_PERMISSIONS_REQUEST_SEND_SMS = 101;
    Button sendBtn;
    EditText txtRegN;
    String phoneNo;
    String message;
    String excptn;
    ProgressDialog pBar;
    private final BroadcastReceiver smsReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            pBar.dismiss();
            if (intent.getAction().equals(SMS_SENT)) {
                switch (getResultCode()) {
                    case Activity.RESULT_OK:
                        Toast.makeText(context, "SMS sent successfully", Toast.LENGTH_SHORT).show();
                        sendBtn.setEnabled(false);
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
            } else {
                if (intent.getAction().equals(SMS_DELIVERED)) {
                    switch (getResultCode()) {
                        case Activity.RESULT_OK:
                            Toast.makeText(getBaseContext(), "SMS delivered", Toast.LENGTH_SHORT).show();
                            txtRegN.setText("");
                            sendBtn.setEnabled(false);
                            break;
                        case Activity.RESULT_CANCELED:
                            Toast.makeText(getBaseContext(), "SMS not delivered", Toast.LENGTH_SHORT).show();
                            break;
                    }
                }
            }
        }
    };
    Tracker t;

    @Override
    protected void onStart() {
        try {
            registerReceiver(smsReceiver, new IntentFilter(SMS_SENT));
            registerReceiver(smsReceiver, new IntentFilter(SMS_DELIVERED));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        super.onStart();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        try {
            t = ((GAnalytics) getApplication()).getDefaultTracker();
            t.setScreenName(this.getClass().getSimpleName());
            t.send(new HitBuilders.ScreenViewBuilder().build());
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        pBar = new ProgressDialog(MainActivity.this) {
            @Override
            public void onBackPressed() {
                pBar.dismiss();
            }
        };

        pBar.setMessage("Sending SMS");
        pBar.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        pBar.setIndeterminate(true);

        sendBtn = (Button) findViewById(R.id.btnSend);
        txtRegN = (EditText) findViewById(R.id.txtRegN);

        sendBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                if (txtRegN.getText().toString().replaceAll(" ", "").trim().length() >= 5) {
                    checkSimExist();
                }
                else {
                    Toast.makeText(getApplicationContext(), "Please Enter Valid Number", Toast.LENGTH_LONG).show();
                    txtRegN.requestFocus();
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.help:
                Intent intent = new Intent(MainActivity.this, HelpActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.r2l_in, R.anim.r2l_out);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void checkSimExist() {
        TelephonyManager telMgr = (TelephonyManager) getApplicationContext().getSystemService(Context.TELEPHONY_SERVICE);
        if (telMgr.getSimState() != TelephonyManager.SIM_STATE_ABSENT) {
            checkMessage();
        } else {
            Toast.makeText(getApplicationContext(), "Please Insert SIM Card", Toast.LENGTH_LONG).show();
        }
    }

    protected void checkMessage() {
        if (Build.VERSION.SDK_INT >= 23) {
            int checkCallPhonePermission = ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS);
            if (checkCallPhonePermission != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.SEND_SMS}, MY_PERMISSIONS_REQUEST_SEND_SMS);
            } else {
                sendSMSMessage();
            }
        } else {
            sendSMSMessage();
        }
    }

    protected void sendSMSMessage() {
        phoneNo = "7738299899";
        message = txtRegN.getText().toString().replaceAll(" ", "").trim();
        message = "Vahan " + message;

        pBar.show();
        pBar.setCancelable(false);

        PendingIntent sentPendingIntent = PendingIntent.getBroadcast(this, 0, new Intent(SMS_SENT), 0);
        PendingIntent deliveredPendingIntent = PendingIntent.getBroadcast(this, 0, new Intent(SMS_DELIVERED), 0);

        try {
            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(phoneNo, null, message, sentPendingIntent, deliveredPendingIntent);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                sendBtn.setEnabled(true);
            }
        }, 10000);
    }

    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_SEND_SMS:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    sendSMSMessage();
                } else {
                    Toast.makeText(getApplicationContext(), "SMS Denied", Toast.LENGTH_SHORT).show();
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    @Override
    protected void onStop() {
        unregisterReceiver(smsReceiver);
        super.onStop();
    }

    @Override
    public void onBackPressed() {
        finish();
    }
}
package com.ngm.bussnisscard;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.telephony.SmsManager;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

public class SendActivity extends AppCompatActivity {

    LinearLayout mainLayout;
    Button sendWhatsapp, sendSms;
    String phoneNumber = "";
    String messageBody = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send);

        phoneNumber = this.getIntent().getExtras().getString("phone_number");
        messageBody = this.getIntent().getExtras().getString("message");

        mainLayout = findViewById(R.id.main_layout);
        sendSms = findViewById(R.id.messageButton);
        sendWhatsapp = findViewById(R.id.whatsappButton);

        //Set click/ Youch events listeners
        mainLayout.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                finish();
                // Return false, then android os will still process click event,
                // if return true, the on click listener will never be triggered.
                return false;
            }
        });

        sendSms.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(phoneNumber == null || phoneNumber == ""){
                    Toast.makeText(getApplicationContext(), "Phone NUmber not found", Toast.LENGTH_LONG).show();
                    return;
                }

                CommonMethods.sendSms(SendActivity.this, phoneNumber, messageBody);
                finish();
            }
        });

        sendWhatsapp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(phoneNumber == null || phoneNumber == ""){
                    Toast.makeText(getApplicationContext(), "Phone NUmber not found", Toast.LENGTH_LONG).show();
                    return;
                }
                CommonMethods.openWhatsApp(SendActivity.this, phoneNumber, messageBody );
            }
        });

    }

}
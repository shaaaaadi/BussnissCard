package com.ngm.bussnisscard;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.os.Bundle;
import android.provider.CallLog;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.w3c.dom.Text;

public class ContactSelectorActivity extends AppCompatActivity {

    Contact last_in, last_out, last_missed;
    TextView lastInCallTV,  lastOutCallTV, lastMissedCallTV;
    Button getContactButton;

    String messageType, message;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_selector);

        messageType = getIntent().getStringExtra("message_type");
        message = CommonMethods.getUserMessage();

        Log.d("LastCall", "Calling Incomming");
        last_in = CommonMethods.getLastCall(this, CallLog.Calls.INCOMING_TYPE);
        Log.d("LastCall", "Calling Outgoing");
        last_out = CommonMethods.getLastCall(this, CallLog.Calls.OUTGOING_TYPE);
        Log.d("LastCall", "Calling Missed");
        last_missed = CommonMethods.getLastCall(this, CallLog.Calls.MISSED_TYPE);

        lastInCallTV = findViewById(R.id.last_incoming_call);
        lastOutCallTV = findViewById(R.id.last_outgoing_call);
        lastMissedCallTV = findViewById(R.id.last_missed_call);
        getContactButton = findViewById(R.id.btn_get_contact);

        String noName = "ללא שם";
        lastInCallTV.setText(String.format("%s - %s", last_in.PhoneNumber, last_in.Name != null ? last_in.Name : noName));
        lastOutCallTV.setText(String.format("%s - %s", last_out.PhoneNumber, last_out.Name != null ? last_out.Name : noName));
        lastMissedCallTV.setText(String.format("%s - %s", last_missed.PhoneNumber, last_missed.Name != null ? last_missed.Name : noName));

        lastInCallTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendMessage(view.getContext(), last_in.PhoneNumber, messageType);
            }
        });

        lastOutCallTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendMessage(view.getContext(), last_out.PhoneNumber, messageType);
            }
        });

        lastMissedCallTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendMessage(view.getContext(), last_missed.PhoneNumber, messageType);
            }
        });

    }

    void sendMessage(Context ctx, String phoneNumber, String messageType){
        if(messageType.toLowerCase().equals("whatsapp")){
            CommonMethods.openWhatsApp(ctx, phoneNumber, message);
            finish();
        }
        else if (messageType.toLowerCase().equals("standard")){
            CommonMethods.sendSms(ctx, phoneNumber, message);
            finish();
        }
    }

}
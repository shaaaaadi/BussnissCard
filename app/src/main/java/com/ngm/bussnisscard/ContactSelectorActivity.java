package com.ngm.bussnisscard;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.CallLog;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;

public class ContactSelectorActivity extends AppCompatActivity {
    static final int LAST_CALLS_COUNT = 10;

    Contact last_in, last_out, last_missed;
    Button getContactButton;
    ListView missedListView, incomingListView, outgoingListView;

    String messageType, message;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_selector);

        messageType = getIntent().getStringExtra("message_type");
        message = CommonMethods.getUserMessage();


        incomingListView = findViewById(R.id.incoming_numbers_list);
        outgoingListView = findViewById(R.id.outgoing_numbers_list);
        missedListView = findViewById(R.id.missed_numbers_list);
        getContactButton = findViewById(R.id.btn_get_contact);

        String noName = "ללא שם";
        ArrayList<String> incommingList = new ArrayList<>();
        ArrayList<String> outgoingList = new ArrayList<>();
        ArrayList<String> missedcallsList = new ArrayList<>();
        for(int i=0; i<LAST_CALLS_COUNT; i++){
            last_in = CommonMethods.getLastCall(this, CallLog.Calls.INCOMING_TYPE, i);
            last_out = CommonMethods.getLastCall(this, CallLog.Calls.OUTGOING_TYPE, i);
            last_missed = CommonMethods.getLastCall(this, CallLog.Calls.MISSED_TYPE, i);
            incommingList.add(String.format("%s - %s", last_in.PhoneNumber, last_in.Name != null ? last_in.Name : noName));
            outgoingList.add(String.format("%s - %s", last_out.PhoneNumber, last_out.Name != null ? last_out.Name : noName));
            missedcallsList.add(String.format("%s - %s", last_missed.PhoneNumber, last_missed.Name != null ? last_missed.Name : noName));
        }

        //set dates into the list view
        ArrayAdapter<String> adapterIncoming = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, android.R.id.text1, incommingList);
        ArrayAdapter<String> adapterOutgoing = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, android.R.id.text1, outgoingList);
        ArrayAdapter<String> adapterMissed = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, android.R.id.text1, missedcallsList);
        incomingListView.setAdapter(adapterIncoming);
        outgoingListView.setAdapter(adapterOutgoing);
        missedListView.setAdapter(adapterMissed);


        incomingListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                // ListView Clicked item index
                String phoneNumber = CommonMethods.getLastCall(view.getContext(), CallLog.Calls.INCOMING_TYPE, position).PhoneNumber;
                sendMessage(view.getContext(), phoneNumber, messageType);
            }
        });

        outgoingListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                // ListView Clicked item index
                String phoneNumber = CommonMethods.getLastCall(view.getContext(), CallLog.Calls.OUTGOING_TYPE, position).PhoneNumber;
                sendMessage(view.getContext(), phoneNumber, messageType);
            }
        });

        missedListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                // ListView Clicked item index
                String phoneNumber = CommonMethods.getLastCall(view.getContext(), CallLog.Calls.MISSED_TYPE, position).PhoneNumber;
                sendMessage(view.getContext(), phoneNumber, messageType);
            }
        });

        getContactButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
                startActivityForResult(i, 1001);
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // TODO Auto-generated method stub
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case 1001:

                Uri contact = data.getData();
                ContentResolver cr = getContentResolver();

                Cursor c = managedQuery(contact, null, null, null, null);
                while(c.moveToNext()){
                    String id = c.getString(c.getColumnIndex(ContactsContract.Contacts._ID));

                    String name = c.getString(c.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
                    if (Integer.parseInt(c.getString(c.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))) > 0) {
                        Cursor pCur = cr.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,null, ContactsContract.CommonDataKinds.Phone.CONTACT_ID +" = ?", new String[]{id}, null);

                        while(pCur.moveToNext()){
                            String phoneNumber = pCur.getString(pCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                            if(phoneNumber.length() == 10 && !phoneNumber.contains("-")){
                                Log.d("LLL", "onActivityResult: " + phoneNumber);

                                //Send Message
                                sendMessage(getApplicationContext(), phoneNumber, messageType);
                                return;
                            }
                        }
                    }

                }

                break;

        }

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
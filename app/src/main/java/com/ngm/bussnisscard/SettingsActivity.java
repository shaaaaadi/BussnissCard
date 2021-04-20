package com.ngm.bussnisscard;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.Spinner;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class SettingsActivity extends AppCompatActivity {

    String settingsFilePath;

    public enum ContactType {NONE, NON_CONTACTS_ONLY, ALL_NUMBERS};
    public enum WhatsappType {NONE, WHATSAPP, WHATSAPP_BUSINESS};

    CheckBox chkboxAfterMissedCall, chkboxAfterCallEnded;
    RadioButton whatsappRadioButton, whatsappBusinessRadioButton;
    Spinner contactTypeSpinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        settingsFilePath = getIntent().getExtras().getString("settings_file_path");
        final SettingsEntry onLoadSetting = CommonMethods.readSettings(settingsFilePath);

        chkboxAfterCallEnded = findViewById(R.id.checkbox_after_call_end);
        chkboxAfterMissedCall = findViewById(R.id.checkbox_after_missed_call);
        contactTypeSpinner = findViewById(R.id.spinner_contact_type);
        initSpinner(onLoadSetting.ContactType.ordinal());

        chkboxAfterCallEnded.setChecked(onLoadSetting.CheckAfterCallEnd);
        chkboxAfterMissedCall.setChecked(onLoadSetting.CheckAfterMissedCall);

        chkboxAfterCallEnded.setOnCheckedChangeListener(
                new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        onLoadSetting.CheckAfterCallEnd = chkboxAfterCallEnded.isChecked();
                        saveSettings(onLoadSetting);
                    }
                });

        chkboxAfterMissedCall.setOnCheckedChangeListener(
                new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        onLoadSetting.CheckAfterMissedCall = chkboxAfterMissedCall.isChecked();
                        saveSettings(onLoadSetting);
                    }
                });

        contactTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                switch (position){
                    case 1: onLoadSetting.ContactType = ContactType.NON_CONTACTS_ONLY; break;
                    case 2: onLoadSetting.ContactType = ContactType.ALL_NUMBERS; break;
                    default: onLoadSetting.ContactType = ContactType.NONE; break;
                }
                saveSettings(onLoadSetting);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // your code here
            }

        });

        whatsappRadioButton = findViewById(R.id.radio_btn_whatsapp);
        whatsappBusinessRadioButton = findViewById(R.id.radio_btn_whatsapp_b);

        whatsappRadioButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                //Cancel the check of the other radio button
                if(b){
                    whatsappBusinessRadioButton.setChecked(false);
                    Log.d("Settings", "onCheckedChanged: WhatsApp selected");
                    onLoadSetting.WhatsappType = WhatsappType.WHATSAPP;
                    saveSettings(onLoadSetting);
                }
            }
        });

        whatsappBusinessRadioButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                //Cancel the check of the other radio button
                if(b){
                    whatsappRadioButton.setChecked(false);
                    Log.d("Settings", "onCheckedChanged: WhatsApp Business selected");
                    onLoadSetting.WhatsappType = WhatsappType.WHATSAPP_BUSINESS;
                    saveSettings(onLoadSetting);
                }

            }
        });

        //Check the appropriate radio button according to the settings file
        if(onLoadSetting.WhatsappType == WhatsappType.WHATSAPP)
            whatsappRadioButton.setChecked(true);
        else if (onLoadSetting.WhatsappType == WhatsappType.WHATSAPP_BUSINESS)
            whatsappBusinessRadioButton.setChecked(true);
    }

    void saveSettings(SettingsEntry s){
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put(SettingsEntry.KEY_SEND_AFTER_CALL_END, s.CheckAfterCallEnd);
            jsonObject.put(SettingsEntry.KEY_SEND_AFTER_MISSED_CALL, s.CheckAfterMissedCall);
            jsonObject.put(SettingsEntry.KEY_CONTACT_TYPE, s.ContactType.ordinal());
            jsonObject.put(SettingsEntry.WHATSAPP_TYPE, s.WhatsappType.ordinal());
        } catch (JSONException e) {
            e.printStackTrace();
        }


        // Convert JsonObject to String Format
        String userString = jsonObject.toString();

        try {
            // Define the File Path and its Name
            File file = new File(settingsFilePath);
            FileWriter fileWriter = null;
            fileWriter = new FileWriter(file);
            BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
            bufferedWriter.write(userString);
            bufferedWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    void initSpinner(int position){
        // Spinner Drop down elements
        List<String> categories = new ArrayList<String>();
        categories.add("לא מוגדר");
        categories.add("מספרים שלא באנשי קשר");
        categories.add("כולם");

        // Creating adapter for spinner
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this, R.layout.layout_spinner, categories);

        // Drop down layout style - list view with radio button
        dataAdapter.setDropDownViewResource(R.layout.layout_spinner);

        // attaching data adapter to spinner
        contactTypeSpinner.setAdapter(dataAdapter);
        contactTypeSpinner.setSelection(position);
    }
}
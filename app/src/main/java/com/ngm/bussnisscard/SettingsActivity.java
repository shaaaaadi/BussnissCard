package com.ngm.bussnisscard;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
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
    CheckBox chkboxAfterMissedCall, chkboxAfterCallEnded;
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
    }

    SettingsEntry readSettings(String settingsPath){
        //Default settings
        SettingsEntry s = new SettingsEntry();
        s.CheckAfterCallEnd = false;
        s.CheckAfterMissedCall = false;
        s.ContactType = ContactType.NONE;

        try {
            File file = new File(settingsPath);
            if(!file.exists()){//return default settings
                return s;
            }

            FileReader fileReader = new FileReader(file);
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            StringBuilder stringBuilder = new StringBuilder();
            String line = null;
            line = bufferedReader.readLine();
            while (line != null){
                stringBuilder.append(line).append("\n");
                line = bufferedReader.readLine();
            }
            bufferedReader.close();

            try {
                // This responce will have Json Format String
                String responce = stringBuilder.toString();
                JSONObject jsonObject  = new JSONObject(responce);
                s.CheckAfterCallEnd = (boolean)jsonObject.get(SettingsEntry.KEY_SEND_AFTER_CALL_END);
                s.CheckAfterMissedCall = (boolean)jsonObject.get(SettingsEntry.KEY_SEND_AFTER_MISSED_CALL);

                switch ((int)jsonObject.get(SettingsEntry.KEY_CONTACT_TYPE)){
                    case 1: s.ContactType = ContactType.NON_CONTACTS_ONLY; break;
                    case 2: s.ContactType = ContactType.ALL_NUMBERS; break;
                    default: s.ContactType = ContactType.NONE; break;
                }
            } catch (JSONException e) {
                e.printStackTrace();
                return s;
            }

        } catch (IOException e) {
            e.printStackTrace();
            return s;
        }
        return s;
    }

    void saveSettings(SettingsEntry s){
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put(SettingsEntry.KEY_SEND_AFTER_CALL_END, s.CheckAfterCallEnd);
            jsonObject.put(SettingsEntry.KEY_SEND_AFTER_MISSED_CALL, s.CheckAfterMissedCall);
            jsonObject.put(SettingsEntry.KEY_CONTACT_TYPE, s.ContactType.ordinal());
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
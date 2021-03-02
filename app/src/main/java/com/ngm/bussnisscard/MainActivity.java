package com.ngm.bussnisscard;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.PowerManager;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

public class MainActivity extends AppCompatActivity {

    LinearLayout editArea;
    Button saveButton, cancelButton, editButton, sendMsgButton, settingsButton;
    EditText userMessageEditText, urlEditText;
    String userMessageStr = "";
    String urlStr = "";

    final int DRAW_OVER_OTHER_APP_PERMISSION = 101;
    private static final int PERMISSION_REQUEST_CODE = 200;

    protected void onCreate(Bundle savedInstanceState) {
        Log.d("MyPhoneListener", "onCreate");

        //Generate Permissions
        generatePermissionsIfNeeded();

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        try {
            // TODO Auto-generated method stubs

            askForSystemOverlayPermission();

            StopMainService(5);
            CommonMethods.createNewDir(CommonMethods.appRootPath);

            CancelBatteryOpt();

            //Attach UI to Java
            editArea = findViewById(R.id.edit_button_area);
            editButton = findViewById(R.id.button_edit);
            saveButton = findViewById(R.id.button_save);
            cancelButton = findViewById(R.id.button_cancel);
            sendMsgButton = findViewById(R.id.btn_send_sms);
            settingsButton = findViewById(R.id.btn_settings);
            userMessageEditText = findViewById(R.id.edit_text_user_msg);
            urlEditText = findViewById(R.id.edit_text_url);

            //Set default visibility
            editArea.setVisibility(View.GONE);
            userMessageEditText.setEnabled(false);
            urlEditText.setEnabled(false);

            //Get the saved messages
            String[] itemsUserMessage, itemsUrl;
            itemsUserMessage = CommonMethods.readAllLines(CommonMethods.userMessageFileName);
            if(itemsUserMessage != null){
                StringBuilder sb = new StringBuilder();
                for (String line: itemsUserMessage) {
                    sb.append(line);
                    sb.append("\n");
                }
                sb.setLength(sb.length() - 1);
                userMessageEditText.setText(sb.toString());
            }
            itemsUrl = CommonMethods.readAllLines(CommonMethods.urlFileName);
            if(itemsUrl != null){
                urlEditText.setText(itemsUrl[0]);
            }



            editButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //Save the original values, then in case of editing was canceled they will be used again
                    userMessageStr = userMessageEditText.getText().toString();
                    urlStr = urlEditText.getText().toString();

                    editArea.setVisibility(View.VISIBLE);
                    editButton.setVisibility(View.GONE);
                    userMessageEditText.setEnabled(true);
                    urlEditText.setEnabled(true);

                }
            });

            saveButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    CommonMethods.writeToFile(CommonMethods.userMessageFileName, userMessageEditText.getText().toString(),false);
                    CommonMethods.writeToFile(CommonMethods.urlFileName, urlEditText.getText().toString(),false);

                    //Lock the Edit
                    editArea.setVisibility(View.GONE);
                    editButton.setVisibility(View.VISIBLE);
                    userMessageEditText.setEnabled(false);
                    urlEditText.setEnabled(false);
                }
            });

            cancelButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //Return the original fields values
                    userMessageEditText.setText(userMessageStr);
                    urlEditText.setText(urlStr);

                    //Do nothing and Lock the Edit
                    editArea.setVisibility(View.GONE);
                    editButton.setVisibility(View.VISIBLE);
                    userMessageEditText.setEnabled(false);
                    urlEditText.setEnabled(false);
                }
            });

            sendMsgButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    PopUp popup = new PopUp();
                    popup.showPopupWindow(getBaseContext(), view);
                }
            });

            settingsButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent settingsIntent = new Intent(MainActivity.this, SettingsActivity.class);
                    settingsIntent.putExtra("settings_file_path", CommonMethods.settingsPath);
                    settingsIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(settingsIntent);
                }
            });
        }
        catch (Exception ex)
        {

        }
    }

    @Override
    protected void onResume(){
        Bundle bund = getIntent().getExtras();

        if(bund != null && bund.getString("LAUNCH").equals("YES")){
            startService(new Intent(MainActivity.this, MainService.class));
        }
        super.onResume();
    }

    private void CancelBatteryOpt(){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Intent intent = new Intent();
            String packageName = getPackageName();
            PowerManager pm = (PowerManager) getSystemService(POWER_SERVICE);
            if (!pm.isIgnoringBatteryOptimizations(packageName)) {
                intent.setAction(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
                intent.setData(Uri.parse("package:" + packageName));
                startActivity(intent);
            }
        }
    }

    private void askForSystemOverlayPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
            //If the draw over permission is not available open the settings screen
            //to grant the permission.
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + getPackageName()));
            startActivityForResult(intent, DRAW_OVER_OTHER_APP_PERMISSION);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case DRAW_OVER_OTHER_APP_PERMISSION:{
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (!Settings.canDrawOverlays(this)) {
                        //Permission is not available. Display error text.
                        finish();
                    }
                }

            }
        }
    }

    //Generate Permissions
    private void generatePermissionsIfNeeded()
    {
        int PERMISSION_ALL = 1;
        String[] PERMISSIONS = {
                Manifest.permission.SYSTEM_ALERT_WINDOW,
                Manifest.permission.READ_PHONE_STATE,
                Manifest.permission.READ_PHONE_NUMBERS,
                Manifest.permission.READ_CONTACTS,
                Manifest.permission.READ_CALL_LOG,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.READ_SMS,
                Manifest.permission.SEND_SMS,
                Manifest.permission.REQUEST_IGNORE_BATTERY_OPTIMIZATIONS};

        if(!hasPermissions(this, PERMISSIONS)){
            ActivityCompat.requestPermissions(this, PERMISSIONS, PERMISSION_ALL);
        }
    }

    private static boolean hasPermissions(Context context, String... permissions) {
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }

    private void StopMainService(int retries){
        //Stopping the floating button service
        int attempts = 0;
        while(attempts < retries){
            boolean isServiceRunning = isMyServiceRunning(MainService.class);
            if(!isServiceRunning){
                Log.d("MyPhoneListener","Service Stopped");
                return;
            }
            else {//if service still running in BG
                attempts++;
                Log.d("MyPhoneListener","Stopping service - attempt: " + attempts);
                stopService(new Intent(this, MainService.class));
            }
        }
    }

    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }



}
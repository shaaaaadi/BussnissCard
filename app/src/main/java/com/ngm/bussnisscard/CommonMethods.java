package com.ngm.bussnisscard;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.provider.CallLog;
import android.provider.ContactsContract;
import android.telephony.SmsManager;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;

public final class CommonMethods {
    static final String appRootPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/BCard/";
    static final String settingsPath = appRootPath + "//settings";
    static final String userMessageFileName = appRootPath + "//user_message";
    static final String urlFileName = appRootPath + "//url";

    public static void writeToFile(String filePath, String body, boolean isToWriteNewLine)
    {
        File dbFile = new File(filePath);
        if (dbFile.exists())
            dbFile.delete();
        try {
            dbFile.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }

        FileOutputStream fos = null;

        if(isToWriteNewLine)
            body = body+"\r\n";

        try
        {
            fos = new FileOutputStream(dbFile, true);

            fos.write(body.getBytes());
            fos.close();
        }
        catch (IOException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public static String[] readAllLines(String fileToReadPath)
    {
        String line = null;
        String[] rows = null;
        File fileToRead = new File(fileToReadPath);

        try
        {
            FileInputStream fileInputStream = new FileInputStream (fileToRead);
            InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            StringBuilder stringBuilder = new StringBuilder();

            while ( (line = bufferedReader.readLine()) != null )
            {
                stringBuilder.append(line + System.getProperty("line.separator"));
            }
            fileInputStream.close();
            line = stringBuilder.toString();

            rows = line.split("\n");

            bufferedReader.close();
        }
        catch(FileNotFoundException ex)
        {
        }
        catch(IOException ex)
        {
        }
        return rows;
    }

    public static boolean createNewDir(String location)
    {
        File appDir = new File(location);
        if(appDir.exists() == false)
        {
            return appDir.mkdir();
        }
        return true;
    }
    public static void openWhatsApp(Context ctx, String phoneNumber, String message) {
        SettingsActivity.WhatsappType whatsappType = readSettings(settingsPath).WhatsappType;

        if(phoneNumber.startsWith("0"))
            phoneNumber = phoneNumber.substring(1, phoneNumber.length());
        String smsNumber = "972" + phoneNumber;

        try {
            Intent sendIntent = new Intent("android.intent.action.MAIN");
            //sendIntent.setComponent(new ComponentName("com.whatsapp", "com.whatsapp.Conversation"));
            sendIntent.setAction(Intent.ACTION_SEND);
            sendIntent.setType("text/plain");
            sendIntent.putExtra(Intent.EXTRA_TEXT, message);
            sendIntent.putExtra("jid", smsNumber + "@s.whatsapp.net"); //phone number without "+" prefix
            sendIntent.setPackage(whatsappType == SettingsActivity.WhatsappType.WHATSAPP_BUSINESS ? "com.whatsapp.w4b" : "com.whatsapp");
            sendIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            ctx.startActivity(sendIntent);
        } catch(Exception e) {
            Toast.makeText(ctx, "Error\n" + e.toString(), Toast.LENGTH_SHORT).show();
        }
    }

    public static void sendSms(Context ctx, String phoneNumber, String messageBody){
        SmsManager smsManager = SmsManager.getDefault();
        smsManager.sendTextMessage(phoneNumber, null, messageBody, null, null);
        Toast.makeText(ctx, "SMS Sent", Toast.LENGTH_LONG).show();
    }

    public static Contact getLastCall(Context ctx, int requestedCallType, int pos){
        Log.d("LastCall", "Requested: " + requestedCallType);
        Cursor managedCursor = ctx.getContentResolver().query(CallLog.Calls.CONTENT_URI, null,
                null, null, null);
        managedCursor.moveToLast();
        int numberCol = managedCursor.getColumnIndex(CallLog.Calls.NUMBER);
        int contactNameCol = managedCursor.getColumnIndex(CallLog.Calls.CACHED_NAME);
        int type = managedCursor.getColumnIndex(CallLog.Calls.TYPE);
        while(managedCursor.moveToPrevious()){
            managedCursor.moveToNext();
            String name = managedCursor.getString(contactNameCol);
            String phNumber = managedCursor.getString(numberCol);
            String callType = managedCursor.getString(type);
            int dircode = Integer.parseInt(callType);
            Log.d("LastCall", "getLastCall: " + dircode);

            if(dircode == requestedCallType){
                if(pos > 0){
                    pos--;
                    managedCursor.moveToPrevious();
                    continue;
                }
                Log.d("LastCall", "Found");
                Contact contact = new Contact();
                contact.Name = name;
                contact.PhoneNumber = phNumber;
                return contact;
            }
            managedCursor.moveToPrevious();
        }
        return null;
    }

    public static String getUserMessage(){
        final String urlFileName = appRootPath + "//url";
        final String userMessageName = appRootPath + "//user_message";

        String[] urlFile = CommonMethods.readAllLines(urlFileName);
        String[] userMessage = CommonMethods.readAllLines(userMessageName);

        if(urlFile == null || urlFile.length == 0)
            return null;

        if(userMessage == null || userMessage.length == 0)
            return null;

        StringBuilder sb = new StringBuilder();
        for (String line : userMessage) {
            sb.append(line);
            sb.append("\n");
        }
        sb.append(urlFile[0]);
        return sb.toString();
    }

    public static SettingsEntry readSettings(String settingsPath){
        //Default settings
        SettingsEntry s = new SettingsEntry();
        s.CheckAfterCallEnd = false;
        s.CheckAfterMissedCall = false;
        s.ContactType = SettingsActivity.ContactType.NONE;
        s.WhatsappType = SettingsActivity.WhatsappType.NONE;

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
                // This response will have Json Format String
                String response = stringBuilder.toString();
                JSONObject jsonObject  = new JSONObject(response);
                s.CheckAfterCallEnd = (boolean)jsonObject.get(SettingsEntry.KEY_SEND_AFTER_CALL_END);
                s.CheckAfterMissedCall = (boolean)jsonObject.get(SettingsEntry.KEY_SEND_AFTER_MISSED_CALL);

                switch ((int)jsonObject.get(SettingsEntry.KEY_CONTACT_TYPE)){
                    case 1: s.ContactType = SettingsActivity.ContactType.NON_CONTACTS_ONLY; break;
                    case 2: s.ContactType = SettingsActivity.ContactType.ALL_NUMBERS; break;
                    default: s.ContactType = SettingsActivity.ContactType.NONE; break;
                }

                switch ((int)jsonObject.get(SettingsEntry.WHATSAPP_TYPE)){
                    case 1: s.WhatsappType = SettingsActivity.WhatsappType.WHATSAPP; break;
                    case 2: s.WhatsappType = SettingsActivity.WhatsappType.WHATSAPP_BUSINESS; break;
                    default: s.WhatsappType = SettingsActivity.WhatsappType.NONE; break;
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

    public static boolean contactExists(Context context, String number) {
        /// number is the phone number
        Uri lookupUri = Uri.withAppendedPath(
                ContactsContract.PhoneLookup.CONTENT_FILTER_URI,
                Uri.encode(number));
        String[] mPhoneNumberProjection = {
                ContactsContract.PhoneLookup._ID,
                ContactsContract.PhoneLookup.NUMBER,
                ContactsContract.PhoneLookup.DISPLAY_NAME
        };
        Cursor cur = context.getContentResolver().query(lookupUri,mPhoneNumberProjection, null, null, null);
        try {
            if (cur.moveToFirst()) {
                return true;
            }
        } finally {
            if (cur != null)
                cur.close();
        }
        return false;
    }
}

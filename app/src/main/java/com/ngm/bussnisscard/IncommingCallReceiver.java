package com.ngm.bussnisscard;
import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;

public class IncommingCallReceiver extends BroadcastReceiver {
    String appRootPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/BCard/";

    static boolean ring = false;
    static boolean callAnswered = false;
    static boolean outgoingCall = false;
    Context appContext = null;

    static MyPhoneStateListener PhoneListener;

    private static boolean mStateOutgoingCall;

    @Override
    public void onReceive(Context context, Intent intent) {
        appContext = context;
        // TELEPHONY MANAGER class object to register one listner
        TelephonyManager tmgr = (TelephonyManager) context
                .getSystemService(Context.TELEPHONY_SERVICE);

        //Create Listner
        if(PhoneListener == null){
            Log.d("MyPhoneListener", "onReceive: phonelistener is null creating instance");
            PhoneListener = new MyPhoneStateListener();

            // Register listener for LISTEN_CALL_STATE
            tmgr.listen(PhoneListener, PhoneStateListener.LISTEN_CALL_STATE);
        }
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
            else{//if service still running in BG
                attempts++;
                Log.d("MyPhoneListener","Stopping service - attempt: " + attempts);
                appContext.stopService(new Intent(appContext, MainService.class));
            }
        }
    }

    private class MyPhoneStateListener extends PhoneStateListener {
        public void onCallStateChanged(int state, String incomingNumber) {
            //Ringing
            if (state == TelephonyManager.CALL_STATE_RINGING) {//1
                Log.d("MyPhoneListener", "Phone is ringing");
                ring = true;

                //reset all static flags
                callAnswered = false;
                outgoingCall = false;
            }

            //During call
            if (state == TelephonyManager.CALL_STATE_OFFHOOK) {//2
                if(ring){//Call Answered
                    Log.d("MyPhoneListener","call answered");

                    boolean isServiceRunning = isMyServiceRunning(MainService.class);
                    if(isServiceRunning)
                        return;

                    //Starting the floating button service
                    Log.d("MyPhoneListener","showing floating button");
                    Intent mainService = new Intent(appContext, MainService.class);
                    mainService.putExtra("incoming_number", incomingNumber);
                    appContext.startService(mainService);

                    callAnswered = true;
                }
                else {//outgoing call
                    Log.d("MyPhoneListener", "Outgoing Call");
                    outgoingCall = true;
                }
             }

            //Idle
            if(state == TelephonyManager.CALL_STATE_IDLE){//0
                boolean missedCall = false;
                boolean callEnded = false;

                if(!callAnswered && !outgoingCall){
                    missedCall = true;
                    Log.d("MyPhoneListener","missed call");
                }
                else {
                    callEnded = true;
                    Log.d("MyPhoneListener","call ended");

                    if(callAnswered){
                        Log.d("MyPhoneListener","Stopping floating button");
                        StopMainService(5);
                    }
                }

                //Send Sms if needed - But not in case of outgoing call
                if(!outgoingCall)
                    if(isSendingSmsNeeded(missedCall, callEnded, incomingNumber)){
                        Log.d("MyPhoneListener","Sms Sent");

                        String userMessage = CommonMethods.getUserMessage();
                        CommonMethods.sendSms(appContext, incomingNumber, userMessage);
                        //Add the sms send code here
                    }

                //Reset All flags
                ring = false;
                callAnswered = false;
                outgoingCall = false;

                //Check if service stil not stopped, stop it
                boolean isServiceRunning = isMyServiceRunning(MainService.class);
                if(isServiceRunning){
                    Log.d("MyPhoneListener","Stopping floating button");
                    StopMainService(5);
                }
            }
        }

    }

    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) appContext.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    private boolean isEligibleNumber(String phoneNumber){
        boolean sendToAll = CommonMethods.readSettings(CommonMethods.settingsPath).ContactType == SettingsActivity.ContactType.ALL_NUMBERS;
        boolean sendToNonContacts = CommonMethods.readSettings(CommonMethods.settingsPath).ContactType == SettingsActivity.ContactType.NON_CONTACTS_ONLY;

        //check if the phone number is in the phone book adn send message if it's not
        boolean knownContact = CommonMethods.contactExists(appContext, phoneNumber);
        if(sendToAll)
            return true;

        if(knownContact){
            if(sendToNonContacts)
                return  false;
        }
        else{//Un saved phone number
            if(sendToNonContacts)
                return true;
        }
        return false;
    }

    private boolean isSendingSmsNeeded(boolean missedCall, boolean callEnded, String phoneNumber){
        boolean sendMessageAfterMissedCall = CommonMethods.readSettings(CommonMethods.settingsPath).CheckAfterMissedCall;
        boolean sendMessageAfterCallEnd = CommonMethods.readSettings(CommonMethods.settingsPath).CheckAfterCallEnd;

        if(missedCall && sendMessageAfterMissedCall){
            return isEligibleNumber(phoneNumber);
        }
        if(callEnded && sendMessageAfterCallEnd){
            return isEligibleNumber(phoneNumber);
        }
        return false;
    }
}

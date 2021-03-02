package com.ngm.bussnisscard;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.telephony.PhoneNumberUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

public class PopUp  {
    public void showPopupWindow(final Context ctx, final View view) {
        //Create a View object yourself through inflater
//        LayoutInflater inflater = (LayoutInflater) view.getContext().getSystemService(view.getContext().LAYOUT_INFLATER_SERVICE);
        LayoutInflater inflater = (LayoutInflater) ctx.getSystemService(ctx.LAYOUT_INFLATER_SERVICE);

        View popupView = null;
        try{
            popupView = inflater.inflate(R.layout.activity_pop_up, null );
        }
        catch (Exception ex){
            Log.d("Shadi", ex.getMessage());
        }

        //Specify the length and width through constants
        int width = LinearLayout.LayoutParams.MATCH_PARENT;
        int height = LinearLayout.LayoutParams.MATCH_PARENT;

        //Make Inactive Items Outside Of PopupWindow
        boolean focusable = true;

        //Create a window with our parameters
        final PopupWindow popupWindow = new PopupWindow(popupView, width, height, focusable);

        //Set the location of the window on the screen
        popupWindow.showAtLocation(view, Gravity.CENTER, 0, 0);

        //Initialize the elements of our window, install the handler

        Button messageButton = popupView.findViewById(R.id.messageButton);
        messageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Context ctx = v.getContext();
                Intent contactSelector = new Intent(ctx, ContactSelectorActivity.class);
                contactSelector.putExtra("message_type", "standard");
                contactSelector.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                ctx.startActivity(contactSelector);
                popupWindow.dismiss();
            }
        });

        Button whatsappButton = popupView.findViewById(R.id.whatsappButton);
        whatsappButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Context ctx = v.getContext();
                Intent contactSelector = new Intent(ctx, ContactSelectorActivity.class);
                contactSelector.putExtra("message_type", "whatsapp");
                contactSelector.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                ctx.startActivity(contactSelector);
                popupWindow.dismiss();
            }
        });


        //Handler for clicking on the inactive zone of the window

        popupView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                //Close the window when clicked
                popupWindow.dismiss();
                return true;
            }
        });

    }

//    private void openWhatsApp(Context ctx, String phoneNumber) {
//        if(phoneNumber.startsWith("0"))
//            phoneNumber = phoneNumber.substring(1, phoneNumber.length());
//        String smsNumber = "972" + phoneNumber;
//
//        try {
//            Intent sendIntent = new Intent("android.intent.action.MAIN");
//            //sendIntent.setComponent(new ComponentName("com.whatsapp", "com.whatsapp.Conversation"));
//            sendIntent.setAction(Intent.ACTION_SEND);
//            sendIntent.setType("text/plain");
//            sendIntent.putExtra(Intent.EXTRA_TEXT, "This is my text to send.");
//            sendIntent.putExtra("jid", smsNumber + "@s.whatsapp.net"); //phone number without "+" prefix
//            sendIntent.setPackage("com.whatsapp");
//            sendIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//            ctx.startActivity(sendIntent);
//        } catch(Exception e) {
//            Toast.makeText(ctx, "Error/n" + e.toString(), Toast.LENGTH_SHORT).show();
//        }
//    }

}
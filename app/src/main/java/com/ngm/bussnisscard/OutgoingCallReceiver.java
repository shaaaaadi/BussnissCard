package com.ngm.bussnisscard;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

public class OutgoingCallReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String number = intent.getStringExtra(Intent.EXTRA_PHONE_NUMBER);

        Toast.makeText(context,"Outgoing: "+number, Toast.LENGTH_LONG).show();

    }
}

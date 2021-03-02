package com.ngm.bussnisscard;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toast;

import java.net.Inet4Address;

public class MainService extends Service {

    private WindowManager windowManager;
    private ImageView floatIcon;
    String incomingNumber = "";

    @Override
    public IBinder onBind(Intent intent) {
        // TODO Auto-generated method stub
        return null;
    }


    @Override
    public void onCreate() {
        Log.d("MainService", "onCreate: Service Created");
        super.onCreate();

        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);

        floatIcon = new ImageView(this);

        floatIcon.setImageResource(R.drawable.sms_40);

        final WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);

        params.gravity = Gravity.TOP | Gravity.LEFT;
        params.x = 0;
        params.y = 100;

        windowManager.addView(floatIcon, params);

        try {
            floatIcon.setOnTouchListener(new View.OnTouchListener() {
                private WindowManager.LayoutParams paramsF = params;
                private int initialX;
                private int initialY;
                private float initialTouchX;
                private float initialTouchY;

                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    switch (event.getAction()) {
                        case MotionEvent.ACTION_DOWN:
                            // Get current time in nano seconds.
                            initialX = paramsF.x;
                            initialY = paramsF.y;
                            initialTouchX = event.getRawX();
                            initialTouchY = event.getRawY();
                            break;
                        case MotionEvent.ACTION_UP:
                            break;
                        case MotionEvent.ACTION_MOVE:
                            paramsF.x = initialX + (int) (event.getRawX() - initialTouchX);
                            paramsF.y = initialY + (int) (event.getRawY() - initialTouchY);
                            windowManager.updateViewLayout(floatIcon, paramsF);
                            break;
                    }
                    return false;
                }
            });

            floatIcon.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //Getting the while call message
                    String userMessage = CommonMethods.getUserMessage();
                    if(userMessage == null){
                        toastMessage("No User Message Found");
                        return;
                    }

                    if(incomingNumber == null) {
                        toastMessage("No Phone Number Found");
                        return;
                    }

                    try{
                        Intent sendIntent = new Intent(getBaseContext(), SendActivity.class);
                        sendIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        sendIntent.putExtra("message", userMessage);
                        sendIntent.putExtra("phone_number", incomingNumber);
                        startActivity(sendIntent);
                    }
                    catch (Exception ex)
                    {
                        Log.d("MainService", "onClick: " + ex.getMessage());
                    }
                }
            });
        } catch (Exception e) {
            toastMessage("Sending SMS to: " + incomingNumber + " Failed Due to :" + e.getMessage());
        }

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        incomingNumber = intent.getStringExtra("incoming_number");
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (floatIcon != null) windowManager.removeView(floatIcon);
    }

    void toastMessage(final String msg){
        Handler handler = new Handler(getMainLooper());
        handler.post(new Runnable() {

            @Override
            public void run() {
                Toast.makeText(MainService.this, msg ,Toast.LENGTH_SHORT).show();
            }
        });
    }
}

package com.example.samyzarour.facebookpopup;

import android.app.Service;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.RelativeLayout;

/**
 * Created by samyzarour on 2017-01-10.
 */

public class ChatHeadService extends Service {

    private WindowManager windowManager;
    private RelativeLayout bubbleView;
    private RelativeLayout removeView;

    private boolean isTextLongPressed = false;
    Point windowSize = new Point();
//    private ImageView chatHead;

    @Override public IBinder onBind(Intent intent) {
        // Not used
        return null;
    }

    @Override public void onCreate() {
        super.onCreate();
        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        LayoutInflater inflater = (LayoutInflater)getSystemService(LAYOUT_INFLATER_SERVICE);

        //Create Bubble View
        bubbleView = (RelativeLayout) inflater.inflate(R.layout.bubble, null);
        windowManager.getDefaultDisplay().getSize(windowSize);
        windowManager.getDefaultDisplay().getSize(windowSize);

        final WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);

        params.gravity = Gravity.TOP | Gravity.LEFT;
        params.x = 0;
        params.y = 100;

        windowManager.addView(bubbleView, params);

        // Create Delete View
        removeView = (RelativeLayout)inflater.inflate(R.layout.remove, null);
        WindowManager.LayoutParams paramsRemove = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);

        paramsRemove.gravity = Gravity.BOTTOM | Gravity.CENTER;
        removeView.setVisibility(View.GONE);
        paramsRemove.y = 100;
        windowManager.addView(removeView, paramsRemove);

        bubbleView.setOnTouchListener(new View.OnTouchListener() {
            private int initialX;
            private int initialY;
            private float initialTouchX;
            private float initialTouchY;

            @Override public boolean onTouch(View v, MotionEvent event) {
                if(isTextLongPressed){
                    switch (event.getAction()) {
                        case MotionEvent.ACTION_DOWN:
                            initialX = params.x;
                            initialY = params.y;
                            initialTouchX = event.getRawX();
                            initialTouchY = event.getRawY();
                            return true;
                        case MotionEvent.ACTION_UP:
                            isTextLongPressed = false;
                            removeView.setVisibility(View.GONE);
                            moveToEdge(params.x);
                            return true;
                        case MotionEvent.ACTION_MOVE:
                            params.x = initialX + (int) (event.getRawX() - initialTouchX);
                            params.y = initialY + (int) (event.getRawY() - initialTouchY);
                            windowManager.updateViewLayout(bubbleView, params);
                            return true;
                    }
                }
                return false;
            }
        });

        bubbleView.setOnLongClickListener(new View.OnLongClickListener(){
            @Override
            public boolean onLongClick(View v) {
                isTextLongPressed = true;
                removeView.setVisibility(View.VISIBLE);
                return false;
            }

        });

        bubbleView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                View expanded = bubbleView.findViewById(R.id.expanded_container);
                View collapse = bubbleView.findViewById(R.id.collapse_view);

                if (expanded.getVisibility() == View.GONE) expanded.setVisibility(View.VISIBLE);
                else expanded.setVisibility(View.GONE);
                if (collapse.getVisibility() == View.GONE) collapse.setVisibility(View.VISIBLE);
                else collapse.setVisibility(View.GONE);
            }
        });

        bubbleView.findViewById(R.id.open_activity_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //open the activity and stop service
                Intent intent = new Intent(ChatHeadService.this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                stopSelf();
            }
        });


    }

    private void moveToEdge(final int x_position){
        final boolean onLeft = x_position <= windowSize.x / 2;
        final int x_distance = (onLeft) ? x_position : windowSize.x - x_position;
        final WindowManager.LayoutParams mParams = (WindowManager.LayoutParams) bubbleView.getLayoutParams();
        final int SLIDE_DURATION = 200;

        new CountDownTimer(SLIDE_DURATION, 5){
            public void onTick(long t) {
                double steps = (double) t/ (double) SLIDE_DURATION;
                if(onLeft) mParams.x = (int) (0 + steps * x_distance);
                else mParams.x = (int) (windowSize.x - steps * x_distance) - bubbleView.getWidth();
                windowManager.updateViewLayout(bubbleView, mParams);
            }
            public void onFinish() {
                if(onLeft) mParams.x = 0;
                else mParams.x = windowSize.x - bubbleView.getWidth();
                windowManager.updateViewLayout(bubbleView, mParams);
            }
        }.start();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (bubbleView != null) windowManager.removeView(bubbleView);
    }

}
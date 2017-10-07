package com.example.samyzarour.facebookpopup;

import android.app.Service;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.os.Vibrator;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.HapticFeedbackConstants;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;

/**
 * Created by samyzarour on 2017-01-10.
 */

public class ChatHeadService extends Service {

    private WindowManager windowManager;
    private RelativeLayout bubbleView;
    private RelativeLayout removeView;
    private ImageView removeImg;

    private boolean inBounded = false;

    private boolean isTextLongPressed = false;
    Point windowSize = new Point();
    final private int REMOVE_DISTANCE_TO_BOTTOM = 100;

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
        final WindowManager.LayoutParams paramsRemove = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);

        paramsRemove.gravity = Gravity.BOTTOM | Gravity.CENTER;
        removeView.setVisibility(View.GONE);
        paramsRemove.dimAmount = 0.7f;
        paramsRemove.y = REMOVE_DISTANCE_TO_BOTTOM;
        windowManager.addView(removeView, paramsRemove);

        // Define Delete View Boundaries
        removeImg = (ImageView) removeView.findViewById(R.id.remove_img);

        final int remove_width = removeImg.getLayoutParams().width;
        final int remove_height = removeImg.getLayoutParams().height;
        final int bound_left = (windowSize.x - (int) (remove_width * 1.5))/ 2;
        final int bound_right = (windowSize.x + (int) (remove_width * 1.5))/2;
        final int bound_top = windowSize.y - (int) (remove_height * 1.5) - REMOVE_DISTANCE_TO_BOTTOM;
        final int bound_bottom = windowSize.y - REMOVE_DISTANCE_TO_BOTTOM;

        Log.e("BOUNDS", "ORIGINAL: " + removeImg.getLayoutParams().width + ", LEFT: " + bound_left + ", RIGHT: " + bound_right + ", BOTTOM: " + bound_bottom + ", TOP: " + bound_top);

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

                            //If user drag and drop the floating widget view into remove view then stop the service else move it to an edge
                            if (inBounded) {
                                stopSelf();
                                inBounded = false;
                            } else moveToEdge(params.x);

                            return true;
                        case MotionEvent.ACTION_MOVE:
                            int fingerX = initialX + (int) (event.getRawX() - initialTouchX);
                            int fingerY = initialY + (int) (event.getRawY() - initialTouchY);
                            params.x = fingerX - bubbleView.getWidth()/2;
                            params.y = fingerY - bubbleView.getHeight()/2;

                            if((fingerX >= bound_left && fingerX <= bound_right && fingerY <= bound_bottom && fingerY >= bound_top)){
                                inBounded = true;

                                // Grow remove view
                                removeImg.getLayoutParams().height = (int) (remove_height * 1.5);
                                removeImg.getLayoutParams().width = (int) (remove_width * 1.5);
                                removeImg.setLayoutParams(removeImg.getLayoutParams());

                                // Set buble in remove view
                                params.x = (windowSize.x - bubbleView.getWidth())/2;
                                params.y = (windowSize.y - (int) (bubbleView.getHeight() * 1.5) - REMOVE_DISTANCE_TO_BOTTOM);
                                Log.e("ABS", "ABS: " + windowSize.y + ", Bubble: " + bubbleView.getHeight() + ", REMOVE_CONST: " + REMOVE_DISTANCE_TO_BOTTOM);
                                Log.e("RESULT", "RES: " + (windowSize.y - bubbleView.getHeight() - REMOVE_DISTANCE_TO_BOTTOM));
                                Log.e("REMOVE POS", "X: " + paramsRemove.x + "Y: " + paramsRemove.y);
                                Log.e("FLOAT POS", "X: " + params.x + ", Y: " + params.y);
                            } else{
                                inBounded = false;

                                // Shrink remove view
                                removeImg.getLayoutParams().height = remove_height;
                                removeImg.getLayoutParams().width = remove_width;
                                removeImg.setLayoutParams(removeImg.getLayoutParams());
                            }

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
                v.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
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

                Log.e("MEASUREMENTS","X: " + params.x + ", Y: " + params.y);
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
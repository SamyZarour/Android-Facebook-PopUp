package com.example.samyzarour.facebookpopup;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import static android.R.attr.permission;

public class MainActivity extends AppCompatActivity {

    final int REQUEST_CODE = 101;

    Button start;
    Button stop;

    //Warning for activating Permission with button to activate it
    TextView permissionText;
    Button permissionButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Buttons for making the pop up appear/disappear
        start = (Button) findViewById(R.id.startPop);
        stop = (Button) findViewById(R.id.stopPop);

        //Warning for activating Permission with button to activate it
        permissionText = (TextView) findViewById(R.id.permissionText);
        permissionButton = (Button) findViewById(R.id.permissionButton);

        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startService(new Intent(getApplication(), ChatHeadService.class));
            }
        });

        stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopService(new Intent(getApplication(), ChatHeadService.class));
            }
        });

        permissionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkDrawOverlayPermission();
            }
        });


        //Check for system alert window permission and activate it if needed
        checkDrawOverlayPermission();
    }


    public void checkDrawOverlayPermission() {
        /** check if we already  have permission to draw over other apps */
        if (!Settings.canDrawOverlays(getApplication())) {
            /** if not construct intent to request permission */
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + getPackageName()));
            /** request permission via start activity for result */
            startActivityForResult(intent, REQUEST_CODE);
        }
        else    setVisibility(true);
    }

    public void setVisibility(boolean drawOveralyPermission){
        if (drawOveralyPermission) {
            permissionText.setVisibility(View.GONE);
            permissionButton.setVisibility(View.GONE);
            start.setVisibility(View.VISIBLE);
            stop.setVisibility(View.VISIBLE);
        }
        else{
            permissionText.setVisibility(View.VISIBLE);
            permissionButton.setVisibility(View.VISIBLE);
            start.setVisibility(View.GONE);
            stop.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode,  Intent data) {
        /** check if received result code
         is equal our requested code for draw permission  */
        if (requestCode == REQUEST_CODE) {
            setVisibility(Settings.canDrawOverlays(this));
        }
    }

}

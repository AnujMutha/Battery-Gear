package com.muthadevelopers.batterygear;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import androidx.appcompat.app.AppCompatActivity;

public class SplashScreen extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

/*

        Animation animTogether = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fade_in);
        findViewById(R.id.splashscreen_logo).startAnimation(animTogether);
*/

     /*   try {
            Thread.sleep(4000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
*/


        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                // Do something after 5s = 5000ms
                startActivity(new Intent(getApplicationContext(),MainActivity.class));
                finish();
            }
        }, 1000);

    }
}
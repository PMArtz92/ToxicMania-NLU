package com.toxicmania.toxicmania.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.toxicmania.toxicmania.R;

/**
 * Created by PM Artz on 10/12/2017.
 */

public class SplashActivity extends AppCompatActivity {
//    static{
//        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
//    }
private static int SPLASH_TIME_OUT = 600;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(SplashActivity.this, NavDrawerActivity.class);
                startActivity(intent);
                finish();
                overridePendingTransition(R.anim.slide_in_right,R.anim.slide_out_left);
            }
        }, SPLASH_TIME_OUT);

    }
}

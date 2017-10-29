package com.toxicmania.toxicmania.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

/**
 * Created by PM Artz on 10/12/2017.
 */

public class SplashActivity extends AppCompatActivity {
//    static{
//        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
//    }
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        Intent intent = new Intent(this, NavDrawerActivity.class);
        startActivity(intent);
        finish();
    }
}

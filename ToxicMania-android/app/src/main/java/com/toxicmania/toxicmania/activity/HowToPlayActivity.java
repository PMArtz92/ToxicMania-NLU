package com.toxicmania.toxicmania.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

import com.toxicmania.toxicmania.R;

public class HowToPlayActivity extends AppCompatActivity {

    private String[] navMenuTitles;
    private TypedArray navMenuIcons;
    private TextView textView,textView2,textView3,textView4;
    Toolbar toolbar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_how_to_play);

        Typeface robotoRegular = Typeface.createFromAsset(getAssets(), "fonts/Roboto-Regular.ttf");
        Typeface robotoBlack = Typeface.createFromAsset(getAssets(), "fonts/Roboto-Black.ttf");
        textView = (TextView)findViewById(R.id.textView);
        textView2 = (TextView)findViewById(R.id.textView2);
        textView3 = (TextView)findViewById(R.id.textView3);
        textView4 = (TextView)findViewById(R.id.textView4);

        textView3.setTypeface(robotoRegular);
        textView4.setTypeface(robotoRegular);
        textView.setTypeface(robotoBlack);
        textView2.setTypeface(robotoBlack);
    }

    public void goHome(View v) {
        Intent i = new Intent(HowToPlayActivity.this, NavDrawerActivity.class);
        startActivity(i);
        //Update sharedPreferences
        SharedPreferences settings = getSharedPreferences("PREFS", 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean("is_first_start", false);
        editor.apply();
        finish();
    }

    @Override
    public void onBackPressed() {}
}


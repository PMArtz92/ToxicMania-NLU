package com.toxicmania.toxicmania.activity;

import android.content.Intent;
import android.graphics.Typeface;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.toxicmania.toxicmania.R;

public class AboutActivity extends AppCompatActivity {

    private static final String TAG = "AboutActivity";
    private TextView textView,textView3,textView4;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //Log.i(TAG, "AboutActivity.onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        TextView t2 = (TextView) findViewById(R.id.textView4);
        t2.setMovementMethod(LinkMovementMethod.getInstance());

        Typeface robotoRegular = Typeface.createFromAsset(getAssets(), "fonts/Roboto-Regular.ttf");
        Typeface robotoBlack = Typeface.createFromAsset(getAssets(), "fonts/Roboto-Black.ttf");

        textView = (TextView)findViewById(R.id.textView);
        textView3 = (TextView)findViewById(R.id.textView3);
        textView4 = (TextView)findViewById(R.id.textView4);

        textView3.setTypeface(robotoRegular);
        textView4.setTypeface(robotoRegular);
        textView.setTypeface(robotoBlack);

    }

    public void nextAction(View v) {
        Intent i = new Intent(AboutActivity.this, TermsActivity.class);
        startActivity(i);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        finish();
    }

    @Override
    public void onBackPressed() {}

}

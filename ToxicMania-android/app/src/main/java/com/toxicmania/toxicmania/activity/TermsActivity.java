package com.toxicmania.toxicmania.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.toxicmania.toxicmania.R;

public class TermsActivity extends AppCompatActivity {

    private static final String TAG = "TermsActivity";
    private TextView termsHeader, termsBody;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //Log.i(TAG, "TermsActivity.onCreate");

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_terms);

        Typeface robotoRegular = Typeface.createFromAsset(getAssets(), "fonts/Roboto-Regular.ttf");
        Typeface robotoBlack = Typeface.createFromAsset(getAssets(), "fonts/Roboto-Black.ttf");
        termsBody = (TextView)findViewById(R.id.termsBody);
        termsHeader = (TextView)findViewById(R.id.termsHeader);

        if (termsBody != null)
            termsBody.setTypeface(robotoRegular);
        if (termsHeader != null)
            termsHeader.setTypeface(robotoBlack);
    }

    public void declineTerms(View v) {
        AlertDialog alertDialog = new AlertDialog.Builder(this).create();
        alertDialog.setTitle("Please Confirm Decline");
        alertDialog.setMessage("You cannot proceed further without accepting terms of services!");
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        alertDialog.show();
    }

    public void acceptTerms(View v) {
        Intent i = new Intent(this, HowToPlayActivity.class);
        startActivity(i);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        finish();
    }

    @Override
    public void onBackPressed() {}

}

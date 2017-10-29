package com.toxicmania.toxicmania.activity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.toxicmania.toxicmania.R;
import com.toxicmania.toxicmania.User;
import com.toxicmania.toxicmania.activity.multiplay.MultiplayActivity;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    Button playBtn, multiPlayBtn, leaderboardBtn;
    boolean isLogged;
    SharedPreferences settings;
    private User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

//
//        Log.i(TAG, "================================");
//
//        String[] navMenuTitles = getResources().getStringArray(R.array.nav_drawer_items);
//        TypedArray navMenuIcons = getResources()
//                .obtainTypedArray(R.array.nav_drawer_icons);
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//            set(navMenuTitles, navMenuIcons);
//        }
//        getSupportActionBar().setTitle(getTitle());
//
//        //Handle one time events
//        settings = getSharedPreferences("PREFS", 0);
//        boolean firstStart = settings.getBoolean("is_first_start", true);
//        if (firstStart) {
//            Log.i(TAG, "Starting for the first time.");
//            //Update sharedPreferences
//            SharedPreferences.Editor editor = settings.edit();
//            editor.putBoolean("is_first_start", false);
//            editor.apply();
//
//            // first time tasks go here
//            Intent i = new Intent(this, AboutActivity.class);
//            i.putExtra("isFirstTime", true);
//            startActivity(i);
//        }
//
//        // set play button action
//        playBtn = (Button) findViewById(R.id.play_button);
//        playBtn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                playBtnAction(view);
//            }
//        });
//
//        // set multi player button action
//        multiPlayBtn = (Button) findViewById(R.id.mplay_button);
//        multiPlayBtn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                // TODO: 10/23/2017 make below 0->3
//                if (user.getLevel() > 3) {
//                    Intent i = new Intent(MainActivity.this, MultiplayActivity.class);
//                    startActivity(i);
//                } else {
//                    AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this).create();
//                    alertDialog.setTitle("Oops!");
//                    alertDialog.setMessage("You must pass level 3 in single player mode to access multi player!");
//                    alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
//                            new DialogInterface.OnClickListener() {
//                                public void onClick(DialogInterface dialog, int which) {
//                                    dialog.dismiss();
//                                }
//                            });
//                    alertDialog.show();
//                }
//            }
//        });
//
//        Button test = (Button)findViewById(R.id.testBtn);
//        test.setOnClickListener(new View.OnClickListener(){
//
//            @Override
//            public void onClick(View view) {
//                Intent i = new Intent(MainActivity.this, NavDrawerActivity.class);
//                startActivity(i);
//            }
//        });
    }

    public void playBtnAction(View v) {
        if (isNetworkAvailable()) {
            Intent i = new Intent(MainActivity.this, PlayActivity.class);
            startActivity(i);
        } else {
            final Snackbar snackbar = Snackbar.make(v, "Cannot connect to network", Snackbar.LENGTH_LONG);
            snackbar.setAction("Retry", new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    playBtnAction(view);
                    snackbar.dismiss();
                }
            });
            snackbar.show();
        }
    }

    public void exitFunc(View v) {
        AlertDialog alertDialog = new AlertDialog.Builder(this).create();
        alertDialog.setTitle("Are you sure?");
        alertDialog.setMessage("Do you really wish to exit from ToxicMania?");
        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "NO",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "YES",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(MainActivity.this, "Thank you for contributing!", Toast.LENGTH_LONG).show();
                        finish();
                        System.exit(0);
                    }
                });
        alertDialog.show();
    }

    @Override
    protected void onPostResume() {
        isLogged = settings.getBoolean("is_logged", false);
//        if (!isLogged) {
//            Log.i(TAG, "not logged in, send to login");
//            Intent k = new Intent(this, LoginActivity.class);
//            startActivity(k);
//        } else {
//            // update user object
//            Log.i(TAG, "Updating user object...");
//            user = new User(settings.getString("ToxicUser", ""));
//        }

        super.onPostResume();
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
}

package com.toxicmania.toxicmania.activity.multiplay;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
import com.toxicmania.toxicmania.R;
import com.toxicmania.toxicmania.User;
import com.toxicmania.toxicmania.VolleyService;

public class MultiplayActivity extends AppCompatActivity {

    public User user;
    public String gameKey;
    public VolleyService volleyService;
    public String firebaseToken;

    private String TAG = "MultiplayActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_multiplay);

        // get user object
        SharedPreferences sharedPreferences = getSharedPreferences("PREFS", 0);
        user = new User(sharedPreferences.getString("ToxicUser", ""));

        volleyService = new VolleyService(this);

        // set default frame
        displayFragment(new MultiplayHomeFragment());

        firebaseToken = FirebaseInstanceId.getInstance().getToken();
        //Log.i(TAG, "################## " + firebaseToken);
    }

    public void displayFragment(Fragment f) {
        if (f != null) {
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.MultiPlayFragmentLayout, f);
            ft.commit();
        }
    }

    @Override
    public void onBackPressed() {
        //ask for confirmation
        new AlertDialog.Builder(this)
                .setTitle("Are you sure?")
                .setMessage("Your game session will be lost!")
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        finish();
                    }})
                .setNegativeButton(android.R.string.no, null).show();
    }

}

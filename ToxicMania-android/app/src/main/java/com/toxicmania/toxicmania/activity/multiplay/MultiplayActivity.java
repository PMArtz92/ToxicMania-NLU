package com.toxicmania.toxicmania.activity.multiplay;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.android.volley.VolleyError;
import com.google.firebase.iid.FirebaseInstanceId;
import com.toxicmania.toxicmania.Question;
import com.toxicmania.toxicmania.R;
import com.toxicmania.toxicmania.User;
import com.toxicmania.toxicmania.VolleyCallback;
import com.toxicmania.toxicmania.VolleyService;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        SharedPreferences settings = getSharedPreferences("PREFS", 0);
        settings.edit().remove("isGameFinished").apply();
    }

    @Override
    public void onBackPressed() {
        //ask for confirmation
        new AlertDialog.Builder(this)
                .setTitle("Are you sure?")
                .setMessage("Your game session will be lost!")
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        removeUserFromSession();
                        finish();
                    }})
                .setNegativeButton(android.R.string.no, null).show();
    }

    public void displayFragment(Fragment f) {
        if (f != null) {
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.MultiPlayFragmentLayout, f);
            ft.commit();
        }
    }

    private void removeUserFromSession() {
        // building send object
        JSONObject finalObject = new JSONObject();
        try {
            finalObject.put("U_Id", user.getID());
            finalObject.put("Se_Id", gameKey);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        String url = "https://app.ucsccareerfair.com/multiplay/disconnect";
        volleyService.volleyPost("POST", url, finalObject, new VolleyCallback() {
            @Override
            public void notifySuccess(String requestType, JSONObject response) {
                Log.d(TAG, "Volley POST success : " + response);
            }

            @Override
            public void notifyError(String requestType, VolleyError error) {
                Log.e(TAG, "Volley POST error : " + error);
            }
        });
    }
}

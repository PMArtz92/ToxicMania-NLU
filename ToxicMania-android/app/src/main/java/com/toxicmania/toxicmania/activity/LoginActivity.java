package com.toxicmania.toxicmania.activity;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.toxicmania.toxicmania.R;

public class LoginActivity extends AppCompatActivity {

    private FragmentManager fm;
    private Fragment fragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        fm = getSupportFragmentManager();
        fragment = fm.findFragmentById(R.id.fragment_container);

        if (isNetworkAvailable()) {
            if (fragment == null) {
                fragment = new GPlusFragment();
                fm.beginTransaction()
                        .add(R.id.fragment_container, fragment)
                        .commit();
            }
        } else {
            final Snackbar snackbar = Snackbar.make(findViewById(R.id.fragment_container), "Cannot connect to network", Snackbar.LENGTH_LONG);
            snackbar.setAction("Retry", new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent k = new Intent(LoginActivity.this, LoginActivity.class);
                    startActivity(k);
                    LoginActivity.this.finish();
                    snackbar.dismiss();
                }
            });
            snackbar.show();
        }
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
}

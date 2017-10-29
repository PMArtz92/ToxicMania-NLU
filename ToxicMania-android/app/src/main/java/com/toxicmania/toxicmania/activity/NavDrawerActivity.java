package com.toxicmania.toxicmania.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.toxicmania.toxicmania.R;
import com.toxicmania.toxicmania.User;

public class NavDrawerActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    boolean isLogged;
    SharedPreferences settings;
    private static final String TAG = "NavDrawerActivity";
    private User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nav_drawer);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //set Main Menu Fragment as default fragment
        MainMenuFragment mainMenu = new MainMenuFragment();
        setFragment(mainMenu);

        settings = getSharedPreferences("PREFS", 0);
        user = new User(settings.getString("ToxicUser", ""));

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        //set Profile Picture, Name and Email in drawer header
        View header = navigationView.getHeaderView(0);

        // TODO: 10/27/2017 For pasan
//        String uName = "Pasan Ranathunga";
//        String uEmail = "pmartz92@gmail.com";
//        String userPropicURL = "https://lh6.googleusercontent.com/-TcU0a4zUy7M/AAAAAAAAAAI/AAAAAAAAAsc/eV0iniaGRm0/photo.jpg";
        String userPropicURL = user.getUrl();
        String uName = user.getName();
        String uEmail = user.getEmail();
        ImageView ProPic = (ImageView)header.findViewById(R.id.navHeaderProPic);
        Picasso.with(header.getContext()).load(userPropicURL).resize(150, 150).centerCrop().transform(new CircleTransform()).into(ProPic);

        TextView userName = (TextView) header.findViewById(R.id.navHeaderName);
        TextView userEmail = (TextView) header.findViewById(R.id.navHeaderEmail);
        userName.setText(uName);
        userEmail.setText(uEmail);

    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.nav_drawer, menu);
//        return true;
//    }

//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        // Handle action bar item clicks here. The action bar will
//        // automatically handle clicks on the Home/Up button, so long
//        // as you specify a parent activity in AndroidManifest.xml.
//        int id = item.getItemId();
//
//        //noinspection SimplifiableIfStatement
//        if (id == R.id.action_settings) {
//            return true;
//        }
//
//        return super.onOptionsItemSelected(item);
//    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_home) {
            MainMenuFragment mainMenu = new MainMenuFragment();
            FragmentManager manager = getSupportFragmentManager();
            manager.beginTransaction().replace(R.id.fragment_replace_layout, mainMenu, mainMenu.getTag()).addToBackStack("tag").commit();

        } else if (id == R.id.nav_achievements) {
            AchievementFragment achievements = new AchievementFragment();
            FragmentManager manager = getSupportFragmentManager();
            manager.beginTransaction().replace(R.id.fragment_replace_layout, achievements, achievements.getTag()).addToBackStack("tag").commit();

        } else if (id == R.id.nav_howtoPlay) {
            HowToPlayFragment howtoPlay = new HowToPlayFragment();
            FragmentManager manager = getSupportFragmentManager();
            manager.beginTransaction().replace(R.id.fragment_replace_layout, howtoPlay, howtoPlay.getTag()).addToBackStack("tag").commit();

        } else if (id == R.id.nav_leaderBoard) {
            LeaderBoardFragment leaderboard = new LeaderBoardFragment();
            FragmentManager manager = getSupportFragmentManager();
            manager.beginTransaction().replace(R.id.fragment_replace_layout, leaderboard, leaderboard.getTag()).addToBackStack("tag").commit();

        } else if (id == R.id.nav_about) {
            AboutFragment about = new AboutFragment();
            FragmentManager manager = getSupportFragmentManager();
            manager.beginTransaction().replace(R.id.fragment_replace_layout, about, about.getTag()).addToBackStack("tag").commit();

        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    protected void onPostResume() {
//        isLogged = settings.getBoolean("is_logged", false);
//        if (!isLogged) {
//            //Log.i(TAG, "not logged in, send to login");
//            Intent k = new Intent(this, LoginActivity.class);
//            startActivity(k);
//        } else {
//            // update user object
//            //Log.i(TAG, "Updating user object...");
//            user = new User(settings.getString("ToxicUser", ""));
//        }

        super.onPostResume();
    }

    public boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    public void setFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.fragment_replace_layout, fragment)
                .commit();
    }
}

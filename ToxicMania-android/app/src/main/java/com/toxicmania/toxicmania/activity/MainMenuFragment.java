package com.toxicmania.toxicmania.activity;


import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;
import android.os.Build;

import com.toxicmania.toxicmania.R;
import com.toxicmania.toxicmania.User;
import com.toxicmania.toxicmania.activity.multiplay.MultiplayActivity;

/**
 * A simple {@link Fragment} subclass.
 */
public class MainMenuFragment extends Fragment {

    private static final String TAG = "MainActivity";
    Button playBtn, multiPlayBtn;
    boolean isLogged;
    SharedPreferences settings;
    private User user;


    public MainMenuFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_main_menu, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        settings = getActivity().getSharedPreferences("PREFS", 0);
        user = new User(settings.getString("ToxicUser", ""));
        boolean firstStart = settings.getBoolean("is_first_start", true);
        if (firstStart) {
            //Log.i(TAG, "Starting for the first time.");

            // first time tasks go here
            Intent i = new Intent(getActivity(), AboutActivity.class);
            i.putExtra("isFirstTime", true);
            startActivity(i);
        }

        // set play button action
        playBtn = (Button)getActivity().findViewById(R.id.play_button);
        playBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                playBtnAction(view);
            }
        });

        // set multi player button action
        multiPlayBtn = (Button) getActivity().findViewById(R.id.mplay_button);

        // TODO: 10/28/2017 change below to enable nultiplayer
        multiPlayBtn.setText("Leaderboard");
        multiPlayBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // TODO: 10/23/2017 make below 0->3
                NavDrawerActivity activity = (NavDrawerActivity)getActivity();
                LeaderBoardFragment leaderboard = new LeaderBoardFragment();
                FragmentManager manager = activity.getSupportFragmentManager();
                manager.beginTransaction().replace(R.id.fragment_replace_layout, leaderboard, leaderboard.getTag()).addToBackStack("tag").commit();
            }
        });
//        multiPlayBtn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                // TODO: 10/23/2017 make below 0->3
//                if (user.getLevel() > 3) {
//                    Intent i = new Intent(getActivity(), MultiplayActivity.class);
//                    startActivity(i);
//                } else {
//                    AlertDialog alertDialog = new AlertDialog.Builder(getActivity()).create();
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

        Button Exitbtn = (Button) getActivity().findViewById(R.id.exit_button);

        Exitbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                exitFunc(v);
            }
        });
    }

    public void playBtnAction(View v) {
        if (isNetworkAvailable()) {
            Intent i = new Intent(getActivity(), PlayActivity.class);
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

    public void exitFunc(final View v) {
        AlertDialog alertDialog = new AlertDialog.Builder(getActivity()).create();
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
                        AlertDialog alert = new AlertDialog.Builder(getActivity()).create();
                        alert.setTitle("Thank you!");
                        alert.setMessage("Your contributions will make the world better.");
                        alert.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        getActivity().finish();
                                        System.exit(0);
                                    }
                                });
                        alert.show();
                    }
                });
        alertDialog.show();
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

}
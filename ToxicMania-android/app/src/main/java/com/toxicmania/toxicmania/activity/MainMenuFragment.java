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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;
import android.os.Build;

import com.android.volley.VolleyError;
import com.toxicmania.toxicmania.R;
import com.toxicmania.toxicmania.User;
import com.toxicmania.toxicmania.VolleyCallback;
import com.toxicmania.toxicmania.VolleyService;
import com.toxicmania.toxicmania.activity.multiplay.MultiplayActivity;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * A simple {@link Fragment} subclass.
 */
public class MainMenuFragment extends Fragment {

    private static final String TAG = "MainActivity";
    Button playBtn, multiPlayBtn, leaderBtn;
    boolean isLogged;
    SharedPreferences settings;
    private User user;
    private ProgressBar progressBar;

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

        leaderBtn = (Button) getActivity().findViewById(R.id.leaderboard_button);
        leaderBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                NavDrawerActivity activity = (NavDrawerActivity)getActivity();
                LeaderBoardFragment leaderboard = new LeaderBoardFragment();
                FragmentManager manager = activity.getSupportFragmentManager();
                manager.beginTransaction().replace(R.id.fragment_replace_layout, leaderboard, leaderboard.getTag()).addToBackStack("tag").commit();
            }
        });

        // set multi player button action
        multiPlayBtn = (Button) getActivity().findViewById(R.id.mplay_button);
        multiPlayBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (user.getLevel() > 3) {
                    Intent i = new Intent(getActivity(), MultiplayActivity.class);
                    startActivity(i);
                } else {
                    AlertDialog alertDialog = new AlertDialog.Builder(getActivity()).create();
                    alertDialog.setTitle("Oops!");
                    alertDialog.setMessage("You must pass level 3 in single player mode to access multi player!");
                    alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            });
                    alertDialog.show();
                }
            }
        });

        Button Exitbtn = (Button) getActivity().findViewById(R.id.exit_button);

        Exitbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                exitFunc(v);
            }
        });

        progressBar = (ProgressBar) getActivity().findViewById(R.id.main_progressBar);
        //get user object
        if (!settings.contains("userDataLoaded")) {
            progressBar.setVisibility(View.VISIBLE);
            playBtn.setEnabled(false);
            multiPlayBtn.setEnabled(false);
            //System.out.println("\n\n>>>>>>>>>>>> getting user data <<<<<<<<<<<<<\n\n");
            updateUserObject();
        }
    }

    private void updateUserObject() {
        if (isNetworkAvailable()) {
            String url = "https://app.ucsccareerfair.com/user/getUser?U_Id=" + user.getID();
            VolleyService volleyService = new VolleyService(getActivity());
            volleyService.volleyGet("POST", url, new VolleyCallback() {
                @Override
                public void notifySuccess(String requestType, JSONObject response) {
                    //Log.d(TAG, "User registration success!\n" + response);
                    try {
                        JSONObject userObj = response.getJSONObject("result");
                        if (userObj != null) {
                            user.setReputation(userObj.getInt("Mark"));
                            user.setLevel(userObj.getInt("Level"));
                            user.setLevelProgress(userObj.getInt("Weight"));
                        }
                        //Log.i(TAG, "User object updated. " + user.toString());
                        SharedPreferences.Editor prefsEditor = settings.edit();
                        prefsEditor.putString("ToxicUser", user.toString());

                        // update badge status
                        if(user.getLevel() > 2 || (user.getLevel() == 2 && user.getLevelProgress() > 5))
                            prefsEditor.putBoolean("beginnerBadge", true);
                        if(user.getReputation() >= 300)
                            prefsEditor.putBoolean("Badge02", true);
                        if(user.getReputation() >= 800)
                            prefsEditor.putBoolean("Badge03", true);
                        if(user.getReputation() >= 1500)
                            prefsEditor.putBoolean("Badge04", true);
                        if(user.getReputation() >= 2500)
                            prefsEditor.putBoolean("Badge05", true);

                        progressBar.setVisibility(View.INVISIBLE);
                        playBtn.setEnabled(true);
                        multiPlayBtn.setEnabled(true);
                        //System.out.println(user.toString());
                        // save log status
                        prefsEditor.putBoolean("is_logged", true);
                        prefsEditor.putBoolean("userDataLoaded", true);
                        prefsEditor.apply();

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void notifyError(String requestType, VolleyError error) {
                    //Log.e(TAG, "User registration error!\n" + error);
                }
            });
        } else {
            final Snackbar snackbar = Snackbar.make(getView(), "Cannot connect to network", Snackbar.LENGTH_LONG);
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
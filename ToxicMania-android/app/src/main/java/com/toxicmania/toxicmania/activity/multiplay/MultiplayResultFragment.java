package com.toxicmania.toxicmania.activity.multiplay;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.toxicmania.toxicmania.R;
import com.toxicmania.toxicmania.User;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * A simple {@link Fragment} subclass.
 */
public class MultiplayResultFragment extends Fragment {

    private ProgressBar timerProgress;
    private TextView timerText, playerCountText, playerCountDesText;
    private Button continueBtn;
    private String TAG = "MultiplayResultFragment";
    CountDownTimer countDownTimer;
    private BroadcastReceiver receiver;
    private User user;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("PREFS", 0);
        user = new User(sharedPreferences.getString("ToxicUser", ""));
        return inflater.inflate(R.layout.fragment_multiplay_result, container, false);
    }

    @Override
    public void onResume() {
        super.onResume();
        SharedPreferences settings = getActivity().getSharedPreferences("PREFS", 0);
        if (settings.contains("isGameFinished")) {
            boolean isGameFinished = settings.getBoolean("isGameFinished", false);
            settings.edit().remove("isGameFinished").apply();
            if (isGameFinished) {
                getActivity().getFragmentManager().popBackStack();
                getActivity().finish();
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        try {
            getActivity().unregisterReceiver(receiver);
        } catch (IllegalArgumentException e) {
            if (e.getMessage().contains("Receiver not registered")) {
                // Ignore this exception. This is exactly what is desired
                Log.w(TAG,"Tried to unregister the reciver when it's not registered");
            } else {
                // unexpected, re-throw
                throw e;
            }
        }
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //init variables
        timerProgress = (ProgressBar) getActivity().findViewById(R.id.timer);
        timerText = (TextView) getActivity().findViewById(R.id.timerText);
        continueBtn = (Button) getActivity().findViewById(R.id.continueBtn);
        playerCountText = (TextView) getActivity().findViewById(R.id.playerCount);
        playerCountDesText = (TextView) getActivity().findViewById(R.id.playerCountDescription);
        continueBtn.setVisibility(View.INVISIBLE);
        continueBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().finish();
            }
        });

        // setting up broadcast receiver
        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String msg = intent.getExtras().getString("text");
                if (msg != null) {
                    Log.i(TAG, msg);
                    try {
                        JSONObject obj = new JSONObject(msg);
                        String event = obj.getString("event");
                        if (event.equals("finish")) {
                            String status = obj.getString("status");
                            if (status.equals("gameFinished")) {
                                String playersStr = obj.getString("players");

                                Intent i = new Intent(getActivity(), MultiplayLeaderBoardActivity.class);
                                i.putExtra("players", playersStr);
                                getActivity().startActivity(i);

                                countDownTimer.cancel();
                            } else if (status.equals("gameNotFinished")) {
                                JSONObject newUser = obj.getJSONObject("U_Obj");
                                if (!user.getName().equals(newUser.getString("U_Name")))
                                    Toast.makeText(getActivity(), newUser.getString("U_Name") + " Finished", Toast.LENGTH_LONG).show();
                                int playerCount = obj.getInt("playerCount");
                                if (playerCount > 1) {
                                    playerCountText.setText("" + playerCount);
                                    playerCountDesText.setText("Players finished.");
                                }
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        };
        IntentFilter filter = new IntentFilter("com.toxicmania.BroadcastReceiver");
        getActivity().registerReceiver(receiver, filter);

        startTimer();
    }

    private void startTimer() {
        final int timeout = 5*60; //in seconds
        timerProgress.setMax(timeout);
        countDownTimer = new CountDownTimer(timeout*1000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                int time = (int)millisUntilFinished/1000;
                timerProgress.setProgress(time);
                timerText.setText("Game will automatically finish in " + time + " seconds." );
            }

            @Override
            public void onFinish() {
                timerProgress.setProgress(0);
                timeout();
            }
        };
        countDownTimer.start();
    }

    private void timeout() {
        System.out.println("++++ timeout results");
        AlertDialog alertDialog = new AlertDialog.Builder(getActivity()).create();
        alertDialog.setTitle("Game session error!");
        alertDialog.setMessage("Sorry! It appears that something went wrong in the game server.");
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        getActivity().finish();
                    }
                });
        alertDialog.show();
    }
}


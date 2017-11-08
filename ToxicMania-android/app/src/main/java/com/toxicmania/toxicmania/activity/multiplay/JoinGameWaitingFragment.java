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
import com.toxicmania.toxicmania.VolleyService;

import org.json.JSONException;
import org.json.JSONObject;

public class JoinGameWaitingFragment extends Fragment {

    private String gameKey;
    private String TAG = "JoinGameWaitingFragment";
    private ProgressBar timerProgress;
    private TextView timerText, playerCountText, playerCountDesText;
    private boolean isGameOn = false;
    private CountDownTimer countDownTimer;
    private BroadcastReceiver receiver;
    private User user;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        MultiplayActivity activity = (MultiplayActivity)getActivity();
        gameKey = activity.gameKey;

        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("PREFS", 0);
        user = new User(sharedPreferences.getString("ToxicUser", ""));

        return inflater.inflate(R.layout.fragment_join_game_waiting, container, false);
    }

    @Override
    public void onDestroy() {
        Log.i(TAG, " === onDestroy");
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

        // init timerProgress progress
        timerProgress = (ProgressBar) getActivity().findViewById(R.id.timer);
        timerText = (TextView) getActivity().findViewById(R.id.timerText);
        startTimer();

        playerCountText = (TextView) getActivity().findViewById(R.id.playerCount);
        playerCountDesText = (TextView) getActivity().findViewById(R.id.playerCountDescription);

        // setting up broadcast receiver
        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String msg = intent.getExtras().getString("text");
                if (msg != null) {
                    Log.i(TAG, "receiver >>>>>" + msg);
                    try {
                        JSONObject obj = new JSONObject(msg);
                        String event = obj.getString("event");
                        if (event.equals("start")) {
                            String status = obj.getString("status");
                            if (status.equals("gameStarted")) {
                                startGame();
                            } else if (status.equals("ready")) {
                                JSONObject newUser = obj.getJSONObject("U_Obj");
                                if (!user.getName().equals(newUser.getString("U_Name")))
                                    Toast.makeText(getActivity(), newUser.getString("U_Name") + " Joined", Toast.LENGTH_LONG).show();
                                int playerCount = obj.getInt("playerCount");
                                if (playerCount > 1) {
                                    playerCountText.setText("" + playerCount);
                                    playerCountDesText.setText("Players connected.");
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
    }

    @Override
    public void onResume() {
        super.onResume();
        SharedPreferences settings = getActivity().getSharedPreferences("PREFS", 0);
        if (settings.contains("isGameFinished")) {
            boolean isGameFinished = settings.getBoolean("isGameFinished", false);
            settings.edit().remove("isGameFinished").apply();
            if (isGameFinished) {
                MultiplayActivity activity = (MultiplayActivity)getActivity();
                activity.displayFragment(new MultiplayResultFragment());
            } else {
                getActivity().getFragmentManager().popBackStack();
                getActivity().finish();
            }
        }
    }

    private void startTimer() {
        final int timeout = 5*60; //in seconds
        timerProgress.setMax(timeout);
        countDownTimer = new CountDownTimer(timeout*1000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                int time = (int)millisUntilFinished/1000;
                timerProgress.setProgress(time);
                timerText.setText("Game will automatically start in " + time + " seconds." );
            }

            @Override
            public void onFinish() {
                if (!isGameOn) {
                    timerProgress.setProgress(0);
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
        };
        countDownTimer.start();
    }

    private void startGame() {
        countDownTimer.cancel();
        isGameOn = true;
        Intent i = new Intent(getActivity(), MultiGamePlayActivity.class);
        i.putExtra("gameKey", gameKey);
        getActivity().startActivity(i);
    }
}

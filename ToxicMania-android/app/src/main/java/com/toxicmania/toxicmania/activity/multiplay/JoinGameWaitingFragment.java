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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.toxicmania.toxicmania.R;
import com.toxicmania.toxicmania.User;
import com.toxicmania.toxicmania.VolleyService;

import org.json.JSONException;
import org.json.JSONObject;

public class JoinGameWaitingFragment extends Fragment {

    private User user;
    private VolleyService volleyService;
    private String gameKey;
    private String TAG = "MultiplayActivity";
    private ProgressBar timerProgress;
    private TextView timerText, playerCountText, playerCountDesText;
    private boolean isGameOn = false;
    private MultiplayActivity mainActivity;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        MultiplayActivity activity = (MultiplayActivity)getActivity();
        user = activity.user;
        volleyService = activity.volleyService;
        gameKey = activity.gameKey;

        // listener for firebase messages
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver((mMessageReceiver),
                new IntentFilter("MyData")
        );

        return inflater.inflate(R.layout.fragment_join_game_waiting, container, false);
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
        CountDownTimer countDownTimer = new CountDownTimer(timeout*1000, 1000) {
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
                }
            }
        };
        countDownTimer.start();
    }

    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String msg = intent.getExtras().getString("text");
            try {
                JSONObject obj = new JSONObject(msg);
                String event = obj.getString("event");
                if (event.equals("start")) {
                    String status = obj.getString("status");
                    if (status.equals("gameStarted")){
                        startGame();
                    }
                    int playerCount = obj.getInt("playerCount");
                    if (playerCount > 1) {
                        playerCountText.setText("" + playerCount);
                        playerCountDesText.setText("Players connected.");
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };

    private void startGame() {
        isGameOn = true;
        Intent i = new Intent(getActivity(), MultiGamePlayActivity.class);
        i.putExtra("gameKey", gameKey);
        getActivity().startActivity(i);
    }
}

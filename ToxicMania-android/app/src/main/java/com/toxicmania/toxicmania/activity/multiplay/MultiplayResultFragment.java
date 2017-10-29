package com.toxicmania.toxicmania.activity.multiplay;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * A simple {@link Fragment} subclass.
 */
public class MultiplayResultFragment extends Fragment {

    private ProgressBar waitProgress, timerProgress;
    private TextView timerText, waitPlayersText, firstPlaceText, secondPlaceText, thirdPlaceText;
    private Button continueBtn;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // listener for firebase messages
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver((mMessageReceiver),
                new IntentFilter("MyData")
        );

        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_multiplay_result, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //init variables
        waitProgress = (ProgressBar) getActivity().findViewById(R.id.progressBar);
        timerProgress = (ProgressBar) getActivity().findViewById(R.id.timer);
        timerText = (TextView) getActivity().findViewById(R.id.timerText);
        waitPlayersText = (TextView) getActivity().findViewById(R.id.waitPlayerText);
        firstPlaceText = (TextView) getActivity().findViewById(R.id.firstPlace);
        secondPlaceText = (TextView) getActivity().findViewById(R.id.secondPlace);
        thirdPlaceText = (TextView) getActivity().findViewById(R.id.thirdPlace);
        continueBtn = (Button) getActivity().findViewById(R.id.continueBtn);

        continueBtn.setVisibility(View.INVISIBLE);
        continueBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().finish();
            }
        });

        startTimer();
    }

    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String msg = intent.getExtras().getString("text");
            try {
                JSONObject obj = new JSONObject(msg);
                String event = obj.getString("event");
                if (event.equals("finish")) {
                    // change ui elements
                    waitPlayersText.setText("Congratulations for the winners!");
                    waitProgress.setVisibility(View.INVISIBLE);
                    timerText.setVisibility(View.INVISIBLE);
                    timerProgress.setVisibility(View.INVISIBLE);
                    continueBtn.setVisibility(View.VISIBLE);

                    String playersStr = obj.getString("players");
                    JSONArray players = new JSONArray(playersStr);
                    // first three places
                    JSONObject leadPlayer = players.getJSONObject(0);
                    firstPlaceText.setText("1 - " + leadPlayer.getString("U_Name"));
                    leadPlayer = players.getJSONObject(1);
                    secondPlaceText.setText("2 - " + leadPlayer.getString("U_Name"));
                    leadPlayer = players.getJSONObject(2);
                    thirdPlaceText.setText("3 - " + leadPlayer.getString("U_Name"));

                    for (int i = 0 ; i < players.length(); i++) {
                        JSONObject player = players.getJSONObject(i);
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };

    private void startTimer() {
        final int timeout = 5*60; //in seconds
        timerProgress.setMax(timeout);
        CountDownTimer countDownTimer = new CountDownTimer(timeout*1000, 1000) {
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
        AlertDialog alertDialog = new AlertDialog.Builder(getActivity()).create();
        alertDialog.setTitle("Game session error!");
        alertDialog.setMessage("Sorry! It appears something wrong in the game server.");
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


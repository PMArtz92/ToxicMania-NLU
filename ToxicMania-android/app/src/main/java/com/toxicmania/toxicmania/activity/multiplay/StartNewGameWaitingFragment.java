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
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import com.android.volley.VolleyError;
import com.toxicmania.toxicmania.R;
import com.toxicmania.toxicmania.User;
import com.toxicmania.toxicmania.VolleyCallback;
import com.toxicmania.toxicmania.VolleyService;
import org.json.JSONException;
import org.json.JSONObject;

public class StartNewGameWaitingFragment extends Fragment {

    private User user;
    private VolleyService volleyService;
    private String gameKey;
    private String TAG = "MultiplayActivity";
    private ProgressBar timerProgress;
    private TextView timerText, playerCountText, playerCountDesText, gameKeyText;
    private Button startGameBtn;
    private boolean isGameOn = false;
    private ProgressBar waitProgress;
    private MultiplayActivity activity;
    private CountDownTimer countDownTimer;
    private BroadcastReceiver receiver;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        activity = (MultiplayActivity) context;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        MultiplayActivity activity = (MultiplayActivity)getActivity();
        user = activity.user;
        volleyService = activity.volleyService;
        gameKey = activity.gameKey;

        return inflater.inflate(R.layout.fragment_start_new_game_waiting, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //disable start game button
        startGameBtn = (Button) getActivity().findViewById(R.id.StartGame);
        startGameBtn.setEnabled(false);

        waitProgress = (ProgressBar) getActivity().findViewById(R.id.progressBar);
        waitProgress.setVisibility(View.INVISIBLE);

        // share game ID
        Button shareGameKey = (Button) getActivity().findViewById(R.id.ShareGameKey);
        shareGameKey.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent share = new Intent(Intent.ACTION_SEND);
                share.setType("text/plain");
                String shareSubject = "ToxicMania Game Invitation";
                String shareBody = "Join with me to play ToxicMania! Game key: " + gameKey;
                share.putExtra(Intent.EXTRA_SUBJECT, shareSubject);
                share.putExtra(Intent.EXTRA_TEXT, shareBody);
                startActivity(Intent.createChooser(share, "Share game key using"));
            }
        });

        //Start game button action
        startGameBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendStartGameRequest();
            }
        });

        // init timerProgress progress
        timerProgress = (ProgressBar) getActivity().findViewById(R.id.timer);
        timerText = (TextView) getActivity().findViewById(R.id.timerText);
        startTimer();

        gameKeyText = (TextView) getActivity().findViewById(R.id.GameID);
        gameKeyText.setText(gameKey);

        playerCountText = (TextView) getActivity().findViewById(R.id.playerCount);
        playerCountDesText = (TextView) getActivity().findViewById(R.id.playerCountDescription);

        // setting up broadcast receiver
        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String msg = intent.getExtras().getString("text");
                if (msg != null) {
                    try {
                        JSONObject obj = new JSONObject(msg);
                        String event = obj.getString("event");
                        if (event.equals("start")) {
                            String status = obj.getString("status");
                            int playerCount = obj.getInt("playerCount");

                            if (status.equals("gameStarted")) {
                                startGame();
                            } else if (status.equals("ready")) {
                                startGameBtn.setEnabled(true);
                                JSONObject newUser = obj.getJSONObject("U_Obj");
                                if (!user.getName().equals(newUser.getString("U_Name")))
                                    Toast.makeText(getActivity(), newUser.getString("U_Name") + " Joined", Toast.LENGTH_LONG).show();
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
                    timeout();
                }
            }
        };
        countDownTimer.start();
    }

    private void timeout() {
        System.out.println("++++ timeout startNew");
        if (startGameBtn.isEnabled()) {
            startGameBtn.callOnClick();
        } else {
            AlertDialog alertDialog = new AlertDialog.Builder(getActivity()).create();
            alertDialog.setTitle("Game session expired!");
            alertDialog.setMessage("Sorry! We could not find any players for this game session.");
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

    private void sendStartGameRequest() {
        waitProgress.setVisibility(View.VISIBLE);
        JSONObject finalObject = new JSONObject();
        try {
            finalObject.put("U_Id", user.getID());
            finalObject.put("Se_Id", gameKey);
            finalObject.put("check", 0);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        //send start game request to server
        String url = "https://app.ucsccareerfair.com/multiplay/startGame";
        volleyService.volleyPost("POST", url, finalObject, new VolleyCallback() {
            @Override
            public void notifySuccess(String requestType, JSONObject response) {
                Log.d(TAG, "Volley: Requesting to start multiplayer game session : " + response);
                waitProgress.setVisibility(View.GONE);
                try {
                    boolean success = response.getBoolean("success");
                    if (success) {
                        Toast.makeText(getActivity(), "Game started!", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(getActivity(), "Server error!", Toast.LENGTH_LONG).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }

            @Override
            public void notifyError(String requestType, VolleyError error) {
                Log.e(TAG, "Volley Error: Requesting to start multiplayer game session : " + error);
                Toast.makeText(getActivity(), "Server error!", Toast.LENGTH_LONG).show();
                waitProgress.setVisibility(View.GONE);
            }
        });
    }

    private void startGame() {
        countDownTimer.cancel();
        isGameOn = true;
        Intent i = new Intent(activity, MultiGamePlayActivity.class);
        i.putExtra("gameKey", gameKey);
        startActivity(i);
    }
}

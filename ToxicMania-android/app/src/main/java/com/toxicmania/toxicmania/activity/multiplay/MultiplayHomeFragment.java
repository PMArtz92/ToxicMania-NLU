package com.toxicmania.toxicmania.activity.multiplay;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.SupportActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.android.volley.VolleyError;
import com.toxicmania.toxicmania.R;
import com.toxicmania.toxicmania.User;
import com.toxicmania.toxicmania.VolleyCallback;
import com.toxicmania.toxicmania.VolleyService;
import com.toxicmania.toxicmania.activity.MainActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class MultiplayHomeFragment extends Fragment {

    private User user;
    private VolleyService volleyService;
    private String TAG = "MultiplayActivity";
    public String firebaseToken;
    private Button joinGame;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        MultiplayActivity activity = (MultiplayActivity)getActivity();
        user = activity.user;
        volleyService = activity.volleyService;
        firebaseToken = activity.firebaseToken;
        return inflater.inflate(R.layout.fragment_muliplay_home, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // disable progress
        final ProgressBar waitProgress = (ProgressBar) getActivity().findViewById(R.id.progressBar);
        waitProgress.setVisibility(View.INVISIBLE);

        // start new game action
        Button initNewGame = (Button) getActivity().findViewById(R.id.InitNewGame);
        initNewGame.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // enable wait progress
                waitProgress.setVisibility(View.VISIBLE);

                JSONObject finalObject = new JSONObject();
                try {
                    JSONObject tmp = new JSONObject();
                    tmp.put("Firebase_Id", firebaseToken);
                    tmp.put("U_Id", user.getID());
                    tmp.put("U_Name", user.getName());
                    tmp.put("U_Img", user.getUrl());
                    finalObject.put("U_Obj", tmp);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                //send create game request to server
                // TODO: 10/24/2017 change url
                String url = "https://app.ucsccareerfair.com/multiplay/newGame";
                volleyService.volleyPost("POST", url, finalObject, new VolleyCallback() {
                    @Override
                    public void notifySuccess(String requestType, JSONObject response) {
                        //Log.d(TAG, "Volley: Requesting new multiplayer game session " + response);
                        waitProgress.setVisibility(View.GONE);
                        try {
                            boolean success = response.getBoolean("success");
                            if (success) {
                                String key = response.getString("Se_id");
                                // send to next fragment
                                MultiplayActivity activity = (MultiplayActivity)getActivity();
                                activity.gameKey = key;
                                activity.displayFragment(new StartNewGameWaitingFragment());
                            } else {
                                Toast.makeText(getActivity(), "Server error!", Toast.LENGTH_LONG).show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void notifyError(String requestType, VolleyError error) {
                        //Log.e(TAG, "Volley Error: Requesting new multiplayer game session " + error);
                        Toast.makeText(getActivity(), "Server error!", Toast.LENGTH_LONG).show();
                        waitProgress.setVisibility(View.GONE);
                    }
                });
            }
        });

        // join game action
        final EditText gameIDText = (EditText) getActivity().findViewById(R.id.gameID);
        gameIDText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() == 9) {
                    joinGame.setEnabled(true);
                } else {
                    joinGame.setEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable s) { }
        });
        joinGame = (Button) getActivity().findViewById(R.id.JoinGame);
        joinGame.setEnabled(false);
        joinGame.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String gameID = gameIDText.getText().toString();

                JSONObject finalObject = new JSONObject();
                try {
                    JSONObject tmp = new JSONObject();
                    tmp.put("Firebase_Id", firebaseToken);
                    tmp.put("U_Id", user.getID());
                    tmp.put("U_Name", user.getName());
                    tmp.put("U_Img", user.getUrl());
                    finalObject.put("U_Obj", tmp);
                    finalObject.put("Se_Id", gameID);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                //send join game request to server
                String url = "https://app.ucsccareerfair.com/multiplay/connect";
                volleyService.volleyPost("POST", url, finalObject, new VolleyCallback() {
                    @Override
                    public void notifySuccess(String requestType, JSONObject response) {
                        //Log.d(TAG, "Volley: Requesting to join multiplayer game session : " + response);
                        waitProgress.setVisibility(View.GONE);
                        boolean success = false;
                        try {
                            success = response.getBoolean("success");
                            if (success) {
                                // send to next fragment
                                MultiplayActivity activity = (MultiplayActivity)getActivity();
                                activity.gameKey = gameID;
                                activity.displayFragment(new JoinGameWaitingFragment());
                            } else {
                                Toast.makeText(getActivity(), "Server error!", Toast.LENGTH_LONG).show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }

                    @Override
                    public void notifyError(String requestType, VolleyError error) {
                        //Log.e(TAG, "Volley Error: Requesting new multiplayer game session : " + error);
                        Toast.makeText(getActivity(), "Server error!", Toast.LENGTH_LONG).show();
                        waitProgress.setVisibility(View.GONE);
                    }
                });
            }
        });
    }
}

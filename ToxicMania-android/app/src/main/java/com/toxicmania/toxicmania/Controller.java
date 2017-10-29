package com.toxicmania.toxicmania;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.android.volley.VolleyError;
import com.toxicmania.toxicmania.activity.MainActivity;
import com.toxicmania.toxicmania.activity.PlayActivity;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;

/**
 * Created by Vihanga Liyanage on 10/12/2017.
 */

public class Controller {

    private Question[] newQuestions, answeredQuestions;
    private String TAG = "Controller";
    private int newQIndex = 0, answeredQIndex = 0;
    private VolleyService volleyService;
    public User user;
    private int Q_BULK_COUNT = 50;
    private Context context;
    private PlayActivity playActivity;

    public Controller(Context context, User user, PlayActivity playActivity) {
        this.context = context;
        volleyService = new VolleyService(context);
        this.user = user;
        this.playActivity = playActivity;
    }

    public void getNextQuestion() {
        if (newQuestions != null && newQIndex < newQuestions.length) {
            playActivity.setNextQuestion(newQuestions[newQIndex]);
            newQIndex++;
        } else {
            if (isNetworkAvailable()) {
                // TODO: 10/24/2017 change url
//                 String url = "https://demo6365273.mockable.io/get-next-question-set?user=" + user.getID() + "&reputation=" + user.getReputation();
                String url = "https://app.ucsccareerfair.com/question/NextTenQuestion?user=" + user.getID() + "&reputation=" + user.getReputation();
                //Log.i(TAG, "Question set loading...");
                //Log.i(TAG, url);
                volleyService.volleyGet("GETCALL", url, new VolleyCallback() {
                    @Override
                    public void notifySuccess(String requestType, JSONObject response) {
                        try {
                            JSONArray array = response.getJSONArray("result");
                            Q_BULK_COUNT = array.length();

                            if (newQuestions != null) {
                                // submit answered questions set to server
                                submitAnswersToServer(Arrays.copyOfRange(answeredQuestions, 0, answeredQIndex));
                                answeredQIndex = 0;
                            }

                            newQuestions = new Question[Q_BULK_COUNT];
                            answeredQuestions = new Question[Q_BULK_COUNT];
                            for (int i = 0; i < array.length(); i++) {
                                JSONObject object = array.getJSONObject(i);
                                newQuestions[i] = new Question(object.getString("Q_Id"), object.getString("Text"));
                            }
                            playActivity.setNextQuestion(newQuestions[0]);
                            newQIndex = 1;
                            //Log.i(TAG, "Question set loading complete.");
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override // Error handler
                    public void notifyError(String requestType, VolleyError error) {
                        //Log.d(TAG, "Volley JSON get error!\n" + error);
                    }
                });
            } else {
                new AlertDialog.Builder(playActivity)
                        .setTitle("No Network!")
                        .setMessage("We couldn't find a working network connection.")
                        .setNeutralButton("OK", new DialogInterface.OnClickListener() {

                            public void onClick(DialogInterface dialog, int whichButton) {
                                playActivity.finish();
                            }}).show();
            }
        }
    }

    private void submitAnswersToServer(Question[] questions) {
        // building send object
        JSONObject finalObject = new JSONObject();
        JSONArray qArray = new JSONArray();
        try {
            for (Question q:questions) {
                JSONObject qObj = new JSONObject();
                qObj.put("id", q.getId());
                qObj.put("readability", q.getReadability());
                qObj.put("toxicity", q.getToxicity());
                qObj.put("skipped", q.isSkipped());
                qArray.put(qObj);
            }
            finalObject.put("answers", qArray);
            finalObject.put("u_id", user.getID());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        if (isNetworkAvailable()) {
            String url = "https://app.ucsccareerfair.com/answer/Singleplay";
            volleyService.volleyPost("POST", url, finalObject, new VolleyCallback() {
                @Override
                public void notifySuccess(String requestType, JSONObject response) {
                    //Log.d(TAG, "Volley JSON post success!\n" + response);
                    try {
                        JSONArray result = response.getJSONArray("result");
                        JSONObject userObj = result.getJSONObject(0);
                        if (userObj != null) {
                            user.setReputation(userObj.getInt("Mark"));
                        }
                        //Log.i(TAG, "User object updated. New reputation: " + user.getReputation());
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void notifyError(String requestType, VolleyError error) {
                    //Log.d(TAG, "Volley JSON post error!\n" + error);
                }
            });
        } else {
            new AlertDialog.Builder(playActivity)
                    .setTitle("No Network!")
                    .setMessage("We couldn't find a working network connection.")
                    .setNeutralButton("OK", new DialogInterface.OnClickListener() {

                        public void onClick(DialogInterface dialog, int whichButton) {
                            playActivity.finish();
                        }}).show();
        }
    }

    private boolean isNetworkAvailable() {
        System.out.println("Checking network...");
        ConnectivityManager connectivityManager
                = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    public void submitAnswersOnStop(User user) {
        if (answeredQIndex > 0) {
            submitAnswersToServer(Arrays.copyOfRange(answeredQuestions, 0, answeredQIndex));
            answeredQuestions = new Question[Q_BULK_COUNT];
            answeredQIndex = 0;
        }
        // building send object
        JSONObject finalObject = new JSONObject();
        try {
            finalObject.put("U_Id", user.getID());
            finalObject.put("Weight", user.getLevelProgress());
            finalObject.put("U_Name", user.getName());
            finalObject.put("Level", user.getLevel());
            finalObject.put("Mark", user.getReputation());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        String url = "https://app.ucsccareerfair.com/user/updateUser";
        volleyService.volleyPost("POST", url, finalObject, new VolleyCallback() {
            @Override
            public void notifySuccess(String requestType, JSONObject response) {
                //Log.d(TAG, "User object posted!\n" + response);
            }

            @Override
            public void notifyError(String requestType, VolleyError error) {
                //Log.d(TAG, "User object post error!\n" + error);
            }
        });
    }

    public void setAnswer(Question question) {
        answeredQuestions[answeredQIndex] = question;
        if (answeredQIndex > 10) {
            submitAnswersToServer(Arrays.copyOfRange(answeredQuestions, 0, answeredQIndex));
            answeredQuestions = new Question[Q_BULK_COUNT];
            answeredQIndex = 0;
        } else {
            answeredQIndex ++;
        }
    }

    public void setLevel(int level) {
        user.setLevel(level);
    }

    public void setLevelProgress(int levelProgress) {
        user.setLevelProgress(levelProgress);
    }
}

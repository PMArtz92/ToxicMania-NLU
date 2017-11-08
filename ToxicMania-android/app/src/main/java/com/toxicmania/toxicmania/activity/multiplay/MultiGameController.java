package com.toxicmania.toxicmania.activity.multiplay;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import com.android.volley.VolleyError;
import com.toxicmania.toxicmania.Question;
import com.toxicmania.toxicmania.User;
import com.toxicmania.toxicmania.VolleyCallback;
import com.toxicmania.toxicmania.VolleyService;
import com.toxicmania.toxicmania.activity.PlayActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Vihanga Liyanage on 10/12/2017.
 */

class MultiGameController {

    private Question[] newQuestions;
    private String TAG = "MultiplayActivity";
    private int qCount = 0;
    private VolleyService volleyService;
    public User user;
    private String gameKey;
    private MultiGamePlayActivity playActivity;

    MultiGameController(MultiGamePlayActivity playActivity, User user, String gameKey) {
        this.playActivity = playActivity;
        volleyService = new VolleyService(playActivity);
        this.user = user;
        this.gameKey = gameKey;
    }

    void getNextQuestion() {
        if (newQuestions != null && qCount < newQuestions.length) {
            playActivity.setNextQuestion(newQuestions[qCount]);
            qCount ++;
        } else {
            String url = "https://app.ucsccareerfair.com/question/SessionQuestions?Se_Id=" + gameKey;
            Log.i(TAG, "Question set loading...");
            volleyService.volleyGet("GETCALL", url, new VolleyCallback() {
                @Override
                public void notifySuccess(String requestType, JSONObject response) {
                    try {
                        JSONArray array = response.getJSONArray("result");
                        if (newQuestions != null) {
                            // submit old questions set to server
                            submitQuestionSetToServer();
                        }

                        newQuestions = new Question[array.length()];
                        for (int i = 0; i < array.length(); i++) {
                            JSONObject object = array.getJSONObject(i);
                            newQuestions[i] = new Question(object.getString("Q_Id"), object.getString("Text"));
                            newQuestions[i].setSkipped(true);
                        }
                        playActivity.setNextQuestion(newQuestions[0]);
                        qCount = 1;
                        playActivity.setLevelBound(array.length());
                        //Log.i(TAG, "Question set loaded!");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                @Override // Error handler
                public void notifyError(String requestType, VolleyError error) {
                    //Log.d(TAG, "Volley JSON get error!\n" + error);
                }
            });
        }
    }

    void submitQuestionSetToServer() {
        // building send object
        JSONObject finalObject = new JSONObject();
        JSONArray qArray = new JSONArray();
        try {
            for (Question q:newQuestions) {
                JSONObject qObj = new JSONObject();
                qObj.put("id", q.getId());
                qObj.put("readability", q.getReadability());
                qObj.put("toxicity", q.getToxicity());
                qObj.put("skipped", q.isSkipped());
                qArray.put(qObj);
            }
            finalObject.put("answers", qArray);
            finalObject.put("u_id", user.getID());
            finalObject.put("mult_session", gameKey);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        String url = "https://app.ucsccareerfair.com/answer/Multiplay";
        volleyService.volleyPost("POST", url, finalObject, new VolleyCallback() {
            @Override
            public void notifySuccess(String requestType, JSONObject response) {
                //Log.d(TAG, "Volley POST success : " + response);
            }

            @Override
            public void notifyError(String requestType, VolleyError error) {
                //Log.e(TAG, "Volley POST error : " + error);
            }
        });
    }

    private boolean isNetworkAvailable() {
        // System.out.println("Checking network...");
        ConnectivityManager connectivityManager
                = (ConnectivityManager) playActivity.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    void submitAnswer(Question question) {
        newQuestions[qCount - 1] = question;
    }

    public void setLevel(int level) {
        user.setLevel(level);
    }

    void setLevelProgress(int levelProgress) {
        user.setLevelProgress(levelProgress);
    }
}

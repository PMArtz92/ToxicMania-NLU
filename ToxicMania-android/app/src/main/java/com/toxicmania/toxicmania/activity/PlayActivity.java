package com.toxicmania.toxicmania.activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.toxicmania.toxicmania.Controller;
import com.toxicmania.toxicmania.Question;
import com.toxicmania.toxicmania.R;
import com.toxicmania.toxicmania.User;

public class PlayActivity extends AppCompatActivity {

    private static final String TAG = "PlayActivity";
    private Controller controller;
    private CheckBox readableCheck;
    private RatingBar rating;
    private Button submitBtn, skipBtn;
    private ProgressBar p, levelProgressBar;
    private TextView questionText, levelText;
    private int level;
    private int levelProgress = 0;
    private int[] levelBounds = {15, 30, 60, 100, 200, 500, 1000, 2000};
//    private int[] levelBounds = {3, 5, 10, 100, 200, 500, 1000};
    private int curRep = 0;
    private Question curQuestion;
    private ImageView rateSimile;
    private Animation smileAnimation;
    private String[] toxicities = {"notatall", "somewhat", "very"};
    private User user;
    SharedPreferences sharedPreferences,badgeLevels;

    @Override
    protected void onStop() {
        // saving user object
        // TODO: 10/23/2017 uncomment below to save user object
        user.setLevel(level);
        user.setLevelProgress(levelProgress);
        SharedPreferences.Editor prefsEditor = sharedPreferences.edit();
        prefsEditor.putString("ToxicUser", controller.user.toString());
        prefsEditor.apply();

        controller.submitAnswersOnStop(user);
        super.onStop();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //Log.i(TAG, "Starting Play activity...");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play);

        //getting user from shared preferences and setting player info
        sharedPreferences = getSharedPreferences("PREFS", 0);

        // TODO: 10/24/2017 change below for pasan 
//        String temp = "115859897652544893855,Vihanga Liyanage,vihangaliyanage007@gmail.com,555,3,6,https://lh6.googleusercontent.com/-TcU0a4zUy7M/AAAAAAAAAAI/AAAAAAAAAsc/eV0iniaGRm0/photo.jpg";
//        user = new User(temp);
        user = new User(sharedPreferences.getString("ToxicUser", ""));
        System.out.println(user.toString());
        System.out.println(user.getLevel() + ":" + user.getLevelProgress()+ ":"+ user.getReputation());

        controller = new Controller(this, user, this);
        level = user.getLevel();
        levelProgress = user.getLevelProgress();
        curRep = user.getReputation();

        // Init variables
        levelText = (TextView) findViewById(R.id.level_value);
        questionText = (TextView) findViewById(R.id.questionTextView);
        levelProgressBar = (ProgressBar) findViewById(R.id.level_progress);
        p = (ProgressBar) findViewById(R.id.progressBar);
        rating = (RatingBar) findViewById(R.id.ratingBar);
        submitBtn = (Button) findViewById(R.id.submitBtn);
        skipBtn = (Button) findViewById(R.id.skipBtn);
        readableCheck = (CheckBox) findViewById(R.id.checkBox);
        rateSimile = (ImageView)findViewById(R.id.rateSmile);
        smileAnimation = AnimationUtils.loadAnimation(this,R.anim.smile_slide);
        final Toast ratingToast = Toast.makeText(getBaseContext(), "",Toast.LENGTH_LONG);

        // set fonts
        Typeface robotoRegular = Typeface.createFromAsset(getAssets(), "fonts/Roboto-Light.ttf");
        questionText.setTypeface(robotoRegular);
        readableCheck.setTypeface(robotoRegular);

        questionText.setMovementMethod(new ScrollingMovementMethod());

        //set level progress bar and level text
        levelProgressBar.setMax(levelBounds[level - 1]);
        levelProgressBar.setProgress(levelProgress);
        levelText.setText(""+ level);

        // Loading first question
        getNextQuestionHandler();

        //disable submit btn if not rated
        rating.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            @Override
            public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
                if (rating == 0.0) {
                    submitBtn.setEnabled(false);
                    rateSimile.clearAnimation();
                    rateSimile.setVisibility(View.INVISIBLE);
                } else {
                    submitBtn.setEnabled(true);
                    //set similes according to rating
                    if (rating == 1.0){
                        rateSimile.setImageResource(R.drawable.rating_smile_face);
                    }
                    else if(rating == 2.0){
                        rateSimile.setImageResource(R.drawable.rating_nutral_face);
                    }
                    else if(rating == 3.0){
                        rateSimile.setImageResource(R.drawable.rating_angry_face);
                    }
                    rateSimile.startAnimation(smileAnimation);
                    rateSimile.setVisibility(View.VISIBLE);

                    //showing toast
                    if (level == 1) {
                        String[] messages = {"Not toxic", "Somewhat toxic", "Very toxic"};
                        ratingToast.setText(messages[(int)rating - 1]);
                        ratingToast.show();
                    }
                }
            }
        });


        // disable rating if not readable
        readableCheck.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if ( !isChecked ) {
                    rating.setEnabled(false);
                    submitBtn.setEnabled(true);
                } else {
                    rating.setEnabled(true);
                    if (rating.getRating() == 0.0)
                        submitBtn.setEnabled(false);
                }
            }
        });
    }

    public void submitAnswer(View v) {
        if (readableCheck.isChecked()) {
            curQuestion.setToxicity(toxicities[(int)rating.getRating() - 1]);
            controller.setAnswer(curQuestion);
            levelProgress++;
            controller.setLevelProgress(levelProgress);
            levelProgressBar.setProgress(levelProgress);
            getNextQuestionHandler();
        } else {
            //ask for confirmation
            new AlertDialog.Builder(this)
                    .setTitle("Confirm")
                    .setMessage("Are you sure this is not readable?")
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

                        public void onClick(DialogInterface dialog, int whichButton) {
                            curQuestion.setReadability(false);
                            controller.setAnswer(curQuestion);
                            getNextQuestionHandler();
                        }})
                    .setNegativeButton(android.R.string.no, null).show();
        }

        // level change
        if (levelProgress == levelBounds[level -1]) {
            user.setLevelProgress(0);
            level++;
            controller.setLevel(level);
            Intent intent = new Intent(PlayActivity.this, LevelPassActivity.class);
            intent.putExtra("NewLevel", level);
            startActivity(intent);

            levelText.setText(""+ level);
            levelProgress = 0;
            levelProgressBar.setMax(levelBounds[level - 1]);
            levelProgressBar.setProgress(levelProgress);
        }

        //achievement badges
        badgeLevels = getSharedPreferences("PREFS", 0);

        SharedPreferences.Editor userBadge = sharedPreferences.edit();

        if(level == 2 && levelProgress == 5 && !badgeLevels.contains("beginnerBadge")){
            badgeActivityDisplay("beginner");
            userBadge.putBoolean("beginnerBadge", true);
        }else if(curRep >= 300 && !badgeLevels.contains("Badge02")){
            badgeActivityDisplay("badge02");
            userBadge.putBoolean("Badge02", true);
        }
        else if(curRep >= 800 && !badgeLevels.contains("Badge03")){
            badgeActivityDisplay("badge03");
            userBadge.putBoolean("Badge03", true);
        }
        else if(curRep >= 1500 && !badgeLevels.contains("Badge04")){
            badgeActivityDisplay("badge04");
            userBadge.putBoolean("Badge04", true);
        }
        else if(curRep >= 2500 && !badgeLevels.contains("Badge05")){
            badgeActivityDisplay("badge05");
            userBadge.putBoolean("Badge05", true);
        }
        userBadge.apply();

        //give reputation updates
        if (levelProgress % 11 == 0) {
            user = new User(sharedPreferences.getString("ToxicUser", ""));
            if (curRep != user.getReputation()) {
                curRep = user.getReputation();
                final Snackbar snackbar = Snackbar.make(v, "Reputation updated: " + curRep + " points", Snackbar.LENGTH_LONG);
                snackbar.setAction("OK", new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                snackbar.dismiss();
                            }
                        });
                snackbar.show();
            }
        }
    }

    public void skipQuestion(View v) {
        curQuestion.setSkipped(true);
        controller.setAnswer(curQuestion);
        getNextQuestionHandler();
    }

    // Prepare UI for next question and request question from controller
    private void getNextQuestionHandler() {
        //Log.i(TAG, "getNextQuestionHandler");
        p.setVisibility(View.VISIBLE);
        questionText.setText("Loading...");
        submitBtn.setEnabled(false);
        skipBtn.setEnabled(false);
        readableCheck.setEnabled(false);
        rating.setEnabled(false);
        rateSimile.clearAnimation();
        rateSimile.setVisibility(View.INVISIBLE);

        controller.getNextQuestion();
    }

    // called by controller to set the next question
    public void setNextQuestion(Question question) {
        curQuestion = question;
        p.setVisibility(View.GONE);
        questionText.setText(question.getText());
        questionText.scrollTo(0, 0);
        rating.setRating(0);
        rating.setEnabled(true);
        readableCheck.setEnabled(true);
        readableCheck.setChecked(true);
        skipBtn.setEnabled(true);
    }

    //Badge Activity Display
    private void badgeActivityDisplay(String levelName){
        Intent i = new Intent(PlayActivity.this, BadgeActivity.class);
        i.putExtra("levelName", levelName);
        startActivity(i);
    }

}

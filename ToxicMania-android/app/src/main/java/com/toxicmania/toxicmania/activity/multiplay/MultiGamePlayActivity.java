package com.toxicmania.toxicmania.activity.multiplay;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
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

import com.toxicmania.toxicmania.Question;
import com.toxicmania.toxicmania.R;
import com.toxicmania.toxicmania.User;

public class MultiGamePlayActivity extends AppCompatActivity {

    private static final String TAG = "PlayActivity";
    private MultiGameController controller;
    private CheckBox readableCheck;
    private RatingBar rating;
    private Button submitBtn, skipBtn;
    private ProgressBar p, levelProgressBar;
    private TextView questionText;
    private int level;
    private int levelProgress = 0;
    private String[] toxicities = {"notatall", "somewhat", "very"};
    // TODO: 10/24/2017 change below
    private int levelBound = 5;
    private Question curQuestion;
    private ImageView rateSimile;
    private Animation smileAnimation;
    private String gameKey;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //Log.i(TAG, "Starting Play activity...");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_multy_play);

        //getting user from shared preferences and setting player info
        SharedPreferences sharedPreferences = getSharedPreferences("PREFS", 0);
        User user = new User(sharedPreferences.getString("ToxicUser", ""));

        //get session id from bundle
        Bundle extraData = getIntent().getExtras();
        gameKey = extraData.getString("gameKey");

        controller = new MultiGameController(this, user, gameKey);

        // Init variables
        questionText = (TextView) findViewById(R.id.questionTextView);
        levelProgressBar = (ProgressBar) findViewById(R.id.level_progress);
        p = (ProgressBar) findViewById(R.id.progressBar);
        rating = (RatingBar) findViewById(R.id.ratingBar);
        submitBtn = (Button) findViewById(R.id.submitBtn);
        skipBtn = (Button) findViewById(R.id.skipBtn);
        readableCheck = (CheckBox) findViewById(R.id.checkBox);
        rateSimile = (ImageView)findViewById(R.id.rateSmile);
        smileAnimation = AnimationUtils.loadAnimation(this,R.anim.smile_slide);

        // set fonts
        Typeface robotoRegular = Typeface.createFromAsset(getAssets(), "fonts/Roboto-Regular.ttf");
        questionText.setTypeface(robotoRegular);
        readableCheck.setTypeface(robotoRegular);

        questionText.setMovementMethod(new ScrollingMovementMethod());

        //set level progress bar and level text
        levelProgressBar.setMax(levelBound);
        levelProgressBar.setProgress(levelProgress);

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

    @Override
    public void onBackPressed() {
        //ask for confirmation
        new AlertDialog.Builder(this)
                .setTitle("Are you sure?")
                .setMessage("Your game session will be lost!")
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        exit(false);
                    }})
                .setNegativeButton(android.R.string.no, null).show();
    }

    private void exit(boolean state) {
        controller.submitQuestionSetToServer();
        SharedPreferences sharedPreferences = getSharedPreferences("PREFS", 0);
        SharedPreferences.Editor prefsEditor = sharedPreferences.edit();
        prefsEditor.putBoolean("isGameFinished", state);
        prefsEditor.apply();
        finish();
    }

    public void submitAnswer(View v) {
        if (readableCheck.isChecked()) {
            curQuestion.setToxicity(toxicities[(int)rating.getRating() - 1]);
        } else {
            //ask for confirmation
            new AlertDialog.Builder(this)
                    .setTitle("Confirm")
                    .setMessage("Are you sure this is not readable?")
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

                        public void onClick(DialogInterface dialog, int whichButton) {
                            curQuestion.setReadability(false);
                        }})
                    .setNegativeButton(android.R.string.no, null).show();
        }
        levelProgress++;
        if (levelProgress == levelBound) {
            exit(true);
        } else {
            controller.setLevelProgress(levelProgress);
            levelProgressBar.setProgress(levelProgress);
            curQuestion.setSkipped(false);
            controller.submitAnswer(curQuestion);
            getNextQuestionHandler();
        }
    }

    public void skipQuestion(View v) {
        levelProgress++;
        if (levelProgress == levelBound) {
            exit(true);
        } else {
            controller.setLevelProgress(levelProgress);
            levelProgressBar.setProgress(levelProgress);
            curQuestion.setSkipped(true);
            controller.submitAnswer(curQuestion);
            getNextQuestionHandler();
        }
    }

    // Prepare UI for next question and request question from controller
    private void getNextQuestionHandler() {
        //Log.i(TAG, "getNextQuestionHandler");
        p.setVisibility(View.VISIBLE);
        questionText.setText("Loading...");
        submitBtn.setEnabled(false);
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
        rating.setRating(0);
        rating.setEnabled(true);
        readableCheck.setEnabled(true);
        readableCheck.setChecked(true);
    }
}

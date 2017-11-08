package com.toxicmania.toxicmania.activity.multiplay;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.CountDownTimer;
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

    private static final String TAG = "MultiplayActivity";
    private MultiGameController controller;
    private CheckBox readableCheck;
    private Button submitBtn, skipBtn;
    private ProgressBar p, levelProgressBar, timerProgress;
    private TextView questionText, timerText;
    private int level, rateValue=0, levelProgress=0;
    private String[] toxicities = {"notatall", "somewhat", "very"};
    private int levelBound = 20;
    private Question curQuestion;
    private Animation smileAnimation;
    private String gameKey;
    private ImageView img01, img02, img03;
    boolean imgb01 = false, imgb02= false, imgb03= false;
    private CountDownTimer countDownTimer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ////Log.i(TAG, "Starting Play activity...");
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
        submitBtn = (Button) findViewById(R.id.submitBtn);
        skipBtn = (Button) findViewById(R.id.skipBtn);
        readableCheck = (CheckBox) findViewById(R.id.checkBox);
        smileAnimation = AnimationUtils.loadAnimation(this,R.anim.smile_slide);

        // set fonts
        Typeface robotoLight = Typeface.createFromAsset(getAssets(), "fonts/Roboto-Light.ttf");
        questionText.setTypeface(robotoLight);
        readableCheck.setTypeface(robotoLight);

        questionText.setMovementMethod(new ScrollingMovementMethod());

        //set level progress bar and level text
        levelProgressBar.setMax(levelBound);
        levelProgressBar.setProgress(levelProgress);

        img01 = (ImageView)findViewById(R.id.rtSmile);
        img02 = (ImageView)findViewById(R.id.rtNutral);
        img03 = (ImageView)findViewById(R.id.rtAngry);

        // disable rating if not readable
        readableCheck.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if ( !isChecked ) {
                    img01.setImageResource(R.drawable.new_rating_smile_grey);
                    img02.setImageResource(R.drawable.new_rating_nutral_grey);
                    img03.setImageResource(R.drawable.new_rating_angry_grey);
                    imgb01 = false;
                    imgb02 = false;
                    imgb03 = false;
                    rateValue = 0;
                    //Log.i(TAG,"Check box ---> " + rateValue);
                    //rating.setEnabled(false);
                    submitBtn.setEnabled(true);
                } else {
                    //rating.setEnabled(true);
                    if (rateValue == 0)
                        submitBtn.setEnabled(false);
                }
            }
        });

        img01.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!imgb01){
                    if(readableCheck.isChecked()){
                        img01.setImageResource(R.drawable.new_rating_smile);
                        img02.setImageResource(R.drawable.new_rating_nutral_grey);
                        img03.setImageResource(R.drawable.new_rating_angry_grey);
                        imgb01 = true;
                        rateValueSet(1);
                        imgb02 = false;
                        imgb03 = false;
                        //Log.i(TAG,"Image 01" + rateValue);
                    }
                }

            }
        });

        img02.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!imgb02){
                    if(readableCheck.isChecked()){
                        img01.setImageResource(R.drawable.new_rating_smile_grey);
                        img02.setImageResource(R.drawable.new_rating_nutral);
                        img03.setImageResource(R.drawable.new_rating_angry_grey);
                        imgb01 = false;
                        rateValueSet(2);
                        imgb02 = true;
                        imgb03 = false;
                        //Log.i(TAG,"Image 02" + rateValue);
                    }
                }

            }
        });

        img03.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!imgb03){
                    if(readableCheck.isChecked()){
                        img01.setImageResource(R.drawable.new_rating_smile_grey);
                        img02.setImageResource(R.drawable.new_rating_nutral_grey);
                        img03.setImageResource(R.drawable.new_rating_angry);
                        imgb01 = false;
                        rateValueSet(3);
                        imgb02 = false;
                        imgb03 = true;
                        //Log.i(TAG,"Image 03" + rateValue);
                    }
                }

            }
        });

        // init timerProgress progress
        timerProgress = (ProgressBar) findViewById(R.id.timer);
        timerText = (TextView) findViewById(R.id.timerText);
        startTimer();

        // Loading first question
        getNextQuestionHandler();
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
        countDownTimer.cancel();
        controller.submitQuestionSetToServer();
        SharedPreferences sharedPreferences = getSharedPreferences("PREFS", 0);
        SharedPreferences.Editor prefsEditor = sharedPreferences.edit();
        prefsEditor.putBoolean("isGameFinished", state);
        prefsEditor.apply();
        finish();
    }

    public void submitAnswer(View v) {
        if (readableCheck.isChecked()) {
            curQuestion.setToxicity(toxicities[rateValue - 1]);
        } else {
            //ask for confirmation
            new AlertDialog.Builder(this)
                    .setTitle("Confirm")
                    .setMessage("Are you sure this is not readable?")
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

                        public void onClick(DialogInterface dialog, int whichButton) {
                            curQuestion.setReadability(false);
                            dialog.dismiss();
                        }})
                    .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    }).show();
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
        readableCheck.setEnabled(false);
        readableCheck.setChecked(false);
        submitBtn.setEnabled(false);
        skipBtn.setEnabled(false);

        img01.setImageResource(R.drawable.new_rating_smile_grey);
        img02.setImageResource(R.drawable.new_rating_nutral_grey);
        img03.setImageResource(R.drawable.new_rating_angry_grey);
        imgb01 = false;
        imgb02 = false;
        imgb03 = false;
        rateValue = 0;

        controller.getNextQuestion();
    }

    // called by controller to set the next question
    public void setNextQuestion(Question question) {
        curQuestion = question;
        p.setVisibility(View.GONE);
        questionText.setText(question.getText());
        questionText.scrollTo(0, 0);
        readableCheck.setEnabled(true);
        readableCheck.setChecked(true);
        skipBtn.setEnabled(true);
    }

    //rate value function
    public void rateValueSet(int a){
        if(!submitBtn.isEnabled()){
            submitBtn.setEnabled(true);
        }
        switch (a){
            case 1:
                rateValue = 1;
                break;
            case 2:
                rateValue = 2;
                break;
            case 3:
                rateValue = 3;
                break;
        }
    }

    private void startTimer() {
        final int timeout = 4*60; //in seconds
        timerProgress.setMax(timeout);
        countDownTimer = new CountDownTimer(timeout*1000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                int time = (int)millisUntilFinished/1000;
                timerProgress.setProgress(time);
                timerText.setText("Time left: " + time + " seconds." );
            }

            @Override
            public void onFinish() {
                exit(true);
            }
        };
        countDownTimer.start();
    }

    public void setLevelBound(int bound) {
        this.levelBound = bound;
        levelProgressBar.setMax(levelBound);
    }
}

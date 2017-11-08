package com.toxicmania.toxicmania.activity;

import android.app.AlertDialog;
import android.app.Dialog;
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
    private Button submitBtn, skipBtn;
    private ProgressBar p, levelProgressBar;
    private TextView questionText, levelText;
    private int level, rateValue = 0; //value for image rating bar
    private int levelProgress = 0;
    private int[] levelBounds = {15, 30, 60, 100, 200, 500, 1000, 1500, 2000};
    private int curRep = 0;
    private Question curQuestion;
    private Animation smileAnimation;
    private String[] toxicities = {"notatall", "somewhat", "very"};
    SharedPreferences sharedPreferences,badgeLevels;
    private ImageView img01, img02, img03;
    boolean imgb01 = false, imgb02= false, imgb03= false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //Log.i(TAG, "Starting Play activity...");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play);

        //getting user from shared preferences and setting player info
        sharedPreferences = getSharedPreferences("PREFS", 0);

        // TODO: 10/24/2017 change below for pasan 
//        String temp = "107199454632343439631,Pasan Ranathunga,pmartz92@gmail.com,145,4,6,https://lh5.googleusercontent.com/--KHJJMfZeFw/AAAAAAAAAAI/AAAAAAAABZ8/iwnETRJBD9g/photo.jpg";
//        user = new User(temp);
        User user = new User(sharedPreferences.getString("ToxicUser", ""));
        // System.out.println(user.toString());

        controller = new Controller(this, user, this);
        level = user.getLevel();
        levelProgress = user.getLevelProgress();
        curRep = user.getReputation();

        // Init variables
        levelText = (TextView) findViewById(R.id.level_value);
        questionText = (TextView) findViewById(R.id.questionTextView);
        levelProgressBar = (ProgressBar) findViewById(R.id.level_progress);
        p = (ProgressBar) findViewById(R.id.progressBar);
        submitBtn = (Button) findViewById(R.id.submitBtn);
        skipBtn = (Button) findViewById(R.id.skipBtn);
        readableCheck = (CheckBox) findViewById(R.id.checkBox);
        smileAnimation = AnimationUtils.loadAnimation(this,R.anim.smile_slide);
        final Toast ratingToast = Toast.makeText(getBaseContext(), "",Toast.LENGTH_LONG);

        // set fonts
        Typeface robotoLight = Typeface.createFromAsset(getAssets(), "fonts/Roboto-Light.ttf");
        questionText.setTypeface(robotoLight);
        readableCheck.setTypeface(robotoLight);

        questionText.setMovementMethod(new ScrollingMovementMethod());

        //set level progress bar and level text
        levelProgressBar.setMax(levelBounds[level - 1]);
        levelProgressBar.setProgress(levelProgress);
        levelText.setText(""+ level);

        //new rating bar with images
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

        // Loading first question
        getNextQuestionHandler();
    }

    @Override
    protected void onStop() {
        // saving user object
        controller.user.setLevel(level);
        controller.user.setLevelProgress(levelProgress);
        SharedPreferences.Editor prefsEditor = sharedPreferences.edit();
        prefsEditor.putString("ToxicUser", controller.user.toString());
        prefsEditor.apply();

        controller.submitAnswersOnStop();
        super.onStop();
    }

    public void submitAnswer(View v) {
        if (readableCheck.isChecked()) {
            curQuestion.setToxicity(toxicities[rateValue - 1]);
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
            controller.user.setLevelProgress(0);
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
        else if(curRep >= 1200 && !badgeLevels.contains("Badge04")){
            badgeActivityDisplay("badge04");
            userBadge.putBoolean("Badge04", true);
        }
        else if(curRep >= 2000 && !badgeLevels.contains("Badge05")){
            badgeActivityDisplay("badge05");
            userBadge.putBoolean("Badge05", true);
        }
        userBadge.apply();

        //give reputation updates
        if (levelProgress % 10 == 0) {
            if (curRep != controller.user.getReputation()) {
                int diff = controller.user.getReputation() - curRep;
                String diffS = ""+diff;
                if (diff > 0)
                    diffS = "+" + diffS;

                curRep = controller.user.getReputation();
                final Snackbar snackbar = Snackbar.make(v, "Reputation updated: " + curRep + " points. (" + diffS + ")", Snackbar.LENGTH_LONG);
                snackbar.setAction("OK", new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                snackbar.dismiss();
                            }
                        });
                snackbar.show();
            }
            if(curRep < 100 && level == 3 ){
                new AlertDialog.Builder(this)
                        .setTitle("Not enough points?")
                        .setMessage("Have a look on \"How To Play\" for tips to go higher quickly. Cheers..!")
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                dialog.dismiss();
                            }});

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
        readableCheck.setChecked(false);
        readableCheck.setEnabled(false);
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

    //Badge Activity Display
    private void badgeActivityDisplay(String levelName){
        Intent i = new Intent(PlayActivity.this, BadgeActivity.class);
        i.putExtra("levelName", levelName);
        startActivity(i);
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

}

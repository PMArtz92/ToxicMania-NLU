package com.toxicmania.toxicmania.activity;

import android.animation.ObjectAnimator;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.toxicmania.toxicmania.R;

/**
 * Created by PM Artz on 10/24/2017.
 */

public class BadgeActivity extends AppCompatActivity {
    private static final String TAG = "BadgeActivity";
    private String beginner = "beginner";
    private ImageView badge;
    private TextView badgeName, badgeDescription;
    private Animation badgeAnimation;
    private Button continueBtn;
    private ObjectAnimator animation1;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_badge);

        Bundle extraData = getIntent().getExtras();
        String levelName = extraData.getString("levelName");

        badge = (ImageView)findViewById(R.id.badgeLogo);
        badge.setVisibility(View.INVISIBLE);
        badgeName = (TextView)findViewById(R.id.badgeName);
        badgeDescription = (TextView)findViewById(R.id.badgeDescription);
        continueBtn = (Button)this.findViewById(R.id.badgeContinueButton);
        continueBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        badgeAnimation = AnimationUtils.loadAnimation(this,R.anim.badge_anim);

        animation1 = ObjectAnimator.ofFloat(badge, "rotationY", 0.0f, 180f);
        animation1.setDuration(1500);
        animation1.setInterpolator(new AccelerateDecelerateInterpolator());



        if (levelName.equals(beginner)){
            badge.setImageResource(R.drawable.beginner_badge);
            badge.setVisibility(View.VISIBLE);
            badge.startAnimation(badgeAnimation);
            badgeAnimation.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    animation1.start();
                }

                @Override
                public void onAnimationRepeat(Animation animation) {
                }
            });
            badgeName.setText(R.string.beginner_badge);
            badgeName.setTextColor(Color.parseColor("#0eabe2"));
            badgeDescription.setText(R.string.beginner_badgeDes);
        }
        else if (levelName.equals("badge02")){
            badge.setImageResource(R.drawable.badge_2);
            badge.setVisibility(View.VISIBLE);
            badge.startAnimation(badgeAnimation);
            badgeAnimation.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    animation1.start();
                }

                @Override
                public void onAnimationRepeat(Animation animation) {
                }
            });
            badgeName.setText(R.string.badge02_name);
            badgeName.setTextColor(Color.parseColor("#8432f9"));
            badgeDescription.setText(R.string.begbadge02_des);
        }
        else if (levelName.equals("badge03")){
            badge.setImageResource(R.drawable.badge_3);
            badge.setVisibility(View.VISIBLE);
            badge.startAnimation(badgeAnimation);
            badgeAnimation.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    animation1.start();
                }

                @Override
                public void onAnimationRepeat(Animation animation) {
                }
            });
            badgeName.setText(R.string.badge03_name);
            badgeName.setTextColor(Color.parseColor("#dd1077"));
            badgeDescription.setText(R.string.begbadge03_des);
        }
        else if (levelName.equals("badge04")){
            badge.setImageResource(R.drawable.badge_4);
            badge.setVisibility(View.VISIBLE);
            badge.startAnimation(badgeAnimation);
            badgeAnimation.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    animation1.start();
                }

                @Override
                public void onAnimationRepeat(Animation animation) {
                }
            });
            badgeName.setText(R.string.badge04_name);
            badgeName.setTextColor(Color.parseColor("#f9b903"));
            badgeDescription.setText(R.string.begbadge04_des);
        }
        else if (levelName.equals("badge05")){
            badge.setImageResource(R.drawable.badge_5);
            badge.setVisibility(View.VISIBLE);
            badge.startAnimation(badgeAnimation);
            badgeAnimation.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    animation1.start();
                }

                @Override
                public void onAnimationRepeat(Animation animation) {
                }
            });
            badgeName.setText(R.string.badge05_name);
            badgeName.setTextColor(Color.parseColor("#43c108"));
            badgeDescription.setText(R.string.begbadge05_des);
        }
    }
}

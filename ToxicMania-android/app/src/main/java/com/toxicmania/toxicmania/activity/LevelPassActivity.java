package com.toxicmania.toxicmania.activity;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.toxicmania.toxicmania.R;

public class LevelPassActivity extends AppCompatActivity {
    ImageView tm_level_img;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_level_pass);

        tm_level_img = (ImageView)findViewById(R.id.tm_level_image);

        Animation animation;
        animation = AnimationUtils.loadAnimation(getApplicationContext(),
                R.anim.zoom);

        tm_level_img.startAnimation(animation);

        Bundle extraData = getIntent().getExtras();
        if (extraData != null) {
            int newLevel = extraData.getInt("NewLevel");
            TextView textView = (TextView) findViewById(R.id.level_pass_info_text);
            textView.setText("Level " + (newLevel-1) + " completed");
        }

        Button backButton = (Button)this.findViewById(R.id.nextbutton);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }
}

package com.toxicmania.toxicmania.activity;


import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.toxicmania.toxicmania.R;
import com.toxicmania.toxicmania.User;

/**
 * A simple {@link Fragment} subclass.
 */
public class AchievementFragment extends Fragment {


    public AchievementFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_achievement, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("PREFS", 0);
        User user = new User(sharedPreferences.getString("ToxicUser", ""));

        int uLevel = user.getLevel();
        int uRep = user.getReputation();

        SharedPreferences badgeLevels;
        badgeLevels = getActivity().getSharedPreferences("PREFS", 0);

        if(badgeLevels.contains("beginnerBadge")){
            ImageView ach_badge01 = (ImageView)getActivity().findViewById(R.id.ach_badge01);
            ach_badge01.setImageResource(R.drawable.beginner_badge_ach);
        }
        if(badgeLevels.contains("Badge02")){
            ImageView ach_badge01 = (ImageView)getActivity().findViewById(R.id.ach_badge02);
            ach_badge01.setImageResource(R.drawable.badge_ach_2);
        }
        if(badgeLevels.contains("Badge03")){
            ImageView ach_badge01 = (ImageView)getActivity().findViewById(R.id.ach_badge03);
            ach_badge01.setImageResource(R.drawable.badge_ach_3);
        }
        if(badgeLevels.contains("Badge04")){
            ImageView ach_badge01 = (ImageView)getActivity().findViewById(R.id.ach_badge04);
            ach_badge01.setImageResource(R.drawable.badge_ach_4);
        }
        if(badgeLevels.contains("Badge05")){
            ImageView ach_badge01 = (ImageView)getActivity().findViewById(R.id.ach_badge05);
            ach_badge01.setImageResource(R.drawable.badge_ach_5);
        }
    }


}

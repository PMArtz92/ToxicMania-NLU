package com.toxicmania.toxicmania.activity;


import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.toxicmania.toxicmania.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class HowToPlayFragment extends Fragment {

    private TextView textView,textView2,textView3,textView4;


    public HowToPlayFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_how_to_play, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Typeface robotoRegular = Typeface.createFromAsset(getActivity().getAssets(), "fonts/Roboto-Regular.ttf");
        Typeface robotoBlack = Typeface.createFromAsset(getActivity().getAssets(), "fonts/Roboto-Black.ttf");

        textView = (TextView)getActivity().findViewById(R.id.textView);
        textView2 = (TextView)getActivity().findViewById(R.id.textView2);
        textView3 = (TextView)getActivity().findViewById(R.id.textView3);
        textView4 = (TextView)getActivity().findViewById(R.id.textView4);


        textView3.setTypeface(robotoRegular);
        textView4.setTypeface(robotoRegular);
        textView.setTypeface(robotoBlack);
        textView2.setTypeface(robotoBlack);

    }
}

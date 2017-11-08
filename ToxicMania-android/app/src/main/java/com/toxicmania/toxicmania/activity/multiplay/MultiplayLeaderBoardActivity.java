package com.toxicmania.toxicmania.activity.multiplay;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.toxicmania.toxicmania.R;
import com.toxicmania.toxicmania.User;
import com.toxicmania.toxicmania.activity.CircleTransform;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class MultiplayLeaderBoardActivity extends AppCompatActivity {

    private User[] playerArray; //levelProgress used as the question count
    private String TAG = "MultiplayActivity";
    private User user;
    private ListView listView;
    private ProgressBar progressBar;
    SharedPreferences sharedPreferences;
    RelativeLayout relativeLayout;
    TextView header, description;
    Button continueBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_leader_board);

        //getting user from shared preferences and setting player info
        sharedPreferences = getSharedPreferences("PREFS", 0);
        user = new User(sharedPreferences.getString("ToxicUser", ""));

        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        listView = (ListView) findViewById(R.id.leader_board_list);

        relativeLayout = (RelativeLayout) findViewById(R.id.relativeLayout);
        relativeLayout.setVisibility(View.GONE);
        header = (TextView) findViewById(R.id.leader_board_header);
        header.setText("Toxic Session Leaderboard");
        description = (TextView) findViewById(R.id.leaderboard_description);
        description.setText("Marks are based on the correctness and time spent.");
        continueBtn = (Button) findViewById(R.id.leaderboard_continue_btn);
        continueBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                exit();
            }
        });

        //get session id from bundle
        Bundle extraData = getIntent().getExtras();
        String playersStr = extraData.getString("players");
        Log.i(TAG, "--------------------------"+playersStr);

        try {
            JSONArray players = new JSONArray(playersStr);
            playerArray = new User[players.length()];
            for (int i = 0 ; i < players.length(); i++) {
                JSONObject player = players.getJSONObject(i);
                User u = new User(player.getString("U_Id"), player.getString("U_Name"), "", player.getString("U_Img"));
                u.setReputation(player.getInt("Mark"));
                playerArray[i] = u;
            }
            dataLoaded();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onBackPressed() {
        exit();
    }

    private void exit() {
        SharedPreferences sharedPreferences = getSharedPreferences("PREFS", 0);
        SharedPreferences.Editor prefsEditor = sharedPreferences.edit();
        prefsEditor.putBoolean("isGameFinished", true);
        prefsEditor.apply();
        finish();
    }

    private void dataLoaded() {
        progressBar.setVisibility(View.INVISIBLE);
        // loading leaderboard list view
        LeaderBoardAdapter leaderBoardAdapter = new LeaderBoardAdapter(this, playerArray);
        listView.setAdapter(leaderBoardAdapter);
        getListViewSize(listView);
    }

    private class LeaderBoardAdapter extends ArrayAdapter<User> {

        private final Activity context;
        private final User[] items;

        public LeaderBoardAdapter(Activity context, User[] items) {
            super(context, R.layout.leader_board_entry_layout, items);

            this.context=context;
            this.items=items;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View view = inflater.inflate(R.layout.leader_board_entry_layout, parent, false);

            ImageView imageView = (ImageView)view.findViewById(R.id.imageView);
            TextView rank = (TextView)view.findViewById(R.id.rank);
            TextView userName = (TextView)view.findViewById(R.id.userNameText);
            TextView userRep = (TextView)view.findViewById(R.id.userRepText);
            TextView userQCount = (TextView)view.findViewById(R.id.userQCountText);

            User tmp = items[position];
            Picasso.with(getContext()).load(tmp.getUrl()).resize(130, 130).centerCrop().transform(new CircleTransform()).into(imageView);
            rank.setText("" + (position+1));

            String[] nameParts = tmp.getName().split(" ");
            if (nameParts.length > 2) {
                userName.setText(nameParts[0] + " " + nameParts[1]);
            } else {
                userName.setText(tmp.getName());
            }

            userRep.setText("" + tmp.getReputation());
            userQCount.setText("" + 20);

            return view;
        }
    }

    private void getListViewSize(ListView myListView) {
        ListAdapter myListAdapter = myListView.getAdapter();
        if (myListAdapter == null) {
            //do nothing return null
            return;
        }
        //set listAdapter in loop for getting final size
        int totalHeight = 0;
        for (int size = 0; size < myListAdapter.getCount(); size++) {
            View listItem = myListAdapter.getView(size, null, myListView);
            listItem.measure(0, 0);
            totalHeight += listItem.getMeasuredHeight();
        }
        //setting listview item in adapter
        ViewGroup.LayoutParams params = myListView.getLayoutParams();
        params.height = totalHeight + (myListView.getDividerHeight() * (myListAdapter.getCount() - 1));
        myListView.setLayoutParams(params);
    }
}

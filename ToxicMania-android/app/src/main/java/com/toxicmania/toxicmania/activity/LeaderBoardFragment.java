package com.toxicmania.toxicmania.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.squareup.picasso.Picasso;
import com.toxicmania.toxicmania.R;
import com.toxicmania.toxicmania.User;
import com.toxicmania.toxicmania.VolleyCallback;
import com.toxicmania.toxicmania.VolleyService;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class LeaderBoardFragment extends Fragment {

    private User[] topUsers; //levelProgress used as the question count
    private String TAG = "LeaderBoardFragment";
    private NavDrawerActivity parent;
    private User user;
    private VolleyService volleyService;
    private ListView listView;
    private ProgressBar progressBar;
    SharedPreferences sharedPreferences;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        parent = (NavDrawerActivity) getActivity();

        //getting user from shared preferences and setting player info
        sharedPreferences = parent.getSharedPreferences("PREFS", 0);
        user = new User(sharedPreferences.getString("ToxicUser", ""));

        volleyService = new VolleyService(parent);

        // get data
        if (parent.isNetworkAvailable()) {
            String url = "https://app.ucsccareerfair.com/answer/leaderBoard?u_id=" + user.getID();
            //Log.i(TAG, "Leader board loading... " + url);
            volleyService.volleyGet("GETCALL", url, new VolleyCallback() {
                @Override
                public void notifySuccess(String requestType, JSONObject response) {
                    try {
                        boolean success = response.getBoolean("success");
                        if (success) {
                            JSONArray leaderBoard = response.getJSONArray("leader_board");
                            topUsers = new User[leaderBoard.length()];
                            for (int i = 0; i < leaderBoard.length(); i++) {
                                JSONObject u = leaderBoard.getJSONObject(i);
                                topUsers[i] = new User(u.getString("U_Id"), u.getString("User_Name"),
                                        u.getString("Email"), u.getString("U_Img"));
                                topUsers[i].setLevelProgress(u.getInt("question_count"));
                                topUsers[i].setReputation(u.getInt("Mark"));
                                topUsers[i].setLevel(u.getInt("rank"));
                            }

                            JSONObject curUser = response.getJSONObject("user");
                            user.setLevelProgress(curUser.getInt("question_count"));
                            user.setLevel(curUser.getInt("rank"));
                            user.setReputation(curUser.getInt("Mark"));
                        }
                        //Log.i(TAG, "Leader board loading complete.");
                        dataLoaded();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                @Override // Error handler
                public void notifyError(String requestType, VolleyError error) {
                    //Log.d(TAG, "Volley JSON get error!\n" + error);
                }
            });
        } else {
            final AlertDialog alertDialog = new AlertDialog.Builder(getActivity()).create();
            alertDialog.setTitle("No Network!");
            alertDialog.setMessage("We couldn't find a working network connection.");
            alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    alertDialog.dismiss();
                }
            });
            alertDialog.show();
        }

        return inflater.inflate(R.layout.fragment_leader_board, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        progressBar = (ProgressBar) parent.findViewById(R.id.progressBar);
        listView = (ListView) parent.findViewById(R.id.leader_board_list);

    }

    private void dataLoaded() {
        progressBar.setVisibility(View.INVISIBLE);
        // loading leaderboard list view
        LeaderBoardAdapter leaderBoardAdapter = new LeaderBoardAdapter(getActivity(), topUsers);
        listView.setAdapter(leaderBoardAdapter);
        getListViewSize(listView);

        // load user info
        TextView userRank = (TextView) parent.findViewById(R.id.userRank);
        TextView userRep = (TextView) parent.findViewById(R.id.userRepText);
        TextView userQCount = (TextView) parent.findViewById(R.id.userQCountText);
        userRank.setText("" + user.getLevel());
        userRep.setText("" + user.getReputation());
        userQCount.setText("" + user.getLevelProgress());
    }

    private class LeaderBoardAdapter extends ArrayAdapter<User> {

        private final Activity context;
        private final User[] items;

        public LeaderBoardAdapter(Activity context, User[] items) {
            super(context, R.layout.leader_board_entry_layout, items);
            // TODO Auto-generated constructor stub

            this.context=context;
            this.items=items;
            System.out.println(">>> " + items.length);
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
            Picasso.with(getActivity()).load(tmp.getUrl()).resize(130, 130).centerCrop().transform(new CircleTransform()).into(imageView);
            if (tmp.getLevel() < 4) {
                rank.setText("" + tmp.getLevel());
            } else {
                rank.setVisibility(View.GONE);
//                imageView.set
            }
            userName.setText(tmp.getName());
            userRep.setText("" + tmp.getReputation());
            userQCount.setText("" + tmp.getLevelProgress());

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

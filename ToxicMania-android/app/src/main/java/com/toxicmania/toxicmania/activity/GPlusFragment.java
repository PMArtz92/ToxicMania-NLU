package com.toxicmania.toxicmania.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.VolleyError;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.OptionalPendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.toxicmania.toxicmania.ImageHelper;
import com.toxicmania.toxicmania.R;
import com.toxicmania.toxicmania.User;
import com.toxicmania.toxicmania.VolleyCallback;
import com.toxicmania.toxicmania.VolleyService;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;


public class GPlusFragment extends Fragment implements GoogleApiClient.OnConnectionFailedListener {

    private static final String TAG = "GPlusFragment";
    private int RC_SIGN_IN = 0;
    private GoogleApiClient mGoogleApiClient;
    private SignInButton signInButton;
    private LinearLayout signOutView;
    private TextView mStatusTextView;
    private ProgressDialog mProgressDialog;
    private ImageView imgProfilePic;
    private SharedPreferences sharedPreferences;
    private ProgressBar progressBar;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        //Log.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        // Configure sign-in to request the user's ID, email address, and basic
        // profile. ID and basic profile are included in DEFAULT_SIGN_IN.
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();

        // Build a GoogleApiClient with access to the Google Sign-In API and the
// options specified by gso.
        mGoogleApiClient = new GoogleApiClient.Builder(getActivity())
                .enableAutoManage(getActivity() /* FragmentActivity */, this /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API,gso)
                .build();
    }

    @Override
    public void onStart() {
        super.onStart();

        OptionalPendingResult<GoogleSignInResult> opr = Auth.GoogleSignInApi.silentSignIn(mGoogleApiClient);
        if (opr.isDone()) {
            // If the user's cached credentials are valid, the OptionalPendingResult will be "done"
            // and the GoogleSignInResult will be available instantly.
            //Log.d(TAG, "Got cached sign-in");
            GoogleSignInResult result = opr.get();
            handleSignInResult(result);
        } else {
            // If the user has not previously signed in on this device or the sign-in has expired,
            // this asynchronous branch will attempt to sign in the user silently.  Cross-device
            // single sign-on will occur in this branch.
            // showProgressDialog();
            opr.setResultCallback(new ResultCallback<GoogleSignInResult>() {
                @Override
                public void onResult(GoogleSignInResult googleSignInResult) {
                    hideProgressDialog();
                    handleSignInResult(googleSignInResult);
                }
            });
        }
    }

    @Override
    public void onStop() {
        hideProgressDialog();
        super.onStop();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_gplus, parent, false);

        signInButton = (SignInButton) v.findViewById(R.id.sign_in_button);

        mStatusTextView = (TextView) v.findViewById(R.id.status);
        //imgProfilePic.setImageBitmap(ImageHelper.getRoundedCornerBitmap(getContext(),icon, 200, 200, 200, false, false, false, false));
        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
                startActivityForResult(signInIntent, RC_SIGN_IN);
            }
        });

        progressBar = (ProgressBar) v.findViewById(R.id.progressBar);
        return v;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            handleSignInResult(result);
        }
    }

    private void handleSignInResult(GoogleSignInResult result) {
        //Log.d(TAG, "handleSignInResult:" + result.isSuccess());
        sharedPreferences = getActivity().getSharedPreferences("PREFS", 0);
        SharedPreferences.Editor prefsEditor = sharedPreferences.edit();
        if (result.isSuccess()) {
            // Signed in successfully, show authenticated UI.
            GoogleSignInAccount acct = result.getSignInAccount();
            mStatusTextView.setText(getString(R.string.signed_in_fmt, acct.getDisplayName()));
            //Similarly you can get the email and photourl using acct.getEmail() and  acct.getPhotoUrl()

            updateUI(true);

            User user = new User(acct.getId(), acct.getDisplayName(), acct.getEmail(), acct.getPhotoUrl().toString());

            //send user to server and get updated data.
            progressBar.setVisibility(View.VISIBLE);
            sendUserToServer(user);

        } else {
            // Signed out, show unauthenticated UI.
            updateUI(false);

            prefsEditor.putString("ToxicUser", "");
            prefsEditor.putBoolean("is_logged", false);
            prefsEditor.apply();
        }
    }

    private User sendUserToServer(final User user) {
        // building send object
        JSONObject finalObject = new JSONObject();
        JSONObject obj = new JSONObject();
        try {
            obj.put("U_Id", user.getID());
            obj.put("U_Name", user.getName());
            obj.put("U_Img", user.getUrl());
            obj.put("U_Email", user.getEmail());

            finalObject.put("U_Obj", obj);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        // TODO: 10/24/2017 add url
        String url = "https://app.ucsccareerfair.com/user/newUser";
        VolleyService volleyService = new VolleyService(getActivity());
        volleyService.volleyPost("POST", url, finalObject, new VolleyCallback() {
            @Override
            public void notifySuccess(String requestType, JSONObject response) {
                //Log.d(TAG, "User registration success!\n" + response);
                try {
                    JSONObject userObj = response.getJSONObject("result");
                    if (userObj != null) {
                        user.setReputation(userObj.getInt("Mark"));
                        user.setLevel(userObj.getInt("Level"));
                        user.setLevelProgress(userObj.getInt("Weight"));
                    }
                    //Log.i(TAG, "User object updated. " + user.toString());
                    SharedPreferences.Editor prefsEditor = sharedPreferences.edit();
                    prefsEditor.putString("ToxicUser", user.toString());

                    // update badge status
                    if(user.getLevel() > 2 || (user.getLevel() == 2 && user.getLevelProgress() > 5))
                        prefsEditor.putBoolean("beginnerBadge", true);
                    if(user.getReputation() >= 300)
                        prefsEditor.putBoolean("Badge02", true);
                    if(user.getReputation() >= 800)
                        prefsEditor.putBoolean("Badge03", true);
                    if(user.getReputation() >= 1500)
                        prefsEditor.putBoolean("Badge04", true);
                    if(user.getReputation() >= 2500)
                        prefsEditor.putBoolean("Badge05", true);

                    progressBar.setVisibility(View.INVISIBLE);
                    Toast.makeText(getActivity(), "Welcome " + user.getName(), Toast.LENGTH_LONG).show();
                    //Log.d(TAG, "Saving user object: " + user.toString());
                    // save log status
                    prefsEditor.putBoolean("is_logged", true);
                    prefsEditor.apply();

                    getActivity().overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                    getActivity().finish();

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void notifyError(String requestType, VolleyError error) {
                //Log.e(TAG, "User registration error!\n" + error);
            }
        });

        return user;
    }


    private void updateUI(boolean signedIn) {
        if (signedIn) {
            signInButton.setVisibility(View.GONE);
        } else {
            mStatusTextView.setText(R.string.signed_out);
            signInButton.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        // An unresolvable error has occurred and Google APIs (including Sign-In) will not
        // be available.
        //Log.d(TAG, "onConnectionFailed:" + connectionResult);
    }

    private void showProgressDialog() {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(getActivity());
            mProgressDialog.setMessage(getString(R.string.loading));
            mProgressDialog.setIndeterminate(true);
        }

        mProgressDialog.show();
    }

    private void hideProgressDialog() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.hide();
        }
    }

    /**
     * Background Async task to load user profile picture from url
     * */
    private class LoadProfileImage extends AsyncTask<String, Void, Bitmap> {
        ImageView bmImage;

        LoadProfileImage(ImageView bmImage) {
            this.bmImage = bmImage;
        }

        protected Bitmap doInBackground(String... uri) {
            String url = uri[0];
            Bitmap mIcon11 = null;
            try {
                InputStream in = new java.net.URL(url).openStream();
                mIcon11 = BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                //Log.e("Error", e.getMessage());
                e.printStackTrace();
            }
            return mIcon11;
        }

        protected void onPostExecute(Bitmap result) {

            if (result != null) {


                Bitmap resized = Bitmap.createScaledBitmap(result,200,200, true);
                bmImage.setImageBitmap(ImageHelper.getRoundedCornerBitmap(getContext(),resized,250,200,200, false, false, false, false));

            }
        }
    }


}

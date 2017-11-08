package com.toxicmania.toxicmania.fcm;

import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;


/**
 * @author ton1n8o - antoniocarlos.dev@gmail.com on 6/13/16.
 */
public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = "######################";
    private LocalBroadcastManager broadcaster;

    @Override
    public void onCreate() {
        broadcaster = LocalBroadcastManager.getInstance(this);
    }

    @Override
    public void onMessageReceived(RemoteMessage message) {

        try {
//            String text = message.getNotification().getBody();
            String text = message.getData().toString();
            Intent intent = new Intent("MyData");
            intent.putExtra("text", text);
            broadcaster.sendBroadcast(intent);

            Intent intent2 = new Intent();
            intent2.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
            intent2.setAction("com.toxicmania.BroadcastReceiver");
            intent2.putExtra("text", text);
            sendBroadcast(intent2);

            Log.i(TAG, text);
        } catch (Exception ignored) {

        }
        Intent intent = new Intent();
        intent.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
        intent.setAction("com.toxicmania.BroadcastReceiver");
        intent.putExtra("Foo", "Bar");
        sendBroadcast(intent);
    }

}
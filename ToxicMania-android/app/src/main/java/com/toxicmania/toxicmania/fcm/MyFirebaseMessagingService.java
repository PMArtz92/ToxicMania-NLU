package com.toxicmania.toxicmania.fcm;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.toxicmania.toxicmania.activity.MainActivity;
import com.toxicmania.toxicmania.activity.multiplay.MultiplayActivity;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;


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

        String text = message.getNotification().getBody();

        Intent intent = new Intent("MyData");
        intent.putExtra("text", text);
        broadcaster.sendBroadcast(intent);

        //Log.i(TAG, text);

    }

    /**
     * Create and show a simple notification containing the received GCM message.
     *
     * @param notificationData GCM message received.
     */
//    private void sendNotification(NotificationData notificationData) {
//
//        Intent intent = new Intent(this, MainActivity.class);
//        intent.putExtra(NotificationData.TEXT, notificationData.getTextMessage());
//
//        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
//                PendingIntent.FLAG_ONE_SHOT);
//
//        NotificationCompat.Builder notificationBuilder = null;
//        try {
//
//            notificationBuilder = new NotificationCompat.Builder(this)
//                    .setSmallIcon(R.mipmap.ic_launcher)
//                    .setContentTitle(URLDecoder.decode(notificationData.getTitle(), "UTF-8"))
//                    .setContentText(URLDecoder.decode(notificationData.getTextMessage(), "UTF-8"))
//                    .setAutoCancel(true)
//                    .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
//                    .setContentIntent(pendingIntent);
//
//        } catch (UnsupportedEncodingException e) {
//            e.printStackTrace();
//        }
//
//        if (notificationBuilder != null) {
//            NotificationManager notificationManager =
//                    (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
//            notificationManager.notify(notificationData.getId(), notificationBuilder.build());
//        } else {
//            Log.d(TAG, "Não foi possível criar objeto notificationBuilder");
//        }
////    }
}
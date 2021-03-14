package com.tam.winati.ksa.services;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.Log;



import org.json.JSONException;
import org.json.JSONObject;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.tam.winati.ksa.ShowStores;
import com.tam.winati.ksa.SpecificStore;
import com.utils.Functions;
import com.utils.SharePrefsEntry;

import static com.tam.winati.ksa.BaseActivity.globalLog;

/**
 * Created by mac on 1/08/2017.
 */

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = MyFirebaseMessagingService.class.getSimpleName();

    private NotificationUtils notificationUtils;
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {

        globalLog("notification :: XX "+remoteMessage);
        if (remoteMessage == null)
            return;

        if (remoteMessage.getNotification() != null)
        {
            handleNotification(remoteMessage.getNotification().getBody());
        }

        if (remoteMessage.getData().toString().length() > 0)
        {
            try
            {
                JSONObject json = new JSONObject(remoteMessage.getData().toString());
                globalLog("notification :: "+json);
                handleDataMessage(json);
            } catch (Exception e) {
                globalLog("notification :: err "+e.getMessage());
                Log.e(TAG, "Exception: " + e.getMessage());
            }
        }
    }

    private void handleNotification(String message)
    {
        if (!NotificationUtils.isAppIsInBackground(getApplicationContext()))
        {
            // app is in foreground, broadcast the push message
            Intent pushNotification = new Intent(MyFirebaseInstanceIDService.PUSH_NOTIFICATION);
            pushNotification.putExtra("message", message);
            LocalBroadcastManager.getInstance(this).sendBroadcast(pushNotification);

            // play notification sound
            NotificationUtils notificationUtils = new NotificationUtils(getApplicationContext());
            notificationUtils.playNotificationSound();
        }
        else
        {

            // If the app is in background, firebase itself handles the notification
        }
    }

    private void handleDataMessage(JSONObject jobj) {
        Functions function              = new Functions(getApplicationContext());
        SharePrefsEntry sp              = new SharePrefsEntry(getApplicationContext());
        try
        {
            JSONObject json = new JSONObject(jobj.getString("data"));
            String title    = json.getString("title");
            String msg      = json.getString("message");
            String ts       = json.getString("timestamp");
            JSONObject jo   = new JSONObject(json.getString("payload"));
            globalLog("notification :: 22 "+jo);
            jo              = new JSONObject(jo.getString("storedata"));
            globalLog("notification :: 33 "+jo);
            String stData   = jo.toString();



            Intent intent   = new Intent(getApplicationContext(), ShowStores.class);
            intent.putExtra("data",stData);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

            NotificationUtils notificationUtils = new NotificationUtils(getApplicationContext());
            notificationUtils.playNotificationSound();
            notificationUtils.showNotificationMessage(title,msg,ts,intent);
        }catch (Exception e){


            try
            {
                JSONObject json = new JSONObject(jobj.getString("data"));
                String title    = json.getString("title");
                String msg      = json.getString("message");
                String ts       = json.getString("timestamp");



                Intent intent   = new Intent(getApplicationContext(), ShowStores.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

                NotificationUtils notificationUtils = new NotificationUtils(getApplicationContext());
                notificationUtils.playNotificationSound();
                notificationUtils.showNotificationMessage(title,msg,ts,intent);
            }catch (Exception ee){
                ee.printStackTrace();
            }
        }
    }

    /**
     * Showing notification with text only
     */
    private void showNotificationMessage(Context context, String title, String message, String timeStamp, Intent intent) {
        notificationUtils = new NotificationUtils(context);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        notificationUtils.showNotificationMessage(title, message, timeStamp, intent);
    }

    /**
     * Showing notification with text and image
     */
    private void showNotificationMessageWithBigImage(Context context, String title, String message, String timeStamp, Intent intent, String imageUrl) {
        notificationUtils = new NotificationUtils(context);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        notificationUtils.showNotificationMessage(title, message, timeStamp, intent, imageUrl,(int)System.currentTimeMillis());
    }
}
package com.example.android.notifyme;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    private static final String PRIMARY_CHANNEL_ID  = "primary_notification_channel";
    private static final String CHANNEL_NAME = "Mascot Notification";
    private static final String NOTIFICATION_CHANNEL_DESCRIPTION = "Notification from Mascot";
    private static final int NOTIFICATION_ID = 0;
    private static final String NOTIFICATION_UPDATED_MESSAGE = "Notification Updated";
    private static final String ACTION_UPDATE_NOTIFICATION =
            "com.example.android.notifyme.ACTION_UPDATE_NOTIFICATION";
    private static final String ACTION_DELETE_NOTIFICATION =
            "com.example.android.notifyme.ACTION_DELETE_NOTIFICATION";
    private static final int ACTION_DELETE_NOTIFICATION_ID = 2;
    private static final String NOTIFICATION_ACTION_UPDATE_TITLE = "Update Notification";

    private Button mButton_notify;
    private Button mButton_update;
    private Button mButton_cancel;
    private NotificationManager mNotifyManager;
    private NotificationReceiver mReceiver = new NotificationReceiver();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mButton_notify = (Button) findViewById(R.id.notify);
        mButton_update = (Button) findViewById(R.id.update);
        mButton_cancel = (Button) findViewById(R.id.cancel);

        mButton_notify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendNotification();
            }
        });

        mButton_update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateNotification();
            }
        });

        mButton_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cancelNotification();
            }
        });

        createNotificationChannel();

        //Enable and disable buttons to set the state of the app at launch for sending a notification
        setNotificationButtonState(true, false, false);

        //Register the Broadcast receiver with a specific filter
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ACTION_UPDATE_NOTIFICATION);
        intentFilter.addAction(ACTION_DELETE_NOTIFICATION);
        registerReceiver(mReceiver, intentFilter);
    }

    public void sendNotification() {

        //Set up intent with action identifier
        Intent updateIntent = new Intent(ACTION_UPDATE_NOTIFICATION);

        //Wrap the intent in a pending intent which lets the NotificationManager broadcast the intent
        //that is then caught by the intentfilter registered with this app
        PendingIntent updatePendingIntent = PendingIntent.getBroadcast(
                this,
                NOTIFICATION_ID,
                updateIntent,
                PendingIntent.FLAG_ONE_SHOT);

        //Add the action to the notification's action text. Pressing the text will send out a broadcast
        //When the broadcast is received by the app, the app will call updateNotification().
        NotificationCompat.Builder notifyBuilder = getNotificationBuilder();

        notifyBuilder.addAction(
                R.drawable.ic_update,
                NOTIFICATION_ACTION_UPDATE_TITLE,
                updatePendingIntent);

        mNotifyManager.notify(NOTIFICATION_ID, notifyBuilder.build());

        setNotificationButtonState(false, true, true);
    }

    public void updateNotification() {
        //Create a bitmap image to display in the notification
        Bitmap androidImage = BitmapFactory
                .decodeResource(getResources(), R.drawable.mascot_1);
        //Get an instance of the builder using the helper method
        NotificationCompat.Builder notifyBuilder = getNotificationBuilder();
        //Set the style for the notification update
        notifyBuilder.setStyle(new NotificationCompat.BigPictureStyle()
        .bigPicture(androidImage)
        .setBigContentTitle(NOTIFICATION_UPDATED_MESSAGE));
        //Build the notification
        mNotifyManager.notify(NOTIFICATION_ID, notifyBuilder.build());

        setNotificationButtonState(false, false, true);
    }

    public void cancelNotification() {
        mNotifyManager.cancel(NOTIFICATION_ID);
        setNotificationButtonState(true, false, false);
    }

    //HELPER METHODS

    //Channel helper
    public void createNotificationChannel() {
        mNotifyManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel(
                    PRIMARY_CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH
                    );
            notificationChannel.enableLights(true);
            notificationChannel.setLightColor(Color.RED);
            notificationChannel.enableVibration(true);
            notificationChannel.setDescription(NOTIFICATION_CHANNEL_DESCRIPTION);
            mNotifyManager.createNotificationChannel(notificationChannel);
        }
    }

    //Notification builder helper
    private NotificationCompat.Builder getNotificationBuilder() {

        Intent notificationIntent = new Intent (this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this,
                NOTIFICATION_ID,
                notificationIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        Intent deleteNotificationIntent = new Intent(ACTION_DELETE_NOTIFICATION);
        PendingIntent deleteNotifyPendingIntent = PendingIntent.getBroadcast(
                this,
                ACTION_DELETE_NOTIFICATION_ID,
                deleteNotificationIntent,
                PendingIntent.FLAG_CANCEL_CURRENT);

        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(this, PRIMARY_CHANNEL_ID)
                .setContentTitle("You've been notified")
                .setContentText("This is your notification text.")
                .setSmallIcon(R.drawable.notification_small_icon)
                .setContentIntent(pendingIntent)
                .setDeleteIntent(deleteNotifyPendingIntent) //calls Receiver with delete action
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setDefaults(NotificationCompat.DEFAULT_ALL);
        return builder;
    }

    //Toggle button state based on notification status
    private void setNotificationButtonState (Boolean isNotifyEnabled,
                                             Boolean isUpdateEnabled,
                                             Boolean isCanceledEnabled) {
        mButton_notify.setEnabled(isNotifyEnabled);
        mButton_update.setEnabled(isUpdateEnabled);
        mButton_cancel.setEnabled(isCanceledEnabled);
    }

    public class NotificationReceiver extends BroadcastReceiver {

        public NotificationReceiver() {
        }

        @Override
        public void onReceive(Context context, Intent intent) {

            //Toggle buttons appropriately based on the action of the intent
            if (intent.getAction().equals(ACTION_DELETE_NOTIFICATION)) {
                cancelNotification();
            } else if (intent.getAction().equals(ACTION_UPDATE_NOTIFICATION)) {
                updateNotification();
            }
        }
    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(mReceiver);
        super.onDestroy();
    }
}

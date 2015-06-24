package at.myfirstgcmapp.snowreporter.myfirstgcmapp;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.android.gms.gcm.GoogleCloudMessaging;

import static com.google.android.gms.internal.zzhl.runOnUiThread;

/**
 * Created by snowreporter on 06.05.2015.
 */
public class GcmIntentService extends IntentService {
    public static final int NOTIFICATION_ID = 1;
    private NotificationManager mNotificationManager;

    static final String TAG = "GCMDemo";

    public GcmIntentService() {
        super("GcmIntentService");
    }

    private static String message = "";

    public String getMessage() {
        return message;
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Bundle extras = intent.getExtras();
        GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);

        String messageType = gcm.getMessageType(intent);

        if (extras.toString() != null) {
            if (!extras.isEmpty()) {
                message = extras.getString("m");

                if (GoogleCloudMessaging.MESSAGE_TYPE_SEND_ERROR.equals(messageType)) {
                    sendNotification("Send error: " + extras.toString());
                } else if (GoogleCloudMessaging.MESSAGE_TYPE_DELETED.equals(messageType)) {
                    sendNotification("Deleted messages on server: " + extras.toString());
                } else if (GoogleCloudMessaging.MESSAGE_TYPE_MESSAGE.equals(messageType)) {
                    for (int i = 0; i < 5; i++) {
                        Log.i(TAG, "Working... " + (i + 1) + "/5 @ " + SystemClock.elapsedRealtime());
                        try {
                            Thread.sleep(500);
                        } catch (InterruptedException e) {
                        }
                    }
                    sendNotification("Received: " + extras.toString());
                    Log.i(TAG, "Message: " + message);

                }
            }
        }

        GcmBroadcastReceiver.completeWakefulIntent(intent);
    }

    private void sendNotification(String msg) {
        Context context = getApplicationContext();

        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, intent, 0);

        Intent settings = new Intent(this, MainInfo.class);
        PendingIntent settinsIntent = PendingIntent.getActivity(this, 0, settings, 0);

        Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        NotificationCompat.BigTextStyle textStyle = new NotificationCompat.BigTextStyle();
        String longTextMessage = msg;
        textStyle.bigText(longTextMessage);
        textStyle.setSummaryText("The summary test goes here...");

        NotificationCompat.InboxStyle inboxStyle =
                new NotificationCompat.InboxStyle();
        String[] events = new String[6];
// Sets a title for the Inbox in expanded layout
        inboxStyle.setBigContentTitle("Event tracker details:");

// Moves events into the expanded layout
        for (int i = 0; i < events.length; i++) {

            inboxStyle.addLine(events[i]);
        }

        final NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this);

        if (MainActivity.isAppInBackground() || !MainActivity.isScreenOn()) {
            mBuilder
                .setSmallIcon(R.drawable.ic_stat_gcm)
                .setSound(alarmSound)
                .setContentTitle("GCM Notification")
                .setContentText(msg)
                .setAutoCancel(true)
                .setContentIntent(contentIntent)
                .setStyle(textStyle)
                .setPriority(Notification.PRIORITY_HIGH)
                .setTicker("new GCM")
                .setLights(0xFFAAAA00, 200, 2000);

            if (Build.VERSION.SDK_INT >= 21) {
                mBuilder
                    .setCategory(Notification.CATEGORY_MESSAGE)
                    .setVisibility(Notification.VISIBILITY_PUBLIC);
            }

            Log.i(TAG, "isInBackground = true --> send notification");
        }
        else {
            Thread thread = new Thread() {
                @Override
                public void run() {
                    try {
                        synchronized (this) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    MainActivity.getNewMessage();
                                }
                            });
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            };
            thread.start();

            mBuilder
                    .setSound(alarmSound);

            Log.i(TAG, "isInBackground = false --> show toast");
        }

        Notification notification = mBuilder.build();
        mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(NOTIFICATION_ID, notification);
    }
}

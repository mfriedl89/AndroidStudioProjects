package at.snowreporter.buenoi.GMS;

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

import at.snowreporter.buenoi.MainActivity;
import at.snowreporter.buenoi.MessageJsonString;
import at.snowreporter.buenoi.MyApp;
import at.snowreporter.buenoi.R;
import at.snowreporter.buenoi.database.Message;

import static com.google.android.gms.internal.zzhl.runOnUiThread;

/**
 * Created by snowreporter on 01.07.2015.
 */
public class GcmIntentService extends IntentService {
    public static final int NOTIFICATION_ID = 1;
    private NotificationManager mNotificationManager;

    static final String TAG = "Buenoi";

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
                    Log.i(TAG, "onHandleIntent: extras = " + extras);

                }
            }
        }

        GcmBroadcastReceiver.completeWakefulIntent(intent);
    }

    private void sendNotification(String msg) {
        Log.i(TAG, "sendNotification: " + msg);

        if (msg.contains("type=gcm_id_verifiziert") ||
                msg.contains("Bundle[{CMD=RST_FULL, from=google.com/iid, android.support.content.wakelockid=1}]")) {
            Log.i(TAG, "Initialization for google cloud messaging, no message to show!");
        }
        else {
            String modTypeText = "";

            Intent intent = new Intent(this, MainActivity.class);
            PendingIntent contentIntent = PendingIntent.getActivity(this, 0, intent, 0);

            Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

            // Split messageString
            Message message = MessageJsonString.splitMessage(msg);

            Log.i(TAG, "sendNotification splitted message: date = " + message.date + ", time = " +
                    message.time + ", type = " + message.type + ", comment = " + message.comment);

            // Add message to the database
            MainActivity.addMessage(message);

            modTypeText = MainActivity.modifiedTypeText(message.type);

            NotificationCompat.BigTextStyle textStyle = new NotificationCompat.BigTextStyle();
            textStyle.bigText(modTypeText);
            textStyle.setSummaryText(message.comment);

            final NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this);

            if (MainActivity.isAppInBackground() || !MainActivity.isScreenOn()) {

                mBuilder
                        .setSound(alarmSound)
                        .setContentTitle("Buenoi")
                        .setContentText(modTypeText)
                        .setAutoCancel(true)
                        .setContentIntent(contentIntent)
                        .setStyle(textStyle)
                        .setPriority(Notification.PRIORITY_HIGH)
                        .setTicker("new Buenoi")
                        .setLights(0xFFAAAA00, 200, 2000)
                        .setSmallIcon(getNotificationIcon(), 1);

                if (Build.VERSION.SDK_INT >= 21) {
                    mBuilder
                            .setCategory(Notification.CATEGORY_MESSAGE)
                            .setVisibility(Notification.VISIBILITY_PUBLIC);
                }

                Log.i(TAG, "isInBackground = true --> send notification");
            } else {
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
            mNotificationManager = (NotificationManager) MyApp.getContext().getSystemService(Context.NOTIFICATION_SERVICE);
            mNotificationManager.notify(NOTIFICATION_ID, notification);
        }
    }

    private int getNotificationIcon() {
        boolean whiteIcon = (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP);
        return whiteIcon ? R.drawable.ic_action_event_note_white : R.drawable.ic_action_event_note;
    }
}

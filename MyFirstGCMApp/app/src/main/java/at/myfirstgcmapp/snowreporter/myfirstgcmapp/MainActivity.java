package at.myfirstgcmapp.snowreporter.myfirstgcmapp;

import android.app.ActivityManager;
import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.hardware.display.DisplayManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.PowerManager;
import android.speech.RecognizerIntent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.apache.http.Header;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class MainActivity extends ActionBarActivity {
    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;

    static final String TAG = "GCMDemo";

    static TextView mDisplay;
    GoogleCloudMessaging gcm;
    AtomicInteger msgId = new AtomicInteger();
    static Context context;

    String regid;
    public String message;

    ProgressDialog prgDialog;
    RequestParams params = new RequestParams();
    public static final String REG_ID = "regId";
    public static final String EMAIL_ID = "eMailId";
    EditText emailET;
    static Button refreshButton;
    AsyncTask<Void, Void, String> createRegIdTask;

    NotificationManager nm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mDisplay = (TextView) findViewById(R.id.display);
        refreshButton = (Button) findViewById(R.id.refresh);

        context = getApplicationContext();

        prgDialog = new ProgressDialog(this);
        // Set Progress Dialog Text
        prgDialog.setMessage("Please wait...");
        // Set Cancelable as False
        prgDialog.setCancelable(false);

        SharedPreferences prefs = getSharedPreferences("UserDetails",
                Context.MODE_PRIVATE);
        String registrationId = prefs.getString(REG_ID, "");

        emailET = (EditText) findViewById(R.id.email);

        nm =  (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        //When Email ID is set in Sharedpref, User will be taken to HomeActivity
        if (!TextUtils.isEmpty(registrationId)) {
            Intent i = new Intent(context, MainActivity.class);
            i.putExtra("regId", registrationId);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkPlayServices();

        nm.cancelAll();
        refreshJSONString();
        refreshButton.setEnabled(false);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        LinearLayout mainLayout = (LinearLayout) findViewById(R.id.content_linear_layout);

        //noinspection SimplifiableIfStatement
        switch (id) {
            case R.id.action_call:
                Intent dialer = new Intent(Intent.ACTION_DIAL);
                startActivity(dialer);
                return true;
            case R.id.action_speech:
                Intent speech = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                speech.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
                startActivityForResult(speech, 1234);
                return true;
            case R.id.action_settings:
                Intent settings = new Intent(this, MainInfo.class);
                startActivity(settings);
                return true;
            case R.id.action_registration:
                Toast.makeText(context, "Registration clicked", Toast.LENGTH_LONG).show();
                return true;
            case R.id.action_background_black:
                if(item.isChecked())
                    item.setChecked(false);
                else
                    item.setChecked(true);
                mainLayout.setBackgroundColor(Color.BLACK);
                emailET.setTextColor(Color.WHITE);
                emailET.setHintTextColor(Color.WHITE);
                return true;
            case R.id.action_background_white:
                if(item.isChecked())
                    item.setChecked(false);
                else
                    item.setChecked(true);
                mainLayout.setBackgroundColor(Color.WHITE);
                emailET.setTextColor(Color.BLACK);
                emailET.setHintTextColor(Color.BLACK);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1234 && resultCode == RESULT_OK) {
            String voice_text = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS).get(0);
            Toast.makeText(getApplicationContext(),voice_text,Toast.LENGTH_LONG).show();

        }
    }

    private boolean checkPlayServices() {
        int resultColde = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (resultColde != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultColde)) {
                GooglePlayServicesUtil.getErrorDialog(resultColde, this, PLAY_SERVICES_RESOLUTION_REQUEST).show();
            }
            else {
                Toast.makeText(
                        context,
                        "This device doesn't support Play services, App will not work normally",
                        Toast.LENGTH_LONG).show();
                Log.i(TAG, "This device is not supported.");
                finish();
            }
            return false;
        }
        return true;
    }

    public static boolean isAppInBackground() {
        boolean isInBackground = true;
        try {
            if (context != null) {
                ActivityManager activityManager = (ActivityManager) context.getSystemService(context.ACTIVITY_SERVICE);
                if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT_WATCH) {
                    List<ActivityManager.RunningAppProcessInfo> runningProcesses = activityManager.getRunningAppProcesses();
                    for (ActivityManager.RunningAppProcessInfo processInfo : runningProcesses) {
                        if (processInfo.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                            for (String activeProcess : processInfo.pkgList) {
                                if (activeProcess.equals(context.getPackageName())) {
                                    isInBackground = false;
                                }
                            }
                        }
                    }
                } else {
                    List<ActivityManager.RunningTaskInfo> taskInfo = activityManager.getRunningTasks(1);
                    ComponentName componentInfo = taskInfo.get(0).topActivity;
                    if (componentInfo.getPackageName().equals(context.getPackageName())) {
                        isInBackground = false;
                    }
                }
            }
            else {
                Log.i(TAG, "isAppInBackground: context is null!");
            }
        }
        catch (NullPointerException e) {
            throw new RuntimeException("Could not get isInBackground: " + e);
        }

        return isInBackground;
    }

    public static boolean isScreenOn() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT_WATCH) {
            DisplayManager dm = (DisplayManager) context.getSystemService(Context.DISPLAY_SERVICE);
            boolean screenOn = false;
            for (Display display : dm.getDisplays()) {
                if (display.getState() != Display.STATE_OFF) {
                    screenOn = true;
                }
            }
            return screenOn;
        }
        else {
            PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
            return pm.isScreenOn();
        }
    }

    // AsyncTask to register Device in GCM Server
    private void registerInBackground(final String emailID) {
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... params) {
                String msg = "";
                try {
                    if (gcm == null) {
                        gcm = GoogleCloudMessaging.getInstance(context);
                    }
                    regid = gcm.register(ApplicationConstants.SENDER_ID);
                    msg = "Registration ID :" + regid;

                } catch (IOException ex) {
                    msg = "Error :" + ex.getMessage();
                }
                return msg;
            }

            @Override
            protected void onPostExecute(String msg) {
                if (!TextUtils.isEmpty(regid)) {
                    // Store RegId created by GCM Server in SharedPref
                    storeRegIdinSharedPref(context, regid, emailID);
                    Toast.makeText(
                            context,
                            "Registered with GCM Server successfully.\n\n"
                                    + msg, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(
                            context,
                            "Reg ID Creation Failed.\n\nEither you haven't enabled Internet or GCM server is busy right now. Make sure you enabled Internet and try registering again after some time."
                                    + msg, Toast.LENGTH_LONG).show();
                }
            }
        }.execute(null, null, null);
    }

    public void onClick(final View view) {
        if (view == findViewById(R.id.send)) {
            new AsyncTask() {
                @Override
                protected Object doInBackground(Object[] params) {
                    String msg = "";
                    try {
                        Bundle data = new Bundle();
                        data.putString("my_message", "Hello World");
                        data.putString("my_action", "com.google.android.gcm.dem.app.ECHO_NOW");
                        String id = Integer.toString(msgId.incrementAndGet());
                        gcm.send(ApplicationConstants.SENDER_ID + "@gcm.googleapis.com", id, data);
                        msg = "Sent message";
                    }
                    catch (IOException ex) {
                        msg = "Error: " + ex.getMessage();
                    }
                    return msg;
                }

                protected void onPostExecute(String msg) {
                    mDisplay.append(msg + "\n");
                }
            }.execute(null, null, null);
        }
        else if (view == findViewById(R.id.clear)) {
            mDisplay.setText("");
        }
        else if (view == refreshButton) {
            refreshJSONString();
            refreshButton.setEnabled(false);
            nm.cancelAll();
        }
    }

    public void refreshJSONString() {
        Thread thread = new Thread(){
            @Override
            public void run() {
                try {
                    synchronized (this){
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                GcmIntentService gcmIntentService = new GcmIntentService();
                                String str = gcmIntentService.getMessage();

                                if (str != null) {
                                    if (!str.isEmpty()) {
                                        try {
                                            JSONObject jsonObject = new JSONObject(str);
                                            String name = jsonObject.getJSONObject("glossary").getJSONObject("GlossDiv").getJSONObject("GlossList").getJSONObject("GlossEntry").getString("ID");
                                            name += jsonObject.getJSONObject("glossary").getJSONObject("GlossDiv").getJSONObject("GlossList").getJSONObject("GlossEntry").getString("Abbrev");
                                            mDisplay.setText(name);
                                        } catch (JSONException e) {
                                            Log.i(TAG, "JSONException: " + e);
                                        }
                                    }
                                    else {
                                        try {
                                            Toast.makeText(context, context.getString(R.string.json_string_not_exists), Toast.LENGTH_LONG).show();
                                        } catch (Exception e) {
                                            Log.i(TAG, "Exception: " + e);
                                        }
                                    }
                                }
                                else {
                                    try {
                                        Toast.makeText(context, context.getString(R.string.json_string_not_exists), Toast.LENGTH_LONG).show();
                                    } catch (Exception e) {
                                        Log.i(TAG, "Exception: " + e);
                                    }
                                }
                            }
                        });
                    }
                }
                catch (Exception e){
                    e.printStackTrace();
                }
            }
        };
        thread.start();

        Toast.makeText(context, context.getString(R.string.json_string_refreshed), Toast.LENGTH_LONG).show();
    }



    public void idRegistration(View view) {
        String emailID = emailET.getText().toString();

        if (!TextUtils.isEmpty(emailID) && Utility.validate(emailID)) {

            // Check if Google Play Service is installed in Device
            // Play services is needed to handle GCM stuffs
            if (checkPlayServices()) {

                Log.i(TAG, "REG - Before registerInBackground: " + emailID);
                // Register Device in GCM Server
                registerInBackground(emailID);
            }
        }
        // When Email is invalid
        else {
            Toast.makeText(context, "Please enter valid email",
                    Toast.LENGTH_LONG).show();
        }
    }

    // Store  RegId and Email entered by User in SharedPref
    private void storeRegIdinSharedPref(Context context, String regId,
                                        String emailID) {
        Log.i(TAG, "REG - Before storeRegIdinSharedPref: " + regId + " " + emailID);

        SharedPreferences prefs = getSharedPreferences("UserDetails",
                Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(REG_ID, regId);
        editor.putString(EMAIL_ID, emailID);
        editor.commit();
        storeRegIdinServer();
    }

    // Share RegID with GCM Server Application (Php)
    private void storeRegIdinServer() {
        prgDialog.show();
        params.put("regId", regid);

        Log.i(TAG, "REG - storeRegIdinServer: " + regid + " ");

        // Make RESTful webservice call using AsyncHttpClient object
        AsyncHttpClient client = new AsyncHttpClient();
        client.post(ApplicationConstants.APP_SERVER_URL, params,
                new AsyncHttpResponseHandler() {
                    // When the response returned by REST has Http
                    // response code '200'
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                        // Hide Progress Dialog
                        prgDialog.hide();
                        if (prgDialog != null) {
                            prgDialog.dismiss();
                        }
                        Toast.makeText(context,
                                "Reg Id shared successfully with Web App ",
                                Toast.LENGTH_LONG).show();
                        Intent i = new Intent(context,
                                MainActivity.class);
                        i.putExtra("regId", regid);
                        //startActivity(i);
                        //finish();
                    }

                    // When the response returned by REST has Http
                    // response code other than '200' such as '404',
                    // '500' or '403' etc
                    @Override
                    public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                        // Hide Progress Dialog
                        prgDialog.hide();
                        if (prgDialog != null) {
                            prgDialog.dismiss();
                        }
                        // When Http response code is '404'
                        if (statusCode == 404) {
                            Toast.makeText(context,
                                    "Requested resource not found",
                                    Toast.LENGTH_LONG).show();
                        }
                        // When Http response code is '500'
                        else if (statusCode == 500) {
                            Toast.makeText(context,
                                    "Something went wrong at server end",
                                    Toast.LENGTH_LONG).show();
                        }
                        // When Http response code other than 404, 500
                        else {
                            Toast.makeText(
                                    context,
                                    "Unexpected Error occcured! [Most common Error: Device might "
                                            + "not be connected to Internet or remote server is not up and running], check for other errors as well",
                                    Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    public static void getNewMessage() {
        Toast.makeText(context, context.getString(R.string.new_json_string), Toast.LENGTH_LONG).show();
        Log.i(TAG, "getNewMessage --> show toast; " + context.getString(R.string.new_json_string));
        refreshButton.setEnabled(true);
    }
}

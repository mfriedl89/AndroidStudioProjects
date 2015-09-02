package at.snowreporter.buenoi;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.Dialog;
import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.hardware.display.DisplayManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.PowerManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Base64;
import android.util.Log;
import android.view.Display;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;

import com.loopj.android.http.*;

import org.apache.http.Header;
import org.json.JSONException;

import java.io.IOException;
import java.security.KeyStore;
import java.text.DateFormat;
import java.util.GregorianCalendar;
import java.util.List;

import at.snowreporter.buenoi.database.Message;
import at.snowreporter.buenoi.database.MessageRepo;
import at.snowreporter.buenoi.database.MyDatabaseHelper;

public class MainActivity extends AppCompatActivity {

    // Requestcode for GooglePlayServicesUtil.getErrorDialog
    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;

    // set context to context = getApplicationContext() in onCreate
    static Context context;

    // For internal logging
    static final String TAG = "Buenoi";

    // Instance for GoogleCloudMessaging
    static GoogleCloudMessaging gcm;

    // Registration ID of the device
    public static String regId;

    // Username from input
    private String inputUsername = "";

    // Password from input
    private String inputPassword = "";

    // Business ID from Webserver
    private String businessID = "";

    // Instance of a progress dialog
    private static ProgressDialog prgDialog;
    public static ProgressDialog prgDialogMessageListActivity;

    // Notification manager
    NotificationManager notificationManager;

    // Login -> show or don't show password (don't show is default)
    boolean checkShowPassword = false;

    public static String storedLoggedInUsername;
    static Button loggedInButton;

    // Set intent
    static Intent loginIntent;
    static Intent messageIntent;
    static Intent preferencesIntent;

    // Set activity at other activity
    public static Activity activity;

    // Database
    public static MessageRepo myMessageRepo;
    private int _Message_Id = 0;

    // Preferences
    public static Preferences myPreferences = new Preferences();

    // AsyncHttpClient for web services
    public static AsyncHttpClient client = new AsyncHttpClient();

    // Cookies
    static PersistentCookieStore myCookieStore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        context = getApplicationContext();

        myCookieStore = new PersistentCookieStore(context);

        activity = this;

        prgDialog = new ProgressDialog(this);
        // Set Progress Dialog Text
        prgDialog.setMessage(getString(R.string.prg_dialog_messagetext));
        // Set Cancelable as False
        prgDialog.setCancelable(false);

        Preferences.prefs = getSharedPreferences("UserDetails", Context.MODE_PRIVATE);

        myPreferences.storedRegistraionId = Preferences.prefs.getString(myPreferences.REG_ID, "");
        myPreferences.storedUsernameId = Preferences.prefs.getString(myPreferences.USERNAME_ID, "");
        myPreferences.storedPasswordId = Preferences.prefs.getString(myPreferences.PASSWORD_ID, "");
        myPreferences.storedBusinessId = Preferences.prefs.getString(myPreferences.BUSINESS_ID, "");
        myPreferences.storedInstantBookingArrived = Preferences.prefs.getInt(myPreferences.PREF_INSTANT_BOOKING_ARRIVED, 0);
        myPreferences.storedOfferAdopted = Preferences.prefs.getInt(myPreferences.PREF_OFFER_ADOPTED, 0);
        myPreferences.storedOfferRejected = Preferences.prefs.getInt(myPreferences.PREF_OFFER_REJECTED, 0);
        myPreferences.storedInquiryArrived = Preferences.prefs.getInt(myPreferences.PREF_INQUIRY_ARRIVED, 0);
        myPreferences.storedOldInboxMessages = Preferences.prefs.getInt(myPreferences.PREF_OLD_INBOX_MESSAGES, 0);
        myPreferences.storedFlatRateRequestArrived = Preferences.prefs.getInt(myPreferences.PREF_FLAT_RATE_REQUEST_ARRIVED, 0);
        myPreferences.storedContactForm = Preferences.prefs.getInt(myPreferences.PREF_CONTACT_FORM, 0);

        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        loginIntent = new Intent(context, MainActivity.class);
        preferencesIntent = new Intent(context, PreferencesActivity.class);

        //When Username-ID is set in Sharedpref, User will be taken to HomeActivity
        if (!TextUtils.isEmpty(myPreferences.storedRegistraionId)) {
            loginIntent.putExtra(myPreferences.REG_ID, myPreferences.storedRegistraionId);
        }

        if (!TextUtils.isEmpty(myPreferences.storedUsernameId)) {
            messageIntent = new Intent(context, MessageListActivity.class);
            startActivity(messageIntent);
            finish();
        }

        // database
        myMessageRepo = new MessageRepo(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkPlayServices();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);

        MenuItem itemSettings = menu.findItem(R.id.action_settings);
        itemSettings.setEnabled(false);
        // set the text for the login/logout-item
        MenuItem itemLoginLogout = menu.findItem(R.id.action_login_logout);
        itemLoginLogout.setTitle(R.string.login);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem item = menu.findItem(R.id.action_login_logout);
        item.setTitle(R.string.login);

        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_info) {
            showInfo();
            return true;
        } else if (id == R.id.action_login_logout) {
            login();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public static void showSettings() {
        getUserSettings();
    }

    public static void showInfo() {
        final Dialog infoDialog = new Dialog(activity);
        infoDialog.setTitle("Info");
        infoDialog.setContentView(R.layout.activity_info);
        infoDialog.show();

        final TextView loggedInText = (TextView) infoDialog.findViewById(R.id.loggedInTextInfo);
        final TextView loggedInUsername = (TextView) infoDialog.findViewById(R.id.loggedInUsernameInfo);

        if (!TextUtils.isEmpty(myPreferences.storedUsernameId)) {
            loginIntent.putExtra(myPreferences.USERNAME_ID, myPreferences.storedUsernameId);
            storedLoggedInUsername = loginIntent.getStringExtra(myPreferences.USERNAME_ID);
            loggedInUsername.setText(storedLoggedInUsername);
            loggedInText.setVisibility(View.VISIBLE);
        } else {
            storedLoggedInUsername = context.getResources().getString(R.string.nobodyLoggedIn);
            loggedInUsername.setText(storedLoggedInUsername);
            loggedInText.setVisibility(View.INVISIBLE);
        }

        // TODO: Delete testmessage [BEGIN]
        GregorianCalendar calendar = new GregorianCalendar();
        DateFormat df = DateFormat.getDateInstance(DateFormat.MEDIUM);
        String date = df.format(calendar.getTime());
        df = DateFormat.getTimeInstance(DateFormat.MEDIUM);
        String time = df.format(calendar.getTime());

        Message message = new Message();
        message.date = date;
        message.time = time;
        message.type = "test_anfrage";
        message.comment = "Testmessage " + date + " " + time;

        addMessage(message);
        // TODO: Delete testmessage [END]
    }

    // User login
    private void login() {
        final Dialog loginDialog = new Dialog(MainActivity.this);
        loginDialog.setTitle(R.string.login);
        loginDialog.setContentView(R.layout.login_customdialog_layout);
        loginDialog.show();

        final EditText loginAlertEditTextUsername = (EditText) loginDialog.findViewById(R.id.loginUsername);
        loginAlertEditTextUsername.setTypeface(Typeface.DEFAULT);
        final EditText loginAlertEditTextPassword = (EditText) loginDialog.findViewById(R.id.loginPassword);
        loginAlertEditTextPassword.setTypeface(Typeface.DEFAULT);
        CheckBox loginAlertCheckBoxShowPassword = (CheckBox) loginDialog.findViewById(R.id.loginShowPassword);
        final Button loginAlertButtonCancel = (Button) loginDialog.findViewById(R.id.loginButtonCancel);
        final Button loginAlertButtonLogin = (Button) loginDialog.findViewById(R.id.loginButtonLogin);
        loginAlertButtonLogin.setEnabled(false);

        loginAlertButtonCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginDialog.cancel();
            }
        });

        loginAlertButtonLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginDialog.cancel();
                prgDialog.show();
                idRegistration();
            }
        });

        loginAlertCheckBoxShowPassword.setOnCheckedChangeListener(new CompoundButton.
                OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    checkShowPassword = true;
                    loginAlertEditTextPassword.setInputType(InputType.TYPE_CLASS_TEXT |
                            InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);

                } else {
                    checkShowPassword = false;
                    loginAlertEditTextPassword.setInputType(InputType.TYPE_CLASS_TEXT |
                            InputType.TYPE_TEXT_VARIATION_PASSWORD);
                }
                loginAlertEditTextPassword.setTypeface(Typeface.DEFAULT);

                if (loginAlertEditTextPassword.isFocused()) {
                    loginAlertEditTextPassword.setSelection(loginAlertEditTextPassword.getText().length());
                }
            }
        });

        // Check user input - keyboard action
        loginAlertEditTextUsername.setOnEditorActionListener(new EditText.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                boolean handled = false;
                inputUsername = loginAlertEditTextUsername.getText().toString();
                inputPassword = loginAlertEditTextPassword.getText().toString();
                if (actionId == EditorInfo.IME_ACTION_NEXT) {
                    if (checkUsernamePassword()) {
                        loginAlertButtonLogin.setEnabled(true);
                        handled = true;
                    }
                }

                return handled;
            }
        });

        loginAlertEditTextUsername.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                inputUsername = loginAlertEditTextUsername.getText().toString();
                inputPassword = loginAlertEditTextPassword.getText().toString();
                if (checkUsernamePassword()) {
                    loginAlertButtonLogin.setEnabled(true);
                } else {
                    loginAlertButtonLogin.setEnabled(false);
                }
            }
        });

        loginAlertEditTextPassword.setOnEditorActionListener(new EditText.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                boolean handled = false;
                inputUsername = loginAlertEditTextUsername.getText().toString();
                inputPassword = loginAlertEditTextPassword.getText().toString();

                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    if (checkUsernamePassword()) {
                        loginAlertButtonLogin.setEnabled(true);
                        handled = true;
                    }
                }
                return handled;
            }
        });

        loginAlertEditTextPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                inputUsername = loginAlertEditTextUsername.getText().toString();
                inputPassword = loginAlertEditTextPassword.getText().toString();
                if (checkUsernamePassword()) {
                    loginAlertButtonLogin.setEnabled(true);
                } else {
                    loginAlertButtonLogin.setEnabled(false);
                }
            }
        });

        // Check user input - focus changed
        loginAlertEditTextUsername.setOnFocusChangeListener(new EditText.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                inputUsername = loginAlertEditTextUsername.getText().toString();
                inputPassword = loginAlertEditTextPassword.getText().toString();
                if (checkUsernamePassword()) {
                    loginAlertButtonLogin.setEnabled(true);
                }
            }
        });

        loginAlertEditTextPassword.setOnFocusChangeListener(new EditText.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                inputUsername = loginAlertEditTextUsername.getText().toString();
                inputPassword = loginAlertEditTextPassword.getText().toString();
                if (checkUsernamePassword()) {
                    loginAlertButtonLogin.setEnabled(true);
                }
            }
        });
    }

    // User logout
    public static void logout() {
        String deleteRegId = Preferences.prefs.getString(myPreferences.REG_ID, "");
        String deleteUsernameId = Preferences.prefs.getString(myPreferences.USERNAME_ID, "");

        Log.i(TAG, "Logout delete reg id: " + deleteRegId + " - username id: " + deleteUsernameId);

        deregisterInBackground(deleteRegId, deleteUsernameId);
    }

    // checks if app is in background
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
            } else {
                Log.i(TAG, "isAppInBackground: context is null!");
            }
        } catch (NullPointerException e) {
            throw new RuntimeException(e);
        }

        return isInBackground;
    }

    // Checks if the screen of the smartphone is activated
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
        } else {
            PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
            return pm.isScreenOn();
        }
    }

    // AsyncTask to register Device in GCM Server
    private void registerInBackground(final String usernameId, final String passwordId) {
        Log.i(TAG, "Registration in background - Start.");

        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... params) {
                Log.i(TAG, "Registration in background - Do in background.");
                String msg = "";
                try {
                    if (gcm == null) {
                        gcm = GoogleCloudMessaging.getInstance(context);
                    }
                    regId = gcm.register(ApplicationConstants.SENDER_ID);
                    msg = "Registration ID :" + regId;

                } catch (IOException ex) {
                    msg = "Error :" + ex.getMessage();
                }
                return msg;
            }

            @Override
            protected void onPostExecute(String msg) {
                Log.i(TAG, "Registration in background - On post execute.");
                if (!TextUtils.isEmpty(regId)) {
                    // Store RegId created by GCM Server in SharedPref
                    Log.i(TAG, "Registration in background - On post execute - before storeRegIdinServer.");

                    storeRegIdinServer(regId, usernameId, passwordId);
                } else {
                    Toast.makeText(
                            context,
                            getString(R.string.registration_failed)
                                    + msg, Toast.LENGTH_LONG).show();
                    prgDialog.hide();
                    if (prgDialog != null) {
                        prgDialog.dismiss();
                    }
                }
            }
        }.execute(null, null, null);
    }

    private static void deregisterInBackground(final String deleteRegId, final String deleteUsernameId) {
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... params) {
                String msg = "";
                try {
                    if (gcm == null) {
                        gcm = GoogleCloudMessaging.getInstance(context);
                    }
                    gcm.unregister();
                } catch (IOException ex) {
                    msg = "Error :" + ex.getMessage();

                    Toast.makeText(
                            context, context.getString(R.string.deregistration_failed)
                             + msg, Toast.LENGTH_LONG).show();
                }
                return msg;
            }

            @Override
            protected void onPostExecute(String msg) {
                deleteRegIdinServer(deleteRegId, deleteUsernameId);
            }
        }.execute(null, null, null);
    }


    // Check if play services are available
    private boolean checkPlayServices() {
        int resultColde = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (resultColde != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultColde)) {
                GooglePlayServicesUtil.getErrorDialog(resultColde, this, PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                Toast.makeText(
                        context,
                        R.string.message_checkplayservices,
                        Toast.LENGTH_LONG).show();
                Log.i(TAG, "This device is not supported.");
                finish();
            }
            return false;
        }
        return true;
    }

    // For ID registration on Login
    public void idRegistration() {
        if (checkUsernamePassword()) {
            // Check if Google Play Service is installed in Device
            // Play services is needed to handle GCM stuffs
            if (checkPlayServices()) {
                // Register Device in GCM Server
                registerInBackground(inputUsername, inputPassword);
            }
        }
    }

    private boolean checkUsernamePassword() {
        if (!TextUtils.isEmpty(inputUsername) && inputPassword.length() > 3) {
            return true;
        }

        return false;
    }

    // Store RegId and Username/Passwort entered by User in SharedPref
    private static void storeRegIdinSharedPref(String regId,
                                        String usernameId, String passwordId) {
        Preferences.editor = Preferences.prefs.edit();
        Preferences.editor.putString(myPreferences.REG_ID, regId);
        Preferences.editor.putString(myPreferences.USERNAME_ID, usernameId);
        Preferences.editor.putString(myPreferences.PASSWORD_ID, passwordId);
        Preferences.editor.commit();

        // Hide Progress Dialog
        prgDialog.hide();
        if (prgDialog != null) {
            prgDialog.dismiss();
        }
    }

    // Delete RegId and Username entered by User in SharedPref
    private static void deleteRegIdinSharedPref(String deleteRegId, String deleteUsernameId) {
        Preferences.editor = Preferences.prefs.edit();
        Preferences.editor.remove(myPreferences.REG_ID);
        Preferences.editor.remove(myPreferences.USERNAME_ID);
        Preferences.editor.remove(myPreferences.PASSWORD_ID);
        Preferences.editor.remove(myPreferences.PREF_INSTANT_BOOKING_ARRIVED);
        Preferences.editor.remove(myPreferences.PREF_OFFER_ADOPTED);
        Preferences.editor.remove(myPreferences.PREF_OFFER_REJECTED);
        Preferences.editor.remove(myPreferences.PREF_INQUIRY_ARRIVED);
        Preferences.editor.remove(myPreferences.PREF_OLD_INBOX_MESSAGES);
        Preferences.editor.remove(myPreferences.PREF_FLAT_RATE_REQUEST_ARRIVED);
        Preferences.editor.remove(myPreferences.PREF_CONTACT_FORM);
        Preferences.editor.commit();

        // Hide Progress Dialog
        prgDialogMessageListActivity.hide();
        if (prgDialogMessageListActivity != null) {
            prgDialogMessageListActivity.dismiss();
        }
    }

    // Share RegID with GCM Server Application (Php)
    private void storeRegIdinServer(String regId2, final String usernameId, final String passwordId) {
        String loginLink = String.format(ApplicationConstants.APP_SERVER_USER_LOGIN, usernameId, passwordId, regId);
        Log.i(TAG, "storeRegIdinServer - loginLink: " + loginLink);

        // Make RESTful webservice call using AsyncHttpClient object
        try {
            KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
            trustStore.load(null, null);
            MySSLSocketFactory sf = new MySSLSocketFactory(trustStore);
            sf.setHostnameVerifier(MySSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
            client.setSSLSocketFactory(sf);
        } catch (Exception e) {
            Log.i(TAG, "Exception MySSLSocketFactory: " + e.toString());
        }

        client.setBasicAuth(ApplicationConstants.BUENOI_USERNAME, ApplicationConstants.BUENOI_PASSWORD);
        client.addHeader("Authorization", "Basic " +
                Base64.encodeToString((ApplicationConstants.BUENOI_USERNAME + ":" +
                        ApplicationConstants.BUENOI_PASSWORD).getBytes(), Base64.NO_WRAP));
        myCookieStore.clear();
        client.setCookieStore(myCookieStore);

        Log.i(TAG, "Cookies storeRegIdinServer: " + myCookieStore.getCookies().toString());

        client.post(loginLink, new AsyncHttpResponseHandler() {
            @Override
            public void onStart() {
                super.onStart();
                Log.i(TAG, "AsyncHttpClient Request starts!");
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                Log.i(TAG, "storeRegIdinServer onSuccess: " + statusCode);

                Toast.makeText(context,
                        R.string.message_storeRegIdinServer_success,
                        Toast.LENGTH_LONG).show();
                Intent i = new Intent(context,
                        MainActivity.class);
                i.putExtra("regId", regId);

                myPreferences.storedUsernameId = usernameId;

                storeRegIdinSharedPref(regId, usernameId, passwordId);

                messageIntent = new Intent(context, MessageListActivity.class);
                startActivity(messageIntent);
                finish();
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                // Hide Progress Dialog
                prgDialog.hide();
                if (prgDialog != null) {
                    prgDialog.dismiss();
                }

                Log.i(TAG, "storeRegIdinServer onFailure: " + statusCode);

                // When Http response code is '400'
                if (statusCode == 400) {
                    Toast.makeText(context,
                            R.string.message_regIdinServer_failure_400,
                            Toast.LENGTH_LONG).show();
                }
                // When Http response code is '404'
                else if (statusCode == 404) {
                    Toast.makeText(context,
                            R.string.message_regIdinServer_failure_404,
                            Toast.LENGTH_LONG).show();
                }
                // When Http response code is '500'
                else if (statusCode == 500) {
                    Toast.makeText(context,
                            R.string.message_regIdinServer_failure_500,
                            Toast.LENGTH_LONG).show();
                }
                // When Http response code other than 404, 500
                else {
                    Toast.makeText(
                            context,
                            R.string.message_regIdinServer_failure,
                            Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    // Delete RegID in GCM Server Application (Php)
    private static void deleteRegIdinServer(final String deleteRegId, final String deleteUsernameId) {
        String logoutLink = String.format(ApplicationConstants.APP_SERVER_USER_LOGOUT);
        Log.i(TAG, "deleteRegIdinServer - logoutLink: " + logoutLink);

        // Make RESTful webservice call using AsyncHttpClient object
        try {
            KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
            trustStore.load(null, null);
            MySSLSocketFactory sf = new MySSLSocketFactory(trustStore);
            sf.setHostnameVerifier(MySSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
            client.setSSLSocketFactory(sf);
        } catch (Exception e) {
            Log.i(TAG, "Exception MySSLSocketFactory: " + e.toString());
        }

        client.setBasicAuth(ApplicationConstants.BUENOI_USERNAME, ApplicationConstants.BUENOI_PASSWORD);
        client.addHeader("Authorization", "Basic " +
                Base64.encodeToString((ApplicationConstants.BUENOI_USERNAME + ":" +
                        ApplicationConstants.BUENOI_PASSWORD).getBytes(), Base64.NO_WRAP));
        client.setCookieStore(myCookieStore);

        Log.i(TAG, "Cookies deleteRegIdinServer: " + myCookieStore.getCookies().toString());

        client.post(logoutLink, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                Log.i(TAG, "deleteRegIdinServer - onSuccess: " + statusCode);

                deleteRegIdinSharedPref(deleteRegId, deleteUsernameId);

                Toast.makeText(
                        context,
                        R.string.message_deleteRegIdinServer_success
                        , Toast.LENGTH_SHORT).show();

                myPreferences.storedUsernameId = "";
                context.deleteDatabase(myMessageRepo.myDatabaseHelper.DATABASE_NAME);
                myCookieStore.clear();

                loginIntent = new Intent(context, MainActivity.class);
                loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(loginIntent);
                activity.finish();
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                Log.i(TAG, "deleteRegIdinServer onFailure: " + statusCode);

                // When the response returned by REST has Http
                // response code other than '200' such as '404',
                // '500' or '403' etc
                // Hide Progress Dialog
                prgDialogMessageListActivity.hide();
                if (prgDialogMessageListActivity != null) {
                    prgDialogMessageListActivity.dismiss();
                }

                // When Http response code is '404'
                if (statusCode == 404) {
                    Toast.makeText(context,
                            R.string.message_regIdinServer_failure_404,
                            Toast.LENGTH_LONG).show();
                }
                // When Http response code is '500'
                else if (statusCode == 500) {
                    Toast.makeText(context,
                            R.string.message_regIdinServer_failure_500,
                            Toast.LENGTH_LONG).show();
                }
                // When Http response code other than 404, 500
                else {
                    Toast.makeText(
                            context,
                            R.string.message_regIdinServer_failure,
                            Toast.LENGTH_LONG).show();
                }
            }
        });
    }


    // New message is received
    public static void getNewMessage() {
        Toast.makeText(context, context.getString(R.string.new_json_string), Toast.LENGTH_LONG).show();
        Log.i(TAG, "getNewMessage --> show toast; " + context.getString(R.string.new_json_string));
    }

    public void buttonLogin(View view) {
        login();
    }

    // database
    public static void addMessage(Message message) {
        ContentValues values = new ContentValues();
        values.put(Message.COL_DATE, message.date);
        values.put(Message.COL_TIME, message.time);
        values.put(Message.COL_TYPE, message.type);
        values.put(Message.COL_COMMENT, message.comment);

        myMessageRepo.insert(message);

        if (!isAppInBackground()) {


            MessageListFragment.refreshListView();
        }
    }

    public static void getUserSettings() {
        final String[] getUserSettingsLink = {String.format(ApplicationConstants.APP_SERVER_GET_USER_SETTINGS)};
        Log.i(TAG, "getUserSettings - getUserSettingsLink: " + getUserSettingsLink[0]);

        final PreferencesJsonString myPreferencesJsonString = new PreferencesJsonString();

        try {
            KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
            trustStore.load(null, null);
            MySSLSocketFactory sf = new MySSLSocketFactory(trustStore);
            sf.setHostnameVerifier(MySSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
            //client.setTimeout(30*1000);
            client.setSSLSocketFactory(sf);
        } catch (Exception e) {
            Log.i(TAG, "Exception MySSLSocketFactory: " + e.toString());
        }

        client.setBasicAuth(ApplicationConstants.BUENOI_USERNAME,
                ApplicationConstants.BUENOI_PASSWORD, true);
        client.addHeader("Authorization", "Basic " +
                Base64.encodeToString((ApplicationConstants.BUENOI_USERNAME + ":" +
                        ApplicationConstants.BUENOI_PASSWORD).getBytes(), Base64.NO_WRAP));
        client.setCookieStore(myCookieStore);

        Log.i(TAG, "Cookies getUserSettings: " + myCookieStore.getCookies().toString());

        client.get(context, getUserSettingsLink[0], new TextHttpResponseHandler() {
            @Override
            public void onStart() {
                Log.i(TAG, "getUserSettingsLink Request starts!");
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, String responseString) {
                String businessId = "";
                Log.i(TAG, "getUserSettingsLink - onSuccess: " + statusCode + ", " + responseString);

                try {
                    myPreferencesJsonString.getPreferences(responseString);

                    storePreferencesInSharedPref();

                    preferencesIntent = new Intent(context, PreferencesActivity.class);
                    context.startActivity(preferencesIntent);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                Log.i(TAG, "getUserSettingsLink - onFailure: " + statusCode + ", " + responseString);

                // When Http response code is '404'
                if (statusCode == 404) {
                    Toast.makeText(context,
                            R.string.message_regIdinServer_failure_404,
                            Toast.LENGTH_LONG).show();
                }
                // When Http response code is '500'
                else if (statusCode == 500) {
                    Toast.makeText(context,
                            R.string.message_regIdinServer_failure_500,
                            Toast.LENGTH_LONG).show();
                }
                // When Http response code other than 404, 500
                else {
                    Toast.makeText(
                            context,
                            R.string.message_regIdinServer_failure,
                            Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    public static void storePreferencesInSharedPref() {
        Preferences.editor = Preferences.prefs.edit();
        Preferences.editor.putInt(myPreferences.PREF_INSTANT_BOOKING_ARRIVED, myPreferences.storedInstantBookingArrived);
        Preferences.editor.putInt(myPreferences.PREF_OFFER_ADOPTED, myPreferences.storedOfferAdopted);
        Preferences.editor.putInt(myPreferences.PREF_OFFER_REJECTED, myPreferences.storedOfferRejected);
        Preferences.editor.putInt(myPreferences.PREF_INQUIRY_ARRIVED, myPreferences.storedInquiryArrived);
        Preferences.editor.putInt(myPreferences.PREF_OLD_INBOX_MESSAGES, myPreferences.storedOldInboxMessages);
        Preferences.editor.putInt(myPreferences.PREF_FLAT_RATE_REQUEST_ARRIVED, myPreferences.storedFlatRateRequestArrived);
        Preferences.editor.putInt(myPreferences.PREF_CONTACT_FORM, myPreferences.storedContactForm);
        Preferences.editor.commit();
    }

    public static void storePreferencesInServer() {
        String preferenceString = myPreferences.storedBusinessId;
        if (myPreferences.storedInstantBookingArrived == 1) { preferenceString +=
                "&enable[]=sofortbuchung_eingelangt"; }
        if (myPreferences.storedOfferAdopted == 1) { preferenceString +=
                "&enable[]=angebot_angenommen"; }
        if (myPreferences.storedOfferRejected == 1) { preferenceString +=
                "&enable[]=angebot_abgelehnt"; }
        if (myPreferences.storedInquiryArrived == 1) { preferenceString +=
                "&enable[]=anfrage_eingelangt"; }
        if (myPreferences.storedOldInboxMessages == 1) { preferenceString +=
                "&enable[]=old_inbox_messages"; }
        if (myPreferences.storedFlatRateRequestArrived == 1) { preferenceString +=
                "&enable[]=anfrage_pauschale_eingelangt"; }
        if (myPreferences.storedContactForm == 1) { preferenceString +=
                "&enable[]=kontakt_formular"; }

        String storePreferencesLink = String.format(ApplicationConstants
                .APP_SERVER_SET_USER_SETTINGS, preferenceString);
        Log.i(TAG, "storePreferencesInServer - loginLink: " + storePreferencesLink);

        // Make RESTful webservice call using AsyncHttpClient object
        try {
            KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
            trustStore.load(null, null);
            MySSLSocketFactory sf = new MySSLSocketFactory(trustStore);
            sf.setHostnameVerifier(MySSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
            client.setSSLSocketFactory(sf);
        } catch (Exception e) {
            Log.i(TAG, "Exception MySSLSocketFactory: " + e.toString());
        }

        client.setBasicAuth(ApplicationConstants.BUENOI_USERNAME, ApplicationConstants.BUENOI_PASSWORD);
        client.addHeader("Authorization", "Basic " +
                Base64.encodeToString((ApplicationConstants.BUENOI_USERNAME + ":" +
                        ApplicationConstants.BUENOI_PASSWORD).getBytes(), Base64.NO_WRAP));
        client.setCookieStore(myCookieStore);

        Log.i(TAG, "Cookies storePreferencesInServer: " + myCookieStore.getCookies().toString());

        client.post(storePreferencesLink, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                Toast.makeText(context,
                        R.string.message_storePreferencesInServer_success,
                        Toast.LENGTH_LONG).show();
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                // When Http response code is '404'
                if (statusCode == 404) {
                    Toast.makeText(context,
                            R.string.message_regIdinServer_failure_404,
                            Toast.LENGTH_LONG).show();
                }
                // When Http response code is '500'
                else if (statusCode == 500) {
                    Toast.makeText(context,
                            R.string.message_regIdinServer_failure_500,
                            Toast.LENGTH_LONG).show();
                }
                // When Http response code other than 404, 500
                else {
                    Toast.makeText(
                            context,
                            R.string.message_regIdinServer_failure,
                            Toast.LENGTH_LONG).show();
                }
            }
        });
    }
}

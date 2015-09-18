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
import android.os.Bundle;
import android.os.PowerManager;
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
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.PersistentCookieStore;
import com.loopj.android.http.TextHttpResponseHandler;

import org.apache.http.Header;
import org.json.JSONException;

import java.io.IOException;
import java.security.KeyStore;
import java.util.List;

import at.snowreporter.buenoi.MessageList.MessageListActivity;
import at.snowreporter.buenoi.Preferences.Preferences;
import at.snowreporter.buenoi.Preferences.PreferencesActivity;
import at.snowreporter.buenoi.Preferences.PreferencesJsonString;
import at.snowreporter.buenoi.database.Message;
import at.snowreporter.buenoi.database.MyDatabaseHelper;

public class MainActivity extends AppCompatActivity {

    // Requestcode for GooglePlayServicesUtil.getErrorDialog
    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;

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

    // login link for webserver
    public static String loginLink = "";

    // Instance of a progress dialog
    private static ProgressDialog prgDialog;
    public static ProgressDialog messagePrgDialog;
    public static ProgressDialog logoutPrgDialog;

    // Notification manager
    NotificationManager notificationManager;

    // Login -> show or don't show password (don't show is default)
    boolean checkShowPassword = false;

    public static String storedLoggedInUsername;

    // Set intent
    static Intent loginIntent;
    static Intent messageIntent;
    static Intent preferencesIntent;

    // Set activity at other activity
    public static Activity activity;

    // Database
    public static MessageRepo myMessageRepo;
    public static MyDatabaseHelper myDatabaseHelper;

    // Preferences
    public static Preferences myPreferences = new Preferences();

    // AsyncHttpClient for web services
    public static AsyncHttpClient client = new AsyncHttpClient();

    // Cookies
    static PersistentCookieStore myCookieStore;

    // local timeouts for serveral server-services
    public static Integer getUserSettingsTimeout = 0;
    public static Integer storeRegIdinServerTimeout = 0;
    public static Integer deleteRegIdinServerTimeout = 0;
    public static Integer storePreferencesInServerTimeout = 0;
    public static Integer deletePreferencesInServerTimeout = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        myCookieStore = new PersistentCookieStore(this);

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

        loginIntent = new Intent(MyApp.getContext(), MainActivity.class);
        preferencesIntent = new Intent(MyApp.getContext(), PreferencesActivity.class);

        //When Username-ID is set in Sharedpref, User will be taken to HomeActivity
        if (!TextUtils.isEmpty(myPreferences.storedRegistraionId)) {
            loginIntent.putExtra(myPreferences.REG_ID, myPreferences.storedRegistraionId);
        }

        if (!TextUtils.isEmpty(myPreferences.storedUsernameId)) {
            messageIntent = new Intent(MyApp.getContext(), MessageListActivity.class);
            startActivity(messageIntent);
            finish();
        }

        // database
        myMessageRepo = new MessageRepo();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.i(TAG, "onResume");
        checkPlayServices();
        //loginRefresh();
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
        getUserSettingsTimeout = 0;
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
            storedLoggedInUsername = MyApp.getContext().getResources().getString(R.string.nobodyLoggedIn);
            loggedInUsername.setText(storedLoggedInUsername);
            loggedInText.setVisibility(View.INVISIBLE);
        }
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
                        loginAlertButtonLogin.setTextColor(getResources().getColor(R.color.button_enabled));
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
                    loginAlertButtonLogin.setTextColor(getResources().getColor(R.color.button_enabled));
                } else {
                    loginAlertButtonLogin.setEnabled(false);
                    loginAlertButtonLogin.setTextColor(getResources().getColor(R.color.button_disabled));
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
                        loginAlertButtonLogin.setTextColor(getResources().getColor(R.color.button_enabled));
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
                    loginAlertButtonLogin.setTextColor(getResources().getColor(R.color.button_enabled));
                } else {
                    loginAlertButtonLogin.setEnabled(false);
                    loginAlertButtonLogin.setTextColor(getResources().getColor(R.color.button_disabled));
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
                    loginAlertButtonLogin.setTextColor(getResources().getColor(R.color.button_enabled));
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
                    loginAlertButtonLogin.setTextColor(getResources().getColor(R.color.button_enabled));
                }
            }
        });
    }

    // User logout
    public static void logout() {
        String deleteRegId = Preferences.prefs.getString(myPreferences.REG_ID, "");
        String deleteUsernameId = Preferences.prefs.getString(myPreferences.USERNAME_ID, "");

        deleteRegIdinServerTimeout = 0;

        Log.i(TAG, "Logout delete reg id: " + deleteRegId + " - username id: " + deleteUsernameId);

        deregisterInBackground(deleteRegId, deleteUsernameId);
    }

    // checks if app is in background
    public static boolean isAppInBackground() {
        boolean isInBackground = true;
        try {
            if (MyApp.getContext() != null) {
                ActivityManager activityManager = (ActivityManager) MyApp.getContext().getSystemService(MyApp.getContext().ACTIVITY_SERVICE);
                if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT_WATCH) {
                    List<ActivityManager.RunningAppProcessInfo> runningProcesses = activityManager.getRunningAppProcesses();
                    for (ActivityManager.RunningAppProcessInfo processInfo : runningProcesses) {
                        if (processInfo.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                            for (String activeProcess : processInfo.pkgList) {
                                if (activeProcess.equals(MyApp.getContext().getPackageName())) {
                                    isInBackground = false;
                                }
                            }
                        }
                    }
                } else {
                    List<ActivityManager.RunningTaskInfo> taskInfo = activityManager.getRunningTasks(1);
                    ComponentName componentInfo = taskInfo.get(0).topActivity;
                    if (componentInfo.getPackageName().equals(MyApp.getContext().getPackageName())) {
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
            DisplayManager dm = (DisplayManager) MyApp.getContext().getSystemService(Context.DISPLAY_SERVICE);
            boolean screenOn = false;
            for (Display display : dm.getDisplays()) {
                if (display.getState() != Display.STATE_OFF) {
                    screenOn = true;
                }
            }
            return screenOn;
        } else {
            PowerManager pm = (PowerManager) MyApp.getContext().getSystemService(Context.POWER_SERVICE);
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
                        gcm = GoogleCloudMessaging.getInstance(MyApp.getContext());
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
                            MyApp.getContext(),
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
                        gcm = GoogleCloudMessaging.getInstance(MyApp.getContext());
                    }
                    gcm.unregister();
                } catch (IOException ex) {
                    msg = "Error :" + ex.getMessage();

                    Toast.makeText(
                            MyApp.getContext(), MyApp.getContext().getString(R.string.deregistration_failed)
                             + msg, Toast.LENGTH_LONG).show();
                }
                return msg;
            }

            @Override
            protected void onPostExecute(String msg) {
                if (myPreferences.storedInstantBookingArrived != 0 ||
                        myPreferences.storedOfferAdopted != 0 ||
                        myPreferences.storedOfferRejected != 0 ||
                        myPreferences.storedInquiryArrived != 0 ||
                        myPreferences.storedOldInboxMessages != 0 ||
                        myPreferences.storedFlatRateRequestArrived != 0 ||
                        myPreferences.storedContactForm != 0) {
                    deletePreferencesInServer();
                }
                else {
                    deletePreferencesInSharedPref();
                }
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
                        MyApp.getContext(),
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
    private static void  deleteRegIdinSharedPref() {
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
        hidePrgDialog();
    }

    // Share RegID with GCM Server Application (Php)
    private void storeRegIdinServer(String regId2, final String usernameId, final String passwordId) {
        loginLink = String.format(ApplicationConstants.APP_SERVER_USER_LOGIN, usernameId, passwordId, regId);
        Log.i(TAG, "storeRegIdinServer - loginLink: " + loginLink);

        serverAuthentication();

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

                Toast.makeText(MyApp.getContext(),
                        R.string.message_storeRegIdinServer_success,
                        Toast.LENGTH_LONG).show();
                Intent i = new Intent(MyApp.getContext(),
                        MainActivity.class);
                i.putExtra("regId", regId);

                myPreferences.storedUsernameId = usernameId;

                storeRegIdinSharedPref(regId, usernameId, passwordId);

                hidePrgDialog();

                messageIntent = new Intent(MyApp.getContext(), MessageListActivity.class);
                startActivity(messageIntent);
                finish();
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                hidePrgDialog();

                Log.i(TAG, "storeRegIdinServer onFailure: " + statusCode);

                // When Http response code is '400'
                if (statusCode == 400) {
                    Toast.makeText(MyApp.getContext(),
                            R.string.message_regIdinServer_failure_400,
                            Toast.LENGTH_LONG).show();
                }
                // When Http response code is '404'
                else if (statusCode == 404) {
                    Toast.makeText(MyApp.getContext(),
                            R.string.message_regIdinServer_failure_404,
                            Toast.LENGTH_LONG).show();
                }
                // When Http response code is '500'
                else if (statusCode == 500) {
                    Toast.makeText(MyApp.getContext(),
                            R.string.message_regIdinServer_failure_500,
                            Toast.LENGTH_LONG).show();
                }
                // When Http response code other than 404, 500
                else {
                    Toast.makeText(
                            MyApp.getContext(),
                            R.string.message_regIdinServer_failure,
                            Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    // Delete RegID in GCM Server Application (Php)
    private static void deleteRegIdinServer() {
        final String logoutLink = String.format(ApplicationConstants.APP_SERVER_USER_LOGOUT);
        Log.i(TAG, "deleteRegIdinServer - logoutLink: " + logoutLink);

        serverAuthentication();

        client.setCookieStore(myCookieStore);

        Log.i(TAG, "Cookies deleteRegIdinServer: " + myCookieStore.getCookies().toString());

        client.post(logoutLink, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                Log.i(TAG, "deleteRegIdinServer - onSuccess: " + statusCode);

                deleteRegIdinSharedPref();

                Toast.makeText(
                        MyApp.getContext(),
                        R.string.message_deleteRegIdinServer_success
                        , Toast.LENGTH_SHORT).show();

                myPreferences.storedUsernameId = "";
                MyApp.getContext().deleteDatabase(myDatabaseHelper.DATABASE_NAME);
                myCookieStore.clear();

                hideLogoutPrgDialog();

                loginIntent = new Intent(MyApp.getContext(), MainActivity.class);
                Log.i(TAG, "deleteRegIdinServer - loginIntent: " + loginIntent);
                activity.startActivity(loginIntent);
                Log.i(TAG, "deleteRegIdinServer - activity: " + activity);
                activity.finish();

            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                Log.i(TAG, "deleteRegIdinServer onFailure: " + statusCode);

                // When Http response code is '0' or '401'
                if (statusCode == 401 || statusCode == 0) {
                    if (deleteRegIdinServerTimeout != ApplicationConstants.SERVER_TIMEOUT) {
                        loginRefresh();
                        deleteRegIdinServerTimeout++;
                        deleteRegIdinServer();
                    } else {
                        Toast.makeText(MyApp.getContext(),
                                R.string.message_server_failure_401,
                                Toast.LENGTH_LONG).show();
                    }
                }
                // When Http response code is '404'
                else if (statusCode == 404) {
                    Toast.makeText(MyApp.getContext(),
                            R.string.message_server_failure_404,
                            Toast.LENGTH_LONG).show();
                }
                // When Http response code is '500'
                else if (statusCode == 500) {
                    Toast.makeText(MyApp.getContext(),
                            R.string.message_server_failure_500,
                            Toast.LENGTH_LONG).show();
                }
                // When Http response code other than 404, 500
                else {
                    Toast.makeText(
                            MyApp.getContext(),
                            R.string.message_server_failure,
                            Toast.LENGTH_LONG).show();
                }
            }
        });
    }


    // New message is received
    public static void getNewMessage() {
        Toast.makeText(MyApp.getContext(), MyApp.getContext().getString(R.string.new_json_string), Toast.LENGTH_LONG).show();
        Log.i(TAG, "getNewMessage --> show toast; " + MyApp.getContext().getString(R.string.new_json_string));
        MessageListActivity.refreshListView();

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
        values.put(Message.COL_READ, 0);

        if (myMessageRepo == null) {
            myMessageRepo = new MessageRepo();
        }

        Log.i(TAG, "addMessage: values = " + values + ", myMessageRepo = " + myMessageRepo +
                ", myDatabaseHelper = " + myDatabaseHelper);

        myMessageRepo.insert(values);
    }

    public static void getUserSettings() {
        final String[] getUserSettingsLink = {String.format(ApplicationConstants.APP_SERVER_GET_USER_SETTINGS)};
        Log.i(TAG, "getUserSettings - getUserSettingsLink: " + getUserSettingsLink[0]);

        final PreferencesJsonString myPreferencesJsonString = new PreferencesJsonString();

        serverAuthentication();

        client.setCookieStore(myCookieStore);

        Log.i(TAG, "Cookies getUserSettings: " + myCookieStore.getCookies().toString());

        client.get(MyApp.getContext(), getUserSettingsLink[0], new TextHttpResponseHandler() {
            @Override
            public void onStart() {
                Log.i(TAG, "getUserSettingsLink Request starts!");
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, String responseString) {
                Log.i(TAG, "getUserSettingsLink - onSuccess: " + statusCode + ", " + responseString);


                try {
                    myPreferencesJsonString.getPreferences(responseString);

                    storePreferencesInSharedPref();

                    hideMessagePrgDialog();

                    preferencesIntent = new Intent(MyApp.getContext(), PreferencesActivity.class);
                    preferencesIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    MyApp.getContext().startActivity(preferencesIntent);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                Log.i(TAG, "getUserSettingsLink - onFailure: " + statusCode + ", " + responseString);

                // When Http response code is '0' or '401'
                if (statusCode == 401 || statusCode == 0) {
                    if (getUserSettingsTimeout != ApplicationConstants.SERVER_TIMEOUT) {
                        loginRefresh();
                        getUserSettingsTimeout++;
                        getUserSettings();
                    } else {
                        Toast.makeText(MyApp.getContext(),
                                R.string.message_regIdinServer_failure_401,
                                Toast.LENGTH_LONG).show();
                    }
                }
                // When Http response code is '404'
                else if (statusCode == 404) {
                    Toast.makeText(MyApp.getContext(),
                            R.string.message_regIdinServer_failure_404,
                            Toast.LENGTH_LONG).show();
                }
                // When Http response code is '500'
                else if (statusCode == 500) {
                    Toast.makeText(MyApp.getContext(),
                            R.string.message_regIdinServer_failure_500,
                            Toast.LENGTH_LONG).show();
                }
                // When Http response code other than 404, 500
                else {
                    Toast.makeText(
                            MyApp.getContext(),
                            R.string.message_regIdinServer_failure,
                            Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    public static void storePreferencesInSharedPref() {
        Preferences.editor = Preferences.prefs.edit();
        Preferences.editor.putString(myPreferences.BUSINESS_ID, myPreferences.storedBusinessId);
        Preferences.editor.putInt(myPreferences.PREF_INSTANT_BOOKING_ARRIVED, myPreferences.storedInstantBookingArrived);
        Preferences.editor.putInt(myPreferences.PREF_OFFER_ADOPTED, myPreferences.storedOfferAdopted);
        Preferences.editor.putInt(myPreferences.PREF_OFFER_REJECTED, myPreferences.storedOfferRejected);
        Preferences.editor.putInt(myPreferences.PREF_INQUIRY_ARRIVED, myPreferences.storedInquiryArrived);
        Preferences.editor.putInt(myPreferences.PREF_OLD_INBOX_MESSAGES, myPreferences.storedOldInboxMessages);
        Preferences.editor.putInt(myPreferences.PREF_FLAT_RATE_REQUEST_ARRIVED, myPreferences.storedFlatRateRequestArrived);
        Preferences.editor.putInt(myPreferences.PREF_CONTACT_FORM, myPreferences.storedContactForm);
        Preferences.editor.commit();
    }

    public static void deletePreferencesInSharedPref() {
        Preferences.editor = Preferences.prefs.edit();
        Preferences.editor.remove(myPreferences.PREF_INSTANT_BOOKING_ARRIVED);
        Preferences.editor.remove(myPreferences.PREF_OFFER_ADOPTED);
        Preferences.editor.remove(myPreferences.PREF_OFFER_REJECTED);
        Preferences.editor.remove(myPreferences.PREF_INQUIRY_ARRIVED);
        Preferences.editor.remove(myPreferences.PREF_OLD_INBOX_MESSAGES);
        Preferences.editor.remove(myPreferences.PREF_FLAT_RATE_REQUEST_ARRIVED);
        Preferences.editor.remove(myPreferences.PREF_CONTACT_FORM);
        Preferences.editor.commit();

        deleteRegIdinServer();
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

        serverAuthentication();

        client.setCookieStore(myCookieStore);

        Log.i(TAG, "Cookies storePreferencesInServer: " + myCookieStore.getCookies().toString());

        client.post(storePreferencesLink, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                Log.i(TAG, "storePreferencesInServer onSuccess: " + statusCode);

                Toast.makeText(MyApp.getContext(),
                        R.string.message_storePreferencesInServer_success,
                        Toast.LENGTH_LONG).show();
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                Log.i(TAG, "storePreferencesInServer onFailure: " + statusCode);

                // When Http response code is '0' or '401'
                if (statusCode == 401 || statusCode == 0) {
                    if (storePreferencesInServerTimeout != ApplicationConstants.SERVER_TIMEOUT) {
                        loginRefresh();
                        storePreferencesInServerTimeout++;
                        storePreferencesInServer();
                    } else {
                        Toast.makeText(MyApp.getContext(),
                                R.string.message_regIdinServer_failure_401,
                                Toast.LENGTH_LONG).show();
                    }
                }
                // When Http response code is '404'
                if (statusCode == 404) {
                    Toast.makeText(MyApp.getContext(),
                            R.string.message_regIdinServer_failure_404,
                            Toast.LENGTH_LONG).show();
                }
                // When Http response code is '500'
                else if (statusCode == 500) {
                    Toast.makeText(MyApp.getContext(),
                            R.string.message_regIdinServer_failure_500,
                            Toast.LENGTH_LONG).show();
                }
                // When Http response code other than 404, 500
                else {
                    Toast.makeText(
                            MyApp.getContext(),
                            R.string.message_regIdinServer_failure,
                            Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    public static void deletePreferencesInServer() {
        String preferenceString = myPreferences.storedBusinessId;

        String deletePreferencesLink = String.format(ApplicationConstants
                .APP_SERVER_SET_USER_SETTINGS, preferenceString);
        Log.i(TAG, "deletePreferencesInServer - Link: " + deletePreferencesLink);

        serverAuthentication();

        client.setCookieStore(myCookieStore);

        Log.i(TAG, "Cookies deletePreferencesInServer: " + myCookieStore.getCookies().toString());

        client.post(deletePreferencesLink, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                Log.i(TAG, "deletePreferencesInServer onSuccess: " + statusCode);

                deletePreferencesInSharedPref();
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                Log.i(TAG, "deletePreferencesInServer onFailure: " + statusCode);
                // When Http response code is '0' or '401'
                if (deletePreferencesInServerTimeout != ApplicationConstants.SERVER_TIMEOUT) {
                    loginRefresh();
                    deletePreferencesInServerTimeout++;
                    deletePreferencesInServer();
                } else {
                    Toast.makeText(MyApp.getContext(),
                            R.string.message_regIdinServer_failure_401,
                            Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    public static void serverAuthentication() {
        try {
            KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
            trustStore.load(null, null);
            at.snowreporter.buenoi.database.MySSLSocketFactory sf = new at.snowreporter.buenoi.database.MySSLSocketFactory(trustStore);
            sf.setHostnameVerifier(at.snowreporter.buenoi.database.MySSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
            client.setSSLSocketFactory(sf);
        } catch (Exception e) {
            Log.i(TAG, "Exception MySSLSocketFactory: " + e.toString());
        }

        client.setAuthenticationPreemptive(true);
        client.setBasicAuth(ApplicationConstants.BUENOI_USERNAME, ApplicationConstants.BUENOI_PASSWORD);
        client.addHeader("Authorization", "Basic " +
                Base64.encodeToString((ApplicationConstants.BUENOI_USERNAME + ":" +
                        ApplicationConstants.BUENOI_PASSWORD).getBytes(), Base64.NO_WRAP));
    }

    public static void loginRefresh() {
        String prefRegId = Preferences.prefs.getString(myPreferences.REG_ID, "");
        String prefUsernameId = Preferences.prefs.getString(myPreferences.USERNAME_ID, "");
        String prefPasswordId = Preferences.prefs.getString(myPreferences.PASSWORD_ID, "");

        loginLink = String.format(ApplicationConstants.APP_SERVER_USER_LOGIN, prefUsernameId, prefPasswordId, prefRegId);

        Log.i(TAG, "loginRefresh - loginLink: " + loginLink);

        serverAuthentication();

        client.setCookieStore(myCookieStore);

        client.post(loginLink, new AsyncHttpResponseHandler() {
            @Override
            public void onStart() {
                super.onStart();
                Log.i(TAG, "AsyncHttpClient Request starts!");
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                Log.i(TAG, "loginRefresh onSuccess: " + statusCode);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                // Hide Progress Dialog
                Log.i(TAG, "loginRefresh onFailure: " + statusCode);
            }
        });
    }

    public static void getServerStatus() {
        final String[] getServerStatusLink = {String.format(ApplicationConstants.APP_SERVER_STATUS)};
        Log.i(TAG, "getServerStatus - getServerStatusLink: " + getServerStatusLink[0]);

        final PreferencesJsonString myPreferencesJsonString = new PreferencesJsonString();

        serverAuthentication();

        client.setCookieStore(myCookieStore);

        Log.i(TAG, "Cookies getServerStatus: " + myCookieStore.getCookies().toString());

        client.get(MyApp.getContext(), getServerStatusLink[0], new TextHttpResponseHandler() {
            @Override
            public void onStart() {
                Log.i(TAG, "getServerStatusLink Request starts!");
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, String responseString) {
                String businessId = "";
                Log.i(TAG, "getServerStatusLink - onSuccess: " + statusCode + ", " + responseString);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                Log.i(TAG, "getServerStatusLink - onFailure: " + statusCode + ", " + responseString);

                // When Http response code is '404'
                if (statusCode == 401 || statusCode == 0) {
                }
                // When Http response code is '404'
                else if (statusCode == 404) {
                    Toast.makeText(MyApp.getContext(),
                            R.string.message_regIdinServer_failure_404,
                            Toast.LENGTH_LONG).show();
                }
                // When Http response code is '500'
                else if (statusCode == 500) {
                    Toast.makeText(MyApp.getContext(),
                            R.string.message_regIdinServer_failure_500,
                            Toast.LENGTH_LONG).show();
                }
                // When Http response code other than 404, 500
                else {
                    Toast.makeText(
                            MyApp.getContext(),
                            R.string.message_regIdinServer_failure,
                            Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    public static void hidePrgDialog() {
        // Hide Progress Dialog
        prgDialog.hide();
        if (prgDialog != null) {
            prgDialog.dismiss();
        }
    }

    public static void hideMessagePrgDialog() {
        // Hide Progress Dialog
        messagePrgDialog.hide();
        if (messagePrgDialog != null) {
            messagePrgDialog.dismiss();
        }
    }

    public static void hideLogoutPrgDialog() {
        // Hide Progress Dialog
        logoutPrgDialog.hide();
        if (logoutPrgDialog != null) {
            logoutPrgDialog.dismiss();
        }
    }

    public static String modifiedTypeText(String type) {
        String modTypeText = "";

        switch (type) {
            case "sofortbuchung_eingelangt":
                modTypeText = MyApp.getContext().getString(R.string.instant_booking_arrived);
                break;
            case "angebot_angenommen":
                modTypeText = MyApp.getContext().getString(R.string.offer_adopted);
                break;
            case "angebot_abgelehnt":
                modTypeText = MyApp.getContext().getString(R.string.offer_rejected);
                break;
            case "anfrage_eingelangt":
                modTypeText = MyApp.getContext().getString(R.string.inquiry_arrived);
                break;
            case "old_inbox_messages":
                modTypeText = MyApp.getContext().getString(R.string.old_inbox_messages);
                break;
            case "anfrage_pauschale_eingelangt":
                modTypeText = MyApp.getContext().getString(R.string.flat_rate_request_arrived);
                break;
            case "kontakt_formular":
                modTypeText = MyApp.getContext().getString(R.string.contact_form);
                break;
            default:
                modTypeText = MyApp.getContext().getString(R.string.undefine_type);
                break;
        }

        return modTypeText;
    }
}

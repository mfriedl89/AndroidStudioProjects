package at.snowreporter.buenoi;

import android.app.ActivityManager;
import android.app.Dialog;
import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.hardware.display.DisplayManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.PowerManager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
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
import com.loopj.android.http.RequestParams;

import org.apache.http.Header;

import java.io.IOException;
import java.util.List;

public class MainActivity extends ActionBarActivity {

    // Requestcode for GooglePlayServicesUtil.getErrorDialog
    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;

    // set context to context = getApplicationContext() in onCreate
    static Context context;

    // For internal logging
    static final String TAG = "Buenoi";

    // Instance for GoogleCloudMessaging
    GoogleCloudMessaging gcm;

    // Registration ID of the device
    String regId;

    // Instance of request params
    RequestParams params = new RequestParams();

    // Stores the regId on phone
    public static final String REG_ID = "regId";

    // Stores the emailId on phone
    public static final String EMAIL_ID = "emailId";

    // Stores the passwordId on phone
    public static final String PASSWORD_ID = "passwordId";

    // Email from input
    String inputEmail = "";

    // Password from input
    String inputPassword = "";

    // Instance of a progress dialog
    ProgressDialog prgDialog;

    // Notification manager
    NotificationManager notificationManager;

    // Login -> show or don't show password (don't show is default)
    boolean checkShowPassword = false;

    TextView loggedInText;
    TextView loggedInEmail;
    String storedLoggedInEmail;
    Button loggedInButton;

    // Set intent
    Intent intent;

    //Stored preferences
    String storedRegistraionId;
    String storedEmailId;
    String storedPasswordId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        context = getApplicationContext();

        prgDialog = new ProgressDialog(this);
        // Set Progress Dialog Text
        prgDialog.setMessage("Please wait...");
        // Set Cancelable as False
        prgDialog.setCancelable(false);

        SharedPreferences prefs = getSharedPreferences("UserDetails", Context.MODE_PRIVATE);

        storedRegistraionId = prefs.getString(REG_ID, "");
        storedEmailId = prefs.getString(EMAIL_ID, "");
        storedPasswordId = prefs.getString(PASSWORD_ID, "");

        loggedInText = (TextView) findViewById(R.id.loggedInText);
        loggedInEmail = (TextView) findViewById(R.id.loggedInEmail);
        loggedInButton = (Button) findViewById(R.id.button_login);

        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        intent = new Intent(context, MainActivity.class);

        //When Email ID is set in Sharedpref, User will be taken to HomeActivity
        if (!TextUtils.isEmpty(storedRegistraionId)) {
            intent.putExtra(REG_ID, storedRegistraionId);
        }

        if (!TextUtils.isEmpty(storedEmailId)) {
            intent.putExtra(EMAIL_ID, storedEmailId);
            storedLoggedInEmail = intent.getStringExtra(EMAIL_ID);
            loggedInEmail.setText(storedLoggedInEmail);
            loggedInText.setVisibility(View.VISIBLE);
            loggedInButton.setVisibility(View.INVISIBLE);
        }
        else {
            storedLoggedInEmail = getString(R.string.nobodyLoggedIn);
            loggedInEmail.setText(storedLoggedInEmail);
            loggedInText.setVisibility(View.INVISIBLE);
            loggedInButton.setVisibility(View.VISIBLE);
        }
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

        // set the text for the login/logout-item
        MenuItem item = menu.findItem(R.id.action_login_logout);
        if (TextUtils.isEmpty(storedEmailId)) {
            item.setTitle(R.string.login);
        }
        else {
            item.setTitle(R.string.logout);
        }

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem item = menu.findItem(R.id.action_login_logout);
        if (TextUtils.isEmpty(storedEmailId)) {
            item.setTitle(R.string.login);
        }
        else {
            item.setTitle(R.string.logout);
        }

        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }
        else if (id == R.id.action_help_feedback) {
            return true;
        }
        else if (id == R.id.action_login_logout) {
            if (TextUtils.isEmpty(storedEmailId)) {
                login();
            }
            else {
                logout();
            }
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    // User login
    private void login() {
        final Dialog loginDialog = new Dialog(MainActivity.this);
        loginDialog.setTitle(R.string.login);
        loginDialog.setContentView(R.layout.login_customdialog_layout);
        loginDialog.show();

        final EditText loginAlertEditTextEMail = (EditText) loginDialog.findViewById(R.id.loginEMail);
        loginAlertEditTextEMail.setTypeface(Typeface.DEFAULT);
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
        loginAlertEditTextEMail.setOnEditorActionListener(new EditText.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                boolean handled = false;
                inputEmail = loginAlertEditTextEMail.getText().toString();
                inputPassword = loginAlertEditTextPassword.getText().toString();
                if (actionId == EditorInfo.IME_ACTION_NEXT) {
                    Log.i(TAG, "Email Input - Next clicked");

                    if (checkEmailPassword()) {
                        loginAlertButtonLogin.setEnabled(true);
                        handled = true;
                    }
                }

                return handled;
            }
        });

        loginAlertEditTextPassword.setOnEditorActionListener(new EditText.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                boolean handled = false;
                inputEmail = loginAlertEditTextEMail.getText().toString();
                inputPassword = loginAlertEditTextPassword.getText().toString();
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    Log.i(TAG, "Password Input - Done clicked");

                    if (checkEmailPassword()) {
                        loginAlertButtonLogin.setEnabled(true);
                        handled = true;
                    }
                }
                return handled;
            }
        });

        // Check user input - focus changed
        loginAlertEditTextEMail.setOnFocusChangeListener(new EditText.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                inputEmail = loginAlertEditTextEMail.getText().toString();
                inputPassword = loginAlertEditTextPassword.getText().toString();
                if (checkEmailPassword()) {
                    loginAlertButtonLogin.setEnabled(true);
                }
            }
        });

        loginAlertEditTextPassword.setOnFocusChangeListener(new EditText.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                inputEmail = loginAlertEditTextEMail.getText().toString();
                inputPassword = loginAlertEditTextPassword.getText().toString();
                if (checkEmailPassword()) {
                    loginAlertButtonLogin.setEnabled(true);
                }
            }
        });
    }

    // User logout
    private void logout() {
        SharedPreferences prefs = getSharedPreferences("UserDetails", Context.MODE_PRIVATE);

        String deleteRegId = prefs.getString(REG_ID, "");
        String deleteEmailId = prefs.getString(EMAIL_ID, "");

        Log.i(TAG, "Logout delete reg id: " + deleteRegId + " - email id: " + deleteEmailId);

        prgDialog.show();

        deregisterInBackground(deleteRegId, deleteEmailId);
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
            }
            else {
                Log.i(TAG, "isAppInBackground: context is null!");
            }
        }
        catch (NullPointerException e) {
            throw new RuntimeException("Could not get isAppInBackground: " + e);
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
        }
        else {
            PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
            return pm.isScreenOn();
        }
    }

    // AsyncTask to register Device in GCM Server
    private void registerInBackground(final String emailId, final String passwordId) {
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... params) {
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
                if (!TextUtils.isEmpty(regId)) {
                    // Store RegId created by GCM Server in SharedPref
                    storeRegIdinSharedPref(regId, emailId, passwordId);
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

    private void deregisterInBackground(final String deleteRegId, final String deleteEmailId) {
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
                }
                return msg;
            }

            @Override
            protected void onPostExecute(String msg) {
                deleteRegIdinSharedPref(deleteRegId, deleteEmailId);
                Toast.makeText(
                        context,
                        "Unregistered with GCM Server successfully."
                        , Toast.LENGTH_SHORT).show();

                loggedInEmail.setText(R.string.nobodyLoggedIn);
                loggedInText.setVisibility(View.INVISIBLE);
                loggedInButton.setVisibility(View.VISIBLE);
                storedEmailId = "";
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
                        "This device doesn't support Play services, App will not work normally",
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
        if (checkEmailPassword()) {
            // Check if Google Play Service is installed in Device
            // Play services is needed to handle GCM stuffs
            if (checkPlayServices()) {
                // Register Device in GCM Server
                registerInBackground(inputEmail, inputPassword);
            }
        }

        /*if (!TextUtils.isEmpty(inputEmail) && Utility.validate(inputEmail)) {
            // Check if Google Play Service is installed in Device
            // Play services is needed to handle GCM stuffs

            if (inputPassword.length() > 3) {

                if (checkPlayServices()) {
                    // Register Device in GCM Server
                    registerInBackground(inputEmail, inputPassword);
                }
            }
            else {
                Toast.makeText(context, getString(R.string.enter_valid_password), Toast.LENGTH_LONG).show();
                prgDialog.hide();
                if (prgDialog != null) {
                    prgDialog.dismiss();
                }
            }
        }
        // When Email is invalid
        else {
            Toast.makeText(context, getString(R.string.enter_valid_email), Toast.LENGTH_LONG).show();
            prgDialog.hide();
            if (prgDialog != null) {
                prgDialog.dismiss();
            }
        }*/
    }

    private boolean checkEmailPassword() {
        if (!TextUtils.isEmpty(inputEmail) && Utility.validate(inputEmail) && inputPassword.length() > 3) {
            return true;
        }

        return  false;
    }

    // Store  RegId and Email entered by User in SharedPref
    private void storeRegIdinSharedPref(String regId,
                                        String emailId, String passwordId) {
        SharedPreferences prefs = getSharedPreferences("UserDetails", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(REG_ID, regId);
        editor.putString(EMAIL_ID, emailId);
        editor.putString(PASSWORD_ID, passwordId);
        editor.commit();
        storeRegIdinServer(regId, emailId, passwordId);
    }

    // Delete RegId and Email entered by User in SharedPref
    private void deleteRegIdinSharedPref(String deleteRegId, String deleteEmailId) {
        SharedPreferences prefs = getSharedPreferences("UserDetails", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.remove(REG_ID);
        editor.remove(EMAIL_ID);
        editor.remove(PASSWORD_ID);
        editor.commit();
        deleteRegIdinServer(deleteRegId, deleteEmailId);
    }

    // Share RegID with GCM Server Application (Php)
    private void storeRegIdinServer(String regId2, final String emailId, String passwordId) {
        params.put("regId", regId);
        params.put("emailId", emailId);
        params.put("passwordId", passwordId);

        Log.i(TAG, "REG - ID: " + regId + " ");

        // Make RESTful webservice call using AsyncHttpClient object
        AsyncHttpClient client = new AsyncHttpClient();
        client.post(ApplicationConstants.APP_SERVER_URL_INSERT_USER, params,
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
                        i.putExtra("regId", regId);

                        loggedInEmail.setText(emailId);
                        loggedInText.setVisibility(View.VISIBLE);
                        loggedInButton.setVisibility(View.INVISIBLE);
                        storedEmailId = emailId;

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

    // Delete RegID in GCM Server Application (Php)
    private void deleteRegIdinServer(final String deleteRegId, String deleteEmailId) {
        params.put("regId", deleteRegId);
        params.put("emailId", deleteEmailId);

        Log.i(TAG, "DeleteRegIdinServer REG - ID: " + deleteRegId + " - EMAIL - ID: " + deleteEmailId);

        // Make RESTful webservice call using AsyncHttpClient object
        AsyncHttpClient client = new AsyncHttpClient();
        client.post(ApplicationConstants.APP_SERVER_URL_DELETE_USER, params,
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
                                "Reg Id deleted successfully with Web App ",
                                Toast.LENGTH_LONG).show();
                        Intent i = new Intent(context,
                                MainActivity.class);
                        i.putExtra("regId", deleteRegId);

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

    // New message is received
    public static void getNewMessage() {
        Toast.makeText(context, context.getString(R.string.new_json_string), Toast.LENGTH_LONG).show();
        Log.i(TAG, "getNewMessage --> show toast; " + context.getString(R.string.new_json_string));
    }

    public void buttonLogin(View view) {
        login();
    }
}

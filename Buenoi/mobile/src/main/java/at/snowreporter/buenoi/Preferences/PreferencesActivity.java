package at.snowreporter.buenoi.Preferences;

import android.app.ProgressDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;

import at.snowreporter.buenoi.MainActivity;
import at.snowreporter.buenoi.R;

public class PreferencesActivity extends AppCompatActivity {

    static final String TAG = "Buenoi";

    CheckBox checkBoxInstantBookingArrived;
    CheckBox checkBoxOfferAdopted;
    CheckBox checkBoxOfferRejected;
    CheckBox checkBoxInquiryArrived;
    CheckBox checkBoxOldInboxMessages;
    CheckBox checkBoxFlatRateRequestArrived;
    CheckBox checkBoxContactForm;

    Button buttonSave;

    Boolean instantBookingArrived;
    Boolean offerAdopted;
    Boolean offerRejected;
    Boolean inquiryArrived;
    Boolean oldInboxMessages;
    Boolean flatRateRequestArrived;
    Boolean contactForm;

    private static ProgressDialog preferencePrgDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preferences);

        preferencePrgDialog = new ProgressDialog(this);
        // Set Progress Dialog Text
        preferencePrgDialog.setMessage(getString(R.string.prg_dialog_messagetext));
        // Set Cancelable as False
        preferencePrgDialog.setCancelable(false);

        checkBoxInstantBookingArrived = (CheckBox) findViewById(R.id.checkBox_instant_booking_arrived);
        checkBoxOfferAdopted = (CheckBox) findViewById(R.id.checkBox_offer_adopted);
        checkBoxOfferRejected = (CheckBox) findViewById(R.id.checkBox_offer_rejected);
        checkBoxInquiryArrived = (CheckBox) findViewById(R.id.checkBox_inquiry_arrived);
        checkBoxOldInboxMessages = (CheckBox) findViewById(R.id.checkBox_old_inbox_messages);
        checkBoxFlatRateRequestArrived = (CheckBox) findViewById(R.id.checkBox_flat_rate_request_arrived);
        checkBoxContactForm = (CheckBox) findViewById(R.id.checkBox_contact_form);

        buttonSave = (Button) findViewById(R.id.button_save);

        checkBoxInstantBookingArrived.setChecked(Preferences.storedInstantBookingArrived == 0 ? false : true);
        checkBoxOfferAdopted.setChecked(Preferences.storedOfferAdopted == 0 ? false : true);
        checkBoxOfferRejected.setChecked(Preferences.storedOfferRejected == 0 ? false : true);
        checkBoxInquiryArrived.setChecked(Preferences.storedInquiryArrived == 0 ? false : true);
        checkBoxOldInboxMessages.setChecked(Preferences.storedOldInboxMessages == 0 ? false : true);
        checkBoxFlatRateRequestArrived.setChecked(Preferences.storedFlatRateRequestArrived == 0 ? false : true);
        checkBoxContactForm.setChecked(Preferences.storedContactForm == 0 ? false : true);

        checkBoxInstantBookingArrived.setOnCheckedChangeListener(new MyCheckBoxChangeListener());
        checkBoxOfferAdopted.setOnCheckedChangeListener(new MyCheckBoxChangeListener());
        checkBoxOfferRejected.setOnCheckedChangeListener(new MyCheckBoxChangeListener());
        checkBoxInquiryArrived.setOnCheckedChangeListener(new MyCheckBoxChangeListener());
        checkBoxOldInboxMessages.setOnCheckedChangeListener(new MyCheckBoxChangeListener());
        checkBoxFlatRateRequestArrived.setOnCheckedChangeListener(new MyCheckBoxChangeListener());
        checkBoxContactForm.setOnCheckedChangeListener(new MyCheckBoxChangeListener());

        buttonSave.setEnabled(false);
    }

    public void onClickButtonSave(View view) {
        // Show progress dialog
        preferencePrgDialog.show();

        instantBookingArrived = checkBoxInstantBookingArrived.isChecked();
        offerAdopted = checkBoxOfferAdopted.isChecked();
        offerRejected = checkBoxOfferRejected.isChecked();
        inquiryArrived = checkBoxInquiryArrived.isChecked();
        oldInboxMessages = checkBoxOldInboxMessages.isChecked();
        flatRateRequestArrived = checkBoxFlatRateRequestArrived.isChecked();
        contactForm = checkBoxContactForm.isChecked();

        PreferencesJsonString.setPreferences(instantBookingArrived, offerAdopted, offerRejected,
                inquiryArrived, oldInboxMessages, flatRateRequestArrived, contactForm);

        MainActivity.storePreferencesInSharedPref();

        MainActivity.storePreferencesInServer();

        hidePreferencePrgDialog();

        buttonSave.setEnabled(false);
    }

    class MyCheckBoxChangeListener implements CheckBox.OnCheckedChangeListener
    {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            buttonSave.setEnabled(true);
        }
    }

    public static void hidePreferencePrgDialog() {
        // Hide Progress Dialog
        preferencePrgDialog.hide();
        if (preferencePrgDialog != null) {
            preferencePrgDialog.dismiss();
        }
    }
}

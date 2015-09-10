package at.snowreporter.buenoi.Preferences;

import org.json.JSONException;
import org.json.JSONObject;

import at.snowreporter.buenoi.MainActivity;

/**
 * Created by markusfriedl on 25.08.15.
 */
public class PreferencesJsonString {

    // For internal logging
    static final String TAG = "Buenoi";

    private static JSONObject jsonObj;
    private static JSONObject jsonObjEinstellungen;

    public static void getPreferences(String jsonString) throws JSONException {

        try {
            jsonObj = new JSONObject(jsonString);

            MainActivity.myPreferences.storedBusinessId = jsonObj.names().getString(0);

            jsonObjEinstellungen = jsonObj
                    .getJSONObject(MainActivity.myPreferences.storedBusinessId)
                    .getJSONObject("einstellungen");
            MainActivity.myPreferences.storedInstantBookingArrived = jsonObjEinstellungen
                    .getInt("sofortbuchung_eingelangt");
            MainActivity.myPreferences.storedOfferAdopted = jsonObjEinstellungen
                    .getInt("angebot_angenommen");
            MainActivity.myPreferences.storedOfferRejected = jsonObjEinstellungen
                    .getInt("angebot_abgelehnt");
            MainActivity.myPreferences.storedInquiryArrived = jsonObjEinstellungen
                    .getInt("anfrage_eingelangt");
            MainActivity.myPreferences.storedOldInboxMessages = jsonObjEinstellungen
                    .getInt("old_inbox_messages");
            MainActivity.myPreferences.storedFlatRateRequestArrived = jsonObjEinstellungen
                    .getInt("anfrage_pauschale_eingelangt");
            MainActivity.myPreferences.storedContactForm = jsonObjEinstellungen
                    .getInt("kontakt_formular");
        }catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public static void setPreferences(Boolean instantBookingArrived, Boolean offerAdopted,
            Boolean offerRejected, Boolean inquiryArrived, Boolean oldInboxMessages,
            Boolean flatRateRequestArrived, Boolean contactForm) {
        MainActivity.myPreferences.storedInstantBookingArrived = instantBookingArrived == false ? 0 : 1;
        MainActivity.myPreferences.storedOfferAdopted = offerAdopted == false ? 0 : 1;
        MainActivity.myPreferences.storedOfferRejected = offerRejected == false ? 0 : 1;
        MainActivity.myPreferences.storedInquiryArrived = inquiryArrived == false ? 0 : 1;
        MainActivity.myPreferences.storedOldInboxMessages = oldInboxMessages == false ? 0 : 1;
        MainActivity.myPreferences.storedFlatRateRequestArrived = flatRateRequestArrived == false ? 0 : 1;
        MainActivity.myPreferences.storedContactForm = contactForm == false ? 0 : 1;
    }
}

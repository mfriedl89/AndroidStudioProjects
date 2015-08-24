package at.snowreporter.buenoi;

/**
 * Created by markusfriedl on 24.08.15.
 */
public class Preferences {
    // Stores sharedPreferences on phone
    public static final String REG_ID = "regId";
    public static final String USERNAME_ID = "usernameId";
    public static final String PASSWORD_ID = "passwordId";
    public static final String PREF_INSTANT_BOOKING_ARRIVED = "prefInstantBookingArrived";
    public static final String PREF_OFFER_ADOPTED = "prefOfferAdopted";
    public static final String PREF_OFFER_REJECTED = "prefOfferRejected";
    public static final String PREF_INQUIRY_ARRIVED = "prefInquiryArrived";
    public static final String PREF_OLD_INBOX_MESSAGES = "prefOldInboxMessages";
    public static final String PREF_FLAT_RATE_REQUEST_ARRIVED = "prefFlatRateRequestArrived";
    public static final String PREF_CONTACT_FORM = "prefContactForm";

    public static String storedRegistraionId;
    public static String storedUsernameId;
    public static String storedPasswordId;

    public static Boolean storedInstantBookingArrived;
    public static Boolean storedOfferAdopted;
    public static Boolean storedOfferRejected;
    public static Boolean storedInquiryArrived;
    public static Boolean storedOldInboxMessages;
    public static Boolean storedFlatRateRequestArrived;
    public static Boolean storedContactForm;
}

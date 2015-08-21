package at.snowreporter.buenoi;

import java.sql.Time;
import java.util.Date;

/**
 * Created by snowreporter on 22.05.2015.
 */
public class ApplicationConstants {
    // Google Project Number
    static final String SENDER_ID = "620272038749";

    // Buenoi Server login
    static final String BUENOI_USERNAME = "buenoi";
    static final String BUENOI_PASSWORD = "knedlbrot";

    static final String APP_SERVER_USER_LOGIN = "https://test.buenoi.com/appapi/?controller=Session&action=login&benutzername=%s&kennwort=%s&gcm_id=%s";
    static final String APP_SERVER_USER_LOGOUT = "https://test.buenoi.com/appapi/?controller=Session&action=logout";
    static final String APP_SERVER_GET_USER_SETTINGS = "https://test.buenoi.com/appapi/?controller=NachrichtenEinstellungen&action=get";
    static final String APP_SERVER_STATUS = "https://test.buenoi.com/appapi/?controller=Session&action=status";

    // Php Application URL to store Reg ID created
    static final String APP_SERVER_URL_INSERT_USER = "http://151.236.10.250/markusf/insertuser.php"; //"http://151.236.10.250/markusf/gcm/gcm.php?shareRegId=true";

    // Php Application URL to delete Reg ID
    static final String APP_SERVER_URL_DELETE_USER = "http://151.236.10.250/markusf/deleteuser.php";
}

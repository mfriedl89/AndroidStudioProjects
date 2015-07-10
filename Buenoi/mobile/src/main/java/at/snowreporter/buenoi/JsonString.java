package at.snowreporter.buenoi;

import android.util.Log;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by snowreporter on 01.07.2015.
 */
public class JsonString {

    GcmIntentService gcmIntentService = new GcmIntentService();
    String str = gcmIntentService.getMessage();

    // For internal logging
    static final String TAG = "Buenoi";

    public String getStringFromJsonString() {
        String name = "";

        if (str != null) {
            if (!str.isEmpty()) {
                try {
                    JSONObject jsonObject = new JSONObject(str);
                    name = jsonObject.getString("greetMsg");
                } catch (JSONException e) {
                    Log.i(TAG, "JSONException: " + e);
                }

                return name;
            }
        }

        return "no string";
    }
}

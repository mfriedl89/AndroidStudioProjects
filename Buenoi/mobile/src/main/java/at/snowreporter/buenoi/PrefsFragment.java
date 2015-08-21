package at.snowreporter.buenoi;

import android.os.Bundle;
import android.preference.PreferenceFragment;

/**
 * Created by snowreporter on 21.08.2015.
 */
public class PrefsFragment extends PreferenceFragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
    }
}

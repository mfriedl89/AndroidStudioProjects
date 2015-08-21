package at.snowreporter.buenoi;

import android.app.Activity;
import android.os.Bundle;
import android.os.PersistableBundle;

/**
 * Created by snowreporter on 21.08.2015.
 */
public class SetPreferenceActivity extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState, PersistableBundle persistentState) {
        super.onCreate(savedInstanceState, persistentState);

        getFragmentManager().beginTransaction().replace(R.id.contentPanel, new PrefsFragment()).commit();
    }
}

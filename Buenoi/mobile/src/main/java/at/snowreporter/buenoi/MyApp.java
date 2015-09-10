package at.snowreporter.buenoi;

import android.app.Activity;
import android.app.Application;
import android.content.Context;

/**
 * Created by markusfriedl on 10.09.15.
 */
public class MyApp extends Application {
    private static MyApp instance;

    public static MyApp getInstance() {
        return instance;
    }

    public static Context getContext() {
        return instance.getApplicationContext();
    }

    public static Activity getActivity() {
        return instance.getActivity();
    }

    @Override
    public void onCreate() {
        instance = this;
        super.onCreate();
    }
}

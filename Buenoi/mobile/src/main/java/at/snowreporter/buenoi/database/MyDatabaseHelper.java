package at.snowreporter.buenoi.database;

import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import at.snowreporter.buenoi.MyApp;

/**
 * Created by snowreporter on 27.07.2015.
 */

public class MyDatabaseHelper extends SQLiteOpenHelper {
    public static final String DATABASE_NAME = "buenoi_database.db";
    public static final int DATABASE_VERSION = 1;

    public MyDatabaseHelper() {
        super(MyApp.getContext(), DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_TABLE_MESSAGE = "CREATE TABLE " + Message.TABLE_MESSAGES + " ("
                + Message.COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + Message.COL_DATE + " DATE NOT NULL,"
                + Message.COL_TIME + " TIME NOT NULL,"
                + Message.COL_TYPE + " TEXT NOT NULL,"
                + Message.COL_COMMENT + " TEXT,"
                + Message.COL_READ + " INTEGER NOT NULL default 0"
                + ");";

        db.execSQL(CREATE_TABLE_MESSAGE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + Message.TABLE_MESSAGES + ";");
        onCreate(db);
    }
}

package at.snowreporter.buenoi.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by snowreporter on 27.07.2015.
 */

// TODO: EXAMPLE http://blog.chrisblunt.com/android-getting-started-with-databases-and-contentproviders-part-1/
// TODO: EXAMPLE 2 http://instinctcoder.com/android-studio-sqlite-database-example/

public class MyDatabaseHelper extends SQLiteOpenHelper {
    public static final String DATABASE_NAME = "buenoi_database.db";
    public static final int DATABASE_VERSION = 1;

    public MyDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_TABLE_MESSAGE = "CREATE TABLE " + Message.TABLE_MESSAGES + " ("
                + Message.COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + Message.COL_DATE + " DATE NOT NULL,"
                + Message.COL_TIME + " TIME NOT NULL,"
                + Message.COL_TYPE + " TEXT NOT NULL,"
                + Message.COL_COMMENT + " TEXT"
                + ");";

        db.execSQL(CREATE_TABLE_MESSAGE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + Message.TABLE_MESSAGES + ";");
        onCreate(db);
    }
}

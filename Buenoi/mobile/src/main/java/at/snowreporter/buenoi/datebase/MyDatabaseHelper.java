package at.snowreporter.buenoi.datebase;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by snowreporter on 27.07.2015.
 */

// TODO: EXAMPLE http://blog.chrisblunt.com/android-getting-started-with-databases-and-contentproviders-part-1/

public class MyDatabaseHelper extends SQLiteOpenHelper {
    // Database constants
    public static final String DATABASE_NAME = "buenoi_database.db";
    public static final String TABLE_MESSAGES = "messages";
    public static final int DATABASE_VERSION = 1;
    public static final String COL_ID = "_id";
    public static final String COL_DATE = "date";
    public static final String COL_TIME = "time";
    public static final String COL_TYPE = "type";
    public static final String COL_COMMENT = "comment";

    public MyDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE_MESSAGES + " ("
                + COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COL_DATE + " DATE NOT NULL,"
                + COL_TIME + " TIME NOT NULL,"
                + COL_TYPE + " TEXT NOT NULL,"
                + COL_COMMENT + " TEXT"
                + ");");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_MESSAGES + ";");
        onCreate(db);
    }

    public long insert(String tableName, ContentValues values) {
        return getWritableDatabase().insert(tableName, null, values);
    }

    public int update(String tableName, long id, ContentValues values) {
        String selection = COL_ID + " = ?";
        String[] selectionArgs = {String.valueOf(id)};
        return getWritableDatabase().update(tableName, values, selection, selectionArgs);
    }

    public int delete(String tableName, long id) {
        String selection = COL_ID + " = ?";
        String[] selectionArgs = {String.valueOf(id)};
        return getWritableDatabase().delete(tableName, selection, selectionArgs);
    }
}

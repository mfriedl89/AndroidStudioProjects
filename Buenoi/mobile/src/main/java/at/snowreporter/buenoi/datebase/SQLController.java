package at.snowreporter.buenoi.datebase;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import java.sql.SQLException;

/**
 * Created by markusfriedl on 29.07.15.
 */
public class SQLController {
    private MyDatabaseHelper myDatabaseHelper;
    private Context context;
    private SQLiteDatabase database;

    public SQLController(Context c) {
        context = c;
    }

    public SQLController open() throws SQLException{
        myDatabaseHelper = new MyDatabaseHelper(context);
        database = myDatabaseHelper.getWritableDatabase();
        return this;
    }
}


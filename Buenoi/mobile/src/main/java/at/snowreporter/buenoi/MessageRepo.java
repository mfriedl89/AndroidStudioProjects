package at.snowreporter.buenoi;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import at.snowreporter.buenoi.database.Message;
import at.snowreporter.buenoi.database.MyDatabaseHelper;

/**
 * Created by snowreporter on 31.07.2015.
 */
public class MessageRepo {
    // For internal logging
    static final String TAG = "Buenoi";

    public SQLiteDatabase db;

    public MessageRepo() {
        MainActivity.myDatabaseHelper = new MyDatabaseHelper();
    }

    public long insert(ContentValues values) {
        // Open connection to write data
        db = MainActivity.myDatabaseHelper.getWritableDatabase();

        Log.i(TAG, "db: " + db);

        // Inserting row
        long message_Id = db.insert(Message.TABLE_MESSAGES, null, values);
        db.close();

        return (int) message_Id;
    }

    public void delete(int message_Id) {
        db = MainActivity.myDatabaseHelper.getReadableDatabase();
        db.delete(Message.TABLE_MESSAGES, Message.COL_ID + "= ?", new String[]{String.valueOf(message_Id)});
        db.close();
    }

    public List<Message> getMessageList() {
        db = MainActivity.myDatabaseHelper.getWritableDatabase();
        String selectQuery = "SELECT " +
                Message.COL_ID + "," +
                Message.COL_DATE + "," +
                Message.COL_TIME + "," +
                Message.COL_TYPE + "," +
                Message.COL_COMMENT + "," +
                Message.COL_READ +
                " FROM " + Message.TABLE_MESSAGES +
                " ORDER BY " + Message.COL_ID + " DESC";

        List<Message> messageList = new ArrayList<Message>();

        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                Message message = new Message();
                message.message_ID = cursor.getInt(cursor.getColumnIndex(Message.COL_ID));
                message.date = cursor.getString(cursor.getColumnIndex(Message.COL_DATE));
                message.time = cursor.getString(cursor.getColumnIndex(Message.COL_TIME));
                message.type = cursor.getString(cursor.getColumnIndex(Message.COL_TYPE));
                message.comment = cursor.getString(cursor.getColumnIndex(Message.COL_COMMENT));
                message.read = cursor.getInt(cursor.getColumnIndex(Message.COL_READ));

                messageList.add(message);
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return messageList;
    }

    public Message getMessageById(Integer Id) {
        db = MainActivity.myDatabaseHelper.getWritableDatabase();

        String selectQuery = "SELECT " +
                Message.COL_ID + "," +
                Message.COL_DATE + "," +
                Message.COL_TIME + "," +
                Message.COL_TYPE + "," +
                Message.COL_COMMENT + "," +
                Message.COL_READ +
                " FROM " + Message.TABLE_MESSAGES +
                " WHERE " +
                Message.COL_ID + "=?";

        Message message = new Message();

        Cursor cursor = db.rawQuery(selectQuery, new String[]{String.valueOf(Id)});

        if (cursor.moveToFirst()) {
            do {
                message.message_ID = cursor.getInt(cursor.getColumnIndex(Message.COL_ID));
                message.date = cursor.getString(cursor.getColumnIndex(Message.COL_DATE));
                message.time = cursor.getString(cursor.getColumnIndex(Message.COL_TIME));
                message.type = cursor.getString(cursor.getColumnIndex(Message.COL_TYPE));
                message.comment = cursor.getString(cursor.getColumnIndex(Message.COL_COMMENT));
                message.read = cursor.getInt(cursor.getColumnIndex(Message.COL_READ));
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return message;
    }

    public Integer getRowNumbers() {
        db = MainActivity.myDatabaseHelper.getWritableDatabase();

        String countQuery = "SELECT Count(*) FROM " +
                Message.TABLE_MESSAGES;

        Integer cnt = (int) DatabaseUtils.longForQuery(db, countQuery, null);
        db.close();

        return cnt;
    }

    public void updateRowMarkAsRead(Integer Id) {
        db = MainActivity.myDatabaseHelper.getWritableDatabase();

        String updateRow = "UPDATE " +
                Message.TABLE_MESSAGES +
                " SET " + Message.COL_READ +
                " = 1 WHERE " +
                Message.COL_ID  + "=?";

        db.execSQL(updateRow, new String[]{String.valueOf(Id)});
        db.close();
    }
}

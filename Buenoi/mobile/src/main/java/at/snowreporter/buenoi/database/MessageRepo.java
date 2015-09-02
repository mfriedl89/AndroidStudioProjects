package at.snowreporter.buenoi.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

/**
 * Created by snowreporter on 31.07.2015.
 */
public class MessageRepo {
    // For internal logging
    static final String TAG = "Buenoi";

    public MyDatabaseHelper myDatabaseHelper;
    public SQLiteDatabase db;

    public MessageRepo(Context context) {
        myDatabaseHelper = new MyDatabaseHelper(context);
    }

    public long insert(Message message) {
        // Open connection to write data
        db = myDatabaseHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(Message.COL_DATE, message.date);
        values.put(Message.COL_TIME, message.time);
        values.put(Message.COL_TYPE, message.type);
        values.put(Message.COL_COMMENT, message.comment);

        // Inserting row
        long message_Id = db.insert(Message.TABLE_MESSAGES, null, values);
        db.close();

        return (int) message_Id;
    }

    public void delete(int message_Id) {
        db = myDatabaseHelper.getReadableDatabase();
        db.delete(Message.TABLE_MESSAGES, Message.COL_ID + "= ?", new String[]{String.valueOf(Message.COL_ID)});
        db.close();
    }

    public List<Message> getMessageList() {
        db = myDatabaseHelper.getWritableDatabase();
        String selectQuery = "SELECT " +
                Message.COL_ID + "," +
                Message.COL_DATE + "," +
                Message.COL_TIME + "," +
                Message.COL_TYPE + "," +
                Message.COL_COMMENT +
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

                messageList.add(message);
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return messageList;
    }

    public Message getMessageById(Integer Id) {
        db = myDatabaseHelper.getWritableDatabase();

        //Integer calcId = db.

        String selectQuery = "SELECT " +
                Message.COL_ID + "," +
                Message.COL_DATE + "," +
                Message.COL_TIME + "," +
                Message.COL_TYPE + "," +
                Message.COL_COMMENT +
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
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return message;
    }

    public Integer getRowNumbers() {
        db = myDatabaseHelper.getWritableDatabase();

        String countQuery = "SELECT Count(*) FROM " +
                Message.TABLE_MESSAGES;

        Integer cnt = (int) DatabaseUtils.longForQuery(db, countQuery, null);
        db.close();

        Log.i(TAG, "cnt: " + cnt);


        return cnt;
    }
}

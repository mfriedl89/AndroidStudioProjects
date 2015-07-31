package at.snowreporter.buenoi.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by snowreporter on 31.07.2015.
 */
public class MessageRepo {
    private MyDatabaseHelper myDatabaseHelper;

    public MessageRepo(Context context) {
        myDatabaseHelper = new MyDatabaseHelper(context);
    }

    public long insert(Message message) {
        // Open connection to write data
        SQLiteDatabase db = myDatabaseHelper.getWritableDatabase();
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

    public void update(Message message) {
        SQLiteDatabase db = myDatabaseHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(Message.COL_DATE, message.date);
        values.put(Message.COL_TIME, message.time);
        values.put(Message.COL_TYPE, message.type);
        values.put(Message.COL_COMMENT, message.comment);

        db.update(Message.TABLE_MESSAGES, values, Message.COL_ID + "= ?", new String[]{String.valueOf(Message.COL_ID)});
        db.close();
    }

    public void delete(int message_Id) {
        SQLiteDatabase db = myDatabaseHelper.getReadableDatabase();
        db.delete(Message.TABLE_MESSAGES, Message.COL_ID + "= ?", new String[]{String.valueOf(Message.COL_ID)});
        db.close();
    }

    public ArrayList<HashMap<String, String>> getMessageList() {
        SQLiteDatabase db = myDatabaseHelper.getWritableDatabase();
        String selectQuery = "SELECT " +
                Message.COL_ID + "," +
                Message.COL_DATE + "," +
                Message.COL_TIME + "," +
                Message.COL_TYPE + "," +
                Message.COL_COMMENT +
                " FROM " + Message.TABLE_MESSAGES;

        ArrayList<HashMap<String, String>> messageList = new ArrayList<HashMap<String, String>>();

        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                HashMap<String, String> message = new HashMap<String, String>();
                message.put("id", cursor.getString(cursor.getColumnIndex(Message.COL_ID)));
                message.put("date", cursor.getString(cursor.getColumnIndex(Message.COL_DATE)));
                message.put("time", cursor.getString(cursor.getColumnIndex(Message.COL_TIME)));
                message.put("type", cursor.getString(cursor.getColumnIndex(Message.COL_TYPE)));
                message.put("comment", cursor.getString(cursor.getColumnIndex(Message.COL_COMMENT)));
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return messageList;
    }

    public Message getMessageById(int Id) {
        SQLiteDatabase db = myDatabaseHelper.getWritableDatabase();
        String selectQuery = "SELECT " +
                Message.COL_ID + "," +
                Message.COL_DATE + "," +
                Message.COL_TIME + "," +
                Message.COL_TYPE + "," +
                Message.COL_COMMENT +
                " FROM " + Message.TABLE_MESSAGES +
                " WHERE " +
                Message.COL_ID + "=?";

        int iCount = 0;
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
}

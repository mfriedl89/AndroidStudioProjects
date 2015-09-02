package at.snowreporter.buenoi.database;

/**
 * Created by snowreporter on 31.07.2015.
 */
public class Message {
    // Table name
    public static final String TABLE_MESSAGES = "messages";

    // Table columns names
    public static final String COL_ID = "_id";
    public static final String COL_DATE = "date";
    public static final String COL_TIME = "time";
    public static final String COL_TYPE = "type";
    public static final String COL_COMMENT = "comment";

    // Property help us to keep data
    public Integer message_ID;
    public String date;
    public String time;
    public String type;
    public String comment;

    public Message() {

    }

    public Message(String dateCon, String timeCon, String typeCon, String commentCon) {
        this.date = dateCon;
        this.time = timeCon;
        this.type = typeCon;
        this.comment = commentCon;
    }
}

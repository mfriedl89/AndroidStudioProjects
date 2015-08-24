package at.snowreporter.buenoi.database;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by snowreporter on 04.08.2015.
 */
public class MessageContent {

    public static List<MessageItem> MESSAGES = new ArrayList<MessageItem>();

    public static Map<String, MessageItem> MESSAGE_MAP = new HashMap<String, MessageItem>();

    // Dummy item
    static {
        addMessageItem(new MessageItem("1","2015-08-04","08:00","1","Test #1"));
        addMessageItem(new MessageItem("2","2015-08-03","10:00","2","Test #2"));

    }

    public static void addMessageItem(MessageItem item) {
        MESSAGES.add(item);
        MESSAGE_MAP.put(item.id, item);
    }

    public static class MessageItem {
        public String id;
        public String date;
        public String time;
        public String type;
        public String comment;

        public MessageItem(String id, String date, String time, String type, String comment) {
            this.id = id;
            this.date = date;
            this.time = time;
            this.type = type;
            this.comment = comment;
        }
    }
}

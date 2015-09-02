package at.snowreporter.buenoi;

import java.text.DateFormat;
import java.text.FieldPosition;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import at.snowreporter.buenoi.database.Message;

/**
 * Created by markusfriedl on 24.08.15.
 */
public class MessageJsonString {
    // Split the json-string from the server and add all infos to a message
    public static Message splitMessage(String messageString) {
        Message message = new Message();

        GregorianCalendar calendar = new GregorianCalendar();
        SimpleDateFormat df = new SimpleDateFormat("dd.MM.yyyy");
        String date = df.format(calendar.getTime());
        df = new SimpleDateFormat("HH:mm:ss");
        String time = df.format(calendar.getTime());

        message.date = date;
        message.time = time;

        Matcher matcher = Pattern.compile("type=(.*?), message=").matcher(messageString);
        while (matcher.find()) {
            message.type = matcher.group(1);
        }

        matcher = Pattern.compile("message=(.*?), android").matcher(messageString);
        while (matcher.find()) {
            message.comment = matcher.group(1);
        }

        return message;
    }
}

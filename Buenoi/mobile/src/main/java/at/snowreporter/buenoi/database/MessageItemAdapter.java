package at.snowreporter.buenoi.database;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

import at.snowreporter.buenoi.R;

/**
 * Created by markusfriedl on 27.08.15.
 */
public class MessageItemAdapter extends ArrayAdapter<Message> {
    private Context context;
    private int layoutResourceId;
    private List<Message> objects = null;

    public MessageItemAdapter(Context context, int resource, List<Message> objects) {
        super(context, resource, objects);
        this.context = context;
        this.layoutResourceId = resource;
        this.objects = objects;
    }

    public View getView(int positon, View convertView, ViewGroup parent) {
        View v = convertView;
        if (v == null) {
            LayoutInflater inflater = ((Activity) context).getLayoutInflater();
            v = inflater.inflate(layoutResourceId, parent, false);
        }

        Message message = objects.get(positon);
        TextView dateText = (TextView) v.findViewById(R.id.textViewMessageDate);
        dateText.setText(message.date);
        TextView timeText = (TextView) v.findViewById(R.id.textViewMessageTime);
        timeText.setText(message.time);
        TextView typeText = (TextView) v.findViewById(R.id.textViewMessageType);
        typeText.setText(message.type);

        return v;
    }
}

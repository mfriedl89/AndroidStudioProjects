package at.snowreporter.buenoi.database;

import android.app.Activity;
import android.content.Context;
import android.graphics.Typeface;
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
        View view = convertView;
        String modTypeText = "";

        if (view == null) {
            LayoutInflater inflater = ((Activity) context).getLayoutInflater();
            view = inflater.inflate(layoutResourceId, parent, false);
        }

        Message message = objects.get(positon);

        TextView dateText = (TextView) view.findViewById(R.id.textViewMessageDate);
        dateText.setText(message.date);

        TextView timeText = (TextView) view.findViewById(R.id.textViewMessageTime);
        timeText.setText(message.time);

        TextView typeText = (TextView) view.findViewById(R.id.textViewMessageType);
        switch (message.type) {
            case "sofortbuchung_eingelangt":
                modTypeText = context.getString(R.string.instant_booking_arrived);
                break;
            case "angebot_angenommen":
                modTypeText = context.getString(R.string.offer_adopted);
                break;
            case "angebot_abgelehnt":
                modTypeText = context.getString(R.string.offer_rejected);
                break;
            case "anfrage_eingelangt":
                modTypeText = context.getString(R.string.inquiry_arrived);
                break;
            case "old_inbox_messages":
                modTypeText = context.getString(R.string.old_inbox_messages);
                break;
            case "anfrage_pauschale_eingelangt":
                modTypeText = context.getString(R.string.flat_rate_request_arrived);
                break;
            case "kontakt_formular":
                modTypeText = context.getString(R.string.contact_form);
                break;
            default:
                modTypeText = context.getString(R.string.undefine_type);
                break;
        }
        typeText.setText(modTypeText);

        if (message.read == 0) {
            dateText.setTypeface(Typeface.DEFAULT_BOLD);
            timeText.setTypeface(Typeface.DEFAULT_BOLD);
            typeText.setTypeface(Typeface.DEFAULT_BOLD);
        }
        else {
            dateText.setTypeface(Typeface.DEFAULT);
            timeText.setTypeface(Typeface.DEFAULT);
            typeText.setTypeface(Typeface.DEFAULT);
        }

        return view;
    }
}

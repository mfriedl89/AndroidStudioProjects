package at.snowreporter.buenoi.MessageList;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import at.snowreporter.buenoi.MainActivity;
import at.snowreporter.buenoi.R;
import at.snowreporter.buenoi.database.Message;

/**
 * A fragment representing a single Message detail screen.
 * This fragment is either contained in a {@link MessageListActivity}
 * in two-pane mode (on tablets) or a {@link MessageDetailActivity}
 * on handsets.
 */
public class MessageDetailFragment extends Fragment {

    // For internal logging
    static final String TAG = "Buenoi";

    /**
     * The fragment argument representing the item ID that this fragment
     * represents.
     */
    public static final String ARG_ITEM_ID = "COL_ID";

    /**
     * The dummy content this fragment is presenting.
     */

    private Message message = new Message();

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public MessageDetailFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments().containsKey(ARG_ITEM_ID)) {
            // Show selected Message in Detail View
            message = MainActivity.myMessageRepo.getMessageById(MessageListActivity.messageId);
            MainActivity.myMessageRepo.updateRowMarkAsRead(MessageListActivity.messageId);
            MessageListActivity.refreshListView();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_message_detail, container, false);
        String modTypeText = "";

        // Show the dummy content as text in a TextView.
        if (message != null) {
            ((TextView) rootView.findViewById(R.id.textViewDate)).setText(message.date);
            ((TextView) rootView.findViewById(R.id.textViewTime)).setText(message.time);
            modTypeText = MainActivity.modifiedTypeText(message.type);
            ((TextView) rootView.findViewById(R.id.textViewType)).setText(modTypeText);
            ((TextView) rootView.findViewById(R.id.textViewComment)).setText(message.comment);
        }

        return rootView;
    }
}

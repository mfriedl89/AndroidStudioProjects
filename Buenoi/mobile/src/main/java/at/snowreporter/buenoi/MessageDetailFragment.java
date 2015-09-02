package at.snowreporter.buenoi;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


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
            Integer diffItemId = MainActivity.myMessageRepo.getRowNumbers() -
                    Integer.parseInt(getArguments().getString(ARG_ITEM_ID)) + 1; // two times '+1' because id in table starts at 1

            Log.i(TAG, "diffItemId: " + diffItemId);


            // Show selected Message in Detail View
            message = MainActivity.myMessageRepo.getMessageById(diffItemId);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_message_detail, container, false);

        // Show the dummy content as text in a TextView.
        if (message != null) {
            ((TextView) rootView.findViewById(R.id.textViewDate)).setText(message.date);
            ((TextView) rootView.findViewById(R.id.textViewTime)).setText(message.time);
            ((TextView) rootView.findViewById(R.id.textViewType)).setText(message.type);
            ((TextView) rootView.findViewById(R.id.textViewComment)).setText(message.comment);
        }

        return rootView;
    }
}

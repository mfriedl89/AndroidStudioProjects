package at.snowreporter.buenoi;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;


import java.io.StringReader;

import at.snowreporter.buenoi.database.Message;
import at.snowreporter.buenoi.database.MessageRepo;
import at.snowreporter.buenoi.dummy.DummyContent;

/**
 * A fragment representing a single Message detail screen.
 * This fragment is either contained in a {@link MessageListActivity}
 * in two-pane mode (on tablets) or a {@link MessageDetailActivity}
 * on handsets.
 */
public class MessageDetailFragment extends Fragment {
    /**
     * The fragment argument representing the item ID that this fragment
     * represents.
     */
    public static final String ARG_ITEM_ID = "item_id";

    /**
     * The dummy content this fragment is presenting.
     */
    private DummyContent.DummyItem mItem;

    private int _Message_Id = 0;
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

        /*_Message_Id = 0;
        Intent intent = Intent.getIntent();
        _Message_Id = intent.getIntExtra("message_id", 0);
        MessageRepo repo = new MessageRepo(this);
        message = repo.getMessageById(_Message_Id);*/
        
        if (getArguments().containsKey(ARG_ITEM_ID)) {
            // Load the dummy content specified by the fragment
            // arguments. In a real-world scenario, use a Loader
            // to load content from a content provider.
            mItem = DummyContent.ITEM_MAP.get(getArguments().getString(ARG_ITEM_ID));
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_message_detail, container, false);

        // Show the dummy content as text in a TextView.
        if (mItem != null) {
            // TODO: Uncomment
            //((TextView) rootView.findViewById(R.id.message_detail)).setText(mItem.content);
            ((TextView) rootView.findViewById(R.id.textViewDate)).setText(mItem.content);//message.date);
            ((TextView) rootView.findViewById(R.id.textViewTime)).setText(mItem.content);//message.time);
            ((TextView) rootView.findViewById(R.id.textViewType)).setText(mItem.content);//message.type);
            ((TextView) rootView.findViewById(R.id.textViewComment)).setText(mItem.content);//message.comment);
        }

        return rootView;
    }
}

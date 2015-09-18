package at.snowreporter.buenoi.MessageList;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Toast;

import com.baoyz.swipemenulistview.SwipeMenu;
import com.baoyz.swipemenulistview.SwipeMenuCreator;
import com.baoyz.swipemenulistview.SwipeMenuItem;
import com.baoyz.swipemenulistview.SwipeMenuListView;

import java.util.List;

import at.snowreporter.buenoi.MainActivity;
import at.snowreporter.buenoi.R;
import at.snowreporter.buenoi.database.Message;
import at.snowreporter.buenoi.database.MessageItemAdapter;


/**
 * An activity representing a list of Messages. This activity
 * has different presentations for handset and tablet-size devices. On
 * handsets, the activity presents a list of items, which when touched,
 * lead to a {@link MessageDetailActivity} representing
 * item details. On tablets, the activity presents the list of items and
 * item details side-by-side using two vertical panes.
 * <p/>
 * The activity makes heavy use of fragments. The list of items is a
 * {@link MessageListFragment} and the item details
 * (if present) is a {@link MessageDetailFragment}.
 * <p/>
 * This activity also implements the required
 * {@link MessageListFragment.Callbacks} interface
 * to listen for item selections.
 */
public class MessageListActivity extends AppCompatActivity
        implements MessageListFragment.Callbacks {

    // For internal logging
    static final String TAG = "Buenoi";

    /**
     * Whether or not the activity is in two-pane mode, i.e. running on a tablet
     * device.
     */
    private boolean mTwoPane;

    // SwipeMenue
    public static SwipeMenuListView menuListView;
    public static MessageItemAdapter myMessageItemAdapter;
    public static List<Message> myMessageList;

    public static Integer messageId = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message_list);

        MainActivity.hidePrgDialog();

        MainActivity.activity = this;

        MainActivity.messagePrgDialog = new ProgressDialog(this);
        MainActivity.messagePrgDialog.setMessage(getString(R.string.message_prg_dialog_messagetext));
        MainActivity.messagePrgDialog.setCancelable(false);

        MainActivity.logoutPrgDialog = new ProgressDialog(this);
        MainActivity.logoutPrgDialog.setMessage(getString(R.string.logout_prg_dialog_messagetext));
        MainActivity.logoutPrgDialog.setCancelable(false);

        // SwipeMenu
        menuListView = (SwipeMenuListView) findViewById(R.id.swipe_message_list);
        myMessageList = MainActivity.myMessageRepo.getMessageList();
        myMessageItemAdapter = new MessageItemAdapter(getApplicationContext(), R.layout.message_layout, myMessageList);
        menuListView.setAdapter(myMessageItemAdapter);
        menuListView.setSwipeDirection(SwipeMenuListView.DIRECTION_LEFT);

        menuListView.setEmptyView(findViewById(R.id.textViewNoMessage));

        SwipeMenuCreator creator = new SwipeMenuCreator() {

            @Override
            public void create(SwipeMenu menu) {
                // create "delete" item
                SwipeMenuItem deleteItem = new SwipeMenuItem(getApplicationContext());
                // set item background
                deleteItem.setBackground(new ColorDrawable(Color.rgb(0xF9,
                        0x3F, 0x25)));
                // set item width
                deleteItem.setWidth(dp2px(90));
                // set a icon
                deleteItem.setIcon(R.drawable.ic_delete_white_48dp);
                // add to menu
                menu.addMenuItem(deleteItem);
            }
        };

        // set creator
        MessageListActivity.menuListView.setMenuCreator(creator);

        // step 2. listener item click event
        MessageListActivity.menuListView.setOnMenuItemClickListener(new SwipeMenuListView.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(int position, SwipeMenu menu, int index) {
                switch (index) {
                    case 0:
                        messageId = myMessageItemAdapter.getItem(position).message_ID;
                        myMessageItemAdapter.remove(myMessageItemAdapter.getItem(position));
                        myMessageItemAdapter.notifyDataSetChanged();
                        MainActivity.myMessageRepo.delete(messageId);
                        break;
                }
                return false;
            }
        });

        // set SwipeListener
        MessageListActivity.menuListView.setOnSwipeListener(new SwipeMenuListView.OnSwipeListener() {

            @Override
            public void onSwipeStart(int position) {
                // swipe start
            }

            @Override
            public void onSwipeEnd(int position) {
                // swipe end
            }
        });

        // set MenuStateChangeListener
        MessageListActivity.menuListView.setOnMenuStateChangeListener(new SwipeMenuListView.OnMenuStateChangeListener() {
            @Override
            public void onMenuOpen(int position) {
            }

            @Override
            public void onMenuClose(int position) {
            }
        });

        // other setting
//		listView.setCloseInterpolator(new BounceInterpolator());

        // test item long click
        MessageListActivity.menuListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {

            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view,
                                           int position, long id) {
                Toast.makeText(getApplicationContext(), position + " long click", Toast.LENGTH_SHORT).show();
                return false;
            }
        });

        menuListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                messageId = myMessageItemAdapter.getItem(position).message_ID;

                Log.i(TAG, "setOnItemClickListener, position: " + position + ", id: " + id + ", messageId: " + messageId);

                onItemSelected(MainActivity.myMessageRepo.getMessageById(messageId).message_ID.toString());
            }
        });

        if (findViewById(R.id.message_detail_container) != null) {
            // The detail container view will be present only in the
            // large-screen layouts (res/values-large and
            // res/values-sw600dp). If this view is present, then the
            // activity should be in two-pane mode.
            mTwoPane = true;

            // In two-pane mode, list items should be given the
            // 'activated' state when touched.
            ((MessageListFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.message_list))
                    .setActivateOnItemClick(true);
        }
    }

    private int dp2px(int dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp,
                getResources().getDisplayMetrics());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);

        MenuItem itemSettings = menu.findItem(R.id.action_settings);
        itemSettings.setEnabled(true);
        // set the text for the login/logout-item
        MenuItem item = menu.findItem(R.id.action_login_logout);
        item.setTitle(R.string.logout);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem itemLoginLogout = menu.findItem(R.id.action_login_logout);
        itemLoginLogout.setTitle(R.string.logout);

        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            MainActivity.messagePrgDialog.show();
            MainActivity.showSettings();

            return true;
        }
        else if (id == R.id.action_info) {
            MainActivity.showInfo();

            return true;
        }
        else if (id == R.id.action_login_logout) {
            MainActivity.logoutPrgDialog.show();
            MainActivity.activity = this;
            MainActivity.logout();

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Callback method from {@link MessageListFragment.Callbacks}
     * indicating that the item with the given ID was selected.
     */
    @Override
    public void onItemSelected(String id) {

        Log.i(TAG, "onItemSelected");

        if (mTwoPane) {
            // In two-pane mode, show the detail view in this activity by
            // adding or replacing the detail fragment using a
            // fragment transaction.
            Bundle arguments = new Bundle();
            arguments.putString(MessageDetailFragment.ARG_ITEM_ID, id);
            MessageDetailFragment fragment = new MessageDetailFragment();
            fragment.setArguments(arguments);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.message_detail_container, fragment)
                    .commit();

        } else {
            // In single-pane mode, simply start the detail activity
            // for the selected item ID.
            Intent detailIntent = new Intent(this, MessageDetailActivity.class);
            detailIntent.putExtra(MessageDetailFragment.ARG_ITEM_ID, id);
            startActivity(detailIntent);
        }
    }

    public static void refreshListView() {
        myMessageList = MainActivity.myMessageRepo.getMessageList();
        try {
            myMessageItemAdapter.clear();
            myMessageItemAdapter.addAll(myMessageList);
            myMessageItemAdapter.notifyDataSetChanged();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

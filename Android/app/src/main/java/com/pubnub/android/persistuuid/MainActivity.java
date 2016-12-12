package com.pubnub.android.persistuuid;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.pubnub.api.PNConfiguration;
import com.pubnub.api.PubNub;
import com.pubnub.api.callbacks.PNCallback;
import com.pubnub.api.callbacks.SubscribeCallback;
import com.pubnub.api.enums.PNLogVerbosity;
import com.pubnub.api.models.consumer.PNPublishResult;
import com.pubnub.api.models.consumer.PNStatus;
import com.pubnub.api.models.consumer.pubsub.PNMessageResult;
import com.pubnub.api.models.consumer.pubsub.PNPresenceEventResult;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {
    final static public String TAG = "MainActivity";
    private PubNub pubnub = null;
    private SubscribeCallback subscribeCallback;
    private ArrayAdapter<ChatMessage> messagesListAdapter;
    private List<ChatMessage> messagesListItems = new ArrayList<ChatMessage>();
    private ListView messagesListView;
    private Button sendButton;
    private EditText publishMessage;
    final private String CHANNEL = "UUID-Demo";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "MainActivity.onCreate");

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        messagesListView = (ListView)findViewById(R.id.messagesListView);
        sendButton = (Button)findViewById(R.id.sendButton);
        publishMessage = (EditText)findViewById(R.id.publishMessage);

        initMessagesListView();

    }

    @Override
    protected void onStart() {
        Log.d(TAG, "MainActivity.onStart");
        super.onStart();
    }

    @Override
    public void onResume() {
        Log.d(TAG, "MainActivity.onResume");
        setPubNub();
        super.onResume();
    }

    @Override
    public void onPause() {
        Log.d(TAG, "MainActivity.onPause");
        super.onPause();
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "MainActivity.onDestroy");
        removePubNubListener();
        super.onDestroy();
    }



    private void setPubNub()
    {
        if (pubnub == null) {
            PNConfiguration pnConfiguration = new PNConfiguration();
            pnConfiguration.setSubscribeKey("demo-36");
            pnConfiguration.setPublishKey("demo-36");
            pnConfiguration.setSecure(false);
            pnConfiguration.setLogVerbosity(PNLogVerbosity.BODY);

            pubnub = new PubNub(pnConfiguration);

            checkPubnubUUID();

            addPubNubListener();

            pubnub.subscribe().channels(Arrays.asList("UUID-Demo")).withPresence().execute();

        }

    }

    private void addPubNubListener() {
        subscribeCallback = new SubscribeCallback() {

            // MESSAGES
            @Override
            public void message(PubNub pubnub, PNMessageResult message) {
                Log.v(TAG, message.getMessage().toString());
                ChatMessage chatMessage = new ChatMessage(message);
                addMessage(chatMessage);
            }

            // STATUS EVENTS
            @Override
            public void status(PubNub pubnub, PNStatus status) {
                Log.v(TAG, status.getCategory().toString());

            }

            // PRESENCE EVENTS
            @Override
            public void presence(PubNub pubnub, PNPresenceEventResult presence) {
                Log.d(TAG, "begin presence: " + presence.getEvent() + ", " + presence.getUuid());

            }
        };

        pubnub.addListener(subscribeCallback);
    }

    private void removePubNubListener()
    {
        if (pubnub!=null) {
            pubnub.removeListener(subscribeCallback);
            pubnub.unsubscribe();
        }
    }


    private void checkPubnubUUID() {
        SharedPreferences defaultPrefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        SharedPreferences.Editor editor = defaultPrefs.edit();

        // get the current uuid value (first time, it will be null)
        String uuid = defaultPrefs.getString("uuid", null);

        // if uuid hasn’t been created/ persisted, then create
        // and persist to use for subsequent app loads/connections
        if (uuid == null || uuid.length() == 0) {
            // generate a UUID or use your own custom uuid, if required
            uuid = getResources().getString(R.string.app_name) + "-" + UUID.randomUUID().toString();
            editor.putString("uuid", uuid);
            editor.commit();
        }

        // set the uuid for pnconfig
        pubnub.getConfiguration().setUuid(uuid);

        ((TextView)findViewById(R.id.uuid)).setText(uuid);
    }

    private void initMessagesListView() {

        messagesListAdapter = new ArrayAdapter<ChatMessage>(this,android.R.layout.simple_list_item_2, android.R.id.text1, messagesListItems) {

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                TextView text1 = (TextView) view.findViewById(android.R.id.text1);
                TextView text2 = (TextView) view.findViewById(android.R.id.text2);

                ChatMessage item = messagesListItems.get(position);
                String sender = item.getSender();
                String text = item.getText();

                text1.setText(text);

                // my messages are blue
                if (sender != null && sender.equals(pubnub.getConfiguration().getUuid()))
                    text1.setTextColor(Color.BLUE);
                    // everyone elses' are red
                else
                    text1.setTextColor(Color.RED);

                String when = getFormattedDateTime(new java.util.Date(
                        (long) item.getPublishTT() / 10000));

                text2.setGravity(Gravity.RIGHT);

                if (sender != null) {
                    String who = sender;

                    text2.setText(who + "\n" + when);
                }
                else
                    text2.setText("unknown \n" + when);

                return view;
            }
        };

        messagesListView.setAdapter(messagesListAdapter);
    }



    private void addMessage(final ChatMessage chatMessage) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                messagesListItems.add(chatMessage);
                messagesListAdapter.notifyDataSetChanged();

                // Select the last row so it will scroll into view
                messagesListView.post(new Runnable() {
                    @Override
                    public void run() {
                        messagesListView.setSelection(messagesListAdapter.getCount() - 1);
                    }
                });
            }
        });
    }

    private String getFormattedDateTime(Date date) {
        android.text.format.DateFormat df = new android.text.format.DateFormat();
        return df.format("MM-dd hh:mm:ss", date).toString();
    }

    public void sendMessage(View view) {
        Log.v(TAG, publishMessage.getText().toString());

        pubnub.publish()
                .channel(CHANNEL)
                .message(createMessage(publishMessage.getText().toString()))
                .async(new PNCallback<PNPublishResult>() {
                    @Override
                    public void onResponse(PNPublishResult result, PNStatus status) {
                        Log.v(TAG, "pub timetoken:   " + result.getTimetoken());
                        Log.v(TAG, "pub status code: " + status.getStatusCode());

                        if (!status.isError()) {
                            publishMessage.setText("");
                        }
                    }
                });
    }

    private ObjectNode createMessage(String message) {
        JsonNodeFactory factory = JsonNodeFactory.instance;
        ObjectNode payload = factory.objectNode();
        payload.put("text", message);
        payload.put("sender", pubnub.getConfiguration().getUuid());

        return payload;
    }


}

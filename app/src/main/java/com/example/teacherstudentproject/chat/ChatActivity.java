package com.example.teacherstudentproject.chat;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.example.teacherstudentproject.R;
import com.example.teacherstudentproject.welcome.WelcomeActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChatActivity extends AppCompatActivity implements View.OnClickListener {

    private String user_id, username, currentUserID;
    private Toolbar toolbar;
    private TextView custom_bar_name, custom_bar_last_seen;
    private DatabaseReference rootRef, userDatabase;
    private FirebaseAuth mAuth;

    private EditText et_message;

    private RecyclerView messageList;
    private SwipeRefreshLayout mRefreshLayout;

    private final List<Messages> messagesList = new ArrayList<>();
    private LinearLayoutManager mLinearLayout;
    private MessageAdapter mAdapter;

    private static final int TOTAL_ITEMS_TO_LOAD = 10;
    private int mCurrentPage = 1;
    private int itemPos = 0;

    private String mLastKey = "";
    private String mPrevKey = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        //Initializing Views
        initViews();
    }

    private void initViews() {

        user_id = getIntent().getStringExtra("user_id");
        //username = getIntent().getStringExtra("username");

        //Toolbar
        Toolbar toolbar = findViewById(R.id.chat_appbar);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setDisplayHomeAsUpEnabled(true);

        LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View action_bar_view = inflater.inflate(R.layout.custom_chat_bar, null);
        actionBar.setCustomView(action_bar_view);

        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
        rootRef = FirebaseDatabase.getInstance().getReference();

        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser != null) {

            userDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(mAuth.getCurrentUser().getUid());
        }

        custom_bar_name = findViewById(R.id.custom_bar_name);
        custom_bar_last_seen = findViewById(R.id.custom_bar_last_seen);
        et_message = findViewById(R.id.et_message);

        mAdapter = new MessageAdapter(messagesList);

        messageList = findViewById(R.id.messageList);
        mRefreshLayout = findViewById(R.id.message_swipe_layout);
        mLinearLayout = new LinearLayoutManager(this);
        messageList.setLayoutManager(mLinearLayout);
        messageList.setAdapter(mAdapter);
        loadMessages();

        //custom_bar_name.setText(username);

        rootRef.child("Users").child(user_id).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                String fname = dataSnapshot.child("firstname").getValue(String.class);
                String lname = dataSnapshot.child("lastname").getValue(String.class);

                custom_bar_name.setText(fname + " " + lname);

                String online = dataSnapshot.child("online").getValue().toString();

                if (online.equals("true")) {

                    custom_bar_last_seen.setText("Online");
                } else {
                    long lastTime = Long.parseLong(online);
                    String lastSeenTime = GetTimeAgo.getTimeAgo(lastTime, getApplicationContext());
                    custom_bar_last_seen.setText(lastSeenTime);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        rootRef.child("Chat").child(currentUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if (!dataSnapshot.hasChild(user_id)) {
                    Map chatAddMap = new HashMap();
                    chatAddMap.put("seen", false);
                    chatAddMap.put("timestamp", ServerValue.TIMESTAMP);

                    Map chatUserMap = new HashMap();
                    chatUserMap.put("Chat/" + currentUserID + "/" + user_id, chatAddMap);
                    chatUserMap.put("Chat/" + user_id + "/" + currentUserID, chatAddMap);

                    rootRef.updateChildren(chatUserMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

                            if (databaseError != null) {
                                Log.d("CHAT_LOG", databaseError.getMessage().toString());

                            }
                        }
                    });

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        mRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {

                mCurrentPage++;

                itemPos = 0;

                loadMoreMessages();
            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser == null) {

            Intent intent = new Intent(ChatActivity.this, WelcomeActivity.class);
            startActivity(intent);
            finish();
        } else {
            userDatabase.child("online").setValue("true");
        }
    }

    @Override
    protected void onStop() {
        super.onStop();

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {

            userDatabase.child("online").setValue(ServerValue.TIMESTAMP);
        }
    }

    public void loadMessages() {

        DatabaseReference messageRef = rootRef.child("messages").child(currentUserID).child(user_id);
        Query messageQuery = messageRef.limitToLast(mCurrentPage * TOTAL_ITEMS_TO_LOAD);

        messageQuery.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                Messages message = dataSnapshot.getValue(Messages.class);

                itemPos++;

                if (itemPos == 1) {

                    String messageKey = dataSnapshot.getKey();

                    mLastKey = messageKey;
                    mPrevKey = messageKey;

                }

                messagesList.add(message);
                mAdapter.notifyDataSetChanged();

                messageList.scrollToPosition(messagesList.size() - 1);

                mRefreshLayout.setRefreshing(false);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void loadMoreMessages() {

        DatabaseReference messageRef = rootRef.child("messages").child(currentUserID).child(user_id);

        Query messageQuery = messageRef.orderByKey().endAt(mLastKey).limitToLast(10);

        messageQuery.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {


                Messages message = dataSnapshot.getValue(Messages.class);
                String messageKey = dataSnapshot.getKey();

                if (!mPrevKey.equals(messageKey)) {

                    messagesList.add(itemPos++, message);

                } else {

                    mPrevKey = mLastKey;

                }


                if (itemPos == 1) {

                    mLastKey = messageKey;

                }


                Log.d("TOTALKEYS", "Last Key : " + mLastKey + " | Prev Key : " + mPrevKey + " | Message Key : " + messageKey);

                mAdapter.notifyDataSetChanged();

                mRefreshLayout.setRefreshing(false);

                mLinearLayout.scrollToPositionWithOffset(10, 0);

            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    private void sendMessage() {

        String message = et_message.getText().toString();

        String current_user_ref = "messages/" + currentUserID + "/" + user_id;
        String chat_user_ref = "messages/" + user_id + "/" + currentUserID;

        DatabaseReference user_message_push = rootRef.child("messages")
                .child(currentUserID).child(user_id).push();

        String push_id = user_message_push.getKey();

        Map messageMap = new HashMap();
        messageMap.put("message", message);
        messageMap.put("seen", false);
        messageMap.put("type", "text");
        messageMap.put("time", ServerValue.TIMESTAMP);
        messageMap.put("from", currentUserID);

        Map messageUserMap = new HashMap();
        messageUserMap.put(current_user_ref + "/" + push_id, messageMap);
        messageUserMap.put(chat_user_ref + "/" + push_id, messageMap);

        et_message.setText("");

        rootRef.child("Chat").child(currentUserID).child(user_id).child("seen").setValue(true);
        rootRef.child("Chat").child(currentUserID).child(user_id).child("timestamp").setValue(ServerValue.TIMESTAMP);

        rootRef.child("Chat").child(user_id).child(currentUserID).child("seen").setValue(false);
        rootRef.child("Chat").child(user_id).child(currentUserID).child("timestamp").setValue(ServerValue.TIMESTAMP);

        rootRef.updateChildren(messageUserMap, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

                if (databaseError != null) {

                    Log.d("CHAT_LOG", databaseError.getMessage().toString());

                }

            }
        });

    }

    @Override
    public void onClick(View v) {

        if (v.getId() == R.id.img_send) {
            if (TextUtils.isEmpty(et_message.getText())) {
                et_message.setError("Please enter message");
                et_message.requestFocus();
            } else {

                sendMessage();
            }
        }
    }
}

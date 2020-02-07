package com.example.teacherstudentproject.chat;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.teacherstudentproject.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;


public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder>{


    private List<Messages> mMessageList;
    private DatabaseReference mUserDatabase;

    public MessageAdapter(List<Messages> mMessageList) {

        this.mMessageList = mMessageList;

    }

    @Override
    public MessageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.message_single_layout ,parent, false);

        return new MessageViewHolder(v);

    }

    public class MessageViewHolder extends RecyclerView.ViewHolder {

        public TextView messageText;
        public TextView displayName;

        public MessageViewHolder(View view) {
            super(view);

            messageText = (TextView) view.findViewById(R.id.message_user_text);
            displayName = (TextView) view.findViewById(R.id.message_user_name);

        }
    }

    @Override
    public void onBindViewHolder(final MessageViewHolder viewHolder, int i) {

        Messages c = mMessageList.get(i);

        String from_user = c.getFrom();

        mUserDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(from_user);

        mUserDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                String fname = dataSnapshot.child("firstname").getValue().toString();
                String lname = dataSnapshot.child("lastname").getValue().toString();
                viewHolder.displayName.setText(fname + " " + lname);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        viewHolder.messageText.setText(c.getMessage());
    }

    @Override
    public int getItemCount() {
        return mMessageList.size();
    }

}


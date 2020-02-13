package com.example.teacherstudentproject.chat;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.teacherstudentproject.R;
import com.google.firebase.database.DatabaseReference;

import java.util.List;


public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder>{


    private List<Messages> mMessageList;
    private DatabaseReference mUserDatabase;
    private Context context;

    public MessageAdapter(List<Messages> mMessageList, Context context) {

        this.mMessageList = mMessageList;
        this.context = context;

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
        public RelativeLayout relativeLayout;

        public MessageViewHolder(View view) {
            super(view);

            messageText = (TextView) view.findViewById(R.id.message_user_text);
            relativeLayout = (RelativeLayout) view.findViewById(R.id.relativeLayout);
            //displayName = (TextView) view.findViewById(R.id.message_user_name);

        }
    }

    @Override
    public void onBindViewHolder(final MessageViewHolder viewHolder, int i) {

        Messages c = mMessageList.get(i);

        String from_user = c.getFrom();

        if (ChatActivity.currentUserID.equals(c.getFrom())){

            RelativeLayout.LayoutParams params1 = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            params1.addRule(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.TRUE);

            viewHolder.messageText.setLayoutParams(params1);
            viewHolder.messageText.setText(c.getMessage());
            viewHolder.messageText.setBackgroundResource(R.drawable.tv_bg_gray);
            viewHolder.messageText.setTextColor(Color.BLACK);

        } else {

            RelativeLayout.LayoutParams params1 = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            params1.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, RelativeLayout.TRUE);

            viewHolder.messageText.setLayoutParams(params1);
            viewHolder.messageText.setText(c.getMessage());
            viewHolder.messageText.setBackgroundResource(R.drawable.tv_bg_blue);
            viewHolder.messageText.setTextColor(Color.WHITE);
        }

        /*mUserDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(from_user);

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
        });*/
    }

    @Override
    public int getItemCount() {
        return mMessageList.size();
    }

}


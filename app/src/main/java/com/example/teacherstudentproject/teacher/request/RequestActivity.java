package com.example.teacherstudentproject.teacher.request;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.teacherstudentproject.chat.GetTimeAgo;
import com.example.teacherstudentproject.R;
import com.example.teacherstudentproject.student.ProfileActivity;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class RequestActivity extends AppCompatActivity {

    private DatabaseReference databaseReference, friendReqdataRef;
    private String uid;
    private RecyclerView usersList;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_request);

        usersList = findViewById(R.id.usersList);
        usersList.setLayoutManager(new LinearLayoutManager(getApplicationContext()));

        uid = getIntent().getStringExtra("uid");

        Toolbar toolbar = findViewById(R.id.toolbar_req);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Request");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        databaseReference = FirebaseDatabase.getInstance().getReference().child("Users");
        friendReqdataRef = FirebaseDatabase.getInstance().getReference().child("Friend_req").child(uid);
        friendReqdataRef.keepSynced(true);

        progressDialog = new ProgressDialog(this);
    }

    @Override
    protected void onStart() {
        super.onStart();

        /*progressDialog.setTitle("Loading Data");
        progressDialog.setMessage("Please wait while we are loading users list");
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();*/

        FirebaseRecyclerAdapter<ModelRequest, ViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<ModelRequest, ViewHolder>(
                ModelRequest.class,
                R.layout.item_users,
                ViewHolder.class,
                friendReqdataRef
        ) {
            @Override
            protected void populateViewHolder(final ViewHolder viewHolder, ModelRequest model, int i) {

                final String list_user_id = getRef(i).getKey();
                //Toast.makeText(getApplicationContext(), list_user_id, Toast.LENGTH_LONG).show();
                //progressDialog.dismiss();

                databaseReference.child(list_user_id).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        //progressDialog.dismiss();

                        final String id = dataSnapshot.getKey();
                        String firstname = dataSnapshot.child("firstname").getValue().toString();
                        String lastname = dataSnapshot.child("lastname").getValue().toString();

                        viewHolder.setName(firstname + " " + lastname);

                        String online = dataSnapshot.child("online").getValue().toString();

                        if (online.equals("true")) {

                            viewHolder.setStatus("Online");
                        } else {
                            long lastTime = Long.parseLong(online);
                            String lastSeenTime = GetTimeAgo.getTimeAgo(lastTime, getApplicationContext());
                            viewHolder.setStatus(lastSeenTime);
                        }

                        viewHolder.view.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {

                                Intent profileIntent = new Intent(getApplicationContext(), ProfileActivity.class);
                                profileIntent.putExtra("user_id", id);
                                startActivity(profileIntent);
                            }
                        });
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                        //progressDialog.hide();
                    }
                });
            }
        };

        usersList.setAdapter(firebaseRecyclerAdapter);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        View view;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            view = itemView;
        }

        public void setName(String name) {

            TextView tv_name = view.findViewById(R.id.tv_username);
            tv_name.setText(name);
        }

        public void setStatus(String status) {

            TextView tv_status = view.findViewById(R.id.tv_status);
            tv_status.setText(status);
        }
    }
}

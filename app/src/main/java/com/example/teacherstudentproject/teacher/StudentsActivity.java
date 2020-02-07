package com.example.teacherstudentproject.teacher;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.teacherstudentproject.chat.ChatActivity;
import com.example.teacherstudentproject.chat.GetTimeAgo;
import com.example.teacherstudentproject.R;
import com.example.teacherstudentproject.teacher.request.ModelRequest;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class StudentsActivity extends AppCompatActivity {

    private RecyclerView studentsList;
    private DatabaseReference friendDatabase, userDatabase;
    private FirebaseAuth mAuth;
    private String current_user_id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_students);

        //Initializing Views
        initViews();
    }

    private void initViews() {

        //Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar_students);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Students");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //RecyclerView
        studentsList = findViewById(R.id.studentsList);
        studentsList.setLayoutManager(new LinearLayoutManager(getApplicationContext()));

        //Firebase Database
        mAuth = FirebaseAuth.getInstance();
        current_user_id = mAuth.getCurrentUser().getUid();
        friendDatabase = FirebaseDatabase.getInstance().getReference().child("Friends").child(current_user_id);
        friendDatabase.keepSynced(true);
        userDatabase = FirebaseDatabase.getInstance().getReference().child("Users");

    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseRecyclerAdapter<ModelRequest, ViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<ModelRequest, ViewHolder>(
                ModelRequest.class,
                R.layout.item_users,
                ViewHolder.class,
                friendDatabase
        ) {
            @Override
            protected void populateViewHolder(final ViewHolder viewHolder, ModelRequest model, int i) {

                final String list_user_id = getRef(i).getKey();

                userDatabase.child(list_user_id).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        final String firstname = dataSnapshot.child("firstname").getValue().toString();
                        final String lastname = dataSnapshot.child("lastname").getValue().toString();


                        if (dataSnapshot.hasChild("online")){

                            String userOnline =  dataSnapshot.child("online").getValue().toString();
                            viewHolder.setUserOnline(userOnline);
                        }

                        String online = dataSnapshot.child("online").getValue().toString();
                        if (online.equals("true")) {

                            viewHolder.setStatus("Online");
                        } else {
                            long lastTime = Long.parseLong(online);
                            String lastSeenTime = GetTimeAgo.getTimeAgo(lastTime, getApplicationContext());
                            viewHolder.setStatus(lastSeenTime);
                        }


                        viewHolder.setName(firstname + " " + lastname);

                        viewHolder.view.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {

                                Intent chatIntent = new Intent(getApplicationContext(), ChatActivity.class);
                                chatIntent.putExtra("user_id", list_user_id);
                                startActivity(chatIntent);
                            }
                        });
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }
        };

        studentsList.setAdapter(firebaseRecyclerAdapter);
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

        public void setUserOnline(String online_status){

            ImageView imageView = view.findViewById(R.id.img_online);

            if (online_status.equals("true")){
                imageView.setVisibility(View.VISIBLE);
            } else {
                imageView.setVisibility(View.INVISIBLE);
            }
        }
    }
}

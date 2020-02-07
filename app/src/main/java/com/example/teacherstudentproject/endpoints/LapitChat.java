package com.example.teacherstudentproject.endpoints;

import android.app.Application;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

public class LapitChat extends Application {

    private DatabaseReference userDatabase;
    private FirebaseAuth auth;

    @Override
    public void onCreate() {
        super.onCreate();

        //FirebaseDatabase.getInstance().setPersistenceEnabled(true);

        auth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = auth.getCurrentUser();

        if (currentUser != null){

            userDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(auth.getCurrentUser().getUid());
            userDatabase.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                    if (dataSnapshot != null){

                        userDatabase.child("online").onDisconnect().setValue(ServerValue.TIMESTAMP);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }

    }
}

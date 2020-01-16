package com.example.teacherstudentproject.student;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.teacherstudentproject.R;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class ProfileActivity extends AppCompatActivity implements View.OnClickListener {

    private TextView tv_display_name;
    private ImageView user_image;
    private DatabaseReference databaseReference, friendReqdataRef, friendDatabase, notificationDatabase, mRootRef;
    private ProgressDialog progressDialog;
    private String current_state, user_id;
    private FirebaseUser currentUser;
    private Button btn_send_friend_req;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        user_id = getIntent().getStringExtra("user_id");
        //user_name = getIntent().getStringExtra("user_name");

        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        mRootRef = FirebaseDatabase.getInstance().getReference();

        databaseReference = FirebaseDatabase.getInstance().getReference().child("Users").child(user_id);
        friendReqdataRef = FirebaseDatabase.getInstance().getReference().child("Friend_req");
        friendDatabase = FirebaseDatabase.getInstance().getReference().child("Friends");
        notificationDatabase = FirebaseDatabase.getInstance().getReference().child("notifications");

        user_image = findViewById(R.id.user_image);
        tv_display_name = findViewById(R.id.tv_display_name);
        btn_send_friend_req = findViewById(R.id.btn_send_friend_req);

        current_state = "not_friends";

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Loading User Data");
        progressDialog.setMessage("Please wait while we load the user data");
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                String firstname = dataSnapshot.child("firstname").getValue().toString();
                String lastname = dataSnapshot.child("lastname").getValue().toString();

                tv_display_name.setText(firstname + " " + lastname);

                /*Picasso
                        .with(ProfileActivity.this)
                        .load(image)
                        .placeholder(R.drawable.default_avatar)
                        .into(user_image);*/

                //-------------------------- Friends List / Request Feature -------------------

                friendReqdataRef.child(currentUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        progressDialog.dismiss();
                        if (dataSnapshot.hasChild(user_id)) {
                            String req_type = dataSnapshot.child(user_id).child("request_type").getValue().toString();

                            if (req_type.equals("received")) {

                                current_state = "req_received";
                                btn_send_friend_req.setText("Accept Friend Request");

                            } else if (req_type.equals("sent")) {

                                current_state = "req_sent";
                                btn_send_friend_req.setText("Cancel Friend Request");
                            }

                            progressDialog.dismiss();

                        } else {

                            friendDatabase.child(currentUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {

                                    if (dataSnapshot.hasChild(user_id)) {

                                        current_state = "friends";
                                        btn_send_friend_req.setText("Unfriend this Person");
                                        progressDialog.dismiss();
                                    }
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                    progressDialog.dismiss();
                                }
                            });

                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    @Override
    public void onClick(View v) {

        if (v.getId() == R.id.btn_send_friend_req) {

            btn_send_friend_req.setEnabled(false);

            if (current_state.equals("not_friends")) {

                DatabaseReference newNotificationref = mRootRef.child("notifications").child(user_id).push();
                String newNotificationId = newNotificationref.getKey();

                HashMap<String, String> notificationData = new HashMap<>();
                notificationData.put("from", currentUser.getUid());
                notificationData.put("type", "request");

                Map requestMap = new HashMap();
                requestMap.put("Friend_req/" + currentUser.getUid() + "/" + user_id + "/request_type", "sent");
                requestMap.put("Friend_req/" + user_id + "/" + currentUser.getUid() + "/request_type", "received");
                requestMap.put("notifications/" + user_id + "/" + newNotificationId, notificationData);

                mRootRef.updateChildren(requestMap, new DatabaseReference.CompletionListener() {
                    @Override
                    public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

                        if(databaseError != null){

                            Toast.makeText(ProfileActivity.this, "There was some error in sending request", Toast.LENGTH_SHORT).show();

                        } else {

                            current_state = "req_sent";
                            btn_send_friend_req.setText("Cancel Friend Request");

                        }

                        btn_send_friend_req.setEnabled(true);


                    }
                });
            }

            if (current_state.equals("req_sent")) {

                friendReqdataRef.child(currentUser.getUid()).child(user_id)
                        .removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        friendReqdataRef.child(user_id).child(currentUser.getUid())
                                .removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {

                                btn_send_friend_req.setEnabled(true);
                                current_state = "not_friends";
                                btn_send_friend_req.setText("Send Friend Request");
                            }
                        });
                    }
                });
            }

            if (current_state.equals("req_received")) {

                final String currentDate = DateFormat.getDateTimeInstance().format(new Date());


                Map friendsMap = new HashMap();
                friendsMap.put("Friends/" + currentUser.getUid() + "/" + user_id + "/date", currentDate);
                friendsMap.put("Friends/" + user_id + "/"  + currentUser.getUid() + "/date", currentDate);


                friendsMap.put("Friend_req/" + currentUser.getUid() + "/" + user_id, null);
                friendsMap.put("Friend_req/" + user_id + "/" + currentUser.getUid(), null);

                /*friendDatabase.child(currentUser.getUid()).child(user_id).setValue(currentDate)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                friendDatabase.child(user_id).child(currentUser.getUid()).setValue(currentDate)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {

                                                friendReqdataRef.child(currentUser.getUid()).child(user_id)
                                                        .removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void aVoid) {
                                                        friendReqdataRef.child(user_id).child(currentUser.getUid())
                                                                .removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                                            @Override
                                                            public void onSuccess(Void aVoid) {

                                                                btn_send_friend_req.setEnabled(true);
                                                                current_state = "friends";
                                                                btn_send_friend_req.setText("Unfriend " + user_name);
                                                            }
                                                        });
                                                    }
                                                });

                                            }
                                        });
                            }
                        });*/

                mRootRef.updateChildren(friendsMap, new DatabaseReference.CompletionListener() {
                    @Override
                    public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {


                        if(databaseError == null){

                            btn_send_friend_req.setEnabled(true);
                            current_state = "friends";
                            btn_send_friend_req.setText("Unfriend this Person");

                        } else {

                            String error = databaseError.getMessage();

                            Toast.makeText(ProfileActivity.this, error, Toast.LENGTH_SHORT).show();


                        }

                    }
                });


            }


        }

    }
}

package com.example.teacherstudentproject.student;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.teacherstudentproject.chat.ChatActivity;
import com.example.teacherstudentproject.endpoints.Api;
import com.example.teacherstudentproject.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.kaopiz.kprogresshud.KProgressHUD;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class TeacherDetailActivity extends AppCompatActivity implements View.OnClickListener {

    private TextView tv_name, tv_email, tv_phone, tv_country, tv_city, tv_address, tv_education, tv_specialization,
            tv_experience, tv_courses;

    private String Teacher_ID, lat = "", lng = "", currentUserID;

    //Loader
    private KProgressHUD loader;

    //Firebase
    private String User_Key_Firebase;
    private DatabaseReference databaseReference, friendDatabaseRef, userDatabaseRef;
    private FirebaseAuth mAuth;

    //FloatingActionButton
    private FloatingActionButton fab_messages, fab_send_req;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teacher_detail);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        //Initializing Views
        initViews();

        Intent intent = getIntent();
        Teacher_ID = intent.getStringExtra("teacher_id");
    }

    private void initViews() {

        fab_messages = findViewById(R.id.fab_message);
        fab_send_req = findViewById(R.id.fab_send_req);

        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();

        databaseReference = FirebaseDatabase.getInstance().getReference();
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if (dataSnapshot.hasChild("Friends")){

                    friendDatabaseRef = FirebaseDatabase.getInstance().getReference().child("Friends");
                    friendDatabaseRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {

                            if (dataSnapshot.hasChild(currentUserID)){

                                userDatabaseRef = FirebaseDatabase.getInstance().getReference().child("Friends").child(currentUserID);
                                userDatabaseRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {

                                        if (dataSnapshot.hasChild(User_Key_Firebase)){

                                            fab_send_req.hide();
                                            fab_messages.show();
                                        } else {

                                            fab_send_req.show();
                                            fab_messages.hide();
                                        }
                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {

                                    }
                                });
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                } else {

                    fab_send_req.show();
                    fab_messages.hide();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        loader = KProgressHUD.create(TeacherDetailActivity.this)
                .setStyle(KProgressHUD.Style.SPIN_INDETERMINATE)
                .setBackgroundColor(R.color.blue)
                .setCancellable(false);

        tv_name = findViewById(R.id.tv_name);
        tv_email = findViewById(R.id.tv_email);
        tv_phone = findViewById(R.id.tv_phone);
        tv_country = findViewById(R.id.tv_country);
        tv_city = findViewById(R.id.tv_city);
        tv_address = findViewById(R.id.tv_address);
        tv_education = findViewById(R.id.tv_education);
        tv_specialization = findViewById(R.id.tv_specialization);
        tv_experience = findViewById(R.id.tv_experience);
        tv_courses = findViewById(R.id.tv_courses);

        //Checking if Internet is connected or not
        isNetworkAvailable();
    }

    private void isNetworkAvailable() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        // if no network is available networkInfo will be null, otherwise check if we are connected
        if (networkInfo != null && networkInfo.isConnected()) {
            //Fetching Details of teacher
            getDetails();
        } else if (networkInfo == null) {
            Toast.makeText(getApplicationContext(), R.string.no_internet_msg,
                    Toast.LENGTH_LONG).show();
        }

    }

    private void getDirections() {
        if (!lat.equals("") && !lng.equals("")) {
            Intent intent = new Intent(android.content.Intent.ACTION_VIEW,
                    Uri.parse("http://maps.google.com/maps?daddr=" + lat + "," + lng));
            startActivity(intent);
        }
    }

    private void getDetails() {

        loader.show();

        StringRequest req = new StringRequest(Request.Method.POST, Api.TeacherDetail_URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            boolean status = jsonObject.getBoolean("success");
                            if (status) {
                                loader.dismiss();

                                JSONObject innerObj = jsonObject.getJSONObject("teacher");
                                String name = innerObj.getString("firstname") + " " + innerObj.getString("lastname");
                                tv_name.setText(name);
                                tv_email.setText(innerObj.getString("email"));

                                //User Firebase ID
                                User_Key_Firebase = innerObj.getString("firebase_id");

                                tv_phone.setText(innerObj.getString("telephone"));
                                tv_country.setText(innerObj.getString("country"));
                                tv_city.setText(innerObj.getString("city"));
                                tv_address.setText(innerObj.getString("address"));
                                tv_education.setText(innerObj.getString("education"));
                                tv_specialization.setText(innerObj.getString("specialization"));
                                tv_experience.setText(innerObj.getString("experience"));
                                lat = innerObj.getString("latitude");
                                lng = innerObj.getString("longitude");
                                JSONArray jsonArray = innerObj.getJSONArray("courses");
                                StringBuilder stringBuilder = new StringBuilder();
                                for (int i = 0; i < jsonArray.length(); i++) {
                                    JSONObject obj = jsonArray.getJSONObject(i);
                                    String course_name = obj.getString("name");
                                    stringBuilder.append(course_name);
                                    stringBuilder.append(", ");
                                }
                                String courses = stringBuilder.toString();
                                if (courses.endsWith(" ")) {
                                    courses = courses.substring(0, courses.length() - 2);
                                }
                                tv_courses.setText(courses);

                            } else {
                                loader.dismiss();
                                Toast.makeText(getApplicationContext(), jsonObject.getString("error"),
                                        Toast.LENGTH_LONG).show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        loader.dismiss();
                        Toast.makeText(getApplicationContext(), error.getMessage(),
                                Toast.LENGTH_LONG).show();
                    }
                }) {

            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> map = new HashMap<String, String>();
                map.put("teacher_id", Teacher_ID);
                return map;
            }
        };

        RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
        requestQueue.add(req);
    }

    private void openMessage(String phoneNumber) {

        phoneNumber = phoneNumber.substring(1);

        Uri sms_uri = Uri.parse("smsto:+" + getCountryZipCode() + phoneNumber);
        Intent sms_intent = new Intent(Intent.ACTION_SENDTO, sms_uri);
        //sms_intent.putExtra("sms_body", "Good Morning ! how r U ?");
        startActivity(sms_intent);

    }

    private void openWhatsApp(String number, Context context) {

        number = number.substring(1);

        String url = "https://api.whatsapp.com/send?phone=+" + getCountryZipCode() + number;
        try {
            PackageManager pm = context.getPackageManager();
            pm.getPackageInfo("com.whatsapp", PackageManager.GET_ACTIVITIES);
            Intent i = new Intent(Intent.ACTION_VIEW);
            i.setData(Uri.parse(url));
            startActivity(i);
        } catch (PackageManager.NameNotFoundException e) {
            Toast.makeText(TeacherDetailActivity.this, "Whatsapp not installed in your phone",
                    Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    public String getCountryZipCode() {
        String CountryID = "";
        String CountryZipCode = "";

        TelephonyManager manager = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
        //getNetworkCountryIso
        CountryID = manager.getSimCountryIso().toUpperCase();
        String[] rl = this.getResources().getStringArray(R.array.CountryCodes);
        for (int i = 0; i < rl.length; i++) {
            String[] g = rl[i].split(",");
            if (g[1].trim().equals(CountryID.trim())) {
                CountryZipCode = g[0];
                break;
            }
        }
        return CountryZipCode;
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {

            /*case R.id.fab_getDirections:
                getDirections();
                break;*/

            case R.id.fab_message:
                //openMessage(tv_phone.getText().toString().trim());

                Intent chatIntent = new Intent(getApplicationContext(), ChatActivity.class);
                chatIntent.putExtra("user_id", User_Key_Firebase);
                startActivity(chatIntent);
                break;

            case R.id.fab_send_req:

                Intent intent = new Intent(getApplicationContext(), ProfileActivity.class);
                intent.putExtra("user_id", User_Key_Firebase);
                startActivity(intent);
                /*openWhatsApp(
                        tv_phone.getText().toString().trim(),
                        TeacherDetailActivity.this
                );*/
                break;
        }
    }
}

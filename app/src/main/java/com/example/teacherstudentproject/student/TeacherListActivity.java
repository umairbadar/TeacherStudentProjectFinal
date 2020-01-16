package com.example.teacherstudentproject.student;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.teacherstudentproject.endpoints.Api;
import com.example.teacherstudentproject.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.kaopiz.kprogresshud.KProgressHUD;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class TeacherListActivity extends AppCompatActivity {

    //Loader
    private KProgressHUD loader;

    private String latitude, longitude;

    private String Course_ID;

    private Adapter_TeacherList adapter;
    private List<Model_TeacherList> arr_list;

    DatabaseReference dbArtists;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teacher_list);

        dbArtists = FirebaseDatabase.getInstance().getReference().child("Users");
        dbArtists.addListenerForSingleValueEvent(valueEventListener);

        SharedPreferences sharedPreferences = getSharedPreferences("MyPre", MODE_PRIVATE);
        latitude = sharedPreferences.getString("latitude", "");
        longitude = sharedPreferences.getString("longitude", "");

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        //Initializing Views
        initView();

        Intent intent = getIntent();
        Course_ID = intent.getStringExtra("course_id");
    }

    ValueEventListener valueEventListener = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            //artistList.clear();
            if (dataSnapshot.exists()) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    /*Artist artist = snapshot.getValue(Artist.class);
                    artistList.add(artist);*/

                    if (snapshot.child("courses").hasChild("science")) {

                        Toast.makeText(getApplicationContext(), snapshot.toString(),
                                Toast.LENGTH_LONG).show();
                        Log.e("Data", snapshot.toString());
                    }
                }
                //adapter.notifyDataSetChanged();
            }
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    };

    @Override
    public void onBackPressed() {

        startActivity(new Intent(getApplicationContext(), SelectCoursesActivity.class));
        finish();
    }

    private void initView() {

        loader = KProgressHUD.create(TeacherListActivity.this)
                .setStyle(KProgressHUD.Style.SPIN_INDETERMINATE)
                .setBackgroundColor(R.color.blue)
                .setCancellable(false);

        RecyclerView recyclerView_teachers = findViewById(R.id.recyclerView_teachers);
        recyclerView_teachers.setLayoutManager(new GridLayoutManager(this, 1));
        arr_list = new ArrayList<>();
        adapter = new Adapter_TeacherList(arr_list, getApplicationContext());
        recyclerView_teachers.setAdapter(adapter);
        isNetworkAvailable();
    }

    private void isNetworkAvailable() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        // if no network is available networkInfo will be null, otherwise check if we are connected
        if (networkInfo != null && networkInfo.isConnected()) {

            getTeachers();
        } else if (networkInfo == null) {
            Toast.makeText(getApplicationContext(), R.string.no_internet_msg,
                    Toast.LENGTH_LONG).show();
        }

    }

    private void getTeachers() {

        loader.show();

        StringRequest req = new StringRequest(Request.Method.POST, Api.TeacherListing_URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            boolean status = jsonObject.getBoolean("success");
                            if (status) {
                                loader.dismiss();
                                JSONArray jsonArray = jsonObject.getJSONArray("teachers");
                                for (int i = 0; i < jsonArray.length(); i++) {
                                    JSONObject object = jsonArray.getJSONObject(i);
                                    String id = object.getString("teacher_id");
                                    String name = object.getString("firstname") + " " + object.getString("lastname");
                                    String distance = object.getString("distance");

                                    Model_TeacherList item = new Model_TeacherList(
                                            id,
                                            name,
                                            distance + " km away"
                                    );
                                    arr_list.add(item);
                                }
                                adapter.notifyDataSetChanged();
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
                map.put("course_id", Course_ID);
                map.put("latitude", latitude);
                map.put("longitude", longitude);
                return map;
            }
        };

        RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
        requestQueue.add(req);
    }
}

package com.example.teacherstudentproject.student;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
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
import com.example.teacherstudentproject.welcome.WelcomeActivity;
import com.kaopiz.kprogresshud.KProgressHUD;
import com.toptoche.searchablespinnerlibrary.SearchableSpinner;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class SelectCoursesActivity extends AppCompatActivity implements View.OnClickListener {


    //Loader
    private KProgressHUD loader;

    private SharedPreferences sharedPreferences;

    //Course Category
    private Spinner spn_course_cat;
    private ArrayList<String> arr_courses_cat;
    private ArrayList<String> arr_courses_cat_id;
    private String Course_Cat_ID;

    //Courses
    private SearchableSpinner spn_courses;
    private ArrayList<String> arr_courses;
    private ArrayList<String> arr_courses_id;
    private String Course_ID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_courses);

        sharedPreferences = getSharedPreferences("MyPre", MODE_PRIVATE);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        //Initializing Views
        initViews();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.student_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.logout){
            showDialog(this, "Are you sure you want to logout?");
        } else if(id == R.id.profile){
            startActivity(new Intent(getApplicationContext(), StudentProfileActivity.class));
        }

        return true;
    }

    private void isInternetAvailable() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        // if no network is available networkInfo will be null, otherwise check if we are connected
        if (networkInfo != null && networkInfo.isConnected()) {

            getCoursesCategory();
        } else if (networkInfo == null) {
            Toast.makeText(getApplicationContext(),R.string.no_internet_msg,
                    Toast.LENGTH_LONG).show();
        }

    }

    private void isNetworkAvailable() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        // if no network is available networkInfo will be null, otherwise check if we are connected
        if (networkInfo != null && networkInfo.isConnected()) {

           callNextActivity();
        } else if (networkInfo == null) {
            Toast.makeText(getApplicationContext(),R.string.no_internet_msg,
                    Toast.LENGTH_LONG).show();
        }

    }

    private void callNextActivity(){
        Intent intent = new Intent(getApplicationContext(), TeacherListActivity.class);
        intent.putExtra("course_id", Course_ID);
        startActivity(intent);
    }

    private void initViews(){

        loader = KProgressHUD.create(SelectCoursesActivity.this)
                .setStyle(KProgressHUD.Style.SPIN_INDETERMINATE)
                .setBackgroundColor(R.color.blue)
                .setCancellable(false);

        //Course Category
        spn_course_cat = findViewById(R.id.spn_course_cat);
        arr_courses_cat = new ArrayList<>();
        arr_courses_cat_id = new ArrayList<>();
        isInternetAvailable();

        spn_course_cat.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Course_Cat_ID = arr_courses_cat_id.get(position);
                if (!Course_Cat_ID.equals("0")){
                    getCourses(Course_Cat_ID);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        //Courses
        spn_courses = findViewById(R.id.spn_courses);
        arr_courses = new ArrayList<>();
        arr_courses_id = new ArrayList<>();

        spn_courses.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Course_ID = arr_courses_id.get(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private void getCoursesCategory(){

        loader.show();

        StringRequest req = new StringRequest(Request.Method.GET, Api.CoursesCategory_URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            loader.dismiss();
                            arr_courses_cat_id.add("0");
                            arr_courses_cat.add("Select Course Category");
                            JSONObject jsonObject = new JSONObject(response);
                            JSONArray jsonArray = jsonObject.getJSONArray("categories");
                            for (int i = 0; i < jsonArray.length(); i++){
                                JSONObject object = jsonArray.getJSONObject(i);
                                arr_courses_cat_id.add(object.getString("category_id"));
                                arr_courses_cat.add(object.getString("name"));
                            }
                            spn_course_cat.setAdapter(new ArrayAdapter<>(SelectCoursesActivity.this,
                                    android.R.layout.simple_spinner_dropdown_item, arr_courses_cat));
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
                });

        RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
        requestQueue.add(req);
    }

    private void getCourses(final String CatID){

        arr_courses.clear();
        arr_courses_id.clear();

        StringRequest req = new StringRequest(Request.Method.POST, Api.Courses_URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            spn_courses.setTitle("Select Courses");
                            JSONObject jsonObject = new JSONObject(response);
                            boolean status = jsonObject.getBoolean("success");
                            if (status){
                                spn_courses.setVisibility(View.VISIBLE);
                                JSONArray jsonArray = jsonObject.getJSONArray("courses");
                                for (int i = 0;i < jsonArray.length(); i++){
                                    JSONObject object = jsonArray.getJSONObject(i);
                                    arr_courses_id.add(object.getString("id"));
                                    arr_courses.add(object.getString("name"));
                                }

                                spn_courses.setAdapter(new ArrayAdapter<>(SelectCoursesActivity.this,
                                        android.R.layout.simple_spinner_dropdown_item, arr_courses));
                            } else {
                                spn_courses.setVisibility(View.GONE);
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
                        Toast.makeText(getApplicationContext(), error.getMessage(),
                                Toast.LENGTH_LONG).show();
                    }
                }){

            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> map = new HashMap<String, String>();
                map.put("category_id", CatID);
                return map;
            }
        };

        RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
        requestQueue.add(req);
    }

    public void showDialog(Activity activity, String msg) {
        final Dialog dialog = new Dialog(activity);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.logout_dialog);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        TextView text = (TextView) dialog.findViewById(R.id.text_dialog);
        text.setText(msg);

        Button dialogButton = (Button) dialog.findViewById(R.id.btn_ok);
        dialogButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.clear();
                editor.apply();
                startActivity(new Intent(getApplicationContext(), WelcomeActivity.class));
                finish();
            }
        });
        Button dialogCancel = (Button) dialog.findViewById(R.id.btn_cancel);
        dialogCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    @Override
    public void onClick(View v) {

        if (v.getId() == R.id.btn_find_teacher){

            if (spn_course_cat.getSelectedItemPosition() == 0){
                Toast.makeText(getApplicationContext(), "Select Course Category",
                        Toast.LENGTH_LONG).show();
            }  else {

                if (spn_courses.getVisibility() == View.VISIBLE){
                    isNetworkAvailable();
                } else {
                    Toast.makeText(getApplicationContext(), "No Courses Found!",
                            Toast.LENGTH_LONG).show();
                }

            }
        }
    }
}

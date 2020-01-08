package com.example.teacherstudentproject.teacher;

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
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
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
import com.kaopiz.kprogresshud.KProgressHUD;
import com.toptoche.searchablespinnerlibrary.SearchableSpinner;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class AddNewCourseActivity extends AppCompatActivity implements View.OnClickListener {

    private SharedPreferences sharedPreferences;

    //Loader
    private KProgressHUD loader;

    private SearchableSpinner spn_course_cat;
    private ArrayList<String> arr_course_cat;
    private ArrayList<String> arr_course_cat_id;
    private String course_Cat_ID;

    private EditText et_course_name, et_subject_name;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_new_course);

        sharedPreferences = getSharedPreferences("MyPre", MODE_PRIVATE);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowTitleEnabled(false);

        //Initializing Views
        isNetworkAvailable();
    }

    private void isNetworkAvailable() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        // if no network is available networkInfo will be null, otherwise check if we are connected
        if (networkInfo != null && networkInfo.isConnected()) {
            initViews();
        } else if (networkInfo == null) {
            Toast.makeText(getApplicationContext(), R.string.no_internet_msg,
                    Toast.LENGTH_LONG).show();
        }

    }

    private void isNetworkAvailable(String cat_id, String course_name, String subject_name) {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        // if no network is available networkInfo will be null, otherwise check if we are connected
        if (networkInfo != null && networkInfo.isConnected()) {
            addNewCourse(cat_id, course_name, subject_name);
        } else if (networkInfo == null) {
            Toast.makeText(getApplicationContext(), R.string.no_internet_msg,
                    Toast.LENGTH_LONG).show();
        }

    }

    private void initViews() {

        loader = KProgressHUD.create(AddNewCourseActivity.this)
                .setStyle(KProgressHUD.Style.SPIN_INDETERMINATE)
                .setBackgroundColor(R.color.blue)
                .setCancellable(false);

        et_course_name = findViewById(R.id.et_course_name);
        et_subject_name = findViewById(R.id.et_subject_name);

        spn_course_cat = findViewById(R.id.spn_courses_cat);
        arr_course_cat = new ArrayList<>();
        arr_course_cat_id = new ArrayList<>();
        getCourseCat();

        spn_course_cat.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                course_Cat_ID = arr_course_cat_id.get(position);
                if (course_Cat_ID.equals("1")) {
                    et_subject_name.setVisibility(View.VISIBLE);
                } else {
                    et_subject_name.setVisibility(View.GONE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

    }

    private void getCourseCat() {

        StringRequest req = new StringRequest(Request.Method.GET, Api.CoursesCategory_URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            spn_course_cat.setTitle("Select Courses");

                            arr_course_cat.add("Select Course Catgory");
                            arr_course_cat_id.add("0");

                            arr_course_cat.add("Add new Course Category");
                            arr_course_cat_id.add("1");

                            JSONObject jsonObject = new JSONObject(response);
                            JSONArray jsonArray = jsonObject.getJSONArray("categories");
                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONObject object = jsonArray.getJSONObject(i);
                                arr_course_cat_id.add(object.getString("category_id"));
                                arr_course_cat.add(object.getString("name"));
                            }
                            spn_course_cat.setAdapter(new ArrayAdapter<>(getApplicationContext(),
                                    android.R.layout.simple_spinner_dropdown_item, arr_course_cat));

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
                });

        RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
        requestQueue.add(req);
    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(getApplicationContext(), CoursesActivity.class));
        finish();
    }

    public void nextActivity() {
        Intent intent = new Intent(getApplicationContext(), CoursesActivity.class);
        startActivity(intent);
        finish();
    }

    private void addNewCourse(final String cat_id, final String course_name, final String subject_name) {

        loader.show();

        StringRequest req = new StringRequest(Request.Method.POST, Api.UpdateProfile_URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            boolean status = jsonObject.getBoolean("success");
                            if (status) {
                                loader.dismiss();
                                showDialog(AddNewCourseActivity.this, "Hello, " + sharedPreferences.getString("firstname", ""));
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
                map.put("user_id", sharedPreferences.getString("customer_id", ""));
                map.put("firstname", sharedPreferences.getString("firstname", ""));
                map.put("lastname", sharedPreferences.getString("lastname", ""));
                map.put("email", sharedPreferences.getString("email", ""));
                map.put("telephone", sharedPreferences.getString("telephone", ""));
                map.put("address_id", sharedPreferences.getString("address_id", ""));
                map.put("address_1", sharedPreferences.getString("address", ""));
                map.put("latitude", sharedPreferences.getString("latitude", ""));
                map.put("longitude", sharedPreferences.getString("longitude", ""));
                map.put("city", sharedPreferences.getString("city", ""));
                map.put("country_id", sharedPreferences.getString("country_id", ""));
                map.put("zone_id", sharedPreferences.getString("zone_id", ""));
                map.put("course_name", course_name);
                map.put("category_name", subject_name);
                map.put("category_id", cat_id);
                return map;
            }
        };

        RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
        requestQueue.add(req);

    }

    public void showDialog(final Context context, String msg) {

        final Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.msg_dialog);

        Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        dialog.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
        dialog.setCancelable(false);

        Button btn_ok = (Button) dialog.findViewById(R.id.btn_ok);

        TextView tv_head = (TextView) dialog.findViewById(R.id.tv_head);
        tv_head.setText(msg);

        btn_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                nextActivity();
            }
        });
        dialog.show();
    }

    @Override
    public void onClick(View v) {

        if (v.getId() == R.id.btn_submit) {

            if (spn_course_cat.getSelectedItemPosition() == 0) {
                Toast.makeText(getApplicationContext(), "Select course category",
                        Toast.LENGTH_LONG).show();
            } else if (spn_course_cat.getSelectedItemPosition() == 1) {

                if (TextUtils.isEmpty(et_subject_name.getText())) {
                    et_subject_name.setError("Please enter subject name");
                    et_subject_name.requestFocus();
                } else if (TextUtils.isEmpty(et_course_name.getText())) {
                    et_course_name.setError("Please enter course name");
                    et_course_name.requestFocus();
                } else {
                    //When adding new category + new course
                    isNetworkAvailable(
                            "",
                            et_course_name.getText().toString(),
                            et_subject_name.getText().toString()
                    );
                }

            } else {
                if (TextUtils.isEmpty(et_course_name.getText())) {
                    et_course_name.setError("Please enter course name");
                    et_course_name.requestFocus();
                } else {
                    //when adding new course in available category
                    isNetworkAvailable(
                            course_Cat_ID,
                            et_course_name.getText().toString(),
                            ""
                    );
                }
            }
        }

    }
}

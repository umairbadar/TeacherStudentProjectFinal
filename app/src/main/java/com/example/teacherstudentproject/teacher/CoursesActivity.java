package com.example.teacherstudentproject.teacher;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class CoursesActivity extends AppCompatActivity implements View.OnClickListener {

    public static SharedPreferences sharedPreferences;

    private ProgressBar progressBar;

    private String id = "";
    private String pre_selected_ids = "";

    private MultiSpinnerSearch searchSpinner;
    private List<KeyPairBoolData> listArray;

    private RecyclerView recyclerView_selectedCourses;
    private Adapter_SelectedCourses adapter;
    private List<Model_SelectedCourses> list;
    public static List<String> list_ids;

    //Loader
    private KProgressHUD loader;

    private TextView tv_error;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_courses);

        sharedPreferences = getSharedPreferences("MyPre", MODE_PRIVATE);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowTitleEnabled(false);

        //Initializing Views
        isNetworkAvailable(1);
    }

    private void isNetworkAvailable(int funcCall) {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        // if no network is available networkInfo will be null, otherwise check if we are connected
        if (networkInfo != null && networkInfo.isConnected()) {
            if (funcCall == 1)
                initViews();
            else
                addCourse();
        } else if (networkInfo == null) {
            Toast.makeText(getApplicationContext(), R.string.no_internet_msg,
                    Toast.LENGTH_LONG).show();
        }

    }

    private void initViews() {

        loader = KProgressHUD.create(CoursesActivity.this)
                .setStyle(KProgressHUD.Style.SPIN_INDETERMINATE)
                .setBackgroundColor(R.color.blue)
                .setCancellable(false);

        progressBar = findViewById(R.id.progressBar1);

        //Selected Courses
        recyclerView_selectedCourses = findViewById(R.id.recyclerView_selectedCourses);
        recyclerView_selectedCourses.setLayoutManager(new LinearLayoutManager(getApplicationContext(),
                LinearLayoutManager.VERTICAL, false));
        list = new ArrayList<>();
        list_ids = new ArrayList<>();
        adapter = new Adapter_SelectedCourses(list, getApplicationContext());
        recyclerView_selectedCourses.setAdapter(adapter);
        getSelectedCourses();

        searchSpinner = (MultiSpinnerSearch) findViewById(R.id.spinner);
        listArray = new ArrayList<KeyPairBoolData>();
        getNonSelectedCourses();

        searchSpinner.setEmptyTitle("No Data Found!");
        searchSpinner.setSearchHint("Find Data");

        searchSpinner.setItems(listArray, -1, new SpinnerListener() {
            @Override
            public void onItemsSelected(List<KeyPairBoolData> items) {

                id = "";
                id = searchSpinner
                        .getSelectedIds()
                        .toString()
                        .replace("[", "")
                        .replace("]", "")
                        .replace(", ", ",");
            }
        });

        tv_error = findViewById(R.id.tv_error);
    }

    private void getSelectedCourses() {
        StringRequest req = new StringRequest(Request.Method.POST, Api.SelectedCourses_URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            boolean status = jsonObject.getBoolean("success");
                            if (status) {
                                tv_error.setVisibility(View.GONE);
                                progressBar.setVisibility(View.GONE);
                                recyclerView_selectedCourses.setVisibility(View.VISIBLE);
                                JSONArray jsonArray = jsonObject.getJSONArray("data");
                                for (int i = 0; i < jsonArray.length(); i++) {
                                    JSONObject object = jsonArray.getJSONObject(i);
                                    String id = object.getString("product_id");
                                    String name = object.getString("name");

                                    Model_SelectedCourses item = new Model_SelectedCourses(
                                            id,
                                            name
                                    );

                                    list.add(item);
                                    list_ids.add(id);

                                    Collections.sort(list, new Comparator<Model_SelectedCourses>() {
                                        public int compare(Model_SelectedCourses obj1, Model_SelectedCourses obj2) {
                                            // ## Ascending order
                                            return obj1.getCourse_name().compareToIgnoreCase(obj2.getCourse_name());
                                        }
                                    });
                                }
                                adapter.notifyDataSetChanged();

                            } else {
                                recyclerView_selectedCourses.setVisibility(View.GONE);
                                progressBar.setVisibility(View.GONE);
                                tv_error.setVisibility(View.VISIBLE);
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
                }) {

            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> map = new HashMap<String, String>();
                map.put("user_id", sharedPreferences.getString("customer_id", ""));
                return map;
            }
        };

        RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
        requestQueue.add(req);
    }

    private void getNonSelectedCourses() {

        StringRequest req = new StringRequest(Request.Method.POST, Api.NonSelectedCourses_URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            boolean status = jsonObject.getBoolean("success");
                            if (status) {
                                JSONArray jsonArray = jsonObject.getJSONArray("courses");
                                for (int i = 0; i < jsonArray.length(); i++) {
                                    JSONObject object = jsonArray.getJSONObject(i);
                                    KeyPairBoolData h = new KeyPairBoolData();
                                    h.setId(Long.parseLong(object.getString("id")));
                                    h.setName(object.getString("name"));
                                    h.setSelected(false);
                                    listArray.add(h);
                                }
                            } else {
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
                }) {

            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> map = new HashMap<String, String>();
                map.put("user_id", sharedPreferences.getString("customer_id", ""));
                return map;
            }
        };

        RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
        requestQueue.add(req);

    }

    public void refreshActivity() {
        Intent intent = getIntent();
        overridePendingTransition(0, 0);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        finish();
        overridePendingTransition(0, 0);
        startActivity(intent);
    }

    private void addCourse() {

        loader.show();

        pre_selected_ids = "";
        pre_selected_ids = list_ids.toString()
                .replace("[", "")
                .replace("]", "")
                .replace(", ", ",");

        String all_ids = "";
        if (!pre_selected_ids.equals("")){
            all_ids = id + "," + pre_selected_ids;
        } else {
            all_ids = id;
        }
        final String finalAll_ids = all_ids;

        StringRequest req = new StringRequest(Request.Method.POST, Api.UpdateProfile_URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            boolean status = jsonObject.getBoolean("success");
                            if (status) {
                                loader.dismiss();
                                refreshActivity();
                                Toast.makeText(getApplicationContext(), "Courses Updated!",
                                        Toast.LENGTH_LONG).show();
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
                map.put("courses", finalAll_ids);
                return map;
            }
        };

        RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
        requestQueue.add(req);

        /*final String final_id = id.trim() *//*+ "," + pre_selected_ids*//*;
        Toast.makeText(getApplicationContext(), final_id, Toast.LENGTH_LONG).show();*/
    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(getApplicationContext(), TeacherActivity.class));
        finish();
    }

    private void deleteCourse() {

        //if (!list_ids.toString().equals("[]")) {
        StringRequest req = new StringRequest(Request.Method.POST, Api.UpdateProfile_URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            boolean status = jsonObject.getBoolean("success");
                            if (status) {
                                refreshActivity();
                                Toast.makeText(getApplicationContext(), "Courses Updated!",
                                        Toast.LENGTH_LONG).show();
                            } else {
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
                }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> map = new HashMap<String, String>();
                map.put("user_id", CoursesActivity.sharedPreferences.getString("customer_id", ""));
                map.put("firstname", CoursesActivity.sharedPreferences.getString("firstname", ""));
                map.put("lastname", CoursesActivity.sharedPreferences.getString("lastname", ""));
                map.put("email", CoursesActivity.sharedPreferences.getString("email", ""));
                map.put("telephone", CoursesActivity.sharedPreferences.getString("telephone", ""));
                map.put("address_id", CoursesActivity.sharedPreferences.getString("address_id", ""));
                map.put("address_1", CoursesActivity.sharedPreferences.getString("address", ""));
                map.put("latitude", CoursesActivity.sharedPreferences.getString("latitude", ""));
                map.put("longitude", CoursesActivity.sharedPreferences.getString("longitude", ""));
                map.put("city", CoursesActivity.sharedPreferences.getString("city", ""));
                map.put("country_id", CoursesActivity.sharedPreferences.getString("country_id", ""));
                map.put("zone_id", CoursesActivity.sharedPreferences.getString("zone_id", ""));
                map.put("courses", list_ids.toString()
                        .replace("[", "")
                        .replace("]", "")
                );
                return map;
            }
        };

        RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
        requestQueue.add(req);

        /*} else {
            Toast.makeText(getApplicationContext(), "List Empty!",
                    Toast.LENGTH_LONG).show();
        }*/
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {

            case R.id.btn_submit:

                if (!id.equals(""))
                    isNetworkAvailable(2);
                else
                    Toast.makeText(getApplicationContext(), "Please select courses",
                            Toast.LENGTH_LONG).show();
                break;

            case R.id.tv_new_course:
                startActivity(new Intent(getApplicationContext(), AddNewCourseActivity.class));
                finish();
                break;

            case R.id.btn_save_list:
                deleteCourse();
                break;
        }
    }
}

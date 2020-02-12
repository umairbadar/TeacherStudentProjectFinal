package com.example.teacherstudentproject.teacher;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
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

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class PortfolioDetailsActivity extends AppCompatActivity implements View.OnClickListener {

    private EditText et_experience, et_specialization;
    private Spinner spn_education;
    private List<String> arr_list;

    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    //Loader
    private KProgressHUD loader;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_portfolio_details);

        sharedPreferences = getSharedPreferences("MyPre", MODE_PRIVATE);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //Initializing Views
        initViews();
    }

    private void initViews() {

        loader = KProgressHUD.create(PortfolioDetailsActivity.this)
                .setStyle(KProgressHUD.Style.SPIN_INDETERMINATE)
                .setBackgroundColor(R.color.blue)
                .setCancellable(false);

        spn_education = findViewById(R.id.spn_education);
        et_specialization = findViewById(R.id.et_speclization);
        et_experience = findViewById(R.id.et_experience);

        String experience = sharedPreferences.getString("experience", "null");
        String specialization = sharedPreferences.getString("specialization", "null");
        String education_no = sharedPreferences.getString("education_no", "null");
        //Toast.makeText(getApplicationContext(), education, Toast.LENGTH_LONG).show();

        arr_list = new ArrayList<>();
        arr_list.add("Select Education");
        arr_list.add("Doctor");
        arr_list.add("Masters");
        arr_list.add("Bachelors");
        arr_list.add("Associate");
        spn_education.setAdapter(new ArrayAdapter<>(getApplicationContext(),
                android.R.layout.simple_spinner_dropdown_item, arr_list));

        if (!experience.equals("null")) {
            et_experience.setText(experience);
        }
        if (!specialization.equals("null")) {
            et_specialization.setText(specialization);
        }

        if (!education_no.equals("null")) {
            spn_education.setSelection(Integer.parseInt(education_no));
        }
    }

    private void refreshActivity() {
        Intent intent = getIntent();
        overridePendingTransition(0, 0);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        finish();
        overridePendingTransition(0, 0);
        startActivity(intent);
    }

    private void updatePortfolio() {

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

                                refreshActivity();

                                editor = sharedPreferences.edit();
                                editor.putString("experience", et_experience.getText().toString());
                                editor.putString("education", spn_education.getSelectedItem().toString());
                                editor.putString("education_no", String.valueOf(spn_education.getSelectedItemPosition()));
                                editor.putString("specialization", et_specialization.getText().toString());
                                editor.apply();

                                Toast.makeText(getApplicationContext(), "Portfolio Updated!",
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
                map.put("experience", et_experience.getText().toString());
                map.put("specialization", et_specialization.getText().toString());
                map.put("education", spn_education.getSelectedItem().toString());
                map.put("education_no", String.valueOf(spn_education.getSelectedItemPosition()));
                map.put("portfolio", sharedPreferences.getString("zone_id", ""));
                return map;
            }
        };

        RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
        requestQueue.add(req);
    }

    private void isNetworkAvailable() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        // if no network is available networkInfo will be null, otherwise check if we are connected
        if (networkInfo != null && networkInfo.isConnected()) {

            updatePortfolio();
        } else if (networkInfo == null) {
            Toast.makeText(getApplicationContext(), R.string.no_internet_msg,
                    Toast.LENGTH_LONG).show();
        }

    }

    private void validation() {
        if (spn_education.getSelectedItemPosition() == 0) {
            Toast.makeText(getApplicationContext(), "Please select education",
                    Toast.LENGTH_LONG).show();
        } else if (TextUtils.isEmpty(et_specialization.getText())) {
            et_specialization.setError("Please enter specialization");
            et_specialization.requestFocus();
        } else if (TextUtils.isEmpty(et_experience.getText())) {
            et_experience.setError("Please enter experience");
            et_experience.requestFocus();
        } else {
            isNetworkAvailable();
        }
    }

    @Override
    public void onClick(View v) {

        if (v.getId() == R.id.btn_update_portfolio) {
            validation();
        }
    }
}

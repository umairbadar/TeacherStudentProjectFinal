package com.example.teacherstudentproject.teacher;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
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

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class ChangePasswordActivity extends AppCompatActivity implements View.OnClickListener {

    private EditText et_old_password, et_password, et_cpassword;

    private SharedPreferences sharedPreferences;

    //Loader
    private KProgressHUD loader;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);

        sharedPreferences = getSharedPreferences("MyPre", MODE_PRIVATE);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowTitleEnabled(false);

        //Initializing Views
        initViews();
    }

    private void initViews() {

        loader = KProgressHUD.create(ChangePasswordActivity.this)
                .setStyle(KProgressHUD.Style.SPIN_INDETERMINATE)
                .setBackgroundColor(R.color.blue)
                .setCancellable(false);

        et_old_password = findViewById(R.id.et_old_password);
        et_password = findViewById(R.id.et_password);
        et_cpassword = findViewById(R.id.et_cpassword);
    }

    private void changePassword(final String password) {

        loader.show();

        StringRequest req = new StringRequest(Request.Method.POST, Api.ChangePassword_URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            boolean status = jsonObject.getBoolean("success");
                            if (status) {
                                finish();
                                Toast.makeText(getApplicationContext(), "Password Changed Sucessfully!",
                                        Toast.LENGTH_LONG).show();
                                loader.dismiss();
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
                map.put("old_password", et_old_password.getText().toString());
                map.put("password", password);
                return map;
            }
        };

        RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
        requestQueue.add(req);

    }

    private void validation() {

        if (TextUtils.isEmpty(et_old_password.getText())) {
            et_old_password.setError("Please enter old password");
            et_old_password.requestFocus();
        } else if (TextUtils.isEmpty(et_password.getText())) {
            et_password.setError("Please enter password");
            et_password.requestFocus();
        } else if (TextUtils.isEmpty(et_cpassword.getText())) {
            et_cpassword.setError("Please enter confirm password");
            et_cpassword.requestFocus();
        } else if (!et_password.getText().toString().equals(et_cpassword.getText().toString())) {
            Toast.makeText(getApplicationContext(), "Password not match",
                    Toast.LENGTH_LONG).show();
        } else {
            changePassword(et_password.getText().toString());
        }
    }

    private void isNetworkAvailable() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        // if no network is available networkInfo will be null, otherwise check if we are connected
        if (networkInfo != null && networkInfo.isConnected()) {

            validation();
        } else if (networkInfo == null) {
            Toast.makeText(getApplicationContext(), R.string.no_internet_msg,
                    Toast.LENGTH_LONG).show();
        }

    }

    @Override
    public void onClick(View v) {

        if (v.getId() == R.id.btn_change_password) {
            isNetworkAvailable();
        }
    }
}

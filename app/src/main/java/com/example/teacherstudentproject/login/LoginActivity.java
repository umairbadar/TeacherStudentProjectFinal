package com.example.teacherstudentproject.login;

import androidx.annotation.NonNull;
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
import com.example.teacherstudentproject.student.SelectCoursesActivity;
import com.example.teacherstudentproject.teacher.TeacherActivity;
import com.example.teacherstudentproject.welcome.WelcomeActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.kaopiz.kprogresshud.KProgressHUD;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {

    private EditText et_email, et_password;

    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    //Loader
    private KProgressHUD loader;

    //Firebase
    private FirebaseAuth mAuth;
    private DatabaseReference userDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();

        userDatabase = FirebaseDatabase.getInstance().getReference().child("Users");

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        sharedPreferences = getSharedPreferences("MyPre", MODE_PRIVATE);

        //Initializing Views
        initViews();
    }

    private void initViews() {

        loader = KProgressHUD.create(LoginActivity.this)
                .setStyle(KProgressHUD.Style.SPIN_INDETERMINATE)
                .setBackgroundColor(R.color.blue)
                .setCancellable(false);

        et_email = findViewById(R.id.et_email);
        et_password = findViewById(R.id.et_password);
    }

    @Override
    public void onBackPressed() {
        finish();
        startActivity(new Intent(getApplicationContext(), WelcomeActivity.class));
    }

    private void loginUserInFirebase(final String customerGroup) {

        mAuth.signInWithEmailAndPassword(et_email.getText().toString(), et_password.getText().toString()).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {

                if (task.isSuccessful()) {

                    loader.dismiss();

                    String current_user_id = mAuth.getCurrentUser().getUid();
                    String deviceToken = FirebaseInstanceId.getInstance().getToken();

                    userDatabase.child(current_user_id).child("device_token").setValue(deviceToken)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {

                            if (customerGroup.equals("1")) {
                                Intent teacherIntent = new Intent(getApplicationContext(), TeacherActivity.class);
                                teacherIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(teacherIntent);
                                finish();
                            } else {
                                Intent studentIntent = new Intent(getApplicationContext(), SelectCoursesActivity.class);
                                studentIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(studentIntent);
                                finish();
                            }
                        }
                    });
                } else {

                    loader.dismiss();
                    Toast.makeText(getApplicationContext(), task.getException().toString(),
                            Toast.LENGTH_LONG).show();
                }
            }
        });

    }

    private void loginUser() {

        loader.show();
        StringRequest req = new StringRequest(Request.Method.POST, Api.Login_URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            boolean status = jsonObject.getBoolean("success");
                            if (status) {
                                //loader.dismiss();

                                JSONObject innerObj = jsonObject.getJSONObject("data");

                                String customerGroup = innerObj.getString("customer_group_id");

                                editor = sharedPreferences.edit();
                                editor.putString("customer_id", innerObj.getString("customer_id"));
                                editor.putString("customer_group", customerGroup);
                                editor.putString("firstname", innerObj.getString("firstname"));
                                editor.putString("lastname", innerObj.getString("lastname"));
                                editor.putString("email", innerObj.getString("email"));
                                editor.putString("telephone", innerObj.getString("telephone"));
                                editor.putString("portfolio", innerObj.getString("portfolio"));
                                editor.putString("experience", innerObj.getString("experience"));
                                editor.putString("education", innerObj.getString("education"));
                                editor.putString("education_no", innerObj.getString("education_no"));
                                editor.putString("specialization", innerObj.getString("specialization"));

                                JSONObject obj = innerObj.getJSONObject("address");

                                editor.putString("address_id", obj.getString("address_id"));
                                editor.putString("address", obj.getString("address_1"));
                                editor.putString("latitude", obj.getString("latitude"));
                                editor.putString("longitude", obj.getString("longitude"));
                                editor.putString("city", obj.getString("city"));
                                editor.putString("zone_id", obj.getString("zone_id"));
                                editor.putString("zone", obj.getString("zone"));
                                editor.putString("country_id", obj.getString("country_id"));
                                editor.putString("country", obj.getString("country"));
                                editor.putBoolean("saveLogin", true);
                                editor.apply();

                                loginUserInFirebase(customerGroup);

                            } else {
                                loader.dismiss();
                                Toast.makeText(getApplicationContext(), "Invalid Email and Password",
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
                map.put("email", et_email.getText().toString());
                map.put("password", et_password.getText().toString());
                return map;
            }
        };

        RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
        requestQueue.add(req);
    }

    private void validation() {

        String emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";

        if (TextUtils.isEmpty(et_email.getText())) {
            et_email.setError("Please enter email address");
            et_email.requestFocus();
        } else if (!et_email.getText().toString().matches(emailPattern)) {
            et_email.setError("Please enter valid email address");
            et_email.requestFocus();
        } else if (TextUtils.isEmpty(et_password.getText())) {
            et_password.setError("Please enter password");
            et_password.requestFocus();
        } else {
            loginUser();
        }
    }

    private void isNetworkAvailable() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        // if no network is available networkInfo will be null, otherwise check if we are connected
        if (networkInfo != null && networkInfo.isConnected()) {

            validation();
        } else if (networkInfo == null) {
            Toast.makeText(getApplicationContext(),R.string.no_internet_msg,
                    Toast.LENGTH_LONG).show();
        }

    }

    @Override
    public void onClick(View view) {

        if (view.getId() == R.id.btn_login) {
            isNetworkAvailable();
        }
    }
}

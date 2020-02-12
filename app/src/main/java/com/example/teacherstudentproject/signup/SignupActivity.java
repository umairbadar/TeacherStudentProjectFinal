package com.example.teacherstudentproject.signup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.teacherstudentproject.endpoints.Api;
import com.example.teacherstudentproject.login.LoginActivity;
import com.example.teacherstudentproject.R;
import com.example.teacherstudentproject.welcome.WelcomeActivity;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.kaopiz.kprogresshud.KProgressHUD;
import com.toptoche.searchablespinnerlibrary.SearchableSpinner;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class SignupActivity extends AppCompatActivity implements View.OnClickListener {

    //Loader
    private KProgressHUD loader;

    //Location
    private String lattitude, longitude;
    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;
    private int PLACE_PICKER_REQUEST = 1;

    //Edittext Declarations
    private EditText et_address, et_first_name, et_last_name, et_telephone, et_city, et_email, et_password, et_confirmPassword;

    //Country
    private SearchableSpinner spn_country;
    private ArrayList<String> arr_countries;
    private ArrayList<String> arr_countries_id;
    private String country_id;

    //Customer Group
    private Spinner spn_customer_group;
    private ArrayList<String> arr_customer_group;
    private String customer_group;

    //States
    private SearchableSpinner spn_state;
    private ArrayList<String> arr_states;
    private ArrayList<String> arr_states_id;
    private String state_id;

    //Firebase Datebase
    private FirebaseAuth mAuth;
    private DatabaseReference databaseReference;
    private FirebaseUser currentUser;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        mAuth = FirebaseAuth.getInstance();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //Initializing Views
        initViews();

        //Getting All Countries
        getAllCountries();
    }

    private void initViews() {

        loader = KProgressHUD.create(SignupActivity.this)
                .setStyle(KProgressHUD.Style.SPIN_INDETERMINATE)
                .setBackgroundColor(R.color.blue)
                .setCancellable(false);

        spn_state = findViewById(R.id.spn_state);
        arr_states = new ArrayList<>();
        arr_states_id = new ArrayList<>();

        spn_state.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                if (spn_state.getSelectedItemPosition() != 0)
                    state_id = arr_states_id.get(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        //Initializing Editexts
        et_first_name = findViewById(R.id.et_first_name);
        et_last_name = findViewById(R.id.et_last_name);
        et_telephone = findViewById(R.id.et_telephone);
        et_city = findViewById(R.id.et_city);
        et_email = findViewById(R.id.et_email);
        et_password = findViewById(R.id.et_password);
        et_confirmPassword = findViewById(R.id.et_cpassword);
        et_address = findViewById(R.id.et_address);

        spn_country = findViewById(R.id.spn_country);
        arr_countries = new ArrayList<>();
        arr_countries_id = new ArrayList<>();
        spn_country.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

                country_id = arr_countries_id.get(i);
                if (!country_id.equals("0"))
                    getAllStates(country_id);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        spn_customer_group = findViewById(R.id.spn_customer_group);
        arr_customer_group = new ArrayList<>();
        arr_customer_group.add("Select group");
        arr_customer_group.add("Instructor");
        arr_customer_group.add("Student");
        spn_customer_group.setAdapter(new ArrayAdapter<>(SignupActivity.this,
                android.R.layout.simple_spinner_dropdown_item, arr_customer_group));

        spn_customer_group.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                if (spn_customer_group.getSelectedItemPosition() != 0) {

                    customer_group = String.valueOf(spn_customer_group.getSelectedItemPosition());
                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PLACE_PICKER_REQUEST) {
            if (resultCode == RESULT_OK) {

                Place place = PlacePicker.getPlace(data, this);
                lattitude = String.valueOf(place.getLatLng().latitude);
                longitude = String.valueOf(place.getLatLng().longitude);
                et_address.setText(place.getAddress());
            }
        }
    }

    private void getAllCountries() {

        StringRequest req = new StringRequest(Request.Method.GET, Api.Countries_URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        try {
                            spn_country.setTitle("Select Country");
                            arr_countries.add("Select Country");
                            arr_countries_id.add("0");
                            JSONObject jsonObject = new JSONObject(response);
                            JSONArray jsonArray = jsonObject.getJSONArray("data");
                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONObject object = jsonArray.getJSONObject(i);
                                arr_countries_id.add(object.getString("country_id"));
                                arr_countries.add(object.getString("name"));
                            }
                            spn_country.setAdapter(new ArrayAdapter<>(SignupActivity.this,
                                    android.R.layout.simple_spinner_dropdown_item, arr_countries));
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

    private void getAllStates(final String country_id) {

        arr_states.clear();
        arr_states_id.clear();

        StringRequest req = new StringRequest(Request.Method.POST, Api.States_URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        try {
                            spn_state.setTitle("Select State");
                            arr_states.add("Select State");
                            arr_states_id.add("0");
                            JSONObject jsonObject = new JSONObject(response);
                            boolean success = jsonObject.getBoolean("success");
                            if (success) {
                                spn_state.setVisibility(View.VISIBLE);
                                JSONArray jsonArray = jsonObject.getJSONArray("data");
                                for (int i = 0; i < jsonArray.length(); i++) {
                                    JSONObject object = jsonArray.getJSONObject(i);

                                    arr_states_id.add(object.getString("zone_id"));
                                    arr_states.add(object.getString("name"));
                                }
                                spn_state.setAdapter(new ArrayAdapter<>(SignupActivity.this,
                                        android.R.layout.simple_spinner_dropdown_item, arr_states));
                            } else {
                                spn_state.setVisibility(View.GONE);
                                Toast.makeText(getApplicationContext(), jsonObject.getString("data"),
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
                map.put("country_id", country_id);
                return map;
            }
        };

        RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
        requestQueue.add(req);

    }

    public void checkLocationPermission() {

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {

                ActivityCompat.requestPermissions(SignupActivity.this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);


            } else {
                ActivityCompat.requestPermissions(SignupActivity.this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);
            }
        } else {

            PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();
            try {
                startActivityForResult(builder.build(SignupActivity.this), PLACE_PICKER_REQUEST);
            } catch (GooglePlayServicesRepairableException | GooglePlayServicesNotAvailableException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {

        if (requestCode == MY_PERMISSIONS_REQUEST_LOCATION) {
            // If request is cancelled, the result arrays are empty.
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                // permission was granted, yay! Do the
                // location-related task you need to do.
                if (ContextCompat.checkSelfPermission(this,
                        Manifest.permission.ACCESS_FINE_LOCATION)
                        == PackageManager.PERMISSION_GRANTED) {

                    //Request location updates:

                    PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();
                    try {
                        startActivityForResult(builder.build(SignupActivity.this), PLACE_PICKER_REQUEST);
                    } catch (GooglePlayServicesRepairableException | GooglePlayServicesNotAvailableException e) {
                        e.printStackTrace();
                    }
                }

            } else {

                // permission denied, boo! Disable the
                // functionality that depends on this permission.
                Toast.makeText(getApplicationContext(), "Kindly grant location permission",
                        Toast.LENGTH_LONG).show();

            }
        }
    }

    private void validation() {

        String emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";

        if (spn_customer_group.getSelectedItemPosition() == 0) {
            Toast.makeText(getApplicationContext(), "Please select customer group",
                    Toast.LENGTH_LONG).show();
        } else if (TextUtils.isEmpty(et_first_name.getText())) {
            et_first_name.setError("Please enter first name");
            et_first_name.requestFocus();
        } else if (TextUtils.isEmpty(et_last_name.getText())) {
            et_last_name.setError("Please enter last name");
            et_last_name.requestFocus();
        } else if (TextUtils.isEmpty(et_email.getText())) {
            et_email.setError("Please enter email address");
            et_email.requestFocus();
        } else if (!et_email.getText().toString().matches(emailPattern)) {
            et_email.setError("Please enter valid email address");
            et_email.requestFocus();
        } else if (TextUtils.isEmpty(et_telephone.getText())) {
            et_telephone.setError("Please enter telephone number");
            et_telephone.requestFocus();
        } else if (et_telephone.getText().length() < 10 || et_telephone.getText().length() > 11) {
            et_telephone.setError("Please enter valid telephone number");
            et_telephone.requestFocus();
        } else if (spn_country.getSelectedItemPosition() == 0) {
            Toast.makeText(getApplicationContext(), "Please select country",
                    Toast.LENGTH_LONG).show();
        } else if (spn_state.getSelectedItemPosition() == 0) {
            Toast.makeText(getApplicationContext(), "Please select state",
                    Toast.LENGTH_LONG).show();
        } else if (TextUtils.isEmpty(et_city.getText())) {
            et_city.setError("Please enter city");
            et_city.requestFocus();
        } else if (TextUtils.isEmpty(et_address.getText())) {
            Toast.makeText(getApplicationContext(), "Please select address",
                    Toast.LENGTH_LONG).show();
        } else if (TextUtils.isEmpty(et_password.getText())) {
            et_password.setError("Please enter password");
            et_password.requestFocus();
        } else if (et_password.getText().length() < 6){
            et_password.setError("Password must be 6 characters long");
            et_password.requestFocus();

        } else if (TextUtils.isEmpty(et_confirmPassword.getText())) {
            et_confirmPassword.setError("Please enter confirm password");
            et_confirmPassword.requestFocus();
        } else if (!et_password.getText().toString().matches(et_confirmPassword.getText().toString())) {
            Toast.makeText(getApplicationContext(), "Password not match",
                    Toast.LENGTH_LONG).show();
        } else {
            registerUserInFirebase();
        }
    }

    private void registerUserInFirebase(){

        loader.show();

        mAuth.createUserWithEmailAndPassword(et_email.getText().toString(), et_password.getText().toString()).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {

                if (task.isSuccessful()) {

                    databaseReference = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUser.getUid());

                    HashMap<String, String> userMap = new HashMap<>();
                    userMap.put("role", customer_group);
                    userMap.put("firstname", et_first_name.getText().toString());
                    userMap.put("lastname", et_last_name.getText().toString());
                    userMap.put("email", et_email.getText().toString());
                    userMap.put("telephone", et_telephone.getText().toString());
                    userMap.put("address", et_address.getText().toString());
                    userMap.put("latitude", lattitude);
                    userMap.put("longitude", longitude);
                    userMap.put("city", et_city.getText().toString());
                    userMap.put("device_token", FirebaseInstanceId.getInstance().getToken());

                    databaseReference.setValue(userMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {

                            if (task.isSuccessful()) {
                                registerUser();
                            }
                        }
                    });

                } else {
                    loader.dismiss();
                    Toast.makeText(getApplicationContext(), task.getException().toString(),
                            Toast.LENGTH_LONG).show();
                    Log.d("FirebaseError", task.getException().toString());
                }
            }
        });


    }

    private void registerUser() {

        StringRequest req = new StringRequest(Request.Method.POST, Api.Signup_URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            boolean status = jsonObject.getBoolean("success");
                            if (status) {
                                loader.dismiss();
                                Toast.makeText(getApplicationContext(), "Please Login to continue",
                                        Toast.LENGTH_LONG).show();
                                startActivity(new Intent(getApplicationContext(), LoginActivity.class));
                                finish();

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
                map.put("customer_group_id", customer_group);
                map.put("firstname", et_first_name.getText().toString());
                map.put("lastname", et_last_name.getText().toString());
                map.put("email", et_email.getText().toString());
                map.put("telephone", et_telephone.getText().toString());
                map.put("address_1", et_address.getText().toString());
                map.put("latitude", lattitude);
                map.put("longitude", longitude);
                map.put("city", et_city.getText().toString());
                map.put("country_id", country_id);
                map.put("zone_id", state_id);
                map.put("password", et_password.getText().toString());
                map.put("firebase_id", currentUser.getUid());
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

            validation();
        } else if (networkInfo == null) {
            Toast.makeText(getApplicationContext(),R.string.no_internet_msg,
                    Toast.LENGTH_LONG).show();
        }

    }

    @Override
    public void onClick(View view) {

        if (view.getId() == R.id.btn_getLocation) {
            checkLocationPermission();
        } else if (view.getId() == R.id.btn_signup) {
            isNetworkAvailable();
        }
    }
}

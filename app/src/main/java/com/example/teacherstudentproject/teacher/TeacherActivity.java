package com.example.teacherstudentproject.teacher;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
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
import com.example.teacherstudentproject.welcome.WelcomeActivity;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.kaopiz.kprogresshud.KProgressHUD;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class TeacherActivity extends AppCompatActivity implements View.OnClickListener {

    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    //Loader
    private KProgressHUD loader;

    //location
    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;
    private int PLACE_PICKER_REQUEST = 1;
    private String lattitude = "", longitude = "";

    private EditText et_first_name, et_last_name, et_email, et_telephone, et_address, et_city;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teacher);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowTitleEnabled(false);

        sharedPreferences = getSharedPreferences("MyPre", MODE_PRIVATE);

        //Initializing Views
        initViews();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.logout) {

            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.clear();
            editor.apply();

            startActivity(new Intent(getApplicationContext(), WelcomeActivity.class));
            finish();
        } else if (id == R.id.add_subject) {
            startActivity(new Intent(getApplicationContext(), CoursesActivity.class));
            finish();
        } else if (id == R.id.portfolio) {
            startActivity(new Intent(getApplicationContext(), PortfolioDetailsActivity.class));
            finish();
        }

        return true;
    }

    private void initViews() {

        loader = KProgressHUD.create(TeacherActivity.this)
                .setStyle(KProgressHUD.Style.SPIN_INDETERMINATE)
                .setBackgroundColor(R.color.blue)
                .setCancellable(false);

        et_first_name = findViewById(R.id.et_first_name);
        et_last_name = findViewById(R.id.et_last_name);
        et_email = findViewById(R.id.et_email);
        et_telephone = findViewById(R.id.et_telephone);
        et_address = findViewById(R.id.et_address);
        et_city = findViewById(R.id.et_city);

        et_first_name.setText(sharedPreferences.getString("firstname", ""));
        et_last_name.setText(sharedPreferences.getString("lastname", ""));
        et_email.setText(sharedPreferences.getString("email", ""));
        et_telephone.setText(sharedPreferences.getString("telephone", ""));
        et_address.setText(sharedPreferences.getString("address", ""));
        et_city.setText(sharedPreferences.getString("city", ""));
    }

    private void refreshActivity() {
        Intent intent = getIntent();
        overridePendingTransition(0, 0);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        finish();
        overridePendingTransition(0, 0);
        startActivity(intent);
    }


    private void updateProfile(final String lat, final String lng) {

        loader.show();

        StringRequest req = new StringRequest(Request.Method.POST, Api.UpdateProfile_URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            boolean status = jsonObject.getBoolean("success");
                            if (status) {
                                editor = sharedPreferences.edit();
                                editor.putString("firstname", et_first_name.getText().toString());
                                editor.putString("lastname", et_last_name.getText().toString());
                                editor.putString("email", et_email.getText().toString());
                                editor.putString("telephone", et_telephone.getText().toString());
                                editor.putString("address", et_address.getText().toString());
                                editor.putString("latitude", lat);
                                editor.putString("longitude", lng);
                                editor.putString("city", et_city.getText().toString());
                                editor.apply();

                                refreshActivity();

                                Toast.makeText(getApplicationContext(), "Profile Updated!",
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
                map.put("firstname", et_first_name.getText().toString());
                map.put("lastname", et_last_name.getText().toString());
                map.put("email", et_email.getText().toString());
                map.put("telephone", et_telephone.getText().toString());
                map.put("address_id", sharedPreferences.getString("address_id", ""));
                map.put("address_1", et_address.getText().toString());
                map.put("latitude", lat);
                map.put("longitude", lng);
                map.put("city", et_city.getText().toString());
                map.put("country_id", sharedPreferences.getString("country_id", ""));
                map.put("zone_id", sharedPreferences.getString("zone_id", ""));
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

                ActivityCompat.requestPermissions(TeacherActivity.this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);


            } else {
                ActivityCompat.requestPermissions(TeacherActivity.this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);
            }
        } else {

            PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();
            try {
                startActivityForResult(builder.build(TeacherActivity.this), PLACE_PICKER_REQUEST);
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
                        startActivityForResult(builder.build(TeacherActivity.this), PLACE_PICKER_REQUEST);
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

    private void isNetworkAvailable() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        // if no network is available networkInfo will be null, otherwise check if we are connected
        if (networkInfo != null && networkInfo.isConnected()) {

            if (lattitude.equals("") && longitude.equals("")) {
                String lat = sharedPreferences.getString("latitude", "");
                String lng = sharedPreferences.getString("longitude", "");
                updateProfile(lat, lng);
            } else {
                updateProfile(lattitude, longitude);
            }
        } else if (networkInfo == null) {
            Toast.makeText(getApplicationContext(), R.string.no_internet_msg,
                    Toast.LENGTH_LONG).show();
        }

    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {

            case R.id.tv_change_password:
                startActivity(new Intent(getApplicationContext(), ChangePasswordActivity.class));
                //finish();
                break;

            case R.id.btn_getLocation:
                checkLocationPermission();
                break;

            case R.id.btn_update_profile:
                isNetworkAvailable();
                break;

        }
    }
}

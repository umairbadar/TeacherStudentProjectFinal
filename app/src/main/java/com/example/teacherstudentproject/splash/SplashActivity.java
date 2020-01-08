package com.example.teacherstudentproject.splash;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;

import com.example.teacherstudentproject.student.SelectCoursesActivity;
import com.example.teacherstudentproject.teacher.TeacherActivity;
import com.example.teacherstudentproject.R;
import com.example.teacherstudentproject.welcome.WelcomeActivity;

public class SplashActivity extends AppCompatActivity {

    private boolean saveLogin;
    private String customerGroup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        SharedPreferences sharedPreferences = getSharedPreferences("MyPre", MODE_PRIVATE);
        saveLogin = sharedPreferences.getBoolean("saveLogin", false);
        customerGroup = sharedPreferences.getString("customer_group", "");

        int SPLASH_DISPLAY_LENGTH = 2000;
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {

                if (saveLogin) {

                    if (customerGroup.equals("1")) {

                        startActivity(new Intent(getApplicationContext(), TeacherActivity.class));
                        finish();
                    } else {
                        startActivity(new Intent(getApplicationContext(), SelectCoursesActivity.class));
                        finish();
                    }
                } else {

                    startActivity(new Intent(getApplicationContext(), WelcomeActivity.class));
                    finish();
                }
            }
        }, SPLASH_DISPLAY_LENGTH);
    }
}

package com.example.teacherstudentproject.welcome;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.example.teacherstudentproject.login.LoginActivity;
import com.example.teacherstudentproject.R;
import com.example.teacherstudentproject.signup.SignupActivity;

public class WelcomeActivity extends AppCompatActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);
    }

    @Override
    public void onClick(View view) {

        switch(view.getId()){

            case R.id.btn_login:
                startActivity(new Intent(getApplicationContext(), LoginActivity.class));
                finish();
                break;

            case R.id.tv_signup:
                startActivity(new Intent(getApplicationContext(), SignupActivity.class));
                finish();
                break;
        }
    }
}

package com.example.simplenotesapp.activitys;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.example.simplenotesapp.MyApplication;
import com.example.simplenotesapp.R;
import com.example.simplenotesapp.activity_manager.AuthManager;

public class SplashScreen extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_splash_screen);


        MyApplication app = (MyApplication) getApplication();
        AuthManager authManager = app.getAuthManager();

        new Handler(Looper.getMainLooper()).postDelayed(() -> {

            if(authManager.isRememberMe()) {
                Intent intent = new Intent(SplashScreen.this, MainActivity.class);
                startActivity(intent);
                finish();
            }

            else{
                Intent intent = new Intent(SplashScreen.this, AuthScreen.class);
                startActivity(intent);
                finish();
            }

        }, 2000);



    }
}
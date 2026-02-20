package com.example.simplenotesapp.activitys;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.example.simplenotesapp.R;
import com.example.simplenotesapp.activity_manager.AuthManager;

public class SplashScreen extends AppCompatActivity {

    AuthManager authManager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SharedPreferences prefs = getSharedPreferences("userPrefs", MODE_PRIVATE);
        boolean isDark = prefs.getBoolean("is_dark_mode", false);
        AppCompatDelegate.setDefaultNightMode(isDark ?
                AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO);

        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_splash_screen);


        authManager = new AuthManager(this);

        new Handler(Looper.getMainLooper()).postDelayed(() -> {

            if(authManager.Y_remember_me()) {
                Intent intent = new Intent(SplashScreen.this, MainNotesActivity.class);
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
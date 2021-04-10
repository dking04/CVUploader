package com.example.cv_uploader.Activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.view.Window;
import android.view.WindowManager;

import com.example.cv_uploader.R;
import com.google.firebase.FirebaseApp;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        FirebaseApp.initializeApp(this);

        Window window = getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(ContextCompat.getColor(this,R.color.white));
        SharedPreferences sharedPreferences = getSharedPreferences("settings",MODE_PRIVATE);
        boolean logged = sharedPreferences.getBoolean("logged",false);
        new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    if(!logged) {
                        Intent intent = new Intent(MainActivity.this, Sign_In.class);
                        startActivity(intent);
                        finish();
                    }else {
                        Intent intent = new Intent(MainActivity.this, Dashboard.class);
                        startActivity(intent);
                        finish();
                    }
                }
            },2000);


    }
}
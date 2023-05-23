package com.codz.okah.school_grades;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Window;

import com.codz.okah.school_grades.admin.MainNavigation;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class SplashScreen extends AppCompatActivity {
    DatabaseReference reference;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        Window window = getWindow();

        reference = FirebaseDatabase.getInstance().getReference();

        // Set the status bar color
        window.setStatusBarColor(getResources().getColor(R.color.aqua_dark));

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent i;
                FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
                if (currentUser != null) {
                    Log.d("LOGIN_USER", "connected: "+currentUser.getUid());
                    i = new Intent(SplashScreen.this, MainNavigation.class);
                } else {
                    i = new Intent(SplashScreen.this, Login.class);
                }
//                i = new Intent(SplashScreen.this, Login.class);
                startActivity(i);
                finish();
            }
        }, 1500);



    }
}
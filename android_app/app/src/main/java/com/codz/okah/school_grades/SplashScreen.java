package com.codz.okah.school_grades;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Window;
import android.widget.Toast;

import com.codz.okah.school_grades.admin.MainNavigation;
import com.codz.okah.school_grades.admin.ProfHome;
import com.codz.okah.school_grades.admin.ScolarityHome;
import com.codz.okah.school_grades.tools.Const;
import com.codz.okah.school_grades.tools.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

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
                    checkUser();
                } else {
                    i = new Intent(SplashScreen.this, Login.class);
                    startActivity(i);
                    finish();
                }

            }
        }, 1500);



    }

    private void checkUser() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        FirebaseDatabase.getInstance().getReference().child("users/data/"+user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User userData;
                if(snapshot.hasChildren()){
                    Log.d("EXCEL_ERR", "im in");
                    userData = new User(
                            snapshot.child("username").getValue(String.class),
                            snapshot.child("user_type").getValue(Integer.class),
                            snapshot.child("fullname").getValue(String.class),
                            snapshot.child("depart_key").getValue(String.class)
                    );
                    userData.setKey(user.getUid());


                    switch (userData.getUserType()){
                        case Const.PROF:
                            startActivity(new Intent(SplashScreen.this, ProfHome.class));
                            finish();
                            break;
                        case Const.STUDENT:
                            startActivity(new Intent(SplashScreen.this, Home.class));
                            finish();
                            break;
                        case Const.SCOLARITY:
                            startActivity(new Intent(SplashScreen.this, ScolarityHome.class));
                            finish();
                            break;
                        default:
                            startActivity(new Intent(SplashScreen.this, MainNavigation.class));
                            finish();
                            break;

                    }
                }else{

                    startActivity(new Intent(SplashScreen.this, MainNavigation.class));
                    finish();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}
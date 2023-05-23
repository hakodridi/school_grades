package com.codz.okah.school_grades;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.widget.TextView;
import android.widget.Toast;

import com.codz.okah.school_grades.admin.MainNavigation;
import com.codz.okah.school_grades.tools.Const;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;


public class Login extends AppCompatActivity {
    TextView usernameED, passwordED;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        Window window = getWindow();
        // Set the status bar color
        window.setStatusBarColor(getResources().getColor(R.color.aqua_dark));

        usernameED = findViewById(R.id.email_input);
        passwordED = findViewById(R.id.password_input);

        findViewById(R.id.loginBtn).setOnClickListener(v -> connect());
    }




    private void connect() {
        String email = usernameED.getText().toString().toLowerCase();
        String password = passwordED.getText().toString();

        if(email.isEmpty()){
            usernameED.setError("Check this field!");
            usernameED.requestFocus();
            return;
        }

        if(password.isEmpty()){
            passwordED.setError("Check this field!");
            passwordED.requestFocus();
            return;
        }

        email += Const.EMAIL_COMPLETE;

        FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    // Login successful
                    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                    String uid = user.getUid();
                    Log.d("LOGIN_USER", "user_id: "+uid);
                    startActivity(new Intent(Login.this, MainNavigation.class));
                    finish();
                } else {
                    // Login failed
                    Exception exception = task.getException();
                    Toast.makeText(Login.this, "check yout inputs", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
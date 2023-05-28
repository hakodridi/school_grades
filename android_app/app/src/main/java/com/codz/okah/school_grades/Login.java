package com.codz.okah.school_grades;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.TextView;
import android.widget.Toast;

import com.codz.okah.school_grades.admin.MainNavigation;
import com.codz.okah.school_grades.admin.ProfHome;
import com.codz.okah.school_grades.tools.Const;
import com.codz.okah.school_grades.tools.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


public class Login extends AppCompatActivity {
    TextView usernameED, passwordED;
    @SuppressLint("MissingInflatedId")
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

        findViewById(R.id.forgot).setOnClickListener(v->{
            AlertDialog.Builder builder = new AlertDialog.Builder(Login.this);
            builder.setTitle("Info");
            builder.setMessage("This service is not available at the moment. please contact the admin");
            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    // Handle the button click event
                    dialog.dismiss();
                }
            });
            AlertDialog dialog = builder.create();
            dialog.show();
        });
    }




    private void connect() {

        String email = usernameED.getText().toString().toLowerCase().trim();
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
        findViewById(R.id.progress).setVisibility(View.VISIBLE);

        FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    // Login successful

                    checkUser();
                } else {
                    // Login failed
                    Exception exception = task.getException();
                    Toast.makeText(Login.this, "check yout inputs", Toast.LENGTH_SHORT).show();
                    findViewById(R.id.progress).setVisibility(View.GONE);
                }
            }
        });




    }

    private void checkUser() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        FirebaseDatabase.getInstance().getReference().child("users/data/"+user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User userData;
                if(snapshot.hasChildren()){
                    userData = new User(
                            snapshot.child("username").getValue(String.class),
                            snapshot.child("user_type").getValue(Integer.class),
                            snapshot.child("fullname").getValue(String.class),
                            snapshot.child("depart_key").getValue(String.class)
                    );
                    userData.setKey(user.getUid());

                    switch (userData.getUserType()){
                        case Const.PROF:
                            startActivity(new Intent(Login.this, ProfHome.class));
                            finish();
                            break;
                        case Const.STUDENT:
                            startActivity(new Intent(Login.this, Home.class));
                            finish();
                            break;
                        case Const.SCOLARITY:
                            break;
                        default:
                            startActivity(new Intent(Login.this, MainNavigation.class));
                            finish();
                            break;

                    }
                }else{

                    startActivity(new Intent(Login.this, MainNavigation.class));
                    finish();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}
package com.codz.okah.school_grades.admin;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.TextView;
import android.widget.Toast;


import com.codz.okah.school_grades.Home;
import com.codz.okah.school_grades.Login;
import com.codz.okah.school_grades.R;

import com.codz.okah.school_grades.adapters.AdsAdapter;
import com.codz.okah.school_grades.adapters.SpinnerAdapter;
import com.codz.okah.school_grades.tools.Ad;
import com.codz.okah.school_grades.tools.Const;
import com.codz.okah.school_grades.tools.Functions;
import com.codz.okah.school_grades.tools.Item;
import com.codz.okah.school_grades.tools.Module;
import com.codz.okah.school_grades.tools.User;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;

public class ScolarityHome extends AppCompatActivity {

    View empty;
    View progress;
    DatabaseReference reference;
    ArrayList<Ad> ads;
    AdsAdapter adapter;

    RecyclerView listView;

    User userData;
    FirebaseUser currentUser;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scolarity_home);
        Window window = getWindow();
        // Set the status bar color
        window.setStatusBarColor(getResources().getColor(R.color.aqua_dark));

        @SuppressLint({"LocalSuppress"}) Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        empty = findViewById(R.id.empty);
        progress = findViewById(R.id.progress);
        listView = findViewById(R.id.listView);

        reference = FirebaseDatabase.getInstance().getReference();
        ads = new ArrayList<>();
        adapter = new AdsAdapter(ads);
        listView.setAdapter(adapter);

        userData = null;
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        loadUserData();


        findViewById(R.id.floatingBtn).setOnClickListener(v->{
            openDialog();
        });





    }



    private void loadUserData() {
        showProgress();
        reference.child("users/data").child(currentUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(!snapshot.hasChildren())return;

                userData = new User(
                        snapshot.child("username").getValue(String.class),
                        snapshot.child("user_type").getValue(Integer.class),
                        snapshot.child("fullname").getValue(String.class),
                        snapshot.child("depart_key").getValue(String.class)
                );
                userData.setKey(snapshot.getKey());

                loadAds();


            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ScolarityHome.this, "Error", Toast.LENGTH_SHORT).show();
                hideProgress();
            }
        });
    }


    @SuppressLint("MissingInflatedId")
    private void openDialog() {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(ScolarityHome.this);

        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_add_ad, null);
        dialogBuilder.setView(dialogView);

        AlertDialog alertDialog = dialogBuilder.create();



        dialogView.findViewById(R.id.cancelBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
            }
        });

        dialogView.findViewById(R.id.confirmBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addAdFromDialog(alertDialog, ((TextInputEditText)dialogView.findViewById(R.id.input_text)));
            }
        });

        alertDialog.show();
    }


    private void addAdFromDialog(AlertDialog alertDialog, TextInputEditText inputED) {
        String inputText = inputED.getText().toString().trim();
        if (inputText.isEmpty()){
            inputED.setError("Check this");
            inputED.requestFocus();
            return;
        }

        Ad ad = new Ad(inputText, Functions.getCurrentDateTime());

        alertDialog.dismiss();
        showProgress();



        reference.child("ads/"+userData.getDepartKey()).push().setValue(ad, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                hideProgress();
                if (databaseError != null) {
                    Toast.makeText(ScolarityHome.this, "Error", Toast.LENGTH_SHORT).show();
                    alertDialog.show();
                    ((TextInputEditText)alertDialog.findViewById(R.id.input_text)).setText(inputText);
                } else {
                    Toast.makeText(ScolarityHome.this, "Added", Toast.LENGTH_SHORT).show();
                    loadAds();
                    Functions.pushNotif(ScolarityHome.this, userData.getDepartKey(), "Scolarity", inputText);

                }
            }
        });


    }



    private void loadAds() {
        showProgress();
        reference.child("ads/"+userData.getDepartKey()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshots) {
                ads = new ArrayList<>();
                for(DataSnapshot snapshot: snapshots.getChildren()){
                    ads.add(snapshot.getValue(Ad.class));
                }

                Collections.sort(ads, new Comparator<Ad>() {
                    SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy hh:mm");

                    @Override
                    public int compare(Ad ad1, Ad ad2) {
                        try {
                            Date date1 = dateFormat.parse(ad1.getDate());
                            Date date2 = dateFormat.parse(ad2.getDate());
                            return date2.compareTo(date1);
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                        return 0;
                    }
                });


                adapter.setAds(ads);
                hideProgress();

                empty.setVisibility(ads.isEmpty()?View.VISIBLE:View.GONE);


            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ScolarityHome.this, "Error", Toast.LENGTH_SHORT).show();
                hideProgress();
            }
        });
    }





    private void showProgress() {
        progress.setVisibility(View.VISIBLE);
    }

    private void hideProgress() {
        progress.setVisibility(View.GONE);
    }




    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_logout) {
            confirmLogout();
            return true;
        }


        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.logout_menu, menu);
        return true;
    }

    private void confirmLogout() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Logout");
        builder.setMessage("Are you sure you want to logout?");
        builder.setPositiveButton("Logout", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // User confirmed logout, perform the logout operation
                logout();
            }
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void logout(){
        FirebaseAuth.getInstance().signOut();
        startActivity(new Intent(ScolarityHome.this, Login.class));
        finish();
    }



}
package com.codz.okah.school_grades;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.codz.okah.school_grades.R;
import com.codz.okah.school_grades.adapters.GradeAdapter;
import com.codz.okah.school_grades.adapters.SpinnerAdapter;
import com.codz.okah.school_grades.admin.ProfHome;
import com.codz.okah.school_grades.tools.Grade;
import com.codz.okah.school_grades.tools.Item;
import com.codz.okah.school_grades.tools.Module;
import com.codz.okah.school_grades.tools.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class Home extends AppCompatActivity {

    View empty;
    RecyclerView listView;
    ArrayList<User> students;
    DatabaseReference reference;
    GradeAdapter adapter;

    Spinner moduleSpinner;
    ArrayAdapter moduleAdapter;
    String selectedModuleKey;
    int selectedModulePosition;

    FirebaseUser currentUser;
    User userData;

    ArrayList<Module> modules;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        Window window = getWindow();
        // Set the status bar color
        window.setStatusBarColor(getResources().getColor(R.color.aqua_dark));

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        empty = findViewById(R.id.empty);
        listView = findViewById(R.id.listView);
        reference = FirebaseDatabase.getInstance().getReference();

        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser==null)logout();


        selectedModuleKey = "";
        selectedModulePosition = 0;
        moduleSpinner = findViewById(R.id.module_spinner);
        moduleAdapter = new SpinnerAdapter(this,android.R.layout.simple_spinner_item,new String[]{
                "Select a choice"
        });
        moduleAdapter.setDropDownViewResource(R.layout.spinner_item);
        moduleSpinner.setAdapter(moduleAdapter);

        students = new ArrayList<>();
        adapter = new GradeAdapter(this, new ArrayList<>());
        listView.setAdapter(adapter);

        moduleSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if(position<=0)return;

                selectedModulePosition = position-1;
                selectedModuleKey = modules.get(position-1).getModule().getKey();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        loadUserData();

        findViewById(R.id.confirmFilterBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(moduleSpinner.getSelectedItemPosition()==0 )return;
                showModuleInfo();
                findViewById(R.id.chooseLayout).setVisibility(View.GONE);
                loadStudents();
            }
        });

    }


    private void loadUserData() {
        showProgress();
        reference.child("users/data")
                .child(currentUser.getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        userData = new User(
                                snapshot.child("username").getValue(String.class),
                                snapshot.child("user_type").getValue(Integer.class),
                                snapshot.child("fullname").getValue(String.class),
                                snapshot.child("depart_key").getValue(String.class)
                        );
                        userData.setKey(snapshot.getKey());
                        userData.setGroup(snapshot.child("group").getValue(Integer.class));
                        userData.setSectionKey(snapshot.child("section_key").getValue(String.class));
                        userData.setSpecialityKey(snapshot.child("speciality_key").getValue(String.class));
                        Log.d("EXCEL", "onDataChange: userDATA : "+snapshot.child("speciality_key").getValue(String.class));
                        loadModules();
                        hideProgress();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        hideProgress();
                        Toast.makeText(Home.this, "Error", Toast.LENGTH_SHORT).show();
                        Log.e("EXP_ERR", "onCancelled: "+error.getMessage()+"\n"+error.getDetails() );
                    }
                });
    }

    private void loadStudents(){
        showProgress();
        reference.child("users/students/"+modules.get(selectedModulePosition).getDepartKey()+"/"+userData.getSectionKey()+"/"+userData.getGroup()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                students = new ArrayList<>();
                for (DataSnapshot childSnapshot : dataSnapshot.getChildren()) {

                    User u = new User(
                            childSnapshot.child("username").getValue(String.class),
                            childSnapshot.child("user_type").getValue(Integer.class),
                            childSnapshot.child("fullname").getValue(String.class),
                            childSnapshot.child("depart_key").getValue(String.class)
                    );
                    u.setKey(childSnapshot.getKey());
                    u.setGroup(childSnapshot.child("group").getValue(Integer.class));
                    u.setSectionKey(childSnapshot.child("section_key").getValue(String.class));
                    students.add(u);

                    loadGrades();

                }


                hideProgress();
                empty.setVisibility(students.isEmpty()?View.VISIBLE:View.GONE);

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                hideProgress();
                Toast.makeText(Home.this, "Error", Toast.LENGTH_SHORT).show();
                Log.e("EXP_ERR", "onCancelled: "+databaseError.getMessage()+"\n"+databaseError.getDetails() );
                // Handle database error
            }
        });
    }

    private void loadModules(){
        showProgress();
        reference.child("struct/modules/"+userData.getSpecialityKey()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                modules = new ArrayList<>();

                ArrayList<String> list = new ArrayList<>();
                list.add("Select a choice");


                for (DataSnapshot childSnapshot : dataSnapshot.getChildren()) {
                    Item item = new Item(
                            childSnapshot.getKey(),
                            childSnapshot.child("name").getValue(String.class)
                    );
                    item.setHasTP(childSnapshot.child("has_tp").getValue(Boolean.class));
                    if(childSnapshot.hasChild("prof_id")){
                        item.setProfID(childSnapshot.child("prof_id").getValue(String.class));
                    }
                    modules.add(new Module(item, "", userData.getDepartKey(), userData.getSpecialityKey()));
                    list.add(childSnapshot.child("name").getValue(String.class));
                }

                hideProgress();
                if(modules.isEmpty()){
                    empty.setVisibility(View.VISIBLE);
                    findViewById(R.id.chooseLayout).setVisibility(View.GONE);
                }


                moduleAdapter = new SpinnerAdapter(
                        Home.this,
                        R.layout.spinner_item,
                        list.toArray(new String[list.size()])){
                    @Override
                    public boolean isEnabled(int position) {
                        // Disable the hint item
                        return position != 0;
                    }
                };
                moduleSpinner.setAdapter(moduleAdapter);
                moduleSpinner.setSelection(0);

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                hideProgress();
                Log.e("EXP_ERR", "onCancelled: "+databaseError.getMessage()+"\n"+databaseError.getDetails() );
                // Handle database error
            }
        });
    }


    private void loadGrades(){
        showProgress();
        reference.child("grades")
                .child(selectedModuleKey)
                .child(userData.getSectionKey())
                .child(""+userData.getGroup())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                            int index = getPositionOfStudent(snapshot.getKey());
                            if(index>-1){

                                String td = snapshot.child("td").getValue(String.class);
                                String tp = snapshot.child("tp").getValue(String.class);
                                String exam = snapshot.child("exam").getValue(String.class);
                                Grade grades = new Grade(exam,td,tp);
                                students.get(index).setGrades(grades);
                            }
                        }

                        adapter.setStudents(students);
                        listView.setAdapter(adapter);


                        hideProgress();

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        hideProgress();
                        Toast.makeText(Home.this, "Error", Toast.LENGTH_SHORT).show();
                        Log.e("EXP_ERR", "onCancelled: "+databaseError.getMessage()+"\n"+databaseError.getDetails() );
                        // Handle database error
                    }
                });
    }


    private int getPositionOfStudent(String key) {
        for (int i = 0; i < students.size(); i++) {
            if (key.equals(students.get(i).getKey()))return i;
        }
        return -1;
    }







    private void showProgress(){
        findViewById(R.id.progress).setVisibility(View.VISIBLE);
    }

    private void hideProgress(){
        findViewById(R.id.progress).setVisibility(View.GONE);
    }


    private void showModuleInfo() {
        ((TextView)findViewById(R.id.module_name)).setText(modules.get(selectedModulePosition).getModule().getValue());
        if(modules.get(selectedModulePosition).getModule().isHasTP()){
            findViewById(R.id.tpTitle).setVisibility(View.VISIBLE);
            adapter.setVisibility(View.VISIBLE);
        }
        else {
            findViewById(R.id.tpTitle).setVisibility(View.GONE);
            adapter.setVisibility(View.GONE);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_logout) {
            confirmLogout();
            return true;
        }

        if (id == R.id.action_filter) {
            findViewById(R.id.chooseLayout).setVisibility(View.VISIBLE);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.prof_menu, menu);
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
        startActivity(new Intent(Home.this, Login.class));
        finish();
    }
}
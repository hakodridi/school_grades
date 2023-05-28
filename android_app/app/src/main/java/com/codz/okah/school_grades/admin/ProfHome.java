package com.codz.okah.school_grades.admin;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.codz.okah.school_grades.Login;
import com.codz.okah.school_grades.R;
import com.codz.okah.school_grades.adapters.EditGradeAdapter;
import com.codz.okah.school_grades.adapters.GradeAdapter;
import com.codz.okah.school_grades.adapters.SpinnerAdapter;
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
import java.util.HashMap;

public class ProfHome extends AppCompatActivity {
    View empty;
    RecyclerView listView, editListView;
    ArrayList<Item> sections;
    ArrayList<User> students;
    DatabaseReference reference;
    GradeAdapter adapter;
    EditGradeAdapter editAdapter;

    Spinner moduleSpinner, sectionSpinner, groupSpinner;
    ArrayAdapter moduleAdapter, sectionAdapter, groupAdapter;

    String selectedModuleKey, selectedSectionKey, selectedGroupKey;

    int selectedModulePosition;
    int selectedSectionPosition;
    int selectedGroupPosition;

    FirebaseUser currentUser;

    ArrayList<Module> modules;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_prof_home);
        Window window = getWindow();
        // Set the status bar color
        window.setStatusBarColor(getResources().getColor(R.color.aqua_dark));

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        findViewById(R.id.group_spinner_layout).setVisibility(View.GONE);

        empty = findViewById(R.id.empty);
        listView = findViewById(R.id.listView);
        editListView = findViewById(R.id.editListView);
        reference = FirebaseDatabase.getInstance().getReference();

        currentUser = FirebaseAuth.getInstance().getCurrentUser();


        selectedModuleKey = "";
        selectedSectionKey = "";
        selectedGroupKey = "";

        selectedModulePosition = 0;
        selectedSectionPosition = 0;
        selectedGroupPosition = 0;

        moduleSpinner = findViewById(R.id.module_spinner);
        sectionSpinner = findViewById(R.id.section_spinner);
        groupSpinner = findViewById(R.id.group_spinner);

        sections = new ArrayList<>();


        moduleAdapter = new SpinnerAdapter(this,android.R.layout.simple_spinner_item,new String[]{
                "Select a choice"
        });
        sectionAdapter = new SpinnerAdapter(this,android.R.layout.simple_spinner_item,new String[]{
                "Select a choice"
        });
        groupAdapter = new SpinnerAdapter(this,android.R.layout.simple_spinner_item,new String[]{
                "Select a choice"
        });

        moduleAdapter.setDropDownViewResource(R.layout.spinner_item);
        sectionAdapter.setDropDownViewResource(R.layout.spinner_item);
        groupAdapter.setDropDownViewResource(R.layout.spinner_item);


        moduleSpinner.setAdapter(moduleAdapter);
        sectionSpinner.setAdapter(sectionAdapter);
        groupSpinner.setAdapter(groupAdapter);

        students = new ArrayList<>();

        adapter = new GradeAdapter(this, new ArrayList<>());
        listView.setAdapter(adapter);

        editAdapter = new EditGradeAdapter(this, new ArrayList<>());
        editListView.setAdapter(editAdapter);

        findViewById(R.id.floatingEditBtn).setOnClickListener(v->{
            showEditeLayout();
        });

        findViewById(R.id.cancelEditBtn).setOnClickListener(v->{
            hideEditeLayout();
        });


        loadUserData();

        findViewById(R.id.confirmFilterBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(selectedGroupPosition==0 )return;
                showModuleInfo();
                findViewById(R.id.chooseLayout).setVisibility(View.GONE);
                loadStudents();
            }
        });

        findViewById(R.id.confirmEditBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uploadGrades();
            }
        });



    }

    private void uploadGrades() {
        HashMap<String, Object> map = new HashMap<>();
        for (int i = 0; i < students.size(); i++) {
            View view = editListView.getChildAt(i);
            EditText tdED = view.findViewById(R.id.td);
            EditText tpED = view.findViewById(R.id.tp);
            EditText examED = view.findViewById(R.id.exam);
            HashMap<String, Object> userMap = new HashMap<>();

            userMap.put("td", tdED.getText().toString());
            userMap.put("tp", tpED.getText().toString());
            userMap.put("exam", examED.getText().toString());

            map.put(students.get(i).getKey(), userMap);
        }
        showProgress();
        reference.child("grades")
                .child(selectedModuleKey)
                .child(selectedSectionKey)
                .child(selectedGroupKey).setValue(map, new DatabaseReference.CompletionListener() {
                    @Override
                    public void onComplete(@Nullable DatabaseError error, @NonNull DatabaseReference ref) {
                        hideProgress();
                        if (error != null) {
                            Toast.makeText(ProfHome.this, "Error", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(ProfHome.this, "Grades added", Toast.LENGTH_SHORT).show();
                            loadStudents();
                        }
                    }
                });

    }

    private void loadUserData() {
        showProgress();
        reference.child("prof_modules")
                .child(currentUser.getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshots) {
                        modules = new ArrayList<>();
                        ArrayList<String> list = new ArrayList<>();
                        list.add("Select a choice");

                        for (DataSnapshot snapshot : snapshots.getChildren()){
                            Item item = snapshot.child("module").getValue(Item.class);
                            Module module = new Module(
                                    item,
                                    snapshot.child("fac_key").getValue(String.class),
                                    snapshot.child("depart_key").getValue(String.class),
                                    snapshot.child("speciality_key").getValue(String.class)
                                    );
                            modules.add(module);
                            list.add(item.getValue());
                        }
                        hideProgress();
                        if(modules.isEmpty()){
                            empty.setVisibility(View.VISIBLE);
                            findViewById(R.id.chooseLayout).setVisibility(View.GONE);
                        }


                        moduleAdapter = new SpinnerAdapter(
                                ProfHome.this,
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
                        init();


                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(ProfHome.this, "Error", Toast.LENGTH_SHORT).show();
                    }
                });

    }

    private void init(){
        moduleSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if(position<=0){
                    findViewById(R.id.section_spinner_layout).setVisibility(View.GONE);
                    selectedSectionPosition = 0;
                    selectedSectionKey = "";
                    return;
                }
                findViewById(R.id.section_spinner_layout).setVisibility(View.VISIBLE);
                selectedModulePosition = position-1;
                selectedModuleKey = modules.get(position-1).getModule().getKey();
                loadSections(position-1);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        sectionSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if(position<=0){
                    findViewById(R.id.group_spinner_layout).setVisibility(View.GONE);
                    selectedGroupPosition = 0;
                    selectedGroupKey = "";
                    return;
                }
                findViewById(R.id.group_spinner_layout).setVisibility(View.VISIBLE);
                selectedSectionPosition = parent.getSelectedItemPosition();
                selectedSectionKey = sections.get(position-1).getKey();


                ArrayList<String> groups = new ArrayList<>();
                groups.add("Select a choice");
                for (int i = 0; i < sections.get(position-1).getNumber(); i++) {
                    groups.add(String.valueOf(i+1));
                }

                hideProgress();
                groupAdapter = new SpinnerAdapter(
                        ProfHome.this,
                        R.layout.spinner_item,
                        groups.toArray(new String[groups.size()])){
                    @Override
                    public boolean isEnabled(int position) {
                        // Disable the hint item
                        return position != 0;
                    }
                };
                groupSpinner.setAdapter(groupAdapter);
                groupSpinner.setSelection(0);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        groupSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if(position<=0)return;
                selectedGroupPosition = parent.getSelectedItemPosition();
                selectedGroupKey = ""+position;

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }


    private void loadStudents(){
        showProgress();
        reference.child("users/students/"+modules.get(selectedModulePosition).getDepartKey()+"/"+selectedSectionKey+"/"+selectedGroupKey).addListenerForSingleValueEvent(new ValueEventListener() {
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

                }


                hideProgress();
                loadGrades();
                empty.setVisibility(students.isEmpty()?View.VISIBLE:View.GONE);

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                hideProgress();
                Toast.makeText(ProfHome.this, "Error", Toast.LENGTH_SHORT).show();
                Log.e("EXP_ERR", "onCancelled: "+databaseError.getMessage()+"\n"+databaseError.getDetails() );
                // Handle database error
            }
        });
    }


    private void loadGrades(){
        showProgress();
        reference.child("grades")
                .child(selectedModuleKey)
                .child(selectedSectionKey)
                .child(selectedGroupKey)
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

                editAdapter.setStudents(students);
                editListView.setAdapter(editAdapter);

                hideProgress();

                hideEditeLayout();

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                hideProgress();
                Toast.makeText(ProfHome.this, "Error", Toast.LENGTH_SHORT).show();
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

    private void loadSections(int position) {
        showProgress();
        reference.child("struct/sections/"+modules.get(position).getSpecialityKey()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                sections = new ArrayList<>();
                ArrayList<String> list = new ArrayList<>();
                list.add("Select a choice");
                for (DataSnapshot childSnapshot : dataSnapshot.getChildren()) {
                    sections.add(new Item(
                            childSnapshot.getKey(),
                            childSnapshot.child("name").getValue(String.class)
                    ));
                    list.add(childSnapshot.child("name").getValue(String.class));
                    sections.get(sections.size()-1).setNumber(childSnapshot.child("number_of_groups").getValue(Integer.class));
                }


                sectionAdapter = new SpinnerAdapter(
                        ProfHome.this,
                        R.layout.spinner_item,
                        list.toArray(new String[list.size()])){
                    @Override
                    public boolean isEnabled(int position) {
                        // Disable the hint item
                        return position != 0;
                    }
                };
                sectionSpinner.setAdapter(sectionAdapter);
                sectionSpinner.setSelection(0);

                hideProgress();

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                findViewById(R.id.progress).setVisibility(View.GONE);
                Toast.makeText(ProfHome.this, "Error", Toast.LENGTH_SHORT).show();
                // Handle database error
            }
        });
    }


    private void showProgress(){
        findViewById(R.id.progress).setVisibility(View.VISIBLE);
    }

    private void hideProgress(){
        findViewById(R.id.progress).setVisibility(View.GONE);
    }


    private void hideEditeLayout(){
        findViewById(R.id.edit_grades_layout).setVisibility(View.GONE);
        findViewById(R.id.floatingEditBtn).setVisibility(View.VISIBLE);
    }

    private void showEditeLayout(){
        editAdapter.setStudents(students);
        editListView.setAdapter(editAdapter);
        editAdapter.notifyDataSetChanged();

        findViewById(R.id.edit_grades_layout).setVisibility(View.VISIBLE);
        findViewById(R.id.floatingEditBtn).setVisibility(View.GONE);
    }

    private void showModuleInfo() {
        ((TextView)findViewById(R.id.module_name)).setText(modules.get(selectedModulePosition).getModule().getValue()+
                " > "+sections.get(sectionSpinner.getSelectedItemPosition()-1).getValue()+
                "["+selectedGroupKey+"]");
        if(modules.get(selectedModulePosition).getModule().isHasTP()){
            findViewById(R.id.tpTitle).setVisibility(View.VISIBLE);
            editAdapter.setVisibility(View.VISIBLE);
            adapter.setVisibility(View.VISIBLE);
        }
        else {
            findViewById(R.id.tpTitle).setVisibility(View.GONE);
            editAdapter.setVisibility(View.GONE);
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
        startActivity(new Intent(ProfHome.this, Login.class));
        finish();
    }
}
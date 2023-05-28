package com.codz.okah.school_grades.admin;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.TextView;
import android.widget.Toast;

import com.codz.okah.school_grades.Login;
import com.codz.okah.school_grades.R;
import com.codz.okah.school_grades.admin.fragments.Department;
import com.codz.okah.school_grades.admin.fragments.Faculty;
import com.codz.okah.school_grades.admin.fragments.Grades;
import com.codz.okah.school_grades.admin.fragments.Module;
import com.codz.okah.school_grades.admin.fragments.Prof;
import com.codz.okah.school_grades.admin.fragments.Section;
import com.codz.okah.school_grades.admin.fragments.Speciality;
import com.codz.okah.school_grades.admin.fragments.Student;
import com.codz.okah.school_grades.listener.Progress;
import com.codz.okah.school_grades.tools.Const;
import com.codz.okah.school_grades.tools.User;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Objects;


public class MainNavigation extends AppCompatActivity implements Progress {
    DrawerLayout drawerLayout;
    NavigationView navigationView;
    ActionBarDrawerToggle drawerToggle;
    View progress;

    TextView fullnameTV, usernameTV;

    DatabaseReference ref;
    FirebaseUser currentUser;
    User userData;

    boolean userLoaded;


    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_navigation);
        Window window = getWindow();
        // Set the status bar color
        window.setStatusBarColor(getResources().getColor(R.color.aqua_dark));

        @SuppressLint({"MissingInflatedId", "LocalSuppress"}) Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        userLoaded = false;


        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        drawerToggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.open, R.string.close);
        drawerLayout.addDrawerListener(drawerToggle);
        drawerToggle.syncState();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        navigationView.bringToFront();


        userData = null;

        ref = FirebaseDatabase.getInstance().getReference();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        fullnameTV = navigationView.getHeaderView(0).findViewById(R.id.name);
        usernameTV = navigationView.getHeaderView(0).findViewById(R.id.second_text);




        progress = findViewById(R.id.progress);
        showProgress();

        getUserData(new UserCallback() {
            @Override
            public void onUserLoaded() {
                Fragment fragment;
                switch (userData.getUserType()){
                    case Const.ADMIN_UNIV:
                        navigationView.getMenu().findItem(R.id.menu_grades).setVisible(false);
                        break;
                    case Const.ADMIN_FAC:
                        navigationView.getMenu().findItem(R.id.menu_facs).setVisible(false);

                        navigationView.getMenu().findItem(R.id.menu_departs).setChecked(true);
                        fragment = new Department(MainNavigation.this);
                        getSupportFragmentManager().beginTransaction().replace(R.id.frameLayout,
                                Objects.requireNonNull(fragment)).commit();
                        drawerLayout.closeDrawer(GravityCompat.START);
                        loadFacKeyForFacAdmin();
                        break;
                    case Const.ADMIN_DEPART:
                        navigationView.getMenu().findItem(R.id.menu_facs).setVisible(false);
                        navigationView.getMenu().findItem(R.id.menu_departs).setVisible(false);

                        navigationView.getMenu().findItem(R.id.menu_specialities).setChecked(true);

                        fragment = new Speciality(MainNavigation.this);
                        getSupportFragmentManager().beginTransaction().replace(R.id.frameLayout,
                                Objects.requireNonNull(fragment)).commit();
                        drawerLayout.closeDrawer(GravityCompat.START);
                        loadFacKeyForDepartAdmin();
                        break;
                }
            }
        });

        getSupportFragmentManager().beginTransaction().replace(R.id.frameLayout,
                Objects.requireNonNull(new Faculty(MainNavigation.this))).commit();
        navigationView.setCheckedItem(R.id.menu_facs);

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                Fragment fragment = null;
                switch (item.getItemId()){
                    case R.id.menu_facs:
                        fragment = new Faculty(MainNavigation.this);
                        break;
                    case R.id.menu_departs:
                        fragment = new Department(MainNavigation.this);
                        break;
                    case R.id.menu_specialities:
                        fragment = new Speciality(MainNavigation.this);
                        break;
                    case R.id.menu_groups:
                        fragment = new Section(MainNavigation.this);
                        break;
                    case R.id.menu_modules:
                        fragment = new Module(MainNavigation.this);
                        break;
                    case R.id.menu_profs:
                        fragment = new Prof(MainNavigation.this);
                        break;
                    case R.id.menu_students:
                        fragment = new Student(MainNavigation.this);
                        break;
                    case R.id.menu_grades:
                        fragment = new Grades(MainNavigation.this);
                        break;

                }
                if(fragment!=null){
                    getSupportFragmentManager().beginTransaction().replace(R.id.frameLayout,
                            Objects.requireNonNull(fragment)).commit();
                    drawerLayout.closeDrawer(GravityCompat.START);
                    return true;
                }
                return false;
            }
        });

        findViewById(R.id.logoutBtn).setOnClickListener(v -> {
            confirmLogout();
        });

    }

    private void loadFacKeyForDepartAdmin() {
        showProgress();
        ref.child("struct/departs").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot s : snapshot.getChildren()){
                    if(s.hasChild(userData.getDepartKey()))Const.SELECTED_FAC_KEY = s.getKey();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void loadFacKeyForFacAdmin() {
        showProgress();
        Log.d("EXCEL", "link : "+"role_fac/profs/"+userData.getKey());
        ref.child("role_fac/profs/"+userData.getKey()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                try {
                    String facKey = snapshot.getValue(String.class);
                    if(!facKey.isEmpty()){
                        Const.SELECTED_FAC_KEY = facKey;
                    }

                }catch (Exception e){
                    Log.e("EXP_ERR", "onDataChange: ",e.getCause() );
                    hideProgress();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }



    void getUserData(UserCallback callback){
        ref.child("users/data/"+currentUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.hasChildren()){
                    userData = new User(
                            snapshot.child("username").getValue(String.class),
                            snapshot.child("user_type").getValue(Integer.class),
                            snapshot.child("fullname").getValue(String.class),
                            snapshot.child("depart_key").getValue(String.class)
                    );
                    userData.setKey(currentUser.getUid());
                    Const.USER_DATA = userData;

                    callback.onUserLoaded();



                }else{
                    userData = new User("admin", Const.ADMIN_UNIV,"admin", "");
                    userData.setKey(currentUser.getUid());
                    navigationView.getMenu().findItem(R.id.menu_grades).setVisible(false);
                }

                initUI();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    interface UserCallback {
        void onUserLoaded();
    }

    private void initUI() {
        usernameTV.setText(userData.getUsername());
        fullnameTV.setText(userData.getFullName());
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
        startActivity(new Intent(MainNavigation.this, Login.class));
        finish();
    }

    private void checkPermission(String permission) {
        // Check if the permission has been granted
        if(ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED)requestPermission();
    }

    private void requestPermission() {
        // Request the permission
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 66);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 66) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted
                // Proceed with your logic here
            } else {
                // Permission denied
                // Handle the denial or disable the functionality that requires the permission
            }
        }
    }


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(drawerToggle.onOptionsItemSelected(item)) return true;
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if(drawerLayout.isDrawerOpen(GravityCompat.START)){
            drawerLayout.closeDrawer(GravityCompat.START);
            return;
        }
        super.onBackPressed();
    }



    @Override
    public void showProgress() {
        progress.setVisibility(View.VISIBLE);
    }

    @Override
    public void hideProgress() {
        progress.setVisibility(View.GONE);

    }





}
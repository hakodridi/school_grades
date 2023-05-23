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
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;

import com.codz.okah.school_grades.R;
import com.codz.okah.school_grades.admin.fragments.Department;
import com.codz.okah.school_grades.admin.fragments.Faculty;
import com.codz.okah.school_grades.admin.fragments.Module;
import com.codz.okah.school_grades.admin.fragments.Prof;
import com.codz.okah.school_grades.admin.fragments.Section;
import com.codz.okah.school_grades.admin.fragments.Speciality;
import com.codz.okah.school_grades.admin.fragments.Student;
import com.codz.okah.school_grades.listener.Progress;
import com.google.android.material.navigation.NavigationView;

import java.util.Objects;


public class MainNavigation extends AppCompatActivity implements Progress {
    DrawerLayout drawerLayout;
    NavigationView navigationView;
    ActionBarDrawerToggle drawerToggle;
    View progress;


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

        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        drawerToggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.open, R.string.close);
        drawerLayout.addDrawerListener(drawerToggle);
        drawerToggle.syncState();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        navigationView.bringToFront();


        progress = findViewById(R.id.progress);

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
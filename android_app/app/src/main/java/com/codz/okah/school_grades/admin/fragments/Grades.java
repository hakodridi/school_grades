package com.codz.okah.school_grades.admin.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.codz.okah.school_grades.R;
import com.codz.okah.school_grades.adapters.EditGradeAdapter;
import com.codz.okah.school_grades.adapters.GradeAdapter;
import com.codz.okah.school_grades.adapters.SpinnerAdapter;
import com.codz.okah.school_grades.adapters.UserAdapter;
import com.codz.okah.school_grades.admin.ProfHome;
import com.codz.okah.school_grades.listener.Progress;
import com.codz.okah.school_grades.tools.Const;
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

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link Grades#newInstance} factory method to
 * create an instance of this fragment.
 */
public class Grades extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;


    Progress progress;
    View mainView;



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


    public Grades(Progress progress) {
        // Required empty public constructor
        this.progress = progress;
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment Grades.
     */
    // TODO: Rename and change types and number of parameters
    public static Grades newInstance(String param1, String param2, Progress progress) {
        Grades fragment = new Grades(progress);
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mainView = inflater.inflate(R.layout.fragment_grades, container, false);

        mainView.findViewById(R.id.group_spinner_layout).setVisibility(View.GONE);

        empty = mainView.findViewById(R.id.empty);
        listView = mainView.findViewById(R.id.listView);
        editListView = mainView.findViewById(R.id.editListView);
        reference = FirebaseDatabase.getInstance().getReference();

        currentUser = FirebaseAuth.getInstance().getCurrentUser();


        selectedModuleKey = "";
        selectedSectionKey = "";
        selectedGroupKey = "";

        selectedModulePosition = 0;
        selectedSectionPosition = 0;
        selectedGroupPosition = 0;

        moduleSpinner = mainView.findViewById(R.id.module_spinner);
        sectionSpinner = mainView.findViewById(R.id.section_spinner);
        groupSpinner = mainView.findViewById(R.id.group_spinner);

        sections = new ArrayList<>();


        moduleAdapter = new SpinnerAdapter(getContext(),android.R.layout.simple_spinner_item,new String[]{
                "Select a choice"
        });
        sectionAdapter = new SpinnerAdapter(getContext(),android.R.layout.simple_spinner_item,new String[]{
                "Select a choice"
        });
        groupAdapter = new SpinnerAdapter(getContext(),android.R.layout.simple_spinner_item,new String[]{
                "Select a choice"
        });

        moduleAdapter.setDropDownViewResource(R.layout.spinner_item);
        sectionAdapter.setDropDownViewResource(R.layout.spinner_item);
        groupAdapter.setDropDownViewResource(R.layout.spinner_item);


        moduleSpinner.setAdapter(moduleAdapter);
        sectionSpinner.setAdapter(sectionAdapter);
        groupSpinner.setAdapter(groupAdapter);

        students = new ArrayList<>();

        adapter = new GradeAdapter(getContext(), new ArrayList<>());
        listView.setAdapter(adapter);

        editAdapter = new EditGradeAdapter(getContext(), new ArrayList<>());
        editListView.setAdapter(editAdapter);

        mainView.findViewById(R.id.floatingEditBtn).setOnClickListener(v->{
            showEditeLayout();
        });

        mainView.findViewById(R.id.cancelEditBtn).setOnClickListener(v->{
            hideEditeLayout();
        });


        loadUserData();

        mainView.findViewById(R.id.confirmFilterBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(selectedGroupPosition==0 )return;
                showModuleInfo();
                mainView.findViewById(R.id.chooseLayout).setVisibility(View.GONE);
                loadStudents();
            }
        });

        mainView.findViewById(R.id.confirmEditBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uploadGrades();
            }
        });

        return mainView;
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
        progress.showProgress();
        reference.child("grades")
                .child(selectedModuleKey)
                .child(selectedSectionKey)
                .child(selectedGroupKey).setValue(map, new DatabaseReference.CompletionListener() {
                    @Override
                    public void onComplete(@Nullable DatabaseError error, @NonNull DatabaseReference ref) {
                        progress.hideProgress();
                        if (error != null) {
                            Toast.makeText(getContext(), "Error", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(getContext(), "Grades added", Toast.LENGTH_SHORT).show();
                            loadStudents();
                        }
                    }
                });

    }

    private void loadUserData() {
        progress.showProgress();
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
                            com.codz.okah.school_grades.tools.Module module = new Module(
                                    item,
                                    snapshot.child("fac_key").getValue(String.class),
                                    snapshot.child("depart_key").getValue(String.class),
                                    snapshot.child("speciality_key").getValue(String.class)
                            );
                            modules.add(module);
                            list.add(item.getValue());
                        }
                        progress.hideProgress();
                        if(modules.isEmpty()){
                            empty.setVisibility(View.VISIBLE);
                            mainView.findViewById(R.id.chooseLayout).setVisibility(View.GONE);
                        }


                        moduleAdapter = new SpinnerAdapter(
                                getContext(),
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
                        Toast.makeText(getContext(), "Error", Toast.LENGTH_SHORT).show();
                    }
                });

    }

    private void init(){
        moduleSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if(position<=0){
                    mainView.findViewById(R.id.section_spinner_layout).setVisibility(View.GONE);
                    selectedSectionPosition = 0;
                    selectedSectionKey = "";
                    return;
                }
                mainView.findViewById(R.id.section_spinner_layout).setVisibility(View.VISIBLE);
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
                    mainView.findViewById(R.id.group_spinner_layout).setVisibility(View.GONE);
                    selectedGroupPosition = 0;
                    selectedGroupKey = "";
                    return;
                }
                mainView.findViewById(R.id.group_spinner_layout).setVisibility(View.VISIBLE);
                selectedSectionPosition = parent.getSelectedItemPosition();
                selectedSectionKey = sections.get(position-1).getKey();


                ArrayList<String> groups = new ArrayList<>();
                groups.add("Select a choice");
                for (int i = 0; i < sections.get(position-1).getNumber(); i++) {
                    groups.add(String.valueOf(i+1));
                }

                progress.hideProgress();
                groupAdapter = new SpinnerAdapter(
                        getContext(),
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
        progress.showProgress();
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


                progress.hideProgress();
                loadGrades();
                empty.setVisibility(students.isEmpty()?View.VISIBLE:View.GONE);

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                progress.hideProgress();
                Toast.makeText(getContext(), "Error", Toast.LENGTH_SHORT).show();
                Log.e("EXP_ERR", "onCancelled: "+databaseError.getMessage()+"\n"+databaseError.getDetails() );
                // Handle database error
            }
        });
    }


    private void loadGrades(){
        progress.showProgress();
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

                        progress.hideProgress();

                        hideEditeLayout();

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        progress.hideProgress();
                        Toast.makeText(getContext(), "Error", Toast.LENGTH_SHORT).show();
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
        progress.showProgress();
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
                        getContext(),
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

                progress.hideProgress();

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                mainView.findViewById(R.id.progress).setVisibility(View.GONE);
                Toast.makeText(getContext(), "Error", Toast.LENGTH_SHORT).show();
                // Handle database error
            }
        });
    }





    private void hideEditeLayout(){
        mainView.findViewById(R.id.edit_grades_layout).setVisibility(View.GONE);
        mainView.findViewById(R.id.floatingEditBtn).setVisibility(View.VISIBLE);
    }

    private void showEditeLayout(){
        editAdapter.setStudents(students);
        editListView.setAdapter(editAdapter);
        editAdapter.notifyDataSetChanged();

        mainView.findViewById(R.id.edit_grades_layout).setVisibility(View.VISIBLE);
        mainView.findViewById(R.id.floatingEditBtn).setVisibility(View.GONE);
    }

    private void showModuleInfo() {
        ((TextView)mainView.findViewById(R.id.module_name)).setText(modules.get(selectedModulePosition).getModule().getValue()+
                " > "+sections.get(sectionSpinner.getSelectedItemPosition()-1).getValue()+
                "["+selectedGroupKey+"]");
        if(modules.get(selectedModulePosition).getModule().isHasTP()){
            mainView.findViewById(R.id.tpTitle).setVisibility(View.VISIBLE);
            editAdapter.setVisibility(View.VISIBLE);
            adapter.setVisibility(View.VISIBLE);
        }
        else {
            mainView.findViewById(R.id.tpTitle).setVisibility(View.GONE);
            editAdapter.setVisibility(View.GONE);
            adapter.setVisibility(View.GONE);
        }
    }




}
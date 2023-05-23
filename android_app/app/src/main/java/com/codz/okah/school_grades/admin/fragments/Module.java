package com.codz.okah.school_grades.admin.fragments;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.codz.okah.school_grades.R;
import com.codz.okah.school_grades.adapters.SpinnerAdapter;
import com.codz.okah.school_grades.adapters.StandardAdapter;
import com.codz.okah.school_grades.listener.Progress;
import com.codz.okah.school_grades.listener.StandardListener;
import com.codz.okah.school_grades.tools.Const;
import com.codz.okah.school_grades.tools.Item;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link Module#newInstance} factory method to
 * create an instance of this fragment.
 */
public class Module extends Fragment implements StandardListener {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    View mainView;
    View empty;
    Progress progress;
    RecyclerView listView;
    StandardAdapter adapter;
    ArrayList<Item> items, facs, departs, specialities;
    DatabaseReference reference;
    Spinner facSpinner, departSpinner, levelSpinner, specialitySpinner;
    ArrayAdapter facAdapter, departAdapter, levelAdapter, specialityAdapter;

    String selectedFacKey, selectedDepartKey, selectedLevelKey, selectedSpecialityKey;
    int selectedFacPosition;
    int selectedDepartPosition;
    int selectedLevelPosition;
    int selectedSpecialityPosition;

    public Module(Progress progress) {
        // Required empty public constructor
        this.progress = progress;
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment Module.
     */
    // TODO: Rename and change types and number of parameters
    public static Module newInstance(String param1, String param2, Progress progress) {
        Module fragment = new Module(progress);
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
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mainView = inflater.inflate(R.layout.fragment_module, container, false);
        empty = mainView.findViewById(R.id.empty);
        listView = mainView.findViewById(R.id.listView);
        reference = FirebaseDatabase.getInstance().getReference();

        selectedFacKey = "";
        selectedDepartKey = "";
        selectedLevelKey = "";
        selectedSpecialityKey = "";

        selectedFacPosition     = 0;
        selectedDepartPosition  = 0;
        selectedLevelPosition   = 0;
        selectedSpecialityPosition = 0;

        facSpinner = mainView.findViewById(R.id.fac_spinner);
        departSpinner = mainView.findViewById(R.id.depart_spinner);
        levelSpinner = mainView.findViewById(R.id.level_spinner);
        specialitySpinner = mainView.findViewById(R.id.speciality_spinner);

        facAdapter = new SpinnerAdapter(getContext(),android.R.layout.simple_spinner_item,new String[0]);
        departAdapter = new SpinnerAdapter(getContext(),android.R.layout.simple_spinner_item,new String[]{
                "Select a choice"
        });
        levelAdapter = new SpinnerAdapter(getContext(),android.R.layout.simple_spinner_item,new String[]{
                "Select a choice", "L1", "L2", "L3", "M1", "M2"
        });
        specialityAdapter = new SpinnerAdapter(getContext(),android.R.layout.simple_spinner_item,new String[]{
                "Select a choice"
        });

        facAdapter.setDropDownViewResource(R.layout.spinner_item);
        departAdapter.setDropDownViewResource(R.layout.spinner_item);
        levelAdapter.setDropDownViewResource(R.layout.spinner_item);
        specialityAdapter.setDropDownViewResource(R.layout.spinner_item);

        facSpinner.setAdapter(facAdapter);
        departSpinner.setAdapter(departAdapter);
        levelSpinner.setAdapter(levelAdapter);
        specialitySpinner.setAdapter(specialityAdapter);

        items = new ArrayList<>();



        adapter = new StandardAdapter(items, Const.DEPART, getContext(), Module.this);

        mainView.findViewById(R.id.floatingBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openDialog();
            }
        });

        facSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                levelSpinner.setSelection(0);
                departSpinner.setSelection(0);
                if(position<=0){
                    mainView.findViewById(R.id.depart_spinner_layout).setVisibility(View.GONE);
                    mainView.findViewById(R.id.level_spinner_layout).setVisibility(View.GONE);
                    mainView.findViewById(R.id.speciality_spinner_layout).setVisibility(View.GONE);

                    selectedDepartPosition  = 0;
                    selectedDepartKey       = "";

                    selectedLevelPosition = 0;
                    selectedLevelKey = "";

                    selectedSpecialityPosition = 0;
                    selectedSpecialityKey = "";

                    return;
                }
                mainView.findViewById(R.id.depart_spinner_layout).setVisibility(View.VISIBLE);
                selectedFacPosition = parent.getSelectedItemPosition();
                selectedFacKey = facs.get(position-1).getKey();
                loadDeparts();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        departSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                levelSpinner.setSelection(0);
                if(position<=0){
                    mainView.findViewById(R.id.level_spinner_layout).setVisibility(View.GONE);
                    mainView.findViewById(R.id.speciality_spinner_layout).setVisibility(View.GONE);
                    selectedLevelPosition = 0;
                    selectedLevelKey = "";

                    selectedSpecialityPosition = 0;
                    selectedSpecialityKey = "";
                    return;
                }
                mainView.findViewById(R.id.level_spinner_layout).setVisibility(View.VISIBLE);
                selectedDepartPosition = parent.getSelectedItemPosition();
                selectedDepartKey = departs.get(position-1).getKey();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        levelSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if(position<=0){
                    mainView.findViewById(R.id.speciality_spinner_layout).setVisibility(View.GONE);

                    selectedSpecialityPosition = 0;
                    selectedSpecialityKey = "";
                    return;
                }
                mainView.findViewById(R.id.speciality_spinner_layout).setVisibility(View.VISIBLE);
                selectedLevelPosition = parent.getSelectedItemPosition();
                selectedLevelKey = Const.LEVELS[position-1];
                loadSpecialities();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        specialitySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if(position<=0)return;
                selectedSpecialityPosition = parent.getSelectedItemPosition();
                selectedSpecialityKey = specialities.get(position-1).getKey();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });


        mainView.findViewById(R.id.confirmFilterBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(selectedFacPosition==0 || selectedDepartPosition==0 || selectedLevelPosition==0 || selectedSpecialityPosition==0)return;

                selectedFacKey = facs.get(selectedFacPosition-1).getKey();
                mainView.findViewById(R.id.chooseLayout).setVisibility(View.GONE);
                progress.showProgress();
                load();
            }
        });

        loadFacs();

        return mainView;
    }




    @SuppressLint("MissingInflatedId")
    private void openDialog() {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getContext());

        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_add, null);
        dialogBuilder.setView(dialogView);

        AlertDialog alertDialog = dialogBuilder.create();

        ((TextView) dialogView.findViewById(R.id.title)).setText("Add a Module");

        dialogView.findViewById(R.id.check_tp_layout).setVisibility(View.VISIBLE);


        dialogView.findViewById(R.id.cancelBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
            }
        });

        dialogView.findViewById(R.id.confirmBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addFromDialog(alertDialog, ((TextInputEditText)dialogView.findViewById(R.id.input_text)),
                        ((CheckBox)dialogView.findViewById(R.id.check_tp)));
            }
        });

        alertDialog.show();
    }

    @SuppressLint("MissingInflatedId")
    private void openEditDialog(int position) {

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getContext());

        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_edit, null);
        dialogBuilder.setView(dialogView);

        AlertDialog alertDialog = dialogBuilder.create();

        ((TextInputEditText)dialogView.findViewById(R.id.input_text)).setText(items.get(position).getValue());
        dialogView.findViewById(R.id.check_tp_layout).setVisibility(View.VISIBLE);

        dialogView.findViewById(R.id.cancelBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
            }
        });

        dialogView.findViewById(R.id.confirmBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editFromDialog(position, alertDialog, ((TextInputEditText)dialogView.findViewById(R.id.input_text))
                        , ((CheckBox)dialogView.findViewById(R.id.check_tp)));
            }
        });

        alertDialog.show();
    }

    @SuppressLint("MissingInflatedId")
    private void openDeleteDialog(int position) {

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getContext());

        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_delete, null);
        dialogBuilder.setView(dialogView);

        AlertDialog alertDialog = dialogBuilder.create();

        ((TextView)dialogView.findViewById(R.id.title)).setText("Are you sure you want to delete '"+items.get(position).getValue()+"' ?");

        dialogView.findViewById(R.id.cancelBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
            }
        });

        dialogView.findViewById(R.id.confirmBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteFromDialog(position, alertDialog);
            }
        });

        alertDialog.show();
    }

    private void openDialogSelectProf(int position) {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getContext());

        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_select_prof, null);
        dialogBuilder.setView(dialogView);

        AlertDialog alertDialog = dialogBuilder.create();



        alertDialog.show();
    }

    private void deleteFromDialog(int position,AlertDialog alertDialog){


        progress.showProgress();
        alertDialog.dismiss();

        reference.child("struct/modules/"+selectedSpecialityKey).child(items.get(position).getKey()).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    // Value removed successfully
                    Toast.makeText(getContext(), "Removed", Toast.LENGTH_SHORT).show();
                    load();
                } else {
                    // Failed to remove value
                    Toast.makeText(getContext(), "Error", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    private void editFromDialog(int position, AlertDialog alertDialog, TextInputEditText inputED, CheckBox checkBox){
        String inputText = inputED.getText().toString().trim();

        if (inputText.isEmpty()){
            inputED.setError("Check this");
            inputED.requestFocus();
            return;
        }



        progress.showProgress();
        alertDialog.dismiss();

        HashMap<String, Object> map = new HashMap<>();
        map.put("name", inputText);
        map.put("has_tp", checkBox.isChecked());

        reference.child("struct/modules/"+selectedSpecialityKey).child(items.get(position).getKey()).setValue(map, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                progress.hideProgress();
                if (databaseError != null) {
                    Toast.makeText(getContext(), "Error", Toast.LENGTH_SHORT).show();
                    alertDialog.show();
                    ((TextInputEditText)alertDialog.findViewById(R.id.input_text)).setText(inputText);
                } else {
                    load();
                }
            }
        });

    }

    private void addFromDialog(AlertDialog alertDialog, TextInputEditText inputED, CheckBox checkBox) {
        String inputText = inputED.getText().toString().trim();


        if (inputText.isEmpty()){
            inputED.setError("Check this");
            inputED.requestFocus();
            return;
        }


        HashMap<String, Object> map = new HashMap<>();
        map.put("name", inputText);
        map.put("has_tp", checkBox.isChecked());

        alertDialog.dismiss();
        progress.showProgress();

        reference.child("struct/modules/"+selectedSpecialityKey).push().setValue(map, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                progress.hideProgress();
                if (databaseError != null) {
                    Toast.makeText(getContext(), "Error", Toast.LENGTH_SHORT).show();
                    alertDialog.show();
                    ((TextInputEditText)alertDialog.findViewById(R.id.input_text)).setText(inputText);
                } else {
                    Toast.makeText(getContext(), "Added", Toast.LENGTH_SHORT).show();
                    load();
                }
            }
        });


    }





    private void loadFacs() {
        //adapter.setSource(Const.FAC);
        progress.showProgress();

        reference.child("struct/facs").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                ArrayList<String> facList = new ArrayList<>();
                facs = new ArrayList<>();
                facList.add("Select a choice");
                for (DataSnapshot childSnapshot : dataSnapshot.getChildren()) {
                    facs.add(new Item(
                            childSnapshot.getKey(),
                            childSnapshot.getValue(String.class)
                    ));
                    facList.add(childSnapshot.getValue(String.class));


                }
                progress.hideProgress();
                if(facs.isEmpty()){
                    mainView.findViewById(R.id.chooseLayout).setVisibility(View.GONE);
                    mainView.findViewById(R.id.empty).setVisibility(View.VISIBLE);
                }else{
                    mainView.findViewById(R.id.chooseLayout).setVisibility(View.VISIBLE);
                    mainView.findViewById(R.id.empty).setVisibility(View.GONE);
                    facAdapter = new SpinnerAdapter(
                            getContext(),
                            R.layout.spinner_item,
                            facList.toArray(new String[facList.size()])){
                        @Override
                        public boolean isEnabled(int position) {
                            // Disable the hint item
                            return position != 0;
                        }
                    };
                    facSpinner.setAdapter(facAdapter);
                    facSpinner.setSelection(0);
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                progress.hideProgress();
                Log.e("EXP_ERR", "onCancelled: "+databaseError.getMessage()+"\n"+databaseError.getDetails() );
                // Handle database error
            }
        });
    }

    private void loadDeparts() {
        progress.showProgress();

        reference.child("struct/departs/"+selectedFacKey).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                ArrayList<String> departList = new ArrayList<>();
                departs = new ArrayList<>();
                departList.add("Select a choice");
                for (DataSnapshot childSnapshot : dataSnapshot.getChildren()) {
                    departs.add(new Item(
                            childSnapshot.getKey(),
                            childSnapshot.getValue(String.class)
                    ));
                    departList.add(childSnapshot.getValue(String.class));




                }
                progress.hideProgress();
                departAdapter = new SpinnerAdapter(
                        getContext(),
                        R.layout.spinner_item,
                        departList.toArray(new String[departList.size()])){
                    @Override
                    public boolean isEnabled(int position) {
                        // Disable the hint item
                        return position != 0;
                    }
                };
                departSpinner.setAdapter(departAdapter);
                departSpinner.setSelection(0);
                levelSpinner.setSelection(0);

                mainView.findViewById(R.id.level_spinner_layout).setVisibility(View.GONE);
                if(departs.isEmpty()){
                    mainView.findViewById(R.id.depart_spinner_layout).setVisibility(View.GONE);
                }else{
                    mainView.findViewById(R.id.depart_spinner_layout).setVisibility(View.VISIBLE);
                }


            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                progress.hideProgress();
                Log.e("EXP_ERR", "onCancelled: "+databaseError.getMessage()+"\n"+databaseError.getDetails() );
                // Handle database error
            }
        });
    }

    private void loadSpecialities() {
        progress.showProgress();

        reference.child("struct/specialities/"+selectedDepartKey+"/"+selectedLevelKey).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                ArrayList<String> specialityList = new ArrayList<>();
                specialities = new ArrayList<>();
                specialityList.add("Select a choice");
                for (DataSnapshot childSnapshot : dataSnapshot.getChildren()) {
                    specialities.add(new Item(
                            childSnapshot.getKey(),
                            childSnapshot.getValue(String.class)
                    ));
                    specialityList.add(childSnapshot.getValue(String.class));
                }
                progress.hideProgress();
                specialityAdapter = new SpinnerAdapter(
                        getContext(),
                        R.layout.spinner_item,
                        specialityList.toArray(new String[specialityList.size()])){
                    @Override
                    public boolean isEnabled(int position) {
                        // Disable the hint item
                        return position != 0;
                    }
                };
                specialitySpinner.setAdapter(specialityAdapter);
                specialitySpinner.setSelection(0);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                progress.hideProgress();
                Log.e("EXP_ERR", "onCancelled: "+databaseError.getMessage()+"\n"+databaseError.getDetails() );
                // Handle database error
            }
        });
    }

    private void load() {
        adapter.setSource(Const.MODULE);
        progress.showProgress();
        reference.child("struct/modules/"+selectedSpecialityKey).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                items = new ArrayList<>();
                for (DataSnapshot childSnapshot : dataSnapshot.getChildren()) {
                    items.add(new Item(
                            childSnapshot.getKey(),
                            childSnapshot.child("name").getValue(String.class)
                    ));
                    items.get(items.size()-1).setHasTP(childSnapshot.child("has_tp").getValue(Boolean.class));
                    if(childSnapshot.hasChild("prof_id")){
                        items.get(items.size()-1).setProfID(childSnapshot.child("prof_id").getValue(String.class));
                    }
                }
                adapter.setItems(items);
                listView.setAdapter(adapter);
                progress.hideProgress();
                mainView.findViewById(R.id.empty).setVisibility(items.isEmpty()?View.VISIBLE:View.GONE);

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                progress.hideProgress();
                Log.e("EXP_ERR", "onCancelled: "+databaseError.getMessage()+"\n"+databaseError.getDetails() );
                // Handle database error
            }
        });
    }



    @Override
    public void onClick(int position) {
        openDialogSelectProf(position);
    }

    @Override
    public void onEdit(int position) {
        openEditDialog(position);
    }

    @Override
    public void onDelete(int position) {
        openDeleteDialog(position);
    }

}
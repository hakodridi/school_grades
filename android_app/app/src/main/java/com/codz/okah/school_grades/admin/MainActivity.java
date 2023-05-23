package com.codz.okah.school_grades.admin;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.TextView;
import android.widget.Toast;

import com.codz.okah.school_grades.R;
import com.codz.okah.school_grades.adapters.StandardAdapter;
import com.codz.okah.school_grades.listener.StandardListener;
import com.codz.okah.school_grades.tools.Const;
import com.codz.okah.school_grades.tools.Item;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;


public class MainActivity extends AppCompatActivity {


    int role;
    String facID, departId, levelID, specialityID, groupId, studentId;

    RecyclerView listView;
    StandardAdapter adapter;
    TextView title;
    
    ArrayList<Item> items;
    DatabaseReference reference;
    Window window;

    View backBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        window = getWindow();
        // Set the status bar color
        window.setStatusBarColor(getResources().getColor(R.color.aqua_dark));

        reference = FirebaseDatabase.getInstance().getReference();


        facID           = "";
        departId        = "";
        levelID         = "";
        specialityID    = "";
        groupId         = "";
        studentId       = "";

        role = Const.ADMIN_UNIV;

        title = findViewById(R.id.title);
        backBtn = findViewById(R.id.backBtn);
        listView = findViewById(R.id.listView);
        items = new ArrayList<>();

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goBack();
            }
        });

        adapter = new StandardAdapter(items, Const.FAC, this, new StandardListener() {
            @Override
            public void onClick(int position) {
                switch (adapter.getSource()){
                    case Const.FAC:
                        facID = items.get(position).getKey();
                        title.setText(items.get(position).getValue());
                        loadDeparts();
                        break;
                    case Const.DEPART:
                        departId = items.get(position).getKey();
                        title.setText(items.get(position).getValue());
                        loadLevels();
                        break;
                    case Const.LEVEL:
                        levelID = items.get(position).getKey();
                        title.setText(items.get(position).getValue());
                        loadSpecialities();
                        break;
                    case Const.SPECIALITY:
                        showChooseDialog(position);
                        break;


                }
            }

            @Override
            public void onEdit(int position) {

            }

            @Override
            public void onDelete(int position) {

            }
        });

        findViewById(R.id.floatingBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openDialog();
            }
        });
        
        switch (role){
            case Const.ADMIN_UNIV:
                loadFacs();
                break;
        }

        

    }

    @SuppressLint("MissingInflatedId")
    private void showChooseDialog(int position) {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_choose, null);
        dialogBuilder.setView(dialogView);

        AlertDialog alertDialog = dialogBuilder.create();
        alertDialog.show();

        final int[] selectedChoice = {-1};

        dialogView.findViewById(R.id.modulesChoice).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogView.findViewById(R.id.modulesChoice).setEnabled(false);
                dialogView.findViewById(R.id.groupsChoice).setEnabled(true);
                selectedChoice[0] = 1;
            }
        });

        dialogView.findViewById(R.id.groupsChoice).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogView.findViewById(R.id.modulesChoice).setEnabled(true);
                dialogView.findViewById(R.id.groupsChoice).setEnabled(false);
                selectedChoice[0] = 2;
            }
        });

        dialogView.findViewById(R.id.confirmBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (selectedChoice[0]){
                    case 1:
                        specialityID = items.get(position).getKey();
                        adapter.setSource(Const.MODULE);
                        title.setText(items.get(position).getValue());
                        loadModules();
                        alertDialog.dismiss();
                        break;
                    case 2:
                        specialityID = items.get(position).getKey();
                        adapter.setSource(Const.SECTION);
                        title.setText(items.get(position).getValue());
                        loadSections();
                        alertDialog.dismiss();
                        break;
                    default:
                        Toast.makeText(MainActivity.this, "Select a choice", Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        });

        dialogView.findViewById(R.id.cancelBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
            }
        });




    }

    @SuppressLint("MissingInflatedId")
    private void openDialog() {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);

        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_add, null);
        dialogBuilder.setView(dialogView);

        AlertDialog alertDialog = dialogBuilder.create();

        if (adapter.getSource()==Const.SECTION)dialogView.findViewById(R.id.numberLayout).setVisibility(View.VISIBLE);
        else dialogView.findViewById(R.id.numberLayout).setVisibility(View.GONE);

        dialogView.findViewById(R.id.cancelBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
            }
        });

        dialogView.findViewById(R.id.confirmBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (adapter.getSource()){
                    case Const.FAC:
                        addFacFromDialog(alertDialog, ((TextInputEditText)dialogView.findViewById(R.id.input_text)));
                        break;
                    case Const.DEPART:
                        addDepartFromDialog(alertDialog, ((TextInputEditText)dialogView.findViewById(R.id.input_text)));
                        break;
                    case Const.SPECIALITY:
                        addSpecialityFromDialog(alertDialog, ((TextInputEditText)dialogView.findViewById(R.id.input_text)));
                        break;
                    case Const.SECTION:
                        addSectionFromDialog(alertDialog, ((TextInputEditText)dialogView.findViewById(R.id.input_text)), ((TextInputEditText)dialogView.findViewById(R.id.input_number)));
                        break;
                    case Const.MODULE:
                        addModuleFromDialog(alertDialog, ((TextInputEditText)dialogView.findViewById(R.id.input_text)));
                        break;
                }
            }
        });



        alertDialog.show();
    }



    private void addFacFromDialog(AlertDialog alertDialog, TextInputEditText inputED) {
        String inputText = inputED.getText().toString().trim();
        if (inputText.isEmpty()){
            inputED.setError("Check this");
            inputED.requestFocus();
            return;
        }

        alertDialog.dismiss();
        showProgress();

        reference.child("struct/facs").push().setValue(inputText, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                hideProgress();
                if (databaseError != null) {
                    Toast.makeText(MainActivity.this, "Error", Toast.LENGTH_SHORT).show();
                    alertDialog.show();
                    ((TextInputEditText)alertDialog.findViewById(R.id.input_text)).setText(inputText);
                } else {
                    Toast.makeText(MainActivity.this, "Added", Toast.LENGTH_SHORT).show();
                    loadFacs();
                }
            }
        });


    }

    private void addDepartFromDialog(AlertDialog alertDialog, TextInputEditText inputED) {
        String inputText = inputED.getText().toString().trim();
        if (inputText.isEmpty()){
            inputED.setError("Check this");
            inputED.requestFocus();
            return;
        }

        alertDialog.dismiss();
        showProgress();

        reference.child("struct/departs/"+facID).push().setValue(inputText, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                hideProgress();
                if (databaseError != null) {
                    Toast.makeText(MainActivity.this, "Error", Toast.LENGTH_SHORT).show();
                    alertDialog.show();
                    ((TextInputEditText)alertDialog.findViewById(R.id.input_text)).setText(inputText);
                } else {
                    Toast.makeText(MainActivity.this, "Added", Toast.LENGTH_SHORT).show();
                    loadDeparts();
                }
            }
        });


    }

    private void addSpecialityFromDialog(AlertDialog alertDialog, TextInputEditText inputED) {
        String inputText = inputED.getText().toString().trim();
        if (inputText.isEmpty()){
            inputED.setError("Check this");
            inputED.requestFocus();
            return;
        }

        alertDialog.dismiss();
        showProgress();

        reference.child("struct/specialities/"+departId+"/"+levelID).push().setValue(inputText, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                hideProgress();
                if (databaseError != null) {
                    Toast.makeText(MainActivity.this, "Error", Toast.LENGTH_SHORT).show();
                    alertDialog.show();
                    ((TextInputEditText)alertDialog.findViewById(R.id.input_text)).setText(inputText);
                } else {
                    Toast.makeText(MainActivity.this, "Added", Toast.LENGTH_SHORT).show();
                    loadSpecialities();
                }
            }
        });


    }

    private void addSectionFromDialog(AlertDialog alertDialog, TextInputEditText inputED, TextInputEditText inputNumberED) {
        String inputText = inputED.getText().toString().trim();
        String inpuNumber = inputNumberED.getText().toString().trim();
        int number = 1;


        if (inputText.isEmpty()){
            inputED.setError("Check this");
            inputED.requestFocus();
            return;
        }

        if (inpuNumber.isEmpty()){
            inputED.setError("Check this");
            inputED.requestFocus();
            return;
        }

        try {
            number = Integer.parseInt(inpuNumber.trim());
            if (number < 1){
                inputED.setError("Check this");
                inputED.requestFocus();
                return;
            }
        }catch (Exception e){
            Log.e("NUMBER_EXP", "number_ : "+e.getMessage());
        }

        alertDialog.dismiss();
        showProgress();

        HashMap<String, Object> map = new HashMap<>();
        map.put("name", inputText);
        map.put("number_of_groups", number);

        reference.child("struct/sections/"+specialityID).push().setValue(map, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                hideProgress();
                if (databaseError != null) {
                    Toast.makeText(MainActivity.this, "Error", Toast.LENGTH_SHORT).show();
                    alertDialog.show();
                    ((TextInputEditText)alertDialog.findViewById(R.id.input_text)).setText(inputText);
                } else {
                    Toast.makeText(MainActivity.this, "Added", Toast.LENGTH_SHORT).show();
                    loadSections();
                }
            }
        });


    }

    private void addModuleFromDialog(AlertDialog alertDialog, TextInputEditText inputED) {
        String inputText = inputED.getText().toString().trim();
        if (inputText.isEmpty()){
            inputED.setError("Check this");
            inputED.requestFocus();
            return;
        }

        alertDialog.dismiss();
        showProgress();

        reference.child("struct/modules/"+specialityID).push().setValue(inputText, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                hideProgress();
                if (databaseError != null) {
                    Toast.makeText(MainActivity.this, "Error", Toast.LENGTH_SHORT).show();
                    alertDialog.show();
                    ((TextInputEditText)alertDialog.findViewById(R.id.input_text)).setText(inputText);
                } else {
                    Toast.makeText(MainActivity.this, "Added", Toast.LENGTH_SHORT).show();
                    loadModules();
                }
            }
        });


    }



    private void loadFacs() {
        adapter.setSource(Const.FAC);
        backBtn.setVisibility(View.GONE);
        findViewById(R.id.floatingBtn).setVisibility(View.VISIBLE);
        showProgress();
        reference.child("struct/facs").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                items = new ArrayList<>();
                for (DataSnapshot childSnapshot : dataSnapshot.getChildren()) {
                    items.add(new Item(
                            childSnapshot.getKey(),
                            childSnapshot.getValue(String.class)
                    ));

                }
                adapter.setItems(items);
                listView.setAdapter(adapter);
                hideProgress();
                findViewById(R.id.empty).setVisibility(items.isEmpty()?View.VISIBLE:View.GONE);

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                findViewById(R.id.progress).setVisibility(View.GONE);
                Log.e("EXP_ERR", "onCancelled: "+databaseError.getMessage()+"\n"+databaseError.getDetails() );
                // Handle database error
            }
        });
    }

    private void loadDeparts() {
        adapter.setSource(Const.DEPART);
        backBtn.setVisibility(View.VISIBLE);
        findViewById(R.id.floatingBtn).setVisibility(View.VISIBLE);
        showProgress();
        reference.child("struct/departs/"+facID+"").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                items = new ArrayList<>();
                for (DataSnapshot childSnapshot : dataSnapshot.getChildren()) {
                    items.add(new Item(
                            childSnapshot.getKey(),
                            childSnapshot.getValue(String.class)
                    ));
                }
                adapter.setItems(items);
                listView.setAdapter(adapter);
                hideProgress();
                findViewById(R.id.empty).setVisibility(items.isEmpty()?View.VISIBLE:View.GONE);

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                findViewById(R.id.progress).setVisibility(View.GONE);
                Log.e("EXP_ERR", "onCancelled: "+databaseError.getMessage()+"\n"+databaseError.getDetails() );
                // Handle database error
            }
        });
    }

    private void loadLevels() {
        adapter.setSource(Const.LEVEL);
        backBtn.setVisibility(View.VISIBLE);
        findViewById(R.id.floatingBtn).setVisibility(View.GONE);
        showProgress();
        items = new ArrayList<>();
        items.add(new Item("L1","L1"));
        items.add(new Item("L2","L2"));
        items.add(new Item("L3","L3"));
        items.add(new Item("M1","M1"));
        items.add(new Item("M2","M2"));
        adapter.setItems(items);
        listView.setAdapter(adapter);
        hideProgress();
        findViewById(R.id.empty).setVisibility(items.isEmpty()?View.VISIBLE:View.GONE);
    }

    private void loadSpecialities() {
        adapter.setSource(Const.SPECIALITY);
        backBtn.setVisibility(View.VISIBLE);
        findViewById(R.id.floatingBtn).setVisibility(View.VISIBLE);
        showProgress();
        reference.child("struct/specialities/"+departId+"/"+levelID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                items = new ArrayList<>();
                for (DataSnapshot childSnapshot : dataSnapshot.getChildren()) {
                    items.add(new Item(
                            childSnapshot.getKey(),
                            childSnapshot.getValue(String.class)
                    ));

                }
                adapter.setItems(items);
                listView.setAdapter(adapter);
                hideProgress();
                findViewById(R.id.empty).setVisibility(items.isEmpty()?View.VISIBLE:View.GONE);

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                findViewById(R.id.progress).setVisibility(View.GONE);
                Log.e("EXP_ERR", "onCancelled: "+databaseError.getMessage()+"\n"+databaseError.getDetails() );
                // Handle database error
            }
        });
    }

    private void loadSections() {
        adapter.setSource(Const.SECTION);
        backBtn.setVisibility(View.VISIBLE);
        findViewById(R.id.floatingBtn).setVisibility(View.VISIBLE);
        showProgress();
        reference.child("struct/sections/"+specialityID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                items = new ArrayList<>();
                for (DataSnapshot childSnapshot : dataSnapshot.getChildren()) {
                    items.add(new Item(
                            childSnapshot.getKey(),
                            childSnapshot.child("name").getValue(String.class)
                    ));
                    items.get(items.size()-1).setNumber(childSnapshot.child("number_of_groups").getValue(Integer.class));
                }
                adapter.setItems(items);
                listView.setAdapter(adapter);
                hideProgress();
                findViewById(R.id.empty).setVisibility(items.isEmpty()?View.VISIBLE:View.GONE);

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                findViewById(R.id.progress).setVisibility(View.GONE);
                Log.e("EXP_ERR", "onCancelled: "+databaseError.getMessage()+"\n"+databaseError.getDetails() );
                // Handle database error
            }
        });
    }

    private void loadModules() {
        adapter.setSource(Const.MODULE);
        backBtn.setVisibility(View.VISIBLE);
        findViewById(R.id.floatingBtn).setVisibility(View.VISIBLE);
        showProgress();
        reference.child("struct/modules/"+specialityID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                items = new ArrayList<>();
                for (DataSnapshot childSnapshot : dataSnapshot.getChildren()) {
                    items.add(new Item(
                            childSnapshot.getKey(),
                            childSnapshot.getValue(String.class)
                    ));
                }
                adapter.setItems(items);
                listView.setAdapter(adapter);
                hideProgress();
                findViewById(R.id.empty).setVisibility(items.isEmpty()?View.VISIBLE:View.GONE);

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                findViewById(R.id.progress).setVisibility(View.GONE);
                Log.e("EXP_ERR", "onCancelled: "+databaseError.getMessage()+"\n"+databaseError.getDetails() );
                // Handle database error
            }
        });
    }




    private void showProgress() {
        window.setStatusBarColor(getResources().getColor(R.color.white));
        findViewById(R.id.progress).setVisibility(View.VISIBLE);
    }
    private void hideProgress() {
        findViewById(R.id.progress).setVisibility(View.GONE);
        window.setStatusBarColor(getResources().getColor(R.color.aqua_dark));
    }

    private boolean goBack(){
        boolean result = false;
        switch (adapter.getSource()){
            case Const.DEPART:
                loadFacs();
                result = true;
                break;
            case Const.LEVEL:
                loadDeparts();
                result = true;
                break;
            case Const.SPECIALITY:
                loadLevels();
                result = true;
                break;
            case Const.SECTION:
            case Const.MODULE:
                loadSpecialities();
                result = true;
                break;
            case Const.GROUP:
                loadSections();
                result = true;
                break;
        }
        return result;
    }

    @Override
    public void onBackPressed() {
        if(goBack())return;
        super.onBackPressed();
    }
}
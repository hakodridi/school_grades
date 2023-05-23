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
import android.widget.TextView;
import android.widget.Toast;

import com.codz.okah.school_grades.R;
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

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link Faculty#newInstance} factory method to
 * create an instance of this fragment.
 */
public class Faculty extends Fragment {

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
    ArrayList<Item> items;
    DatabaseReference reference;

    public Faculty(Progress progress) {
        // Required empty public constructor
        this.progress = progress;
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment Home.
     */
    // TODO: Rename and change types and number of parameters
    public static Faculty newInstance(String param1, String param2, Progress progress) {
        Faculty fragment = new Faculty(progress);
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
        mainView = inflater.inflate(R.layout.fragment_faculty, container, false);

        empty = mainView.findViewById(R.id.empty);
        listView = mainView.findViewById(R.id.listView);
        reference = FirebaseDatabase.getInstance().getReference();

        adapter = new StandardAdapter(items, Const.FAC, getContext(), new StandardListener() {
            @Override
            public void onClick(int position) {

            }

            @Override
            public void onEdit(int position) {
                openEditDialog(position);
            }

            @Override
            public void onDelete(int position) {
                openDeleteDialog(position);
            }
        });

        loadFacs();

        mainView.findViewById(R.id.floatingBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openDialog();
            }
        });

        return mainView;
    }



    @SuppressLint("MissingInflatedId")
    private void openDialog() {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getContext());

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
                addFacFromDialog(alertDialog, ((TextInputEditText)dialogView.findViewById(R.id.input_text)));
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

        dialogView.findViewById(R.id.cancelBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
            }
        });

        dialogView.findViewById(R.id.confirmBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editFacFromDialog(position, alertDialog, ((TextInputEditText)dialogView.findViewById(R.id.input_text)));
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
                deleteFacFromDialog(position, alertDialog);
            }
        });

        alertDialog.show();
    }




    private void deleteFacFromDialog(int position,AlertDialog alertDialog){


        progress.showProgress();
        alertDialog.dismiss();

        reference.child("struct/facs").child(items.get(position).getKey()).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    // Value removed successfully
                    Toast.makeText(getContext(), "Removed", Toast.LENGTH_SHORT).show();
                    loadFacs();
                } else {
                    // Failed to remove value
                    Toast.makeText(getContext(), "Error", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    private void editFacFromDialog(int position,AlertDialog alertDialog, TextInputEditText inputED){
        String inputText = inputED.getText().toString().trim();
        if (inputText.isEmpty()){
            inputED.setError("Check this");
            inputED.requestFocus();
            return;
        }

        progress.showProgress();
        alertDialog.dismiss();

        reference.child("struct/facs").child(items.get(position).getKey()).setValue(inputText, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                progress.hideProgress();
                if (databaseError != null) {
                    Toast.makeText(getContext(), "Error", Toast.LENGTH_SHORT).show();
                    alertDialog.show();
                    ((TextInputEditText)alertDialog.findViewById(R.id.input_text)).setText(inputText);
                } else {
                    loadFacs();
                }
            }
        });

    }

    private void addFacFromDialog(AlertDialog alertDialog, TextInputEditText inputED) {
        String inputText = inputED.getText().toString().trim();
        if (inputText.isEmpty()){
            inputED.setError("Check this");
            inputED.requestFocus();
            return;
        }

        alertDialog.dismiss();
        progress.showProgress();

        reference.child("struct/facs").push().setValue(inputText, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                progress.hideProgress();
                if (databaseError != null) {
                    Toast.makeText(getContext(), "Error", Toast.LENGTH_SHORT).show();
                    alertDialog.show();
                    ((TextInputEditText)alertDialog.findViewById(R.id.input_text)).setText(inputText);
                } else {
                    Toast.makeText(getContext(), "Added", Toast.LENGTH_SHORT).show();
                    loadFacs();
                }
            }
        });


    }

    private void loadFacs() {
        adapter.setSource(Const.FAC);
        progress.showProgress();

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
                progress.hideProgress();
                empty.setVisibility(items.isEmpty()?View.VISIBLE:View.GONE);

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                progress.hideProgress();
                Log.e("EXP_ERR", "onCancelled: "+databaseError.getMessage()+"\n"+databaseError.getDetails() );
                // Handle database error
            }
        });
    }

}
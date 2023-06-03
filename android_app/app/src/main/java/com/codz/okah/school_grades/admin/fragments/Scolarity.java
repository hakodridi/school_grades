package com.codz.okah.school_grades.admin.fragments;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import com.codz.okah.school_grades.R;
import com.codz.okah.school_grades.adapters.AdsAdapter;
import com.codz.okah.school_grades.listener.Progress;
import com.codz.okah.school_grades.tools.Ad;
import com.codz.okah.school_grades.tools.Const;
import com.codz.okah.school_grades.tools.CustomJsonObjectRequest;
import com.codz.okah.school_grades.tools.Functions;
import com.codz.okah.school_grades.tools.User;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONObject;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link Scolarity#newInstance} factory method to
 * create an instance of this fragment.
 */
public class Scolarity extends Fragment {

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
    DatabaseReference reference;
    User userData, scolarityUserData;
    String selectedScolarityUserKey;
    FirebaseUser currentUser;

    RecyclerView listView;
    AdsAdapter adapter;
    ArrayList<Ad> ads;



    public Scolarity(Progress progress) {
        // Required empty public constructor
        this.progress = progress;
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment Scolarity.
     */
    // TODO: Rename and change types and number of parameters
    public static Scolarity newInstance(String param1, String param2, Progress progress) {
        Scolarity fragment = new Scolarity(progress);
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
        mainView = inflater.inflate(R.layout.fragment_scolarity, container, false);
        empty = mainView.findViewById(R.id.empty);
        listView = mainView.findViewById(R.id.listView);

        reference = FirebaseDatabase.getInstance().getReference();

        progress.showProgress();

        userData = null;
        scolarityUserData = null;

        selectedScolarityUserKey = "";

        ads = new ArrayList<>();
        adapter = new AdsAdapter(ads);

        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        loadUserData();


        mainView.findViewById(R.id.create_user_btn).setOnClickListener(v -> {
            openDialog();
        });

        /*mainView.findViewById(R.id.edit_btn).setOnClickListener(v -> {

        });

        mainView.findViewById(R.id.delete_btn).setOnClickListener(v -> {

        });*/


        return mainView;
    }

    @SuppressLint("MissingInflatedId")
    private void openDialog() {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getContext());

        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_add_user, null);
        dialogBuilder.setView(dialogView);

        AlertDialog alertDialog = dialogBuilder.create();

        ((TextView) dialogView.findViewById(R.id.title)).setText("Add a Scolarity user");

        dialogView.findViewById(R.id.cancelBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
            }
        });

        dialogView.findViewById(R.id.confirmBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addFromDialog(alertDialog, ((TextInputEditText)dialogView.findViewById(R.id.input_fullname)),
                        (TextInputEditText)dialogView.findViewById(R.id.input_username));
            }
        });

        alertDialog.show();
    }

    private void addFromDialog(AlertDialog alertDialog, TextInputEditText inputFullnameED, TextInputEditText inputUsernameED) {
        String fullNameText = inputFullnameED.getText().toString().trim();
        if (fullNameText.isEmpty()){
            inputFullnameED.setError("Check this");
            inputFullnameED.requestFocus();
            return;
        }

        String userNameText = inputUsernameED.getText().toString().trim();
        if (userNameText.isEmpty()){
            inputUsernameED.setError("Check this");
            inputUsernameED.requestFocus();
            return;
        }

        alertDialog.dismiss();
        progress.showProgress();

        RequestQueue requestQueue = Volley.newRequestQueue(getContext());

        User u = new User(userNameText, Const.SCOLARITY, fullNameText, userData.getDepartKey());

        CustomJsonObjectRequest request = new CustomJsonObjectRequest(Request.Method.POST, Const.API_BASE_URL+"create_scolarity_user", Functions.getUserRequestBody(u),
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        progress.hideProgress();
                        String message = response.optString("message");
                        String error = response.optString("error");

                        if(error.isEmpty()){
                            Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
                            progress.showProgress();
                            getScolarityUserKey();
                        }else{
                            inputFullnameED.setText(fullNameText);
                            inputUsernameED.setText(userNameText);
                            alertDialog.show();
                            Toast.makeText(getContext(), error, Toast.LENGTH_SHORT).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // Handle the error
                        // This code will be executed if there's an error with the request
                        progress.hideProgress();
                        inputFullnameED.setText(fullNameText);
                        inputUsernameED.setText(userNameText);
                        alertDialog.show();
                        Toast.makeText(getContext(), "Error", Toast.LENGTH_SHORT).show();
                    }
                });

        requestQueue.add(request);


    }





    private void loadUserData() {
        reference.child("users/data").child(currentUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(!snapshot.hasChildren()){
                    mainView.findViewById(R.id.display_layout).setVisibility(View.GONE);
                    progress.hideProgress();
                    return;
                }
                userData = new User(
                        snapshot.child("username").getValue(String.class),
                        snapshot.child("user_type").getValue(Integer.class),
                        snapshot.child("fullname").getValue(String.class),
                        snapshot.child("depart_key").getValue(String.class)
                );
                userData.setKey(snapshot.getKey());
                
                getScolarityUserKey();
                
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                mainView.findViewById(R.id.display_layout).setVisibility(View.GONE);
                progress.hideProgress();
            }
        });
    }

    private void getScolarityUserKey() {
        reference.child("scolarity").child(userData.getDepartKey()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(!snapshot.exists()){
                    mainView.findViewById(R.id.display_layout).setVisibility(View.GONE);
                    progress.hideProgress();
                    return;
                }

                selectedScolarityUserKey = snapshot.getValue(String.class);

                loadScolarityData();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                mainView.findViewById(R.id.display_layout).setVisibility(View.GONE);
                progress.hideProgress();
            }
        });

    }

    private void loadScolarityData() {
        reference.child("users/data").child(selectedScolarityUserKey).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(!snapshot.hasChildren()){
                    mainView.findViewById(R.id.display_layout).setVisibility(View.GONE);
                    progress.hideProgress();
                    return;
                }
                scolarityUserData = new User(
                        snapshot.child("username").getValue(String.class),
                        snapshot.child("user_type").getValue(Integer.class),
                        snapshot.child("fullname").getValue(String.class),
                        snapshot.child("depart_key").getValue(String.class)
                );
                scolarityUserData.setKey(snapshot.getKey());
                mainView.findViewById(R.id.display_layout).setVisibility(View.VISIBLE);
                progress.hideProgress();

                ((TextView)mainView.findViewById(R.id.name)).setText(scolarityUserData.getFullName());
                ((TextView)mainView.findViewById(R.id.user_name)).setText(scolarityUserData.getUsername());

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                mainView.findViewById(R.id.display_layout).setVisibility(View.GONE);
                progress.hideProgress();
            }
        });
    }
}
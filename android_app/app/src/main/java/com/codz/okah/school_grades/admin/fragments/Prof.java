package com.codz.okah.school_grades.admin.fragments;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.NoConnectionError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.codz.okah.school_grades.R;
import com.codz.okah.school_grades.adapters.SpinnerAdapter;
import com.codz.okah.school_grades.adapters.UserAdapter;
import com.codz.okah.school_grades.listener.Progress;
import com.codz.okah.school_grades.listener.StandardListener;
import com.codz.okah.school_grades.tools.Const;
import com.codz.okah.school_grades.tools.CustomJsonObjectRequest;
import com.codz.okah.school_grades.tools.Functions;
import com.codz.okah.school_grades.tools.Item;
import com.codz.okah.school_grades.tools.User;
import com.codz.okah.school_grades.tools.VolleyMultipartRequest;
import com.codz.okah.school_grades.tools.VolleySingleton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link Prof#newInstance} factory method to
 * create an instance of this fragment.
 */
public class Prof extends Fragment implements StandardListener {

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
    ArrayList<Item> facs, departs;
    ArrayList<User> profs;
    DatabaseReference reference;
    Spinner facSpinner, departSpinner;
    ArrayAdapter facAdapter, departAdapter;

    UserAdapter adapter ;

    String selectedFacKey, selectedDepartKey;
    int selectedFacPosition;
    int selectedDepartPosition;

    public Prof(Progress progress) {
        // Required empty public constructor
        this.progress = progress;
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment Prof.
     */
    // TODO: Rename and change types and number of parameters
    public static Prof newInstance(String param1, String param2, Progress progress) {
        Prof fragment = new Prof(progress);
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
        mainView = inflater.inflate(R.layout.fragment_prof, container, false);

        empty = mainView.findViewById(R.id.empty);
        listView = mainView.findViewById(R.id.listView);
        reference = FirebaseDatabase.getInstance().getReference();

        selectedFacKey = "";
        selectedDepartKey = "";

        selectedFacPosition     = 0;
        selectedDepartPosition  = 0;

        facSpinner = mainView.findViewById(R.id.fac_spinner);
        departSpinner = mainView.findViewById(R.id.depart_spinner);

        facAdapter = new SpinnerAdapter(getContext(),android.R.layout.simple_spinner_item,new String[0]);
        departAdapter = new SpinnerAdapter(getContext(),android.R.layout.simple_spinner_item,new String[]{
                "Select a choice"
        });

        facAdapter.setDropDownViewResource(R.layout.spinner_item);
        departAdapter.setDropDownViewResource(R.layout.spinner_item);

        facSpinner.setAdapter(facAdapter);
        departSpinner.setAdapter(departAdapter);

        profs = new ArrayList<>();

        adapter = new UserAdapter(profs, getContext(), Prof.this);

        mainView.findViewById(R.id.floatingBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openDialog();
            }
        });

        mainView.findViewById(R.id.floatingBtn).setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                openFilePicker();
                return false;
            }
        });

        facSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                departSpinner.setSelection(0);
                if(position<=0){
                    mainView.findViewById(R.id.depart_spinner_layout).setVisibility(View.GONE);

                    selectedDepartPosition  = 0;
                    selectedDepartKey       = "";

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
                if(position<=0)return;
                selectedDepartPosition = parent.getSelectedItemPosition();
                selectedDepartKey = departs.get(position-1).getKey();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });


        mainView.findViewById(R.id.confirmFilterBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(selectedFacPosition==0 || selectedDepartPosition==0)return;

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
        View dialogView = inflater.inflate(R.layout.dialog_add_user, null);
        dialogBuilder.setView(dialogView);

        AlertDialog alertDialog = dialogBuilder.create();

        ((TextView) dialogView.findViewById(R.id.title)).setText("Add a Prof");

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

    @SuppressLint("MissingInflatedId")
    private void openEditDialog(int position) {

        /*AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getContext());

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
                editFromDialog(position, alertDialog, ((TextInputEditText)dialogView.findViewById(R.id.input_text)));
            }
        });

        alertDialog.show();*/
    }

    @SuppressLint("MissingInflatedId")
    private void openDeleteDialog(int position) {

        /*AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getContext());

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

        alertDialog.show();*/
    }




    private void deleteFromDialog(int position,AlertDialog alertDialog){


        /*progress.showProgress();
        alertDialog.dismiss();

        reference.child("struct/specialities/"+selectedDepartKey+"/"+selectedLevelKey).child(items.get(position).getKey()).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
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
        });*/

    }

    private void editFromDialog(int position,AlertDialog alertDialog, TextInputEditText inputED){
        /*String inputText = inputED.getText().toString().trim();
        if (inputText.isEmpty()){
            inputED.setError("Check this");
            inputED.requestFocus();
            return;
        }

        progress.showProgress();
        alertDialog.dismiss();

        reference.child("struct/specialities/"+selectedDepartKey+"/"+selectedLevelKey).child(items.get(position).getKey()).setValue(inputText, new DatabaseReference.CompletionListener() {
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
        });*/

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

        User u = new User(userNameText, Const.PROF, fullNameText, selectedDepartKey);

        CustomJsonObjectRequest request = new CustomJsonObjectRequest(Request.Method.POST, Const.API_BASE_URL+"create_prof", Functions.getUserRequestBody(u),
            new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    progress.hideProgress();
                    String message = response.optString("message");
                    String error = response.optString("error");

                    if(error.isEmpty()){
                        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
                        load();
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

                    Log.d("EXCEL", "fac: "+Const.SELECTED_FAC_KEY);
                    Log.d("EXCEL", "user: "+Const.USER_DATA);
                    if (Const.USER_DATA!=null && Const.SELECTED_FAC_KEY!=null &&
                            (Const.USER_DATA.getUserType()==Const.ADMIN_FAC || Const.USER_DATA.getUserType()==Const.ADMIN_DEPART)){
                        selectedFacKey = Const.SELECTED_FAC_KEY;
                        facSpinner.setSelection(getFacPosition()+1);
                        facSpinner.setEnabled(false);
                    }
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
        //adapter.setSource(Const.FAC);
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

                if (Const.USER_DATA!=null && Const.SELECTED_FAC_KEY!=null && Const.USER_DATA.getUserType()==Const.ADMIN_DEPART){
                    selectedDepartKey = Const.USER_DATA.getDepartKey();
                    departSpinner.setSelection(getDepartPosition()+1);
                    departSpinner.setEnabled(false);
                }

                mainView.findViewById(R.id.depart_spinner_layout).setVisibility(View.GONE);
                if(!departs.isEmpty()){
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

    private int getFacPosition(){
        for (int i = 0; i < facs.size(); i++) {
            if(facs.get(i).getKey().equals(Const.SELECTED_FAC_KEY))return i;
        }
        return -1;
    }

    private int getDepartPosition(){
        for (int i = 0; i < departs.size(); i++) {
            if(departs.get(i).getKey().equals(Const.USER_DATA.getDepartKey()))return i;
        }
        return -1;
    }

    private void load() {

        progress.showProgress();
        reference.child("users/profs/"+selectedDepartKey).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                profs = new ArrayList<>();
                for (DataSnapshot childSnapshot : dataSnapshot.getChildren()) {

                    User u = new User(
                            childSnapshot.child("username").getValue(String.class),
                            childSnapshot.child("user_type").getValue(Integer.class),
                            childSnapshot.child("fullname").getValue(String.class),
                            childSnapshot.child("depart_key").getValue(String.class)
                    );
                    u.setKey(childSnapshot.getKey());
                    profs.add(u);

                }
                adapter.setItems(profs);
                listView.setAdapter(adapter);
                progress.hideProgress();
                mainView.findViewById(R.id.empty).setVisibility(profs.isEmpty()?View.VISIBLE:View.GONE);

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

    }

    @Override
    public void onEdit(int position) {
        openEditDialog(position);
    }

    @Override
    public void onDelete(int position) {
        openDeleteDialog(position);
    }




    private void openFilePicker() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
        startActivityForResult(intent, Const.READ_REQUEST_CODE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent resultData) {
        super.onActivityResult(requestCode, resultCode, resultData);
        if (requestCode == Const.READ_REQUEST_CODE && resultCode == getActivity().RESULT_OK) {
            if (resultData != null) {
                Uri fileUri = resultData.getData();
                String path = fileUri.getPath();
                File f = new File(path);
                uploadProfs(f);



            }
        }


    }





    private void uploadProfs(File file) {
        // loading or check internet connection or something...
        // ... then
        String url = Const.API_BASE_URL+"upload_profs";
        VolleyMultipartRequest multipartRequest = new VolleyMultipartRequest(Request.Method.POST, url, new Response.Listener<NetworkResponse>() {
            @Override
            public void onResponse(NetworkResponse response) {
                String resultResponse = new String(response.data);
                try {
                    JSONObject result = new JSONObject(resultResponse);
                    String error = result.getString("error");
                    String message = result.getString("message");
                    Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
                    /*if (status.equals(Constant.REQUEST_SUCCESS)) {
                        // tell everybody you have succed upload image and post strings
                        Log.i("Messsage", message);
                    } else {
                        Log.i("Unexpected", message);
                    }*/
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                NetworkResponse networkResponse = error.networkResponse;
                String errorMessage = "Unknown error";
                if (networkResponse == null) {
                    if (error.getClass().equals(TimeoutError.class)) {
                        errorMessage = "Request timeout";
                    } else if (error.getClass().equals(NoConnectionError.class)) {
                        errorMessage = "Failed to connect server";
                    }
                } else {
                    String result = new String(networkResponse.data);
                    try {
                        JSONObject response = new JSONObject(result);
                        String status = response.getString("status");
                        String message = response.getString("message");

                        Log.e("Error Status", status);
                        Log.e("Error Message", message);

                        if (networkResponse.statusCode == 404) {
                            errorMessage = "Resource not found";
                        } else if (networkResponse.statusCode == 401) {
                            errorMessage = message+" Please login again";
                        } else if (networkResponse.statusCode == 400) {
                            errorMessage = message+ " Check your inputs";
                        } else if (networkResponse.statusCode == 500) {
                            errorMessage = message+" Something is getting wrong";
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                Log.i("Error", errorMessage);
                error.printStackTrace();
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                // Add any additional form data parameters if needed
                params.put("depart_key", selectedDepartKey);
                params.put("section_key", "");
                params.put("group", "-1");
                return params;
            }

            @Override
            protected Map<String, DataPart> getByteData() {
                Map<String, DataPart> params = new HashMap<>();
                // file name could found file base or direct access from real path
                // for now just get bitmap data from ImageView
                params.put("file", new DataPart(file.getName(), getFileDataFromPath(file.getPath()), getMimeType(file.getPath())));

                return params;
            }
        };

        VolleySingleton.getInstance(getContext()).addToRequestQueue(multipartRequest);
    }


    private byte[] getFileDataFromPath(String filePath) {
        File file = new File(filePath);
        FileInputStream fileInputStream;
        byte[] bytesArray = new byte[(int) file.length()];

        try {
            fileInputStream = new FileInputStream(file);
            fileInputStream.read(bytesArray);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return bytesArray;
    }

    private String getMimeType(String filePath) {
        String extension = MimeTypeMap.getFileExtensionFromUrl(filePath);
        return MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
    }





}
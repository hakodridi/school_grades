package com.codz.okah.school_grades.tools;


import android.content.Context;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import com.codz.okah.school_grades.admin.ScolarityHome;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Functions {
    public static JSONObject getUserRequestBody(User user){
        JSONObject requestBody = new JSONObject();
        try {
            requestBody.put("fullname", user.getFullName());
            requestBody.put("username", user.getUsername());
            requestBody.put("password", "password123");
            requestBody.put("user_type", user.getUserType());
            requestBody.put("speciality_key", user.getSpecialityKey());
            requestBody.put("depart_key", user.getDepartKey());
            requestBody.put("section_key", user.getSectionKey());
            requestBody.put("group", user.getGroup());
            return requestBody;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String getCurrentDateTime() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy hh:mm");
        Date currentDate = new Date();
        return dateFormat.format(currentDate);
    }

    public static void pushNotif(Context context, String departKey, String title, String body) {
        JSONObject requestBody = new JSONObject();
        try {
            requestBody.put("depart_key", departKey);
            requestBody.put("title", title);
            requestBody.put("body", body);

            CustomJsonObjectRequest request = new CustomJsonObjectRequest(Request.Method.POST, Const.API_BASE_URL+"push_notification/", requestBody,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {

                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {

                        }
                    });

            Volley.newRequestQueue(context).add(request);



        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}

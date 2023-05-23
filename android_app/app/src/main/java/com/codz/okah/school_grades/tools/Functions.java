package com.codz.okah.school_grades.tools;


import org.json.JSONException;
import org.json.JSONObject;

public class Functions {
    public static JSONObject getUserRequestBody(User user){
        JSONObject requestBody = new JSONObject();
        try {
            requestBody.put("fullname", user.getFullName());
            requestBody.put("username", user.getUsername());
            requestBody.put("password", "password123");
            requestBody.put("user_type", user.getUserType());
            requestBody.put("depart_key", user.getDepartKey());
            requestBody.put("section_key", user.getSectionKey());
            requestBody.put("group", user.getGroup());
            return requestBody;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

}

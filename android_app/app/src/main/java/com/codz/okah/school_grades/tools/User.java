package com.codz.okah.school_grades.tools;

public class User {
    private String username;
    private int userType;
    private String fullName;
    private String departKey;
    private String sectionKey;
    private int group;
    private String key;

    public User(String username, int userType, String fullName, String departKey) {
        this.username = username;
        this.userType = userType;
        this.fullName = fullName;
        this.departKey = departKey;
        this.sectionKey = "";
        this.group = -1;
        this.key = "";
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public int getUserType() {
        return userType;
    }

    public void setUserType(int userType) {
        this.userType = userType;
    }

    public String getDepartKey() {
        return departKey;
    }

    public void setDepartKey(String departKey) {
        this.departKey = departKey;
    }

    public String getSectionKey() {
        return sectionKey;
    }

    public void setSectionKey(String sectionKey) {
        this.sectionKey = sectionKey;
    }

    public int getGroup() {
        return group;
    }

    public void setGroup(int group) {
        this.group = group;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }
}

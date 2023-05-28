package com.codz.okah.school_grades.tools;

public class Item {
    private String key;
    private String value;
    private int number;
    private boolean hasTP;
    private String profID;

    public Item(){

    }
    public Item(String key, String value) {
        this.key = key;
        this.value = value;
        this.number = -1;
        this.hasTP= false;
        this.profID = "";
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public boolean isHasTP() {
        return hasTP;
    }

    public void setHasTP(boolean hasTP) {
        this.hasTP = hasTP;
    }

    public String getProfID() {
        return profID;
    }

    public void setProfID(String profID) {
        this.profID = profID;
    }
}

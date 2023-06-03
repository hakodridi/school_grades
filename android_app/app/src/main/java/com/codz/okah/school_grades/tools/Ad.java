package com.codz.okah.school_grades.tools;

public class Ad {
    private String text;
    private String date;

    public Ad(String text, String date) {
        this.text = text;
        this.date = date;
    }

    public Ad() {

    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
}

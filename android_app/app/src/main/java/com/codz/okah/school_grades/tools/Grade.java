package com.codz.okah.school_grades.tools;

public class Grade {
    private String exam;
    private String td;
    private String tp;

    public Grade(String exam, String td, String tp) {
        this.exam = exam;
        this.td = td;
        this.tp = tp;
    }

    public Grade(String exam, String td) {
        this.exam = exam;
        this.td = td;
        this.tp = "";
    }

    public Grade() {

    }

    public String getExam() {
        return exam;
    }

    public void setExam(String exam) {
        this.exam = exam;
    }

    public String getTd() {
        return td;
    }

    public void setTd(String td) {
        this.td = td;
    }

    public String getTp() {
        return tp;
    }

    public void setTp(String tp) {
        this.tp = tp;
    }
}

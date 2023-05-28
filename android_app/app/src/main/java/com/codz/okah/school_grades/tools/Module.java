package com.codz.okah.school_grades.tools;

public class Module {
    private String facKey;
    private String departKey;
    private String specialityKey;
    Item module;

    public Module(Item module, String facKey, String departKey, String specialityKey) {
        this.facKey = facKey;
        this.departKey = departKey;
        this.specialityKey = specialityKey;
        this.module = module;
    }

    public String getFacKey() {
        return facKey;
    }

    public void setFacKey(String facKey) {
        this.facKey = facKey;
    }

    public String getDepartKey() {
        return departKey;
    }

    public void setDepartKey(String departKey) {
        this.departKey = departKey;
    }

    public String getSpecialityKey() {
        return specialityKey;
    }

    public void setSpecialityKey(String specialityKey) {
        this.specialityKey = specialityKey;
    }

    public Item getModule() {
        return module;
    }

    public void setModule(Item module) {
        this.module = module;
    }
}

package com.qfdqc.views.seattable;

public class Library {
    private String library_id;
    private String school;
    private String library_name;
    private double localx;
    private double localy;
    private int floors;

    public String getLibrary_id() {
        return library_id;
    }

    public void setLibrary_id(String library_id) {
        this.library_id = library_id;
    }

    public String getSchool() {
        return school;
    }

    public void setSchool(String school) {
        this.school = school;
    }

    public String getLibrary_name() {
        return library_name;
    }

    public void setLibrary_name(String ibrary_name) {
        this.library_name = ibrary_name;
    }

    public double getLocalx() {
        return localx;
    }

    public void setLocalx(double localx) {
        this.localx = localx;
    }

    public double getLocaly() {
        return localy;
    }

    public void setLocaly(double localy) {
        this.localy = localy;
    }

    public int getFloors() {
        return floors;
    }

    public void setFloors(int floors) {
        this.floors = floors;
    }

    @Override
    public String toString() {
        return "Library{" +
                "library_id='" + library_id + '\'' +
                ", school='" + school + '\'' +
                ", ibrary_name='" + library_name + '\'' +
                ", localx=" + localx +
                ", localy=" + localy +
                ", floors=" + floors +
                '}';
    }
}

package com.qfdqc.views.seattable;

public class Room {
    private String id;
    private String name;
    private int no;
    private int flooer;
    private int countx;
    private int county;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getNo() {
        return no;
    }

    public void setNo(int no) {
        this.no = no;
    }

    public int getFlooer() {
        return flooer;
    }

    public void setFlooer(int flooer) {
        this.flooer = flooer;
    }

    public int getCountx() {
        return countx;
    }

    public void setCountx(int countx) {
        this.countx = countx;
    }

    public int getCounty() {
        return county;
    }

    public void setCounty(int county) {
        this.county = county;
    }

    @Override
    public String toString() {
        return "Room{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", no=" + no +
                ", flooer=" + flooer +
                ", countx=" + countx +
                ", county=" + county +
                '}';
    }
}

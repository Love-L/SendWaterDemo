package com.qfdqc.views.seattable;

public class Seat {
    private String id;
    private int X;
    private int Y;
    private int angle;  //0 90  180 270
    private int state;  //0 空地 1 有位 2 有人 3 故障

    public Seat() {}

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getX() {
        return X;
    }

    public void setX(int x) {
        X = x;
    }

    public int getY() {
        return Y;
    }

    public void setY(int y) {
        Y = y;
    }

    public int getAngle() {
        return angle;
    }

    public void setAngle(int angle) {
        this.angle = angle;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    @Override
    public String toString() {
        return "Seat{" +
                "id='" + id + '\'' +
                ", X=" + X +
                ", Y=" + Y +
                ", angle=" + angle +
                ", state=" + state +
                '}';
    }
}

package com.example.ignas.walkup;

public class Trips {

    String date;
    long time;
    double distance;
    double avgSpeed;

    public Trips() {

    }

    public Trips (String date, long time, double distance, double avgSpeed) {

        this.date = date;
        this.time = time;
        this.distance = distance;
        this.avgSpeed = avgSpeed;
    }

}

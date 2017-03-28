package com.example.ar_slithers;

public class SnakeInfo {
    int colorNo;
    float X = 0, Y = 0, sensorX = 0, sensorY = 0, degree;
    private double distance;
    private float radius = 120;

    SnakeInfo(int colorNo) {
        this.colorNo = colorNo;
    }

    public void setDistance(double xDistance, double yDistance) {
        double xDistance2 = Math.pow(xDistance, 2);
        double yDistance2 = Math.pow(yDistance, 2);
        distance = Math.sqrt(xDistance2 + yDistance2);
    }

    public double getDistance() {
        return distance;
    }

    public float getRadius() {
        return radius;
    }
}

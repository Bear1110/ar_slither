package com.example.ar_slithers.Draw;

public class BodyInfo {
    public float X = 0, Y = 0, sensorX = 0, sensorY = 0, degree;
    private double distance;
    private float radius = 80;

    public BodyInfo() {

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
        // 假設距離 5 時 r = 80，而距離 (250 + 5) 時 r = 0
        float ratio = (float) ((200 - (distance - 5)) / 200);
        if (ratio <= 0) { ratio = 0; }
        return radius  * ratio;
    }
}

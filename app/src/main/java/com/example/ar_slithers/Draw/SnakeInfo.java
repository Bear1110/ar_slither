package com.example.ar_slithers.Draw;

import java.util.ArrayList;

public class SnakeInfo {
    public int colorNo;
//    public float X = 0, Y = 0, sensorX = 0, sensorY = 0, degree;
//    private double distance;
//    private float radius = 80;
    private float density; // according to different phone

    private double[][] body;
    public ArrayList<BodyInfo> drawBody = new ArrayList<>(); // a body's X and Y

    public SnakeInfo(double[][] body, int color) {
        this.body = body;
        this.colorNo = color;
        for (int i=0; i<body.length; i++) {
            drawBody.add(new BodyInfo());
        }
    }

//    public void setDistance(double xDistance, double yDistance) {
//        double xDistance2 = Math.pow(xDistance, 2);
//        double yDistance2 = Math.pow(yDistance, 2);
//        distance = Math.sqrt(xDistance2 + yDistance2);
//    }
//
//    public double getDistance() {
//        return distance;
//    }
//
//    public float getRadius() {
//        // 假設距離 5 時 r = 80，而距離 (250 + 5) 時 r = 0
//        float ratio = (float) ((200 - (distance - 5)) / 200);
//        if (ratio <= 0) { ratio = 0; }
//        return radius * density * ratio;
//    }
//

    public double[][] getBodyPos() {
        return body;
    }
}

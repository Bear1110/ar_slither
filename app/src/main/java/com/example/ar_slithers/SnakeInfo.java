package com.example.ar_slithers;

class SnakeInfo {
    int colorNo;
    float x, y, sensorX, sensorY, degree;

    SnakeInfo(int colorNo) {
        this.colorNo = colorNo;
        x = 0;
        y = 0;
        sensorX = 0;
        sensorY = 0;
        degree = 45;
    }
}

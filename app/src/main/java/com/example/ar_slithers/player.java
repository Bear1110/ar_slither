package com.example.ar_slithers;

import java.util.Random;

public class player {
    public int id;
    public String Lat="0.0";
    public String Lng="0.0";
    public String ip="test";

    public double[] remoteSelfCenter={0.0,0.0};
    final public double[] remoteFakeCenter;
    public double[] map = {0,0};
    public player(int id,String ip){
        Random ran = new Random();
        this.id = id;
        this.ip = ip;
        remoteFakeCenter = new double[]{ran.nextInt(100)-50,ran.nextInt(100)-50};
    }
}

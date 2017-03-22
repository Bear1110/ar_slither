package com.example.ar_slithers;

public class player {
    public int id;
    public String Lat="0.0";
    public String Lng="0.0";
    public String ip="test";
    public double[] map = {0,0};
    public double[] dif = { 0, 0 };

    public player(int id,String ip){
        this.id = id;
        this.ip = ip;
    }
}

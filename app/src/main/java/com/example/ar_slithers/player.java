package com.example.ar_slithers;

public class player {
    public int id;
    public String ip = "test";

    public double[] remoteCenter = {0.0, 0.0}; // 自身在遠處的進來座標
    public double[] fakeCenter = {0.0, 0.0}; // 遠處的人假的 地圖中心 (隨機位置)
    public double[][] body = new double[7][2];
    public int bodyLength = 1;
    public double[] map = {0, 0};

    public player(int id, String ip) {
        this.id = id;
        this.ip = ip;
    }
}

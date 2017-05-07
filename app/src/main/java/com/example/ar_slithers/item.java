package com.example.ar_slithers;

/**
 * Created by Bear on 2017/5/7.
 */

public class item {
    public float[] map = {0, 0}; // 物品
    public String type = "None";
    public boolean isDead = true;

    public item(String type) {
        this.type = type;
    }

    public void reBorm() {
        isDead = false;
        this.map[0] =(float) (Math.random()*100+1); //1~100
        this.map[1] =(float) (Math.random()*100+1);
    }
}
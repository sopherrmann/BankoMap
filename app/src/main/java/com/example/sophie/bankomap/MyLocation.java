package com.example.sophie.bankomap;

import android.graphics.Bitmap;
import android.location.Location;

class MyLocation {

    private Location location;
    private String session;
    private String open;
    private String bank;
    private int num;
    private String fee;
    private String info;
    private byte[] image;
    //private double lat;
    //private double lon;
    //private double alt;
    //private String time;

    public MyLocation(Location location, String session, String open, String bank, int num, String fee, String info, byte[] image) {
        this.location = location;
        this.session = session;
        this.open = open;
        this.bank = bank;
        this.num = num;
        this.fee = fee;
        this.info = info;
        this.image = image;
    }

    public Location getLocation() {
        return location;
    }
    public String getSession() {
        return session;
    }
    public String getOpen() {
        return open;
    }
    public String getBank() {
        return bank;
    }
    public int getNum() {
        return num;
    }
    public String getFee() {
        return fee;
    }
    public String getInfo() {
        return info;
    }
    public byte[] getImage() {
        return image;
    }
}
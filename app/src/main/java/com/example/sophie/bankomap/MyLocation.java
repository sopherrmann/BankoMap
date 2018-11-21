package com.example.sophie.bankomap;

class MyLocation {

    private String session;
    private double lat;
    private double lon;
    private double alt;
    private String time;

    public MyLocation(String session, double lat, double lon, double alt, String time) {
        this.session = session;
        this.lat = lat;
        this.lon = lon;
        this.alt = alt;
        this.time = time;
    }

    public String getSession() {
        return session;
    }

    public double getLat() {
        return lat;
    }

    public double getLon() {
        return lon;
    }

    public double getAlt() {
        return alt;
    }

    public String getTime() {
        return time;
    }
}

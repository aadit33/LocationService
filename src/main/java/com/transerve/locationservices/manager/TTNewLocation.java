package com.transerve.locationservices.manager;

public class TTNewLocation {
    private Double lat, lng,altitude;
    private Boolean isAccurate;
    private Float accuracy,bearing;
    private Object tag;

    public TTNewLocation(Double lat, Double lng, Boolean isAccurate, Float accuracy, Float bearing, Double altitude) {
        this.lat = lat;
        this.lng = lng;
        this.isAccurate = isAccurate;
        this.accuracy = accuracy;
        this.bearing = bearing;
        this.altitude = altitude;
    }

    public Double getLat() {
        return lat;
    }

    public void setLat(Double lat) {
        this.lat = lat;
    }

    public Double getLng() {
        return lng;
    }

    public void setLng(Double lng) {
        this.lng = lng;
    }

    public Boolean getAccurate() {
        return isAccurate;
    }

    public void setAccurate(Boolean accurate) {
        isAccurate = accurate;
    }

    public Float getAccuracy() {
        return accuracy;
    }

    public void setAccuracy(Float accuracy) {
        this.accuracy = accuracy;
    }

    public Double getAltitude() {
        return altitude;
    }

    public void setAltitude(Double altitude) {
        this.altitude = altitude;
    }

    public Float getBearing() {
        return bearing;
    }

    public void setBearing(Float bearing) {
        this.bearing = bearing;
    }

    public Object getTag() {
        return tag;
    }

    public void setTag(Object tag) {
        this.tag = tag;
    }
}


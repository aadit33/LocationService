package com.transerve.locationservices.manager;

public class TTNewLocation {
    private Double lat, lng;
    private Boolean isAccurate;
    private Float accuracy;

    public TTNewLocation(Double lat, Double lng, Boolean isAccurate, Float accuracy) {
        this.lat = lat;
        this.lng = lng;
        this.isAccurate = isAccurate;
        this.accuracy = accuracy;
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
}

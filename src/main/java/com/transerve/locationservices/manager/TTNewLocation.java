package com.transerve.locationservices.manager;

import org.json.JSONObject;

public class TTNewLocation {
    private Double lat, lng,altitude;
    private Boolean isAccurate;
    private Float accuracy,bearing;
    private JSONObject extraPaylod;

    public TTNewLocation(Double lat, Double lng, Boolean isAccurate, Float accuracy, Float bearing, Double altitude, JSONObject extraPayload) {
        this.lat = lat;
        this.lng = lng;
        this.isAccurate = isAccurate;
        this.accuracy = accuracy;
        this.bearing = bearing;
        this.altitude = altitude;
        this.extraPaylod = extraPayload;
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

    public JSONObject getExtraPaylod() {
        return extraPaylod;
    }

    public void setExtraPaylod(JSONObject extraPaylod) {
        this.extraPaylod = extraPaylod;
    }
}

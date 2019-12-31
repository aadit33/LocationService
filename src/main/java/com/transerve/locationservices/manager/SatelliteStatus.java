package com.transerve.locationservices.manager;

public class SatelliteStatus {

    private int svid;
    private GnssType gnssType;
    private Float cn0DbHz;
    private Boolean hasAlmanac;
    private Boolean hasEphemeris;
    private Boolean usedInFix;
    private Float elevationDegrees;
    private Float azimuthDegrees;
    private SbasType sbasType;
    private Boolean hasCarrierFrequency;
    private Float carrierFrequencyH ;

    public SatelliteStatus(int svid, GnssType gnssType, Float cn0DbHz, Boolean hasAlmanac, Boolean hasEphemeris, Boolean usedInFix, Float elevationDegrees, Float azimuthDegrees) {
        this.svid = svid;
        this.gnssType = gnssType;
        this.cn0DbHz = cn0DbHz;
        this.hasAlmanac = hasAlmanac;
        this.hasEphemeris = hasEphemeris;
        this.usedInFix = usedInFix;
        this.elevationDegrees = elevationDegrees;
        this.azimuthDegrees = azimuthDegrees;
        sbasType = SbasType.UNKNOWN;
        hasCarrierFrequency = false;
    }

    public int getSvid() {
        return svid;
    }

    public void setSvid(int svid) {
        this.svid = svid;
    }

    public GnssType getGnssType() {
        return gnssType;
    }

    public void setGnssType(GnssType gnssType) {
        this.gnssType = gnssType;
    }

    public Float getCn0DbHz() {
        return cn0DbHz;
    }

    public void setCn0DbHz(Float cn0DbHz) {
        this.cn0DbHz = cn0DbHz;
    }

    public Boolean getHasAlmanac() {
        return hasAlmanac;
    }

    public void setHasAlmanac(Boolean hasAlmanac) {
        this.hasAlmanac = hasAlmanac;
    }

    public Boolean getHasEphemeris() {
        return hasEphemeris;
    }

    public void setHasEphemeris(Boolean hasEphemeris) {
        this.hasEphemeris = hasEphemeris;
    }

    public Boolean getUsedInFix() {
        return usedInFix;
    }

    public void setUsedInFix(Boolean usedInFix) {
        this.usedInFix = usedInFix;
    }

    public Float getElevationDegrees() {
        return elevationDegrees;
    }

    public void setElevationDegrees(Float elevationDegrees) {
        this.elevationDegrees = elevationDegrees;
    }

    public Float getAzimuthDegrees() {
        return azimuthDegrees;
    }

    public void setAzimuthDegrees(Float azimuthDegrees) {
        this.azimuthDegrees = azimuthDegrees;
    }

    public SbasType getSbasType() {
        return sbasType;
    }

    public void setSbasType(SbasType sbasType) {
        this.sbasType = sbasType;
    }

    public Boolean getHasCarrierFrequency() {
        return hasCarrierFrequency;
    }

    public void setHasCarrierFrequency(Boolean hasCarrierFrequency) {
        this.hasCarrierFrequency = hasCarrierFrequency;
    }

    public Float getCarrierFrequencyH() {
        return carrierFrequencyH;
    }

    public void setCarrierFrequencyH(Float carrierFrequencyH) {
        this.carrierFrequencyH = carrierFrequencyH;
    }
}

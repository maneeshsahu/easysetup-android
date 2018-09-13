package io.artik.easysetup.api.model;

import com.google.gson.annotations.SerializedName;

/**
 * Created by vsingh on 06/03/17.
 */

public class AccessPointInfo {

    @SerializedName("ssid")
    private String ssid;
    @SerializedName("bssid")
    private String bssid;
    @SerializedName("security")
    private String security;
    @SerializedName("signal")
    private int signal;

    public String getSsid() {
        return ssid;
    }

    public void setSsid(String ssid) {
        this.ssid = ssid;
    }

    public String getBssid() {
        return bssid;
    }

    public void setBssid(String bssid) {
        this.bssid = bssid;
    }

    public String getSecurity() {
        return security;
    }

    public void setSecurity(String security) {
        this.security = security;
    }

    public int getSignal() {
        return signal;
    }

    public void setSignal(int signal) {
        this.signal = signal;
    }

    public boolean isSecure(){

        return security.contains("Secure");

    }
}

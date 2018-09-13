package io.artik.easysetup.wifi;

import io.artik.easysetup.util.Constants;

public class WiFiNetwork {

    public int networkId = -1;
    private String SSID;
    private String authentication;
    private String password;

    public WiFiNetwork(String SSID, String authentication, String password) {
        this.SSID = SSID;
        this.authentication = authentication;
        this.password = password;
    }

    public boolean isSameConfig(String SSID, String authentication,
                                String password) {

        if (SSID == null || authentication == null)
            return false;

        if (!this.SSID.equals(SSID))
            return false;

        if (!this.authentication.equals(authentication))
            return false;

        // If authentication is NOT open
        if (!this.authentication.equals(Constants.OPEN)) {
            // If the autentication is NOT open but password is null
            if (password == null)
                return false;
            // If password is the same we
            if (!this.password.equals(password))
                return false;
        }
        // Data matches!
        return true;
    }
}
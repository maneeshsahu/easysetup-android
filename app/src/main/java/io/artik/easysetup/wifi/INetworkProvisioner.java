package io.artik.easysetup.wifi;

import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;

import java.util.List;

public interface INetworkProvisioner {

    boolean initWifiNetworkProvisioner();

    boolean isAvailable();

    boolean isProvisioned();

    boolean setWifiConfiguration(String SSID, String encryption, String password);

    boolean setWifiConfiguration(WifiConfiguration configuration);

    WifiConfiguration getWifiConfiguration();

    void unregisterBroadcastReceiver();

    void releaseWifiConfiguration();

    void requestScan();

    void stopScan();

    void registerBroadcastReceiver();

    interface Listener {

        void onScanResult(List<ScanResult> wifiList);

        void onConnecting();

        void onAuthenticating();

        void onObtainingIP();

        public void onConnected(WiFiNetwork wifiNetwork);

        public void onDisconnected();

        public void onFailed();

    }


}


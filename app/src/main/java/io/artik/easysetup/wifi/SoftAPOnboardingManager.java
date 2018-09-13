package io.artik.easysetup.wifi;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.net.NetworkRequest;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.text.format.Formatter;
import android.util.Log;

import java.util.List;

import io.artik.easysetup.api.client.SoftAPCallHandling;
import io.artik.easysetup.util.MDNSBrowser;
import io.artik.easysetup.util.Module;

import static android.os.Build.VERSION.SDK_INT;
import static android.os.Build.VERSION_CODES.M;

/**
 * Created by vsingh on 06/03/17.
 */

public class SoftAPOnboardingManager {

    private Context mContext;
    private Module module;
    private String networkSSID;
    WifiManager wifiManager;
    private final String ap_address = "192.168.10.1";
    private SoftAPCallHandling.SoftAPListener mListener;
    SoftAPCallHandling softAPCallHandling;
    SoftAPCallHandling nodeCallHandling;
    private String targetSSID;
    private MDNSBrowser.HubListener mNodeListener;
    private MDNSBrowser mdnsBrowser;
    private String TAG = "SoftAPOnboardingManager";
    private boolean discovered = false;
    private boolean module_wifi_bound_process = false;

    private ConnectivityManager connectivityManager;


    public SoftAPOnboardingManager(Context mContext, Module module, SoftAPCallHandling.SoftAPListener listener) {
        this.mContext = mContext;
        this.module = module;

        if (module == null || module.getMacAddress() == null) {
            Log.e(TAG, "Error in SoftAPOnboarding");
        }

        networkSSID = "ARTIK_" + module.getMacAddress().toLowerCase();
        wifiManager = (WifiManager)mContext.getSystemService(Context.WIFI_SERVICE);
        wifiManager.setWifiEnabled(true);
        connectivityManager = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        mListener = listener;
        softAPCallHandling = new SoftAPCallHandling(mContext, ap_address, mListener);
    }

    public void bindToRequiredNetwork(Network network) {
        if (SDK_INT >= M) {
            connectivityManager.bindProcessToNetwork(network);
            connectivityManager.reportNetworkConnectivity(network, true);
        } else {
            connectivityManager.setProcessDefaultNetwork(network);
        }
    }

    public void connectToModuleAP() {

        List<WifiConfiguration> list = wifiManager.getConfiguredNetworks();

        if (list == null) {
            return;
        }

        for( WifiConfiguration i : list ) {
            if(i.SSID != null && i.SSID.equals("\"" + networkSSID + "\"")) {
                Log.i(TAG, "Removing network " + i.SSID);
                wifiManager.removeNetwork(i.networkId);
            } else {
                wifiManager.disableNetwork(i.networkId);
            }
        }

        /* If the network is not found */
        WifiConfiguration conf = new WifiConfiguration();

        conf.SSID = "\"" + networkSSID + "\"";
        conf.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
        assignHighestPriority(conf);
        wifiManager.addNetwork(conf);

        list = wifiManager.getConfiguredNetworks();

        if (list == null) {
            return;
        }

        for( WifiConfiguration i : list ) {
            if(i.SSID != null && i.SSID.equals("\"" + networkSSID + "\"")) {
                wifiManager.disconnect();
                wifiManager.enableNetwork(i.networkId, true);
                wifiManager.reconnect();
                Log.i(TAG, "Enabling network " + networkSSID);
                requestNetwork(networkSSID);
                break;
            }
        }
    }

    public void requestNetwork(final String ssid) {

        NetworkRequest.Builder request = new NetworkRequest.Builder();
        request.addTransportType(NetworkCapabilities.TRANSPORT_WIFI);
        module_wifi_bound_process = false;

        connectivityManager.requestNetwork(request.build(), new ConnectivityManager.NetworkCallback() {

            @Override
            public void onAvailable(Network network) {

                NetworkInfo networkInfo = connectivityManager.getNetworkInfo(network);
                Log.i(TAG, "Network is Available. Network Info: " + networkInfo);

                if (ssid != null && networkInfo != null && networkInfo.getExtraInfo().contains(ssid)) {
                    Log.i(TAG, "Binding Network to " + ssid);
                    connectivityManager.unregisterNetworkCallback(this);
                    bindToRequiredNetwork(network);
                    module_wifi_bound_process = true;
                }

            }
        });
    }

    public void unbindNetwork() {
        Log.i(TAG, "Removing any network bindings");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            connectivityManager.bindProcessToNetwork(null);
        } else {
            connectivityManager.setProcessDefaultNetwork(null);
        }
    }

    public void connectToAP() {

        List<WifiConfiguration> list = wifiManager.getConfiguredNetworks();

        if (list == null) {
            return;
        }

        WifiConfiguration apConfiguration = null;

        for( WifiConfiguration i : list ) {
            if(i.SSID != null && i.SSID.equals("\"" + targetSSID + "\"")) {
                apConfiguration = i;
            } else {
                wifiManager.enableNetwork(i.networkId, false);
            }
        }

        if (apConfiguration != null) {
            assignHighestPriority(apConfiguration);
            wifiManager.disconnect();
            wifiManager.enableNetwork(apConfiguration.networkId, true);
            wifiManager.reconnect();
            requestNetwork(targetSSID);
        }
    }

    private void assignHighestPriority(WifiConfiguration config) {
        List<WifiConfiguration> configuredNetworks = wifiManager.getConfiguredNetworks();
        if (configuredNetworks != null) {
            for (WifiConfiguration existingConfig : configuredNetworks) {
                if (config.priority <= existingConfig.priority) {
                    config.priority = existingConfig.priority + 1;
                }
            }
        }
    }

    public void setIPAddress(String ipaddress) {
        nodeCallHandling = new SoftAPCallHandling(mContext, ipaddress, mListener);
    }

    public void discoverNode() {
        mNodeListener = new MDNSBrowser.HubListener() {
            @Override
            public void onNodeFound(String hubInfo) {
                if (mListener != null) {
                    mdnsBrowser.stopDiscovery();
                    if (!discovered) {
                        discovered = true;
                        mListener.onNodeDiscovered(hubInfo);
                    }
                }

            }
        };

        mdnsBrowser = new MDNSBrowser(mContext, mNodeListener, module.getMacAddress().toLowerCase());
        mdnsBrowser.discoverServices();
    }

    @SuppressWarnings("deprecation")
    public boolean isConnectedToSoftAP() {
       if (wifiManager.isWifiEnabled()) {
            WifiInfo wifiInfo = wifiManager.getConnectionInfo();
            if (wifiInfo != null) {
                NetworkInfo.DetailedState state = WifiInfo.getDetailedStateOf(wifiInfo.getSupplicantState());
                Log.i(TAG, "Current wifi " + wifiInfo.getSSID() + " and state is " + state);
                if (wifiInfo.getSSID().contains(networkSSID)
                        && (state == NetworkInfo.DetailedState.CONNECTED || state == NetworkInfo.DetailedState.OBTAINING_IPADDR)
                        && Formatter.formatIpAddress(wifiInfo.getIpAddress()).contains("192.168.10.")
                        && module_wifi_bound_process) {
                    return true;
                }
            }
        }
        return false;
    }

    public void fetchWifiAPList() {
        softAPCallHandling.fetchAPList();
    }

    public void passNetworkConfig(String ssid, String password){
        targetSSID = ssid;
        softAPCallHandling.passAPConfiguration(ssid, password);
    }

}

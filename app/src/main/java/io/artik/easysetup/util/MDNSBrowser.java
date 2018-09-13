package io.artik.easysetup.util;

import android.content.Context;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;
import android.util.Log;

import java.util.ArrayList;
import java.util.Map;

/**
 * Helper MDNS-SD Browser to discover artik modules
 * Created by vsingh on 07/03/17.
 */

public class MDNSBrowser {

    Context mContext;
    NsdManager mNsdManager;
    NsdManager.ResolveListener mResolveListener;
    NsdManager.DiscoveryListener mDiscoveryListener;

    public static final String SERVICE_TYPE = "_http._tcp.";
    public static final String TAG = "MDNSBrowser";
    public ArrayList<String> Hubs;
    public ArrayList<NsdServiceInfo> ServiceList;
    private String searchString;
    private final MDNSBrowser.HubListener mListener;


    public interface  HubListener{
        void onNodeFound(String hubInfo);
    }

    public MDNSBrowser(Context mContext, MDNSBrowser.HubListener listener, String search) {
        this.mContext = mContext;
        mNsdManager = (NsdManager) mContext.getSystemService(Context.NSD_SERVICE);
        searchString = search;
        mListener = listener;
        initializeResolveListener();
    }

    public void initializeDiscoveryListener() {
        mDiscoveryListener = new NsdManager.DiscoveryListener() {
            @Override
            public void onDiscoveryStarted(String regType) {
                Log.d(TAG, "Service discovery started");
            }
            @Override
            public void onServiceFound(NsdServiceInfo service) {
                Log.d(TAG, "Service discovery success" + service);
                ServiceList.add(service);
                mNsdManager.resolveService(service, new MyResolveListener());
            }
            @Override
            public void onServiceLost(NsdServiceInfo service) {
                Log.e(TAG, "service lost" + service);

            }
            @Override
            public void onDiscoveryStopped(String serviceType) {
                Log.i(TAG, "Discovery stopped: " + serviceType);
            }
            @Override
            public void onStartDiscoveryFailed(String serviceType, int errorCode) {
                Log.i(TAG, "Discovery failed: Error code:" + errorCode);
            }
            @Override
            public void onStopDiscoveryFailed(String serviceType, int errorCode) {
                Log.i(TAG, "Discovery failed: Error code:" + errorCode);
            }
        };
    }
    public void initializeResolveListener() {
        mResolveListener = new NsdManager.ResolveListener() {
            @Override
            public void onResolveFailed(NsdServiceInfo serviceInfo, int errorCode) {
                Log.i(TAG, "Resolve failed" + errorCode);
            }
            @Override
            public void onServiceResolved(NsdServiceInfo serviceInfo) {
                Log.i(TAG, "Resolve Succeeded. " + serviceInfo);

                Map<String, byte[]> data = serviceInfo.getAttributes();
                Hubs.add(new String(data.get("data")));
            }
        };
    }

    public void discoverServices() {
        stopDiscovery();  // Cancel any existing discovery request
        Hubs = new ArrayList<String>();
        ServiceList = new ArrayList<NsdServiceInfo>();
        initializeDiscoveryListener();
        mNsdManager.discoverServices(SERVICE_TYPE, NsdManager.PROTOCOL_DNS_SD, mDiscoveryListener);
    }

    public void stopDiscovery() {
        if (mDiscoveryListener != null) {
            try {
                mNsdManager.stopServiceDiscovery(mDiscoveryListener);
            } finally {
            }
            mDiscoveryListener = null;
        }
    }

    private class MyResolveListener implements NsdManager.ResolveListener {

        @Override
        public void onResolveFailed(NsdServiceInfo serviceInfo, int errorCode) {
            Log.i(TAG, "Resolve failed" + errorCode);
        }
        @Override
        public void onServiceResolved(NsdServiceInfo serviceInfo) {
            Log.i(TAG, "Resolve Succeeded. " + serviceInfo.getServiceName() + " " + serviceInfo.getHost());

            if (serviceInfo.getServiceName().contains(searchString)) {
                Log.i(TAG, "Found a node ");
                String ip = serviceInfo.getHost().toString().substring(1);
                boolean registered = false;
                stopDiscovery();
                for (String dev : Hubs) {
                    if (dev.equals(ip))
                        registered = true;
                }
                if (!registered)
                    mListener.onNodeFound(ip);
            }
        }
    }
}

package io.artik.easysetup.ble;


import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.cert.CertificateException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import io.artik.easysetup.R;
import io.artik.easysetup.util.Constants;
import io.artik.easysetup.util.Constants.REGISTRATION_STATE;
import io.artik.easysetup.util.Module;

public class ControlBluetoothConnection {
    private static String TAG = ControlBluetoothConnection.class.getSimpleName();

    private Context mContext;
    private static ControlBluetoothConnection mControlBluetoothConnection;
    private BLEConnectionManager mBLEConnectionMgr;
    private Module mModule;

    private static int RSSI = -35;
    private static Boolean mWifiOnboarding = true;
    final String api_url = "https://api.artik.cloud/v1.1/devices/registrations/pin";
    protected int mRetryCount;
    private boolean DEBUG = false;
    private static Handler mHandler;
    private String mAccessToken;
    private String mDeviceTypeId, mChallengePin, mDeviceDID, mDeviceName, mIpAddress;
    private JSONArray apList = new JSONArray();
    private boolean isServiceBound = false;


    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            mBLEConnectionMgr = ((BLEConnectionManager.LocalBinder) service).getService(mContext.getApplicationContext(), mHandler);
            if (mBLEConnectionMgr.init()) {

                isServiceBound = true;
                mBLEConnectionMgr.setRegistrationState(REGISTRATION_STATE.REGIS_STATE_START);
                mBLEConnectionMgr.setModuleMacAddress(mModule.getMacAddress());

                if(mBLEConnectionMgr.isEnabled()) {
                    UUID serviceUuid = UUID.fromString(ArtikGattServices.SERVICE_UUID_FORMAT + mModule.getMacAddress().replace(":",""));
                    mBLEConnectionMgr.startScanLeDevices(serviceUuid, RSSI);
                }else
                    mBLEConnectionMgr.enableBluetooth();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBLEConnectionMgr = null;
            isServiceBound = false;
        }
    };

    public static Handler getHandler() {
        return mHandler;
    }

    private ControlBluetoothConnection(Context context) {
        this.mContext = context;

        mHandler = new Handler(Looper.getMainLooper()) {

            @Override
            public void handleMessage(Message inputMessage) {
                Constants.BLE_STATUS_MESSAGES msg = Constants.BLE_STATUS_MESSAGES.fromId(inputMessage.what);
                Log.d(TAG, "msg value ===" + msg);
                switch (msg) {

                    case START_SCANNING:
                        mRetryCount = 0;
                        Log.d(TAG, "START_SCANNING");
                        if (mWifiOnboarding) {
                            UUID serviceUuid = UUID.fromString(ArtikGattServices.SERVICE_UUID_FORMAT + mModule.getMacAddress().replace(":",""));
                            mBLEConnectionMgr.startScanLeDevices(serviceUuid, RSSI);
                        } else {
                            mBLEConnectionMgr.startScanLeDevices(ArtikGattServices.SDR_REGISTRATION_UUID, RSSI);
                        }

                        break;
                    case STOP_SCANNING:
                        Log.d(TAG, "STOP_SCANNING");
                        mBLEConnectionMgr.stopScanLeDevices(false);
                        break;
                    case DETECTED_DEVICE:
                        Log.d(TAG, "DETECTED_DEVICE");
                        if (mWifiOnboarding) {
                            Log.d(TAG, "CONNECT_DEVICE");
                            try {
                                mBLEConnectionMgr.connectGatt();
                            } catch (Exception e) {
                                e.printStackTrace();

                            }
                        } else {
                            Log.d(TAG, "DETECTED_SDR_DEVICE");
                            mDeviceName = (String) inputMessage.obj;
                            mBLEConnectionMgr.connectGatt();
                        }
                        break;
                    case DETECTED_SDR_DEVICE:
                        if (!mWifiOnboarding) {
                            Log.d(TAG, "DETECTED_SDR_DEVICE");
                            mDeviceName = (String) inputMessage.obj;
                            Log.d(TAG, "CONNECT_DEVICE");
                            mBLEConnectionMgr.connectGatt();
                        }
                        break;
                    case GATT_CONNECTED:
                        Log.d(TAG, "GATT_CONNECTED");
                        // TODO Maybe discoverServices should be called from here instead of doing it
                        mBLEConnectionMgr.discoverServices();
                        break;
                    case GATT_DISCONNECTED:
                        Log.d(TAG, "GATT_DISCONNECTED");
                        if (DEBUG)
                            Toast.makeText(mContext.getApplicationContext(), "GATT DISCONNECTED", Toast.LENGTH_SHORT).show();
                        break;
                    case GATT_FAILED:
                        Log.d(TAG, "GATT_FAILED");
                        if (mBLEConnectionMgr != null) {
                            if (mRetryCount < 1) {
                                if (DEBUG)
                                    Toast.makeText(mContext.getApplicationContext(), "Trying to connect module", Toast.LENGTH_SHORT).show();
                                Log.d(TAG, "Reconnecting to GATT");

                                mBLEConnectionMgr.connectGatt();
                                mRetryCount++;
                            } else {
                                Log.d(TAG, "Reconnecting to GATT failed");
                                Intent intent=new Intent(Constants.BOARD_CONNECTION_STATUS.BLE_DISCONNECTED.toString());
                                intent.putExtra(Constants.ERROR_STATUS, mContext.getResources().getString(R.string.bluetooth_connection_failed));
                                mContext.sendBroadcast(intent);
                                LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
                                disconnectModule();
                            }
                        }
                        break;
                    case GATT_SERVICES_DISCOVERED:
                        Log.d(TAG, "GATT_SERVICES_DISCOVERED");
                        if (mWifiOnboarding) {
                            if (DEBUG)
                                Toast.makeText(mContext.getApplicationContext(), "SERVICES DISCOVERED", Toast.LENGTH_SHORT).show();
                            Log.d(TAG, "GATT_SERVICES_DISCOVERED for Wifi Onboarding");
                            mBLEConnectionMgr.setCharacteristicNotification(ArtikGattServices.CHARACTERISTIC_STATUS, true);
                            mBLEConnectionMgr.setCharacteristicNotification(ArtikGattServices.CHARACTERISTIC_LONG_STATUS, true);

                            // Once services are discovered read VendorID and DeviceID
                            mBLEConnectionMgr.readCharacteristic(ArtikGattServices.CHARACTERISTIC_VENDOR_ID);
                            //mBLEConnectionMgr.readCharacteristic(ArtikGattServices.CHARACTERISTIC_DEVICE_ID);
                        } else {
                            Log.d(TAG, "GATT_SERVICES_DISCOVERED for Device Onboarding");
                            mBLEConnectionMgr.setCharacteristicNotification(ArtikGattServices.CHALLENGE_PIN_UUID, true);
                            mBLEConnectionMgr.setCharacteristicNotification(ArtikGattServices.DEVICE_TOKEN_UUID, true);

                            mBLEConnectionMgr.readCharacteristic(ArtikGattServices.DEVICE_TYPE_ID_UUID);
                        }
                        mContext.sendBroadcast(new Intent(Constants.BOARD_CONNECTION_STATUS.BLE_CONNECTED.toString()));

                        break;
                    case DEVICE_TYPE_ID_READ:

                        if (inputMessage.obj != null) {
                            mDeviceTypeId = (String) inputMessage.obj;
                            Log.d(TAG, "DEVICE_TYPE_ID_READ : " + mDeviceTypeId);
                        }
                        Intent inmsg = new Intent(Constants.BOARD_CONNECTION_STATUS.DTID_READ.toString());
                        inmsg.putExtra("DTID", mDeviceTypeId);
                        LocalBroadcastManager.getInstance(mContext).sendBroadcast(inmsg);

                        break;
                    case DEVICE_CHALLENGE_PIN_READ:
                        mChallengePin = (String) inputMessage.obj;
                        Log.d(TAG, "DEVICE_CHALLENGE_PIN_READ : " + mChallengePin);
                        confirmUserWithSAMI();

                        break;
                    case DEVICE_DID_READ:
                        if (!mWifiOnboarding) {
                            mDeviceDID = (String) inputMessage.obj;
                            Log.d(TAG, "DEVICE_DID_READ : " + mDeviceDID);

                        } else {
                            LocalBroadcastManager.getInstance(mContext).sendBroadcast(new Intent(Constants.BOARD_CONNECTION_STATUS.REGISTRATION_COMPLETE.toString()));
                        }

                        break;
                    case VENDOR_ID_READ:
                        if (mWifiOnboarding) {
                            Log.d(TAG, "VENDOR_ID_READ");
                            LocalBroadcastManager.getInstance(mContext).sendBroadcast(new Intent(Constants.BOARD_CONNECTION_STATUS.REGISTRATION_COMPLETE.toString()));
                        }
                        break;
                    case DEVICE_ID_READ:
                        if (mWifiOnboarding) {
                            Log.d(TAG, "DEVICE_ID_READ");
                            Log.e(TAG, "DEVICE_ID = " + inputMessage.obj);
                        }
                        break;
                    case WIFI_AP_READ:
                        JSONObject ap = null;
                        try {
                            ap = new JSONObject((String)inputMessage.obj);
                            Log.d(TAG, "AP Read " + ap.toString());
                            if (ap.getDouble("signal") != 0) {
                                apList.put(ap);
                                mBLEConnectionMgr.readCharacteristic(ArtikGattServices.CHARACTERISTIC_WIFIAP);
                            } else {
                                /* Send the AP List */
                                Log.d(TAG, "Send the AP List");
                                Intent intent = new Intent(Constants.BOARD_CONNECTION_STATUS.WIFI_LIST_RECEIVED.toString());
                                intent.putExtra(Constants.WIFI_LIST, apList.toString());
                                LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                            Log.e(TAG, "WIFI AP Read " + (String)inputMessage.obj);
                            mBLEConnectionMgr.readCharacteristic(ArtikGattServices.CHARACTERISTIC_WIFIAP);
                        }
                        break;
                    case GET_WIFI_LIST:
                        apList = new JSONArray();
                        if (mBLEConnectionMgr == null) {
                            Log.e(TAG, "BLE Connection Mgr is null.");
                            return;
                        }
                        mBLEConnectionMgr.readCharacteristic(ArtikGattServices.CHARACTERISTIC_WIFIAP);
                        break;
                    case SEND_WIFI_CRED:
                        final Map<String, String> wifi_data = (HashMap<String, String>) inputMessage.obj;
                        Log.d(TAG, "Sending WIFI Credentials :");
                        if (mBLEConnectionMgr == null) {
                            Log.e(TAG, "BLE Connection Mgr is null.");
                            return;
                        }
                        // At this point we can execute all necessary writes for onboarding
                        new Thread(){
                            @Override
                            public void run() {
                                super.run();
                                mBLEConnectionMgr.writeLongCharacteristic(ArtikGattServices.CHARACTERISTIC_SSID, wifi_data.get("ssid"));
                                try {
                                    Thread.sleep(2000);
                                    if (mBLEConnectionMgr != null) {
                                        mBLEConnectionMgr.writeCharacteristic(ArtikGattServices.CHARACTERISTIC_AUTH, wifi_data.get("encryption"));
                                        Thread.sleep(1000);
                                    }
                                    if (mBLEConnectionMgr != null) {
                                        if (!wifi_data.get("encryption").equals("OPEN"))
                                            mBLEConnectionMgr.writeLongCharacteristic(ArtikGattServices.CHARACTERISTIC_PASS, wifi_data.get("password"));
                                        else
                                            mBLEConnectionMgr.writeCharacteristic(ArtikGattServices.CHARACTERISTIC_PASS, "");
                                    }
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }
                        }.start();

                        break;
                    case STATUS_NOTIFIED:

                        String status = (String) inputMessage.obj;
                        Log.d(TAG, "STATUS_NOTIFIED : " + status);
                        if (status.equals(ArtikGattServices.STATUS_RECVD_PIN)) {
                            Log.e(TAG, "CHALLENGE_PIN_UUID");
                            mBLEConnectionMgr.readCharacteristic(ArtikGattServices.CHALLENGE_PIN_UUID);
                        } else if (status.equals(ArtikGattServices.STATUS_RECVD_TOKEN)) {
                            mBLEConnectionMgr.readCharacteristic(ArtikGattServices.DEVICE_ID_CHARACTERISTIC_UUID);
                        }
                        break;
                    case LONG_STATUS_NOTIFIED:

                        String longStatus = (String) inputMessage.obj;
                        Log.d(TAG, "LONG_STATUS_NOTIFIED : " + longStatus);
                        if ((longStatus.equals(ArtikGattServices.LONG_STATUS_CONN_ESTAB)) && mWifiOnboarding) {
                            mBLEConnectionMgr.readCharacteristic(ArtikGattServices.CHARACTERISTIC_IPADDRESS);
                        } else if (isError(longStatus)) {
                            handleErrorMessage(longStatus);
                        }
                        break;
                    case IPADDRESS_READ:
                        String ipAddress = (String) inputMessage.obj;
                        Log.d(TAG, "ipAddress : " + ipAddress);
                        mIpAddress = ipAddress;
                        mBLEConnectionMgr.readCharacteristic(ArtikGattServices.DEVICE_TYPE_ID_UUID);
                        if (mWifiOnboarding) {
                            Intent in = new Intent(Constants.BOARD_CONNECTION_STATUS.WIFI_CONNECTED.toString());
                            in.putExtra("IPADDRESS", ipAddress);
                            LocalBroadcastManager.getInstance(mContext).sendBroadcast(in);
                        }
                        break;
                    case DISCONNECT_TARGET:
                        Log.d(TAG, "DISCONNECT_TARGET");
                        mBLEConnectionMgr.writeCharacteristic(ArtikGattServices.CHARACTERISTIC_COMMAND, new byte[]{0x02});
                        break;
                    case RESET_TARGET:
                        Log.d(TAG, "RESET_TARGET");
                        mBLEConnectionMgr.writeCharacteristic(ArtikGattServices.CHARACTERISTIC_COMMAND, new byte[]{0x03});
                        break;
                    case ERROR_INVALID_MAC_ADDRESS:
                        Log.d(TAG, "ERROR_INVALID_MAC_ADDRESS");
                        break;

                    case DISCONNECT_GATTSERVICE:
                        mBLEConnectionMgr.disconnectGatt();
                        mBLEConnectionMgr.discoverServices();
                        mBLEConnectionMgr = null;
                        break;
                    default:
                        super.handleMessage(inputMessage);

                }
            }


        };
    }

    public void startClassicRegistration(final String did, final String token) {

        if (mBLEConnectionMgr != null && !did.isEmpty() && !token.isEmpty()) {
            new Thread(){
                @Override
                public void run() {
                    super.run();
                    mBLEConnectionMgr.writeLongCharacteristic(ArtikGattServices.CHARACTERISTIC_CLOUD_DEVICE_ID, did);
                    try {
                        Thread.sleep(2000);
                        if (mBLEConnectionMgr != null) {
                            mBLEConnectionMgr.writeLongCharacteristic(ArtikGattServices.CHARACTERISTIC_CLOUD_DEVICE_TOKEN, token);
                            Thread.sleep(2000);
                        }
                        if (mBLEConnectionMgr != null) {
                            mBLEConnectionMgr.writeCharacteristic(ArtikGattServices.START_REGISTRATION_UUID, "1");
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }.start();
        }
    }

    public void startSecureRegistration() {
        if (mBLEConnectionMgr != null) {
            mBLEConnectionMgr.writeCharacteristic(ArtikGattServices.START_REGISTRATION_UUID, "1");
        }
    }

    public static ControlBluetoothConnection getInstance(Context context) {
        if (mControlBluetoothConnection == null) {
            mControlBluetoothConnection = new ControlBluetoothConnection(context);
        }
        return mControlBluetoothConnection;
    }

    public void connectBLE(Module module) {

        if (isServiceBound) {
            disconnectModule();
        }

        this.mModule = module;
        Intent gattServiceIntent = new Intent(mContext.getApplicationContext(), BLEConnectionManager.class);
        mContext.getApplicationContext().bindService(gattServiceIntent, mServiceConnection, Context.BIND_AUTO_CREATE);

    }


    public void disconnectModule() {
        if (mBLEConnectionMgr != null) {
            mBLEConnectionMgr.stopScanLeDevices(false);
            mBLEConnectionMgr.disconnectGatt();
        }

        if (isServiceBound)
            mContext.getApplicationContext().unbindService(mServiceConnection);

        isServiceBound = false;
        mBLEConnectionMgr = null;
    }

    private boolean isError(String status) {
        switch (status) {
            case ArtikGattServices.LONG_STATUS_START_REGISTRATION_ERROR:
            case ArtikGattServices.LONG_STATUS_COMPLETE_REGISTRATION_ERROR:
            case ArtikGattServices.LONG_STATUS_INVALID_SSID:
            case ArtikGattServices.LONG_STATUS_WRONG_PASSWORD:
            case ArtikGattServices.LONG_STATUS_INTERNET_UNAVAILABLE:
            case ArtikGattServices.LONG_STATUS_NO_IPADDRESS:
            case ArtikGattServices.LONG_STATUS_INVALID_WIFI_STATE:
                return true;
            default:
                return false;

        }
    }

    /**
     * Error Message handler
     * @param status
     */
    private void handleErrorMessage(String status) {
        int resId;
        Intent intent = null;


        Log.e(TAG, "status = " + status);
        switch (status) {
            case ArtikGattServices.LONG_STATUS_START_REGISTRATION_ERROR:
                resId = R.string.registration_error;
                intent = new Intent(Constants.BOARD_CONNECTION_STATUS.REGISTRATION_FAILED.toString());
                intent.putExtra(Constants.ERROR_STATUS, mContext.getResources().getString(resId));
                break;
            case ArtikGattServices.LONG_STATUS_COMPLETE_REGISTRATION_ERROR:
                resId = R.string.registration_complete_error;
                intent = new Intent(Constants.BOARD_CONNECTION_STATUS.REGISTRATION_FAILED.toString());
                intent.putExtra(Constants.ERROR_STATUS, mContext.getResources().getString(resId));
                break;
            case ArtikGattServices.LONG_STATUS_INVALID_SSID:
                resId = R.string.invalid_ssid;
                intent = new Intent(Constants.BOARD_CONNECTION_STATUS.WIFI_FAILED.toString());
                intent.putExtra(Constants.ERROR_STATUS, mContext.getResources().getString(resId));
                break;
            case ArtikGattServices.LONG_STATUS_WRONG_PASSWORD:

                resId = R.string.invalid_password;
                intent = new Intent(Constants.BOARD_CONNECTION_STATUS.WIFI_FAILED.toString());
                intent.putExtra(Constants.ERROR_STATUS, mContext.getResources().getString(resId));
                break;
            case ArtikGattServices.LONG_STATUS_NO_IPADDRESS:
                resId = R.string.no_ip_address;

                intent = new Intent(Constants.BOARD_CONNECTION_STATUS.WIFI_FAILED.toString());
                intent.putExtra(Constants.ERROR_STATUS, mContext.getResources().getString(resId));

                break;
            case ArtikGattServices.LONG_STATUS_INTERNET_UNAVAILABLE:
                resId = R.string.internet_unavailable;
                intent = new Intent(Constants.BOARD_CONNECTION_STATUS.INTERNET_UNAVAILABLE.toString());
                intent.putExtra(Constants.ERROR_STATUS, mContext.getResources().getString(resId));

                break;
            case ArtikGattServices.LONG_STATUS_INVALID_WIFI_STATE:
                resId = R.string.invalid_wifi_status;
                intent = new Intent(Constants.BOARD_CONNECTION_STATUS.WIFI_FAILED.toString());
                intent.putExtra(Constants.ERROR_STATUS, mContext.getResources().getString(resId));
                break;
            default:
                intent = new Intent(Constants.BOARD_CONNECTION_STATUS.WIFI_FAILED.toString());
                intent.putExtra(Constants.ERROR_STATUS, mContext.getString(R.string.technical_code_on_error) + status);
                break;

        }

        LocalBroadcastManager.getInstance(mContext).
                sendBroadcast(intent);
    }

    private SSLSocketFactory getSelfSignedSSLSocketFactory() {
        try {

            // Create a trust manager that does not validate certificate chains
            final TrustManager[] trustAllCerts = new TrustManager[] {
                    new X509TrustManager() {
                        @Override
                        public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType) throws CertificateException {
                        }

                        @Override
                        public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType) throws CertificateException {
                        }

                        @Override
                        public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                            return new java.security.cert.X509Certificate[]{};
                        }
                    }
            };

            // Install the all-trusting trust manager
            final SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
            // Create an ssl socket factory with our all-trusting manager
            final SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();

            HttpsURLConnection.setDefaultSSLSocketFactory(sslSocketFactory);
            HttpsURLConnection.setDefaultHostnameVerifier(new HostnameVerifier() {
                @Override
                public boolean verify(String arg0, SSLSession arg1) {
                    return true;
                }
            });

            return sslSocketFactory;
        } catch (Exception e) {
            throw new AssertionError(e);
        }
    }
    /*
        Commenting these two functions as they require the smartphone to be connected to the same wifi network as the gateway.
        These are simply alternative methods of doing the cloud onboarding.

        private void startRegistration() {
            // Instantiate the RequestQueue.
            RequestQueue queue = Volley.newRequestQueue(mContext, new HurlStack(null, getSelfSignedSSLSocketFactory()));
            String url ="https://" + mIpAddress + ":1331/v1.0/artikcloud/registration";

            JsonObjectRequest myReq = new JsonObjectRequest(Request.Method.GET, url, null,
                    new Response.Listener<JSONObject>()
                    {
                        @Override
                        public void onResponse(JSONObject response) {
                            Log.d("Response", response.toString());
                            try {
                                if (response.getBoolean("error")) {
                                    handleErrorMessage(ArtikGattServices.LONG_STATUS_START_REGISTRATION_ERROR);
                                    return;
                                } else {
                                    mChallengePin = response.getString("pin");
                                    confirmUserWithSAMI();
                                }
                            } catch (Exception e) {
                                handleErrorMessage(ArtikGattServices.LONG_STATUS_START_REGISTRATION_ERROR);
                            }

                        }
                    },
                    new Response.ErrorListener()
                    {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            if (error != null && error.getLocalizedMessage() != null) {
                                Log.d(TAG," Start SDR Error.Response" + error.getLocalizedMessage());
                            }
                            handleErrorMessage(ArtikGattServices.LONG_STATUS_START_REGISTRATION_ERROR);
                        }
                    }
            );


            myReq.setRetryPolicy(new DefaultRetryPolicy(
                    15000,
                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));


            queue.add(myReq);
            queue.start();
        }

        private void completeRegistration() {
            // Instantiate the RequestQueue.
            RequestQueue queue = Volley.newRequestQueue(mContext, new HurlStack(null, getSelfSignedSSLSocketFactory()));
            String url ="https://" + mIpAddress + ":1331/v1.0/artikcloud/registration";

            JsonObjectRequest myReq = new JsonObjectRequest(Request.Method.PUT, url, null,
                    new Response.Listener<JSONObject>()
                    {
                        @Override
                        public void onResponse(JSONObject response) {
                            Log.d("Response", response.toString());
                            try {
                                if (response.getBoolean("error")) {
                                    handleErrorMessage(ArtikGattServices.LONG_STATUS_COMPLETE_REGISTRATION_ERROR);
                                    return;
                                } else {
                                    LocalBroadcastManager.getInstance(mContext).sendBroadcast(new Intent(Constants.BOARD_CONNECTION_STATUS.REGISTRATION_COMPLETE.toString()));
                                }
                            } catch (Exception e) {
                                handleErrorMessage(ArtikGattServices.LONG_STATUS_COMPLETE_REGISTRATION_ERROR);
                            }

                        }
                    },
                    new Response.ErrorListener()
                    {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            if (error != null && error.getLocalizedMessage() != null) {
                                Log.d(TAG," Start SDR Error.Response" + error.getLocalizedMessage());
                            }
                            handleErrorMessage(ArtikGattServices.LONG_STATUS_COMPLETE_REGISTRATION_ERROR);
                        }
                    }
            );

            myReq.setRetryPolicy(new DefaultRetryPolicy(
                    15000,
                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

            queue.add(myReq);
            queue.start();
        }
    */
    private void confirmUserWithSAMI() {
        try {

            mAccessToken = "";//Hawk.get(Constants.ONBOARDING_ACCESS_TOKEN_PREF, null);

            if (mAccessToken.isEmpty() || mAccessToken.equals(null)) {
                Log.d(TAG, "mAccessToken is null");
            }
            new confirmUserTask(mContext, mAccessToken).execute().get();
        } catch (Exception e) {
            Log.i(TAG, e.getMessage());
        }
    }

    /**
     * Confirm the registration process with pin
     */
    private class confirmUserTask extends AsyncTask<String, Void, Boolean> {

        private final Context context;
        private String accessToken;

        public confirmUserTask(Context c, String accessToken) {
            Log.d(TAG, " confirmUserTask Access token: " + mAccessToken);
            this.context = c;
            this.accessToken = accessToken;
        }

        protected void onPreExecute() {

        }

        @Override
        protected Boolean doInBackground(String... params) {
            try {
                URL url = new URL(api_url);
                String payload = "{\"pin\":" + "\"" + mChallengePin + "\",\"deviceName\":" + "\"" + mModule.getName() + "\"}";
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                Log.d(TAG, "mAccessToken : " + mAccessToken + " Challengepin :" + mChallengePin);
                connection.setRequestMethod("PUT");
                connection.setRequestProperty("USER-AGENT", "Mozilla/5.0");
                connection.setRequestProperty("ACCEPT-LANGUAGE", "en-US,en;0.5");
                connection.setRequestProperty("Content-Type", " application/json");
                connection.setRequestProperty("Accept", " application/json");
                connection.setRequestProperty("Authorization", "  bearer " + mAccessToken);
                Log.d(TAG, "Put Request: " + connection.getURL().toString());
                connection.setDoOutput(true);

                DataOutputStream dStream = new DataOutputStream(connection.getOutputStream());
                dStream.writeBytes(payload);
                dStream.flush();
                dStream.close();
                int responseCode = connection.getResponseCode();

                if (responseCode == 200) {
                    return true;
                }
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            Log.d(TAG, "returning null");
            return false;
        }

        protected void onPostExecute(Boolean httpOk) {
            // use the result
            super.onPostExecute(httpOk);
            if (httpOk) {
                mBLEConnectionMgr.setRegistrationState(REGISTRATION_STATE.REGIS_STATE_USER_CONFIRMED);
                mBLEConnectionMgr.writeCharacteristic(ArtikGattServices.COMPLETE_REGISTRATION_UUID, "1");
            } else {
                Intent intent = new Intent(Constants.BOARD_CONNECTION_STATUS.REGISTRATION_FAILED.toString());
                intent.putExtra(Constants.ERROR_STATUS, mContext.getResources().getString(R.string.registration_failed));
                LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
            }
        }

    }


}


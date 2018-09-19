package io.artik.easysetup.view;

import android.Manifest;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import io.artik.easysetup.R;
import io.artik.easysetup.ble.ArtikGattServices;
import io.artik.easysetup.ble.ControlBluetoothConnection;
import io.artik.easysetup.controller.WifiListAdapter;
import io.artik.easysetup.util.CheckConnectionReceiver;
import io.artik.easysetup.util.ConnectionDetector;
import io.artik.easysetup.util.Constants;
import io.artik.easysetup.util.Constants.BLE_STATUS_MESSAGES;
import io.artik.easysetup.util.CustomAlertDialog;
import io.artik.easysetup.util.CustomProgressDialog;
import io.artik.easysetup.util.Module;
import io.artik.easysetup.util.Utility;

/**
 * Created by 20115642 on 14-12-2016.
 */
public class WiFiListActivity extends Activity implements View.OnClickListener, CheckConnectionReceiver.ICheckConnection {

    private static final String TAG = "WIFIListactivity";
    private Button btnEnterPassword, cancelPassword;
    private LinearLayout llcancel, llRefresh, llWiFiList, llNoWiFiNetwork;
    private ImageView btnBack, logout, imgShowPassword, imgSavePassword;
    private ListView wifiList;
    private EditText password;
    private WifiListAdapter wifiListAdapter;
    private TextView show, save;
    private IntentFilter mIntentFilter;
    private boolean showPasswordflag = false;
    private boolean savePasswordflag = false;
    private Dialog dialog;
    private String mModuleServiceID;
    private Module mModule;
    private List<Map<String, String>> mWifiNetworksList;
    private Dialog mProgressDialog;
    private CustomAlertDialog alertDialog;
    private static final int RC_HANDLE_COARSE_LOCATION_PERMISSION = 5;
    private ControlBluetoothConnection mBluetoothController;
    private TextView title;
    private CheckConnectionReceiver connectionEnableReceiver;
    private String ipAddress;
    private boolean isLoaderActivated = false;
    private boolean isDailogActive = false;
    SharedPreferences sharedpreferences;
    private String deviceTypeId;
    private JSONArray apList;
    private boolean isRegistrationInProgress = false;
    private boolean isRegistartionComplete = false;


    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i(TAG, "Received msg " + intent.getAction());
            if (intent.getAction() == Constants.DEVICE_TYPES_LOADED) {
                //checkDTID(context);
            } else if (intent.getAction() == Constants.DEVICE_CREATED_SUCCESS) {
                final String did = intent.getStringExtra("deviceId");
                final String token = intent.getStringExtra("deviceToken");
                mBluetoothController.startClassicRegistration(did, token);
            } else if (intent.getAction() == Constants.DEVICE_CREATED_FAILED) {
                hideProgressBar();
                isLoaderActivated = true;
                mBluetoothController.disconnectModule();
                /*Intent in = new Intent(WiFiListActivity.this, HomeScreenActivity.class);
                in.putExtra(Constants.IS_NEW_MODULE_ADDED, false);
                showErrorDailog(null, getResources().getString(R.string.registration_failed), true, in);*/
            }
        }
    };


    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            String status = intent.getStringExtra(Constants.ERROR_STATUS);
            Constants.BOARD_CONNECTION_STATUS msg = Constants.BOARD_CONNECTION_STATUS.valueOf(intent.getAction());
            switch (msg) {
                case FOUND_NON_SDR_DEVICE:
                    deviceTypeId = intent.getStringExtra("DTID");
                    hideProgressBar();
                    isLoaderActivated = true;
                    showProgressDialog(getResources().getString(R.string.succesfully_registered_text), getResources().getString(R.string.registering_module_text));
                    break;
                case WIFI_LIST_RECEIVED:
                    try {
                        apList = new JSONArray(intent.getStringExtra(Constants.WIFI_LIST));
                        displayWifiList();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    break;
                case WIFI_CONNECTED:
                    hideProgressBar();
                    isLoaderActivated = true;
                    showProgressDialog("", getResources().getString(R.string.connecting_message));
                    ipAddress = intent.getStringExtra("IPADDRESS");
                    final SharedPreferences.Editor meditor = sharedpreferences.edit();
                    meditor.putString(Constants.GATEWAY_IPADDRESS, ipAddress);
                    meditor.commit();
                    break;
                case WIFI_FAILED:
                    hideProgressBar();
                    showErrorDailog(getResources().getString(R.string.wifi_connection_failed), status, false, null);
                    break;
                case BLE_DISCONNECTED:
                    hideProgressBar();
                    Intent data = new Intent(WiFiListActivity.this, PlugInModuleActivity.class);
                    data.putExtra(Constants.DISCOVERED_SERVICE_ID, ArtikGattServices.SERVICE_UUID.toString());
                    Bundle bundle = new Bundle();
                    bundle.putParcelable(Constants.MODULE_INFO, mModule);
                    data.putExtra(Constants.MODULE_INFO_BUNDLE, bundle);
                    showErrorDailog(null, status, true, data);
                    break;
                case REGISTRATION_STARTED:
                    hideProgressBar();
                    isLoaderActivated = true;
                    showProgressDialog(getResources().getString(R.string.succesfully_registered_text), getResources().getString(R.string.registering_module_text));
                    break;
                case INTERNET_UNAVAILABLE:
                    hideProgressBar();
                    showErrorDailog(null, status, true, null);
                    break;
                /*case REGISTRATION_COMPLETE:
                    hideProgressBar();
                    mBluetoothController.disconnectModule();
                    final SharedPreferences.Editor editor = sharedpreferences.edit();
                    editor.putString(Constants.GATEWAY_IPADDRESS, ipAddress);
                    editor.commit();
                    ActivityManager activityManager = (ActivityManager) getApplicationContext()
                            .getSystemService(Activity.ACTIVITY_SERVICE);
                    String className = activityManager.getRunningTasks(1).get(0).topActivity
                            .getClassName();

                    if (className.equals("com.artik.onboarding.View.WiFiListActivity")) {
                        Intent activityIntent = new Intent(getApplicationContext(), RegistrationSuccessActivity.class);
                        startActivity(activityIntent);
                        finish();
                    } else {
                        isRegistartionComplete = true;
                    }
                    break;
                case REGISTRATION_FAILED:
                    hideProgressBar();
                    isLoaderActivated = true;
                    mBluetoothController.disconnectModule();
                    Intent in = new Intent(WiFiListActivity.this, HomeScreenActivity.class);
                    in.putExtra(Constants.IS_NEW_MODULE_ADDED, false);
                    showErrorDailog(null, status, true, in);
                    break;*/
                case DTID_READ:
                    deviceTypeId = intent.getStringExtra("DTID");
                    checkDTID(context);
                    break;
                default:
                    break;
            }
        }
    };

    public void checkDTID(Context context) {

        if (deviceTypeId == null) {
            return;
        }

        hideProgressBar();
        mBluetoothController.disconnectModule();
        final SharedPreferences.Editor editor = sharedpreferences.edit();
        editor.putString(Constants.GATEWAY_IPADDRESS, ipAddress);
        editor.commit();
        ActivityManager activityManager = (ActivityManager) getApplicationContext()
                .getSystemService(Activity.ACTIVITY_SERVICE);
        String className = activityManager.getRunningTasks(1).get(0).topActivity
                .getClassName();

        if (className.equals("io.artik.easysetup.view.WiFiListActivity")) {
            Intent activityIntent = new Intent(getApplicationContext(), RegistrationSuccessActivity.class);
            startActivity(activityIntent);
            finish();
        } else {
            isRegistartionComplete = true;
        }

        /*DeviceType deviceType = databaseAccessor.getDeviceType(deviceTypeId);
        if (deviceType == null) {
            Log.i(TAG, "Fetching Device Type Information for " + deviceTypeId);
            fetchDevicesHandling.loadDeviceType(deviceTypeId, true);
        } else {

            if (isRegistrationInProgress)
                return;
            else
                isRegistrationInProgress = true;

            if (deviceType.isSDR()) {
                // Proceed with SDR Flow
                Log.i(TAG, "Proceeding with Secure Device Registration for " + deviceTypeId);
                mBluetoothController.startSecureRegistration();
                hideProgressBar();
                isLoaderActivated = true;
                showProgressDialog(getResources().getString(R.string.succesfully_registered_text), getResources().getString(R.string.registering_module_text));
            } else {
                Log.i(TAG, "Proceeding with Classic Registration for " + deviceTypeId);
                fetchDevicesHandling.addDevice(mModule.getName(), deviceTypeId);
                hideProgressBar();
                isLoaderActivated = true;
                showProgressDialog(getResources().getString(R.string.succesfully_registered_text), getResources().getString(R.string.registering_module_text));
            }
        }*/
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.wifi_list);
        llWiFiList = (LinearLayout) findViewById(R.id.wifi_list_layout);
        llNoWiFiNetwork = (LinearLayout) findViewById(R.id.no_wifi_layout);

        mWifiNetworksList = new ArrayList<>();
        mBluetoothController = ControlBluetoothConnection.getInstance(this);
        sharedpreferences = getSharedPreferences(Constants.ONBOARDING_PREFS, Context.MODE_PRIVATE);

        mIntentFilter = new IntentFilter();
        mIntentFilter.addAction(Constants.WIFI_STATE_CHANGED);
        mIntentFilter.addAction(Constants.CONNECTIVITY_CHANGE);
        mIntentFilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        mIntentFilter.addAction(Constants.STATE_CHANGE);
        mIntentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        mIntentFilter.addAction(Constants.EDGE_NODE_REGISTRATION_STATUS.REGISTRATION_COMPLETE.name());
        mIntentFilter.addAction(Constants.EDGE_NODE_REGISTRATION_STATUS.REGISTRATION_FAILED.name());


        Utility.setConnectivityListener(this);
        connectionEnableReceiver = new CheckConnectionReceiver();
        registerReceiver(connectionEnableReceiver, mIntentFilter);
        if (getIntent() != null) {
            mModuleServiceID = getIntent().getStringExtra(Constants.DISCOVERED_SERVICE_ID);
        }
        if (mModuleServiceID != null) {
            mModule = getModuleInfo(mModuleServiceID);
        } else {
            Log.d(TAG, "Unable to Identify module");
        }
        initialiseUI();

        if (!ConnectionDetector.isConnectingToInternet(this)) {
            isLoaderActivated = true;
            final CustomAlertDialog alertDialog = new CustomAlertDialog(this, null, getResources().getString(R.string.internet_failure_message), getResources().getString(R.string.wifi_network_message));
            alertDialog.findViewById(R.id.ok).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    alertDialog.cancel();
                    getWifiList();
                    isLoaderActivated = false;

                }
            });
            alertDialog.setCancelable(false);
            alertDialog.show();


        } else {
            getWifiList();
        }
    }


    /**
     * Handled Dashboard activity launch.
     **/
    /*private void launchHomeScreenActivity(boolean isRegistrationSuccessful) {
        Intent in = new Intent(WiFiListActivity.this, HomeScreenActivity.class);
        in.putExtra(Constants.IS_NEW_MODULE_ADDED, isRegistrationSuccessful);
        startActivity(in);
        finish();
        return;
    }*/

    private void getWifiList() {
        if (mBluetoothController.getHandler() != null) {
            mBluetoothController.getHandler().sendMessage(Message.obtain(ControlBluetoothConnection.getHandler(), BLE_STATUS_MESSAGES.GET_WIFI_LIST.ordinal(), null));
            showProgressDialog("", getResources().getString(R.string.fetching_wifi_network));
        }
    }

    private void displayWifiList(){

        if (mWifiNetworksList != null)
            mWifiNetworksList.clear();

        hideProgressBar();

        if (apList.length() == 0) {
            llWiFiList.setVisibility(View.GONE);
            llNoWiFiNetwork.setVisibility(View.VISIBLE);
            return;
        } else {
            llWiFiList.setVisibility(View.VISIBLE);
            llNoWiFiNetwork.setVisibility(View.GONE);
        }

        for (int i = 0; i < apList.length(); i++) {
            try {
                JSONObject ap = apList.getJSONObject(i);
                mWifiNetworksList.add(putData(ap.getString("ssid"), ap.getString("security")));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        wifiListAdapter.notifyDataSetChanged();
    }


    @Override
    protected void onResume() {
        super.onResume();


        //*******************************//
        /*if (isRegistartionComplete) {
            Intent activityIntent = new Intent(getApplicationContext(), RegistrationSuccessActivity.class);
            startActivity(activityIntent);
            finish();
        }*/
        //************************************//
    }


    @Override
    protected void onPause() {
        super.onPause();
    }

    /**
     * Request Bluetooth Permission.
     **/
    private void requestBluetoothPermission() {
        Log.w(TAG, "Camera permission is not granted. Requesting permission");

        final String[] permissions = new String[]{Manifest.permission.ACCESS_COARSE_LOCATION};

        if (ContextCompat.checkSelfPermission(WiFiListActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_COARSE_LOCATION)) {
                ActivityCompat.requestPermissions(WiFiListActivity.this, permissions, RC_HANDLE_COARSE_LOCATION_PERMISSION);
                return;
            } else {
                final CustomAlertDialog alertDialog = new CustomAlertDialog(this, null, getResources().getString(R.string.wifi_permission_failure), "");
                alertDialog.setCancelable(true);
                alertDialog.setCanceledOnTouchOutside(false);
                ((Button) alertDialog.findViewById(R.id.ok)).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        alertDialog.cancel();
                        finish();
                    }
                });
                alertDialog.show();
            }
        }

        final Activity thisActivity = this;

        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ActivityCompat.requestPermissions(thisActivity, permissions,
                        RC_HANDLE_COARSE_LOCATION_PERMISSION);
            }
        };

    }

    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case RC_HANDLE_COARSE_LOCATION_PERMISSION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    getWifiList();

                } else {
                    final CustomAlertDialog alertDialog = new CustomAlertDialog(WiFiListActivity.this, null, getResources().getString(R.string.wifi_permission_failure), "");
                    alertDialog.setCancelable(true);
                    alertDialog.setCanceledOnTouchOutside(false);
                    alertDialog.show();

                    alertDialog.findViewById(R.id.ok).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            alertDialog.cancel();
                            finish();
                        }
                    });
                }
                return;
            }
        }
    }

    /**
     * Module Information.
     **/
    private Module getModuleInfo(String mModuleServiceID) {
        Bundle bundle = getIntent().getBundleExtra(Constants.MODULE_INFO_BUNDLE);
        Module module = bundle.getParcelable(Constants.MODULE_INFO);
        if (module != null) {
            module.setUuid(UUID.fromString(mModuleServiceID));
        }
        return module;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            unregisterReceiver(connectionEnableReceiver);

        } catch (IllegalArgumentException e) {
            Log.e(TAG, "receiver not registered");
        }

        try {
            unregisterReceiver(mMessageReceiver);

        } catch (IllegalArgumentException e) {
            Log.e(TAG, "receiver not registered");
        }

        try {
            unregisterReceiver(broadcastReceiver);

        } catch (IllegalArgumentException e) {
            Log.e(TAG, "receiver not registered");
        }

        mMessageReceiver = null;
        hideProgressBar();
    }


    /**
     * Error Dialog.
     **/
    private void showErrorDailog(String message1, String message2, final boolean finishOnClick, final Intent launchIntent) {

        if (!isFinishing()) {
            if ((message1 == null) || (message1.isEmpty()))
                alertDialog = new CustomAlertDialog(WiFiListActivity.this, null, message2, "");
            else
                alertDialog = new CustomAlertDialog(WiFiListActivity.this, null, message1, message2);
            alertDialog.setCancelable(true);
            alertDialog.setCanceledOnTouchOutside(false);
            alertDialog.findViewById(R.id.ok).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    isLoaderActivated = false;
                    alertDialog.dismiss();

                    if (finishOnClick) {
                        if (launchIntent != null) {
                            isLoaderActivated = false;
                            startActivity(launchIntent);
                        }
                        finish();
                    }
                }
            });
            alertDialog.show();
        }
    }

    private void initialiseUI() {

        title = (TextView) findViewById(R.id.title);
        title.setText(R.string.avilable_wifi);
        dialog = new Dialog(this);
        wifiList = (ListView) findViewById(R.id.wifi_list);
        llRefresh = (LinearLayout) findViewById(R.id.llRefresh);
        wifiListAdapter = new WifiListAdapter(this, 0, mWifiNetworksList);

        wifiList.setAdapter(wifiListAdapter);
        wifiList.addFooterView(new View(this));
        btnBack = (ImageView) findViewById(R.id.back);
        logout = (ImageView) findViewById(R.id.logout);
        llcancel = (LinearLayout) findViewById(R.id.llcancellbackground);

        btnBack.setVisibility(View.GONE);
        logout.setVisibility(View.GONE);
        btnBack.setOnClickListener(this);
        llcancel.setOnClickListener(this);
        llRefresh.setOnClickListener(this);

        llcancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (ControlBluetoothConnection.getHandler() != null) {
                    mBluetoothController.disconnectModule();
                    finish();
                }

            }
        });


        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Constants.BOARD_CONNECTION_STATUS.WIFI_CONNECTED.name());
        intentFilter.addAction(Constants.BOARD_CONNECTION_STATUS.REGISTRATION_STARTED.name());
        intentFilter.addAction(Constants.BOARD_CONNECTION_STATUS.REGISTRATION_COMPLETE.name());
        intentFilter.addAction(Constants.BOARD_CONNECTION_STATUS.WIFI_FAILED.name());
        intentFilter.addAction(Constants.BOARD_CONNECTION_STATUS.REGISTRATION_FAILED.name());
        intentFilter.addAction(Constants.BOARD_CONNECTION_STATUS.INTERNET_UNAVAILABLE.name());
        intentFilter.addAction(Constants.BOARD_CONNECTION_STATUS.BLE_DISCONNECTED.name());
        intentFilter.addAction(Constants.BOARD_CONNECTION_STATUS.FOUND_NON_SDR_DEVICE.name());
        intentFilter.addAction(Constants.BOARD_CONNECTION_STATUS.DTID_READ.name());
        intentFilter.addAction(Constants.BOARD_CONNECTION_STATUS.WIFI_LIST_RECEIVED.name());
        mIntentFilter.addAction(Constants.EDGE_NODE_REGISTRATION_STATUS.REGISTRATION_COMPLETE.name());
        mIntentFilter.addAction(Constants.EDGE_NODE_REGISTRATION_STATUS.REGISTRATION_FAILED.name());

        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver,
                intentFilter);


        IntentFilter mFilter = new IntentFilter();
        mFilter.addAction(Constants.DEVICE_TYPES_LOADED);
        mFilter.addAction(Constants.DEVICE_CREATED_SUCCESS);
        mFilter.addAction(Constants.DEVICE_CREATED_FAILED);
        registerReceiver(broadcastReceiver, mFilter);


        wifiList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String encryption = mWifiNetworksList.get(position).get("encryption");

                if (encryption.equals("Open")) {
                    sendWiFiDataForConnection(position, "Open");
                } else {
                    createDailog(position);
                }
            }
        });

    }

    /**
     * Check Wi-Fi Connection for Encryption.
     **/
    private void sendWiFiDataForConnection(int selectedPosition, String password) {
        String SSID = wifiListAdapter.getItem(selectedPosition).get("ssid");
        String Encryption = wifiListAdapter.getItem(selectedPosition).get("encryption");

        Map<String, String> wifi_data = new HashMap<>();
        wifi_data.put("ssid", SSID);
        // wifi_data.put("encryption", Encryption);
        if (!Encryption.equals("Open"))
            wifi_data.put("encryption", "SECURE");
        else
            wifi_data.put("encryption", "OPEN");
        wifi_data.put("password", password);

        if (mBluetoothController.getHandler() != null) {
            mBluetoothController.getHandler().sendMessage(Message.obtain(ControlBluetoothConnection.getHandler(), BLE_STATUS_MESSAGES.SEND_WIFI_CRED.ordinal(), wifi_data));
            showProgressDialog("", getResources().getString(R.string.connecting_message));
        }
    }


    /**
     * Map List for data handling.
     **/
    private Map<String, String> putData(String ssid, String encryption) {
        Map<String, String> item = new HashMap<>();
        item.put("ssid", ssid);
        item.put("encryption", encryption);
        return item;
    }


    /**
     * Custom Dialog for Wi-Fi Password.
     **/
    private void createDailog(final int selectedPosition) {

        LayoutInflater inflater = getLayoutInflater();
        View alertLayout = inflater.inflate(R.layout.wifi_password_dialog, null);
        isDailogActive = true;
        dialog.setContentView(alertLayout);
        dialog.setCancelable(false);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        cancelPassword = (Button) alertLayout.findViewById(R.id.cancel);
        btnEnterPassword = (Button) alertLayout.findViewById(R.id.btn_enter_password);
        imgShowPassword = (ImageView) alertLayout.findViewById(R.id.showimg);
        imgSavePassword = (ImageView) alertLayout.findViewById(R.id.saveimg);
        password = (EditText) alertLayout.findViewById(R.id.password);
        show = (TextView) alertLayout.findViewById(R.id.show);
        save = (TextView) alertLayout.findViewById(R.id.save);
        TextView wifiTitleTxt = (TextView) alertLayout.findViewById(R.id.wifi_titletxt);
        final String ssid = wifiListAdapter.getItem(selectedPosition).get("ssid");
        wifiTitleTxt.setText(ssid);
        cancelPassword.setOnClickListener(this);
        btnEnterPassword.setOnClickListener(this);
        imgShowPassword.setOnClickListener(this);
        show.setOnClickListener(this);
        save.setOnClickListener(this);
        imgSavePassword.setOnClickListener(this);

        btnEnterPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (Utility.isBluetoothEnabled()) {
                    if (password.getText().toString() == null || password.getText().toString().equals("")) {
                        Toast.makeText(WiFiListActivity.this, getResources().getString(R.string.empty_password), Toast.LENGTH_SHORT).show();
                    } else {

                        isDailogActive = false;
                        isLoaderActivated = true;
                        sendWiFiDataForConnection(selectedPosition, password.getText().toString());
                        dialog.cancel();
                    }
                } else {
                    String status = getResources().getString(R.string.bluetooth_available);
                    showErrorDailog(null, status, false, null);
                }
            }
        });
        dialog.show();
    }

    @Override
    public void onBackPressed() {
    }

    @Override
    public void onClick(View view) {

        switch (view.getId()) {
            case R.id.cancel:
                if (dialog != null)
                    isDailogActive = false;
                dialog.dismiss();
                break;
            case R.id.show:
            case R.id.showimg:
                if (!showPasswordflag) {
                    password.setTransformationMethod(new HideReturnsTransformationMethod());
                    imgShowPassword.setImageResource(R.mipmap.hidepwd);

                    show.setText(R.string.hide_pwd);
                    showPasswordflag = true;
                } else {
                    password.setTransformationMethod(new PasswordTransformationMethod());
                    imgShowPassword.setImageResource(R.mipmap.showpwd);

                    show.setText(R.string.show_password);
                    showPasswordflag = false;
                }

                password.setSelection(password.getText().length());

                break;
            case R.id.save:
            case R.id.saveimg:
                if (!savePasswordflag) {
                    imgSavePassword.setImageResource(R.mipmap.hidepwd);
                    save.setText(R.string.no_save_password);
                    savePasswordflag = true;
                } else {
                    imgSavePassword.setImageResource(R.mipmap.showpwd);
                    save.setText(R.string.save_password);
                    savePasswordflag = false;
                }
                break;
            case R.id.llcancellbackground:

                //launchHomeScreenActivity(false);

                break;

            case R.id.llRefresh:
                getWifiList();
                break;
        }
    }

    /**
     * Hide Progress Dialog.
     **/
    private void hideProgressBar() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
            mProgressDialog = null;
        }
    }


    /**
     * Show Progress Dialog.
     **/
    private void showProgressDialog(String title, String message) {
        hideProgressBar();
        if (!isFinishing()) {
            mProgressDialog = new CustomProgressDialog(WiFiListActivity.this, title, message);
            int width = (int) (getResources().getDisplayMetrics().widthPixels * 0.85);
            int height = (int) (getResources().getDisplayMetrics().heightPixels * 0.30);
            mProgressDialog.getWindow().setLayout(width, height);
            mProgressDialog.setTitle(title);
            mProgressDialog.setCancelable(false);
            mProgressDialog.show();
        }
    }

    @Override
    public void showAlert(Boolean isConnected) {
        if (!isFinishing() && !isDailogActive) {
            final CustomAlertDialog alertDialog = new CustomAlertDialog(WiFiListActivity.this, null, getResources().getString(R.string.internet_failure_message), getResources().getString(R.string.wifi_network_message));
            alertDialog.findViewById(R.id.ok).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    isDailogActive = false;
                    alertDialog.dismiss();
                }
            });

            if (!isConnected && !isLoaderActivated) {
                isDailogActive = true;
                alertDialog.show();
            }
        } else {
            if (alertDialog != null) {
                alertDialog.dismiss();
            }
        }
    }


    /**
     * Handle Plugin Activity launch.
     **/
    private void launchPlugInActivity() {
        Intent in = new Intent(this, PlugInModuleActivity.class);
        in.putExtra(Constants.DISCOVERED_SERVICE_ID, ArtikGattServices.SERVICE_UUID.toString());
        Bundle bundle = new Bundle();
        bundle.putParcelable(Constants.MODULE_INFO, mModule);
        in.putExtra(Constants.MODULE_INFO_BUNDLE, bundle);
        startActivity(in);
        finish();
    }


    @Override
    public void showBluetoothAlert(Boolean isConnected) {
        if (!isConnected && !isDailogActive) {
            hideProgressBar();
            isDailogActive = true;
            final CustomAlertDialog alertDialog = new CustomAlertDialog(this, null, getResources().getString(R.string.bluetooth_failed_message_wifiscreen), "");
            alertDialog.findViewById(R.id.ok).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    alertDialog.cancel();
                    isDailogActive = false;
                    mBluetoothController.disconnectModule();
                    launchPlugInActivity();
                }
            });
            alertDialog.show();
        }
    }
}



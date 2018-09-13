package io.artik.easysetup.view;

import android.app.Activity;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.PowerManager;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import io.artik.easysetup.wifi.SoftAPOnboardingManager;
import io.artik.easysetup.api.client.SoftAPCallHandling;
import io.artik.easysetup.api.model.AccessPointInfo;
import io.artik.easysetup.controller.SoftAPWifiListAdapter;
import io.artik.easysetup.R;
import io.artik.easysetup.util.Constants;
import io.artik.easysetup.util.CustomAlertDialog;
import io.artik.easysetup.util.CustomProgressDialog;
import io.artik.easysetup.util.Module;

import java.util.ArrayList;

import io.artik.easysetup.util.Constants;
import io.artik.easysetup.util.CustomAlertDialog;
import io.artik.easysetup.util.CustomProgressDialog;
import io.artik.easysetup.util.Module;
import io.artik.easysetup.wifi.SoftAPOnboardingManager;

public class SoftAPOnboarding extends Activity implements View.OnClickListener {

    private static final String TAG = "SoftAPActivity";

    private Dialog mProgressDialog;
    private CustomAlertDialog mAlertDialog;
    private Module mModule;
    private SoftAPOnboardingManager softAPOnboardingManager;
    private IntentFilter mIntentFilter;
    private SoftAPWifiListAdapter softAPWifiListAdapter;
    private ArrayList<AccessPointInfo> wifiList = new ArrayList<>();

    private boolean isDailogActive = false;
    private Dialog dialog;
    private Button btnEnterPassword, cancelPassword;
    private LinearLayout llcancel;
    private EditText password;
    private boolean showPasswordflag = false;
    private boolean savePasswordflag = false;

    private ImageView btnBack, imgShowPassword, logout, imgSavePassword;
    private LinearLayout llRefresh;
    private TextView show, save;
    private String deviceTypeId;
    PowerManager.WakeLock wakeLock;

    private final static int MAXIMUM_RETRIES = 60;
    private final static int MAX_RETRY_FOR_FETCH = 3;

    public enum STATES {
        SEARCHING_MODULE,
        REFRESH_WIFILIST,
        SEARCHING_DEVICE_ON_LOCAL_WIFI,
    };

    private STATES state = STATES.SEARCHING_MODULE;
    private int retry = MAXIMUM_RETRIES;
    private int fetch_retry = MAX_RETRY_FOR_FETCH;

    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

/*            if (intent.getAction() == Constants.DEVICE_CREATED_SUCCESS) {
                final String did = intent.getStringExtra("deviceId");
                final String token = intent.getStringExtra("deviceToken");
                new android.os.Handler().postDelayed(
                        new Runnable() {
                            public void run() {
                                softAPOnboardingManager.provisionDevice(did, token);
                                showProgressDialog(getString(R.string.cloud_onboarding), getResources().getString(R.string.pass_cloud_credentials));
                            }
                        }, 1000);
            }

            if (intent.getAction() == Constants.DEVICE_CREATED_FAILED) {
                showAlertDialog(getString(R.string.cloud_onboarding),getResources().getString(R.string.registration_failed) );
            }

            if (intent.getAction() == Constants.SDR_USER_CONFIRM_SUCCESS) {
                new android.os.Handler().postDelayed(
                        new Runnable() {
                            public void run() {
                                softAPOnboardingManager.completeSDR();
                                showProgressDialog(getString(R.string.cloud_onboarding), getResources().getString(R.string.pin_confirmed));
                            }
                        }, 1000);
            }

            if (intent.getAction() == Constants.DEVICE_TYPES_LOADED) {
                checkDTID();
            }*/
        }
    };

    private SoftAPCallHandling.SoftAPListener mListener = new SoftAPCallHandling.SoftAPListener() {

        @Override
        public void onAPListResponse(ArrayList<AccessPointInfo> aps) {
            hideProgressBar();

            if (state != STATES.REFRESH_WIFILIST) {
                return;
            }
            if (aps == null) {

                if (fetch_retry <= 0) {
                    fetch_retry = MAX_RETRY_FOR_FETCH;
                    showAlertDialog(getString(R.string.wifi_onboarding), getResources().getString(R.string.unable_to_fetch_ap_list));
                } else {
                    new android.os.Handler().postDelayed(
                            new Runnable() {
                                public void run() {
                                    refreshWifiList();
                                    fetch_retry--;
                                }
                            }, 5000);
                }
            } else {
                wifiList = aps;
                softAPWifiListAdapter.setWifiList(aps);
            }
        }

        @Override
        public void onAPConfigured(boolean success) {

            hideProgressBar();
            /* Clear List */
            softAPWifiListAdapter.setWifiList(new  ArrayList<AccessPointInfo>());
            if (success) {
                new android.os.Handler().postDelayed(
                        new Runnable() {
                            public void run() {
                                softAPOnboardingManager.unbindNetwork();
                                softAPOnboardingManager.connectToAP();
                                searchDeviceOnLocalWifi();
                            }
                        }, 1000);

                new android.os.Handler().postDelayed(
                        new Runnable() {
                            public void run() {
                                searchDeviceOnLocalWifi();
                            }
                        }, 5000);


            } else {
                showAlertDialog(getString(R.string.wifi_onboarding), getResources().getString(R.string.wifi_connection_failed));
            }
        }

        @Override
        public void onAPProvisionDevice(boolean success) {
            if (success) {
                hideProgressBar();
                /*Intent activityIntent = new Intent(getApplicationContext(), RegisterationSuccessActivity.class);
                startActivity(activityIntent);*/
                finish();
            }
        }

        @Override
        public void onNodeDiscovered(String ip) {
            final String Ip = ip;
//            showProgressDialog(getString(R.string.cloud_onboarding), getString(R.string.reading_device_type));
//
//            if (state == STATES.ONBOARDING_CLOUD) {
//                return;
//            }
//
//            state = STATES.ONBOARDING_CLOUD;

            new android.os.Handler().postDelayed(
                    new Runnable() {
                        public void run() {
                            softAPOnboardingManager.setIPAddress(Ip);
                        }
                    }, 5000);

        }

        @Override
        public void onAPError(String reason, int error_code) {
            hideProgressBar();

            String message = getString(R.string.error_code) +  " : " + error_code + "\n" + reason;
//            showAlertDialog(getString(R.string.cloud_onboarding), message);
        }

    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_soft_aponboarding);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        btnBack = (ImageView) findViewById(R.id.back);
        logout = (ImageView) findViewById(R.id.logout);
        llcancel = (LinearLayout) findViewById(R.id.llcancellbackground);
        llcancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        btnBack.setVisibility(View.GONE);
        logout.setVisibility(View.GONE);
        llRefresh = (LinearLayout) findViewById(R.id.llRefresh);
        dialog = new Dialog(this);

        mIntentFilter = new IntentFilter();
        mIntentFilter.addAction(Constants.DEVICE_CREATED_SUCCESS);
        mIntentFilter.addAction(Constants.DEVICE_CREATED_FAILED);

        ListView wifiListView = (ListView) findViewById(R.id.wifi_list);
        softAPWifiListAdapter = new SoftAPWifiListAdapter(this, 0, wifiList);
        wifiListView.setAdapter(softAPWifiListAdapter);

        mModule = getModuleInfo();
        softAPOnboardingManager = new SoftAPOnboardingManager(this, mModule, mListener);

        wifiListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                if (wifiList.get(position).isSecure())
                    createDialog(position);
                else
                    softAPOnboardingManager.passNetworkConfig(wifiList.get(position).getSsid(), null);
            }
        });

        llRefresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                switch (state) {
                    case SEARCHING_MODULE:
                        retry = MAXIMUM_RETRIES;
                        searchModule(true);
                        break;
                    case REFRESH_WIFILIST:
                        refreshWifiList();
                        break;
                    case SEARCHING_DEVICE_ON_LOCAL_WIFI:
                        searchDeviceOnLocalWifi();
                        break;
                    default:
                }

            }
        });

        PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                "ArtikOnboarding");

    }



    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(broadcastReceiver, mIntentFilter);
        retry = MAXIMUM_RETRIES;
        searchModule(true);
        wakeLock.acquire();
    }


    private void searchModule(boolean reconnect) {

        state = STATES.SEARCHING_MODULE;

        if (reconnect)
            softAPOnboardingManager.connectToModuleAP();
        if (retry == MAXIMUM_RETRIES )
            showProgressDialog(getString(R.string.wifi_onboarding), getString(R.string.searching_module));
        retry--;
        new android.os.Handler().postDelayed(
                new Runnable() {
                    public void run() {
                        if (softAPOnboardingManager.isConnectedToSoftAP()) {

                            new android.os.Handler().postDelayed(
                                    new Runnable() {
                                        public void run() {
                                            refreshWifiList();
                                        }
                                    }, 5000);
                        } else {
                            if (retry == 0) {
                                hideProgressBar();
                                showAlertDialog(getString(R.string.wifi_onboarding), getString(R.string.unable_to_find_module));
                            } else {
                                searchModule(false);
                            }
                        }
                    }
                }, 1000);
    }

    private void refreshWifiList() {
        state = STATES.REFRESH_WIFILIST;
        showProgressDialog(getString(R.string.wifi_onboarding), getString(R.string.fetching_ap_list));
        softAPOnboardingManager.fetchWifiAPList();
    }

    private void searchDeviceOnLocalWifi() {
        state = STATES.SEARCHING_DEVICE_ON_LOCAL_WIFI;
        showProgressDialog(getString(R.string.wifi_onboarding), getString(R.string.searching_device_local_wifi));
        softAPOnboardingManager.discoverNode();
        new android.os.Handler().postDelayed(
                new Runnable() {
                    public void run() {
                        if (state == STATES.SEARCHING_DEVICE_ON_LOCAL_WIFI) {
                            hideProgressBar();
                            CustomAlertDialog customAlertDialog = new CustomAlertDialog(SoftAPOnboarding.this, getString(R.string.wifi_onboarding), getString(R.string.module_not_found), null);
                            customAlertDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                                @Override
                                public void onCancel(DialogInterface dialog) {
                                    state = STATES.SEARCHING_MODULE;
                                    retry = MAXIMUM_RETRIES;
                                    searchModule(true);
                                }
                            });
                            if (!isFinishing()) {
                                customAlertDialog.show();
                            }
                        }
                    }
                }, 60000);
    }

    @Override
    protected void onPause() {
        super.onPause();
        try {
            softAPOnboardingManager.unbindNetwork();
            wakeLock.release();
            unregisterReceiver(broadcastReceiver);
            hideProgressBar();
            hideAlertDialog();
        } catch (Exception e) {
            e.printStackTrace();
        }
        finish();
    }

    /**
     * Module Information.
     **/
    private Module getModuleInfo() {
        Bundle bundle = getIntent().getBundleExtra(Constants.MODULE_INFO_BUNDLE);
        Module module = bundle.getParcelable(Constants.MODULE_INFO);
        return module;
    }

    /**
     * Show Progress Dialog.
     **/
    private void showProgressDialog(String title, String message) {
        hideProgressBar();
        if (!isFinishing()) {
            mProgressDialog = new CustomProgressDialog(SoftAPOnboarding.this, title, message);
            int width = (int) (getResources().getDisplayMetrics().widthPixels * 0.85);
            int height = (int) (getResources().getDisplayMetrics().heightPixels * 0.30);
            mProgressDialog.getWindow().setLayout(width, height);
            mProgressDialog.setTitle(title);
            mProgressDialog.setCancelable(false);
            mProgressDialog.show();
        }
    }

    /**
     * Show Alert Dialog.
     **/
    private void showAlertDialog(String title, String message) {
        hideAlertDialog();
        if (!isFinishing()) {
            mAlertDialog = new CustomAlertDialog(SoftAPOnboarding.this, title, message, null);
            mAlertDialog.show();
        }
    }

    /**
     * Hide Alert Dialog.
     **/
    private void hideAlertDialog() {
        if (mAlertDialog != null && mAlertDialog.isShowing()) {
            mAlertDialog.dismiss();
            mAlertDialog = null;
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
     * Custom Dialog for Wi-Fi Password.
     **/
    private void createDialog(final int selectedPosition) {

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

        final String ssid = wifiList.get(selectedPosition).getSsid();
        wifiTitleTxt.setText(ssid);
        cancelPassword.setOnClickListener(this);
        btnEnterPassword.setOnClickListener(this);
        imgShowPassword.setOnClickListener(this);
        imgSavePassword.setOnClickListener(this);
        show.setOnClickListener(this);
        save.setOnClickListener(this);

        btnEnterPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (password.getText().toString() == null || password.getText().toString().equals("")) {
                    Toast.makeText(SoftAPOnboarding.this, getResources().getString(R.string.empty_password), Toast.LENGTH_SHORT).show();
                } else {
                    isDailogActive = false;
                    dialog.cancel();

                    showProgressDialog(getString(R.string.wifi_onboarding), getString(R.string.configuring_wifi));
                    softAPOnboardingManager.passNetworkConfig(ssid, password.getText().toString());
                }

            }
        });
        dialog.show();

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
                break;

            case R.id.llRefresh:
                break;
        }
    }
}

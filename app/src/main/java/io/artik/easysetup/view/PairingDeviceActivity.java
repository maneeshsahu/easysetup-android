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
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import io.artik.easysetup.R;
import io.artik.easysetup.ble.ArtikGattServices;
import io.artik.easysetup.ble.ControlBluetoothConnection;
import io.artik.easysetup.util.CheckConnectionReceiver;
import io.artik.easysetup.util.Constants;
import io.artik.easysetup.util.CustomAlertDialog;
import io.artik.easysetup.util.CustomMessageDialog;
import io.artik.easysetup.util.Module;
import io.artik.easysetup.util.Utility;

/**
 * Created by 20102455 on 02-12-2016.
 */
public class PairingDeviceActivity extends Activity implements CheckConnectionReceiver.ICheckConnection {

    private static final String TAG = "bluetoothPairing";
    private Utility utility;
    private ImageView btnBack, logout, ble;
    private String mModuleServiceID;
    private Module mModule;
    private Activity activity;
    private TextView title;
    private ControlBluetoothConnection controlBluetoothConnection;
    private static final int RC_HANDLE_COARSE_LOCATION_PERMISSION = 2;
    private IntentFilter intentFilter;
    private CheckConnectionReceiver connectionEnableReceiver;
    private Dialog dialog;
    //*******************//
    private boolean isBLEConnected = false;

    //********************//
    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            if (!isFinishing()) {
                if (intent.getAction().equals(Constants.BLE_SCAN_TIMEOUT) || intent.getAction().equals(Constants.BOARD_CONNECTION_STATUS.BLE_DISCONNECTED.toString())) {
                    //***************************//
                    isBLEConnected = false;
                    //**************************//
                    final CustomAlertDialog alertDialog = new CustomAlertDialog(activity, null, getResources().getString(R.string.bluetooth_search_failed), "");
                    alertDialog.setCancelable(true);
                    alertDialog.setCanceledOnTouchOutside(false);
                    ((Button) alertDialog.findViewById(R.id.ok)).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            alertDialog.cancel();
                            launchPlugInActivity();
//                            finish();
                        }
                    });
                    alertDialog.show();
                } else if (intent.getAction().equals(Constants.BOARD_CONNECTION_STATUS.BLE_CONNECTED.toString())) {

                    //***************//
                    ActivityManager activityManager = (ActivityManager) getApplicationContext()
                            .getSystemService(Activity.ACTIVITY_SERVICE);
                    String className = activityManager.getRunningTasks(1).get(0).topActivity
                            .getClassName();

                    if (className.equals("com.artik.onboarding.View.PairingDeviceActivity")) {
                        Intent in = new Intent(activity, DevicePairedActivity.class);
                        in.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                        Bundle bundle = new Bundle();
                        bundle.putParcelable(Constants.MODULE_INFO, mModule);
                        in.putExtra(Constants.MODULE_INFO_BUNDLE, bundle);
                        startActivity(in);
                        finish();
                    }
                    else
                    {
                        isBLEConnected =true;
                    }
                    //*********************//

                }
            }
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_pairing_devices);

        initiailiseUI();
        activity = this;
        utility = new Utility(this);
        intentFilter = new IntentFilter();
        intentFilter.addAction(Constants.BLE_SCAN_TIMEOUT);
        intentFilter.addAction(Constants.BOARD_CONNECTION_STATUS.BLE_CONNECTED.toString());
        intentFilter.addAction(Constants.BOARD_CONNECTION_STATUS.BLE_DISCONNECTED.toString());
        intentFilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);

        Utility.setConnectivityListener(this);
        connectionEnableReceiver = new CheckConnectionReceiver();

        registerReceiver(broadcastReceiver, intentFilter);
        registerReceiver(connectionEnableReceiver, intentFilter);

        if (getIntent() != null) {
            mModuleServiceID = getIntent().getStringExtra(Constants.DISCOVERED_SERVICE_ID);
        }
        if (mModuleServiceID != null) {
            mModule = getModuleInfo(mModuleServiceID);
        } else {
            Log.d(TAG, String.valueOf(R.string.unabletoidentify));
        }
        if (!Utility.isBluetoothEnabled()) {
            enableBluetoothDialog();
        }
        else {
            startAnimation();

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestBluetoothPermission();
            } else {
                connectToBluetooth();
            }
        }
    }

    /**
     *
     * Custom Dialog for Bluetooth Permission
     *
     **/
    private void enableBluetoothDialog() {

        dialog = new CustomMessageDialog(this, getResources().getString(R.string.confirmation), getResources().getString(R.string.bluetooth_enable_message));
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        final Button confirm = (Button) dialog.findViewById(R.id.confirm);
        Button cancel = (Button) dialog.findViewById(R.id.no);

        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
                // controlBluetoothConnection = new ControlBluetoothConnection(this);

                startAnimation();

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    requestBluetoothPermission();
                } else {
                    connectToBluetooth();
                }

            }
        });

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                launchPlugInActivity();
            }
        });

        dialog.show();
    }

    @Override
    protected void onResume() {
        super.onResume();

        //*******************************//
        if(isBLEConnected)
        {
            Intent in = new Intent(activity, DevicePairedActivity.class);
            in.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            Bundle bundle = new Bundle();
            bundle.putParcelable(Constants.MODULE_INFO, mModule);
            in.putExtra(Constants.MODULE_INFO_BUNDLE, bundle);
            startActivity(in);
            finish();
        }
        //************************************//
    }

    @Override
    protected void onPause() {
        super.onPause();


    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            unregisterReceiver(connectionEnableReceiver);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }

        try {
            unregisterReceiver(broadcastReceiver);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
    }

    /**
     *
     * Initialising Animation.
     *
     **/
    private void startAnimation() {
        final Animation animation = new AlphaAnimation(1, 0);
        animation.setDuration(1000);
        animation.setInterpolator(new LinearInterpolator());
        animation.setRepeatCount(Animation.INFINITE);
        animation.setRepeatMode(Animation.REVERSE);
        ble.startAnimation(animation);
    }

    /**
     *
     * @param moduleServiceId
     * @return
     */
    private Module getModuleInfo(String moduleServiceId) {
        Bundle bundle = getIntent().getBundleExtra(Constants.MODULE_INFO_BUNDLE);
        Module module = bundle.getParcelable(Constants.MODULE_INFO);
        return module;
    }


    @Override
    public void onBackPressed() {
    }

    /**
     *
     * Initialising UI Variables.
     *
     **/
    private void initiailiseUI() {

        title = (TextView) findViewById(R.id.title);
        title.setText(R.string.pairing_devices);
        ble = (ImageView) findViewById(R.id.ble);
        btnBack = (ImageView) findViewById(R.id.back);
        logout = (ImageView) findViewById(R.id.logout);
        btnBack.setVisibility(View.GONE);
        logout.setVisibility(View.GONE);
    }

    /**
     *
     * Bluetooth Permission Check.
     *
     **/
    private void requestBluetoothPermission() {
        Log.w(TAG, "Camera permission is not granted. Requesting permission");

        final String[] permissions = new String[]{Manifest.permission.ACCESS_COARSE_LOCATION};

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_COARSE_LOCATION)) {
                final CustomAlertDialog alertDialog = new CustomAlertDialog(this, null, getResources().getString(R.string.bluetooth_permission_failure), "");
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

            } else {
                ActivityCompat.requestPermissions(this, permissions, RC_HANDLE_COARSE_LOCATION_PERMISSION);
                return;
            }
        } else {
            connectToBluetooth();
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

                    connectToBluetooth();

                } else {
                    final CustomAlertDialog alertDialog = new CustomAlertDialog(PairingDeviceActivity.this, null, getResources().getString(R.string.bluetooth_permission_error), "");
                    alertDialog.show();
                    alertDialog.setCancelable(true);
                    alertDialog.setCanceledOnTouchOutside(false);
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
            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    /**
     *
     * Bluetooth Initialisation.
     *
     **/
    private void connectToBluetooth() {
        controlBluetoothConnection = ControlBluetoothConnection.getInstance(getApplicationContext());
        controlBluetoothConnection.connectBLE(mModule);
        // controlBluetoothConnection = new ControlBluetoothConnection(getApplicationContext(), mModule);
    }

    /**
     *
     * Handling Plugin Activity Initialisation.
     *
     **/
    private void launchPlugInActivity() {
        Intent in = new Intent(activity, PlugInModuleActivity.class);
        in.putExtra(Constants.DISCOVERED_SERVICE_ID, ArtikGattServices.SERVICE_UUID.toString());
        Bundle bundle = new Bundle();
        bundle.putParcelable(Constants.MODULE_INFO, mModule);
        in.putExtra(Constants.MODULE_INFO_BUNDLE, bundle);
        startActivity(in);
        finish();
    }


    @Override
    public void showAlert(Boolean isConnected) {

    }

    @Override
    public void showBluetoothAlert(Boolean isConnected) {
        if (!isConnected) {
            //******************//
            isBLEConnected = false;
            //******************//
            final CustomAlertDialog alertDialog = new CustomAlertDialog(PairingDeviceActivity.this, null, getResources().getString(R.string.bluetooth_failed_message), "");
            alertDialog.findViewById(R.id.ok).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    alertDialog.cancel();
                    controlBluetoothConnection.disconnectModule();
                    launchPlugInActivity();
//                    finish();
                }
            });
            alertDialog.show();
        }
    }
}

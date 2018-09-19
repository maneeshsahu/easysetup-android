package io.artik.easysetup.view;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import io.artik.easysetup.ble.ArtikGattServices;
import io.artik.easysetup.ble.ControlBluetoothConnection;
import io.artik.easysetup.R;
import io.artik.easysetup.util.CheckConnectionReceiver;
import io.artik.easysetup.util.Constants;
import io.artik.easysetup.util.CustomAlertDialog;
import io.artik.easysetup.util.Module;
import io.artik.easysetup.util.Utility;



/**
 * Created by 20102455 on 02-12-2016.
 */
public class DevicePairedActivity extends Activity implements View.OnClickListener, CheckConnectionReceiver.ICheckConnection {

    private static final String TAG = "DevicepairedActivity";
    private Button mConnectWifi;
    private ImageView btnBack, logout;
    private String mModuleServiceID;
    private Module mModule;
    private Utility utility;
    private TextView title;
    private IntentFilter intentFilter;
    private CheckConnectionReceiver connectionEnableReceiver;
    private ControlBluetoothConnection controlBluetoothConnection;

    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (!isFinishing()) {
                if (intent.getAction().equals(Constants.BOARD_CONNECTION_STATUS.BLE_DISCONNECTED.toString())) {
                    final CustomAlertDialog alertDialog = new CustomAlertDialog(DevicePairedActivity.this, null, getResources().getString(R.string.bluetooth_search_failed), "");
                    alertDialog.setCancelable(true);
                    alertDialog.setCanceledOnTouchOutside(false);
                    ((Button) alertDialog.findViewById(R.id.ok)).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            alertDialog.cancel();
                            Intent in = new Intent(DevicePairedActivity.this, PlugInModuleActivity.class);
                            in.putExtra(Constants.DISCOVERED_SERVICE_ID, ArtikGattServices.SERVICE_UUID.toString());
                            Bundle bundle = new Bundle();
                            bundle.putParcelable(Constants.MODULE_INFO, mModule);
                            in.putExtra(Constants.MODULE_INFO_BUNDLE, bundle);
                            startActivity(in);
                            finish();
                        }
                    });
                    alertDialog.show();
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_device_paired);

        intentFilter = new IntentFilter();
        intentFilter.addAction(Constants.BOARD_CONNECTION_STATUS.BLE_DISCONNECTED.toString());
        intentFilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);

        connectionEnableReceiver = new CheckConnectionReceiver();

        registerReceiver(broadcastReceiver, intentFilter);
        registerReceiver(connectionEnableReceiver, intentFilter);

        Utility.setConnectivityListener(this);

        utility = new Utility(this);
        mModule = getModuleInfo();
        if (mModule == null) {
            Log.d(TAG, String.valueOf(R.string.unabletoidentify));
        } else {
            initialiseUI();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        try {
            unregisterReceiver(broadcastReceiver);
        } catch (IllegalArgumentException e) {

        }

        try {
            unregisterReceiver(connectionEnableReceiver);
        } catch (IllegalArgumentException e) {

        }
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
     * Module Information.
     **/
    public void sendModuleInfo() {
        Intent data = new Intent(this, WiFiListActivity.class);
        data.putExtra(Constants.DISCOVERED_SERVICE_ID, ArtikGattServices.SERVICE_UUID.toString());
        Bundle bundle = new Bundle();
        bundle.putParcelable(Constants.MODULE_INFO, mModule);
        data.putExtra(Constants.MODULE_INFO_BUNDLE, bundle);
        startActivity(data);
    }

    /**
     * Initialing UI Variables.
     **/
    private void initialiseUI() {

        title = (TextView) findViewById(R.id.title);
        title.setText(R.string.device_paired_txt);
        mConnectWifi = (Button) findViewById(R.id.connect_wifi);
        mConnectWifi.setOnClickListener(this);
        btnBack = (ImageView) findViewById(R.id.back);
        logout = (ImageView) findViewById(R.id.logout);
        btnBack.setVisibility(View.GONE);
        logout.setVisibility(View.GONE);
        btnBack.setOnClickListener(this);
    }

    @Override
    public void onBackPressed() {
    }

    @Override
    public void onClick(View view) {

        switch (view.getId()) {
            case R.id.connect_wifi:

                sendModuleInfo();
                finish();

                break;
        }
    }

    @Override
    public void showAlert(Boolean isConnected) {

    }

    /**
     * Handled PluginActivity launch.
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
        if (!isConnected) {
            final CustomAlertDialog alertDialog = new CustomAlertDialog(DevicePairedActivity.this, null, getResources().getString(R.string.bluetooth_failed_message), "");
            alertDialog.findViewById(R.id.ok).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    alertDialog.cancel();
                    controlBluetoothConnection = ControlBluetoothConnection.getInstance(getApplicationContext());
                    controlBluetoothConnection.disconnectModule();
                    launchPlugInActivity();
                }
            });
            alertDialog.show();
        }
    }
}
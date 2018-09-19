package io.artik.easysetup.ble;

import android.annotation.TargetApi;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import io.artik.easysetup.util.Constants;
import io.artik.easysetup.util.Constants.BLE_STATUS_MESSAGES;
import io.artik.easysetup.util.Constants.REGISTRATION_STATE;


@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class BLEConnectionManager extends Service{

    private final IBinder mBinder = new LocalBinder();
    private Context mContext;
    private Handler mHandler;
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothDevice mDevice;
    private BluetoothGatt mBluetoothGatt;
    private BleBlockingQueue mBleBlockingQueue;
    private Boolean mRetvalue = false;
    private State mState = State.DISABLED;
    private String TAG = "BLEConnectionManager";
    //For Kitkat.
    private BluetoothAdapter.LeScanCallback leScanCallback;

    //For Lollipop.
    private ScanCallback mLeScanBack;

    private static final long SCAN_PERIOD = 30000;
    private UUID mServiceUUID;
    private int mRegistrationState = 0;
    private List<ScanFilter> filters;
    private String mModuleMacAddress;

    Runnable scanTimeout = new Runnable() {
        @Override
        public void run() {
            stopScanLeDevices(true);
        }
    };

    Runnable connectTimeout = new Runnable() {
        @Override
        public void run() {
            mHandler.sendEmptyMessage(BLE_STATUS_MESSAGES.GATT_FAILED.ordinal());
        }
    };

    private final BroadcastReceiver mBluetotohBroadcastReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {
                int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1);

                switch (state) {
                    case BluetoothAdapter.STATE_OFF:
                        //Indicates the local Bluetooth adapter is off.
                        break;

                    case BluetoothAdapter.STATE_TURNING_ON:
                        //Indicates the local Bluetooth adapter is turning on. However local clients should wait for STATE_ON before attempting to use the adapter.
                        break;

                    case BluetoothAdapter.STATE_ON:
                        //Indicates the local Bluetooth adapter is on, and ready for use.
                        mHandler.sendEmptyMessage(BLE_STATUS_MESSAGES.START_SCANNING.ordinal());
                        break;

                    case BluetoothAdapter.STATE_TURNING_OFF:
                        //Indicates the local Bluetooth adapter is turning off. Local clients should immediately attempt graceful disconnection of any remote links.
                        break;
                }
            }
        }

    };

    private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            Log.i(TAG, "onConnectionStateChange status is " + status + " and state is " + newState);

            if ((status == 133) || (status == 257)) {
                Log.d(TAG, "Unrecoverable error 133 or 257. DEVICE_DISCONNECTED intent broadcast with full reset");
            }
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                mState = State.CONNECTED;
                mBluetoothGatt = gatt;
                Log.i(TAG, "Connected to GATT server : " + mRegistrationState);
                if (mRegistrationState == REGISTRATION_STATE.REGIS_STATE_GET_CHALLENGE_PIN) {
                    Log.i(TAG, "mRegistrationState : REGIS_STATE_GET_CHALLENGE_PIN. Get ChallengePin");
                    writeCharacteristic(ArtikGattServices.START_REGISTRATION_UUID, "1");

                } else if (mRegistrationState == REGISTRATION_STATE.REGIS_STATE_GET_DEVICE_DID) {
                    //GATT reconnected. So request DeviceToken
                    Runnable task = new Runnable() {
                        public void run() {
                            readCharacteristic(ArtikGattServices.DEVICE_ID_CHARACTERISTIC_UUID);
                        }
                    };
                    worker.schedule(task, 5, TimeUnit.SECONDS);

                } else if (mRegistrationState == REGISTRATION_STATE.REGIS_STATE_COMPLETED) {

                } else if (mRegistrationState == REGISTRATION_STATE.REGIS_STATE_GET_DEVICE_TYPE_ID) {
                    readCharacteristic(ArtikGattServices.DEVICE_TYPE_ID_UUID);
                } else {
                    mHandler.sendEmptyMessage(BLE_STATUS_MESSAGES.GATT_CONNECTED.ordinal());
                }

            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                if (mState == State.DISCONNECTING) {
                    mState = State.DISCONNECTED;

                    Log.i(TAG, "Correctly disconnected from GATT server.");
                    mHandler.sendEmptyMessage(BLE_STATUS_MESSAGES.GATT_DISCONNECTED.ordinal());
                } else {
                    if (mRegistrationState >= REGISTRATION_STATE.REGIS_STATE_GET_DEVICE_DID) {
                        mHandler.sendMessage(Message.obtain(mHandler, BLE_STATUS_MESSAGES.DEVICE_DID_READ.ordinal(), "1232"));
                        mRegistrationState = REGISTRATION_STATE.REGIS_STATE_COMPLETED;
                    } else {
                        Log.i(TAG, "Something wrong happened and we're disconnected from GATT server.");
                        mHandler.sendEmptyMessage(BLE_STATUS_MESSAGES.GATT_FAILED.ordinal());
                        mBluetoothGatt.disconnect();
                    }
                }
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.w(TAG, "onServicesDiscovered!!!: " + status);
                mHandler.sendEmptyMessage(BLE_STATUS_MESSAGES.GATT_SERVICES_DISCOVERED.ordinal());
            } else {
                Log.w(TAG, "onServicesDiscovered received: " + status);
            }
        }


        @Override
        public void onCharacteristicRead(BluetoothGatt gatt,
                                         BluetoothGattCharacteristic characteristic,
                                         int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.d(TAG, "onCharacteristicRead received: " + status);
                mBleBlockingQueue.newResponse(characteristic);

                final byte[] data = characteristic.getValue();
                if (data != null && data.length > 0) {

                    String value = new String(data);
                    if (characteristic.getUuid().equals(ArtikGattServices.CHARACTERISTIC_WIFIAP)) {
                        mHandler.sendMessage(Message.obtain(mHandler, BLE_STATUS_MESSAGES.WIFI_AP_READ.ordinal(), value));
                    } else if (characteristic.getUuid().equals(ArtikGattServices.CHARACTERISTIC_VENDOR_ID)) {
                        mHandler.sendMessage(Message.obtain(mHandler, BLE_STATUS_MESSAGES.VENDOR_ID_READ.ordinal(), value));
                    } else if (characteristic.getUuid().equals(ArtikGattServices.CHARACTERISTIC_DEVICE_ID)) {
                        mHandler.sendMessage(Message.obtain(mHandler, BLE_STATUS_MESSAGES.DEVICE_ID_READ.ordinal(), value));
                    } else if (characteristic.getUuid().equals(ArtikGattServices.CHARACTERISTIC_IPADDRESS)) {
                        if ((value != null) || (!value.isEmpty()) || (value.contains("INVALID"))) {
                            Log.d(TAG, "Read Characeteristic CHARACTERISTIC_IPADDRESS :" + value);
                            mHandler.sendMessage(Message.obtain(mHandler, BLE_STATUS_MESSAGES.IPADDRESS_READ.ordinal(), value));
                        }
                    } else if (characteristic.getUuid().equals(ArtikGattServices.DEVICE_ID_CHARACTERISTIC_UUID)) {
                        if ((value != null) || (!value.isEmpty())) {
                            Log.d(TAG, "Read Characeteristic DEVICE_DID_UUID :" + value);
                            mHandler.sendMessage(Message.obtain(mHandler, BLE_STATUS_MESSAGES.DEVICE_DID_READ.ordinal(), value));
                            mRegistrationState = REGISTRATION_STATE.REGIS_STATE_COMPLETED;
                        }

                    } else {

                        Log.d(TAG, "onCharacteristicRead received: " + value + " mRegistrationState : " + mRegistrationState);
                        if (mRegistrationState == REGISTRATION_STATE.REGIS_STATE_GET_DEVICE_TYPE_ID) {
                            mHandler.sendMessage(Message.obtain(mHandler, BLE_STATUS_MESSAGES.DEVICE_TYPE_ID_READ.ordinal(), value));

                        } //else if (mRegistrationState == Constants.REGIS_STATE_GET_CHALLENGE_PIN) {
                        else if (characteristic.getUuid().equals(ArtikGattServices.CHALLENGE_PIN_UUID)) {
                            Log.e(TAG, "onCharacteristicRead CHALLENGE_PIN_UUID");
                            //Received Challenge Pin
                            mHandler.sendMessage(Message.obtain(mHandler, BLE_STATUS_MESSAGES.DEVICE_CHALLENGE_PIN_READ.ordinal(), value));
                            Log.d("TAG", "Read Characeteristic CHALLENGE_PIN :" + value);
                            mRegistrationState = REGISTRATION_STATE.REGIS_STATE_RECEIVED_CHALLENGE_PIN;

                        } else if (mRegistrationState == REGISTRATION_STATE.REGIS_STATE_GET_DEVICE_DID) {
                            mHandler.sendMessage(Message.obtain(mHandler, BLE_STATUS_MESSAGES.DEVICE_DID_READ.ordinal(), value));
                            mRegistrationState = REGISTRATION_STATE.REGIS_STATE_COMPLETED;

                        }
                    }
                }
            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt,
                                            BluetoothGattCharacteristic characteristic) {
            Log.d(TAG, "onCharacteristicChanged received: " + characteristic.getUuid().toString());
            // TODO notifications are not passed to the blocking queue
            // mBleBlockingQueue.newResponse(characteristic);

            final byte[] data = characteristic.getValue();
            if (data != null && data.length > 0) {

                String value = new String(data);

                if (characteristic.getUuid().equals(ArtikGattServices.CHARACTERISTIC_STATUS)) {
                    mHandler.sendMessage(Message.obtain(mHandler, BLE_STATUS_MESSAGES.STATUS_NOTIFIED.ordinal(), value));
                    Log.e(TAG, "status 1=" + value);
                } else if (characteristic.getUuid().equals(ArtikGattServices.CHARACTERISTIC_LONG_STATUS)) {
                    mHandler.sendMessage(Message.obtain(mHandler, BLE_STATUS_MESSAGES.LONG_STATUS_NOTIFIED.ordinal(), value));
                    Log.e(TAG, "status= 2" + value);
                } else {
                    if (mRegistrationState == REGISTRATION_STATE.REGIS_STATE_GET_DEVICE_TYPE_ID) {
                        Log.d(TAG, "Received REGIS_STATE_GET_DEVICE_TYPE_ID Pin mRegistrationState : " + mRegistrationState);
                        mHandler.sendMessage(Message.obtain(mHandler, BLE_STATUS_MESSAGES.DEVICE_TYPE_ID_READ.ordinal(), value));

                    } //else if (mRegistrationState == Constants.REGIS_STATE_GET_CHALLENGE_PIN) {
                    else if (characteristic.getUuid().equals(ArtikGattServices.CHALLENGE_PIN_UUID)) {
                        Log.d(TAG, "Received Challenge Pin mRegistrationState : " + mRegistrationState);
                        //Received Challenge Pin
                        mHandler.sendMessage(Message.obtain(mHandler, BLE_STATUS_MESSAGES.DEVICE_CHALLENGE_PIN_READ.ordinal(), value));
                        mRegistrationState = REGISTRATION_STATE.REGIS_STATE_RECEIVED_CHALLENGE_PIN;
                        Log.d("TAG", "changed mRegistrationState : " + mRegistrationState + " CHALLENGE_PIN :" + value);

                    } //else if (mRegistrationState == Constants.REGIS_STATE_GET_DEVICE_TOKEN) {
                    else if (characteristic.getUuid().equals(ArtikGattServices.DEVICE_ID_CHARACTERISTIC_UUID)) {
                        mHandler.sendMessage(Message.obtain(mHandler, BLE_STATUS_MESSAGES.DEVICE_DID_READ.ordinal(), value));
                        mRegistrationState = REGISTRATION_STATE.REGIS_STATE_COMPLETED;

                        Log.d(TAG, "mRegistrationState : " + mRegistrationState + " DEVICE_TOKEN :" + value);
                    }
                }
            }
        }

        @Override
        public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            Log.d(TAG, "onCharacteristicWrite received: " + characteristic.getUuid().toString() + "and STATUS: " + status);
            mBleBlockingQueue.newResponse(characteristic);
        }
    };

    private final ScheduledExecutorService worker =
            Executors.newSingleThreadScheduledExecutor();


    // Constructor

    public BLEConnectionManager() {
    }



    public BLEConnectionManager(Context context, Handler handler) {
        mContext = context;
        mHandler = handler;
    }

    private static void reverse(byte[] array) {
        if (array == null) {
            return;
        }
        int i = 0;
        int j = array.length - 1;
        byte tmp;
        while (j > i) {
            tmp = array[j];
            array[j] = array[i];
            array[i] = tmp;
            j--;
            i++;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        close();
    }

    public void setRegistrationState(int state) {

        mRegistrationState = state;
        Log.d("WifiOnboarding_Activity", "setRegistrationState : " + mRegistrationState);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        // After using a given device, you should make sure that
        // BluetoothGatt.close() is called
        // such that resources are cleaned up properly. In this particular
        // example, close() is
        // invoked when the UI is disconnected from the Service.
        close();
        try {
            unregisterReceiver(mBluetotohBroadcastReceiver);
        }catch (IllegalArgumentException e){
            Log.e(TAG, "Broadcast receiver not registered");
        }
        return super.onUnbind(intent);

    }

    public void close() {
        if (mBluetoothGatt == null) {
            return;
        }
        mBluetoothGatt.close();
        mBluetoothGatt = null;
    }

    public boolean init() {

        Log.d(TAG, "Ble Provisioner init");
        if (mContext == null) {
            return false;
        }

        if (!mContext.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            return false;
        }

        // Initializes a Bluetooth adapter.  For API level 18 and above, get a reference to
        // BluetoothAdapter through BluetoothManager.
        final BluetoothManager bluetoothManager =
                (BluetoothManager) mContext.getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();
        mBluetoothGatt = null;

        // Checks if Bluetooth is supported on the device.
        if (mBluetoothAdapter == null) {
            return false;
        }

        /*oms - start*/
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);

        registerReceiver(mBluetotohBroadcastReceiver,intentFilter);

        // Create the Blocking Queue with it's own thread and start it.
        mBleBlockingQueue = new BleBlockingQueue();
        mBleBlockingQueue.start();

        mState = State.INITIALIZED;
        Log.d(TAG, "Bluetooth initialized.");
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            //For Jelly Bean MR2 and Kitkat
            leScanCallback = new BluetoothAdapter.LeScanCallback() {
                @Override
                public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
                    if (mState == State.SCANNING && scanRecord != null &&
                            hasServiceUUID(scanRecord)) {
                        //if (hasServiceUUID(result.getScanRecord().getBytes())) { // If device advertise searched UUID
                        Log.d(TAG, "Found an onboarding device!");
                        mHandler.removeCallbacks(scanTimeout);
                        stopScanLeDevices(false);
                        mDevice = device;
                        mHandler.sendEmptyMessage(BLE_STATUS_MESSAGES.DETECTED_DEVICE.ordinal());
                    } else if (device.getName() != null && device.getName().equals("Artik Cloud Secure Regi")) {
                        Log.d(TAG, "Found SDR Device");
                        mHandler.removeCallbacks(scanTimeout);
                        stopScanLeDevices(false);
                        mDevice = device;
                        mHandler.sendMessage(Message.obtain(mHandler, BLE_STATUS_MESSAGES.DETECTED_SDR_DEVICE.ordinal(), device.getName()));
                    }
                }
            };
        } else {
            //For Lollipop.
            filters = new ArrayList<ScanFilter>();

            mLeScanBack = new ScanCallback() {
                @Override
                public void onBatchScanResults(List<ScanResult> results) {
                    super.onBatchScanResults(results);
                }

                @Override
                public void onScanResult(int callbackType, ScanResult result) {
                    super.onScanResult(callbackType, result);

                    if ((mState == State.SCANNING) && (result.getScanRecord().getServiceUuids() != null) &&
                            result.getScanRecord().getServiceUuids().toString().equalsIgnoreCase("[" + mServiceUUID.toString() + "]")) {
                        Log.d(TAG, "Found an onboarding device!");
                        mHandler.removeCallbacks(scanTimeout);
                        stopScanLeDevices(false);
                        result.getDevice().getAddress();
                        mDevice = result.getDevice();
                        mHandler.sendEmptyMessage(BLE_STATUS_MESSAGES.DETECTED_DEVICE.ordinal());
                    } else if (result.getDevice().getName() != null && result.getDevice().getName().equals("Artik Cloud Secure Regi")) {
                        Log.d(TAG, "Found SDR Device");
                        mHandler.removeCallbacks(scanTimeout);
                        stopScanLeDevices(false);
                        mDevice = result.getDevice();
                        mHandler.sendMessage(Message.obtain(mHandler, BLE_STATUS_MESSAGES.DETECTED_SDR_DEVICE.ordinal(), result.getDevice().getName()));
                    }
                }

                @Override
                public void onScanFailed(int errorCode) {

                }
            };
        }

        return true;
    }

    public boolean isEnabled() {

        if (mBluetoothAdapter == null)
            return false;

        return mBluetoothAdapter.isEnabled();
    }

    public void enableBluetooth(){
        if (!isEnabled())
            mBluetoothAdapter.enable();
    }

    public void setModuleMacAddress(String moduleMacAddress) {
        this.mModuleMacAddress = moduleMacAddress;
    }

    public void startScanLeDevices(UUID uuid, int rssiThreshold) {
        if (uuid == null) {
            Log.d(TAG, "startScanLeDevices is null");
        } else {
            Log.d(TAG, "startScanLeDevices UUID :" + uuid.toString());
        }
        mServiceUUID = uuid;

        startScanLeDevices();
    }

    public void startScanLeDevices() {
		
        try {
            ScanFilter filter = new ScanFilter.Builder().setDeviceAddress(mModuleMacAddress).build();
            filters.add(filter);
            Log.d(TAG, "LE Device Scan started");
            mState = State.SCANNING;
            mHandler.postDelayed(scanTimeout, SCAN_PERIOD);
            if (mBluetoothAdapter.isEnabled()) {
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                    mBluetoothAdapter.startLeScan(leScanCallback);
                } else {
                    mBluetoothAdapter.getBluetoothLeScanner().startScan(mLeScanBack);
                }
            }
        } catch (IllegalArgumentException exception) {
            mHandler.sendEmptyMessage(BLE_STATUS_MESSAGES.ERROR_INVALID_MAC_ADDRESS.ordinal());
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    public void stopScanLeDevices(boolean isTimeOut) {
        if (isTimeOut) {
            Intent i = new Intent();
            i.setAction(Constants.BLE_SCAN_TIMEOUT);
            sendBroadcast(i);
        }
        if (mState == State.SCANNING) {
            mState = State.INITIALIZED;
            if (mBluetoothAdapter.isEnabled()) {
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                    mBluetoothAdapter.stopLeScan(leScanCallback);
                } else {
                    mBluetoothAdapter.getBluetoothLeScanner().stopScan(mLeScanBack);
                }
            }
            // mBluetoothAdapter.stopLeScan(mLeScanCallback);
        } else if (mState == State.CONNECTING) {

        } else if (mState == State.CONNECTED) {

        }
    }

    private boolean hasServiceUUID(byte[] scanRecord) {

        int current_entry_idx = 0;
        int data_type_idx;
        int data_start_idx;

        while (current_entry_idx < scanRecord.length) {

            data_type_idx = current_entry_idx + 1;
            data_start_idx = current_entry_idx + 2;

            if (scanRecord[current_entry_idx] > 16 && scanRecord[data_type_idx] == 6) {
                // We found a 128 bits service UUID
                byte[] possibleService = Arrays.copyOfRange(scanRecord, data_start_idx, data_start_idx + 16);
                // We need to reverse the UUID
                BLEConnectionManager.reverse(possibleService);
                // Build the UUID to compare
                ByteBuffer bb = ByteBuffer.wrap(possibleService);
                UUID posible = new UUID(bb.getLong(), bb.getLong());

                if (posible.equals(mServiceUUID))
                    return true;

            }
            current_entry_idx = current_entry_idx + scanRecord[current_entry_idx] + 1;
        }
        return false;
    }

    public void connectGatt() {
        if (mBluetoothAdapter == null || mDevice == null) {
            Log.w(TAG, "BluetoothAdapter not initialized or undetected device.");
        }

        if (mBluetoothGatt != null) {
            Log.d(TAG, "Trying to use an existing mBluetoothGatt for connection.");
            if (mBluetoothGatt.connect()) {
                mState = State.CONNECTING;
            }
            mHandler.postDelayed(connectTimeout, SCAN_PERIOD);
        } else if (mDevice != null){

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                mBluetoothGatt = mDevice.connectGatt(mContext, true, mGattCallback, BluetoothDevice.TRANSPORT_LE);
            } else {
                mBluetoothGatt = mDevice.connectGatt(mContext, true, mGattCallback);
            }
        }
        Log.d(TAG, "Trying to create a new connection.");
        mState = State.CONNECTING;
    }

    public void disconnectGatt() {
        Log.e(TAG, "Disconnecting from gatt server.");
        if (mBluetoothAdapter == null || mDevice == null) {
            Log.w(TAG, "BluetoothAdapter not initialized or undetected device.");
        }
        if (mBluetoothGatt != null) {
            try {
                mBluetoothGatt.disconnect();
                mBluetoothGatt.close();
                Log.d(TAG, "Disconnecting from gatt server.");
                mState = State.DISCONNECTING;
            } catch (Exception ex) {
                Log.e(TAG, "Exception in disconnecting from gatt server.");
            }

        }
        mBluetoothGatt = null;
        mState = State.DISCONNECTING;
    }

    protected BluetoothGattCharacteristic getCharacteristicFromUUID(
            final UUID characteristicUUID) {

        if (mBluetoothAdapter == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return null;
        } else if (mBluetoothGatt == null) {
            Log.w(TAG, "mBluetoothGatt not initialized");
            return null;
        } else if (mServiceUUID == null) {
            Log.w(TAG, "mServiceUUID not initialized");
            return null;
        } else if (characteristicUUID == null) {
            Log.w(TAG, "characteristicUUID not initialized");
            return null;
        }

        BluetoothGattService gattService = mBluetoothGatt.getService(mServiceUUID);

        if (gattService == null) {
            Log.w(TAG, "GATT Service not found for UUID " + mServiceUUID.toString());
            return null;
        }
        // Get Characteristic
        return gattService.getCharacteristic(characteristicUUID);
    }

    public void readCharacteristic(final UUID characteristicUUID) {
        Log.w(TAG, "GATT Service Read Characteristic " + characteristicUUID.toString());
        final BluetoothGattCharacteristic characteristic = getCharacteristicFromUUID(characteristicUUID);

        if (characteristic == null) {
            Log.w(TAG, "Problem getting characteristic from UUID " + characteristicUUID.toString());
            return;
        }
        if (characteristicUUID.equals(ArtikGattServices.CHALLENGE_PIN_UUID)) {
            Log.d(TAG, "readCharacteristic CHALLENGE_PIN_UUID");
            mRegistrationState = REGISTRATION_STATE.REGIS_STATE_GET_CHALLENGE_PIN;
            Log.d("WifiOnboarding_Activity", "mRegistrationState : " + mRegistrationState);
        } else if (characteristicUUID.equals(ArtikGattServices.DEVICE_TYPE_ID_UUID)) {
            mRegistrationState = REGISTRATION_STATE.REGIS_STATE_GET_DEVICE_TYPE_ID;
            Log.d("WifiOnboarding_Activity", "mRegistrationState : " + mRegistrationState);
        } else if (characteristicUUID.equals(ArtikGattServices.CHARACTERISTIC_IPADDRESS)) {
            Log.d(TAG, "readCharacteristic CHARACTERISTIC_IPADDRESS");
        }
        mBleBlockingQueue.newRequest(characteristic, new Runnable() {
            @Override
            public void run() {

                if (mBluetoothGatt == null) {
                    Log.e(TAG, "Problem in gatt initialization");
                    Log.e(TAG, "For UUID " + characteristicUUID.toString());
                    /* TODO - Re-init */
                    return;
                }
                if (!mBluetoothGatt.readCharacteristic(characteristic))
                    Log.w(TAG, "readCharacteristic not possible for UUID " + characteristicUUID.toString());
            }
        }, true);


    }

    public void setCharacteristicNotification(final UUID characteristicUUID,
                                              final boolean enabled) {

        final BluetoothGattCharacteristic characteristic = getCharacteristicFromUUID(characteristicUUID);

        mBleBlockingQueue.newRequest(characteristic, new Runnable() {
            @Override
            public void run() {
                // Get Service
                try {
                    if (!mBluetoothGatt.setCharacteristicNotification(characteristic, enabled))
                        Log.w(TAG, "setCharacteristicNotification not possible for UUID " + characteristicUUID.toString());

                    List<BluetoothGattDescriptor> descriptors = characteristic.getDescriptors();
                    for (BluetoothGattDescriptor bluetoothGattDescriptor : descriptors) {
                        if (enabled) {
                            if (!bluetoothGattDescriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE))
                                Log.w(TAG, "setValue not possible for UUID " + characteristicUUID.toString());
                        } else {
                            if (!bluetoothGattDescriptor.setValue(BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE))
                                Log.w(TAG, "setValue not possible for UUID " + characteristicUUID.toString());
                        }
                        if (!mBluetoothGatt.writeDescriptor(bluetoothGattDescriptor))
                            Log.w(TAG, "writeDescriptor not possible for UUID " + characteristicUUID.toString());
                    }
                } catch (Exception e) {
                    Log.e(TAG, e.getMessage());
                }
            }
        }, false);
    }


    public void writeLongCharacteristic(final UUID characteristicUUID, final String value) {


        new Thread(){
            @Override
            public void run() {
                super.run();
                int offset = 0;
                while (offset < value.length()) {

                    int endOffSet = (offset + 20) > value.length() ?  value.length(): offset + 20;
                    String packetVal =  value.substring(offset, endOffSet);
                    offset = endOffSet;
                    writeCharacteristic(characteristicUUID, packetVal);
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

            }
        }.start();

        return;
    }

    public boolean writeCharacteristic(final UUID characteristicUUID, final String value) {

        mRetvalue = false;

        final BluetoothGattCharacteristic characteristic = getCharacteristicFromUUID(characteristicUUID);

        if (characteristic == null) {
            Log.w(TAG, "Problem getting characteristic from UUID " + characteristicUUID.toString());
            return false;
        }


        mBleBlockingQueue.newRequest(characteristic, new Runnable() {
            @Override
            public void run() {

                characteristic.setValue(value);

                if (!mBluetoothGatt.writeCharacteristic(characteristic)) {
                    Log.w(TAG, "writeCharacteristic not possible for UUID " + characteristicUUID.toString());

                } else {
                    mRetvalue = true;
                    if (characteristicUUID.equals(ArtikGattServices.COMPLETE_REGISTRATION_UUID)) {
                        Log.d("WifiOnboarding_Activity", "mRegistrationState : " + REGISTRATION_STATE.REGIS_STATE_GET_DEVICE_DID);
                        mRegistrationState = REGISTRATION_STATE.REGIS_STATE_GET_DEVICE_DID;
                    }
                }
            }
        }, true);
        return mRetvalue;
    }

    public void writeCharacteristic(final UUID characteristicUUID, final byte[] value) {

        final BluetoothGattCharacteristic characteristic = getCharacteristicFromUUID(characteristicUUID);

        Log.d(TAG, "Writing Chactersitc UUID " + characteristicUUID.toString());
        if (characteristic == null) {
            Log.w(TAG, "Problem getting characteristic from UUID " + characteristicUUID.toString());
            return;
        }

        mBleBlockingQueue.newRequest(characteristic, new Runnable() {
            @Override
            public void run() {

                characteristic.setValue(value);

                if (!mBluetoothGatt.writeCharacteristic(characteristic))
                    Log.w(TAG, "writeCharacteristic not possible for UUID " + characteristicUUID.toString());
            }
        }, true);

    }

    public void discoverServices() {
        if (mBluetoothAdapter == null || mDevice == null) {
            Log.w(TAG, "BluetoothAdapter not initialized or undetected device.");

        }

        if (mBluetoothGatt != null) {
            mBluetoothGatt.discoverServices();

        }

    }

    public void wait(int milliseconds) {
        try {
            synchronized (Thread.currentThread()) {
                Thread.currentThread().wait(milliseconds);
            }
        } catch (InterruptedException e) {
            //ignore
        }
    }

    private enum State {
        DISABLED,        // Provisioner not started or released
        INITIALIZED,    // Provisioner started
        SCANNING,        // BLE interface is ON and scanning
        CONNECTING,    // Device found trying to connect
        CONNECTED,        // Connected as central device
        DISCONNECTING,  // Disconnecting
        DISCONNECTED,   // Disconnected
        FAILED,            // Provisioner has failed
        PAUSED            // Provisioner is paused
    }

    public class LocalBinder extends Binder {
        public BLEConnectionManager getService(Context context, Handler handler) {
            mContext = context;
            mHandler = handler;
            return BLEConnectionManager.this;
        }
    }

}

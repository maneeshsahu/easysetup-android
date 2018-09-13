package io.artik.easysetup.util;

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * Created by 20102455 on 28-12-2016.
 */
public class CheckConnectionReceiver extends BroadcastReceiver {
    public static ICheckConnection connectivityReceiverListener;
    private static boolean isConnectedToInternet = true;

    public CheckConnectionReceiver() {
        super();
    }


    @Override
    public void onReceive(Context context, Intent intent) {

        if (Constants.WIFI_STATE_CHANGED.equalsIgnoreCase(intent.getAction()) || Constants.CONNECTIVITY_CHANGE.equalsIgnoreCase(intent.getAction()) ||Constants.STATE_CHANGE.equalsIgnoreCase(intent.getAction())) {
            checkInternet(context);
        }
        if (BluetoothAdapter.ACTION_STATE_CHANGED.equalsIgnoreCase(intent.getAction()))
        {
            checkBluetooth(intent);

        }


    }

    public interface ICheckConnection {
        void showAlert(Boolean isConnected);
        void showBluetoothAlert(Boolean isConnected);
    }

    public void checkBluetooth(Intent intent)
    {
        if(intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1)
                == BluetoothAdapter.STATE_OFF)
        {
            if (connectivityReceiverListener != null) {
                connectivityReceiverListener.showBluetoothAlert(false);
            }

        }
      else
        {
            if (connectivityReceiverListener != null) {
                connectivityReceiverListener.showBluetoothAlert(true);
            }

        }

    }

    public void checkInternet(Context mcontext){


        ConnectivityManager connectivityManager = (ConnectivityManager) mcontext
                .getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo networkInfo = connectivityManager
                .getActiveNetworkInfo();

        // Check internet connection and accrding to state change the
        // text of activity by calling method
        if (networkInfo != null && networkInfo.isConnected()) {

            if (connectivityReceiverListener != null && !isConnectedToInternet) {
                connectivityReceiverListener.showAlert(true);
                isConnectedToInternet = true;
            }
        } else {
            if (connectivityReceiverListener != null && isConnectedToInternet) {
                connectivityReceiverListener.showAlert(false);
                isConnectedToInternet = false;
            }
        }
    }

}

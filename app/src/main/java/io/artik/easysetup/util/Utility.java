package io.artik.easysetup.util;

import android.app.Activity;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import io.artik.easysetup.R;

/**
 * Created by 20115642 on 07-12-2016.
 */
public class Utility implements View.OnClickListener{
    private Activity context;
    private TextView dialogMessage1;
    private TextView dialogMessage2;
    private TextView dialogTitle;
    private Button ok;
    private Dialog dialog;
    public Utility(Activity context) {
        this.context = context;
    }


    /**
     * Bluetooth enable check
     * @return
     */
    public static boolean isBluetoothEnabled()
    {
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        return mBluetoothAdapter.isEnabled();

    }

    /**
     * Register the listener for broadcast network check
     * @param listener
     */
    public static void setConnectivityListener(CheckConnectionReceiver.ICheckConnection listener) {
        CheckConnectionReceiver.connectivityReceiverListener = listener;
    }


    /**
     *  dismiss alert dialog property
     */
    private  void dismissAlertDialog(){
        if(dialog!= null)
        dialog.dismiss();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId())
        {
            case R.id.ok:
                dismissAlertDialog();

        }
    }
}

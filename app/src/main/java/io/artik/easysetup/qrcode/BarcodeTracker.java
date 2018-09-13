package io.artik.easysetup.qrcode;

/**
 * Created by 20115642 on 18-01-2017.
 */


import android.content.Context;
import com.google.android.gms.vision.Tracker;
import com.google.android.gms.vision.barcode.Barcode;

/**
 * Created by 20112980 on 01/10/17.
 */
public class BarcodeTracker extends Tracker<Barcode> {

    private BarcodeGraphicTrackerCallback mListener;

    public interface BarcodeGraphicTrackerCallback {
        void onBarcodeAvailable(Barcode barcode);
    }

    BarcodeTracker(Context listener) {
        mListener = (BarcodeGraphicTrackerCallback) listener;
    }

    @Override
    public void onNewItem(int id, Barcode item) {
        if (item.displayValue != null)
            mListener.onBarcodeAvailable(item);
        }


}

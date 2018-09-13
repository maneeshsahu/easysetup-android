package io.artik.easysetup.view;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.vision.MultiProcessor;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;

import java.io.IOException;

import io.artik.easysetup.R;
import io.artik.easysetup.model.IQRScanCallback;
import io.artik.easysetup.qrcode.BarcodeTracker;
import io.artik.easysetup.qrcode.BarcodeTrackerFactory;
import io.artik.easysetup.qrcode.CameraSource;
import io.artik.easysetup.qrcode.CameraSourcePreview;
import io.artik.easysetup.util.Constants;
import io.artik.easysetup.util.CustomAlertDialog;
import io.artik.easysetup.util.Module;
import io.artik.easysetup.wifi.UserInputManager;

/**
 * User scans the QR code.
 */


public class QRCodeScanActivity extends Activity implements View.OnClickListener, BarcodeTracker.BarcodeGraphicTrackerCallback, IQRScanCallback {

    private static final Boolean AUTOFOCUS = true;
    private static final Boolean USEFLASH = false;

    private static final String TAG = "QRC_CaptureActivity";

    // intent request code to handle updating play services if needed.
    private static final int RC_HANDLE_GMS = 9001;

    // permission request codes need to be < 256
    private static final int RC_HANDLE_CAMERA_PERM = 2;

    private CameraSource mCameraSource;
    private CameraSourcePreview mPreview;

    private ImageView btnBack, logout;
    private UserInputManager userInputManager;
    private TextView mTitle;
    private Handler handler;
    private int rc;
    private boolean isBarcodeDetected = false;

    // helper objects for detecting taps and pinches.
    private ScaleGestureDetector scaleGestureDetector;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qr_scan);
        initialiseUI();
        handler = new Handler();
        userInputManager = new UserInputManager(this);
        rc = ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA);
        if (rc == PackageManager.PERMISSION_GRANTED) {
            createCameraSource(AUTOFOCUS, USEFLASH);
        } else {
            requestCameraPermission();
        }

        scaleGestureDetector = new ScaleGestureDetector(this, new ScaleListener());

    }


    /**
     * Initialising the UI variables.
     **/
    private void initialiseUI() {
        btnBack = (ImageView) findViewById(R.id.back);
        mPreview = (CameraSourcePreview) findViewById(R.id.preview);
        logout = (ImageView) findViewById(R.id.logout);
        mTitle = (TextView) findViewById(R.id.title);
        mTitle.setText(R.string.scan_qr);
        btnBack.setVisibility(View.VISIBLE);
        logout.setVisibility(View.INVISIBLE);
        btnBack.setOnClickListener(this);

    }

    @Override
    protected void onResume() {
        super.onResume();
        startCameraSource();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mPreview != null) {
            mPreview.stop();
        }
    }

    @Override
    public void onBackPressed() {

        handleBackPress();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        isBarcodeDetected = false;

        if (mPreview != null) {
            mPreview.release();
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.back:
                if (mPreview != null) {
                    mPreview.stop();

                    handleBackPress();
                    break;

                }
        }
    }

    private void handleBackPress() {

        finish();
    }

    @Override
    public synchronized void onBarcodeAvailable(Barcode barcode) {
        if (!isFinishing() && !isBarcodeDetected) {
            isBarcodeDetected = true;
            Module module = null;
            if (barcode != null && barcode.displayValue != null) {

                Thread t = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        if (mPreview != null) {
                            mPreview.stop();
                            //   mPreview.release();

                        }
                    }
                });
                t.start();

                String barCodeString = barcode.displayValue;
                Log.e(TAG, barCodeString);
                module = userInputManager.validateuserInput(barCodeString);
                if (module != null) {
                    goToArtikInfo(module, barCodeString);
                }
            } else {
                Log.e(TAG, "no barcode detected");
            }

        } else {
            Log.e(TAG, String.valueOf(R.string.invalid_qr));
        }

    }

    @Override
    public boolean onTouchEvent(MotionEvent e) {
        boolean b = scaleGestureDetector.onTouchEvent(e);

        return b || super.onTouchEvent(e);
    }

    /**
     * @param module
     * @param userInput
     */
    private void goToArtikInfo(Module module, String userInput) {

        Intent intent = new Intent(this, BoardDetailsActivity.class);
        intent.putExtra(Constants.USER_INPUT, userInput);
        intent.putExtra(Constants.MODULE, Constants.QRCODE);
        Bundle bundle = new Bundle();
        bundle.putParcelable(Constants.MODULE_INFO, module);
        intent.putExtra(Constants.MODULE_INFO_BUNDLE, bundle);
        startActivity(intent);
        finish();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == RC_HANDLE_CAMERA_PERM) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                createCameraSource(AUTOFOCUS, USEFLASH);
            } else {
                final CustomAlertDialog alertDialog = new CustomAlertDialog(this, null, getResources().getString(R.string.camera_permission_failure), "");
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
    }

    /**
     * Starts or restarts the camera source, if it exists.  If the camera source doesn't exist yet
     * (e.g., because onResume was called before the camera source was created), this will be called
     * again when the camera source is created.
     */
    private void startCameraSource() throws SecurityException {
        // check that the device has play services available.
        int code = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(
                getApplicationContext());
        isBarcodeDetected = false;
        if (code != ConnectionResult.SUCCESS) {
            Dialog dlg =
                    GoogleApiAvailability.getInstance().getErrorDialog(this, code, RC_HANDLE_GMS);
            dlg.show();
        }

        if (mCameraSource != null) {
            try {
                mPreview.start(mCameraSource);
            } catch (IOException e) {
                Log.e(TAG, String.valueOf(R.string.camera_notstarted), e);
                mCameraSource.release();
                mCameraSource = null;
            }
        }
    }

    /**
     * Handles the requesting of the camera permission.
     */
    private void requestCameraPermission() {

        Log.w(TAG, String.valueOf(R.string.camera_permission));

        final String[] permissions = new String[]{Manifest.permission.CAMERA};

        if (!ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.CAMERA)) {
            ActivityCompat.requestPermissions(this, permissions, RC_HANDLE_CAMERA_PERM);
            return;
        } else {
            final CustomAlertDialog alertDialog = new CustomAlertDialog(this, null, getResources().getString(R.string.camera_permission_failure_deny), "");
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

    /**
     * Creates and starts the camera.  Note that this uses a higher resolution in comparison
     * to other detection examples to enable the barcode detector to detect small barcodes
     * at long distances.
     * <p>
     * Suppressing InlinedApi since there is a check that the minimum version is met before using
     * the constant.
     */
    private void createCameraSource(boolean autoFocus, boolean useFlash) {
        Context context = getApplicationContext();

        // A barcode detector is created to track barcodes.  An associated multi-processor instance
        // is set to receive the barcode detection results, track the barcodes, and maintain
        // graphics for each barcode on screen.  The factory is used by the multi-processor to
        // create a separate tracker instance for each barcode.
        BarcodeDetector barcodeDetector = new BarcodeDetector.Builder(context).build();
        BarcodeTrackerFactory barcodeFactory = new BarcodeTrackerFactory(this);
        barcodeDetector.setProcessor(
                new MultiProcessor.Builder<>(barcodeFactory).build());


        if (!barcodeDetector.isOperational()) {
            // Note: The first time that an app using the barcode or face API is installed on a
            // device, GMS will download a native libraries to the device in order to do detection.
            // Usually this completes before the app is run for the first time.  But if that
            // download has not yet completed, then the above call will not detect any barcodes
            // and/or faces.
            //
            // isOperational() can be used to check if the required native libraries are currently
            // available.  The detectors will automatically become operational once the library
            // downloads complete on device.
            Log.w(TAG, String.valueOf(R.string.detector_dependency));


            // Check for low storage.  If there is low storage, the native library will not be
            // downloaded, so detection will not become operational.

            IntentFilter lowstorageFilter = new IntentFilter(Intent.ACTION_DEVICE_STORAGE_LOW);
            boolean hasLowStorage = registerReceiver(null, lowstorageFilter) != null;

            if (hasLowStorage) {
                Toast.makeText(this, R.string.low_storage_error, Toast.LENGTH_LONG).show();
                Log.w(TAG, getString(R.string.low_storage_error));
            }
        }

        // Creates and starts the camera.  Note that this uses a higher resolution in comparison
        // to other detection examples to enable the barcode detector to detect small barcodes
        // at long distances.
        CameraSource.Builder builder = new CameraSource.Builder(getApplicationContext(), barcodeDetector)
                .setFacing(CameraSource.CAMERA_FACING_BACK)
                .setRequestedPreviewSize(1600, 1024)
                .setRequestedFps(15.0f);

        // make sure that auto focus is an available option
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            builder = builder.setFocusMode(
                    autoFocus ? Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE : null);
        }

        mCameraSource = builder
                .setFlashMode(useFlash ? Camera.Parameters.FLASH_MODE_TORCH : null)
                .build();

    }

    @Override
    public void showDialog() {
        handler.post(new Runnable() {
            @Override
            public void run() {
                final CustomAlertDialog alertDialog = new CustomAlertDialog(QRCodeScanActivity.this, null, QRCodeScanActivity.this.getResources().getString(R.string.errormessage1), QRCodeScanActivity.this.getResources().getString(R.string.errormessage2));
                alertDialog.show();

                alertDialog.findViewById(R.id.ok).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        startCameraSource();
                        alertDialog.cancel();
                    }
                });
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Check which request we're responding to
        if (requestCode == Constants.QR_SCAN_ACTIVITY_REQUEST) {
            finish();
        }
    }

    private class ScaleListener implements ScaleGestureDetector.OnScaleGestureListener {

        /**
         * Responds to scaling events for a gesture in progress.
         * Reported by pointer motion.
         *
         * @param detector The detector reporting the event - use this to
         *                 retrieve extended info about event state.
         * @return Whether or not the detector should consider this event
         * as handled. If an event was not handled, the detector
         * will continue to accumulate movement until an event is
         * handled. This can be useful if an application, for example,
         * only wants to update scaling factors if the change is
         * greater than 0.01.
         */
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            return false;
        }

        /**
         * Responds to the beginning of a scaling gesture. Reported by
         * new pointers going down.
         *
         * @param detector The detector reporting the event - use this to
         *                 retrieve extended info about event state.
         * @return Whether or not the detector should continue recognizing
         * this gesture. For example, if a gesture is beginning
         * with a focal point outside of a region where it makes
         * sense, onScaleBegin() may return false to ignore the
         * rest of the gesture.
         */
        @Override
        public boolean onScaleBegin(ScaleGestureDetector detector) {
            return true;
        }

        /**
         * Responds to the end of a scale gesture. Reported by existing
         * pointers going up.
         * <p/>
         * Once a scale has ended, {@link ScaleGestureDetector#getFocusX()}
         * and {@link ScaleGestureDetector#getFocusY()} will return focal point
         * of the pointers remaining on the screen.
         *
         * @param detector The detector reporting the event - use this to
         *                 retrieve extended info about event state.
         */
        @Override
        public void onScaleEnd(ScaleGestureDetector detector) {
            mCameraSource.doZoom(detector.getScaleFactor());
        }
    }
}
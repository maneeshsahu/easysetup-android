package io.artik.easysetup.view;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import io.artik.easysetup.R;

import io.artik.easysetup.util.Constants;
import io.artik.easysetup.util.Module;
import io.artik.easysetup.util.ServiceUUID;
import io.artik.easysetup.util.Utility;

/**
 * User enters the device name and its location(unused) to be onboarded
 */
public class BoardDetailsActivity extends Activity implements View.OnClickListener {
    private static final String TAG = "BoardDetails";
    private Button enter, skip;
    private ImageView btnBack, logout, artikIdentified;
    private String mModuleServiceID;
    private Module mModule;
    private Utility utility;
    private TextView moduleId, moduleTitle;
    private EditText moduleName, moduleLocation;
    private TextView tvcodeTitle;
    private String codeTitle;
    private String userInput;
    private Boolean isModuleName = false;
//    private ArrayList<Device> devices = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_board_details);
        initialiseUI();

        utility = new Utility(this);

        if (getIntent() != null) {
            userInput = getIntent().getStringExtra(Constants.USER_INPUT);
        }


        mModule = getModuleInfo(mModuleServiceID);
        if (mModule != null) {
            moduleId.setText(userInput);
            //checkIfAlreadyRegistered(mModule);
            if (mModule.getType() != null) {
                moduleTitle.setText(getString(R.string.artikcaps) + " " + mModule.getType() + " " + getString(R.string.identified));
                artikIdentified(mModule.getType());
            } else {
                moduleTitle.setText(R.string.artikmoduleidentified);
                artikIdentified.setVisibility(View.GONE);
            }
        }
    }

    /**
     *
     * @param module
     */
    private void artikIdentified(String module) {

        switch (module) {
            case Constants.ARTIK_0:
                artikIdentified.setImageResource(R.drawable.artik_zero_icon);
                break;
            case Constants.ARTIK_5:
                artikIdentified.setImageResource(R.drawable.artik_five_icon);
                break;
            case Constants.ARTIK_7:
                artikIdentified.setImageResource(R.drawable.artik_seven_icon);
                break;

        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        InputMethodManager imm = (InputMethodManager) getSystemService(this.
                INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        return true;
    }

    /**
     *
     * @param mModuleServiceID
     * @return
     */
    private Module getModuleInfo(String mModuleServiceID) {
        Bundle bundle = getIntent().getBundleExtra(Constants.MODULE_INFO_BUNDLE);
        Module module = bundle.getParcelable(Constants.MODULE_INFO);
        module.setUuid(ServiceUUID.SERVICE_UUID);
        return module;
    }

    /**
     *
     * Initialising the UI variables.
     *
     **/
    private void initialiseUI() {
        moduleId = (TextView) findViewById(R.id.uuid);
        moduleId.setAllCaps(true);
        moduleName = (EditText) findViewById(R.id.modulename);
        artikIdentified = (ImageView) findViewById(R.id.artik_identified);
        moduleLocation = (EditText) findViewById(R.id.modulelocation);
        enter = (Button) findViewById(R.id.enterbtn);
        enter.setEnabled(false);
        moduleTitle = (TextView) findViewById(R.id.title);
        moduleName.addTextChangedListener(moduleNameWatcher);

        tvcodeTitle = (TextView) findViewById(R.id.codeTitle);
        enter.setOnClickListener(this);
        btnBack = (ImageView) findViewById(R.id.back);
        skip = (Button) findViewById(R.id.skip);
        logout = (ImageView) findViewById(R.id.logout);
        skip.setOnClickListener(this);
        btnBack.setVisibility(View.VISIBLE);
        logout.setVisibility(View.GONE);
        btnBack.setOnClickListener(this);
        Bundle bundle = getIntent().getExtras();
        codeTitle = bundle.getString(Constants.MODULE);
        if (codeTitle != null && codeTitle.equalsIgnoreCase(Constants.MANUALINPUT)) {
            tvcodeTitle.setText(R.string.modelId);

        } else if (codeTitle != null && codeTitle.equalsIgnoreCase(Constants.QRCODE)) {
            tvcodeTitle.setText(R.string.qrcode);
        }
    }


    /**
     *
     * Edittext Watcher for module name.
     *
     **/
    private final TextWatcher moduleNameWatcher = new TextWatcher() {
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        public void onTextChanged(CharSequence s, int start, int before, int count) {
            if (s.length() == 0) {

                isModuleName = false;
                moduleName.setError(null);
                enter.setEnabled(false);
            }

            if ((s.length() > 0 && s.length() < 5) || (s.length() == 64)) {

                isModuleName = false;
                moduleName.setError(getResources().getString(R.string.namecharerror));
                enter.setEnabled(false);
            }
        }

        public void afterTextChanged(Editable s) {
            if (s.length() >= 5) {

                /* Check if the name already exists */
                if (doesDeviceNameExist(s.toString())) {
                    moduleName.setError(getString(R.string.error_same_name));
                    enter.setEnabled(false);
                    return;
                }
                isModuleName = true;
                enter.setEnabled(true);
            }

        }
    };


    private boolean doesDeviceNameExist(String name) {
//        for (Device module : devices) {
//            if (module.getName().equalsIgnoreCase(name)) {
//                return true;
//            }
//        }
        return false;
    }

    @Override
    public void onBackPressed() {
        handleBackPress();

    }

    /**
     *
     * Handling the BackPress.
     *
     **/
    private void handleBackPress() {
        if (codeTitle != null && codeTitle.equalsIgnoreCase(Constants.MANUALINPUT)) {
            Intent in2 = new Intent(this, ManualInputActivity.class);
            in2.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(in2);
        } else if (codeTitle != null && codeTitle.equalsIgnoreCase(Constants.QRCODE)) {
            Intent in2 = new Intent(this, QRCodeScanActivity.class);
            in2.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(in2);
        }
        finish();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.skip:
                String devname = getString(R.string.artikcaps) + mModule.getVersion();
                if (doesDeviceNameExist(devname)) {
                    moduleName.setError(getString(R.string.default_device_name_error));
                } else {
                    mModule.setPlace("");
                    mModule.setName(getString(R.string.artikcaps) + mModule.getVersion());
                    sendModuleInfo();
                }
                break;
            case R.id.enterbtn:
                if (isModuleName) {
                    mModule.setPlace(moduleLocation.getText().toString());
                    mModule.setName(moduleName.getText().toString());
                    sendModuleInfo();
                }
                break;
            case R.id.back:

                handleBackPress();
                break;
        }
    }

    /**
     *
     * Module Information.
     *
     **/
    public void sendModuleInfo() {
        Intent data = new Intent(this, PlugInModuleActivity.class);
        data.putExtra(Constants.DISCOVERED_SERVICE_ID, ServiceUUID.SERVICE_UUID.toString());
        Bundle bundle = new Bundle();
        bundle.putParcelable(Constants.MODULE_INFO, mModule);
        data.putExtra(Constants.MODULE_INFO_BUNDLE, bundle);
        startActivity(data);
        finish();
    }

}
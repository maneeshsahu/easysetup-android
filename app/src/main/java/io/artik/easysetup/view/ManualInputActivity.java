package io.artik.easysetup.view;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import io.artik.easysetup.R;

import io.artik.easysetup.model.IQRScanCallback;
import io.artik.easysetup.util.Constants;
import io.artik.easysetup.util.CustomAlertDialog;
import io.artik.easysetup.util.Module;
import io.artik.easysetup.wifi.UserInputManager;

/**
 * User enters Serial Number, Mac ID(s) in a text field.
 */
public class ManualInputActivity extends Activity implements View.OnClickListener, IQRScanCallback {

    private Button btnsubmit;
    private ImageView btnBack, logout;
    private EditText enterModuleID;
    private Boolean isModuleID = false;
    private TextView mTitle;
    private UserInputManager userInputManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        setContentView(R.layout.activity_manual_input);
        userInputManager = new UserInputManager(this);
        initialiseUI();
    }

    /**
     * Initialising UI Variables.
     **/
    private void initialiseUI() {
        btnsubmit = (Button) findViewById(R.id.submit);
        mTitle = (TextView) findViewById(R.id.title);
        mTitle.setText(R.string.manual_input);
        btnBack = (ImageView) findViewById(R.id.back);
        logout = (ImageView) findViewById(R.id.logout);
        enterModuleID = (EditText) findViewById(R.id.enterModuleID);
        enterModuleID.addTextChangedListener(moduleNameWatcher);
        btnBack.setOnClickListener(this);
        logout.setVisibility(View.GONE);
        btnsubmit.setOnClickListener(this);
        btnsubmit.setVisibility(View.VISIBLE);
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        InputMethodManager imm = (InputMethodManager) getSystemService(this.
                INPUT_METHOD_SERVICE);

        if (getCurrentFocus() != null) {
            imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        }
        return true;
    }


    /**
     * EditText Watcher for module name entry.
     *
     **/
    private final TextWatcher moduleNameWatcher = new TextWatcher() {
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            enterModuleID.setError(null);
        }

        public void onTextChanged(CharSequence s, int start, int before, int count) {
            // Checking the string length for error condition
            if (s.length() == 0) {
                isModuleID = false;
                enterModuleID.setError(null);
                btnsubmit.setEnabled(false);
            }

            if (s.length() > 0 && s.length() < 4) {
                isModuleID = false;
                enterModuleID.setError(getResources().getString(R.string.enter_valid_moduleId));
                btnsubmit.setEnabled(false);
            }
        }

        public void afterTextChanged(Editable s) {
            // Checking the string length for error condition
            if (s.length() >= 4) {
                isModuleID = true;
                enterModuleID.setError(null);
                btnsubmit.setEnabled(true);
            }
        }
    };


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == Constants.MANUAL_INPUT_ACTIVITY_REQUEST) {
            finish();
        }
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.submit:
                if (isModuleID) {
                    Module module = null;
                    if (enterModuleID.getText() != null) {
                        module = userInputManager.validateuserInput(enterModuleID.getText().toString().toUpperCase());
                        if (module != null) {
                            goToArtikInfo(module, enterModuleID.getText().toString());
                        }
                    }
                }

                break;

            case R.id.back:
                finish();
                break;
        }
    }

    /**
     * @param module
     * @param userInput
     */
    private void goToArtikInfo(Module module, String userInput) {

        Intent intent = new Intent(this, BoardDetailsActivity.class);
        intent.putExtra(Constants.USER_INPUT, userInput);
        intent.putExtra(Constants.MODULE, Constants.MANUALINPUT);
        Bundle bundle = new Bundle();
        bundle.putParcelable(Constants.MODULE_INFO, module);
        intent.putExtra(Constants.MODULE_INFO_BUNDLE, bundle);
        startActivity(intent);
        finish();
    }

    @Override
    public void showDialog() {
        final CustomAlertDialog alertDialog = new CustomAlertDialog(ManualInputActivity.this, "", getResources().getString(R.string.errordialogmessage), getResources().getString(R.string.enter_moduleId));
        alertDialog.setCancelable(true);
        alertDialog.setCanceledOnTouchOutside(false);
        alertDialog.show();

        alertDialog.findViewById(R.id.ok).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.cancel();
            }
        });
    }
}



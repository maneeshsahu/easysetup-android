package io.artik.easysetup.view;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.UUID;

import io.artik.easysetup.R;
import io.artik.easysetup.util.Constants;
import io.artik.easysetup.util.Module;

/**
 * Users needs to turn on the module and press the pairing button.
 */
public class PlugInModuleActivity extends Activity implements View.OnClickListener {

    private ImageView btnBack, logout, plug;
    private Button btnContinue;
    private String mModuleServiceID;
    private Module mModule;
    private TextView moduleTitle;
    private static String TAG = "PluginInModule";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_plugin_artik_module);
        if (getIntent() != null) {
            mModuleServiceID = getIntent().getStringExtra(Constants.DISCOVERED_SERVICE_ID);
        }

        initialiseUI();
        if (mModuleServiceID != null) {
            mModule = getModuleInfo(mModuleServiceID);
        }
    }

    @Override
    public void onBackPressed() {

        finish();
    }

    /**
     *
     * Initialising the UI variable.
     *
     **/
    private void initialiseUI() {

        moduleTitle = (TextView) findViewById(R.id.title);
        moduleTitle.setText(R.string.plug_in);
        btnContinue = (Button) findViewById(R.id.continue_pairing);
        btnContinue.setOnClickListener(this);
        btnBack = (ImageView) findViewById(R.id.back);
        plug = (ImageView) findViewById(R.id.plug);
        logout = (ImageView) findViewById(R.id.logout);
        logout.setVisibility(View.GONE);
        btnBack.setOnClickListener(this);

        ObjectAnimator cloudAnim = ObjectAnimator.ofFloat(plug.findViewById(R.id.plug), "y", getResources().getDimension(R.dimen._185sdp), getResources().getDimension(R.dimen._144sdp));
        cloudAnim.setDuration(3000);
        cloudAnim.setRepeatCount(ValueAnimator.INFINITE);
        cloudAnim.setRepeatMode(ValueAnimator.RESTART);
        cloudAnim.start();

    }


    /**
     *
     * @param mModuleServiceID
     * @return
     */
    private Module getModuleInfo(String mModuleServiceID) {
        Bundle bundle = getIntent().getBundleExtra(Constants.MODULE_INFO_BUNDLE);
        Module module = bundle.getParcelable(Constants.MODULE_INFO);
        assert module != null;
        module.setUuid(UUID.fromString(mModuleServiceID));
        return module;
    }


    /**
     *
     * Module Information.
     *
     **/
    private void sendModuleInfo() {

        int moduleVersion = Integer.valueOf(mModule.getType());
        if (moduleVersion >= 3) {
            Log.i(TAG, "ARTIK Gateway Module identified");
            Intent data = new Intent(this, SoftAPOnboarding.class);
            //data.putExtra(Constants.DISCOVERED_SERVICE_ID, ArtikGattServices.SERVICE_UUID.toString());
            Bundle bundle = new Bundle();
            bundle.putParcelable(Constants.MODULE_INFO, mModule);
            data.putExtra(Constants.MODULE_INFO_BUNDLE, bundle);
            startActivity(data);
        } else if (mModule.getVersion().startsWith("051") || mModule.getVersion().startsWith("053") || mModule.getVersion().startsWith("055")) {
            Log.i(TAG, "ARTIK 05x identified");
            Intent data = new Intent(this, SoftAPOnboarding.class);
            Bundle bundle = new Bundle();
            bundle.putParcelable(Constants.MODULE_INFO, mModule);
            data.putExtra(Constants.MODULE_INFO_BUNDLE, bundle);
            startActivity(data);
        } /*else {
            Log.i(TAG, "ARTIK Edge Node Identified");
            Intent data = new Intent(this, EdgeNodeOnBoardingActivity.class);
            data.putExtra(Constants.DISCOVERED_SERVICE_ID, ArtikGattServices.SERVICE_UUID.toString());
            Bundle bundle = new Bundle();
            bundle.putParcelable(Constants.MODULE_INFO, mModule);
            data.putExtra(Constants.MODULE_INFO_BUNDLE, bundle);
            startActivity(data);
        }*/
        finish();
    }

    @Override
    public void onClick(View view) {

        switch (view.getId()) {
            case R.id.continue_pairing:
                sendModuleInfo();
                break;
            case R.id.back:
                finish();
                break;
        }
    }
}
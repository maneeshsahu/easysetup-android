package io.artik.easysetup.view;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.UUID;

import io.artik.easysetup.R;
import io.artik.easysetup.util.Constants;
import io.artik.easysetup.util.Module;

/**
 * Created by 20102455 on 06-12-2016.
 */
public class RegistrationSuccessActivity extends Activity implements View.OnClickListener {

    private static final String TAG = "RegisterationSuccess";
    private Button continuesetup;
    private ImageView btnBack,logout;
    private String mModuleServiceID;
    private Module mModule;
    private TextView title;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registeration_success);
        if(getIntent()!= null)
        {
            mModuleServiceID = getIntent().getStringExtra(Constants.DISCOVERED_SERVICE_ID);
        }
        if(mModuleServiceID != null)
        {
            mModule= getModuleInfo(mModuleServiceID);
        }
        else
        {
            Log.d(TAG, String.valueOf(R.string.unabletoidentify));
        }

        initialiseUI();

    }
    private Module getModuleInfo(String mModuleServiceID)
    {
        Bundle bundle = getIntent().getBundleExtra(Constants.MODULE_INFO_BUNDLE);
        Module module = bundle.getParcelable(Constants.MODULE_INFO);
        if (module != null) {
            module.setUuid(UUID.fromString(mModuleServiceID));
        }
        return module;
    }


    /**
     * Initialising UI variables.
     **/
    private void initialiseUI() {

        title = (TextView) findViewById(R.id.title);
        title.setText(R.string.registeration_successful);
        continuesetup = (Button) findViewById(R.id.continuesetup);
        continuesetup.setOnClickListener(this);
        btnBack = (ImageView) findViewById(R.id.back);
        logout = (ImageView) findViewById(R.id.logout);
        logout.setVisibility(View.GONE);
        btnBack.setVisibility(View.GONE);
        btnBack.setOnClickListener(this);

    }

    @Override
    public void onBackPressed() {
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.continuesetup:
                Intent in = new Intent(RegistrationSuccessActivity.this, StartEasySetupActivity.class);
                in.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                in.putExtra(Constants.IS_NEW_MODULE_ADDED, true);
                startActivity(in);
                finish();

                break;
        }
    }
}

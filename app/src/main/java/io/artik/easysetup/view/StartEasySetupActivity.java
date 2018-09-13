package io.artik.easysetup.view;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;

import io.artik.easysetup.R;

public class StartEasySetupActivity extends AppCompatActivity implements View.OnClickListener {
    private Button qrScanButton, manualInputButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        setContentView(R.layout.activity_start_easysetup);
        initializeUI();
    }

    /**
     * Initialize UI Variables.
     **/
    private void initializeUI() {
        qrScanButton = (Button) findViewById(R.id.qrScan);
        manualInputButton = (Button) findViewById(R.id.manualInput);

        qrScanButton.setOnClickListener(this);
        manualInputButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.qrScan:
                Intent in = new Intent(this, QRCodeScanActivity.class);
                startActivity(in);
                break;

            case R.id.manualInput:
                Intent in1 = new Intent(this, ManualInputActivity.class);
                startActivity(in1);
                break;

        }
    }
}

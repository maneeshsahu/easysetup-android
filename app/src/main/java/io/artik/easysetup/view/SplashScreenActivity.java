package io.artik.easysetup.view;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.Window;

import io.artik.easysetup.R;
/**
 * Displays a splash screen.
 */
public class SplashScreenActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.splash_screen);
        Handler mHandler = new Handler();
        Window window = getWindow();

        if (Build.VERSION.SDK_INT >= 21) {
            window.setStatusBarColor(getResources().getColor(R.color.black_color_background));
        }

        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent in = new Intent(SplashScreenActivity.this, StartEasySetupActivity.class);
                startActivity(in);
                finish();
            }
        }, 2000);

    }
}

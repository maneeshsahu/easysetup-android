package io.artik.easysetup.util;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import io.artik.easysetup.R;

/**
 * Helper Class to create custom success message dialog
 * Created by vsingh on 20/02/17.
 */

public class CustomMessageSuccessDialog extends Dialog {
    private String mMessage1 ,mMessage2;

    public CustomMessageSuccessDialog(Context context, String Message1, String Message2) {
        super(context);
        setContentView(R.layout.custom_success_dialog);

        mMessage1 = Message1;
        mMessage2 = Message2;
        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        TextView errorTxt1 = (TextView) findViewById(R.id.text1);
        TextView errorTxt2 = (TextView) findViewById(R.id.text2);
        if(mMessage2 == null || mMessage2.isEmpty() || mMessage2.equals("") )
        {
            errorTxt2.setVisibility(View.GONE);
        }
        else
        {
            errorTxt2.setVisibility(View.VISIBLE);
        }
        errorTxt1.setText(mMessage1);
        errorTxt2.setText(mMessage2);

        Button btnNo = (Button) findViewById(R.id.ok);
        btnNo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cancel();
            }
        });
    }
}

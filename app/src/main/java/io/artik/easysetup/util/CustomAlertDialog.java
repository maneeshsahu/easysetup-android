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
 * Helper Class to create a custom alert
 */
public class CustomAlertDialog extends Dialog{

    private String mHeader, mMessage1 ,mMessage2;


    /**
     *
     * @param context
     * @param Header
     * @param Message1
     * @param Message2
     */
    public CustomAlertDialog(Context context, String Header, String Message1 ,String Message2) {
        super(context);
        setContentView(R.layout.custom_error_dialog);

        mHeader = Header;
        mMessage1 = Message1;
        mMessage2 = Message2;
        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        TextView errorTxt1 = (TextView) findViewById(R.id.errortext1);
        TextView errorTxt2 = (TextView) findViewById(R.id.errortext2);
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

        Button btn = (Button) findViewById(R.id.ok);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cancel();
            }
        });
    }
}
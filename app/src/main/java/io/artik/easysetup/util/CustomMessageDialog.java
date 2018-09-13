package io.artik.easysetup.util;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import io.artik.easysetup.R;

/**
 * Created by 291626 on 12/28/2016.
 */

public class CustomMessageDialog extends Dialog{


    /**
     *
     * @param context
     * @param Header
     * @param Body
     */
    public CustomMessageDialog(Context context, String Header, String Body) {
        super(context);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.custom_message_dialog);
        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));


        TextView headerTxt = (TextView) findViewById(R.id.message_header);
        TextView messageTxt = (TextView) findViewById(R.id.message_body);
        headerTxt.setText(Header);
        messageTxt.setText(Body);

        Button btnNo = (Button) findViewById(R.id.no);
        btnNo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cancel();
            }
        });
    }
}

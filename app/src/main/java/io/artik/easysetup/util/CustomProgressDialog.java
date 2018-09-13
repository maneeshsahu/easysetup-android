package io.artik.easysetup.util;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.view.Window;
import android.widget.TextView;

import io.artik.easysetup.R;

/**
 * Helper Class to create a custom progress dialog
 * Created by 291626 on 12/28/2016.
 */

public class CustomProgressDialog extends Dialog {

    /**
     *
     * @param context
     * @param progressHeader
     * @param progressMessage
     */
    public CustomProgressDialog(Context context, String progressHeader, String progressMessage) {
        super(context);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE); //before
        setContentView(R.layout.custom_progress_dialog);
        //getWindow().setBackgroundDrawable(context.getResources().getDrawable(R.color.list_item_color));

        TextView progressHeaderTxt = (TextView) findViewById(R.id.progress_header);
        TextView progressMessageTxt = (TextView) findViewById(R.id.progress_message);

        if(progressHeader != null)
            progressHeaderTxt.setText(progressHeader);
        else
            progressHeaderTxt.setVisibility(View.INVISIBLE);

        if(progressMessage != null)
            progressMessageTxt.setText(progressMessage);
        else
            progressMessageTxt.setVisibility(View.INVISIBLE);
    }
}

package io.artik.easysetup.controller;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import io.artik.easysetup.R;

/**
 * Created by 20115642 on 14-12-2016.
 */
public class WifiListAdapter extends ArrayAdapter<Map<String, String>> {

    private List<Map<String, String>> wifiList = new ArrayList<>();
    private Context context;
    private LayoutInflater inflater = null;

    public WifiListAdapter(Context context, int resource, List<Map<String, String>> wifiList) {
        super(context, resource, wifiList);
        this.wifiList = wifiList;
        this.context = context;
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }


    @Override
    public int getCount() {
        return wifiList.size();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ViewHolder holder = null;

        if (convertView == null) {
            convertView = inflater.inflate(R.layout.wifi_list_item, parent, false);
            holder = new ViewHolder();

            holder.locked = (ImageView) convertView.findViewById(R.id.del);
            holder.wifiName = (TextView) convertView.findViewById(R.id.wifiName); // Wifi Name

            convertView.setTag(holder);

        } else {
            holder = (ViewHolder) convertView.getTag();
        }


        if (wifiList.get(position).get("encryption").equals("Open")) {
            holder.locked.setVisibility(View.INVISIBLE);
        } else {
            holder.locked.setVisibility(View.VISIBLE);
        }

        holder.wifiName.setText(wifiList.get(position).get("ssid"));

        return convertView;
    }

    static class ViewHolder {
        private ImageView locked;
        private TextView wifiName;
    }
}

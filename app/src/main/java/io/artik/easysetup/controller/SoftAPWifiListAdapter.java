package io.artik.easysetup.controller;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import io.artik.easysetup.api.model.AccessPointInfo;
import io.artik.easysetup.R;

import java.util.ArrayList;

import io.artik.easysetup.api.model.AccessPointInfo;

/**
 * Created by vsingh on 06/03/17.
 */

public class SoftAPWifiListAdapter extends ArrayAdapter<AccessPointInfo> {

    private ArrayList<AccessPointInfo> wifiList = new ArrayList<>();
    private Context context;
    private LayoutInflater inflater = null;

    public SoftAPWifiListAdapter(Context context, int resource, ArrayList<AccessPointInfo> wifiList) {
        super(context, resource, wifiList);
        this.wifiList = wifiList;
        this.context = context;
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public void setWifiList(ArrayList<AccessPointInfo> accessPointInfos) {
        wifiList = accessPointInfos;
        this.notifyDataSetChanged();
    }


    @Override
    public int getCount() {
        if (wifiList != null)
            return wifiList.size();
        else
            return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        SoftAPWifiListAdapter.ViewHolder holder = null;

        if (convertView == null) {
            convertView = inflater.inflate(io.artik.easysetup.R.layout.wifi_list_item, parent, false);
            holder = new SoftAPWifiListAdapter.ViewHolder();

            holder.locked = (ImageView) convertView.findViewById(io.artik.easysetup.R.id.del);
            holder.wifiName = (TextView) convertView.findViewById(io.artik.easysetup.R.id.wifiName); // Wifi Name

            convertView.setTag(holder);

        } else {
            holder = (SoftAPWifiListAdapter.ViewHolder) convertView.getTag();
        }


        if (wifiList.get(position).isSecure()) {
            holder.locked.setVisibility(View.VISIBLE);
        } else {
            holder.locked.setVisibility(View.INVISIBLE);
        }

        holder.wifiName.setText(wifiList.get(position).getSsid());

        return convertView;
    }

    static class ViewHolder {
        private ImageView locked;
        private TextView wifiName;
    }
}

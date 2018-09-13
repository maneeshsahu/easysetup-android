package io.artik.easysetup.api.model;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

/**
 * Created by vsingh on 06/03/17.
 */

public class GetAPListBaseResponse {

    @SerializedName("accesspoints")
    private ArrayList<AccessPointInfo> accessPointInfoArrayList = new ArrayList<>();


    public ArrayList<AccessPointInfo> getAccessPointInfoArrayList() {
        return accessPointInfoArrayList;
    }

    public void setAccessPointInfoArrayList(ArrayList<AccessPointInfo> accessPointInfoArrayList) {
        this.accessPointInfoArrayList = accessPointInfoArrayList;
    }
}

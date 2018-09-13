package io.artik.easysetup.api.model;

import com.google.gson.annotations.SerializedName;

/**
 * Created by vsingh on 07/03/17.
 */

public class SoftAPGetDTIDResponse {
    @SerializedName("dtid")
    private String dtid;

    public String getDtid() {
        return dtid;
    }

    public void setDtid(String dtid) {
        this.dtid = dtid;
    }
}

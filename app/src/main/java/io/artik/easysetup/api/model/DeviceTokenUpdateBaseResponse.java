package io.artik.easysetup.api.model;

import com.google.gson.annotations.SerializedName;

/**
 * Created by vsingh on 07/03/17.
 */

public class DeviceTokenUpdateBaseResponse {
    @SerializedName("data")
    private DeviceTokenUpdateResponse deviceTokenUpdateResponse;

    public DeviceTokenUpdateResponse getDeviceTokenUpdateResponse() {
        return deviceTokenUpdateResponse;
    }

    public void setDeviceTokenUpdateResponse(DeviceTokenUpdateResponse deviceTokenUpdateResponse) {
        this.deviceTokenUpdateResponse = deviceTokenUpdateResponse;
    }
}

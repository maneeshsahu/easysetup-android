package io.artik.easysetup.api.model;

import com.google.gson.annotations.SerializedName;

/**
 * Created by vsingh on 07/03/17.
 */

public class SoftAPGenericResponse {
    @SerializedName("error")
    private boolean error;
    @SerializedName("reason")
    private String reason;
    @SerializedName("error_code")
    private int error_code;

    public boolean isError() {
        return error;
    }

    public void setError(boolean error) {
        this.error = error;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public int getError_code() {
        return error_code;
    }

    public void setError_code(int error_code) {
        this.error_code = error_code;
    }
}

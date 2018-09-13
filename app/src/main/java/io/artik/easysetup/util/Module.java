package io.artik.easysetup.util;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.UUID;

/**
 * Device Module Class
 * Created by 20115642 on 19-12-2016.
 */
public class Module implements Parcelable {
    public static final Creator<Module> CREATOR = new Creator<Module>() {
        @Override
        public Module createFromParcel(Parcel in) {
            return new Module(in);
        }

        @Override
        public Module[] newArray(int size) {
            return new Module[size];
        }
    };
    String mName = "Module";
    String mPlace = "Living Room";
    String mType ;
    String mVersion;
    String mMacAddress;
    String mServiceID;
    String mIPAddress;
    String mDeviceUUID;
    String mDID;
    String mSecondaryMac = "";

    UUID uuid = null;

    public Module() {

    }

    /**
     *
     * @param name
     * @param place
     * @param type
     * @param version
     * @param macAddress
     * @param serviceID
     */
    public Module(String name, String place, String type, String version, String macAddress, String serviceID) {
        this.mName = name;
        this.mPlace = place;
        this.mType = type;
        this.mVersion = version;
        this.mMacAddress = macAddress;
        this.mServiceID = serviceID;
    }

    public void setSecondaryMac(String mac) {
        mSecondaryMac = mac;
    }
    public String getSecondaryMac() {
        return mSecondaryMac;
    }

    /**
     *
     * @param in
     */
    protected Module(Parcel in) {
        mName = in.readString();
        mPlace = in.readString();
        mType = in.readString();
        mVersion = in.readString();
        mMacAddress = in.readString();
        mServiceID = in.readString();
        mIPAddress = in.readString();
        mDeviceUUID = in.readString();
        mDID = in.readString();
        mSecondaryMac = in.readString();
    }

    public String getMacAddress() {
        return mMacAddress;
    }

    public void setMacAddress(String macAddress) {
        this.mMacAddress = macAddress;
    }

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        this.mName = name;
    }

    public String getPlace() {
        return mPlace;
    }

    public void setPlace(String place) {
        this.mPlace = place;
    }

    public String getServiceID() {
        return mServiceID;
    }

    public void setServiceID(String serviceID) {
        this.mServiceID = serviceID;
    }

    public String  getType() {
        return mType;
    }

    public void setType(String type) {
        this.mType = type;
    }

    public String getVersion() {
        return mVersion;
    }

    public void setVersion(String version) {
        this.mVersion = version;
    }

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }


    public String getIPAddress() {
        return mIPAddress;
    }

    public void setIPAddress(String IPAddress) {
        this.mIPAddress = IPAddress;
    }

    public String getDeviceUUID() {
        return mDeviceUUID;
    }

    public void setDeviceUUID(String deviceUUID) {
        this.mDeviceUUID = deviceUUID;
    }

    public String getDID() {
        return mDID;
    }

    public void setDID(String mDID) {
        this.mDID = mDID;
    }
    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mName);
        dest.writeString(mPlace);
        dest.writeString(mType);
        dest.writeString(mVersion);
        dest.writeString(mMacAddress);
        dest.writeString(mServiceID);
        dest.writeString(mIPAddress);
        dest.writeString(mDeviceUUID);
        dest.writeString(mDID);
        dest.writeString(mSecondaryMac);
    }
}

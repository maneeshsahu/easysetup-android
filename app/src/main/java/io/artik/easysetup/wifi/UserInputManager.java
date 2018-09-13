package io.artik.easysetup.wifi;

import android.app.Activity;
import android.util.Log;

import io.artik.easysetup.model.IQRScanCallback;
import io.artik.easysetup.util.Constants;
import io.artik.easysetup.util.Module;

/**
 * Created by 20115642 on 19-12-2016.
 */
public class UserInputManager {
    private static final String TAG = "QRC_CaptureActivity";
    private Activity mContext;
    private Module mModule;


    private IQRScanCallback iqrScanCallback;

    public UserInputManager(IQRScanCallback iqrScanCallback) {
        this.iqrScanCallback = iqrScanCallback;
    }

    public Module validateuserInput(String userInput ) {
             mModule  = null;
            String[] userInputContent = userInput.split(",");

            if (((userInputContent.length == 3) &&
                    isValidMacAddress(userInputContent[1])
                    && isValidMacAddress(userInputContent[2])) ||
                    ((userInputContent.length == 2) && isValidMacAddress(userInputContent[1]))) {



                mModule = new Module();
                if (validateVersion(userInputContent)) {
                    if (mModule.getType().equals(Constants.ARTIK_0)) {
                        /* Thread or BLE * Wiif Mac for Edge Nodes */
                        mModule.setMacAddress(userInputContent[1]);
                    } else {
                        /* BLE Mac */
                        mModule.setMacAddress(formatMACAddress(userInputContent[2]));
                        /* Wifi Mac */
                        mModule.setSecondaryMac(userInputContent[1]);
                    }
                    //mModule.setDeviceUUID(ArtikGattServices.SERVICE_UUID.toString());
                } else {
                    mModule = null;
                    iqrScanCallback.showDialog();

                }
            } else {
                mModule = null;
                iqrScanCallback.showDialog();

        }
        return mModule;
    }


    /**
     *
     * @param macAddress
     * @return
     */
    private boolean isValidMacAddress(String macAddress){
        boolean isValid = true;

        if((macAddress == null) || (macAddress.length() != 12))
        {
            isValid = false;
            return isValid;
        }

        for(int i =0; i < macAddress.length() ; i++){
            char c =  macAddress.charAt(i);
            if(((c >= '0') && (c <= '9'))||((c >= 'A') &&(c <= 'F')) || ((c >= 'a') && (c <= 'f')))
                continue;
            else {
                isValid = false;
                break;
            }
        }
        return isValid;
    }


    /**
     *
     * @param uuid
     * @return
     */
    private boolean validateVersion(String[] uuid) {
        boolean isValid = true;
        String version = uuid[0];
        Log.e(TAG, "" + version);
        if (version != null) {
            String ver_String =  version.substring(0, 1);
            char ver_code = ver_String.charAt(0);

            if(!(ver_code >= '0' && ver_code <= '9')){
                isValid = false;
            }else{
                int versionCode = Integer.parseInt(ver_String);
                switch (String.valueOf(versionCode)) {
                    case Constants.ARTIK_0:
                        if(uuid.length != 2)
                            isValid = false;
                        else
                            setModuleInfo(version,versionCode);
                        break;
                    case Constants.ARTIK_5:
                    case Constants.ARTIK_3:
                    case Constants.ARTIK_7:
                    case Constants.ARTIK_10:
                        if(uuid.length != 3)
                            isValid = false;
                        else
                            setModuleInfo(version,versionCode);
                        break;
                    default:
                        isValid = false;
                        break;
                }
            }


        }else{
            isValid = false;
        }


        return isValid;
    }


    /**
     *
     * @param version
     * @param versionCode
     */
    private void setModuleInfo(String version , int versionCode){
        mModule.setVersion(version);
        mModule.setType(String.valueOf(versionCode));
    }


    /**
     *
     * @param unfomratedMACAddress
     * @return
     */
    private String formatMACAddress(String unfomratedMACAddress) {
        char divisionChar = ':';
        String formatedMAC = unfomratedMACAddress.replaceAll("(.{2})", "$1" + divisionChar).substring(0, 17);
        Log.e(TAG, "formatedMAC = " + formatedMAC);
        return formatedMAC;
    }
}

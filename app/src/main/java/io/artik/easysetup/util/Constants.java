package io.artik.easysetup.util;

/**
 * Commone static string values used in the project
 * Created by 20102455 on 01-12-2016.
 */
public class Constants {
    public static final String ALL_ARTIK ="ALL";
    public static final String ARTIK_0 = "0";
    public static final String ARTIK_3 = "3";
    public static final String ARTIK_5 = "5";
    public static final String ARTIK_7 = "7";
    public static final String ARTIK_10 = "10";
    public static final String ARTIK_0_DTID  = "dtffe018d82ab24f2981dea0307a630f71";
    public static final String ARTIK_5_DTID  = "dt6594d3d6959446f292ab19b26609251b";
    public static final String ARTIK_7_DTID  = "dtc5ecf0abccaa428c853e144c964ad727";
    public static final String ARTIK_7_GEN2_DTID  = "dt2d93bdb9c8fa446eb4a35544e66150f7";
    public static final String ARTIK_053_DTID = "dt2d93bdb9c8fa446eb4a35544e66150f7";
    public static final String IS_NEW_MODULE_ADDED = "newModuleAdded";
    public static final String POSITION  = "position";
    public static final String REFRESH_IS_DONE  = "refreshed";
    public static final String ONBOARDING_ACCESS_TOKEN_PREF = "OnboardingAccessToken";
    public static final String GATEWAY_IPADDRESS = "GATEWAY_IPADDRESS";
    public static final String ONBOARDING_PREFS = "OnboardingPreferences" ;
    public static final String ONBOARDING_USERID_PREF = "OnboardingUserId";
    public static final String ACCESS_TOKEN = "AccessToken";
    public static final String DISCOVERED_SERVICE_ID = "DISCOVERED_SERVICE_ID";
    public static final String MODULE_INFO = "MODULE_INFO";
    public static final String MODULE_INFO_BUNDLE = "MODULE_INFO_BUNDLE";
    public static final String USER_DETAILS = "user_details";
    public static final String DRAWER_LAST_POSITION = "lastposition";
    public static final String MODULE = "module";
    public static final String MANUALINPUT = "manualInput";
    public static final String QRCODE = "qrcode";
    public static final String ERROR_STATUS = "ERROR_STATUS";
    public static final String OPEN = "OPEN";
    public static final String WIFI_LIST = "wifilist";
    public static final String SETWIFIAPENABLED = "setWifiApEnabled";
    public static final String ISWIFIAPENABLED = "isWifiApEnabled";
    public static final String BLE_SCAN_TIMEOUT = "bluetooth_scan_time_out";
    public static final int QR_SCAN_ACTIVITY_REQUEST = 3;
    public static final int MANUAL_INPUT_ACTIVITY_REQUEST = 4;
    public static final String DELETE_FAILED = "delete_failed";
    public static final String DEVICE_PRESENCE_UPDATED = "device_presence_updated";
    public static final String CONNECTIVITY_CHANGE  = "android.net.conn.CONNECTIVITY_CHANGE";
    public static final String WIFI_STATE_CHANGED  = "android.net.wifi.WIFI_STATE_CHANGED";
    public static final String STATE_CHANGE  = "android.net.wifi.STATE_CHANGE";
    public static final String USER_INPUT = "userinput";
    public final static  String BASE_URL_ARTIK = "https://api.artik.cloud/v1.1/";
    /* This one is just without last slash */
    public final static  String CLOUD_URL = "https://api.artik.cloud/v1.1";
    public final static String CLOUD_ACCOUNTS_URL ="https://accounts.artik.cloud";
    public final static String CLOUD_AUTHORIZE_URL = "https://accounts.artik.cloud/authorize";
    public final static String CLOUD_SIGNOUT_URL = "https://accounts.artik.cloud/signout";
    public final static  String NAME = "name";
    public final static  String EMAILID = "emailId";
    public final static  String CREATED_ON = "createdDate";
    public final static  String MODIFIED_ON = "modifiedDate";
    public static final String DEVICE_TYPES_LOADED = "devicetypesloaded";
    public static final String DEVICE_MANIFEST_LOADED = "devicemanifestloaded";
    public static final String DEVICE_ACTIVITY_REFRESHED = "deviceactivityrefreshed";
    public static final String DEVICE_ACTION_SUCCESS = "deviceactionsuccess";
    public static final String DEVICE_ACTION_FAILED = "deviceactionfailed";
    public static final String DEVICE_PRESENCE_ONLINE = "deviceonline";
    public static final String DEVICE_PRESENCE_OFFLINE = "deviceoffline";
    public static final String DELETE_SUCCESS = "deletesuccess";
    public static final String DEVICE_CREATED_SUCCESS = "devicecreatedsuccess";
    public static final String DEVICE_CREATED_FAILED = "devicecreatedfailed";
    public static final String ADD_NEW_MODULE = "addnewmodule" ;
    public static final String SDR_USER_CONFIRM_SUCCESS = "sdrconfirmuser";
    public static final String ERROR_CODE = "errorcode" ;
    public static final String CODE_VERIFIER_KEY = "code_verifier";
    public static final String CODE_CHALLENGE_KEY = "code_challenge";
    public static final String CLIENT_ID_KEY = "client_id";
    public static final String CLOUD_TOKEN_URL = "https://accounts.artik.cloud/token" ;
    public static final String RULES_REFRESHED = "rules_refreshed";
    public static final String RULES_CREATE_SUCCESS = "rules_create_success";
    public static final String RULES_CREATE_FAILED = "rules_create_failed";
    public static final String RULES_DELETE_FAILED = "rules_delete_failed";
    public static final String RULES_DELETE_SUCCESS = "rules_delete_success";
    public static final String RULES_ACTION_SUCCESS = "rules_action_success";
    public static final String RULES_ACTION_FAILED = "rules_action_failed";
    public static final String RULES_UPDATE_FAILED = "rules_update_failed";
    public static final String RULES_UPDATE_SUCCESS = "rules_update_success";
    public static String DEVICE_SHARE_FAILED = "share_device_failed";
    public static String DEVICE_SHARE_SUCCESS = "share_device_success";
    public static String AUTH_FAILURE = "authfailure";


    public class REGISTRATION_STATE{
        public static final int REGIS_STATE_START = 0;
        public static final int REGIS_STATE_GET_DEVICE_TYPE_ID = 1;
        public static final int REGIS_STATE_GET_CHALLENGE_PIN = 2;
        public static final int REGIS_STATE_RECEIVED_CHALLENGE_PIN = 5;
        public static final int REGIS_STATE_USER_CONFIRMED = 6;
        public static final int REGIS_STATE_GET_DEVICE_DID = 7;
        public static final int REGIS_STATE_COMPLETED = 8;
    }

    /**
     *     enum for Node Registration Status
     */
    public enum EDGE_NODE_REGISTRATION_STATUS{
        REGISTRATION_COMPLETE,
        REGISTRATION_FAILED,
        AUTHENTICATION_PROMPT;

        public static EDGE_NODE_REGISTRATION_STATUS fromId(int id) {
            for (EDGE_NODE_REGISTRATION_STATUS type : EDGE_NODE_REGISTRATION_STATUS.values()) {
                if (type.ordinal() == id) {
                    return type;
                }
            }
            return null;
        }
    }


    /**
     *     enum for Board Connection Status
     */
    public enum BOARD_CONNECTION_STATUS{
        REGISTRATION_COMPLETE,
        REGISTRATION_FAILED,
        FOUND_NON_SDR_DEVICE,
        DTID_READ,
        WIFI_LIST_RECEIVED,
        WIFI_CONNECTED,
        WIFI_FAILED,
        BLE_DISCONNECTED,
        BLE_CONNECTED,
        INTERNET_UNAVAILABLE,
        REGISTRATION_STARTED;

        public static BOARD_CONNECTION_STATUS fromId(int id) {
            for (BOARD_CONNECTION_STATUS type : BOARD_CONNECTION_STATUS.values()) {
                if (type.ordinal() == id) {
                    return type;
                }
            }
            return null;
        }
    }


    /**
     *     enum for BLE Status Message
     */
    public enum BLE_STATUS_MESSAGES{
        SERVICE_FAILED,
        START_SCANNING,
        STOP_SCANNING,
        DETECTED_DEVICE,
        CONNECT_DEVICE,
        GATT_CONNECTED,
        GATT_DISCONNECTED,
        GATT_FAILED,
        GATT_SERVICES_DISCOVERED,
        STATUS_NOTIFIED,
        LONG_STATUS_NOTIFIED,
        VENDOR_ID_READ,
        DEVICE_ID_READ,
        DISCONNECT_TARGET,
        RESET_TARGET,
        GET_WIFI_LIST,
        WIFI_AP_READ,
        SEND_WIFI_CRED,
        DETECTED_SDR_DEVICE,
        ACTION_DATA_AVAILABLE,
        EXTRA_DATA,
        DEVICE_TYPE_ID_READ,
        DEVICE_CHALLENGE_PIN_READ,
        DEVICE_DID_READ,
        IPADDRESS_READ,
        ERROR_INVALID_MAC_ADDRESS,
        DISCONNECT_GATTSERVICE;


        /**
         *
         * @param id
         * @return
         */
        public static BLE_STATUS_MESSAGES fromId(int id) {
            for (BLE_STATUS_MESSAGES type : BLE_STATUS_MESSAGES.values()) {
                if (type.ordinal() == id) {
                    return type;
                }
            }
            return null;
        }

    }
}

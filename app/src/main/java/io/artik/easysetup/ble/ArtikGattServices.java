package io.artik.easysetup.ble;

import java.util.UUID;

/**
 * Created by 20115642 on 20-12-2016.
 */
public class ArtikGattServices {

    /* Service UUID */
    public static final String SERVICE_UUID_FORMAT = "ffffffff-c0c1-ffff-c0c1-";
    public static final UUID SERVICE_UUID = UUID.fromString("ffffffff-c0c1-ffff-c0c1-201401000000");

    /* Characteristics UUIDs */
    public static final UUID CHARACTERISTIC_STATUS = UUID.fromString("FFFFFFFF-C0C1-FFFF-C0C1-201401000001");
    public static final UUID CHARACTERISTIC_LONG_STATUS = UUID.fromString("FFFFFFFF-C0C1-FFFF-C0C1-201401000002");
    public static final UUID CHARACTERISTIC_SSID = UUID.fromString("FFFFFFFF-C0C1-FFFF-C0C1-201401000003");
    public static final UUID CHARACTERISTIC_AUTH = UUID.fromString("FFFFFFFF-C0C1-FFFF-C0C1-201401000004");
    public static final UUID CHARACTERISTIC_PASS = UUID.fromString("FFFFFFFF-C0C1-FFFF-C0C1-201401000005");
    public static final UUID CHARACTERISTIC_CHANNEL = UUID.fromString("FFFFFFFF-C0C1-FFFF-C0C1-201401000006");
    public static final UUID CHARACTERISTIC_COMMAND = UUID.fromString("FFFFFFFF-C0C1-FFFF-C0C1-201401000007");
    public static final UUID CHARACTERISTIC_VENDOR_ID = UUID.fromString("FFFFFFFF-C0C1-FFFF-C0C1-201401000008");
    public static final UUID CHARACTERISTIC_DEVICE_ID = UUID.fromString("FFFFFFFF-C0C1-FFFF-C0C1-201401000009");
    public static final UUID CHARACTERISTIC_WIFI_STATE = UUID.fromString("FFFFFFFF-C0C1-FFFF-C0C1-201401000010");
    public static final UUID CHARACTERISTIC_IPADDRESS = UUID.fromString("FFFFFFFF-C0C1-FFFF-C0C1-201401000011");
    public static final UUID CHARACTERISTIC_WIFIAP = UUID.fromString("FFFFFFFF-C0C1-FFFF-C0C1-201401000012");
    public static final UUID CHARACTERISTIC_CLOUD_DEVICE_ID = UUID.fromString("FFFFFFFF-C0C1-FFFF-C0C1-201401000013");
    public static final UUID CHARACTERISTIC_CLOUD_DEVICE_TOKEN = UUID.fromString("FFFFFFFF-C0C1-FFFF-C0C1-201401000014");

    public static final UUID CHALLENGE_PIN_UUID = UUID.fromString("0000FFF1-0000-1000-8000-00805F9B34FB");
    public static final UUID DEVICE_TOKEN_UUID = UUID.fromString("0000FFF2-0000-1000-8000-00805F9B34FB");
    public static final UUID DEVICE_TYPE_ID_UUID = UUID.fromString("0000FFF3-0000-1000-8000-00805F9B34FB");
    public static final UUID START_REGISTRATION_UUID = UUID.fromString("0000FFF5-0000-1000-8000-00805F9B34FB");
    public static final UUID COMPLETE_REGISTRATION_UUID = UUID.fromString("0000FFF6-0000-1000-8000-00805F9B34FB");
    public static final UUID DEVICE_ID_CHARACTERISTIC_UUID = UUID.fromString("0000FFF7-0000-1000-8000-00805F9B34FB");
    public static final UUID UID_CHARACTERISTIC_UUID = UUID.fromString("0000FFF8-0000-1000-8000-00805F9B34FB");
    public static final UUID SDR_REGISTRATION_UUID = UUID.fromString("0000FFF0-0000-1000-8000-00805F9B34FB");
    public static final String SDR_SERVICE = "0000FFF0-0000-1000-8000-00805F9B34FB";


    /* Documentation of Status Codes
       DISCONNECTED =  "1"
       INITIALIZING = "2"
       CONNECTING   = "3"
       CONNECTED    = "4"
       FAILED       = "5"
       RECVD_PIN    = "6"
       RECVD_TOKEN  = "7"
    *
    * */
    public static final String STATUS_DISCONNECTED = "1";
    public static final String STATUS_INITIALIZING = "2";
    public static final String STATUS_CONNECTING = "3";
    public static final String STATUS_CONNECTED = "4";
    public static final String STATUS_FAILED = "5";
    public static final String STATUS_RECVD_PIN = "6";
    public static final String STATUS_RECVD_TOKEN = "7";

    /*
        Detailed Status

    NONE                        = "51" // "NONE"
    SET_SSID                    = "52" // "Set SSID"
    SET_AUTH                    = "53" // "Set auth type"
    WRONG_AUTH                  = "54" // "Wrong auth. type"
    SET_PSK                     = "55" // "Set PSK"
    SET_CHAN                    = "56" // "Conn. or set channel"
    AUTHING                     = "57" // "Authenticating"
    GETTING_IP                  = "58" // "Getting IP address"
    CONN_ESTAB                  = "59" // "Connection completed"
    PSK_NOT_REQ                 = "60" // "No PSK required"
    RECVD_CHALLENGE             = "61" // "Received Challenge"
    RECVD_DEVICE_TOKEN          = "62" // "Received Token"
    START_REGISTRATION_ERROR    = "63" // "Registeration Error"
    COMPLETE_REGISTRATION_ERROR = "64" // "Invalid Token"
    INVALID_SSID                = "65" // "Invalid SSID"
    WRONG_PASSWORD              = "66" // "Invalid Password"
    NO_IPADDRESS                = "67" // "Unable to get Ip"
    INTERNET_UNAVAILABLE        = "68" // "No Internet"
    INVALID_WIFI_STATE          = "69" // "Invalid wifi module state"
    SCAN_FINISH                 = "70" // "Wifi Scan Finish"
            )
      */

    public static final String LONG_STATUS_NONE = "51";
    public static final String LONG_STATUS_SET_SSID = "52";
    public static final String LONG_STATUS_SET_AUTH = "53";
    public static final String LONG_STATUS_WRONG_AUTH = "54";
    public static final String LONG_STATUS_SET_PSK = "55";
    public static final String LONG_STATUS_SET_CHAN = "56";
    public static final String LONG_STATUS_AUTHING = "57";
    public static final String LONG_STATUS_GETTING_IP = "58";
    public static final String LONG_STATUS_CONN_ESTAB = "59";
    public static final String LONG_STATUS_PSK_NOT_REQ = "60";
    public static final String LONG_STATUS_RECVD_CHALLENGE = "61";
    public static final String LONG_STATUS_RECVD_DEVICE_TOKEN = "62";
    public static final String LONG_STATUS_START_REGISTRATION_ERROR = "63";
    public static final String LONG_STATUS_COMPLETE_REGISTRATION_ERROR = "64";
    public static final String LONG_STATUS_INVALID_SSID = "65";
    public static final String LONG_STATUS_WRONG_PASSWORD = "66";
    public static final String LONG_STATUS_NO_IPADDRESS = "67";
    public static final String LONG_STATUS_INTERNET_UNAVAILABLE = "68";
    public static final String LONG_STATUS_INVALID_WIFI_STATE = "69";
    public static final String LONG_STATUS_SCAN_FINISH = "70";
}

/**
 * ConnectionDetector.java My Aquarium Created by L&T TS on
 * 08/08/2015. � 2015 Spectrum Brands - Pet, Home and Garden Division.
 * All rights reserved.
 */
package io.artik.easysetup.util;

/**
 * This class helps to check Internet connectivity.
 */
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;

public class ConnectionDetector {

	private Context _context;

	public ConnectionDetector(Context context) {
		this._context = context;
	}

	/**
	 * To check if the app is connected to internet
	 * @param mContext
	 * @return true/false
     */
	public static boolean isConnectingToInternet(final Context mContext) {
		boolean isConnected = false;
		ConnectivityManager connectivity =
				(ConnectivityManager) mContext
						.getSystemService(Context.CONNECTIVITY_SERVICE);
		if (connectivity != null) {
			NetworkInfo info = connectivity.getActiveNetworkInfo();
			if (info != null) {
					if (info.isAvailable() && info.isConnected()){
						isConnected = true;
					}
			}
		}
		return isConnected;
	}


	/**
	 * To check if the Wifi is Enabled
	 * @param mContext
	 * @return true/false
     */
	public static boolean isWifiEnabled(final Context mContext) {

		WifiManager wifi = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);

		return wifi.isWifiEnabled();
	}

}
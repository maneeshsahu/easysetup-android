package io.artik.easysetup.api.client;

import android.content.Context;
import android.util.Log;

import com.google.gson.JsonObject;

import java.net.Socket;
import java.util.ArrayList;

import io.artik.easysetup.api.SoftAPApiClient;
import io.artik.easysetup.api.SoftAPConnectionAPI;
import io.artik.easysetup.api.model.AccessPointInfo;
import io.artik.easysetup.api.model.GetAPListBaseResponse;
import io.artik.easysetup.api.model.SoftAPGenericResponse;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

/**
 * Controller for handling connections with Soft AP Clients on boards like 05X onboarding
 * Created by vsingh on 06/03/17.
 */

public class SoftAPCallHandling {

    private static final String TAG = "SoftAPCallHandling";
    private Context context;
    private String BASE_URL = "https://ipaddress/v1.0/";
    private String BASE_URL_HTTP = "http://ipaddress/v1.0/";
    public String URL;
    public Retrofit retrofit;
    public SoftAPConnectionAPI connectionAPI;
    private String deviceIp;
    public boolean isHTTPS = true;


    private final SoftAPListener mListener;

    public interface  SoftAPListener{
        void onAPListResponse(ArrayList<AccessPointInfo> aps);
        void onAPConfigured(boolean success);
        void onAPProvisionDevice(boolean success);
        void onNodeDiscovered(String ip);
        void onAPError(String reason, int code);
    }


    public SoftAPCallHandling(Context context, String ipaddress, SoftAPListener listener) {

        this.context = context;
        mListener = listener;

        deviceIp = ipaddress;
        URL = getURL(ipaddress);
        retrofit = SoftAPApiClient.getApiClient(URL);
        connectionAPI = retrofit.create(SoftAPConnectionAPI.class);

    }

    /**
     * For backward compatibility, we leave a fallback on http instead of https
     *
     */
    public void checkHTTPS() {

        Socket s = null;
        try
        {
            s = new Socket(deviceIp, 443);
        }
        catch (Exception e)
        {
            isHTTPS = false;
            e.printStackTrace();
        }
        finally
        {
            if(s != null)
                try {s.close();}
                catch(Exception e){}
        }

        if (!isHTTPS) {
            URL = getHTTPURL(deviceIp);
            retrofit = SoftAPApiClient.getApiClient(URL);
            connectionAPI = retrofit.create(SoftAPConnectionAPI.class);
        }

    }


    /**
     * Get the Access Point List from the device
     */
    public void fetchAPList() {

        new Thread(new Runnable() {
            public void run() {
                checkHTTPS();
                Call<GetAPListBaseResponse> call = connectionAPI.getAPList();
                call.enqueue(new Callback<GetAPListBaseResponse>() {
                    @Override
                    public void onResponse(Call<GetAPListBaseResponse> call, Response<GetAPListBaseResponse> response) {
                        GetAPListBaseResponse apBaseResponse = response.body();

                        if (apBaseResponse == null) {
                            Log.i("SoftAPCallHandling", "No AP List ");
                            return;
                        }
                        ArrayList<AccessPointInfo> apList = apBaseResponse.getAccessPointInfoArrayList();
                        Log.i("SoftAPCallHandling", "Got AP List " + apList.size());
                        if (mListener != null) {
                            mListener.onAPListResponse(apList);
                        }
                    }

                    @Override
                    public void onFailure(Call<GetAPListBaseResponse> call, Throwable t) {

                        Log.e("SoftAPCallHandling", "Unable to get AP List " + t);
                        //initConnection();
                        if (mListener != null) {
                            mListener.onAPListResponse(null);
                        }

                    }
                });
            }
        }).start();

    }

    /**
     * Configure the device with Wifi Credentials
     * @param ssid
     * @param password
     */
    public void passAPConfiguration(String ssid, String password) {
        JsonObject config = new JsonObject();

        config.addProperty("ssid", ssid);
        if (password != null) {
            config.addProperty("passphrase", password);
            config.addProperty("security", "Secure");
        } else {
            config.addProperty("security", "Open");
        }
        config.addProperty("connect", true);

        Call<SoftAPGenericResponse> call = connectionAPI.configureWifi(config);
        call.enqueue(new Callback<SoftAPGenericResponse>() {
            @Override
            public void onResponse(Call<SoftAPGenericResponse> call, Response<SoftAPGenericResponse> response) {

                if (mListener != null) {
                    mListener.onAPConfigured(true);
                }
            }

            @Override
            public void onFailure(Call<SoftAPGenericResponse> call, Throwable t) {
                /* Due to socket issues on 05x, some responses get truncated  */
                mListener.onAPConfigured(true);
            }
        });
    }

    private String getURL( String address ) {
        return BASE_URL.replace("ipaddress", address);
    }

    private String getHTTPURL( String address ) {
        return BASE_URL_HTTP.replace("ipaddress", address);
    }

}

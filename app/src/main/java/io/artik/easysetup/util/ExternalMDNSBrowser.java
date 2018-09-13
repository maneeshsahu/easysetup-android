package io.artik.easysetup.util;

import android.content.Context;
import android.util.Log;

import com.github.druk.rxdnssd.BonjourService;
import com.github.druk.rxdnssd.RxDnssd;
import com.github.druk.rxdnssd.RxDnssdBindable;

import java.util.ArrayList;
import java.util.List;

import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

/**
 * Created by vsingh on 02/11/2017.
 */

public class ExternalMDNSBrowser {

    RxDnssd rxdnssd;
    private static final String TAG = "ExternalMDNSBrowser";
    private Subscription browseSubscription = null;
    private final HubListener mListener;
    public ArrayList<String> Hubs = new ArrayList<>();
    private List<BonjourService> services = new ArrayList<>();


    public interface  HubListener{
        void onHubFound(String hubInfo);
    }

    public ExternalMDNSBrowser(Context context, ExternalMDNSBrowser.HubListener listener) {
        rxdnssd = new RxDnssdBindable(context);
        mListener = listener;
    }

    public void discoverServices() {
        stopDiscovery();  // Cancel any existing discovery request
        Hubs.clear();
        services.clear();

        browseSubscription = rxdnssd.browse("_enm._tcp", "local.")
                .compose(rxdnssd.resolve())
                .compose(rxdnssd.queryRecords())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<BonjourService>() {
                    @Override
                    public void call(BonjourService bonjourService) {
                        Log.d("TAG", bonjourService.toString());

                        synchronized (this) {
                            if (bonjourService.isLost()) {
                                services.remove(bonjourService);
                            } else {
                                services.add(bonjourService);
                                if (bonjourService.getInet4Address() == null) {
                                    Log.e("TAG", "Unable to get IPv4 Address");
                                    return;
                                }
                                String hubInfo = bonjourService.getInet4Address().getHostAddress() + "," + bonjourService.getTxtRecords().get("data");
                                Boolean found = false;
                                /* Sometimes there are duplicates */
                                for (String hub : Hubs) {
                                    if (hub.contentEquals(hubInfo)) {
                                        found = true;
                                    }
                                }
                                if (!found) {
                                    Hubs.add(hubInfo);
                                    Log.d(TAG, "Found a Hub " + hubInfo);
                                    mListener.onHubFound(hubInfo);
                                }
                            }
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        Log.e("TAG", "error", throwable);
                    }
                });

    }

    public void stopDiscovery() {
        if (browseSubscription != null) {
            browseSubscription.unsubscribe();
            browseSubscription = null;
        }
    }
}

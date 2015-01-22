package com.bernd.buddytracker;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.net.wifi.p2p.WifiP2pManager;

public class WifiStateChangedReceiver extends BroadcastReceiver {

    private static final String TAG = ConnectActivity.class.getSimpleName();

    //Konstanten für dynamischen Intentfilter in ConnectActivity
    public static final String INTENT_FILTER_SHOW_ALERTDIALOG = "SHOW_ALERTDIALOG";
    public static final String INTENT_FILTER_DISMISS_ALERTDIALOG = "DISMISS_ALERTDIALOG";

    public WifiStateChangedReceiver() {}

    @Override
    public void onReceive(Context context, Intent intent) {

        String action = intent.getAction();
        if(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)){
            int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);
            if(state == WifiP2pManager.WIFI_P2P_STATE_ENABLED){
                // P2P enabled
            } else{
                // P2P disabled
            }
        }

        //WLAN Statusänderungen sollen nur weitergereicht werden, wenn ConnectActivity sichtbar ist
        if (ConnectActivity.active) {
            //Empfangener Intent enthält Wifistatus --> herausholen mit getIntExtra()
            int state = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, -1);

            if ((state == WifiManager.WIFI_STATE_ENABLED) || (state == WifiManager.WIFI_STATE_ENABLING)) {
                //WIFI an
                //TODO Intent explicit machen
                context.sendBroadcast(new Intent(INTENT_FILTER_DISMISS_ALERTDIALOG));
            } else {
                //WIFI aus
                //TODO Intent explicit machen
                context.sendBroadcast(new Intent(INTENT_FILTER_SHOW_ALERTDIALOG));
            }
        }

    }
}

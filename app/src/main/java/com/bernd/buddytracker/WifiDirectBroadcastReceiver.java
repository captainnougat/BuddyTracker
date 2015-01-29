package com.bernd.buddytracker;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.widget.Toast;

/**
 * Broadcastreceiver für die Connectactivity
 *
 * Created by Tobias on 22.01.2015.
 */
public class WifiDirectBroadcastReceiver extends BroadcastReceiver {
    private static final String TAG = WifiDirectBroadcastReceiver.class.getSimpleName();

    //schaltet Debugausgaben ein und aus
    private boolean DEBUG = true;

    private ConnectActivity mActivity;

    public WifiDirectBroadcastReceiver(WifiP2pManager manager, WifiP2pManager.Channel channel, ConnectActivity activity) {
        this.mActivity = activity;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();


        switch (action) {
            //----WIFI Statusänderungen----
            case WifiManager.WIFI_STATE_CHANGED_ACTION:
                int state = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, -1);

                //if (mActivity.active) {
                if ((state == WifiManager.WIFI_STATE_ENABLING)) {
                    // WIFI wird gerade eingeschalten
                    mActivity.getWifiAlertDialog().dismiss();
                    mActivity.getWifiEnablingProgressDialog().show();
                } else if (state != WifiManager.WIFI_STATE_ENABLED) {
                    // WIFI ist aus
                    mActivity.getWifiAlertDialog().show();
                }
                //}
                break;
            case WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION:
                //WIFI ist an
                int p2pState = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);
                if ((p2pState == WifiP2pManager.WIFI_P2P_STATE_ENABLED)) {
                    mActivity.getWifiEnablingProgressDialog().dismiss();
                }
                break;

            //-----WIFI Direct----
            case WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION:
                // Call WifiP2pManager.requestPeers() to get a list of current peers

                // request available peers from the wifi p2p manager. This is an
                // asynchronous call and the calling activity is notified with a
                // callback on PeerListListener.onPeersAvailable()
                WifiP2pDeviceList deviceList = intent.getParcelableExtra(WifiP2pManager.EXTRA_P2P_DEVICE_LIST);
                WifiP2pDeviceAdapter adapter = (WifiP2pDeviceAdapter) mActivity.getPeerListView().getAdapter();

                boolean updated = false;
                for (WifiP2pDevice dev : deviceList.getDeviceList()) {
                    if (adapter.updateDevice(dev)) {
                        updated = true;
                         if (dev.status==WifiP2pDevice.CONNECTED){
                             BuddyManager.getInstance().addConnectedBuddy(dev);
                         }
                    }
                }

                if (updated)
                    adapter.notifyDataSetChanged();

                break;
            case WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION:
                // Respond to new connection or disconnections
                WifiP2pInfo info = intent.getParcelableExtra(WifiP2pManager.EXTRA_WIFI_P2P_INFO);
                if (info.groupFormed) {
                    Toast.makeText(mActivity, "Verbindung hergestellt", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(mActivity, "Verbindung getrennt", Toast.LENGTH_SHORT).show();
                    //TODO im BuddyManager austragen
                }

                break;
            case WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION:
                // Respond to this device's wifi state changing
                //muss man hier etwas regeln?
                break;
        }
    }


}
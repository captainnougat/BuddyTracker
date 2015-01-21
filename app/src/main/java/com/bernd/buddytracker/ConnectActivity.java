package com.bernd.buddytracker;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.app.ActionBarActivity;
import android.view.KeyEvent;

public class ConnectActivity extends ActionBarActivity {
    private static final String TAG = ConnectActivity.class.getSimpleName();
    //FALSE, wenn Activity nicht sichtbar, TRUE wenn Activity läuft
    public static boolean active = false;
    private AlertDialog wifiAlertDialog;

    //empfängt Ereignisse, wenn WLAN State sich ändert. Intent enthält Aufforderung Alertdialog zu zeigen oder zu verstecken
    BroadcastReceiver wifiStateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            //nur wenn diese activity sicher bar ist, sollen aktionen mit dem Dialog durchgeführt werden
            if (active) {
                //zeige Alertdialog
                if (intent.getAction().equals(WifiStateChangedReceiver.INTENT_FILTER_SHOW_ALERTDIALOG) && !wifiAlertDialog.isShowing()) {
                    wifiAlertDialog.show();
                }
                //entferne Alertdialog
                if (intent.getAction().equals(WifiStateChangedReceiver.INTENT_FILTER_DISMISS_ALERTDIALOG) && wifiAlertDialog.isShowing()) {
                    wifiAlertDialog.dismiss();
                }
            }
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connect);

        //Dialog bauen
        wifiAlertDialog = createWifiAlertDialog();

        //dynamischen Intentfilter bauen
        IntentFilter filter = new IntentFilter();
        filter.addAction(WifiStateChangedReceiver.INTENT_FILTER_SHOW_ALERTDIALOG);
        filter.addAction(WifiStateChangedReceiver.INTENT_FILTER_DISMISS_ALERTDIALOG);

        //receiver mit gebautem Filter dynamisch registrieren
        registerReceiver(wifiStateReceiver, filter);
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume(){
        super.onResume();
        active = true;
        //WLAN aus, also Alertdialog zeigen
        if(!wifiIsEnabled(this))
            wifiAlertDialog.show();
    }

    @Override
    public void onPause() {
        super.onPause();
        //TODO active = false in onResume reicht, oder?
        active = false;
    }

    @Override
    public void onStop() {
        super.onStop();
        active = false;
    }

    @Override
    public void onDestroy() {
        super.onStop();
        active = false;

        //nicht vergessen den dynamisch registrieren Broadcastreceiver wieder zu entfernen
        unregisterReceiver(wifiStateReceiver);
    }

    /**
     * Baut den Alertdialog, der einen zwingen soll WIFI einzuschalten
     *
     * @return liefert den fertigen Alertdialog zurück
     */
    private AlertDialog createWifiAlertDialog(){
        AlertDialog.Builder alertDiaBuilder = new AlertDialog.Builder(this);

        alertDiaBuilder.setTitle("Wifi muss eingeschaltet sein");
        alertDiaBuilder.setMessage("Zu Einstellungen wechseln?");
        alertDiaBuilder.setCancelable(false);

        alertDiaBuilder.setPositiveButton("ja",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // WLAN einstellungen öffnen
                        startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
                    }
                });

        alertDiaBuilder.setNegativeButton("nein",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                        finish();
                    }
                });

        alertDiaBuilder.setOnKeyListener(new AlertDialog.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface arg0, int keyCode,
                                 KeyEvent event) {
                //Wenn zurück Button gedrückt, beende Activity
                if (keyCode == KeyEvent.KEYCODE_BACK) {
                    finish();
                }
                return true;
            }
        });
        return alertDiaBuilder.create();
    }

    /**
     * Gibt true zurück, wenn WIFI an ist
     *
     * @return Boolean
     */
    public boolean wifiIsEnabled(Context con){
        WifiManager wifiMgr = (WifiManager) con.getSystemService(Context.WIFI_SERVICE);
        int state = wifiMgr.getWifiState();

        return (state == WifiManager.WIFI_STATE_ENABLED) || (state == WifiManager.WIFI_STATE_ENABLING);
    }
}

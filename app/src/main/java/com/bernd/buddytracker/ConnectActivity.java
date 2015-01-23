package com.bernd.buddytracker;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiManager;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.app.ActionBarActivity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

public class ConnectActivity extends ActionBarActivity {
    private static final String TAG = ConnectActivity.class.getSimpleName();
    public AlertDialog wifiAlertDialog;
    //wifiEnablingProgressDialog hat nur den Zweck ein Feedback zu geben, dass WIFI Status=Enabling, da ansonsten noch immer angezeigt wird, dass WLAN aus ist
    public ProgressDialog wifiEnablingProgressDialog;
    public ListView peerListView;

    //WIFI Direct
    WifiP2pManager mManager;
    WifiP2pManager.Channel mChannel;
    WifiDirectBroadcastReceiver wifiDirectReceiver;

    IntentFilter wifiDirectFilter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connect);

        //Dialoge bauen
        wifiAlertDialog = createWifiAlertDialog();
        wifiEnablingProgressDialog = createProgressDialog();

        //Filter für WifiDirect Receiver
        wifiDirectFilter = new IntentFilter();
        wifiDirectFilter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        wifiDirectFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        wifiDirectFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        wifiDirectFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        wifiDirectFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);

        //WIFI Direct setup
        mManager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        mChannel = mManager.initialize(this, getMainLooper(), null);
        wifiDirectReceiver = new WifiDirectBroadcastReceiver(mManager, mChannel, this);

        //ListView und Adapter verbinden
        peerListView = (ListView) findViewById(R.id.listView_peerlist);
        peerListView.setAdapter(new WifiP2pDeviceAdapter(this));
        peerListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,int position, long id) {
                WifiP2pDevice dev = (WifiP2pDevice) peerListView.getItemAtPosition(position);
                //TODO verbindung implementieren
                Toast.makeText(ConnectActivity.this,"Du hast auf " + dev.deviceName + " getippt",Toast.LENGTH_SHORT).show();
            }
        });

        Button btn_scan = (Button) findViewById(R.id.btn_scan);
        btn_scan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //The discovery remains active until a connection is initiated or a p2p group is formed --> muss man also nur ein mal aufrufen
                mManager.discoverPeers(mChannel, new WifiP2pManager.ActionListener() {
                    @Override
                    public void onSuccess() {
                          //TODO Rückmeldung unnötig?
//                        Toast toast = Toast.makeText(getApplicationContext(), "Scan erfolgreich" ,Toast.LENGTH_LONG);
//                        toast.show();
                    }

                    @Override
                    public void onFailure(int reasonCode) {
                        Toast toast = Toast.makeText(getApplicationContext(), "Scan schlug fehl" ,Toast.LENGTH_LONG);
                        toast.show();
                    }
                });
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkWifiStatus();
        //WifiDirectReceiver registrieren
        registerReceiver(wifiDirectReceiver, wifiDirectFilter);
    }

    @Override
    public void onPause() {
        super.onPause();
        //WifiDirectReceiver entfernen
        unregisterReceiver(wifiDirectReceiver);
        wifiAlertDialog.dismiss();
        wifiEnablingProgressDialog.dismiss();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onDestroy() {
        super.onStop();
    }

    /**
     * Baut den Alertdialog, der einen zwingen soll WIFI einzuschalten
     *
     * @return liefert den fertigen Alertdialog zurück
     */
    private AlertDialog createWifiAlertDialog() {
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
     * Baut den Progressdioalog, der angezeigt wird, solange WLAN P2P noch nicht verfügbar ist und WIFI sich in Modul ENABLING befindet
     *
     * @return liefert den fertigen Progressdialog zurück
     */
    private ProgressDialog createProgressDialog() {
        ProgressDialog progress = new ProgressDialog(this);
        progress.setCancelable(false);
        progress.setTitle("Loading");
        progress.setMessage("WLAN DIRECT wird aktiviert...");
        progress.setOnKeyListener(new AlertDialog.OnKeyListener() {
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
        return progress;
    }

    /**
     * Checkt nach onResume den WIFI status und öffnet entsprechende Dialoge
     *
     */
    public void checkWifiStatus() {
        WifiManager wifiMgr = (WifiManager) this.getSystemService(Context.WIFI_SERVICE);
        int state = wifiMgr.getWifiState();

        if ((state == WifiManager.WIFI_STATE_ENABLING)) {
            // WIFI wird gerade eingeschalten
            wifiEnablingProgressDialog.show();
        } else if (state != WifiManager.WIFI_STATE_ENABLED) {
            // WIFI ist aus
            wifiAlertDialog.show();
        }
    }
}

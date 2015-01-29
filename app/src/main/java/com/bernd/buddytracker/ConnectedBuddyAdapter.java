package com.bernd.buddytracker;

import android.content.Context;
import android.net.wifi.p2p.WifiP2pDevice;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Created by Bereza on 29.01.2015.
 */
public class ConnectedBuddyAdapter extends BaseAdapter{
    //der LayoutInflater entfaltet die XML Beschreibung der einzelnen Listen Items
    private final LayoutInflater inflator;

    public ConnectedBuddyAdapter(Context context){
        // wird für das aufblasen der XML Datei benötigt
        inflator = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return BuddyManager.getInstance().getBuddyCount();
    }

    @Override
    public Object getItem(int position) {return BuddyManager.getInstance().getConnectedBuddy(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            //Layoutdatei entfalten
            convertView = inflator.inflate(R.layout.item_peer_text_only, parent, false);

            //Holder erzeugen(kommentierte Klasse weiter unten im code)
            holder = new ViewHolder();
            holder.icon = (ImageView) convertView.findViewById(R.id.item_cB_icon);
            holder.nickName = (TextView) convertView.findViewById(R.id.item_cB_nick);
            holder.status = (TextView) convertView.findViewById(R.id.item_cB_status);
            convertView.setTag(holder);
        }else{
            // Holder bereits vorhanden
            holder = (ViewHolder) convertView.getTag();
        }

        //View mit Informationen füllen
        BuddyManager.ConnectedBuddy buddy = (BuddyManager.ConnectedBuddy) getItem(position);
        holder.nickName.setText(buddy.getNickName());

        String status;
        switch (buddy.getDev().status){
            case (WifiP2pDevice.AVAILABLE):
                status = "verfügbar";
                break;
            case (WifiP2pDevice.CONNECTED):
                status = "verbunden";
                break;
            case (WifiP2pDevice.FAILED):
                status = "fehlgeschlagen";
                break;
            case (WifiP2pDevice.INVITED):
                status = "eingeladen";
                break;
            case (WifiP2pDevice.UNAVAILABLE):
                status = "nicht verfügbar";
                break;
            default:
                status="Fehler";

        }
        holder.status.setText("Status: " + status);
        holder.icon.setImageDrawable(buddy.getProfilePic());
        return convertView;
    }

    static class ViewHolder{
        ImageView icon;
        TextView nickName,status;
    }
}

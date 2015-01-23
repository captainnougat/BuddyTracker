package com.bernd.buddytracker;

import android.content.Context;
import android.net.wifi.p2p.WifiP2pDevice;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Adapter um mit discoverPeers() gefundene Peers in der {@link com.bernd.buddytracker.ConnectActivity} richtig anzuzeigen
 *
 * Created by Alex on 22.01.2015.
 */
public class WifiP2pDeviceAdapter extends BaseAdapter {
    //Liste der gefundenen Devices
    private ArrayList<WifiP2pDevice> devices = new ArrayList<WifiP2pDevice>();

    //der LayoutInflater entfaltet die XML Beschreibung der einzelnen Listen Items
    private final LayoutInflater inflator;

    /**
     * Aktualisiert Liste mit den Devices
     *
     * @param collection        neue Devices
     */
    public void updateDeviceList(Collection<WifiP2pDevice> collection){
        devices.clear();
        devices.addAll(collection);
    }

    public WifiP2pDeviceAdapter(Context context){
        // wird für das aufblasen der XML Datei benötigt
        inflator = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return devices.size();
    }

    @Override
    public Object getItem(int position) {
        return devices.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }


    //Die Methode getView() baut aus den Daten des Modells einen Eintrag zusammen und stellt ihn in Gestalt einer View-Instanz zur Verfügung.
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            //Layoutdatei entfalten
            convertView = inflator.inflate(R.layout.item_peer_text_only, parent, false);

            //Holder erzeugen(kommentierte Klasse weiter unten im code)
            holder = new ViewHolder();
            holder.name = (TextView) convertView.findViewById(R.id.item_peer_name);
            holder.macAdress = (TextView) convertView.findViewById(R.id.item_peer_mac);
            holder.type = (TextView) convertView.findViewById(R.id.item_peer_type);

            convertView.setTag(holder);
        }else{
            // Holder bereits vorhanden
            holder = (ViewHolder) convertView.getTag();
        }

        //View mit Informationen füllen
        WifiP2pDevice device = (WifiP2pDevice) getItem(position);
        holder.name.setText(device.deviceName);
        holder.macAdress.setText("MAC: "+ device.deviceAddress);
        holder.type.setText("Typ: " + device.primaryDeviceType);

        return convertView;
    }

    /**
     * Aus Effizienzgründen puffert Android eine gewisse Menge an View Objekten. Im Falle
     * der erstmaligen Verwendung ist der Parameter convertView der Methode getView gleich null. Dann wird
     * mithilfe eines LayoutInflator aus einer XML-Datei (icon_text_text.xml) ein entsprechender
     * Komponentenbaum erzeugt und der Variable convertView zugewiesen.
     * Anschließend wird deren Methode setTag() ein sogenannter ViewHolder übergeben.
     * Er fungiert als eine Art Platzhalter, um später einfach und effizient an die Elemente
     * des Baums zu gelangen. Der erneute Aufruf der Methode findViewById() wäre
     * wesentlich kostspieliger.
     *
     */
    static class ViewHolder{
        TextView name, macAdress, type;
    }
}

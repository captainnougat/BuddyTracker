package com.bernd.buddytracker;

import android.content.Context;
import android.net.wifi.p2p.WifiP2pDevice;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Adapter um mit discoverPeers() gefundene Peers in der {@link com.bernd.buddytracker.ConnectActivity} richtig anzuzeigen
 *
 * Created by Alex on 22.01.2015.
 */
public class WifiP2pDeviceAdapter extends BaseAdapter {
    //Liste der gefundenen Devices(mit Nicknamen)
    private ArrayList<AvailableBuddy> availableBuddies = new ArrayList<>();

    //der LayoutInflater entfaltet die XML Beschreibung der einzelnen Listen Items
    private final LayoutInflater inflator;

    public WifiP2pDeviceAdapter(Context context){
        // wird für das aufblasen der XML Datei benötigt
        inflator = LayoutInflater.from(context);
    }

    /**
     * Verfügbarer Buddy wurde gefunden und wird der Liste hinzugefügt, wenn er nicht bereits enthalten ist
     *
     * @param newDev        WifiP2pDevice des Buddy
     * @param nickname      Nickname des Buddy
     */
    public void addAvailableBuddy(WifiP2pDevice newDev, String nickname){
        AvailableBuddy buddy = new AvailableBuddy(newDev,nickname);
        if(!availableBuddies.contains(buddy)){
            availableBuddies.add(buddy);
        }
    }

    /**
     * Updatet die Informationen eines Devices, wenn es zu einem Buddy gehört
     *
     * @param dev       Das aktualisierte Device
     * @return          gibt True zurück, wenn BuddyInfos geändert wurden
     */
    public boolean updateDevice(WifiP2pDevice dev){
        AvailableBuddy updatedBuddy = new AvailableBuddy(dev, null);

        int index = availableBuddies.indexOf(updatedBuddy);
        if (index > -1) {
            //Liste enthält Buddy
            AvailableBuddy oldBuddy = availableBuddies.get(index);
            oldBuddy.setMyDev(dev);
            return true;
        } else {
            return false;
        }
    }

    /**
     * Gibt zu einem Gerät gehörigen Buddy zurück, wenn er in der Liste enthalten ist
     *
     * @param dev   gerät des Buddy
     * @return      AvailableBuddy der zu dem Device gehört, oder NULL, wenn das Device in der Available Buddy Liste nicht vorkommt
     */
    public AvailableBuddy getBuddy(WifiP2pDevice dev){
        AvailableBuddy searchBuddy = new AvailableBuddy(dev, null);
        int index = availableBuddies.indexOf(searchBuddy);

        if (index > -1)
            //Liste enthält Buddy
            return availableBuddies.get(index);

        return null;
    }

    /**
     * Entfernt Device aus der Liste
     *
     * @param dev       device, dass entfernt werden soll
     * @return          TRUE, wenn ein Device entfernt wurde
     */
    public boolean removeDevice(WifiP2pDevice dev){
        AvailableBuddy updatedBuddy = new AvailableBuddy(dev, null);
        return availableBuddies.remove(updatedBuddy);
    }

    @Override
    public int getCount() {
        return availableBuddies.size();
    }

    @Override
    public Object getItem(int position) {return availableBuddies.get(position);
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
            holder.nickName = (TextView) convertView.findViewById(R.id.item_peer_nickname);
            holder.phoneName = (TextView) convertView.findViewById(R.id.item_peer_phonename);
            holder.macAdress = (TextView) convertView.findViewById(R.id.item_peer_mac);
            holder.status = (TextView) convertView.findViewById(R.id.item_peer_status);

            convertView.setTag(holder);
        }else{
            // Holder bereits vorhanden
            holder = (ViewHolder) convertView.getTag();
        }

        //View mit Informationen füllen
        AvailableBuddy buddy = (AvailableBuddy) getItem(position);
        holder.nickName.setText(buddy.getNickname());
        holder.phoneName.setText("(" + buddy.getMyDev().deviceName + ")");
        holder.macAdress.setText("MAC: "+ buddy.getMyDev().deviceAddress);

        String status;
        switch (buddy.getMyDev().status){
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
        TextView phoneName, nickName, macAdress, status;
    }


    /**
     * Innere Klasse, die einen entdeckten, aber noch nciht verbundenen Buddy mit seinem Nicknamen repräsentiert
     */
    public static class AvailableBuddy{
        private String Nickname;
        private WifiP2pDevice myDev;

        /**
         *
         *
         * @param  dev          WifiP2pDevice des Buddy
         * @param Nickname      Nickanme des Buddy
         */
        public AvailableBuddy (WifiP2pDevice dev, String Nickname) {
            this.Nickname=Nickname;
            this.myDev=dev;
        }

        @Override
        public boolean equals(Object obj) {
            /*if (obj.getClass().equals(WifiP2pDevice.class)){
                WifiP2pDevice dev = (WifiP2pDevice) obj;
                return dev.deviceAddress.equals(myDev.deviceAddress);
            }else*/ if (obj.getClass().equals(AvailableBuddy.class)) {
                AvailableBuddy buddy = (AvailableBuddy) obj;
                return buddy.getMyDev().deviceAddress.equals(myDev.deviceAddress);
            }else{
                return false;
            }
        }

        public void setMyDev(WifiP2pDevice myDev) {
            this.myDev = myDev;
        }

        public String getNickname() {
            return Nickname;
        }

        public WifiP2pDevice getMyDev() {
            return myDev;
        }
    }
}

package com.bernd.buddytracker;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.wifi.p2p.WifiP2pDevice;
import android.os.AsyncTask;
import android.util.Log;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

/**
 * Created by Alex on 27.01.2015.
 */
public class BuddyManager {
    private static final String TAG = BuddyManager.class.getSimpleName();
    private static int PORT = 8888;
    private static String CONNECTED_BUDDIES_CHANGED_ACTION = "connectedBuddyListChanged";
    private Context context;


    //TODO persistent machen
    private ArrayList<ConnectedBuddy> buddyList = new ArrayList<>();

    private static BuddyManager instance;

    private BuddyManager() {
        instance = new BuddyManager();
    }

    public static BuddyManager getInstance() {
        return instance;
    }

    public void setContext(Context con){
        this.context =con;
    }

    public int getBuddyCount(){
        return buddyList.size();
    }

    public void addConnectedBuddy(WifiP2pDevice dev) {
        ConnectedBuddy connectedBuddy = new ConnectedBuddy(dev);
        if (!buddyList.contains(connectedBuddy)){
            buddyList.add(connectedBuddy);
            new ProfilePicServerAsyncTask(context,dev).execute();
            new ProfilePicClientAsyncTask(dev).execute();
        }
    }

    public ConnectedBuddy getConnectedBuddy(WifiP2pDevice dev){
        int index = buddyList.indexOf(dev);
        if (index > -1){
            return buddyList.get(index);
        }
        return null;
    }

    public ConnectedBuddy getConnectedBuddy(int position){
        return buddyList.get(position);
    }

    public static class ConnectedBuddy {
        private WifiP2pDevice dev;
        private Drawable profilePic;
        private String nickName;

        public ConnectedBuddy(WifiP2pDevice dev){
            this.dev=dev;
        }

        public WifiP2pDevice getDev() {
            return dev;
        }

        public Drawable getProfilePic() {
            return profilePic;
        }

        public void setProfilePic(Drawable profilePic) {
            this.profilePic = profilePic;
        }

        public String getNickName() {
            return nickName;
        }

        public void setNickName(String nickName) {
            this.nickName = nickName;
        }

        @Override
        public boolean equals(Object o){
            if (o.getClass().equals(WifiP2pDevice.class)){
                WifiP2pDevice dev = (WifiP2pDevice) o;
                return this.getDev().deviceAddress.equals(dev.deviceAddress);
            }
            return false;
        }

    }

    //empfangen
    public static class ProfilePicServerAsyncTask extends AsyncTask<Void, Integer, Drawable> {
        private Context context;
        private WifiP2pDevice dev;

        public ProfilePicServerAsyncTask(Context context, WifiP2pDevice dev) {
            this.context = context;
            this.dev=dev;
        }

        @Override
        protected Drawable doInBackground(Void... params) {
            ServerSocket serverSocket = null;
            try {
                serverSocket = new ServerSocket(PORT);
                Socket client = serverSocket.accept();
                /**
                 * If this code is reached, a client has connected and transferred data
                 * Save the input stream from the client as a JPEG file
                 */

                ObjectInputStream inputstream = new ObjectInputStream(client.getInputStream());
                Object receivedObject = inputstream.readObject();
                if (receivedObject.getClass().equals(Drawable.class))
                    return (Drawable) receivedObject;

            } catch (IOException e) {
                Log.e(TAG, e.getMessage());
                return null;
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } finally {
                try {
                    if (serverSocket!=null)
                        serverSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Drawable result){
            ConnectedBuddy buddy = BuddyManager.getInstance().getConnectedBuddy(dev);
            if (buddy!=null){
                buddy.setProfilePic(result);
            }
            //TODO GUI benachrichtigen#
            context.startActivity(new Intent(BuddyManager.CONNECTED_BUDDIES_CHANGED_ACTION));
        }
    }

    //senden
    public static class ProfilePicClientAsyncTask extends AsyncTask<Drawable, Integer, Void> {
        private WifiP2pDevice dev;

        public ProfilePicClientAsyncTask(WifiP2pDevice dev) {
            this.dev = dev;
        }

        @Override
        protected Void doInBackground(Drawable... drawables) {
            String host;
            int port;
            int len;
            Socket socket = new Socket();
            byte buf[]  = new byte[1024];

            //TODO nullcheck
            Drawable d = drawables[0];

            try {
                /**
                 * Create a client socket with the host,
                 * port, and timeout information.
                 */
                socket.bind(null);
                socket.connect((new InetSocketAddress(dev.deviceAddress, PORT)), 500);

                /**
                 * Create a byte stream from a JPEG file and pipe it to the output stream
                 * of the socket. This data will be retrieved by the server device.
                 */
                OutputStream outputStream = socket.getOutputStream();

                Bitmap bitmap = ((BitmapDrawable)d).getBitmap();
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
                byte[] bitmapdata = stream.toByteArray();
                ByteArrayInputStream inputStream = new ByteArrayInputStream(bitmapdata);

                while ((len = inputStream.read(buf)) != -1) {
                    outputStream.write(buf, 0, len);
                }

                outputStream.close();
                inputStream.close();
            } catch (FileNotFoundException e) {
                //catch logic
            } catch (IOException e) {
                //catch logic
            }

            /**
             * Clean up any open sockets when done
             * transferring or if an exception occurred.
             */
            finally {
                if (socket != null) {
                    if (socket.isConnected()) {
                        try {
                            socket.close();
                        } catch (IOException e) {
                            //catch logic
                        }
                    }
                }
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void v){
                Log.d(TAG, "Profilbild wurde versendet.");
        }
    }
}

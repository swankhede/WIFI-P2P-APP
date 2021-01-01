package com.swanmade.sharego;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Network;
import android.net.NetworkInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;

/**
 * A BroadcastReceiver that notifies of important Wi-Fi p2p events.
 */
class WiFiDirectBroadcastReceiver extends BroadcastReceiver {

    private WifiP2pManager manager;
    private WifiP2pManager.Channel channel;
    private MainActivity2 mActivity;

    public WiFiDirectBroadcastReceiver (WifiP2pManager manager, WifiP2pManager.Channel channel,
                                        MainActivity2 mActivity){
        super ();
        this.manager = manager;
        this.channel = channel;
        this.mActivity = mActivity;
    }

    @Override
    public void onReceive (Context context, Intent intent){
        String action = intent.getAction ();

        if (WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals ( action )) {
            // Check to see if Wi-Fi is enabled and notify appropriate activity
            int state = intent.getIntExtra ( WifiP2pManager.EXTRA_WIFI_STATE, -1 );
            if (state == WifiP2pManager.WIFI_P2P_STATE_ENABLED) {
                // Wifi P2P is enabled
              //  Toast.makeText ( context, "wifi is on", Toast.LENGTH_SHORT ).show ();
            } else {
                // Wi-Fi P2P is not enabled
                Toast.makeText ( context, "Please turn on WiFi", Toast.LENGTH_SHORT ).show ();
            }

        } else if (WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals ( action )) {
            // Call WifiP2pManager.requestPeers() to get a list of current peers
            if (manager != null) {
                if (ActivityCompat.checkSelfPermission (  context, Manifest.permission.ACCESS_FINE_LOCATION ) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return;
                }
                manager.requestPeers ( channel, mActivity.peerListListener );
            }

        } else if (WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)) {
            // Respond to new connection or disconnections

            if(manager==null){
                return;
            }

            NetworkInfo networkInfo = intent.getParcelableExtra ( WifiP2pManager.EXTRA_NETWORK_INFO );
            if(networkInfo.isConnected ()){
                manager.requestConnectionInfo ( channel,mActivity.connectionInfoListener );
            }

        } else if (WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION.equals(action)) {
            // Respond to this device's wifi state changing
        }
    }
}
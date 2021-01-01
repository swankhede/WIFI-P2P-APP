package com.swanmade.sharego;



import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;

import android.net.Uri;
import android.net.wifi.WifiManager;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.nsd.WifiP2pServiceRequest;
import android.nfc.Tag;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.StrictMode;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.swanmade.sharego.R;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URI;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import static android.net.wifi.p2p.WifiP2pManager.EXTRA_WIFI_P2P_DEVICE;
import static android.net.wifi.p2p.WifiP2pManager.WIFI_P2P_DISCOVERY_STARTED;
import static android.net.wifi.p2p.WifiP2pManager.WIFI_P2P_STATE_ENABLED;
import static com.swanmade.sharego.R.drawable.discoverimg;
import static com.swanmade.sharego.R.drawable.discovershadow;


public class MainActivity2 extends AppCompatActivity {

    Button  send, filechoose;
    Button  discover;
    ListView listView;
    int count=0;
    TextView statusText, filepath;

    WifiManager wifiManager;
    WifiP2pManager manager;
    WifiP2pManager.Channel channel;
    BroadcastReceiver receiver;

    IntentFilter intentFilter;
    TextView textView, textView2;
    List<WifiP2pDevice> peers = new ArrayList<WifiP2pDevice> ();
    String[] devicesNameArray;
    WifiP2pDevice[] deviceArray;
    String FilePath;
    static final int MESSAGE_READ = 1;
    ServerClass serverClass;
    SendReceive sendReceive;
    CilentClass cilentClass;
    String FileName;
    int FileSize;
    private Object Tag;
    private ProgressDialog progress;



    @Override
    protected void onCreate (Bundle savedInstanceState){
        super.onCreate ( savedInstanceState );
        setContentView ( R.layout.new_layout );
        initialWork ();
        exListner ();


        if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder ().permitAll ().build ();
            StrictMode.setThreadPolicy ( policy );


        }


        Checkpermission();
        setRequestedOrientation ( ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);     //  Fixed Portrait orientation


    }

    private void Checkpermission (){
        if (ActivityCompat.shouldShowRequestPermissionRationale ( this,
                Manifest.permission.READ_EXTERNAL_STORAGE ) && ActivityCompat.shouldShowRequestPermissionRationale ( this,
                Manifest.permission.ACCESS_FINE_LOCATION ) &&
                ActivityCompat.shouldShowRequestPermissionRationale ( this,Manifest.permission.WRITE_EXTERNAL_STORAGE )) {

            new AlertDialog.Builder (this).setTitle ( "Permission Needed" ).setMessage ( "PLEASE GRANT ALL PERMISSIONS" )
                    .setPositiveButton ( "ok", new DialogInterface.OnClickListener () {
                        @Override
                        public void onClick (DialogInterface dialogInterface, int i){
                            ActivityCompat.requestPermissions(MainActivity2.this,
                                    new String[] {Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.WRITE_EXTERNAL_STORAGE},1);
                        }
                    } ).setNegativeButton ( "cancel", new DialogInterface.OnClickListener () {
                @Override
                public void onClick (DialogInterface dialogInterface, int i){
                    dialogInterface.dismiss();
                }
            } ).create ().show ();


        }else{
            ActivityCompat.requestPermissions(this,
                    new String[] {Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.WRITE_EXTERNAL_STORAGE},1);
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 1)  {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

            } else {
                Toast.makeText(this, "PERMISSION DENIED", Toast.LENGTH_SHORT).show();
            }
        }
    }


    Handler handler = new Handler ( new Handler.Callback () {
        @Override
        public boolean handleMessage (@NonNull Message message){
            switch (message.what) {
                case MESSAGE_READ:
                    byte[] readBuff = (byte[]) message.obj;
                    //System.out.println ( "in the handler 133" );
                    String msg = new String ( readBuff, 0, message.arg1 );
                    FileName=msg;

                    System.out.println ( "msg received" );
            }
            return true;
        }
    } );



    private void exListner (){
        /**   wifion.setOnClickListener ( new View.OnClickListener () {
        @Override
        public void onClick (View view){
        if (wifiManager.isWifiEnabled ()) {
        wifiManager.setWifiEnabled ( false );
        } else {
        wifiManager.setWifiEnabled ( true );
        }
        }
        } );**/

        send.setOnClickListener ( new View.OnClickListener () {
            @Override
            public void onClick (View view){

                if (filepath.getText ().toString ()!=null) {
                    // String msg1 = msgText.getText ().toString ();
                    if (MainActivity2.this.sendReceive.socket != null) {


                        try {


                            sendReceive.writestream ( FileName.getBytes ( Charset.defaultCharset () ) );
                            System.out.println ( "Filesizeddd:" + FileSize );

                            System.out.println ( "FileNamefyfyf:" + FileName );
                            Thread.sleep ( 1000 );
                            Toast.makeText ( getApplicationContext (), "Transfer Complete ", Toast.LENGTH_SHORT ).show ();
                            sendReceive.sendFile ( FilePath );

                        } catch (IOException | InterruptedException e) {
                            e.printStackTrace ();
                        }

                    }
                }else{
                    Toast.makeText ( getApplicationContext (),"Please Connect to other device first" ,Toast.LENGTH_SHORT).show ();
                }

            }
        } );


        /** refresh.setOnClickListener ( new View.OnClickListener () {
        @Override
        public void onClick (View view){
        if (MainActivity.this.sendReceive.socket!=null){

        try {
        MainActivity.this.sendReceive.inputStream.reset ();
        MainActivity.this.sendReceive.outputStream.flush ();
        MainActivity.this.sendReceive.socket.close ();



        StopWifip2p();
        textView2.setText ( " " );
        textView.setText ( "Connection Status" );

        } catch (IOException e) {
        e.printStackTrace ();
        }
        deleteCache ( getApplicationContext () );
        Toast.makeText ( getApplicationContext (), "Cache cleared", Toast.LENGTH_SHORT ).show ();

        }
        }
        } );**/



        discover.setOnClickListener ( new View.OnClickListener () {
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onClick (View view){
                if (count == 0) {
                    discover.setBackground ( getApplicationContext ().getResources ().getDrawable ( discovershadow ) );
                    System.out.println ( "count"+count );



                    if (ActivityCompat.checkSelfPermission ( getApplicationContext (), Manifest.permission.ACCESS_FINE_LOCATION ) != PackageManager.PERMISSION_GRANTED) {
                        // TODO: Consider calling
                        //    ActivityCompat#requestPermissions
                        // here to request the missing permissions, and then overriding
                        //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                        //                                          int[] grantResults)
                        // to handle the case where the user grants the permission. See the documentation
                        // for ActivityCompat#requestPermissions for more details.
                        return;
                    }


                    manager.discoverPeers ( channel, new WifiP2pManager.ActionListener () {
                        @Override
                        public void onSuccess (){
                            //textView.setText ( "Discovery started" );
                            Toast.makeText ( getApplicationContext (), "Discovery Started", Toast.LENGTH_SHORT ).show ();


                        }

                        @Override
                        public void onFailure (int i){
                            //textView.setText ( "Discovery failed" );
                            Toast.makeText ( getApplicationContext (), "Discovery Failed", Toast.LENGTH_SHORT ).show ();

                            System.out.println ( "failed:(" + i + WIFI_P2P_DISCOVERY_STARTED );
                            System.out.println ( "failed:(" + EXTRA_WIFI_P2P_DEVICE );
                        }


                    } );
                    count++;

                }

                else{
                    discover.setBackground ( getApplicationContext ().getResources ().getDrawable(discoverimg));
                    count--;
                    manager.stopPeerDiscovery ( channel, new WifiP2pManager.ActionListener () {
                        @Override
                        public void onSuccess (){
                            Toast.makeText ( getApplicationContext (), "Discovery Stopped", Toast.LENGTH_SHORT ).show ();
                        }

                        @Override
                        public void onFailure (int i){
                            Toast.makeText ( getApplicationContext (), "Discovery Stopped Failled", Toast.LENGTH_SHORT ).show ();

                        }
                    } );
                }
            }
        } );



        listView.setOnItemClickListener ( new AdapterView.OnItemClickListener () {
            @Override
            public void onItemClick (AdapterView<?> adapterView, View view, int i, long l){


                final WifiP2pDevice pdevice = deviceArray[i];
                final WifiP2pConfig config = new WifiP2pConfig ();
                config.deviceAddress = pdevice.deviceAddress;
                if (ActivityCompat.checkSelfPermission ( getApplicationContext (), Manifest.permission.ACCESS_FINE_LOCATION ) != PackageManager.PERMISSION_GRANTED) {
                    //TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    // public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return;
                }
                manager.connect ( channel, config, new WifiP2pManager.ActionListener () {
                    @Override
                    public void onSuccess (){
                        Toast.makeText ( getApplicationContext (), "Connected to " + pdevice.deviceName, Toast.LENGTH_SHORT ).show ();

                    }

                    @Override
                    public void onFailure (int i){
                        Toast.makeText ( getApplicationContext (), "unable to connect", Toast.LENGTH_SHORT ).show ();
                    }
                } );
            }
        } );

        filechoose.setOnClickListener ( new View.OnClickListener () {
            @Override
            public void onClick (View view){

                Intent fileIntent = new Intent ( Intent.ACTION_GET_CONTENT );
                fileIntent.setType ( "*/*" );
                startActivityForResult ( fileIntent, 10 );


            }
        } );


    }

    private void StopWifip2p (){
        manager.stopPeerDiscovery ( channel, new WifiP2pManager.ActionListener () {
            @Override
            public void onSuccess (){

            }

            @Override
            public void onFailure (int i){
                System.out.println ( "stopped failed"+i );
            }
        } );
    }

    public void deleteCache (Context context){

        try {
            File dir = context.getCacheDir ();
            deleteDir ( dir );
        } catch (Exception e) {
            e.printStackTrace ();
        }
    }


    private static boolean deleteDir (File dir){

        if (dir != null && dir.isDirectory ()) {
            String[] children = dir.list ();
            for (int i = 0; i < children.length; i++){
                boolean success = deleteDir ( new File ( dir, children[i] ) );
                if (!success) {
                    return false;
                }
            }
            return dir.delete ();
        } else if (dir != null && dir.isFile ()) {
            return dir.delete ();
        } else {
            return false;
        }
    }


    @Override
    protected void onActivityResult (int requestCode, int resultCode, @Nullable Intent data){
        super.onActivityResult ( requestCode, resultCode, data );
        switch (requestCode) {
            case 10:
                if (resultCode == RESULT_OK) {
                    FilePath = data.getData ().getPath ();
                    Uri uri = null;
                    if (data != null) {
                        uri = data.getData ();
                        System.out.println ( "uri:"+uri );
                        // Perform operations on the document using its URI.
                        System.out.println ( uri.getPath ().split ( ":" ));
                        String[] name = uri.getPath ().split ( ":" );
                        for (String i:name){
                            System.out.println ( i );
                        }
                        System.out.println ( "name:"+name[1] );
                        File file = new File (
                                Environment.getExternalStorageDirectory (),name[1]);
                        try {
                            filepath.setText ( file.getCanonicalPath () );
                            FilePath = file.getCanonicalPath ();
                            FileSize = (int) file.length ();
                            FileName=file.getName ();


                            System.out.println ( "from server" + FilePath );

                        } catch (IOException e) {
                            e.printStackTrace ();
                        }

                    }


                }
                break;
            default:
                throw new IllegalStateException ( "Unexpected value: " + requestCode );
        }
    }


    private void initialWork (){
        //wifion = (Button) findViewById ( R.id.wifibtn );
        discover = (Button) findViewById ( R.id.discoverbtn );
        send = (Button) findViewById ( R.id.sendbtn );
        send.setEnabled ( false );
        listView = (ListView) findViewById ( R.id.listView );

        textView = (TextView) findViewById ( R.id.textView );
        textView2 = (TextView) findViewById ( R.id.textView2 );
        filepath = (TextView) findViewById ( R.id.path );
        //refresh = (Button) (Button) findViewById ( R.id.refresh );
        wifiManager = (WifiManager) getApplicationContext ().getSystemService ( Context.WIFI_SERVICE );

        manager = (WifiP2pManager) getSystemService ( Context.WIFI_P2P_SERVICE );
        channel = manager.initialize ( this, getMainLooper (), null );
        receiver = new WiFiDirectBroadcastReceiver ( manager, channel, this );



        filechoose = (Button) findViewById ( R.id.pick );
        filechoose.setEnabled ( false );
        intentFilter = new IntentFilter ();
        intentFilter.addAction ( WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION );
        intentFilter.addAction ( WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION );
        intentFilter.addAction ( WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION );
        intentFilter.addAction ( WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION );

    }


    WifiP2pManager.PeerListListener peerListListener = new WifiP2pManager.PeerListListener () {
        @Override
        public void onPeersAvailable (WifiP2pDeviceList peerList){
            if (!peerList.getDeviceList ().equals ( peers )) {
                peers.clear ();
                peers.addAll ( peerList.getDeviceList () );

                devicesNameArray = new String[peerList.getDeviceList ().size ()];
                deviceArray = new WifiP2pDevice[peerList.getDeviceList ().size ()];
                int index = 0;

                for (WifiP2pDevice device : peerList.getDeviceList ()){
                    devicesNameArray[index] = device.deviceName;
                    deviceArray[index] = device;
                    index++;
                }

                ArrayAdapter<String> adapter = new ArrayAdapter<String> ( getApplicationContext (),
                        android.R.layout.simple_list_item_1, devicesNameArray );
                listView.setAdapter ( adapter );

            }

            if (peers.size () == 0) {
                Toast.makeText ( getApplicationContext (), "No device found", Toast.LENGTH_SHORT ).show ();
                return;
            }

        }
    };

    WifiP2pManager.ConnectionInfoListener connectionInfoListener = new WifiP2pManager.ConnectionInfoListener () {
        @Override
        public void onConnectionInfoAvailable (WifiP2pInfo wifiP2pInfo){
            final InetAddress groupOnwerAddress = wifiP2pInfo.groupOwnerAddress;
            if (wifiP2pInfo.groupFormed && wifiP2pInfo.isGroupOwner) {
                textView2.setText ( "Host" );
                send.setEnabled ( true );
                filechoose.setEnabled ( true );
                serverClass = new ServerClass ();
                serverClass.start ();
            } else if (wifiP2pInfo.groupFormed) {
                textView2.setText ( "Client" );
                send.setEnabled ( true );
                filechoose.setEnabled ( true );
                cilentClass = new CilentClass ( groupOnwerAddress );
                cilentClass.start ();
            }
        }
    };

    @Override
    protected void onResume (){
        super.onResume ();
        registerReceiver ( receiver, intentFilter );
    }

    @Override
    protected void onPause (){
        super.onPause ();

        unregisterReceiver ( receiver );
    }

    public class ServerClass extends Thread {
        Socket socket;
        ServerSocket serverSocket;

        @Override
        public void run (){
            try {

                serverSocket = new ServerSocket ();
                serverSocket.setReuseAddress ( true );
                serverSocket.bind ( new InetSocketAddress ( 7777) ); // <-- now bind it

                socket = serverSocket.accept ();
                sendReceive = new SendReceive ( socket);
                sendReceive.start ();
            } catch (IOException e) {
                e.printStackTrace ();
            }
        }
    }

    private class SendReceive extends Thread {
        private Socket socket;
        private InputStream inputStream,inputStream2;
        private OutputStream outputStream,outputStream2;
        public String filePath;

        int bytesRead;
        int current = 0;
        FileOutputStream fos ;
        BufferedOutputStream bos;



        public SendReceive (Socket skt){
            socket = skt;



            try {
                inputStream = socket.getInputStream ();
                System.out.println ("avail:"+inputStream.available ());
                outputStream = socket.getOutputStream ();
            } catch (IOException e) {
                e.printStackTrace ();
            }
        }

        @RequiresApi(api = Build.VERSION_CODES.KITKAT)
        @Override
        public void run (){
            byte[] buffer = new byte[70000000];


            int bytes;

            while (socket != null) {

                try {
                    System.out.println ( "avail:" + inputStream.available () );
                    bytes = inputStream.read ( buffer );


                    if (bytes > 0) {
                        Socket s = socket;

                        handler.obtainMessage ( MESSAGE_READ, bytes, -1, buffer ).sendToTarget ();


                        System.out.println ( "connecting...." );

                        System.out.println ( "avail:" + inputStream.available () );
                        System.out.println ( "isread:" + socket.getSendBufferSize () );
                        System.out.println ( "avail:" + inputStream.toString () );



                        //4th method
                        Socket clientSocket = socket;
                        OutputStream os = outputStream;
                        os = clientSocket.getOutputStream ();
                        PrintWriter pw = new PrintWriter ( os );


                        InputStream is = clientSocket.getInputStream ();
                        InputStreamReader isr = new InputStreamReader ( is );
                        BufferedReader br = new BufferedReader ( isr );

                        Thread.sleep ( 1000 );

                        byte[] buffer3 = new byte[4096];
                        int bytesRead;
                        System.out.println ("FileSize on line 679:" +FileSize+FileName);
                        //String extension =FileName.substring(FileName.lastIndexOf("."));
                        //System.out.println ( "extension: " +extension);
                        File dir = new File(Environment.getExternalStorageDirectory()+"/"+"ShareGo");
                        // System.out.println ( "directory:"+ dir.exists () );
                        if(!dir.exists ()){
                            dir.mkdirs () ;

                        }

                        FileOutputStream fos = new FileOutputStream (Environment.getExternalStorageDirectory()+"/"+"ShareGo"+"/"+FileName);
                        BufferedOutputStream bos = new BufferedOutputStream ( fos );

                        while (true) {

                            bytesRead = is.read ( buffer, 0, buffer.length );
                            System.out.println ( "line 700" );
                            if (bytesRead == -1) {
                                break;
                            }
                            bos.write ( buffer, 0, bytesRead );



                            System.out.println ( "line 706" );
                            bos.flush ();



                        }

                        System.out.println ( "line 710" );



                        bos.close();
                        socket.close();

                        System.out.println ( "file received" );
                        break;





                    }
                } catch (IOException | InterruptedException e) {
                    e.printStackTrace ();


                }


            }
            //Toast.makeText ( getApplicationContext (), "file received", Toast.LENGTH_SHORT ).show ();


        }
        public void writestream (byte[] bytes) throws IOException{
            if (outputStream == null) {

                System.out.println ( "error:" + bytes );
            } else {
                System.out.println ( outputStream );
                outputStream.write ( bytes );

            }
        }


        public void sendFile (String filepath1) throws IOException{

            System.out.println ( "file sending" );
            filePath = filepath1;
            if (outputStream == null) {

                System.out.println ( "error" );
            } else {
                System.out.println ( outputStream );



                File myFile = new File ( filepath1 );
                byte[] mybytearray = new byte[(int) myFile.length ()];
                System.out.println ( "File Details:" + myFile.getName () + myFile.length () );

                FileInputStream fis = new FileInputStream ( myFile );
                fis.read (mybytearray,0,mybytearray.length);
                outputStream.write (mybytearray,0,mybytearray.length);
                fis.close ();

                System.out.println ( filepath1 );

                System.out.println ( "not empty" );


            }



        }

    }
    public class CilentClass extends Thread {
        Socket socket;

        String hostAdd;

        public CilentClass (InetAddress hostAddress){
            hostAdd = hostAddress.getHostAddress ();
            socket = new Socket ();


        }

        @Override
        public void run (){
            try {
                socket.bind ( null );
                socket.connect ( new InetSocketAddress ( hostAdd, 7777 ), 5000 );
                sendReceive = new SendReceive ( socket);
                sendReceive.start ();
            } catch (IOException e) {
                e.printStackTrace ();


            }
        }
    }


}









package com.zgr.gpxcreator;


import android.Manifest;
import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;
import java.util.ArrayList;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;



public class LiveTrackingActivity extends FragmentActivity implements OnMapReadyCallback {

    private static final String TAG = "LiveTrackingActivity";
    private static final int WRITING_REQUEST_CODE = 1;
    private GoogleMap mMap;
    private LiveTrackingService liveTrackingService;
    private boolean isConnected;//false by default
    private ImageView pauseResumeButton , doneButton;
    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            LiveTrackingService.LocalBinder mLocalBinder = (LiveTrackingService.LocalBinder) service;
            liveTrackingService = mLocalBinder.getLiveTrackingService();
            assert liveTrackingService != null;
            //Get the last user interaction with Location service
            isConnected = liveTrackingService.getConnectingMood();
            if (isConnected){
                //user connected to Location Service
                //show pause button
                pauseResumeButton.setImageResource(R.mipmap.ic_pause);
                doneButton.setVisibility(View.INVISIBLE);
            }else {
                //user disConnected from Location Service
                //show resume button
                pauseResumeButton.setImageResource(R.mipmap.ic_start);
                doneButton.setVisibility(View.VISIBLE);
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };
    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            //Get LatLng From the Intent In String Data Type and Converting it to Original Object
            String latLngAsString = intent.getExtras().getString(IntentKeys.LAT_LNG);
            LatLng latLng = Converter.convertStringToLatLng(latLngAsString);
            moveCamera(latLng);
            //Get the Track in String Data Type and Converting it to Original Object
            String trackAsString = intent.getExtras().getString(IntentKeys.TRACK);
            Track track = Converter.convertStringToTrack(trackAsString);
            drawTrack(track);
        }
    };
    private IntentFilter intentFilter = new IntentFilter(LiveTrackingService.INTENT_ACTION_NAME);



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_live_tracking);

        //Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


        pauseResumeButton = findViewById(R.id.pause_resume_button);
        doneButton = findViewById(R.id.done_button);

        //initialize 'admob' banner
        AdView mAdView = findViewById(R.id.tracking_banner);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);


        //bind to service
        bindService(new Intent(this , LiveTrackingService.class) , serviceConnection , Context.BIND_AUTO_CREATE);
    }









    @Override
    protected void onResume() {
        super.onResume();
        //register BroadCast Receiver
        registerReceiver(broadcastReceiver , intentFilter);
    }






    @SuppressLint("MissingPermission")
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        if (mMap != null){
            //Configure map Ui settings
            mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
            mMap.setMyLocationEnabled(true);
            mMap.getUiSettings().setZoomControlsEnabled(true);
        }
    }



    /*click event handling for done button*/
    public void doneOnClick(View view) {
        if (liveTrackingService != null){
            saveFile();
        }
    }




    /*Save GpxFileContent in external storage as Gpx File */
    public void saveFile(){
        //check android version
        if (Build.VERSION.SDK_INT >= 23){
            if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
                //permission denied
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE} , WRITING_REQUEST_CODE);
            }else {
                //Get Gpx File Content
                GpxParser gpxParser = liveTrackingService.getGpxParser();
                String fileContent = gpxParser.getFinalGpxContent();
                //Save File in the External Storage
                String path = FilesManager.saveFile(this , fileContent);
                Toast.makeText(this, "File Saved Successfully "+path, Toast.LENGTH_LONG).show();
                startMainActivity();
            }
        }else {
            //Get Gpx File Content
            GpxParser gpxParser = liveTrackingService.getGpxParser();
            String fileContent = gpxParser.getFinalGpxContent();
            //Save File in the External Storage
            String path = FilesManager.saveFile(this , fileContent);
            Toast.makeText(this, "File Saved Successfully "+path, Toast.LENGTH_LONG).show();
            startMainActivity();
        }
    }




    private void startMainActivity(){
        unbindService(serviceConnection);
        liveTrackingService.stopSelf();
        finish();
        startActivity(new Intent(this , MainActivity.class));
    }




    public void pauseOnClick(View view) {
        if (!isConnected){
            //tell the service the use resume the connection to the location service
            if (liveTrackingService != null){
                liveTrackingService.resume();
            }
            //
            pauseResumeButton.setImageResource(R.mipmap.ic_pause);
            doneButton.setVisibility(View.INVISIBLE);
            isConnected = true;
        }else {
            //tell the service the use pause the connection to the location service
            if (liveTrackingService != null){
                liveTrackingService.pause();
            }
            //
            pauseResumeButton.setImageResource(R.mipmap.ic_start);
            doneButton.setVisibility(View.VISIBLE);
            isConnected = false;
        }
    }



    /**
     * @param latLng member used to move camera to certain position
     */
    private void moveCamera (LatLng latLng){
        try {
            mMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));
        }catch (NullPointerException e){
            Log.i(TAG , e.getMessage());
        }
    }




    /**
     * @param track an Object Used to extract {@link PolylineOptions} List and draw it to the map
     * */
    private void drawTrack (Track track){
        try {
            //Get The ArrayList
            ArrayList<PolylineOptions> pathList = track.getPathArrayList();
            //Check if the ArrayList is Not Empty (No Data on it)
            if (!pathList.isEmpty()){
                //draw the path in map using addPolyline
                for(PolylineOptions path : pathList){
                    mMap.addPolyline(path);
                }
            }
        }catch (NullPointerException e){
            Log.i(TAG , e.getMessage());
        }
    }







    @Override
    protected void onPause() {
        super.onPause();
        //unregister BroadCast Receiver
        unregisterReceiver(broadcastReceiver);
    }








    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case WRITING_REQUEST_CODE:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    //Permission Granted
                    saveFile();
                }else {
                    //Permission Denied
                    DialogNotifier.showExplainingDialogForWritePermission(this , this);
                }
                break;
        }
    }





    /**wrap unbind () with try/catch
     *  because the user may unbind when he click done button
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();

        try {
            //unbind from the service
            unbindService(serviceConnection);
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}

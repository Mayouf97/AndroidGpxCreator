package com.zgr.gpxcreator;


import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import com.google.android.gms.maps.model.LatLng;


public class LiveTrackingService extends Service {

    protected static final String INTENT_ACTION_NAME = "locationDataIntent";
    private Intent i = new Intent(INTENT_ACTION_NAME);
    private static final String TAG = "LiveTrackingService";
    private static final String NOTIFICATION_CHANEL_ID = "id";
    public static final int NOTIFICATION_ID = 0;//Notification channel ID
    private final IBinder mBinder = new LocalBinder();
    private boolean isConnected = true;
    private Track track = new Track();
    private GpxParser gpxParser = new GpxParser();
    private Location oldLocation;
    private LocationHandler locationHandler;


    @Override
    public void onCreate() {
        super.onCreate();
        //
        showNotificationBar();
        //start location Services
        locationHandler = new LocationHandler();
        locationHandler.requestLocationUpdates();

        Log.i(TAG , "service onCreate called");
    }



    private void showNotificationBar (){
        //Set up Notification Bar
        //Intent of the LiveTracingActivity
        Intent notificationIntent = new Intent(this, LiveTrackingActivity.class);
        //For Android Oreo and Higher Versions
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            //Pending Intent For the Notification
            PendingIntent pendingIntent = PendingIntent.getActivity(this , 0 , notificationIntent , PendingIntent.FLAG_NO_CREATE);
            //setup and create Notification channel
            NotificationChannel notificationChannel = new NotificationChannel (NOTIFICATION_CHANEL_ID , "Notification" , NotificationManager.IMPORTANCE_DEFAULT);
            notificationChannel.setDescription("Tracking Your Work-out");
            // Register the channel with the system
            NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            assert notificationManager != null;
            notificationManager.createNotificationChannel(notificationChannel);
            //Notification Builder
            NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this, NOTIFICATION_CHANEL_ID)
                    .setSmallIcon(R.mipmap.ic_my_location)
                    .setContentTitle(getResources().getString(R.string.app_name))
                    .setContentText(getResources().getString(R.string.notification_description))
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    // Set the intent that will fire when the user taps the notification
                    .setContentIntent(pendingIntent);
            //show Notification Manager in status bar
            notificationManager.notify(NOTIFICATION_ID, mBuilder.build());
        }else {
            //Under Android Oreo Versions
            //Pending Intent and in Here we Gonna use our Intent
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 , notificationIntent, 0);
            //Notification Builder
            Notification notification = new Notification.Builder(this)
                    .setContentIntent(pendingIntent)
                    .setContentTitle(getResources().getString(R.string.app_name))
                    .setContentText(getResources().getString(R.string.notification_description))
                    .setSmallIcon(R.mipmap.ic_my_location)
                    .build();
            //show notification bar in foreground
            startForeground(1 , notification);
        }
    }



    private void removeNotificationBar (){
        //check the android version
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            if (notificationManager != null){
                notificationManager.cancel(NOTIFICATION_ID);
            }
        }else {
            //remove the Notification Bar The Tracking
            stopForeground(true);
        }
    }



    class LocalBinder extends Binder {
        LiveTrackingService getLiveTrackingService (){
            return LiveTrackingService.this;
        }
    }



    @Override
    public IBinder onBind(Intent intent) {
        Log.i(TAG , "Client connected to the service");
       return mBinder;
    }



    private void processLocationUpdates (Location location){
        //Construct LatLng Object Using Longitude and Latitude from the location Object
        LatLng latLng = new LatLng(location.getLatitude() , location.getLongitude());

        //check the accuracy of the location
        if (location.getAccuracy() <= 20){
            Log.i(TAG , "accuracy = "+location.getAccuracy());
            //User Location Changed
            //Append Gpx File Content
            double elevation = location.getAltitude();
            gpxParser.processLocationUpdates(latLng , elevation);

            //Draw the User Track
            if (oldLocation != null){
                preparePathData(location , oldLocation);
                //send the track updates to the client
                i.putExtra(IntentKeys.TRACK , Converter.convertTrackToString(track));
            }
            //change old location to the new one to get ready for the new calculation when we get updates
            oldLocation = location;
            Log.i(TAG  ,"processLocationUpdates Called");
        }
        //Convert LatLng Object To String and Put in the Intent
        String latLngAsString = Converter.convertLatLngToString(latLng);
        i.putExtra(IntentKeys.LAT_LNG , latLngAsString);
        //
        sendBroadcast(i);
    }



    //append path using the received locations data
    private void preparePathData (Location currentLocation , Location previousLocation){
            //when distance = 0
            // that mean user don't move from his previous location
            // and it not necessary to prepare any data fot the path
            if(currentLocation.distanceTo(previousLocation) > 0){
                //Create LatLng
                LatLng ll = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
                //check if the user connect to location service or not
                if (isConnected){
                    //
                    track.appendPathData(ll , Color.BLUE);
                }else {
                    //
                    track.appendPathData(ll , Color.GRAY);
                }
            }
            Log.i(TAG  ,"preparePathData Called");
    }



    /*Get Called when user Click pause Button*/
    public void pause (){
        track.getPolyLineOptionInstance();
        isConnected = false;
    }



    /*Get Called when user Click resume Button*/
    public void resume (){
        track.getPolyLineOptionInstance();
        isConnected = true;
    }



    public GpxParser getGpxParser(){
        return gpxParser;
    }



    /*returns the last user interaction with tracking service*/
    public boolean getConnectingMood (){
        return isConnected;
    }



    @Override
    public void onDestroy() {
        super.onDestroy();
        removeNotificationBar();
        locationHandler.removeLocationUpdates();
        //
        Log.i(TAG , "onDestroy service");
    }



    /*class used to request and remove location updates and listening for the location updates*/
    public class LocationHandler implements LocationListener {

        private static final int location_updates_refresh_rate = 3 * 1000;
        private LocationManager locationManager;


        @SuppressLint("MissingPermission")
        void requestLocationUpdates (){
            locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
            assert locationManager != null;
            //request location updates from the network and gps providers
            try {
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER , location_updates_refresh_rate  , 0 , this);
            }catch (Exception e){
                e.printStackTrace();
            }
            try {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER , location_updates_refresh_rate  , 0 , this);
            }catch (Exception e){
                e.printStackTrace();
            }
        }


        void removeLocationUpdates (){
            assert locationManager != null;
            locationManager.removeUpdates(this);
        }


        @Override
        public void onLocationChanged(Location location) {
            if (location != null){
                processLocationUpdates(location);
            }
        }


        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }


        @Override
        public void onProviderEnabled(String provider) {

        }


        @Override
        public void onProviderDisabled(String provider) {

        }
    }


}

package com.zgr.gpxcreator;

import android.Manifest;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

public class MainActivity extends AppCompatActivity {


    private static final int LOCATION_REQUEST_CODE = 0;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //check if the LiveTrackingService is running
        if (getServiceState()){
            //Service is Running
            // Direct User the LiveTrackingActivity
            finish();
            startActivity(new Intent(this , LiveTrackingActivity.class));
        }
        setContentView(R.layout.activity_main);

        //ads
        AdView mAdView = findViewById(R.id.main_banner);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);
    }



    /**
     * check if service {@link LiveTrackingService}
     * @return it will return true when it's running and false when it's not running
     */
    private boolean getServiceState() {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        //Get the list of the Running services by Using the getRunning services from Activity Manager
        if (manager != null) {
            for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
                if (LiveTrackingService.class.getName().equals(service.service.getClassName())) {
                    return true;
                }
            }
        }
        return false;
    }





    /**
     * @return true when location services are activated on the Device or not
     */
    private boolean getLocationServicesStat() {
        LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (manager != null){
            //Provider List
            String[] providersList = {
                    LocationManager.GPS_PROVIDER ,
                    LocationManager.NETWORK_PROVIDER
            };
            //check if one of the providers in the list is Enabled or not
            for (String aProvidersList : providersList) {
                if (manager.isProviderEnabled(aProvidersList)) {
                    return true;
                }
            }
        }
        return false;
    }







    public void onClick (View view) {
        switch (view.getId()){

            case R.id.manually_button:
                //
                startManualSelectingActivity();
                break;

            case R.id.live_tracking_button:
                //check if the Location Services enabled or not
                if (getLocationServicesStat()){
                    //check location permission
                    startLiveTrackingActivity();
                }else {
                    //show alert dialog to notify the user to activate Location Services
                    DialogNotifier.showGpsOffDialog(this , this);
                }
                break;
        }
    }





    /**
     * check if location permission was granted or not
     * Kill Current Activity by calling {@link Activity#finish()}
     *and start  {@link ManualSelectingActivity} activity
     */
    private void startManualSelectingActivity(){
        //check the android version
        if (Build.VERSION.SDK_INT >= 23){
            //check if the permission was Granted or not
            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
                //Permission is not Granted
                //Ask Permission from the User
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION} , LOCATION_REQUEST_CODE);
            }else {
                finish();
                startActivity(new Intent(this , ManualSelectingActivity.class));
            }
        }else {
            //
            finish();
            startActivity(new Intent(this , ManualSelectingActivity.class));
        }
    }






    /**check if Location permission Granted or not by calling {@link android.content.ContextWrapper#checkCallingOrSelfPermission(String)}
     * kill current activity by calling {@link Activity#finish()} and
     * start {@link LiveTrackingActivity} and service {@link LiveTrackingService}
     */
    private void startLiveTrackingActivity(){
        //check the android version
        if (Build.VERSION.SDK_INT >= 23){
            //check if the permission was Granted or not
            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
                //Permission is not Granted
                //Ask Permission
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION} , LOCATION_REQUEST_CODE);
            }else {
                finish();
                startService(new Intent(this , LiveTrackingService.class));
                startActivity(new Intent(this , LiveTrackingActivity.class));
            }
        }else {
            finish();
            startService(new Intent(this , LiveTrackingService.class));
            startActivity(new Intent(this , LiveTrackingActivity.class));
        }
    }







    @Override
    public void onRequestPermissionsResult (int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case LOCATION_REQUEST_CODE:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    //Permission Granted
                    Toast.makeText(this, "Perfect Permission Granted !", Toast.LENGTH_SHORT).show();
                }else {
                    //Permission Denied
                    DialogNotifier.showExplainingDialog(this);
                }
                break;
        }
    }


}

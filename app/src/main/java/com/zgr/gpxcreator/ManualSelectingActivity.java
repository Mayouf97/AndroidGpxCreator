package com.zgr.gpxcreator;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;


public class ManualSelectingActivity extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnMapClickListener {


    public static final String TAG = "ManualSelectingActivity";
    private static final int WRITING_REQUEST_CODE = 1;
    private static final float polyLineWidth = 8f;
    private static final int polyLineColor = Color.BLUE;
    private GoogleMap mMap;
    private TextView title;
    private ImageView moodIcon , doneIcon , backIcon;
    private boolean switchHelper;//help as to switch btw pause and resume selecting mood
    private List<Polyline> polylineList = new ArrayList<>();//list for polylines
    private LatLng lastLatLng;
    private Marker firstMarker;
    private GpxParser gpxParser = new GpxParser();



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manual_selecting);

        //initialize 'admob' banner
        AdView mAdView = findViewById(R.id.manual_banneer);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

        //Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);

        //sync the map
        mapFragment.getMapAsync(this);

        //initialize the ui components
        setUpUi ();
    }





    /**
     * find all necessary {@link View}  and set up listeners to get ready for the User interactions
     * */
    private void setUpUi (){
        title = findViewById(R.id.map_title);
        moodIcon = findViewById(R.id.mood_image);
        backIcon = findViewById(R.id.deletePoint_image);
        //click listener for moodIcon
        moodIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!switchHelper){
                    //set up click listener for the map
                    //so the user can click in map the select places
                    moodIcon.setImageResource(R.mipmap.ic_pause);
                    doneIcon.setVisibility(View.VISIBLE);
                    backIcon.setVisibility(View.VISIBLE);
                    title.setText(getResources().getString(R.string.selecting));
                    switchHelper = true;
                }else {
                    moodIcon.setImageResource(R.mipmap.ic_start);
                    doneIcon.setVisibility(View.INVISIBLE);
                    backIcon.setVisibility(View.INVISIBLE);
                    title.setText(getResources().getString(R.string.start_select_points));
                    switchHelper = false;
                }
            }
        });
        //
        doneIcon = findViewById(R.id.done_image);
        doneIcon.setVisibility(View.INVISIBLE);
        backIcon.setVisibility(View.INVISIBLE);
    }








    @SuppressLint("MissingPermission")
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        if (mMap != null){
            mMap.setOnMapClickListener(ManualSelectingActivity.this);
            //
            mMap.setMyLocationEnabled(true);
            //
            mMap.getUiSettings().setZoomControlsEnabled(true);
        }
    }







    @Override
    public void onMapClick(LatLng latLng) {
        //check if use in selecting Mood or not
        if (switchHelper){
            selectPlace(latLng);
        }
    }





    /**
     * @param latLng used to select specific point in the map and append a {@link Polyline} 'Path'
     * and send it to append {@link GpxParser#processManualUpdates(LatLng)}
     */
    private void selectPlace(LatLng latLng){

        //check if their is a location update before to start append polyline (Path)
        if (lastLatLng != null){

            //add polyline with the two gotten updates
            polylineList.add(mMap.addPolyline(new PolylineOptions()
                    .add(lastLatLng , latLng)
                    .width(polyLineWidth)
                    .color(polyLineColor)));

        }

        //change the last update to the New one to get ready to handle next update
        lastLatLng = latLng;

        //check if the start marker in the map or not if its not exist go head and add it to the map
        if (firstMarker == null){
            //add green marker to the map
            firstMarker = mMap.addMarker(new MarkerOptions().position(latLng)
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
        }

        //send updates to append Gpx File Content
        gpxParser.processManualUpdates(latLng);
    }







    /**
     * Delete the last add {@link LatLng} on Map
     * */
    public void deleteOnePoint(View view){
        //Check if the PolyLine List is empty or not
        //if it's empty that mean no polyLines have been added to the map
        if (!polylineList.isEmpty()){
            //remove The Last PolyLine from The List and from the map
            //Get the last available index on the list
            int lastIndexM = polylineList.size()-1;

            //get Last Polyline by using Last Index
            Polyline lastPolyLine = polylineList.get(lastIndexM);

            //remove polyline from the list
            polylineList.remove(lastIndexM);

            //remove polyline from the map
            lastPolyLine.remove();



            //After removing Polyline check if the list is Empty or not
            if (polylineList.size() == 0){
                //remove Marker from the map
                firstMarker.remove();

                //reset state to get ready for next update
                lastLatLng = null;

                //make marker null to Get ready to next update
                firstMarker = null;
            }else {
                //get Last Polyline in list after removing and get Last LatLng's from it
                List<LatLng> pointsOFLastPolyline = polylineList.get(polylineList.size()-1).getPoints();

                //get the last index in the LatLng list
                int lastIndex = pointsOFLastPolyline.size()-1;

                //make update  equal to the latest selected point to Get ready to add next update
                lastLatLng = pointsOFLastPolyline.get(lastIndex);
            }

            //Delete Single Data Tag from The Gpx Content Text
            gpxParser.deleteOneTag();
        }
    }



    //Event Handling For the Done Button
    public void done(View view) {
       saveFile();
    }





    /**
     * get final parsed GpxFileContent from {@link GpxParser#getFinalGpxContent()}
     * and save it in external device storage in 'gpx' Format
     * */
    private void saveFile (){
        //check android version
        if (Build.VERSION.SDK_INT >= 23){
            if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
                //permission denied
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE} , WRITING_REQUEST_CODE);
            }else {
                //permission granted
                //Get Gpx File Content
                String fileContent = gpxParser.getFinalGpxContent();
                //Save File in the External Storage
                String path = FilesManager.saveFile(this , fileContent);
                finish();
                startActivity(new Intent(this , MainActivity.class));
                Toast.makeText(this, "File Saved Successfully "+path, Toast.LENGTH_LONG).show();
            }
        }else {
            //Get Gpx File Content
            String fileContent = gpxParser.getFinalGpxContent();
            //Save File in the External Storage
            String path = FilesManager.saveFile(this , fileContent);
            finish();
            startActivity(new Intent(this , MainActivity.class));
            Toast.makeText(this, "File Saved Successfully "+path, Toast.LENGTH_LONG).show();
        }
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
                    showExplainingDialogForWritePermission();
                }
                break;
        }
    }



    ///Show alert Dialog to Explain to the user about the effect of deny permission
    private void showExplainingDialogForWritePermission (){
        final AlertDialog alertDialog = new AlertDialog.Builder(this).create();
        alertDialog.setTitle(getResources().getString(R.string.permission_deny));
        alertDialog.setMessage(getResources().getString(R.string.write_permission_explaining));
        alertDialog.setButton(DialogInterface.BUTTON_NEGATIVE, getResources().getString(R.string.ask_me_again), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                //close dialog
                alertDialog.dismiss();
                //give the user opportunity to allow permission and save file
                saveFile();
            }
        });
        alertDialog.show();
    }

}

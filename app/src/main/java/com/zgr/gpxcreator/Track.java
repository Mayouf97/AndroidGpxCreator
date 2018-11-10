package com.zgr.gpxcreator;


/*class generate tracking path*/


import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;
import java.util.ArrayList;



class Track {

    private int currentIndex = 0;//represent current index in the array list
    private ArrayList<PolylineOptions> pathArrayList = new ArrayList<>();//Holds all the polylineOption we create
    private PolylineOptions p = new PolylineOptions();//temporary member they hold collection of data for period of time


    //append current polyline with latLng Points
    void appendPathData (LatLng l1  , int color){

        //add point to the current
        p.add(l1).color(color).width(8f);
        //if the list is empty then add the first polyline to it
        if (pathArrayList.size() == 0){

            //add polyline
            pathArrayList.add(p);

        }else {
            //check if the list need to expand or not
            if (currentIndex + 1 == pathArrayList.size()){

                //change the value in the currentIndex
                pathArrayList.set(currentIndex , p);

            }else {

                //add new Polyline Option to the list
                pathArrayList.add(p);
            }
        }
    }


    void getPolyLineOptionInstance(){
        //when current polyline don't Contain any LatLng points
        //it not necessary to create new Instance
        //and still using the current one
        if (p.getPoints().size() > 0){
            //current polyline Contain LatLng points
            p = new PolylineOptions();
            //increase the current index
            currentIndex ++;
        }
    }


    ArrayList<PolylineOptions> getPathArrayList (){
        return pathArrayList;
    }

}

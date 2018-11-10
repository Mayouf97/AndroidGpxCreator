package com.zgr.gpxcreator;

/*class used to convert Object to json format
 and Strings to Original Object*/

import com.google.android.gms.maps.model.LatLng;
import com.google.gson.Gson;

class Converter {

    /*Convert LatLng Object to String*/
    static String convertLatLngToString (LatLng latLng){
        Gson gson = new Gson();
        return gson.toJson(latLng);
    }

    /*Convert LatLng Object To String*/
    static LatLng convertStringToLatLng (String json){
        Gson gson = new Gson();
        return gson.fromJson(json , LatLng.class);
    }

    /*Convert Track Object to String*/
    static String convertTrackToString (Track track){
        Gson gson = new Gson();
        return gson.toJson(track);
    }

    /*Convert Track Object to String*/
    static Track convertStringToTrack (String sessionPath){
        Gson gson = new Gson();
        return gson.fromJson(sessionPath , Track.class);
    }

}

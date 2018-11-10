package com.zgr.gpxcreator;


import com.google.android.gms.maps.model.LatLng;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;


/**
 * GpxParser allow as to generate a full xml file content
 * */

class GpxParser {



    private List<String> tagsList = new ArrayList<>();



    /**
     * @param latLng Used to append Xml data Tag
     * */
    void processManualUpdates (LatLng latLng){
        //append Xml Content with Tag using  Received Data
        //Note when user select point manually the elevation always be 0
        String updateText ="<trkpt lon=\""+latLng.longitude+"\" lat=\""+latLng.latitude+"\">\n"+
                "        <ele>"+0+"</ele>\n"+
                "        <time>"+ timeFormatter()+".000Z</time>\n"+
                "      </trkpt> \n";
        //Add the Tag the tags list to be use later
        tagsList.add(updateText);
    }






    /**
     * @param latLng Used to append Xml Content with data tag
    * */
    void processLocationUpdates (LatLng latLng , double elevation){
        //append Xml Content with Tag using  Received Data
        String dataTag ="<trkpt lon=\""+latLng.longitude+"\" lat=\""+latLng.latitude+"\">\n"+
                "        <ele>"+elevation+"</ele>\n"+
                "        <time>"+ timeFormatter()+".000Z</time>\n"+
                "      </trkpt> \n";
        //Add the Tag the tags list to be use later
        tagsList.add(dataTag);
    }





    /**
     * @return current Date as String in this Format yyyy-MM-dd'T'HH:mm:ss
     */
    private String timeFormatter(){
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss" , Locale.US);
        return sdf.format(new Date());
    }







    //Remove the last added xml data tag
    void deleteOneTag (){
        int lastIndex = tagsList.size() - 1;//get last available index
        tagsList.remove(lastIndex);//remove item by using the last index
    }




    /**
     * @return the final  Parsed Gpx xml Content
     */
    String getFinalGpxContent(){
        StringBuilder gpxFileContent
                = new StringBuilder("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + "<gpx version=\"1.1\" creator=\"Gpx Creator\">\n" + "<trk>\n" + "\n" + "  <trkseg> \n");
        //append xml body with all collected tags
        for (int i=0 ; i < tagsList.size(); i++){
            gpxFileContent.append(tagsList.get(i));
        }
        //close xml
        String closerText = "</trkseg>\n" + "</trk>\n" + "</gpx>";
        gpxFileContent.append(closerText);
        return gpxFileContent.toString();
    }

}

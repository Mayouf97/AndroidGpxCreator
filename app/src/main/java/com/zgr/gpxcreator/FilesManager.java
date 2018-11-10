package com.zgr.gpxcreator;


/*class used to Save Files*/

import android.content.Context;
import android.os.Environment;
import android.util.Log;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import static com.zgr.gpxcreator.ManualSelectingActivity.TAG;


class FilesManager {




    /**
     * @param context used to access to some of the functions
     * @param fileContents the content wanted to be saved
     * @return the path of this saved file in the external storage if operation failed return null
     */
    public static String saveFile (Context context , String fileContents){
        //File Name Text Format
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd 'at' HH:mm:ss" , Locale.US);
        String filename = sdf.format(new Date())+".gpx";
        //check the external
        if (isExternalStorageWritable()){
            //create Dir
            File directory = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                    , context.getResources().getString(R.string.folder_name));
            //create the dic
            if (!directory.mkdir()){
                Log.i(TAG , "Directory not created");
            }

            FileWriter fw;
            try {
                fw = new FileWriter(new File(directory , filename));
                fw.write(fileContents);
                fw.flush();
                fw.close();
                return directory.getAbsolutePath();
            } catch (IOException e) {
                Log.i(TAG , e.getMessage());
            }
        }
        return null;
    }







    /***
     * @return true if external storage is mounted and false if is not
     */
    private static boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)){
            Log.i(TAG , "media is mounted");
            return true;
        }
        else {
            Log.i(TAG , "media is not mounted");
            return false;
        }
    }

}

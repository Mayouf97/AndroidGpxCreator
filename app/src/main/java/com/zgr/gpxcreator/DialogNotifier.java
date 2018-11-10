package com.zgr.gpxcreator;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.provider.Settings;
import android.widget.Toast;

class DialogNotifier {




    /**@param context used to access some function needed to show alert Dialog
     * show alert Dialog to explain to the user about the important of granting location Permission
     */
    static void showExplainingDialog (Context context){
        final AlertDialog alertDialog = new AlertDialog.Builder(context).create();
        alertDialog.setMessage(context.getResources().getString(R.string.explaining_message));
        alertDialog.setButton(DialogInterface.BUTTON_NEGATIVE, context.getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                //close dialog
                alertDialog.dismiss();
            }
        });
        alertDialog.show();
    }







    /**@param context used to access some function needed to show alert Dialog
     * Show Explaining Alert Dialog to Notify the User About the GPS State
     */
    static void showGpsOffDialog (final Activity activity , final Context context){
        final AlertDialog alertDialog = new AlertDialog.Builder(context).create();
        alertDialog.setTitle(context.getResources().getString(R.string.gps_off_title));
        alertDialog.setMessage(context.getResources().getString(R.string.gps_off_dialog_message));
        alertDialog.setButton(DialogInterface.BUTTON_NEGATIVE, context.getResources().getString(R.string.activate), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                //close dialog
                alertDialog.dismiss();
                //create intent
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                //Open Gps Setting Activity Safely Using resolver
                if (intent.resolveActivity(activity.getPackageManager()) != null){
                    //start activity
                    activity.startActivity(intent);
                }else {
                    //Explaining Toast to the User !
                    Toast.makeText(context, "Error Happens ! You Can Active Your GPS Manually", Toast.LENGTH_LONG).show();
                }
            }
        });
        alertDialog.show();
    }







    /**
     * @param context used to access some function needed to show alert Dialog
     * @param liveTrackingActivity allow as to access {@link LiveTrackingActivity#saveFile()} function
     */
    static void showExplainingDialogForWritePermission (Context context ,final LiveTrackingActivity liveTrackingActivity){
        final AlertDialog alertDialog = new AlertDialog.Builder(context).create();
        alertDialog.setTitle(context.getResources().getString(R.string.permission_deny));
        alertDialog.setMessage(context.getResources().getString(R.string.write_permission_explaining));
        alertDialog.setButton(DialogInterface.BUTTON_NEGATIVE, context.getResources().getString(R.string.ask_me_again), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                //close dialog
                alertDialog.dismiss();
                //give the user opportunity to allow permission and save file
                liveTrackingActivity.saveFile();
            }
        });
        alertDialog.show();
    }


}

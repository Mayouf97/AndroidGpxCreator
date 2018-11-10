package com.zgr.gpxcreator;


import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import com.google.android.gms.ads.MobileAds;



public class SplashScreen extends AppCompatActivity {

	 public static final int time_to_wait = 5 * 1000;
	 private Handler handler = new Handler();
	 private Runnable runnable;


	 @Override
	 protected void onCreate(Bundle savedInstanceState) {
		  super.onCreate(savedInstanceState);
		  setContentView(R.layout.activity_splach_screen);

		  //initialize 'admob' for the app
		  MobileAds.initialize(this, getResources().getString(R.string.admob_key));
	 }



	 @Override
	 protected void onResume() {
		  super.onResume();
		   runnable = new Runnable(){
				@Override
				public void run() {
					 //finish the activity
					 finish();
					 //start the home activity
					 startActivity(new Intent(SplashScreen.this , MainActivity.class));
					 //remove callback from the Handler
					 handler.removeCallbacks(runnable);
				}
		  };
		  //After the Screen shows wait 5 second and switch to the home activity
		  handler.postDelayed(runnable , time_to_wait);
	 }

}

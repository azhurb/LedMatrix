package com.azhurb.LedMatrix;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

public class ServiceLaunchActivity extends Activity{
    
    static final String TAG = "ServiceLaunchActivity";

    protected void onCreate(Bundle savedInstanceState) {
        
        super.onCreate(savedInstanceState);
        
        Intent i = new Intent(this, LedMatrixService.class);
        
        //i.setAction("com.azhurb.LedMatrix.ACCESSORY_ATTACHED");
        //sendBroadcast(new Intent("com.azhurb.LedMatrix.ACCESSORY_ATTACHED"));
        
        startService(i);
        
        Log.d(TAG, "service started");
        
        showToast("Service started from activity");
        
        //sendBroadcast(new Intent("com.azhurb.LedMatrix.ACCESSORY_ATTACHED"));
        //finish();
    }
    
    public void showToast(String msg){
    	Context context = getApplicationContext();
    	CharSequence text = msg;
    	int duration = Toast.LENGTH_SHORT;

    	Toast toast = Toast.makeText(context, text, duration);
    	toast.show();
    }
}

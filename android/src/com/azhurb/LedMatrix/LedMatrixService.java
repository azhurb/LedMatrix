package com.azhurb.LedMatrix;

import com.android.future.usb.UsbAccessory;
import com.android.future.usb.UsbManager;

import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.audiofx.Visualizer;
import android.os.IBinder;
import android.os.ParcelFileDescriptor;
import android.util.Log;
import android.widget.Toast;

import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

public class LedMatrixService extends Service implements Runnable {
    
    private static final String TAG = "LedMatrixService";    

    private static final String ACTION_USB_PERMISSION = "com.azhurb.LedMatrix.action.USB_PERMISSION";
    
    private UsbManager mUsbManager;
    private PendingIntent mPermissionIntent;
    private boolean mPermissionRequestPending;

    UsbAccessory mAccessory;
    ParcelFileDescriptor mFileDescriptor;
    FileInputStream mInputStream;
    FileOutputStream mOutputStream;
    
    private Visualizer mVisualizer;
    
    private VisualizerView mVisualizerView;
    
    @Override
    public IBinder onBind(Intent intent) {
        // TODO Auto-generated method stub
        return null;
    }
    
    private final BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            showToast("Receive broadcast msg: "+action);
            if (ACTION_USB_PERMISSION.equals(action)) {
                synchronized (this) {
                    UsbAccessory accessory = UsbManager.getAccessory(intent);
                    if (intent.getBooleanExtra(
                            UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                        openAccessory(accessory);
                    } else {
                        Log.d(TAG, "permission denied for accessory "
                                + accessory);
                    }
                    mPermissionRequestPending = false;
                }
            } else if (UsbManager.ACTION_USB_ACCESSORY_DETACHED.equals(action)) {
                UsbAccessory accessory = UsbManager.getAccessory(intent);
                if (accessory != null && accessory.equals(mAccessory)) {
                    closeAccessory();
                }
            }
        }
    };
    
    private void openAccessory(UsbAccessory accessory) {
    	Log.d("openAccessory", "openAccessory");
    	showToast("Open accessory");
        mFileDescriptor = mUsbManager.openAccessory(accessory);
        if (mFileDescriptor != null) {
            mAccessory = accessory;
            FileDescriptor fd = mFileDescriptor.getFileDescriptor();
            mInputStream = new FileInputStream(fd);
            mOutputStream = new FileOutputStream(fd);
            Thread thread = new Thread(null, this, "LedMatrix");
            thread.start();
            Log.d(TAG, "accessory opened");
            //enableControls(true);
        } else {
            Log.d(TAG, "accessory open fail");
        }
    }
    
    private void closeAccessory() {
        //enableControls(false);
    	showToast("Close accessory");
        try {
            if (mFileDescriptor != null) {
                mFileDescriptor.close();
            }
        } catch (IOException e) {
        } finally {
            mFileDescriptor = null;
            mAccessory = null;
        }
    }
    
    @Override
    public void onCreate() {
    	
    	Log.d(TAG, "onCreate");
    	
    	showToast("Starting service");
    	
    	mVisualizerView = VisualizerView.getInstance();
    	
        mVisualizer = new Visualizer(0);
        
        mVisualizer.setCaptureSize(512);

        mVisualizer.setDataCaptureListener(new Visualizer.OnDataCaptureListener() {

            public void onWaveFormDataCapture(Visualizer visualizer, byte[] bytes, int samplingRate) {
            }

            public void onFftDataCapture(Visualizer visualizer, byte[] bytes,
                    int samplingRate) {
                //Log.d(TAG, "Captured " + bytes.length);
                updateMatrix(bytes);
            }

        }, Visualizer.getMaxCaptureRate(), false, true);
        
        mVisualizer.setEnabled(true);
        
        mUsbManager = UsbManager.getInstance(this);
		mPermissionIntent = PendingIntent.getBroadcast(this, 0, new Intent(
				ACTION_USB_PERMISSION), 0);
		IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
		filter.addAction(UsbManager.ACTION_USB_ACCESSORY_DETACHED);
		filter.addAction(UsbManager.ACTION_USB_ACCESSORY_ATTACHED);
		registerReceiver(mUsbReceiver, filter);

    }

	public void showToast(String msg){
    	Context context = getApplicationContext();
    	CharSequence text = msg;
    	int duration = Toast.LENGTH_SHORT;

    	Toast toast = Toast.makeText(context, text, duration);
    	toast.show();
    }

    @Override
    public void run() {
        // TODO Auto-generated method stub
        
    }
    private byte[] mBytes;
    /*private VisualizerView mVisualizerView;*/
    
    private void updateMatrix(byte[] bytes){
        mBytes = bytes;
        
        mVisualizerView = VisualizerView.getInstance();
        
        if (mVisualizerView.isShown()){
        	mVisualizerView.updateVisualizer(bytes);
        }
        
        ArrayList<Double> mClearDouble = new ArrayList<Double>();
        
        for (int i = 2; i <= 66; i = i + 2) {

            mClearDouble.add((double) 10
                    * Math.log((mBytes[i] * mBytes[i]) + (mBytes[i + 1] * mBytes[i + 1])));
        }
        
        byte[] frame;
        frame = new byte[128];
        Arrays.fill(frame, (byte) 0);

        for (int i = 0; i < mClearDouble.size() - 1; i++) {

            int piece_h = (int) Math.ceil(128 / 16);

            int num_pieces = (int) Math.ceil(mClearDouble.get(i) / piece_h);
            int color = 0;

            for (int j = 16; j >= 1; j--) {
                
                if (j <= num_pieces){

                    if (j >= 15) {
                        color = 2;
                    } else if (j >= 12) {
                        color = 3;
                    } else {
                        color = 1;
                    }
                }else{
                    color = 0;
                }
                
                int z = 16 - j;
                
                //todo: filling the frame
                frame[i * 4 + (int) z/4] = (byte) (frame[i * 4 + (int) z/4] | (color << (z % 4) * 2));
            }
        }
        
        //Log.d("frame", "frame: " + LedMatrixActivity.getHex(frame));
        
        sendCommand(frame);
    }
    
    public void sendCommand(byte[] buffer) {
        if (mOutputStream != null) {
            try {
                mOutputStream.write(buffer);
            } catch (IOException e) {
                Log.e(TAG, "write failed", e);
            }
        }
    }
    
    @Override
    public void onDestroy() {
        unregisterReceiver(mUsbReceiver);
        super.onDestroy();
    }

}

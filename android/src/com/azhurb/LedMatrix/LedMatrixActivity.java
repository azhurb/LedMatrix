/*
 * Copyright (C) 2010 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.azhurb.LedMatrix;

import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import android.app.Activity;
import android.app.PendingIntent;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;

import com.azhurb.LedMatrix.R;
import android.media.audiofx.Visualizer;
import android.media.MediaPlayer;
import android.media.AudioManager;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Button;
import android.widget.Toast;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.view.View;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.util.Log;
import android.view.ViewGroup;

import com.android.future.usb.*;
//import com.android.future.usb.UsbAccessory;
//import com.android.future.usb.UsbManager;

public class LedMatrixActivity extends Activity implements Runnable {
    private static final String TAG = "LedMatrix";
    
    

	UsbAccessory mAccessory;
	ParcelFileDescriptor mFileDescriptor;
	FileInputStream mInputStream;
	FileOutputStream mOutputStream;

    private static final String ACTION_USB_PERMISSION = "com.azhurb.LedMatrix.action.USB_PERMISSION";
    
    /*private UsbManager mUsbManager;
	private PendingIntent mPermissionIntent;
	private boolean mPermissionRequestPending;
*/
    private MediaPlayer mMediaPlayer;
    /*private Visualizer mVisualizer;*/

    private LinearLayout mLinearLayout;
    private VisualizerView mVisualizerView;
    private TextView mStatusTextView;
    private Button mButtonPlay;
    
    
    /*private final BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			showToast("Activity action: " + action);
			if (ACTION_USB_PERMISSION.equals(action)) {
				synchronized (this) {
					UsbAccessory accessory = UsbManager.getAccessory(intent);
					if (intent.getBooleanExtra(
							UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
						//openAccessory(accessory);
					} else {
						Log.d(TAG, "permission denied for accessory "
								+ accessory);
					}
					//mPermissionRequestPending = false;
				}
			} else if (UsbManager.ACTION_USB_ACCESSORY_DETACHED.equals(action)) {
				UsbAccessory accessory = UsbManager.getAccessory(intent);
				if (accessory != null && accessory.equals(mAccessory)) {
					//closeAccessory();
				}
			}
		}
	};*/
	
	/*private void openAccessory(UsbAccessory accessory) {
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

		try {
			if (mFileDescriptor != null) {
				mFileDescriptor.close();
			}
		} catch (IOException e) {
		} finally {
			mFileDescriptor = null;
			mAccessory = null;
		}
	}*/

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setVolumeControlStream(AudioManager.STREAM_MUSIC);

        mStatusTextView = new TextView(this);

        mLinearLayout = new LinearLayout(this);
        mLinearLayout.setOrientation(LinearLayout.VERTICAL);
        mLinearLayout.addView(mStatusTextView);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        setContentView(mLinearLayout);
        // setContentView(R.layout.main);

        mMediaPlayer = MediaPlayer.create(this, R.raw.test_cbr);
        Log.d(TAG, "MediaPlayer audio session ID: " + mMediaPlayer.getAudioSessionId());
        mMediaPlayer.setLooping(true);

        mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            public void onCompletion(MediaPlayer mediaPlayer) {
                // mVisualizer.setEnabled(false);
            }
        });

        mButtonPlay = new Button(this);
        mLinearLayout.addView(mButtonPlay);
        mButtonPlay.setText("Play sample");
        mStatusTextView.setText("Capturing audio out...");
        //setupVisualizerFxAndUI(true);
        //mVisualizer.setEnabled(true);

        mButtonPlay.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                if (mVisualizerView != null) {
                    //mVisualizer.setEnabled(false);
                }

                if (mMediaPlayer.isPlaying()) {
                    mMediaPlayer.pause();
                    mButtonPlay.setText("Play sample");
                    mStatusTextView.setText("Capturing audio out...");

                    //setupVisualizerFxAndUI(true);
                } else {
                    mMediaPlayer.start();
                    mButtonPlay.setText("Stop playing");
                    mStatusTextView.setText("Playing audio...");

                    //setupVisualizerFxAndUI(false);
                }

                //mVisualizer.setEnabled(true);
            }
        });

        mVisualizerView = new VisualizerView(this);
        mVisualizerView.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.FILL_PARENT,
                // (int)(VISUALIZER_HEIGHT_DIP *
                // getResources().getDisplayMetrics().density)));
                128));
        mLinearLayout.addView(mVisualizerView);/**/
        
        /*mUsbManager = UsbManager.getInstance(this);
		mPermissionIntent = PendingIntent.getBroadcast(this, 0, new Intent(
				ACTION_USB_PERMISSION), 0);
		IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
		filter.addAction(UsbManager.ACTION_USB_ACCESSORY_DETACHED);
		filter.addAction(UsbManager.ACTION_USB_ACCESSORY_ATTACHED);
		registerReceiver(mUsbReceiver, filter);
*/
		/*if (getLastNonConfigurationInstance() != null) {
			mAccessory = (UsbAccessory) getLastNonConfigurationInstance();
			//openAccessory(mAccessory);
			showToast(msg)
		}*/
        Log.d("Service", "before");
        startService(new Intent(this, LedMatrixService.class));
        Log.d("Service", "after");
    }
    
    public void showToast(String msg){
    	Context context = getApplicationContext();
    	CharSequence text = msg;
    	int duration = Toast.LENGTH_SHORT;

    	Toast toast = Toast.makeText(context, text, duration);
    	toast.show();
    }

    /*private void setupVisualizerFxAndUI(boolean captureAudioOut) {

        if (captureAudioOut) {
            mVisualizer = new Visualizer(0);
        } else {
            mVisualizer = new Visualizer(mMediaPlayer.getAudioSessionId());
        }

        Log.d("CaptureSizeRange", "SamplingRate: " + mVisualizer.getSamplingRate() + "; "
                + Visualizer.getCaptureSizeRange()[0] + " " + Visualizer.getCaptureSizeRange()[1]);

        // mVisualizer.setCaptureSize(Visualizer.getCaptureSizeRange()[1]);
        mVisualizer.setCaptureSize(512);

        mVisualizer.setDataCaptureListener(new Visualizer.OnDataCaptureListener() {

            public void onWaveFormDataCapture(Visualizer visualizer, byte[] bytes, int samplingRate) {
            }

            public void onFftDataCapture(Visualizer visualizer, byte[] bytes,
                    int samplingRate) {
                Log.d(TAG, "Captured " + bytes.length + ": " + getHex(bytes));
                mVisualizerView.updateVisualizer(bytes);
            }

        }, Visualizer.getMaxCaptureRate(), false, true);
    }*/
    
    static final String HEXES = "0123456789ABCDEF";

    public static String getHex(byte[] raw) {
        if (raw == null) {
            return null;
        }
        final StringBuilder hex = new StringBuilder(2 * raw.length);
        for (final byte b : raw) {
            hex.append(HEXES.charAt((b & 0xF0) >> 4))
                    .append(HEXES.charAt((b & 0x0F)));
        }
        return hex.toString();
    }
    
   /* @Override
	public Object onRetainNonConfigurationInstance() {
		if (mAccessory != null) {
			return mAccessory;
		} else {
			return super.onRetainNonConfigurationInstance();
		}
	}*/
    
    /*@Override
	public void onResume() {
		super.onResume();

		//Intent intent = getIntent();
		if (mInputStream != null && mOutputStream != null) {
			return;
		}

		UsbAccessory[] accessories = mUsbManager.getAccessoryList();
		UsbAccessory accessory = (accessories == null ? null : accessories[0]);
		if (accessory != null) {
			if (mUsbManager.hasPermission(accessory)) {
				openAccessory(accessory);
			} else {
				synchronized (mUsbReceiver) {
					if (!mPermissionRequestPending) {
						mUsbManager.requestPermission(accessory,
								mPermissionIntent);
						mPermissionRequestPending = true;
					}
				}
			}
		} else {
			Log.d(TAG, "mAccessory is null");
		}
	}*/

    @Override
    protected void onPause() {
        super.onPause();
        
        //closeAccessory();

        if (isFinishing() && mMediaPlayer != null) {
            //mVisualizer.release();
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
    }
    
    public void run() {
    	
    }
    
    /*public void sendCommand(byte[] buffer) {
	    if (mOutputStream != null) {
			try {
				mOutputStream.write(buffer);
			} catch (IOException e) {
				Log.e(TAG, "write failed", e);
			}
		}
	}*/
    
    /*@Override
	public void onDestroy() {
		unregisterReceiver(mUsbReceiver);
		super.onDestroy();
	}*/
}

class VisualizerView extends View {
    private byte[] mBytes;
    // private float[] mPoints;
    private Rect mRect = new Rect();
    
    private static VisualizerView instance;
    
    protected LedMatrixActivity mActivity;

    private Paint mForePaint = new Paint();

    public VisualizerView(LedMatrixActivity activity) {
        super(activity);
        mActivity = activity;
        instance = this;
        init();
    }
    
    public static VisualizerView getInstance(){
    	return instance;
    }

    private void init() {
        mBytes = null;

        mForePaint.setStrokeWidth(1f);
        mForePaint.setAntiAlias(true);
        // mForePaint.setColor(Color.rgb(0, 128, 255));
    }

    public void updateVisualizer(byte[] bytes) {
        mBytes = bytes;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (mBytes == null) {
            return;
        }

        // if (mPoints == null || mPoints.length < mBytes.length * 4) {
        // mPoints = new float[mBytes.length * 4];
        // }

        mRect.set(0, 0, getWidth(), getHeight());

        ArrayList<Double> mClearDouble = new ArrayList<Double>();

        for (int i = 2; i <= 66; i = i + 2) {

            mClearDouble.add((double) 10
                    * Math.log((mBytes[i] * mBytes[i]) + (mBytes[i + 1] * mBytes[i + 1])));
            // mClearDouble.add((double) 10 * Math.log(Math.sqrt((mBytes[i] *
            // mBytes[i]) + (mBytes[i+1] * mBytes[i+1]))));
            // mClearDouble.add((double) Math.sqrt((mBytes[i] * mBytes[i]) +
            // (mBytes[i+1] * mBytes[i+1])));
        }

        // Log.d("mClearDouble", "mClearDouble: " + mClearDouble.size() + "; " +
        // mClearDouble.toString());
        
        byte[] frame;
        frame = new byte[128];
        Arrays.fill(frame, (byte) 0);

        for (int i = 0; i < mClearDouble.size() - 1; i++) {

            int piece_h = (int) Math.ceil(mRect.height() / 16);

            int num_pieces = (int) Math.ceil(mClearDouble.get(i) / piece_h);

            // Log.d("num_pices", "" + num_pices + "; mRect.height() " +
            // mRect.height() + "; pice_h " + pice_h + "; mClearDouble.get(i) "
            // + mClearDouble.get(i));
            
            int color = 0;

            //todo: change direction from 0 to 15
            for (int j = 16; j >= 1; j--) {
            //for (int j = 0; j <= 15; j++) {
                
                if (j <= num_pieces){

                    if (j >= 15) {
                        mForePaint.setColor(Color.rgb(255, 0, 0));
                        color = 2;
                    } else if (j >= 12) {
                        mForePaint.setColor(Color.rgb(255, 128, 0));
                        color = 3;
                    } else {
                        mForePaint.setColor(Color.rgb(0, 255, 128));
                        color = 1;
                    }
    
                    canvas.drawRect(
                            mRect.width() * i / (mClearDouble.size() - 1),
                            mRect.height() - (int) ((j - 1) * piece_h) - 1,
                            (mRect.width() * i / (mClearDouble.size() - 1)) + 10,
                            mRect.height() - (int) (j * piece_h),
                            mForePaint);
                }else{
                    color = 0;
                }
                
                //int z = 16 - j;
                
                //todo: filling the frame
                //frame[i * 4 + (int) z/4] = (byte) (frame[i * 4 + (int) z/4] | (color << (z % 4) * 2));
            }
        }
        
        //Log.d("frame", "frame: " + LedMatrixActivity.getHex(frame));
        
        //if (mActivity != null) {
        	//mActivity.sendCommand(frame);
        //}
    }
}


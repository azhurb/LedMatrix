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

import com.android.future.usb.UsbAccessory;
import com.android.future.usb.UsbManager;

public class LedMatrixActivity extends Activity implements Runnable {
    private static final String TAG = "LedMatrix";
    
    private static final String ACTION_USB_PERMISSION = "com.google.android.DemoKit.action.USB_PERMISSION";

	private UsbManager mUsbManager;
	private PendingIntent mPermissionIntent;
	private boolean mPermissionRequestPending;

	UsbAccessory mAccessory;
	ParcelFileDescriptor mFileDescriptor;
	FileInputStream mInputStream;
	FileOutputStream mOutputStream;

    private MediaPlayer mMediaPlayer;
    private Visualizer mVisualizer;

    private LinearLayout mLinearLayout;
    private VisualizerView mVisualizerView;
    private TextView mStatusTextView;
    private Button mButtonPlay;
    
    private final BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
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
		mFileDescriptor = mUsbManager.openAccessory(accessory);
		if (mFileDescriptor != null) {
			mAccessory = accessory;
			FileDescriptor fd = mFileDescriptor.getFileDescriptor();
			mInputStream = new FileInputStream(fd);
			mOutputStream = new FileOutputStream(fd);
			Thread thread = new Thread(null, this, "DemoKit");
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
	}

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
        setupVisualizerFxAndUI(true);
        mVisualizer.setEnabled(true);

        mButtonPlay.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                if (mVisualizerView != null) {
                    mVisualizer.setEnabled(false);
                }

                if (mMediaPlayer.isPlaying()) {
                    mMediaPlayer.pause();
                    mButtonPlay.setText("Play sample");
                    mStatusTextView.setText("Capturing audio out...");

                    setupVisualizerFxAndUI(true);
                } else {
                    mMediaPlayer.start();
                    mButtonPlay.setText("Stop playing");
                    mStatusTextView.setText("Playing audio...");

                    setupVisualizerFxAndUI(false);
                }

                mVisualizer.setEnabled(true);
            }
        });

        mVisualizerView = new VisualizerView(this);
        mVisualizerView.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.FILL_PARENT,
                // (int)(VISUALIZER_HEIGHT_DIP *
                // getResources().getDisplayMetrics().density)));
                128));
        mLinearLayout.addView(mVisualizerView);
        
        mUsbManager = UsbManager.getInstance(this);
		mPermissionIntent = PendingIntent.getBroadcast(this, 0, new Intent(
				ACTION_USB_PERMISSION), 0);
		IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
		filter.addAction(UsbManager.ACTION_USB_ACCESSORY_DETACHED);
		registerReceiver(mUsbReceiver, filter);

		if (getLastNonConfigurationInstance() != null) {
			mAccessory = (UsbAccessory) getLastNonConfigurationInstance();
			openAccessory(mAccessory);
		}
    }

    private void setupVisualizerFxAndUI(boolean captureAudioOut) {

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
    }
    
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
    
    @Override
	public Object onRetainNonConfigurationInstance() {
		if (mAccessory != null) {
			return mAccessory;
		} else {
			return super.onRetainNonConfigurationInstance();
		}
	}
    
    @Override
	public void onResume() {
		super.onResume();

		Intent intent = getIntent();
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
	}

    @Override
    protected void onPause() {
        super.onPause();
        
        closeAccessory();

        if (isFinishing() && mMediaPlayer != null) {
            mVisualizer.release();
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
    }
    
    public void run() {
    	
    }
    
    public void sendCommand(byte command, byte target, int value) {
		byte[] buffer = new byte[3];
		if (value > 255)
			value = 255;

		buffer[0] = command;
		buffer[1] = target;
		buffer[2] = (byte) value;
		if (mOutputStream != null && buffer[1] != -1) {
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

class VisualizerView extends View {
    private byte[] mBytes;
    // private float[] mPoints;
    private Rect mRect = new Rect();
    
    private LedMatrixActivity mActivity;

    private Paint mForePaint = new Paint();

    public VisualizerView(Context context) {
        super(context);
        init();
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

        for (int i = 0; i < mClearDouble.size() - 1; i++) {

            int pice_h = (int) Math.ceil(mRect.height() / 16);

            int num_pices = (int) Math.ceil(mClearDouble.get(i) / pice_h);

            // Log.d("num_pices", "" + num_pices + "; mRect.height() " +
            // mRect.height() + "; pice_h " + pice_h + "; mClearDouble.get(i) "
            // + mClearDouble.get(i));

            for (int j = 1; j <= num_pices; j++) {

                if (j >= 15) {
                    mForePaint.setColor(Color.rgb(255, 0, 0));
                } else if (j >= 12) {
                    mForePaint.setColor(Color.rgb(255, 128, 0));
                } else {
                    mForePaint.setColor(Color.rgb(0, 255, 128));
                }

                canvas.drawRect(
                        mRect.width() * i / (mClearDouble.size() - 1),
                        // mRect.height(),
                        mRect.height() - (int) ((j - 1) * pice_h) - 1,
                        (mRect.width() * i / (mClearDouble.size() - 1)) + 10,
                        // (float) (mRect.height() - (mClearDouble.get(i)) *
                        // mRect.height() / 128),
                        mRect.height() - (int) (j * pice_h),
                        mForePaint);

            }
            
            mActivity.sendCommand((byte) 1, (byte) 1, (int) 1);
        }
    }
}

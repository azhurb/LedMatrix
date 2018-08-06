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

public class LedMatrixActivity extends Activity implements Runnable {

    private static final String TAG = "LedMatrix";

    UsbAccessory mAccessory;
    ParcelFileDescriptor mFileDescriptor;
    FileInputStream mInputStream;
    FileOutputStream mOutputStream;

    private static final String ACTION_USB_PERMISSION = "com.azhurb.LedMatrix.action.USB_PERMISSION";

    private MediaPlayer mMediaPlayer;

    private LinearLayout mLinearLayout;
    private VisualizerView mVisualizerView;
    private TextView mStatusTextView;
    private Button mButtonPlay;

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

        mButtonPlay.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                if (mMediaPlayer.isPlaying()) {
                    mMediaPlayer.pause();
                    mButtonPlay.setText("Play sample");
                    mStatusTextView.setText("Capturing audio out...");
                } else {
                    mMediaPlayer.start();
                    mButtonPlay.setText("Stop playing");
                    mStatusTextView.setText("Playing audio...");
                }
            }
        });

        mVisualizerView = new VisualizerView(this);
        mVisualizerView.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.FILL_PARENT,
                128));
        mLinearLayout.addView(mVisualizerView);

        Log.d("Service", "before");
        startService(new Intent(this, LedMatrixService.class));
        Log.d("Service", "after");
    }

    public void showToast(String msg) {
        Context context = getApplicationContext();
        CharSequence text = msg;
        int duration = Toast.LENGTH_SHORT;

        Toast toast = Toast.makeText(context, text, duration);
        toast.show();
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
    protected void onPause() {
        super.onPause();

        if (isFinishing() && mMediaPlayer != null) {
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
    }

    public void run() {

    }
}

class VisualizerView extends View {

    private byte[] mBytes;

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

    public static VisualizerView getInstance() {
        return instance;
    }

    private void init() {
        mBytes = null;

        mForePaint.setStrokeWidth(1f);
        mForePaint.setAntiAlias(true);
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

        mRect.set(0, 0, getWidth(), getHeight());

        ArrayList<Double> mClearDouble = new ArrayList<Double>();

        for (int i = 2; i <= 66; i = i + 2) {

            mClearDouble.add((double) 10
                    * Math.log((mBytes[i] * mBytes[i]) + (mBytes[i + 1] * mBytes[i + 1])));
        }

        byte[] frame;
        frame = new byte[128];
        Arrays.fill(frame, (byte) 0);

        for (int i = 0; i < mClearDouble.size() - 1; i++) {

            int piece_h = (int) Math.ceil(mRect.height() / 16);

            int num_pieces = (int) Math.ceil(mClearDouble.get(i) / piece_h);

            int color = 0;

            for (int j = 16; j >= 1; j--) {

                if (j <= num_pieces) {

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
                } else {
                    color = 0;
                }
            }
        }
    }
}


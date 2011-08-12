package com.azhurb.LedMatrix;

import java.util.ArrayList;

import android.app.Activity;
import android.os.Bundle;
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
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.util.Log;
import android.view.ViewGroup;

public class LedMatrixActivity extends Activity{
	private static final String TAG = "LedMatrix";
	
	private MediaPlayer mMediaPlayer;
	private Visualizer mVisualizer;
	
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
		//setContentView(R.layout.main);
		
		mMediaPlayer = MediaPlayer.create(this, R.raw.test_cbr);
		Log.d(TAG, "MediaPlayer audio session ID: " + mMediaPlayer.getAudioSessionId());
		mMediaPlayer.setLooping(true);
		
		mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
			public void onCompletion(MediaPlayer mediaPlayer) {
				//mVisualizer.setEnabled(false);
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
				
				if (mVisualizerView != null){
					mVisualizer.setEnabled(false);
				}
				
				if(mMediaPlayer.isPlaying()){
					mMediaPlayer.pause();
					mButtonPlay.setText("Play sample");
					mStatusTextView.setText("Capturing audio out...");
					
					setupVisualizerFxAndUI(true);
				}else{
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
                //(int)(VISUALIZER_HEIGHT_DIP * getResources().getDisplayMetrics().density)));
                128));
        mLinearLayout.addView(mVisualizerView);
    }
    
    private void setupVisualizerFxAndUI(boolean captureAudioOut) {
    	
    	if(captureAudioOut){
        	mVisualizer = new Visualizer(0);
        }else{
        	mVisualizer = new Visualizer(mMediaPlayer.getAudioSessionId());
        }
        
        Log.d("CaptureSizeRange", "SamplingRate: " + mVisualizer.getSamplingRate() + "; " + Visualizer.getCaptureSizeRange()[0] + " " + Visualizer.getCaptureSizeRange()[1]);
        
        mVisualizer.setCaptureSize(Visualizer.getCaptureSizeRange()[1]);
        
        mVisualizer.setDataCaptureListener(new Visualizer.OnDataCaptureListener() {

        	public void onWaveFormDataCapture(Visualizer visualizer, byte[] bytes,int samplingRate) {}
            
            public void onFftDataCapture(Visualizer visualizer, byte[] bytes,
                    int samplingRate) {
            	Log.d(TAG, "Captured "  + bytes.length + ": " + getHex(bytes));
                mVisualizerView.updateVisualizer(bytes);
            }
            
        }, Visualizer.getMaxCaptureRate(), false, true);
    }
    
    /*public void run() {
    	
    }*/
    
    static final String HEXES = "0123456789ABCDEF";
	public static String getHex( byte [] raw ) {
	  if ( raw == null ) {
	    return null;
	  }
	  final StringBuilder hex = new StringBuilder( 2 * raw.length );
	  for ( final byte b : raw ) {
	    hex.append(HEXES.charAt((b & 0xF0) >> 4))
	       .append(HEXES.charAt((b & 0x0F)));
	  }
	  return hex.toString();
	}
    
    @Override
    protected void onPause() {
        super.onPause();

        if (isFinishing() && mMediaPlayer != null) {
            mVisualizer.release();
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
    }
}

class VisualizerView extends View {
    private byte[] mBytes;
    //private float[] mPoints;
    private Rect mRect = new Rect();

    private Paint mForePaint = new Paint();

    public VisualizerView(Context context) {
        super(context);
        init();
    }

    private void init() {
        mBytes = null;

        mForePaint.setStrokeWidth(1f);
        mForePaint.setAntiAlias(true);
        //mForePaint.setColor(Color.rgb(0, 128, 255));
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

        //if (mPoints == null || mPoints.length < mBytes.length * 4) {
        //    mPoints = new float[mBytes.length * 4];
        //}
        
        mRect.set(0, 0, getWidth(), getHeight());

        ArrayList<Double> mClearDouble = new ArrayList<Double>();
        
        for (int i = 2; i <= 66; i=i+2) {
        	
        	mClearDouble.add((double) 10 * Math.log((mBytes[i] * mBytes[i]) + (mBytes[i+1] * mBytes[i+1])));
        	//mClearDouble.add((double) 10 * Math.log(Math.sqrt((mBytes[i] * mBytes[i]) + (mBytes[i+1] * mBytes[i+1]))));
        	//mClearDouble.add((double) Math.sqrt((mBytes[i] * mBytes[i]) + (mBytes[i+1] * mBytes[i+1])));
        }
        
        //Log.d("mClearDouble", "mClearDouble: " + mClearDouble.size() + "; " + mClearDouble.toString());
        
        for (int i = 0; i < mClearDouble.size() - 1; i++) {
        	
        	int pice_h = (int) Math.ceil(mRect.height() / 16);
        	
        	int num_pices = (int) Math.ceil(mClearDouble.get(i) / pice_h);
        	
        	//Log.d("num_pices", "" + num_pices + "; mRect.height() " + mRect.height() + "; pice_h " + pice_h + "; mClearDouble.get(i) " + mClearDouble.get(i));
        	
        	for (int j = 1; j <= num_pices; j++){
        		
        		
        		if (j >= 15){
        			mForePaint.setColor(Color.rgb(255, 0, 0));
        		}else if (j >= 12){
        			mForePaint.setColor(Color.rgb(255, 128, 0));
        		}else{
        			mForePaint.setColor(Color.rgb(0, 255, 128));
        		}
        		
        		canvas.drawRect(
    		    		mRect.width() * i / (mClearDouble.size() - 1),
    		    		//mRect.height(),
    		    		mRect.height() - (int) ((j - 1) * pice_h) - 1,
    		    		(mRect.width() * i / (mClearDouble.size() - 1)) + 10,
    		    		//(float) (mRect.height() - (mClearDouble.get(i)) * mRect.height() / 128),
    		    		mRect.height() - (int) (j * pice_h),
    		    		mForePaint);
        		
        	}
		}
    }
}
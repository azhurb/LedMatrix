package com.azhurb.LedMatrix;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.TextView;

public class LedMatrixPhone extends BaseActivity implements OnClickListener {
	static final String TAG = "LedMatrixPhone";
	/** Called when the activity is first created. */
	TextView mInputLabel;
	TextView mOutputLabel;
	LinearLayout mInputContainer;
	LinearLayout mOutputContainer;
	Drawable mFocusedTabImage;
	Drawable mNormalTabImage;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}


	public void onClick(View v) {

	}

}
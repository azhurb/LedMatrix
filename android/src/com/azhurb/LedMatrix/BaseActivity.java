package com.azhurb.LedMatrix;

import android.os.Bundle;

public class BaseActivity extends LedMatrixActivity {

	//private InputController mInputController;

	public BaseActivity() {
		super();
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//if (mAccessory != null) {
			//showControls();
		//} else {
			//hideControls();
		//}
	}
}
package com.example.apollo_1;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.ImageView;

public class Apollo extends Fragment implements OnClickListener {
	static Uart uart;
	ImageView heart, step, uv;

	public Apollo() {
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.apollo, container, false);
		getActivity().setTitle("Apollo");
		Log.v("test", "Apollo");
		uart = new Uart();
		heart = (ImageView) rootView.findViewById(R.id.heart_im);
		step = (ImageView) rootView.findViewById(R.id.step_im);
		uv = (ImageView) rootView.findViewById(R.id.uv_im);

		heart.setOnClickListener(Apollo.this);
		step.setOnClickListener(Apollo.this);
		uv.setOnClickListener(Apollo.this);
		return rootView;
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		Log.v("test", "" + v.getId());
		switch (v.getId()) {
		case R.id.heart_im:
			MainActivity.mService.writeRXCharacteristic(uart.hex2Byte("00010001"));
			break;
		case R.id.step_im:
			MainActivity.mService.writeRXCharacteristic(uart.hex2Byte("00010002"));
			break;
		case R.id.uv_im:
			MainActivity.mService.writeRXCharacteristic(uart.hex2Byte("00010003"));
			break;
		}
	}
}
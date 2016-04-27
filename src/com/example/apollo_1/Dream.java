package com.example.apollo_1;

import com.example.seekarc_library.SeekArc;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

public class Dream extends Fragment {
	Activity activity;
	static Uart uart;
	static SeekArc progBar;
	static TextView bat;
	ImageView iv;
	static Boolean maonoff = true;
	
	public Dream(Context context) {
		activity = (Activity) context;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.dream, container, false);
		activity.setTitle("Dream");
		uart = new Uart();
		progBar = (SeekArc) rootView.findViewById(R.id.seekArcComplete);
		bat = (TextView) rootView.findViewById(R.id.battery);
		iv = (ImageView) rootView.findViewById(R.id.onoff);

		iv.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if (maonoff) {
					iv.setImageResource(R.drawable.off);
					maonoff = false;
					MainActivity.mService.writeRXCharacteristic(uart.hex2Byte("40040000"));
				} else {
					iv.setImageResource(R.drawable.on);
					maonoff = true;
					MainActivity.mService.writeRXCharacteristic(uart.hex2Byte("40050000"));
				}
			}
		});
		return rootView;
	}
}
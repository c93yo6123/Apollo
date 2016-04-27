package com.example.apollo_1;

import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;

public class Apollo extends Fragment implements OnClickListener {
	Activity activity;
	static Uart uart;
	ImageView heart, step, uv;
	static TextView tv_heart, tv_step, tv_uv;
	int Steps = 0;
	float Cal = 0;

	public Apollo(Context context) {
		activity = (Activity) context;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.apollo, container, false);
		activity.setTitle("Apollo");
		// Log.v("test", "Apollo");
		uart = new Uart();
		heart = (ImageView) rootView.findViewById(R.id.heart_im);
		step = (ImageView) rootView.findViewById(R.id.step_im);
		uv = (ImageView) rootView.findViewById(R.id.uv_im);

		heart.setImageResource(R.drawable.time);

		tv_heart = (TextView) rootView.findViewById(R.id.heart_msg);
		tv_step = (TextView) rootView.findViewById(R.id.step_msg);
		tv_uv = (TextView) rootView.findViewById(R.id.uv_msg);

		heart.setOnClickListener(Apollo.this);
		step.setOnClickListener(Apollo.this);
		uv.setOnClickListener(Apollo.this);

		Timer timer = new Timer(true);
		timer.schedule(new MyTimerTask(), 2000, 2000);
		return rootView;
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
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

	public class MyTimerTask extends TimerTask {
		public void run() {
			activity.runOnUiThread(new Runnable() {

				@Override
				public void run() {
					// TODO Auto-generated method stub
					// Calendar calendar = Calendar.getInstance();
					// int hour = calendar.get(Calendar.HOUR_OF_DAY);
					// int min = calendar.get(Calendar.MINUTE);
					// int sec = calendar.get(Calendar.SECOND);
					// Apollo.tv_heart.setText(fill_zero(hour) + ":" +
					// fill_zero(min) + ":" + fill_zero(sec));
					tv_step.setText(MainActivity.BLE_Steps + " ¤j¥d");
				}
			});
		}
	};

	String fill_zero(int number) {
		return ((number < 10) ? "0" : "") + number;
	}
}
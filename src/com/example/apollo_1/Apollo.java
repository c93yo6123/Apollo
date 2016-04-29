package com.example.apollo_1;

import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v4.app.Fragment;
import android.telephony.PhoneStateListener;
import android.telephony.SmsMessage;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

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

//		TelephonyManager TelephonyMgr = (TelephonyManager) activity.getSystemService(Context.TELEPHONY_SERVICE);
//		TelephonyMgr.listen(new TeleListener(), PhoneStateListener.LISTEN_CALL_STATE);

		Timer timer = new Timer(true);
		timer.schedule(new MyTimerTask(), 2000, 2000);

		return rootView;
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.heart_im:
			MainActivity.mService.writeRXCharacteristic(uart.hex2Byte("00010004"));
			break;
		case R.id.step_im:
			MainActivity.mService.writeRXCharacteristic(uart.hex2Byte("00010002"));
			break;
		case R.id.uv_im:
			MainActivity.mService.writeRXCharacteristic(uart.hex2Byte("00010003"));
			break;
		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
//		try {
//			activity.unregisterReceiver(SMSReceiver);
//		} catch (Exception ignore) {
//		}

	}

	final static BroadcastReceiver SMSReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent.getAction().equals("android.provider.Telephony.SMS_RECEIVED")) {
				// 當接受到簡訊時
				MainActivity.mService.writeRXCharacteristic(uart.hex2Byte("00010005"));
				// Bundle bundle = intent.getExtras();
				// if (bundle != null) {
				// Object[] myObjects = (Object[]) bundle.get("pdus");
				// // protocol description units 標準協定
				// // PDU可以是文字或多媒體，用Object陣列全包下來
				//
				// SmsMessage[] messages = new SmsMessage[myObjects.length];
				//
				// for (int i = 0; i < myObjects.length; i++) {
				//
				// messages[i] = SmsMessage.createFromPdu((byte[])
				// myObjects[i]);
				// // 需將PDU格式轉成byte陣列
				// // 將PDU格式的資料轉為smsMessage的格式
				// // 由於簡訊長度的限制可能不止一封
				// }
				//
				// StringBuilder sb = new StringBuilder();
				// for (SmsMessage tempMessage : messages) {
				// sb.append("收到來自：\n");
				// sb.append(tempMessage.getDisplayOriginatingAddress() +
				// "\n\n");
				// sb.append("內容為：\n");
				// sb.append(tempMessage.getDisplayMessageBody());
				// }
				// Toast.makeText(context, sb.toString(),
				// Toast.LENGTH_LONG).show();
				// }
			}
		}
	};

//	class TeleListener extends PhoneStateListener {
//		public void onCallStateChanged(int state, String incomingNumber) {
//			super.onCallStateChanged(state, incomingNumber);
//			switch (state) {
//			// case TelephonyManager.CALL_STATE_IDLE:
//			// // CALL_STATE_IDLE;
//			// Toast.makeText(getApplicationContext(), "CALL_STATE_IDLE",
//			// Toast.LENGTH_LONG).show();
//			// break;
//			// case TelephonyManager.CALL_STATE_OFFHOOK:
//			// // CALL_STATE_OFFHOOK;
//			// Toast.makeText(getApplicationContext(), "CALL_STATE_OFFHOOK",
//			// Toast.LENGTH_LONG).show();
//			// break;
//			case TelephonyManager.CALL_STATE_RINGING:
//				// CALL_STATE_RINGING
//				MainActivity.mService.writeRXCharacteristic(uart.hex2Byte("00010005"));
//				// Toast.makeText(getApplicationContext(), incomingNumber,
//				// Toast.LENGTH_LONG).show();
//				// Toast.makeText(getApplicationContext(), "CALL_STATE_RINGING",
//				// Toast.LENGTH_LONG).show();
//				break;
//			default:
//				break;
//			}
//		}
//	}

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
					tv_step.setText((float) MainActivity.BLE_Steps / 20 + " 大卡");
				}
			});
		}
	};

	String fill_zero(int number) {
		return ((number < 10) ? "0" : "") + number;
	}
}
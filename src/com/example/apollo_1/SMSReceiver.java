package com.example.apollo_1;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.widget.Toast;

public class SMSReceiver extends BroadcastReceiver {
	@Override
	public void onReceive(Context context, Intent intent) {
		if (intent.getAction().equals("android.provider.Telephony.SMS_RECEIVED")) {
			// 當接受到簡訊時
			Bundle bundle = intent.getExtras();
			if (bundle != null) {
				Object[] myObjects = (Object[]) bundle.get("pdus");
				// protocol description units 標準協定
				// PDU可以是文字或多媒體，用Object陣列全包下來

				SmsMessage[] messages = new SmsMessage[myObjects.length];

				for (int i = 0; i < myObjects.length; i++) {

					messages[i] = SmsMessage.createFromPdu((byte[]) myObjects[i]);
					// 需將PDU格式轉成byte陣列
					// 將PDU格式的資料轉為smsMessage的格式
					// 由於簡訊長度的限制可能不止一封
				}

				StringBuilder sb = new StringBuilder();
				for (SmsMessage tempMessage : messages) {
					sb.append("收到來自：\n");
					sb.append(tempMessage.getDisplayOriginatingAddress() + "\n\n");
					sb.append("內容為：\n");
					sb.append(tempMessage.getDisplayMessageBody());
				}
				Toast.makeText(context, sb.toString(), Toast.LENGTH_LONG).show();
			}
		}
	}
}
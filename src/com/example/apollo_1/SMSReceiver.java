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
			// ������²�T��
			Bundle bundle = intent.getExtras();
			if (bundle != null) {
				Object[] myObjects = (Object[]) bundle.get("pdus");
				// protocol description units �зǨ�w
				// PDU�i�H�O��r�Φh�C��A��Object�}�C���]�U��

				SmsMessage[] messages = new SmsMessage[myObjects.length];

				for (int i = 0; i < myObjects.length; i++) {

					messages[i] = SmsMessage.createFromPdu((byte[]) myObjects[i]);
					// �ݱNPDU�榡�নbyte�}�C
					// �NPDU�榡������ରsmsMessage���榡
					// �ѩ�²�T���ת�����i�ण��@��
				}

				StringBuilder sb = new StringBuilder();
				for (SmsMessage tempMessage : messages) {
					sb.append("����ӦۡG\n");
					sb.append(tempMessage.getDisplayOriginatingAddress() + "\n\n");
					sb.append("���e���G\n");
					sb.append(tempMessage.getDisplayMessageBody());
				}
				Toast.makeText(context, sb.toString(), Toast.LENGTH_LONG).show();
			}
		}
	}
}
package com.example.apollo_1;

import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

public class MainActivity extends ActionBarActivity {

	/**
	 * The {@link android.support.v4.view.PagerAdapter} that will provide
	 * fragments for each of the sections. We use a {@link FragmentPagerAdapter}
	 * derivative, which will keep every loaded fragment in memory. If this
	 * becomes too memory intensive, it may be best to switch to a
	 * {@link android.support.v4.app.FragmentStatePagerAdapter}.
	 */
	// SectionsPagerAdapter mSectionsPagerAdapter;

	/**
	 * The {@link ViewPager} that will host the section contents.
	 */
	// ViewPager mViewPager;
	// public final static String SMS_RECEIVED =
	// "android.intent.action.PHONE_STATE";
	private static final int REQUEST_SELECT_DEVICE = 1;
	private static final int REQUEST_ENABLE_BT = 2;
	private static final int UART_PROFILE_READY = 10;
	public static final String TAG = "Apollo";
	private static final int UART_PROFILE_CONNECTED = 20;
	private static final int UART_PROFILE_DISCONNECTED = 21;
	private static final int STATE_OFF = 10;
	private int mState = UART_PROFILE_DISCONNECTED;
	public static UartService mService = null;
	private BluetoothDevice mDevice = null;
	private BluetoothAdapter mBtAdapter = null;

	static String data;
	String connmenu;
	static Uart uart;
	Calendar calendar;
	// 年 月 分 日 時 秒
	int[][] format = { { 8, 8, 8, 8 }, { 6, 4, 6, 5, 5, 6 }, { 8, 8, 8, 8 }, { 8, 8, 8, 8 } };
	static String header;
	Boolean tx = false;

	Fragment device_layout;
	Dream dream;
	Apollo apollo;
	static int BLE_Steps = 0;

	static int mProgressStatus = 0;
	String name, address, device;
	ProgressDialog PDialog;
	BluetoothAdapter mBluetoothAdapter;

	String BLE_action = "";
	Boolean fragment_show = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
		mBluetoothAdapter = bluetoothManager.getAdapter();

		mBtAdapter = BluetoothAdapter.getDefaultAdapter();
		if (mBtAdapter == null) {
			Toast.makeText(this, "Bluetooth is not available", Toast.LENGTH_LONG).show();
			finish();
			return;
		}

		if (!mBtAdapter.isEnabled()) {
			Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
			startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
		}

		uart = new Uart();
		dream = new Dream(MainActivity.this);
		apollo = new Apollo(MainActivity.this);

		getSupportFragmentManager().beginTransaction().add(R.id.pager, dream).commit();
		getSupportFragmentManager().beginTransaction().hide(dream).commit();
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		getSupportFragmentManager().beginTransaction().add(R.id.pager, apollo).commit();
		getSupportFragmentManager().beginTransaction().hide(apollo).commit();

		setTitle("Devices");
		connmenu = getResources().getString(R.string.connect);

		service_init();

		while (true) {
			if (mBtAdapter.isEnabled()) {
				Intent newIntent = new Intent(MainActivity.this, DeviceListActivity.class);
				startActivityForResult(newIntent, REQUEST_SELECT_DEVICE);
				break;
			}
		}

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		if (connmenu.equals(getResources().getString(R.string.connect)))
			getMenuInflater().inflate(R.menu.conn, menu);
		else
			getMenuInflater().inflate(R.menu.disconn, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.

		if (!mBtAdapter.isEnabled()) {
			Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
			startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
		} else {
			if (item.getTitle().toString().equals(getResources().getString(R.string.connect))) {
				Intent newIntent = new Intent(MainActivity.this, DeviceListActivity.class);
				startActivityForResult(newIntent, REQUEST_SELECT_DEVICE);
			} else {
				if (mDevice != null) {
					DeviceListActivity.saveData("", "", "");
					if (mService != null)
						mService.disconnect();
				}
			}
		}
		return super.onOptionsItemSelected(item);
	}

	// UART service connected/disconnected
	private ServiceConnection mServiceConnection = new ServiceConnection() {
		public void onServiceConnected(ComponentName className, IBinder rawBinder) {
			mService = ((UartService.LocalBinder) rawBinder).getService();

			if (!mService.initialize()) {
				finish();
			}

		}

		public void onServiceDisconnected(ComponentName classname) {
			mService = null;
		}
	};

	private final BroadcastReceiver UARTStatusChangeReceiver = new BroadcastReceiver() {

		public void onReceive(Context context, Intent intent) {
			BLE_action = intent.getAction();

			// *********************//
			if (BLE_action.equals(UartService.ACTION_GATT_CONNECTED)) {
				connmenu = getResources().getString(R.string.disconnect);
				invalidateOptionsMenu();
				mState = UART_PROFILE_CONNECTED;
			}

			// *********************//
			if (BLE_action.equals(UartService.ACTION_GATT_DISCONNECTED)) {
				connmenu = getResources().getString(R.string.connect);
				invalidateOptionsMenu();
				mState = UART_PROFILE_DISCONNECTED;
				tx = false;
				setTitle("Devices");

				if (fragment_show) {
					getSupportFragmentManager().beginTransaction().hide(device_layout).commit();
					fragment_show = false;
				}

				DeviceListActivity.readData();

				if (PDialog != null) {
					PDialog.dismiss();
					PDialog = null;
				}
			}

			// *********************//
			if (BLE_action.equals(UartService.ACTION_GATT_SERVICES_DISCOVERED)) {
				mService.enableTXNotification();
			}
			// *********************//
			if (BLE_action.equals(UartService.ACTION_DATA_AVAILABLE)) {

				final byte[] txValue = intent.getByteArrayExtra(UartService.EXTRA_DATA);

				runOnUiThread(new Runnable() {
					public void run() {
						try {
							if (tx) {
								String[] s = uart.byte2data(txValue, format);
								header = s[0].toUpperCase();
								data = s[2];

								// Log.v("test", header + " " + data);
								if (header.equals("00000014") && device.equals("Headset")) {
									// Log.v("test", data);
									Dream.progBar.setProgress(Integer.parseInt(data));
									Dream.bat.setText(data + "%");
								}

								if (header.equals("FF000000") && data.equals("1")) {
									getSupportFragmentManager().beginTransaction().show(apollo).commit();
									device_layout = apollo;
									setTitle("apollo");
									registerReceiver(Apollo.SMSReceiver,
											new IntentFilter("android.provider.Telephony.SMS_RECEIVED"));
								} else if (header.equals("FF000000") && data.equals("2")) {
									getSupportFragmentManager().beginTransaction().show(dream).commit();
									device_layout = dream;
									setTitle("dream");
								}

								if (header.equals("FF000000") && data.equals("1")
										|| header.equals("FF000000") && data.equals("2")) {
									fragment_show = true;
									calendar = Calendar.getInstance();
									String time2 = uart.dec2Byte(
											new int[] { calendar.get(Calendar.DAY_OF_MONTH),
													calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.SECOND) },
											new int[] { 5, 5, 6 });
									mService.writeRXCharacteristic(uart.hex2Byte("7001" + time2.toUpperCase()));
									try {
										Thread.sleep(1000);
									} catch (InterruptedException e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									}
									String time1 = uart.dec2Byte(
											new int[] { (calendar.get(Calendar.YEAR) - 2015),
													(calendar.get(Calendar.MONTH) + 1), calendar.get(Calendar.MINUTE) },
											new int[] { 6, 4, 6 });

									mService.writeRXCharacteristic(uart.hex2Byte("7000" + time1.toUpperCase()));

									// mService.writeRXCharacteristic(uart.hex2Byte("4100"
									// +
									// time1.toUpperCase()));
									// mService.writeRXCharacteristic(uart.hex2Byte("4101"
									// +
									// time2.toUpperCase()));
								}

								if (header.equals("01000002")) {
									// Log.v("test", data);
									BLE_Steps = Integer.parseInt(data);
									// Apollo.tv_step.setText(data + " 次");
								}

								new CountDownTimer(1000, 500) {

									public void onFinish() {
										if (PDialog != null) {
											PDialog.dismiss();
											PDialog = null;
										}
									}

									public void onTick(long millisUntilFinished) {

									}

								}.start();
							}

						} catch (Exception e) {
							Log.e(TAG, e.toString());
						}
					}
				});
			}
		}
	};

	private void service_init() {
		Intent bindIntent = new Intent(this, UartService.class);
		bindService(bindIntent, mServiceConnection, Context.BIND_AUTO_CREATE);

		Log.v("test", "123");
		LocalBroadcastManager.getInstance(this).registerReceiver(UARTStatusChangeReceiver,
				makeGattUpdateIntentFilter());
		// registerReceiver(SMSReceiver, IntentFilter());
	}

	private static IntentFilter makeGattUpdateIntentFilter() {
		final IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(UartService.ACTION_GATT_CONNECTED);
		intentFilter.addAction(UartService.ACTION_GATT_DISCONNECTED);
		intentFilter.addAction(UartService.ACTION_GATT_SERVICES_DISCOVERED);
		intentFilter.addAction(UartService.ACTION_DATA_AVAILABLE);
		intentFilter.addAction(UartService.DEVICE_DOES_NOT_SUPPORT_UART);
		return intentFilter;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		try {
			LocalBroadcastManager.getInstance(this).unregisterReceiver(UARTStatusChangeReceiver);
		} catch (Exception ignore) {
			Log.e(TAG, ignore.toString());
		}
		try {
			unregisterReceiver(Apollo.SMSReceiver);
		} catch (Exception ignore) {
		}
		unbindService(mServiceConnection);
		mService.stopSelf();

	}

	// private final BroadcastReceiver SMSReceiver = new BroadcastReceiver() {
	//
	// @Override
	// public void onReceive(Context context, Intent intent) {
	// Toast.makeText(context,intent.getAction(), Toast.LENGTH_LONG).show();
	// mService.writeRXCharacteristic(uart.hex2Byte("00010005"));
	//
	//// if (intent.getAction().equals(SMS_RECEIVED)) {
	//// // 當接受到簡訊時
	//// //
	// MainActivity.mService.writeRXCharacteristic(uart.hex2Byte("00010005"));
	//// Bundle bundle = intent.getExtras();
	//// if (bundle != null) {
	//// Object[] myObjects = (Object[]) bundle.get("pdus");
	//// // protocol description units 標準協定
	//// // PDU可以是文字或多媒體，用Object陣列全包下來
	////
	//// SmsMessage[] messages = new SmsMessage[myObjects.length];
	////
	//// for (int i = 0; i < myObjects.length; i++) {
	////
	//// messages[i] = SmsMessage.createFromPdu((byte[]) myObjects[i]);
	//// // 需將PDU格式轉成byte陣列
	//// // 將PDU格式的資料轉為smsMessage的格式
	//// // 由於簡訊長度的限制可能不止一封
	//// }
	////
	//// StringBuilder sb = new StringBuilder();
	//// for (SmsMessage tempMessage : messages) {
	//// sb.append("收到來自：\n");
	//// sb.append(tempMessage.getDisplayOriginatingAddress() + "\n\n");
	//// sb.append("內容為：\n");
	//// sb.append(tempMessage.getDisplayMessageBody());
	//// }
	//// Toast.makeText(context, sb.toString(), Toast.LENGTH_LONG).show();
	//// }
	//// }
	//
	// }
	// };

	@Override
	public void onResume() {
		super.onResume();
		if (BLE_action.equals(UartService.ACTION_GATT_DISCONNECTED)) {
			if (fragment_show) {
				getSupportFragmentManager().beginTransaction().hide(device_layout).commit();
				fragment_show = false;
			}
		}
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {

		case REQUEST_SELECT_DEVICE:
			// When the DeviceListActivity return, with the selected device
			// address
			if (resultCode == Activity.RESULT_OK && data != null) {
				String deviceAddress = data.getStringExtra(BluetoothDevice.EXTRA_DEVICE);
				mDevice = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(deviceAddress);

				name = data.getStringExtra("NAME");
				address = data.getStringExtra("ADDRESS");
				device = data.getStringExtra("Device");

				if (mService != null)
					mService.close();
				mService.connect(deviceAddress);

				Loading(deviceAddress);
			}
			break;
		case REQUEST_ENABLE_BT:
			// When the request to enable Bluetooth returns
			if (resultCode == Activity.RESULT_OK) {
				Toast.makeText(this, "Bluetooth has turned on ", Toast.LENGTH_SHORT).show();

			} else {
				// User did not enable Bluetooth or an error occurred
				Toast.makeText(this, "Problem in BT Turning ON ", Toast.LENGTH_SHORT).show();
				finish();
			}
			break;
		default:
			Log.e(TAG, "wrong request code");
			break;
		}
	}

	private void showMessage(String msg) {
		Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();

	}

	private void Loading(String deviceAddress) {
		PDialog = ProgressDialog.show(MainActivity.this, getResources().getString(R.string.dialogtittle),
				getResources().getString(R.string.dialogmassage), true);
		// PDialog = new ProgressDialog(MainActivity.this);
		// PDialog.setTitle(getResources().getString(R.string.dialogtittle));
		// PDialog.setMessage(getResources().getString(R.string.dialogmassage));
		// PDialog.setCancelable(false);
		// PDialog.setButton("Cancel", new DialogInterface.OnClickListener() {
		//
		// @Override
		// public void onClick(DialogInterface dialog, int which) {
		// // TODO Auto-generated method stub
		// }
		// });
		// PDialog.show();

		new Thread(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				while (true) {
					if (mService.mConnectionState == 2) {
						Log.v("test", "444 " + mService.mConnectionState);
						try {
							Thread.sleep(1000);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						Log.v("test", "555 " + mService.mConnectionState);
						tx = true;
						// if (device.equals("Headset")) {
						// mService.writeRXCharacteristic(uart.hex2Byte("10000000"));
						// getSupportFragmentManager().beginTransaction().show(dream).commit();
						// } else if (device.equals("Wristband")) {
						mService.writeRXCharacteristic(uart.hex2Byte("F0000000"));
						// getSupportFragmentManager().beginTransaction().show(apollo).commit();
						// }
						// Log.v("test", "" + uart.hex2Byte("F0000000"));
						try {
							Thread.sleep(1000);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						runOnUiThread(new Runnable() {
							public void run() {
								new CountDownTimer(3000, 1000) {

									public void onFinish() {
										if (PDialog != null) {
											PDialog.dismiss();
											PDialog = null;

											if (mService != null)
												mService.disconnect();
										}
									}

									public void onTick(long millisUntilFinished) {

									}

								}.start();
							}
						});

						break;
					}
				}
			}
		}).start();
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		if (mState == UART_PROFILE_CONNECTED) {
			showMessage(getResources().getString(R.string.exit));
		}
	}

	@Override
	public void onBackPressed() {
		if (mState == UART_PROFILE_CONNECTED) {
			Intent startMain = new Intent(Intent.ACTION_MAIN);
			startMain.addCategory(Intent.CATEGORY_HOME);
			startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			startActivity(startMain);
			showMessage(getResources().getString(R.string.exit));
		} else {
			new AlertDialog.Builder(this).setIcon(android.R.drawable.ic_dialog_alert).setTitle(R.string.popup_title)
					.setMessage(R.string.popup_message)
					.setPositiveButton(R.string.popup_yes, new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							DeviceListActivity.saveData(name, address, device);
							finish();
						}
					}).setNegativeButton(R.string.popup_no, null).show();
		}
	}
}

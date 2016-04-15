/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.apollo_1;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class DeviceListActivity extends Activity {
	private BluetoothAdapter mBluetoothAdapter;

	// private BluetoothAdapter mBtAdapter;
	private TextView mEmptyList;
	public static final String TAG = "DeviceListActivity";

	List<BluetoothDevice> deviceList;
	private DeviceAdapter deviceAdapter;
	Map<String, Integer> devRssiValues;
	private static final long SCAN_PERIOD = 10000; // 10 seconds
	private boolean mScanning;

	ProgressBar pgbar;
	int myProgress = 0;
	Button cancelButton;

	private static SharedPreferences settings;
	static final String data = "DATA";
	static final String nameField = "NAME";
	static final String addressField = "ADDRESS";
	static final String DeviceField = "DEVICE";
	static String name, address, Device;

	static TextView tv_address;

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		Log.d(TAG, "onCreate");
		setContentView(R.layout.device_list);

		cancelButton = (Button) findViewById(R.id.btn_cancel);
		tv_address = (TextView) findViewById(R.id.bleaddress);

		settings = getSharedPreferences(data, 0);
		readData();

		pgbar = (ProgressBar) findViewById(R.id.progressBar1);
		pgbar.setMax((int) SCAN_PERIOD / 1000);

		android.view.WindowManager.LayoutParams layoutParams = this.getWindow().getAttributes();
		layoutParams.gravity = Gravity.TOP;
		layoutParams.y = 200;

		// Use this check to determine whether BLE is supported on the device.
		// Then you can
		// selectively disable BLE-related features.
		if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
			Toast.makeText(this, R.string.ble_not_supported, Toast.LENGTH_SHORT).show();
			finish();
		}

		// Initializes a Bluetooth adapter. For API level 18 and above, get a
		// reference to
		// BluetoothAdapter through BluetoothManager.
		final BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
		mBluetoothAdapter = bluetoothManager.getAdapter();

		// Checks if Bluetooth is supported on the device.
		if (mBluetoothAdapter == null) {
			Toast.makeText(this, R.string.ble_not_supported, Toast.LENGTH_SHORT).show();
			finish();
			return;
		}
		mEmptyList = (TextView) findViewById(R.id.empty);
		populateList();
		Button cancelButton = (Button) findViewById(R.id.btn_cancel);
		cancelButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {

				if (mScanning == false)
					// scanLeDevice(true);
					populateList();
				else
					finish();
			}
		});

	}

	private void populateList() {
		/* Initialize device list container */
		Log.d(TAG, "populateList");
		mEmptyList.setVisibility(View.VISIBLE);

		deviceList = new ArrayList<BluetoothDevice>();
		deviceAdapter = new DeviceAdapter(this, deviceList);
		devRssiValues = new HashMap<String, Integer>();

		ListView newDevicesListView = (ListView) findViewById(R.id.new_devices);
		newDevicesListView.setAdapter(deviceAdapter);
		newDevicesListView.setOnItemClickListener(mDeviceClickListener);
		newDevicesListView.setOnItemLongClickListener(mDeviceLongClickListener);

		scanLeDevice(true);

	}

	Runnable runnable = new Runnable() {
		@Override
		public void run() {
			while (myProgress < SCAN_PERIOD / 1000) {
				try {
					myHandle.sendMessage(myHandle.obtainMessage());
					Thread.sleep(1000);
				} catch (Throwable t) {
				}
			}
		}
	};

	Handler myHandle = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			myProgress++;
			pgbar.setProgress(myProgress);

			if (myProgress >= SCAN_PERIOD / 1000) {
				mScanning = false;
				mBluetoothAdapter.stopLeScan(mLeScanCallback);
				cancelButton.setText(R.string.scan);
			}
		}
	};

	private void scanLeDevice(final boolean enable) {

		if (enable) {
			myProgress = 0;
			pgbar.setProgress(myProgress);

			new Thread(runnable).start();

			mScanning = true;
			mBluetoothAdapter.startLeScan(mLeScanCallback);
			cancelButton.setText(R.string.cancel);
		} else {
			mScanning = false;
			mBluetoothAdapter.stopLeScan(mLeScanCallback);
			cancelButton.setText(R.string.scan);
		}

	}

	private BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {

		@Override
		public void onLeScan(final BluetoothDevice device, final int rssi, byte[] scanRecord) {
			runOnUiThread(new Runnable() {
				@Override
				public void run() {

					runOnUiThread(new Runnable() {
						@Override
						public void run() {
							addDevice(device, rssi);
						}
					});

				}
			});
		}
	};

	private void addDevice(BluetoothDevice device, int rssi) {
		boolean deviceFound = false;

		for (BluetoothDevice listDev : deviceList) {
			if (listDev.getAddress().equals(device.getAddress())) {
				deviceFound = true;
				break;
			}
		}
		devRssiValues.put(device.getAddress(), rssi);

		if (device.getAddress().equals(address) && MainActivity.mService != null)
			ble_connect(name, address, Device);

		if (!deviceFound) {
			deviceList.add(device);
			mEmptyList.setVisibility(View.GONE);

			deviceAdapter.notifyDataSetChanged();
		}
	}

	@Override
	public void onStart() {
		super.onStart();

		IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
		filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
		filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
	}

	@Override
	public void onStop() {
		super.onStop();
		mBluetoothAdapter.stopLeScan(mLeScanCallback);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		mBluetoothAdapter.stopLeScan(mLeScanCallback);
	}

	private OnItemLongClickListener mDeviceLongClickListener = new OnItemLongClickListener() {

		@Override
		public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
			// TODO Auto-generated method stub
			ble_connect(deviceList.get(position).getName(), deviceList.get(position).getAddress(), "Headset");
			return false;
		}

	};

	private OnItemClickListener mDeviceClickListener = new OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			ble_connect(deviceList.get(position).getName(), deviceList.get(position).getAddress(), "Wristband");
		}
	};

	public void ble_connect(String ble_name, String ble_address, String ble_Device) {
		mBluetoothAdapter.stopLeScan(mLeScanCallback);
		Bundle b = new Bundle();
		b.putString(BluetoothDevice.EXTRA_DEVICE, ble_address);
		b.putString("NAME", ble_name);
		b.putString("ADDRESS", ble_address);
		b.putString("Device", ble_Device);

		name = ble_name;
		address = ble_address;
		Device = ble_Device;

		Intent result = new Intent();
		result.putExtras(b);
		setResult(Activity.RESULT_OK, result);
		finish();
	}

	protected void onPause() {
		super.onPause();
		saveData(name, address, Device);
		scanLeDevice(false);
		if (myHandle != null) {
			myHandle.removeCallbacks(runnable);
		}
	}

	public static void readData() {
		name = settings.getString(nameField, "");
		address = settings.getString(addressField, "");
		Device = settings.getString(DeviceField, "");

		if (!name.equals("") && !address.equals(""))
			tv_address.setText(name);
	}

	public static void saveData(String name, String address, String Device) {
		settings.edit().putString(nameField, name).putString(addressField, address).putString(DeviceField, Device)
				.commit();
	}

	class DeviceAdapter extends BaseAdapter {
		Context context;
		List<BluetoothDevice> devices;
		LayoutInflater inflater;

		public DeviceAdapter(Context context, List<BluetoothDevice> devices) {
			this.context = context;
			inflater = LayoutInflater.from(context);
			this.devices = devices;
		}

		@Override
		public int getCount() {
			return devices.size();
		}

		@Override
		public Object getItem(int position) {
			return devices.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewGroup vg;

			if (convertView != null) {
				vg = (ViewGroup) convertView;
			} else {
				vg = (ViewGroup) inflater.inflate(R.layout.device_element, null);
			}

			BluetoothDevice device = devices.get(position);
			final TextView tvadd = ((TextView) vg.findViewById(R.id.address));
			final TextView tvname = ((TextView) vg.findViewById(R.id.name));
			final ImageView ivrssi = (ImageView) vg.findViewById(R.id.rssi);

			byte rssival = (byte) devRssiValues.get(device.getAddress()).intValue();
			if (rssival != 0) {
				if (rssival < -95)
					ivrssi.setImageResource(R.drawable.rssi1);
				else if (rssival > -90)
					ivrssi.setImageResource(R.drawable.rssi3);
				else
					ivrssi.setImageResource(R.drawable.rssi2);
			}

			tvname.setText(device.getName());
			tvadd.setText(device.getAddress());
			if (device.getBondState() == BluetoothDevice.BOND_BONDED) {
				Log.i(TAG, "device::" + device.getName());
				tvname.setTextColor(Color.WHITE);
				tvadd.setTextColor(Color.WHITE);

			} else {
				tvname.setTextColor(Color.WHITE);
				tvadd.setTextColor(Color.WHITE);
			}
			return vg;
		}
	}
}

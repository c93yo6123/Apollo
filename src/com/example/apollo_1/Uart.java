package com.example.apollo_1;

import java.util.ArrayList;

import android.util.Log;

public class Uart {

	public String[] byte2data(byte[] txValue, int[][] format) {
		String bin = bytetransform(txValue, 2);
		// int[][] result = new int[txValue.length / 4][txValue.length / 2];
		String[] Array;
		ArrayList<String> mStringList = new ArrayList<String>();
		for (int i = 0; i < txValue.length / 4; i++) {
			String str = "";
			int count = 32 * (txValue.length / 4 - i - 1);
			for (int j = 0; j < format[i].length; j++) {
				if (i == 0) {
					int pars = Integer.parseInt(bin.substring(count, count + format[i][j]), 2);
					str += ((pars < 10) ? "0" : "") + Integer.toHexString(pars);
				} else
					str += Integer.parseInt(bin.substring(count, count + format[i][j]), 2);
				count += format[i][j];
			}
			if (i == 0)
				mStringList.add(str);
			else {
				String restr = str.replaceFirst("^0*", "");
				mStringList.add((restr.equals("")) ? "0" : restr);
			}
		}
		Array = new String[mStringList.size()];
		Array = mStringList.toArray(Array);
		return Array;
	}

	public byte[] hex2Byte(String hexString) {
		byte[] bytes = new byte[hexString.length() / 2];
		for (int i = 0; i < bytes.length; i++) {
			bytes[i] = (byte) Integer.parseInt(hexString.substring(2 * i, 2 * i + 2), 16);
		}
		return bytes;
	}

	public String dec2Byte(int[] dec, int[] format) {
		String reString = "";
		for (int i = 0; i < format.length; i++) {
			byte[] bytes = new byte[1];
			bytes[0] = (byte) dec[i];
			reString += bytetransform(bytes, 2).substring(bytetransform(bytes, 2).length() - format[i],
					bytetransform(bytes, 2).length());
		}

		return "0000".substring(0, 4 - String.valueOf(Long.toHexString(Long.parseLong(reString, 2))).length())
				+ String.valueOf(Long.toHexString(Long.parseLong(reString, 2)));
	}

	public String bytetransform(byte[] b, int ary) {
		String result = "";
		String r = "";
		for (int i = b.length - 1; i >= 0; i--) {
			result += Integer.toString((b[i] & 0xff) + 0x100, ary).substring(1);
			r += Integer.toString((b[i] & 0xff) + 0x100, ary).substring(1) + ",";
		}
		return result;
	}

	public String[] Rxdata(String str) {
		String[] Array;
		ArrayList<String> mStringList = new ArrayList<String>();

		for (int i = str.length(); i > 0; i -= str.length() / 4)
			mStringList.add("" + Long.parseLong(str.substring(i - str.length() / 4, i), 16));

		Array = new String[mStringList.size()];
		Array = mStringList.toArray(Array);
		return Array;
	}

	// public String hex2String(String hexString) {
	// StringBuilder str = new StringBuilder();
	// for (int i = 0; i < hexString.length(); i += 2)
	// str.append(Integer.parseInt(hexString.substring(i, i + 2), 16) + ",");
	// return str.toString();
	// }

	// public String[] Rxdata(int[][] time, int[][] format) {
	// String currentDateTimeString = DateFormat.getTimeInstance().format(new
	// Date());
	//
	// String[] Array;
	// ArrayList<String> mStringList = new ArrayList<String>();
	//
	// for (int i = time.length - 1; i >= 0; i--) {
	// Boolean b = false;
	// String r = "";
	// for (int j = 0; j < format[i].length; j++) {
	// if (time[i][j] != 0)
	// b = true;
	// if (b) {
	// r += time[i][j];
	// }
	// }
	// mStringList.add("[" + currentDateTimeString + "] RX: " + (String)
	// ((r.length() != 0) ? r : "0"));
	// }
	// Array = new String[mStringList.size()];
	// Array = mStringList.toArray(Array);
	// return Array;
	// }
}

package com.example.apollo_1;

public class Calorie {
	// 卡路里(kcal) = METs*體重(kg)*運動時間(hr)

	// 距離=步數×每步距離………………………(公式1)
	// 0-2 身高/5
	// 2-3 身高/4
	// 3-4 身高/3
	// 4-5 身高/2
	// 5-6 身高/1.2
	// 6-8 身高
	// >=8 身高*1.2
	public static float distance(int steps, float hight/* (公尺) */) {
		float h = (float) (hight * 1.2);
		switch (steps) {
		case 0:
			h = (float) (hight / 5);
		case 1:
		case 2:
		case 3:
		case 4:
			h = (float) (hight / 5-steps);
			break;
		case 5:
			h = (float) (hight / 1.2);
			break;
		case 6:
		case 7:
			h = (float) hight;
			break;

		default:
			break;
		}
		return (float) (steps * h);
	}

	// 速度=每2秒步數×跨步÷2秒………………(公式2)
	public static float speed(float distance/* (公尺) */, int sec/* (秒) */) {
		return (float) (distance / sec);
	}

	// METs(C/kg/h)=1.25×跑步速度(km/h) ……(公式3)
	// METs(C/kg/h)=1.25×速度(m/s)×3,600÷1,000 =4.5×速度(m/s) ……(公式4)

	// 2秒消耗卡路里
	// 卡路里(C)=METs×體重÷1,800 ……(公式5)
	// 卡路里(C)=1×體重÷1,800………………(公式6)(速度為0)
	public static float Cal(float speed/* (公尺/秒) */, float weight/* (公斤) */) {
		if (speed > 0)
			return (float) ((float) (4.5 * speed) * weight / 1800);
		else
			return (float) (weight / 1800);
	}
}

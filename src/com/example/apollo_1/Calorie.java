package com.example.apollo_1;

public class Calorie {
	// �d����(kcal) = METs*�魫(kg)*�B�ʮɶ�(hr)

	// �Z��=�B�ơѨC�B�Z���K�K�K�K�K�K�K�K�K(����1)
	// 0-2 ����/5
	// 2-3 ����/4
	// 3-4 ����/3
	// 4-5 ����/2
	// 5-6 ����/1.2
	// 6-8 ����
	// >=8 ����*1.2
	public static float distance(int steps, float hight/* (����) */) {
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

	// �t��=�C2��B�ơѸ�B��2��K�K�K�K�K�K(����2)
	public static float speed(float distance/* (����) */, int sec/* (��) */) {
		return (float) (distance / sec);
	}

	// METs(C/kg/h)=1.25�Ѷ]�B�t��(km/h) �K�K(����3)
	// METs(C/kg/h)=1.25�ѳt��(m/s)��3,600��1,000 =4.5�ѳt��(m/s) �K�K(����4)

	// 2����ӥd����
	// �d����(C)=METs���魫��1,800 �K�K(����5)
	// �d����(C)=1���魫��1,800�K�K�K�K�K�K(����6)(�t�׬�0)
	public static float Cal(float speed/* (����/��) */, float weight/* (����) */) {
		if (speed > 0)
			return (float) ((float) (4.5 * speed) * weight / 1800);
		else
			return (float) (weight / 1800);
	}
}

package Modules;

public class ParkingArea {
	int in;// ���ܼ� �������� �޾ƿ� ���� ���� ����,1�̸� ���� �� , 0�̸� ����ִ� ����
	String Area_Name;// ex) A�����̶�� Area_Name = A;
	int index;// ParkingArea �迭������ index
	char Dis = ' ';

	ParkingArea() {
	}

	ParkingArea(String Area_Name, int in, int index, char Dis) {//
		this.Area_Name = Area_Name;
		this.in = in;
		this.index = index;
		this.Dis = Dis;
	}
	
	// Ȯ���� toString() �Լ� �߰�
	public String toString() {
		return "Area_Name:" + Area_Name + "\nin:" + in + "\nindex:" + index + "\nDis:" + Dis + "\n";
	}
}

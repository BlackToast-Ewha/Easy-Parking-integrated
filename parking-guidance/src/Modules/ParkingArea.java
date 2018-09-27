package Modules;

public class ParkingArea {
	int in;// 적외선 센서에서 받아온 차량 주차 여부,1이면 주차 중 , 0이면 비어있는 공간
	String Area_Name;// ex) A구역이라면 Area_Name = A;
	int index;// ParkingArea 배열에서의 index
	char Dis = ' ';

	ParkingArea() {
	}

	ParkingArea(String Area_Name, int in, int index, char Dis) {//
		this.Area_Name = Area_Name;
		this.in = in;
		this.index = index;
		this.Dis = Dis;
	}
	
	// 확인차 toString() 함수 추가
	public String toString() {
		return "Area_Name:" + Area_Name + "\nin:" + in + "\nindex:" + index + "\nDis:" + Dis + "\n";
	}
}

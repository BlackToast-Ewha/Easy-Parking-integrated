package Modules;

//주차장에 들어온 차량 클래스
public class Car {
	String car_num = ""; //먼저 실행될 번호판 인식 모듈에 의해서 추출한 번호판 문자열

	public Car() {
	}

	public Car(String car_num) {
		this.car_num = car_num;
	}
}

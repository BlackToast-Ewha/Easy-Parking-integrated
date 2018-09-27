package Modules;

public class Area {
	String AreaName; // 주차 구역 이름
	int remainNum; // 남아있는 주차구역 개수
	int totalNum; // 전체 주차구역 개수
	double entranceScore; // 입출차게이트 점수
	double elevatorScore; // 엘리베이터 점수
	double disabledScore; // 장애인구역 점수
	Intersection nearIntersection; // 인접 인터섹션
	int indexInPA; // 0부터 시작
	
	Area(String areaName,int remainNum,int totalNum,Intersection nearsection,int indexPA){
		this.AreaName = areaName;
		this.remainNum = remainNum;
		this.totalNum = totalNum;
		this.nearIntersection = nearsection;
		this.indexInPA = indexPA;
	}


}

package Modules;

import java.util.ArrayList;

// Intersection을 Node로 하는 그래프의 Edge
public class Edge {
	public final double cost;
	public final Intersection target;
	public ArrayList<ParkingArea> NA;// 해당 edge에서 주변부에 있는 주차 구역을 배열리스트로 저장

	public Edge(Intersection targetNode, double costVal) {
		NA = new ArrayList<ParkingArea>(); 
		target = targetNode;
		cost = costVal;
	}

	public void Decide_Area(ParkingArea[] A) {// 배열리스트에 주차 구역을 추가해줌
		NA.add(A[0]);

	}// Decide_Area

}
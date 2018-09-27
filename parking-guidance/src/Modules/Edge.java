package Modules;

import java.util.ArrayList;

// Intersection�� Node�� �ϴ� �׷����� Edge
public class Edge {
	public final double cost;
	public final Intersection target;
	public ArrayList<ParkingArea> NA;// �ش� edge���� �ֺ��ο� �ִ� ���� ������ �迭����Ʈ�� ����

	public Edge(Intersection targetNode, double costVal) {
		NA = new ArrayList<ParkingArea>(); 
		target = targetNode;
		cost = costVal;
	}

	public void Decide_Area(ParkingArea[] A) {// �迭����Ʈ�� ���� ������ �߰�����
		NA.add(A[0]);

	}// Decide_Area

}
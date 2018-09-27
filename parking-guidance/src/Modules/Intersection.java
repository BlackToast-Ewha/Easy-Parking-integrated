package Modules;

public class Intersection {

	public double g_scores;
	public double h_scores = 0.0; // �̰� ����� �ϳ��ϳ� �ٸ� ������ initialize ����� �ϴ�
										// �� ���� astar ���ǻ�
	public double f_scores = 0;
	public Edge[] adjacencies;
	public Intersection parent;
	public Intersection child;
	public int index;// intersection�� index(��ȣ)

	public int row;
	public int col;

	public Intersection(int index) {
		// value = val;
		// h_scores = hVal;
		this.index = index;
	}

	public String toString() {
		return Integer.toString(index);
	}

	public void setRowCol(int row,int col){
		this.row = row;
		this.col = col;
	}

	public int getRow() {
		return this.row;
	}

	public int getCol() {
		return this.col;
	}
	
//	public void setSourceChild(){
//		source_child = new Intersection[this.adjacencies.length];
//		for(int i = 0; i < source_child.length; i++){
//			source_child[i] = this.adjacencies[i].target;
//		}
//	}

	public void calculateHScore(Intersection dest) {
		this.h_scores = Math.abs(dest.getRow() - this.getRow()) + Math.abs(dest.getCol() - this.getCol());

	}

	

}
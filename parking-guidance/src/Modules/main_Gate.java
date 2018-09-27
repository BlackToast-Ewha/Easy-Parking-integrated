package Modules;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;

/*2018-08-03 UPDATE
 * (1) Path�� ȭ��ǥ�� ��ȯ�ϴ� �۾� �Ϸ�
 * (2) ��ȯ�� ȭ��ǥ�� �ֿܼ� ����ϴ� �۾� �Ϸ�
 * (3) ���� ���� ���� �Ϸ�
 * (4) ���ͼ��� ���� �Ϸ�
 * (5) ������ ���� ���� ���� ���ͼ����� �ε��� ���� �Ϸ�
 * (6) AstarSearch()�� ���� ���� �Ϸ�
 * (7) ������ �ʴ� �ڵ�� �ǹ� ���� �ּ� ���� �Ϸ�
 */

import java.util.PriorityQueue;
import java.util.Scanner;
import java.util.Set;
import java.util.stream.Collectors;

import javax.swing.plaf.synth.SynthSeparatorUI;

import com.fazecast.jSerialComm.SerialPort;

public class main_Gate {

	static Serial s = new Serial();
	/* ���� ����, ���ͼ���, ������ ���� ���� */
	static ParkingArea[][] area;
	public static Intersection[] n = new Intersection[8];
	static Area[] parkArea;
	static Display[][] display = new Display[8][4];

	/* ���� ������ ��� �켱���� ť�� ���� ���� �̸��� ��� String���� */
	static PriorityQueue<Area> entranceArea;
	static PriorityQueue<Area> elevatorArea;
	static PriorityQueue<Area> disabledArea;
	static String totalAssign;

	/* MySQL �����ͺ��̽� ���ῡ �ʿ��� ���� ���� */
	static final String JDBC_driver = "com.mysql.cj.jdbc.Driver";
	static final String DB_url = "jdbc:mysql://localhost:3306/team15?useSSL=false&serverTimezone=UTC";
	static final String User = "root";
	static final String password = "root";
	static Connection conn = null;
	static Statement stmt = null;
	static PreparedStatement pr_stmt = null;

	/* Display �Լ��� �Ѱ��ֱ� ���� ����ϴ� List<Intersection> ���� */
	static List<Intersection> originalPath;
	static List<Intersection> gateUIPath;

	/* ������ ��ü�� �ʱ�ȭ�ϴ� �Լ� */
	public static void setDisplay() {
		for (int i = 0; i < 8; i++) {
			for (int j = 0; j < 4; j++) {
				display[i][j] = new Display(i, j);
				// j=0 -> u, j=1 -> l, j=2 -> d, j=3 -> r �������� ��Ÿ��,
			}
		}

	}

	/* �����ǿ� ȭ��ǥ�� ����ϴ� �Լ� */
	public static void printDisplay(List<Intersection> path, String CarPlate) throws IOException, InterruptedException {

		System.out.println("\nprintDisplay ȣ��\n");

		int a, b, c;

		if (path.size() == 3) {
			c = path.get(0).index;
			a = path.get(1).index;
			b = path.get(2).index;
		} else {
			c = 0;
			a = path.get(0).index;
			b = path.get(1).index;
		}
		// c = a; // ������ i ����, previous Index
		// a = path.get(i).index; // current Index
		// b = path.get(i + 1).index; // next Index
		// }
		// ���⼭ �ɰ������� �ϵ��ڵ�

		// /for (int i = 0; i < path.size() - 1; i++) { // path �� intersection�� �� ���� ����
		// ������� ����

		System.out.printf("previous:%d, current:%d, next:%d", c, a, b); // a, b, c: 1���� ���۵Ǵ� ��ȣ

		// a=7, b=8, c=3
		if ((b - a) == 1) { // (1->2, 2->3, 3->4, 4->5, 5->6, 7->8)
			if ((c - a) == -1) { // ���� ��Ʈ. ���� ���ͼ����� index �� ���� ���ͼ����� index ���� ���� ��
				if (a == 3) { // 234
					display[2][0].printLeft(CarPlate);// 3 �� ���ͼ��� }

				} else if (a == 4) {
					display[3][0].printLeft(CarPlate); // 4->5
				} else if (a == 7) {
					display[6][3].printRight(CarPlate);// 7->8
					System.out.println("378");
				} else // 12 23 56
					display[a - 1][0].printStraight(CarPlate);
			} else {// ������ ���ͼ����� index �� ���� ���ͼ����� index ���� ���� ��
					// 612 523 812 734 256 618 218
				if (c == 8)
					display[0][0].printStraight(CarPlate);// 812
				else if (c == 7) {
					display[2][1].printStraight(CarPlate);// 734
					System.out.println("this 734");
				} else if (c == 2 && a == 5)
					display[4][3].printLeft(CarPlate);// 256
				else if (c == 6)
					display[0][3].printStraight(CarPlate);// 618
				else if (c == 2 && a == 1)
					display[0][2].printLeft(CarPlate); // 218

				else if (c == 3) {
					display[a - 1][3].printRight(CarPlate); // 378
				}

				else {
					display[a - 1][3].printLeft(CarPlate); // 612 523
					System.out.println("this");
				}
			}
		} else if (c == a) { // before==curr
			if (b == 8)
				display[0][3].printStraight(CarPlate);
			if (b == 6)
				display[0][0].printLeft(CarPlate);
			if (b == 2)
				display[0][0].printStraight(CarPlate);
		} else if ((b - a) == -1) {// 6->5, 5->4, 4->3, 3->2, 2->1, 8->7

			if (a == 4) { // 543
				display[3][3].printRight(CarPlate);
			} else if (a == 3) { // 3->2
				if (c == 4) {
					display[2][3].printRight(CarPlate); // 432
					System.out.print("432");
				}
				if (c == 7) {
					display[2][1].printLeft(CarPlate); // 732
					System.out.print("732");
				} else
					System.out.println("restart from intersection3"); // 3����
				// ������Ͽ�
				// 32
			} else if (a == 8) { // 8->7
				display[7][0].printLeft(CarPlate);
			} else {// 6->5, 5->4, 2->1
				if (c - a == -1) {
					display[a - 1][2].printStraight(CarPlate); // 65 654 321
				} else {
					display[a - 1][3].printRight(CarPlate); // 165 254 521
				}
			}

		} else {// index�� �̾����� ���� ���.b-a != 1 1->6, 1->8, 2->5, 3->7, �׸��� �� �ݴ� 6->1, 8->1, 5->2,
				// 7->3
			if (c < a) {// index �� �������� ū���� ���� ��
				if (b > a) {// 016(816) 018 125 237
					if (a == 1 && b == 8)
						display[0][0].printRight(CarPlate); // 018 �̷��� �Ͼ�� �ȵ�.

					else if (a == 3)
						display[2][0].printRight(CarPlate); // 237
					else
						display[a - 1][0].printLeft(CarPlate); // 016(816) 125

				} else {// 561 452 781
					if (a == 8)
						display[7][3].printRight(CarPlate); // 781
					else
						display[a - 1][0].printLeft(CarPlate); // 561 452

				}

			} else {// index �� ū���� �������� ���� ��, c>a // ��Ž���� ���, 437 652 873 216 325
				if (b > a) {// 437 216 325
					if (a == 3)
						display[2][3].printStraight(CarPlate); // 437
					else
						display[a - 1][2].printRight(CarPlate); // 216 325

				} else { // 652 873
					if (a == 5)
						display[4][3].printRight(CarPlate); // 652
					if (a == 7)
						display[6][0].printLeft(CarPlate);// 873

				}
			}

		}

	}
	// }

	/* ���� ���� �� ĭ���� �ʱ�ȭ �ϴ� �Լ� */
	public static void setParkingArea() throws IOException {

		BufferedReader br = new BufferedReader(new FileReader("numArea.txt"));
		String numOfArea = br.readLine();
		int numOfAreas = Integer.parseInt(numOfArea);
		area = new ParkingArea[numOfAreas][];
		br = new BufferedReader(new FileReader("lotNumArea.txt"));
		String nums = br.readLine();
		String[] splitNums = nums.split(" ");
		int[] splits = new int[splitNums.length];

		for (int i = 0; i < splits.length; i++) {
			splits[i] = Integer.parseInt(splitNums[i]);
		}

		for (int i = 0; i < area.length; i++) {
			area[i] = new ParkingArea[splits[i]];
		}

		br = new BufferedReader(new FileReader("disabledArea_set.txt"));
		String dis = br.readLine();
		String[] disArr = dis.split(",");
		List<String> disList = Arrays.asList(disArr);
		char tempAreaName;
		String areaName;
		char mode = 0;

		// ���� ���� ��ü ����, ���߿� ������ ���̽��� ������ �κ�
		for (int i = 0; i < area.length; i++) {
			for (int j = 0; j < area[i].length; j++) {
				tempAreaName = (char) (i + 65);
				areaName = Character.toString(tempAreaName);
				if (disList.contains(areaName))
					mode = 'd';
				else
					mode = 'a';
				area[i][j] = new ParkingArea(areaName, 0, j, mode);
				// System.out.println("[setParkingArea()]"+areaName+"������ mode�� "+mode);
			}
		}
	}

	/* �������� Intersection �ʱ�ȭ */
	public static void setIntersection() throws NumberFormatException, IOException { // �տ��� ������ 8 ���� intersection �� ��ü��
																						// �����Ͽ� �ο�

		BufferedReader br = new BufferedReader(new FileReader("numIntersection.txt"));
		int numIntsc = Integer.parseInt(br.readLine());
		// 1~8 ������ �ε��� �ο�
		for (int i = 0; i < numIntsc; i++) {
			n[i] = new Intersection((i + 1));

		}

		br = new BufferedReader(new FileReader("rowcol.txt"));
		String rowcol;
		String[] rowcolArr;
		int count = 0;
		/* row�� column �� �����ϴ� �� */
		while (true) {
			rowcol = br.readLine();
			if (rowcol == null)
				break;
			rowcolArr = rowcol.split(" ");
			int row = Integer.parseInt(rowcolArr[0]);
			int col = Integer.parseInt(rowcolArr[1]);
			n[count].setRowCol(row, col);
			count++;
		}

		// n[i].adjacencies = �� ���ͼ��ǿ��� ����� edge���� ����

		br = new BufferedReader(new FileReader("connectedIntsc.txt"));
		String connected;
		String[] tempArr;
		count = 0;
		Edge[] edges;

		while (true) {
			connected = br.readLine();
			// System.out.println("[setIntersection()]�� ���ͼ��ǿ��� ���������� ����� ���ͼ��ǵ���
			// "+connected);
			if (connected == null)
				break;
			tempArr = connected.split(",");
			edges = new Edge[tempArr.length];

			for (int i = 0; i < edges.length; i++) {
				edges[i] = new Edge(n[Integer.parseInt(tempArr[i]) - 1], 1);
				// System.out.println("[setIntersection()]"+count+"���ͼ��ǿ��� �߰��� edge�� Ÿ������ �ϴ�
				// ���ͼ����� "+edges[i].target.index);
			}
			n[count].adjacencies = edges;
			count++;
		}

	}

	/*
	 * ���� ���� (A,B ��)�� �ʱ�ȭ �ϴ� �Լ� ������ �̸�(���ĺ�), �ܿ����� ��, ��ü���� ��, ������ ����� ���ͼ����� �����ϰ� ��.
	 */
	public static void setArea(Connection conn, Statement stmt)
			throws NumberFormatException, IOException, SQLException {
		BufferedReader br = new BufferedReader(new FileReader("numArea.txt"));
		BufferedReader br1 = new BufferedReader(new FileReader("lotNumArea.txt"));
		BufferedReader br2 = new BufferedReader(new FileReader("intersectionIndex.txt"));

		int numArea = Integer.parseInt(br.readLine());
		String lotnums = br1.readLine();

		/*
		 * IMPLEMENTED!!! �����ͺ��̽� parkingAreas ���̺��� total column�� ���� ���� occupied ������ lot
		 * ������ �� free�� ������ �� �ջ� remained ������ total - occupied
		 */

		String[] lotnum = lotnums.split(" ");
		parkArea = new Area[numArea];
		char a = 'A';
		String nearIntsc;
		int nearIntsc_int;
		int count = 0;
		int lotnum_int;

		while (true) {
			nearIntsc = br2.readLine();
			if (nearIntsc == null)
				break;

			lotnum_int = Integer.parseInt(lotnum[count]);

			String query = String.format(
					"insert into parkingareas (areaName, total, occupied, remained) " + "values ('%c', %d, %d, %d)", a,
					lotnum_int, 0, lotnum_int);
			System.out.println("query with " + a + ":" + query);

			stmt.executeUpdate(query);

			// dealing with smaller areas
			for (int non_avaliable_idx = lotnum_int + 1; non_avaliable_idx <= 24; non_avaliable_idx++) {
				query = String.format("update parkingareas set lot%d='occupied' where areaName='%c'", non_avaliable_idx,
						a);
				stmt.executeUpdate(query);
			}

			nearIntsc_int = Integer.parseInt(nearIntsc);
			parkArea[count] = new Area(Character.toString(a), lotnum_int, lotnum_int, n[nearIntsc_int - 1], count);
			count++;
			a = (char) (a + 1);
		}

	}

	/*
	 * 2018-08-01 AstarSearch�� parent �Ӹ� �ƴ϶� child ��带 �����ϰ� ������� ���� ������
	 * printPath()�� �޸� �ڽ��� �ڽĳ�带 Ȯ���ϸ� path�� ����ϰ� �ϴ� �Լ� �ʿ� �װ� printPathTemp()��.
	 */
	public static List<Intersection> printPathTemp(Intersection source, Intersection dest, Intersection beforeIntsc) {

		List<List<Intersection>> allPath = new ArrayList<List<Intersection>>();
		boolean destTarget = false;

		if (source == dest) {
			List<Intersection> path = new ArrayList<Intersection>();
			path.add(source);
			allPath.add(path);

			return allPath.get(0);
		}

		else {

			for (Edge e : source.adjacencies) {

				if (e.target != beforeIntsc) {
					List<Intersection> path = new ArrayList<Intersection>();
					path.add(source);
					for (Intersection node = e.target; node != null; node = node.child) {
						if (!path.contains(node))
							path.add(node);
						if (node == dest) {
							path.remove(dest);
						}
					}
					if (!path.contains(dest)) {
						for (Edge d : path.get(path.size() - 1).adjacencies) {
							if (d.target == dest) {
								// System.out.println(d.target.index);
								destTarget = true;
								break;
							}
						}
						if (destTarget) {
							path.add(dest);
							allPath.add(path);
							destTarget = false;
						}
					}

				}

			}

			int minIndex = 0;
			for (int i = 0; i < allPath.size(); i++) {
				// System.out.println(allPath.get(i));
				if (allPath.get(i).size() < allPath.get(minIndex).size())
					minIndex = i;
			}
			return allPath.get(minIndex);
		}
	}

	/* ������ ����ϴ� path ����Լ� */
	public static List<Intersection> printPath(Intersection target) {
		List<Intersection> path = new ArrayList<Intersection>();

		for (Intersection node = target; node != null; node = node.parent) {
			path.add(node);
		}

		Collections.reverse(path);

		return path;
	}

	/*
	 * destination node(=goal)�� �ʿ��ϴ� ������ AstarSearch�� source,goal,��� ������ ���ͼ��Ǳ��� ����ϰ�
	 * �ϴ� �Լ��� ����
	 */
	public static void AstarSearch(Intersection source, Intersection goal, Intersection beforeIntsc) {

		/* ������ ���ͼ��ǵ��� ��� ��ü Set explored */
		Set<Intersection> explored = new HashSet<Intersection>();

		/*
		 * �ĺ��� �Ǵ� ���ͼ����� ��� PriorityQueue queue f_score�� ���� ���� �켱 ������ ����
		 */
		PriorityQueue<Intersection> queue = new PriorityQueue<Intersection>(20, new Comparator<Intersection>() {
			// override compare method
			public int compare(Intersection i, Intersection j) {
				if (i.f_scores > j.f_scores) {
					return 1;
				}

				else if (i.f_scores < j.f_scores) {
					return -1;
				}

				else {
					return 0;
				}
			}

		});

		// cost from start
		source.g_scores = 0;
		queue.add(source);

		boolean found = false;

		while ((!queue.isEmpty()) && (!found)) {

			// the node in having the lowest f_score value
			Intersection current = queue.poll();
			explored.add(current);

			// goal found
			if (current.index == goal.index) {
				found = true;
				break; // �ٷ� while�� Ż��.
			}

			// check every child of current node
			for (Edge e : current.adjacencies) {
				Intersection child = e.target;
				double cost = e.cost;
				double temp_g_scores = current.g_scores + cost;
				double temp_f_scores = temp_g_scores + child.h_scores;
				/*
				 * if child node has been evaluated and the newer f_score is higher, skip
				 */

				if ((explored.contains(child)) && (temp_f_scores >= child.f_scores)) {
					continue;
				}

				/*
				 * else if child node is not in queue or newer f_score is lower
				 */

				else if ((!queue.contains(child)) || (temp_f_scores < child.f_scores)) {

					child.parent = current;
					child.g_scores = temp_g_scores;
					child.f_scores = temp_f_scores;

					if (queue.contains(child)) {
						queue.remove(child);
					}
					if (child != beforeIntsc) { // ����Ǿ��ִ� ���ͼ����� ��� ������ ���ͼ����� �ƴ� ��쿡�� queue�� �߰�. ���� �ȳ��� ���� ���� �ڵ���
						queue.add(child);
						current.child = child;
						if (child.index == goal.index)
							break;
					}

				}

			} // for

		}
	}

	/* ��ȣ�ϴ� ���� (����������, ����Ʈ, ����� ��������)�� ���� ������ ���ͼ����� �ε����� ��ȯ */
	public static int find_dest(String mode, String line, Connection conn, Statement stmt) throws SQLException {
		int findFlag = 0; // ���� ������ �Ҵ�Ǿ����� üũ�ϴ� flag ����
		String selectedAreaName = ""; // ���� ������ �̸��� ���� String ����
		int selectedLotNumber = 0; // ���� ���� ������ ���� ��ȣ�� ��� int ����
		Intersection answer = null; // ������ ���� ������ ���� ������ ���� ���ͼ����� ��� Intersection ����

		if (mode.equals("entrance")) { // �Ա� ��ó�� ������ ���
			while (findFlag != 1) {
				Area tempArea = entranceArea.poll(); // �켱����ť�� ��Ʈ ��带 ����. ���� �������� ��Ȳ�� �ݿ��� �ּ��� ���� ���� ����

				/*
				 * IMPLEMENTED!!!: tempArea.remained�� class ������ �ƴ� SQL���� column���� ��������
				 */

				String query = String.format("select remained, occupied from parkingareas where areaName='%s'",
						tempArea.AreaName);

				ResultSet rs = stmt.executeQuery(query);

				int sql_remained = -1;
				int sql_occupied = -1;

				while (rs.next()) {
					sql_remained = rs.getInt(1);
					sql_occupied = rs.getInt(2);
				}

//				System.out.println("remained from SQL:" + sql_remained);
//				System.out.println("occupied from SQL:" + sql_occupied);
//				System.out.println("remained from Class:" + tempArea.remainNum);

				if (tempArea.remainNum != 0) {

					// ���� �Ҵ�
					selectedAreaName = tempArea.AreaName;
					selectedLotNumber = tempArea.totalNum - tempArea.remainNum + 1;

					/*
					 * IMPLEMENTED!!!: �ش� area�� lot ������ 1�� ���� (in �Ҵ�) occupied ++ remained --
					 */
					query = String.format("update parkingareas set remained=%d, occupied=%d where areaName='%s'",
							sql_remained - 1, sql_occupied + 1, tempArea.AreaName);

					stmt.executeUpdate(query);

					tempArea.remainNum--; // in������ 1�� �ٲٴ°Ͱ� ������ ������ ��. �ܿ� ���� ���� 1 ����

					entranceArea.add(tempArea); // peek()�� �ƴ϶� �ƿ� ť ������ Area�� �������Ƿ� �ٽ� �־���
					answer = tempArea.nearIntersection; // ���� ���ͼ��� �޾ƿ�
					findFlag = 1; // ���� ���� �Ҵ� �ϷḦ ���ϴ� ������ 1�� �ٲ�
				} else {
					if (entranceArea.isEmpty()) // �������� �����̸� findFlag ������ 0���� �ΰ� (���� ����) ���� Ż��
						break;
					continue; // �������� ������ �ƴ����� poll()�� ���� ������ �ܿ� �������� 0�� ��� �ٽ� while�� ���ư��� ��� poll()
				}
			}

		}
		if (mode.equals("elevator")) {
			while (findFlag != 1) {
				Area tempArea = elevatorArea.poll(); // �켱����ť�� ��Ʈ ��带 ����. ���� �������� ��Ȳ�� �ݿ��� �ּ��� ���� ���� ����

				/*
				 * IMPLEMENTED!!!: tempArea.remained�� class ������ �ƴ� SQL���� column���� ��������
				 */

				String query = String.format("select remained, occupied from parkingareas where areaName='%s'",
						tempArea.AreaName);

				ResultSet rs = stmt.executeQuery(query);

				int sql_remained = -1;
				int sql_occupied = -1;

				while (rs.next()) {
					sql_remained = rs.getInt(1);
					sql_occupied = rs.getInt(2);
				}

//				System.out.println("remained from SQL:" + sql_remained);
//				System.out.println("occupied from SQL:" + sql_occupied);
//				System.out.println("remained from Class:" + tempArea.remainNum);

				if (tempArea.remainNum != 0) {

					// ���� �Ҵ�
					selectedAreaName = tempArea.AreaName;
					selectedLotNumber = tempArea.totalNum - tempArea.remainNum + 1;

					/*
					 * IMPLEMENTED!!!: �ش� area�� lot ������ 1�� ���� (in �Ҵ�) occupied ++ remained --
					 */
					query = String.format("update parkingareas set remained=%d, occupied=%d where areaName='%s'",
							sql_remained - 1, sql_occupied + 1, tempArea.AreaName);

					stmt.executeUpdate(query);

					tempArea.remainNum--; // in������ 1�� �ٲٴ°Ͱ� ������ ������ ��. �ܿ� ���� ���� 1 ����

					elevatorArea.add(tempArea); // peek()�� �ƴ϶� �ƿ� ť ������ Area�� �������Ƿ� �ٽ� �־���
					answer = tempArea.nearIntersection; // ���� ���ͼ��� �޾ƿ�
					findFlag = 1; // ���� ���� �Ҵ� �ϷḦ ���ϴ� ������ 1�� �ٲ�
				} else {
					if (elevatorArea.isEmpty()) // �������� �����̸� findFlag ������ 0���� �ΰ� (���� ����) ���� Ż��
						break;
					continue; // �������� ������ �ƴ����� poll()�� ���� ������ �ܿ� �������� 0�� ��� �ٽ� while�� ���ư��� ��� poll()
				}
			}
		}
		if (mode.equals("disabled")) {
			while (findFlag != 1) {
				Area tempArea = disabledArea.poll(); // �켱����ť�� ��Ʈ ��带 ����. ���� �������� ��Ȳ�� �ݿ��� �ּ��� ���� ���� ����

				/*
				 * IMPLEMENTED!!!: tempArea.remained�� class ������ �ƴ� SQL���� column���� ��������
				 */

				String query = String.format("select remained, occupied from parkingareas where areaName='%s'",
						tempArea.AreaName);

				ResultSet rs = stmt.executeQuery(query);

				int sql_remained = -1;
				int sql_occupied = -1;

				while (rs.next()) {
					sql_remained = rs.getInt(1);
					sql_occupied = rs.getInt(2);
				}

				System.out.println("remained from SQL:" + sql_remained);
				System.out.println("occupied from SQL:" + sql_occupied);
				System.out.println("remained from Class:" + tempArea.remainNum);

				if (tempArea.remainNum != 0) {

					// ���� �Ҵ�
					selectedAreaName = tempArea.AreaName;
					selectedLotNumber = tempArea.totalNum - tempArea.remainNum + 1;

					/*
					 * IMPLEMENTED!!!: �ش� area�� lot ������ 1�� ���� (in �Ҵ�) occupied ++ remained --
					 */
					query = String.format("update parkingareas set remained=%d, occupied=%d where areaName='%s'",
							sql_remained - 1, sql_occupied + 1, tempArea.AreaName);

					stmt.executeUpdate(query);

					tempArea.remainNum--; // in������ 1�� �ٲٴ°Ͱ� ������ ������ ��. �ܿ� ���� ���� 1 ����

					disabledArea.add(tempArea); // peek()�� �ƴ϶� �ƿ� ť ������ Area�� �������Ƿ� �ٽ� �־���
					answer = tempArea.nearIntersection; // ���� ���ͼ��� �޾ƿ�
					findFlag = 1; // ���� ���� �Ҵ� �ϷḦ ���ϴ� ������ 1�� �ٲ�
				} else {
					if (disabledArea.isEmpty()) // �������� �����̸� findFlag ������ 0���� �ΰ� (���� ����) ���� Ż��
						break;
					continue; // �������� ������ �ƴ����� poll()�� ���� ������ �ܿ� �������� 0�� ��� �ٽ� while�� ���ư��� ��� poll()
				}
			}
		}
		if (findFlag == 0) {
			System.out.printf("%s ����\n", line);
			System.out.println("[����]�����忡 �����Ͻ� �� �����ϴ�!!");
			return -1;
		}
		totalAssign = selectedAreaName + selectedLotNumber;

		// Console message
		System.out.println("\n===================================");
		System.out.printf("%s ����\n", line);
		System.out.println("������ ���������� " + selectedAreaName + selectedLotNumber + "�Դϴ�.");
		System.out.println("===================================\n");

		String query = String.format("update parkingareas set lot%d='booked' where areaName='%s'", selectedLotNumber,
				selectedAreaName);
		stmt.executeUpdate(query);

		return answer.index - 1; // (0~7 ������ �ε��� ��ȯ)
	}

	/*
	 * �Ա�, ����������, ����� ���� ������ ���� ������ �켱����ť�� ������. �켱������ ��ȣ�������κ����� �Ÿ�^2+�ܿ�����/��ü������ ������
	 * ����
	 */
	public static void setPriorityQueue() {
		entranceArea = new PriorityQueue<Area>(12, new Comparator<Area>() {

			public int compare(Area a0, Area a1) {
				double score0 = Math.pow(a0.entranceScore, 2) + (a0.remainNum / a0.totalNum);
				double score1 = Math.pow(a1.entranceScore, 2) + (a1.remainNum / a1.totalNum);

				if (score0 < score1)
					return 1;
				else if (score0 > score1)
					return -1;
				else
					return 0;
			}

		});
		elevatorArea = new PriorityQueue<Area>(12, new Comparator<Area>() {
			public int compare(Area a0, Area a1) {
				double score0 = Math.pow(a0.elevatorScore, 2) + (a0.remainNum / a0.totalNum);
				double score1 = Math.pow(a1.elevatorScore, 2) + (a1.remainNum / a1.totalNum);

				if (score0 < score1)
					return 1;
				else if (score0 > score1)
					return -1;
				else
					return 0;
			}
		});
		disabledArea = new PriorityQueue<Area>(12, new Comparator<Area>() {
			public int compare(Area a0, Area a1) {
				double score0 = Math.pow(a0.disabledScore, 2) + (a0.remainNum / a0.totalNum);
				double score1 = Math.pow(a1.disabledScore, 2) + (a1.remainNum / a1.totalNum);

				if (score0 < score1)
					return 1;
				else if (score0 > score1)
					return -1;
				else
					return 0;
			}
		});
	}

	/* ���ͼ����� h_score�� ���������� ��� */
	public static void setIntersectionInfo(Intersection goal) {
		for (int i = 0; i < 8; i++) {
			n[i].calculateHScore(goal);
		}
	}

	/* ��ȣ���� �°� ���� ������ ������ ����ϴ� �Լ� */
	/* �ð� ���⵵�� ������ ������ �ʱ� ���ÿ��� ���̴� �Լ��̹Ƿ� �Ϸ翡 1ȸ ���Ϸ� ȣ��� */
	public static void setParkAreaScore() throws IOException {
		int setCount = 0;
		/* ������ �����ڰ� �Է��� ������ ���߾� ���� ��� */
		BufferedReader br = new BufferedReader(new FileReader("managerUI_Test.txt"));

		while (true) {
			String line;
			String[] tempArr;
			line = br.readLine();
			if (line == null)
				break;
			tempArr = line.split(",");
			if (setCount == 0) {
				for (int i = 0; i < 12; i++) {
					for (int j = 0; j < tempArr.length; j++) {
						if (parkArea[i].AreaName.equals(tempArr[j])) {
							parkArea[i].entranceScore = (tempArr.length - j) * 0.5;
							entranceArea.add(parkArea[i]);
						}
					}
				}
			}
			if (setCount == 1) {
				for (int i = 0; i < 12; i++) {
					for (int j = 0; j < tempArr.length; j++) {
						if (parkArea[i].AreaName.equals(tempArr[j])) {
							parkArea[i].elevatorScore = (tempArr.length - j) * 0.5;
							elevatorArea.add(parkArea[i]);
						}
					}
				}
			}
			if (setCount == 2) {
				for (int i = 0; i < 12; i++) {
					for (int j = 0; j < tempArr.length; j++) {
						if (parkArea[i].AreaName.equals(tempArr[j])) {
							parkArea[i].disabledScore = (tempArr.length - j) * 0.5;
							disabledArea.add(parkArea[i]);
						}
					}
				}
			}
			setCount++;
		}
	}

	// ���� �����ÿ� ����
	public static String gateUI(int user_input, Scanner sc, Connection conn, Statement stmt)
			throws IOException, SQLException, InterruptedException {

		BufferedReader br = new BufferedReader(new FileReader("nowEnter.txt"));
		String line = "";
		String assigned_lot = "";

		while ((line = br.readLine()) != null) {
			String carPlate = line;
			String listPath = "";

			System.out.println("================= ���� �ȳ� �ý��� =================");
			System.out.println(carPlate + "�� ��ȣ�ϴ� ������ ������ �ּ���");
			System.out.println("1. ������ ��/�ⱸ ��ó ");
			System.out.println("2. ���������� ��ó ");
			System.out.println("3. ����� ���� �¼�");
			System.out.println("4. ����");
			System.out.print("��ȣ�� �Է����ּ��� : ");
			user_input = sc.nextInt();

			switch (user_input) {
			case 1: {
				int dest = 0;// ������ ���ͼ����� ��ȣ�� ������ ����
				try {
					dest = find_dest("entrance", line, conn, stmt);
				} // ������ ���ͼ��� �� ������ ã�´�.
				catch (NullPointerException e) {
					System.out.println(e);
				}
				System.out.printf("Destination intersection: %d��\n\n", dest + 1);

				if (dest == -1) { // �������� ������ ��� dest���� -1�� return�ǵ��� �����س���
					System.out.println("�����忡 �����Ͻ� �� �����ϴ�! �ý����� ��ġ�ڽ��ϴ�");
					System.exit(0); // �ý��� ��ü ����
				}

				else {
					AstarSearch(n[0], n[dest], null);// ������ ���ͼ��Ǳ����� ��θ� ���

					gateUIPath = printPathTemp(n[0], n[dest], null); // list type �� path ����
					System.out.println("Path: " + gateUIPath);

					// System.out.println("gateUIPath in gateUI:" + gateUIPath);

					// �� �Ʒ� line�� �ϴ� ���� ���� �ּ�ó��. ���߿� Ǯ��� ��!
					// printDisplay(gateUIPath, carPlate);

					System.out.println();
					listPath = gateUIPath.stream().map(Object::toString).collect(Collectors.joining(", "));

					// System.out.println(listPath);

					String query = String.format("insert into cars (carPlate, parkArea, assignPath, beforeIndex)"
							+ " values ('%s', '%s', '%s', %d)", carPlate, totalAssign, listPath, 0);

					stmt.executeUpdate(query);
					assigned_lot = totalAssign;

					break;
				}

			}

			case 2: {
				int dest = 0;
				try {
					dest = find_dest("elevator", line, conn, stmt);
				} catch (NullPointerException e) {
					System.out.println(e);
				}
				System.out.printf("Destination intersection: %d��\n\n", dest + 1);

				if (dest == -1) {
					System.out.println("�����忡 �����Ͻ� �� �����ϴ�!");
					System.exit(0);
				} else {
					AstarSearch(n[0], n[dest], null);

					gateUIPath = printPathTemp(n[0], n[dest], null); // list type �� path ����
					System.out.println("Path: " + gateUIPath);

					// System.out.println("gateUIPath in gateUI:" + gateUIPath);

					// �� �Ʒ� line�� �ϴ� ���� ���� �ּ� ó��. ���߿� Ǯ����� ��!
					// printDisplay(gateUIPath, carPlate);

					System.out.println();
					listPath = gateUIPath.stream().map(Object::toString).collect(Collectors.joining(", "));

					String query = String.format("insert into cars (carPlate, parkArea, assignPath, beforeIndex)"
							+ " values ('%s', '%s', '%s', %d)", carPlate, totalAssign, listPath, 0);

					stmt.executeUpdate(query);
					assigned_lot = totalAssign;

					break;
				}

			}
			case 3: {
				int dest = 0;
				try {
					dest = find_dest("disabled", line, conn, stmt);

				} catch (NullPointerException e) {
					System.out.println(e);
				}
				System.out.printf("Destination intersection: %d��\n\n", dest + 1);

				if (dest == -1) {
					System.out.println("�����忡 �ܿ� ���� ������ �����ϴ�!");
					System.exit(0);
				} else {
					AstarSearch(n[0], n[dest], null);

					gateUIPath = printPathTemp(n[0], n[dest], null); // list type �� path ����
					System.out.println("Path: " + gateUIPath);

					// System.out.println("gateUIPath in gateUI:" + gateUIPath);

					// �� �Ʒ� line�� �ϴ� ���� ���� �ּ� ó��. ���߿� Ǯ����� ��!
					// printDisplay(gateUIPath, carPlate);

					System.out.println();
					listPath = gateUIPath.stream().map(Object::toString).collect(Collectors.joining(", "));

					String query = String.format("insert into cars (carPlate, parkArea, assignPath, beforeIndex)"
							+ " values ('%s', '%s', '%s', %d)", carPlate, totalAssign, listPath, 0);

					stmt.executeUpdate(query);
					assigned_lot = totalAssign;

					break;
				}

			}
			case 4: {
				System.out.println("�ý����� ��ġ�ڽ��ϴ�.");
				conn.close();
				stmt.close();
				break;
			}
			}// switch

			if (user_input == 4) {
				// break;
			}
		} // while

		return assigned_lot;

	}

	/*
	 * ������ �������� �ȳ� ���ø� ������ �ʰ� (���ǵ� �ƴϵ�) ���� �߸� ����� �� �ش� ���ͼ��ǿ��� �ٷ� ���� ������ ���� ���������ڰ�
	 * �̾߱� ���� �� ���� �Լ� �����ڰ� ������ �������� �� ȣ��Ǵ� �Լ��̸� ���� ������� �ʰ� ���� �׷��� ���߿� �ʿ��ϰ� �ɱ��(�ٸ�
	 * �Լ��� ġ������ ���� ���� ������) �ϴ� �������� �ʾ���
	 */
	@SuppressWarnings("null")
	public static int find_dest_err(Intersection current, String[] area_path, String numPlate, Intersection beforeGoal)
			throws IOException, SQLException {
		boolean firstFind = false;
		Intersection errIntersection = null;

		Statement stmt1 = conn.createStatement();

		for (int i = 0; i < parkArea.length; i++) {
			if (parkArea[i].nearIntersection == current) { // � ���� ������ ���� ���ͼ����� ���� ���ͼ����� ���
				if (parkArea[i].remainNum != 0) { // �ܿ������� �ִٸ�
					String name = parkArea[i].AreaName;
					int lot = parkArea[i].totalNum - parkArea[i].remainNum + 1;
					stmt1.executeUpdate("update cars set parkArea = '" + name + Integer.toString(lot) + "' "
							+ "where carPlate = '" + numPlate + "'");
					parkArea[i].remainNum--;
					// �Ҵ��ϰ� firstFind�� true�� �ٲ� --> ���� ���ͼ����� �迭 �� �ε����� ��ȯ�ϰ� ��
					firstFind = true;
				}
			}
		}
		if (firstFind)
			return current.index - 1;
		else {
			for (int i = 0; i < parkArea.length; i++) {
				if (parkArea[i].remainNum != 0) {
					if (parkArea[i].nearIntersection != beforeGoal) {
						String name = parkArea[i].AreaName;
						int lot = parkArea[i].totalNum - parkArea[i].remainNum + 1;
						stmt.executeQuery("update cars set parkArea = '" + name + Integer.toString(lot) + "' "
								+ "where numPlate = '" + numPlate + "'");
						parkArea[i].remainNum--;
						errIntersection = parkArea[i].nearIntersection;
					}
				}
			}
			return errIntersection.index - 1;
		}

	}

	/*
	 * ���ͼ����� �������� ȭ��ǥ�� ���� �ȳ��� �ϱ� ���� ������ ������ ���ͼ����� �˾ƾ� �� ���� �� ���ͼ����� ���� ���ͼ����� �ȳ��� ����
	 * �ڽ��� �ε����� �����ͺ��̽��� column �� beforeIntsc�� ���� ��� ���ͼ����� �ڽſ��� �ٰ����� ������ ���� �� �۾���
	 * �����ϸ� ���� �ȳ� ���� �ش� �۾��� ������
	 */
	public static void saveIntscIndex(int myIndex, Connection conn) throws IOException, SQLException {

		System.out.println("saveIntscIndex ȣ��");

		Statement stmt2 = conn.createStatement();

		BufferedReader br = new BufferedReader(new FileReader("intersection_getIn\\nowEnter.txt"));
		String numPlate = br.readLine();

		stmt2.executeUpdate(
				"update cars set beforeIndex='" + Integer.toString(myIndex) + "' where carPlate ='" + numPlate + "'");

	}

	/*
	 * detect.py�� intersection���� detect�� ���� ��ȣ���� ���ڿ��� intersection_getIn�̶�� ������
	 * �����ؾ���
	 */
	/* myIntersection�� �� ���ͼ����� ��ȣ�� �ǹ��ϸ� �� �κ��� �ϵ� �ڵ��Ǿ� ���ε� �ؾ� �Ѵ� */

	public static void intersectionDisplay(Connection conn, Statement stmt, int myIntersection)
			throws IOException, SQLException, InterruptedException {

		System.out.println("\nintersectionDisplay ȣ��");

		BufferedReader br = new BufferedReader(new FileReader("intersection_getIn\\nowEnter.txt"));
		String numPlate = br.readLine();

		int beforeIndex, currentIndex, nextIndex;

		List<Intersection> currentPath = new ArrayList<Intersection>();

		ResultSet rs_car = stmt
				.executeQuery("select parkArea,assignPath,beforeIndex from cars where carPlate = '" + numPlate + "'");

		rs_car.next();
		// while (rs_car.next()) {
		String[] area_path = { rs_car.getString(1), rs_car.getString(2), rs_car.getString(3) };
		area_path[1] = area_path[1].replaceAll(" ", "");
		int driverError = Arrays.asList(area_path[1].split(",")).indexOf(Integer.toString(myIntersection));
		// System.out.println("�����ڰ� ���� �߸�����ٸ� -1 ��� :"+driverError);

		int beforeIndex_sql = Integer.parseInt(area_path[2]);

		System.out.println("beforeIndex_sql:" + beforeIndex_sql);

		// if (beforeIndex_sql == null) {
		// beforeIndex_sql = myIntersection;
		// }
		//

		// �����ڰ� ���� �߸� �� ���
		if (driverError == -1) {
			mainSerial sr = new mainSerial("COM3");
			sr.pushToArduino(510); // Send redirection message to Arduino LCD module

			String goalAreaName = area_path[0].substring(0, 1);

			Intersection goalIntersection = parkArea[goalAreaName.charAt(0) - 65].nearIntersection; // ������
			AstarSearch(n[myIntersection - 1], goalIntersection, n[beforeIndex_sql - 1]);

			// List<Intersection> newPathMyIntersection = printPath(goalIntersection);
			List<Intersection> tempPath = printPathTemp(n[myIntersection - 1], goalIntersection,
					n[beforeIndex_sql - 1]);

			currentIndex = myIntersection - 1;
			// System.out.println("currentInx:" + currentIndex);
			// System.out.println("���Ӱ� ������ path�� " + tempPath);
			System.out.println("�ӽ÷� ���� printPathTemp�� ����� " + tempPath);

			String newPath = "";
			newPath = tempPath.stream().map(Object::toString).collect(Collectors.joining(", "));

			// tempPath DB�� assignPath ����
			stmt.executeUpdate("Update cars set assignPath='" + newPath + "' where carPlate='" + numPlate + "'");

			if (tempPath.size() != 1) {
				System.out.println(
						"(0~7 ���� ���)�����ڰ� ���� �߸� ������� ���Ӱ� ������ Path�� ���̰� 1���� �� ��,�׸��� ���ο� Path�� ���̴� " + tempPath.size());
				Intersection beforeIntsc = n[beforeIndex_sql - 1];
				// currentPath.add(beforeIntsc);
				// currentPath.addAll(tempPath);
				// ����
				currentPath.add(n[currentIndex]);
				// ������
				System.out.println("Current Index:" + currentIndex);

				// tempPath������ 7�� index
				int indexOf8 = tempPath.indexOf(n[currentIndex]);
				System.out.println(indexOf8);

				System.out.println(tempPath.get(indexOf8 + 1).toString());
				int nextIdx = Integer.parseInt(tempPath.get(indexOf8 + 1).toString()) - 1;
				System.out.println("nextIdx:" + nextIdx);
				currentPath.add(n[nextIdx]);

				System.out.println(currentPath);

				printDisplay(currentPath, numPlate);
			} else {
				System.out.println("�����ڰ� ���� �߸� ������� ���Ӱ� ������ Path�� ���̰� 1�� �� (0~7 ���� ���)");
				System.out.println("���� ���ͼ����� ��ȣ");
				System.out.println(currentIndex);
				// ����� �ƹ��͵� ����Ʈ ���� ��
			}

		} else { // �����ڰ� ����� ���� ���
			String[] assignPathArr = area_path[1].split(",");
			List<String> tempPath = Arrays.asList(assignPathArr);
			currentIndex = myIntersection - 1;
			if (tempPath.indexOf(Integer.toString(myIntersection)) != 0) {
				beforeIndex = Integer.parseInt(tempPath.get(tempPath.indexOf(Integer.toString(myIntersection)) - 1))
						- 1;

				currentPath.add(n[beforeIndex]);

			} else {
				System.out.println("���� ���ͼ����� �����ڿ��� ������ �Ҵ�� Path�� ù��° ���ͼ����̹Ƿ� ���� ���ͼ����� ��Ÿ���� beforeIndex ������ -1�� �Ҵ��Ѵ�");
				beforeIndex = 0;

			}
			int myIndexOfOriginPath = tempPath.indexOf(Integer.toString(myIntersection));

			if (myIndexOfOriginPath + 1 != tempPath.size()) {
				System.out.println("�����ڰ� ������ path��� �����ϰ� ������ ���� ���ͼ��� ���Ŀ��� ���İ����� ���ͼ����� �� �������� ��");
				nextIndex = Integer.parseInt(tempPath.get(myIndexOfOriginPath + 1)) - 1;
				System.out.println("���� ���ͼ����� ��ȣ, ���� ���ͼ����� ��ȣ, ������ ���������� ���ͼ����� ��ȣ");
				System.out.println(currentIndex + " " + beforeIndex + " " + nextIndex);
				// currentPath.add(n[beforeIndex]);
				currentPath.add(n[currentIndex]);
				currentPath.add(n[nextIndex]);

				System.out.println("Current Path: " + currentPath);

				printDisplay(currentPath, numPlate);

			} else {
				System.out.println("�����ڰ� ������ path��� �����ϰ� ������ ���� ���ͼ��� ���Ŀ� ���İ��� �� ���ͼ����� ���� ��");
				System.out.println("���� ���ͼ����� ��ȣ, ���� ���ͼ����� ��ȣ");
				System.out.println(currentIndex + " " + beforeIndex);
				// ���⵵ ����Ʈ �Ұ� ���� ��?
			}

		}
	}
	// }

	// main������ ����� �Է¹޾Ƽ� ������ �Լ��� ȣ���ϴ� ������ ����
	public static void main(String[] args) throws IOException, SQLException, InterruptedException {

		Scanner sc = new Scanner(System.in);
		int user_input = -1;
		
		
		// initialize database
		String init_cars = "truncate cars";
		String init_parkings = "truncate parkingareas";
		String assigned_lot = "";

		/* �������� �����ͺ��̽��� ���� */
		try {
			Class.forName(JDBC_driver);
			conn = DriverManager.getConnection(DB_url, User, password);
			stmt = conn.createStatement();
		} catch (Exception e) {
			e.printStackTrace();
		}

		// initialize query => truncate all tables
		try {
			pr_stmt = conn.prepareStatement(init_cars);
		} finally {
			pr_stmt.execute();
		}

		try {
			pr_stmt = conn.prepareStatement(init_parkings);
		} finally {
			pr_stmt.execute();
		}

		/* ���� ����, ���ͼ���, ���� ���� ����, ��ȣ���� ���� �켱���� ť, ������ ��ü, ���� ������ ���� ���� �ʱ�ȭ */
		setParkingArea();
		setIntersection();
		setArea(conn, stmt);
		setPriorityQueue();
		setDisplay();
		setParkAreaScore();

		// gate ����
		assigned_lot = gateUI(user_input, sc, conn, stmt);

		BufferedReader br = new BufferedReader(new FileReader("intersection_getIn/nowEnter.txt"));
		String line;
		line = br.readLine();
		String carPlate = line;

		/*
		 * ����� ������ AstarSearch()�� printPathTemp�� ���� ���� Ȯ���� ���� saveIntscIndex()�� ����
		 * ȣ���Ͽ����� �� ���ͼ��ǿ����� intersectionDisplay()-> saveIntscIndex() ������ ȣ��Ǿ�� ��
		 */

		boolean parkEnd = false;
		int nowIntscIndex = 1; // current intersection number

		System.out.println("carPlate:" + carPlate);
		
		String area_path= "";
		String goalAreaName = "";

		/* �ַܼ� ������ ���� �޾Ƽ� ��� */
		while (parkEnd == false) {
			String query = String.format("select parkArea from cars where carplate='%s'", carPlate);
			ResultSet rs_car = stmt.executeQuery(query);
			Intersection goalIntersection = null;
			while (rs_car.next()) {
				area_path = rs_car.getString(1);
				goalAreaName = area_path.substring(0, 1);
				
				System.out.println("goalAreaName:" + goalAreaName);
				
				
				goalIntersection = parkArea[goalAreaName.charAt(0) - 65].nearIntersection;

				System.out.println("nowIntscIndex:" + nowIntscIndex);
				System.out.println("goalIntersection:" + goalIntersection);

			}
			
			if (nowIntscIndex == goalIntersection.index) {
				parkEnd = true;
				mainSerial sr = new mainSerial("COM3");
				sr.pushToArduino(500); // Send destination message to Arduino LCD module
				break;
			}
			intersectionDisplay(conn, stmt, nowIntscIndex);
			System.out.println("intersectionDisplay ���� �Ϸ�\n");

			saveIntscIndex(nowIntscIndex, conn);
			System.out.println("saveIntscIndex ���� �Ϸ�\n");

			System.out.print("�����ڰ� ��� ���ͼ������� ������ �Է� : ");
			nowIntscIndex = sc.nextInt();
			sc.nextLine();

		}

		/*
		 * TO IMPLEMENT: [1] ������ �Ϸ��� ������ ĭ�� ���°� booked->parked�� ���� [2] ������ �����ϸ� ����ĭ ���°�
		 * parked->free�� ����
		 */

		// �Ÿ� ���� ��� ��Ʈ��ȣ: COM4
		mainSerial sr = new mainSerial("COM4");
		sr.receiveFromArduino();
//		System.out.println("Read from arduino:" + isParked);

		System.out.printf("%s ���� �Ϸ�!\n", area_path);
		String area = area_path.substring(0, 1);
		int lotnum = Integer.parseInt(area_path.substring(1));

		System.out.println("Area:" + area + " lotnum:" + lotnum);

		String query = String.format("update parkingareas set lot%d='parked' where areaName='%s'", lotnum, area);
		stmt.executeUpdate(query);

	}

}

class mainSerial {
	public static String port;

	mainSerial(String port) {
		this.port = port;
	}

	public static byte[] str2byteArr(String input) throws UnsupportedEncodingException {
		byte[] output = input.getBytes();

		return output;
	}

	public static void pushToArduino(int endSignal) throws IOException, InterruptedException {
		SerialPort sp = SerialPort.getCommPort(port);
		sp.setComPortParameters(9600, 8, 1, 0);
		sp.setComPortTimeouts(SerialPort.TIMEOUT_WRITE_BLOCKING, 0, 0);

		if (sp.openPort()) {
//			System.out.println("\nPort is open :)");
		} else {
//			System.out.println("\nFailed to open port :(");
			return;
		}

		for (Integer i = 0; i < 2; i++) {
			Integer space = 32;
			sp.getOutputStream().write(space.byteValue());
			sp.getOutputStream().flush();
//			System.out.println("Waiting....");
			Thread.sleep(1000);
		}

		System.out.println("Desination!");
		sp.getOutputStream().write(endSignal); // push 500
		sp.getOutputStream().flush();
		Thread.sleep(10);

		if (sp.closePort()) {
//			System.out.println("Port is closed :)");
		} else {
//			System.out.println("Failed to close port :(");
			return;
		}
	}

	public static int receiveFromArduino() {
		SerialPort sp = SerialPort.getCommPort(port);
		sp.setComPortParameters(9600, 8, 1, 0);
		sp.setComPortTimeouts(SerialPort.TIMEOUT_WRITE_BLOCKING, 0, 0);

		int portInput = -1;

		if (sp.openPort()) {
//			System.out.println("\nPort is open :)");
		} else {
//			System.out.println("\nFailed to open port :(");

		}

		try {
			int count = 0;
			while (true) {
				while (sp.bytesAvailable() == 0) {
					Thread.sleep(20);
				}

				byte[] readBuffer = new byte[sp.bytesAvailable()];
				portInput = sp.readBytes(readBuffer, readBuffer.length);
//				System.out.println("Read " + portInput + " bytes.");
				count ++;
				
				if (count == 2)
					break;
				
				
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		if (sp.closePort()) {
//			System.out.println("Port is closed :)");
		} else {
//			System.out.println("Failed to close port :(");
		}

		return portInput;

	}
}

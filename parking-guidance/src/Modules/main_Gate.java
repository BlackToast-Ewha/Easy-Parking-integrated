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
 * (1) Path를 화살표로 변환하는 작업 완료
 * (2) 변환한 화살표를 콘솔에 출력하는 작업 완료
 * (3) 주차 구역 생성 완료
 * (4) 인터섹션 생성 완료
 * (5) 목적지 주차 구역 인접 인터섹션의 인덱스 추출 완료
 * (6) AstarSearch()의 에러 수정 완료
 * (7) 쓰이지 않는 코드와 의미 없는 주석 정리 완료
 */

import java.util.PriorityQueue;
import java.util.Scanner;
import java.util.Set;
import java.util.stream.Collectors;

import javax.swing.plaf.synth.SynthSeparatorUI;

import com.fazecast.jSerialComm.SerialPort;

public class main_Gate {

	static Serial s = new Serial();
	/* 주차 구역, 인터섹션, 전광판 변수 선언 */
	static ParkingArea[][] area;
	public static Intersection[] n = new Intersection[8];
	static Area[] parkArea;
	static Display[][] display = new Display[8][4];

	/* 주차 구역을 담는 우선순위 큐와 주차 구역 이름을 담는 String변수 */
	static PriorityQueue<Area> entranceArea;
	static PriorityQueue<Area> elevatorArea;
	static PriorityQueue<Area> disabledArea;
	static String totalAssign;

	/* MySQL 데이터베이스 연결에 필요한 변수 선언 */
	static final String JDBC_driver = "com.mysql.cj.jdbc.Driver";
	static final String DB_url = "jdbc:mysql://localhost:3306/team15?useSSL=false&serverTimezone=UTC";
	static final String User = "root";
	static final String password = "root";
	static Connection conn = null;
	static Statement stmt = null;
	static PreparedStatement pr_stmt = null;

	/* Display 함수에 넘겨주기 위해 사용하는 List<Intersection> 변수 */
	static List<Intersection> originalPath;
	static List<Intersection> gateUIPath;

	/* 전광판 객체를 초기화하는 함수 */
	public static void setDisplay() {
		for (int i = 0; i < 8; i++) {
			for (int j = 0; j < 4; j++) {
				display[i][j] = new Display(i, j);
				// j=0 -> u, j=1 -> l, j=2 -> d, j=3 -> r 전광판을 나타냄,
			}
		}

	}

	/* 전광판에 화살표를 출력하는 함수 */
	public static void printDisplay(List<Intersection> path, String CarPlate) throws IOException, InterruptedException {

		System.out.println("\nprintDisplay 호출\n");

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
		// c = a; // 이전의 i 저장, previous Index
		// a = path.get(i).index; // current Index
		// b = path.get(i + 1).index; // next Index
		// }
		// 여기서 쪼갠다음에 하드코딩

		// /for (int i = 0; i < path.size() - 1; i++) { // path 에 intersection이 한 개일 때는
		// 실행되지 않음

		System.out.printf("previous:%d, current:%d, next:%d", c, a, b); // a, b, c: 1부터 시작되는 번호

		// a=7, b=8, c=3
		if ((b - a) == 1) { // (1->2, 2->3, 3->4, 4->5, 5->6, 7->8)
			if ((c - a) == -1) { // 정상 루트. 이전 인터섹션의 index 가 현재 인터섹션의 index 보다 작을 때
				if (a == 3) { // 234
					display[2][0].printLeft(CarPlate);// 3 번 인터섹션 }

				} else if (a == 4) {
					display[3][0].printLeft(CarPlate); // 4->5
				} else if (a == 7) {
					display[6][3].printRight(CarPlate);// 7->8
					System.out.println("378");
				} else // 12 23 56
					display[a - 1][0].printStraight(CarPlate);
			} else {// 이전의 인터섹션의 index 가 현재 인터섹션의 index 보다 높을 때
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
					System.out.println("restart from intersection3"); // 3에서
				// 재시작하여
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

		} else {// index가 이어지지 않은 경우.b-a != 1 1->6, 1->8, 2->5, 3->7, 그리고 그 반대 6->1, 8->1, 5->2,
				// 7->3
			if (c < a) {// index 가 작은데서 큰데로 왔을 때
				if (b > a) {// 016(816) 018 125 237
					if (a == 1 && b == 8)
						display[0][0].printRight(CarPlate); // 018 이런일 일어나면 안됨.

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

			} else {// index 가 큰데서 작은데로 왔을 때, c>a // 재탐색의 경우, 437 652 873 216 325
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

	/* 주차 구역 한 칸들을 초기화 하는 함수 */
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

		// 주차 공간 객체 선언, 나중에 데이터 베이스와 연동할 부분
		for (int i = 0; i < area.length; i++) {
			for (int j = 0; j < area[i].length; j++) {
				tempAreaName = (char) (i + 65);
				areaName = Character.toString(tempAreaName);
				if (disList.contains(areaName))
					mode = 'd';
				else
					mode = 'a';
				area[i][j] = new ParkingArea(areaName, 0, j, mode);
				// System.out.println("[setParkingArea()]"+areaName+"구역의 mode는 "+mode);
			}
		}
	}

	/* 주차장의 Intersection 초기화 */
	public static void setIntersection() throws NumberFormatException, IOException { // 앞에서 선언한 8 개의 intersection 에 객체를
																						// 생성하여 부여

		BufferedReader br = new BufferedReader(new FileReader("numIntersection.txt"));
		int numIntsc = Integer.parseInt(br.readLine());
		// 1~8 사이의 인덱스 부여
		for (int i = 0; i < numIntsc; i++) {
			n[i] = new Intersection((i + 1));

		}

		br = new BufferedReader(new FileReader("rowcol.txt"));
		String rowcol;
		String[] rowcolArr;
		int count = 0;
		/* row와 column 을 설정하는 곳 */
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

		// n[i].adjacencies = 한 인터섹션에서 연결된 edge들의 집합

		br = new BufferedReader(new FileReader("connectedIntsc.txt"));
		String connected;
		String[] tempArr;
		count = 0;
		Edge[] edges;

		while (true) {
			connected = br.readLine();
			// System.out.println("[setIntersection()]각 인터섹션에게 직접적으로 연결된 인터섹션들은
			// "+connected);
			if (connected == null)
				break;
			tempArr = connected.split(",");
			edges = new Edge[tempArr.length];

			for (int i = 0; i < edges.length; i++) {
				edges[i] = new Edge(n[Integer.parseInt(tempArr[i]) - 1], 1);
				// System.out.println("[setIntersection()]"+count+"인터섹션에게 추가된 edge가 타겟으로 하는
				// 인터섹션은 "+edges[i].target.index);
			}
			n[count].adjacencies = edges;
			count++;
		}

	}

	/*
	 * 주차 구역 (A,B 등)을 초기화 하는 함수 구역의 이름(알파벳), 잔여구역 수, 전체구역 수, 구역과 가까운 인터섹션을 지정하게 됨.
	 */
	public static void setArea(Connection conn, Statement stmt)
			throws NumberFormatException, IOException, SQLException {
		BufferedReader br = new BufferedReader(new FileReader("numArea.txt"));
		BufferedReader br1 = new BufferedReader(new FileReader("lotNumArea.txt"));
		BufferedReader br2 = new BufferedReader(new FileReader("intersectionIndex.txt"));

		int numArea = Integer.parseInt(br.readLine());
		String lotnums = br1.readLine();

		/*
		 * IMPLEMENTED!!! 데이터베이스 parkingAreas 테이블의 total column에 변수 저장 occupied 변수는 lot
		 * 변수들 중 free를 가지는 값 합산 remained 변수는 total - occupied
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
	 * 2018-08-01 AstarSearch가 parent 뿐만 아니라 child 노드를 설정하게 만들었음 따라서 기존의
	 * printPath()와 달리 자신의 자식노드를 확인하며 path를 출력하게 하는 함수 필요 그게 printPathTemp()임.
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

	/* 기존에 사용하던 path 출력함수 */
	public static List<Intersection> printPath(Intersection target) {
		List<Intersection> path = new ArrayList<Intersection>();

		for (Intersection node = target; node != null; node = node.parent) {
			path.add(node);
		}

		Collections.reverse(path);

		return path;
	}

	/*
	 * destination node(=goal)만 필요하던 기존의 AstarSearch를 source,goal,방금 지나온 인터섹션까지 고려하게
	 * 하는 함수로 변경
	 */
	public static void AstarSearch(Intersection source, Intersection goal, Intersection beforeIntsc) {

		/* 지나온 인터섹션들을 담는 객체 Set explored */
		Set<Intersection> explored = new HashSet<Intersection>();

		/*
		 * 후보가 되는 인터섹션을 담는 PriorityQueue queue f_score가 낮을 수록 우선 순위가 높다
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
				break; // 바로 while문 탈출.
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
					if (child != beforeIntsc) { // 연결되어있는 인터섹션이 방금 지나온 인터섹션이 아닌 경우에만 queue에 추가. 후진 안내를 막기 위한 코드임
						queue.add(child);
						current.child = child;
						if (child.index == goal.index)
							break;
					}

				}

			} // for

		}
	}

	/* 선호하는 구역 (엘리베이터, 게이트, 장애인 주차구역)에 따라 인접한 인터섹션의 인덱스를 반환 */
	public static int find_dest(String mode, String line, Connection conn, Statement stmt) throws SQLException {
		int findFlag = 0; // 주차 구역이 할당되었는지 체크하는 flag 변수
		String selectedAreaName = ""; // 상위 구역의 이름을 담을 String 변수
		int selectedLotNumber = 0; // 상위 구역 내에서 구역 번호를 담는 int 변수
		Intersection answer = null; // 선정된 주차 구역의 상위 구역의 인접 인터섹션을 담는 Intersection 변수

		if (mode.equals("entrance")) { // 입구 근처를 선택한 경우
			while (findFlag != 1) {
				Area tempArea = entranceArea.poll(); // 우선순위큐의 루트 노드를 꺼냄. 현재 주차장의 상황을 반영한 최선의 주차 구역 도출

				/*
				 * IMPLEMENTED!!!: tempArea.remained을 class 변수가 아닌 SQL에서 column으로 가져오기
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

					// 구역 할당
					selectedAreaName = tempArea.AreaName;
					selectedLotNumber = tempArea.totalNum - tempArea.remainNum + 1;

					/*
					 * IMPLEMENTED!!!: 해당 area의 lot 변수를 1로 변경 (in 할당) occupied ++ remained --
					 */
					query = String.format("update parkingareas set remained=%d, occupied=%d where areaName='%s'",
							sql_remained - 1, sql_occupied + 1, tempArea.AreaName);

					stmt.executeUpdate(query);

					tempArea.remainNum--; // in변수를 1로 바꾸는것과 동일한 역할을 함. 잔여 구역 수를 1 줄임

					entranceArea.add(tempArea); // peek()이 아니라 아예 큐 내에서 Area를 꺼냈으므로 다시 넣어줌
					answer = tempArea.nearIntersection; // 인접 인터섹션 받아옴
					findFlag = 1; // 주차 구역 할당 완료를 뜻하는 변수를 1로 바꿈
				} else {
					if (entranceArea.isEmpty()) // 주차장이 만차이면 findFlag 변수를 0으로 두고 (변경 없이) 루프 탈출
						break;
					continue; // 주차장은 만차가 아니지만 poll()한 주차 구역의 잔여 구역수가 0인 경우 다시 while로 돌아가서 계속 poll()
				}
			}

		}
		if (mode.equals("elevator")) {
			while (findFlag != 1) {
				Area tempArea = elevatorArea.poll(); // 우선순위큐의 루트 노드를 꺼냄. 현재 주차장의 상황을 반영한 최선의 주차 구역 도출

				/*
				 * IMPLEMENTED!!!: tempArea.remained을 class 변수가 아닌 SQL에서 column으로 가져오기
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

					// 구역 할당
					selectedAreaName = tempArea.AreaName;
					selectedLotNumber = tempArea.totalNum - tempArea.remainNum + 1;

					/*
					 * IMPLEMENTED!!!: 해당 area의 lot 변수를 1로 변경 (in 할당) occupied ++ remained --
					 */
					query = String.format("update parkingareas set remained=%d, occupied=%d where areaName='%s'",
							sql_remained - 1, sql_occupied + 1, tempArea.AreaName);

					stmt.executeUpdate(query);

					tempArea.remainNum--; // in변수를 1로 바꾸는것과 동일한 역할을 함. 잔여 구역 수를 1 줄임

					elevatorArea.add(tempArea); // peek()이 아니라 아예 큐 내에서 Area를 꺼냈으므로 다시 넣어줌
					answer = tempArea.nearIntersection; // 인접 인터섹션 받아옴
					findFlag = 1; // 주차 구역 할당 완료를 뜻하는 변수를 1로 바꿈
				} else {
					if (elevatorArea.isEmpty()) // 주차장이 만차이면 findFlag 변수를 0으로 두고 (변경 없이) 루프 탈출
						break;
					continue; // 주차장은 만차가 아니지만 poll()한 주차 구역의 잔여 구역수가 0인 경우 다시 while로 돌아가서 계속 poll()
				}
			}
		}
		if (mode.equals("disabled")) {
			while (findFlag != 1) {
				Area tempArea = disabledArea.poll(); // 우선순위큐의 루트 노드를 꺼냄. 현재 주차장의 상황을 반영한 최선의 주차 구역 도출

				/*
				 * IMPLEMENTED!!!: tempArea.remained을 class 변수가 아닌 SQL에서 column으로 가져오기
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

					// 구역 할당
					selectedAreaName = tempArea.AreaName;
					selectedLotNumber = tempArea.totalNum - tempArea.remainNum + 1;

					/*
					 * IMPLEMENTED!!!: 해당 area의 lot 변수를 1로 변경 (in 할당) occupied ++ remained --
					 */
					query = String.format("update parkingareas set remained=%d, occupied=%d where areaName='%s'",
							sql_remained - 1, sql_occupied + 1, tempArea.AreaName);

					stmt.executeUpdate(query);

					tempArea.remainNum--; // in변수를 1로 바꾸는것과 동일한 역할을 함. 잔여 구역 수를 1 줄임

					disabledArea.add(tempArea); // peek()이 아니라 아예 큐 내에서 Area를 꺼냈으므로 다시 넣어줌
					answer = tempArea.nearIntersection; // 인접 인터섹션 받아옴
					findFlag = 1; // 주차 구역 할당 완료를 뜻하는 변수를 1로 바꿈
				} else {
					if (disabledArea.isEmpty()) // 주차장이 만차이면 findFlag 변수를 0으로 두고 (변경 없이) 루프 탈출
						break;
					continue; // 주차장은 만차가 아니지만 poll()한 주차 구역의 잔여 구역수가 0인 경우 다시 while로 돌아가서 계속 poll()
				}
			}
		}
		if (findFlag == 0) {
			System.out.printf("%s 고객님\n", line);
			System.out.println("[만차]주차장에 입차하실 수 없습니다!!");
			return -1;
		}
		totalAssign = selectedAreaName + selectedLotNumber;

		// Console message
		System.out.println("\n===================================");
		System.out.printf("%s 고객님\n", line);
		System.out.println("배정된 주차구역은 " + selectedAreaName + selectedLotNumber + "입니다.");
		System.out.println("===================================\n");

		String query = String.format("update parkingareas set lot%d='booked' where areaName='%s'", selectedLotNumber,
				selectedAreaName);
		stmt.executeUpdate(query);

		return answer.index - 1; // (0~7 사이의 인덱스 반환)
	}

	/*
	 * 입구, 엘리베이터, 장애인 주차 구역에 따른 각각의 우선순위큐를 설정함. 우선순위는 선호구역으로부터의 거리^2+잔여구역/전체구역수 비율로
	 * 결정
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

	/* 인터섹션의 h_score를 유동적으로 계산 */
	public static void setIntersectionInfo(Intersection goal) {
		for (int i = 0; i < 8; i++) {
			n[i].calculateHScore(goal);
		}
	}

	/* 선호도에 맞게 주차 구역의 점수를 계산하는 함수 */
	/* 시간 복잡도가 높으나 주차장 초기 세팅에만 쓰이는 함수이므로 하루에 1회 이하로 호출됨 */
	public static void setParkAreaScore() throws IOException {
		int setCount = 0;
		/* 주차장 관리자가 입력한 정보에 맞추어 점수 계산 */
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

	// 차량 입차시에 실행
	public static String gateUI(int user_input, Scanner sc, Connection conn, Statement stmt)
			throws IOException, SQLException, InterruptedException {

		BufferedReader br = new BufferedReader(new FileReader("nowEnter.txt"));
		String line = "";
		String assigned_lot = "";

		while ((line = br.readLine()) != null) {
			String carPlate = line;
			String listPath = "";

			System.out.println("================= 주차 안내 시스템 =================");
			System.out.println(carPlate + "님 선호하는 구역을 선택해 주세요");
			System.out.println("1. 주차장 입/출구 근처 ");
			System.out.println("2. 엘리베이터 근처 ");
			System.out.println("3. 장애인 전용 좌석");
			System.out.println("4. 종료");
			System.out.print("번호를 입력해주세요 : ");
			user_input = sc.nextInt();

			switch (user_input) {
			case 1: {
				int dest = 0;// 목적지 인터섹션의 번호를 저장할 변수
				try {
					dest = find_dest("entrance", line, conn, stmt);
				} // 목적지 인터섹션 및 구역을 찾는다.
				catch (NullPointerException e) {
					System.out.println(e);
				}
				System.out.printf("Destination intersection: %d번\n\n", dest + 1);

				if (dest == -1) { // 주차장이 만차인 경우 dest에는 -1이 return되도록 설정해놨음
					System.out.println("주차장에 입차하실 수 없습니다! 시스템을 마치겠습니다");
					System.exit(0); // 시스템 전체 종료
				}

				else {
					AstarSearch(n[0], n[dest], null);// 목적지 인터섹션까지의 경로를 출력

					gateUIPath = printPathTemp(n[0], n[dest], null); // list type 의 path 받음
					System.out.println("Path: " + gateUIPath);

					// System.out.println("gateUIPath in gateUI:" + gateUIPath);

					// 이 아래 line은 일단 실행 위해 주석처리. 나중에 풀어야 함!
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
				System.out.printf("Destination intersection: %d번\n\n", dest + 1);

				if (dest == -1) {
					System.out.println("주차장에 입차하실 수 없습니다!");
					System.exit(0);
				} else {
					AstarSearch(n[0], n[dest], null);

					gateUIPath = printPathTemp(n[0], n[dest], null); // list type 의 path 받음
					System.out.println("Path: " + gateUIPath);

					// System.out.println("gateUIPath in gateUI:" + gateUIPath);

					// 이 아래 line은 일단 실행 위해 주석 처리. 나중에 풀어줘야 함!
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
				System.out.printf("Destination intersection: %d번\n\n", dest + 1);

				if (dest == -1) {
					System.out.println("주차장에 잔여 주차 구역이 없습니다!");
					System.exit(0);
				} else {
					AstarSearch(n[0], n[dest], null);

					gateUIPath = printPathTemp(n[0], n[dest], null); // list type 의 path 받음
					System.out.println("Path: " + gateUIPath);

					// System.out.println("gateUIPath in gateUI:" + gateUIPath);

					// 이 아래 line은 일단 실행 위해 주석 처리. 나중에 풀어줘야 함!
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
				System.out.println("시스템을 마치겠습니다.");
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
	 * 차량이 전광판의 안내 지시를 따르지 않고 (고의든 아니든) 길을 잘못 들었을 때 해당 인터섹션에서 바로 주차 구역을 새로 설정해주자고
	 * 이야기 했을 때 만든 함수 운전자가 에러를 일으켰을 때 호출되는 함수이며 현재 사용하지 않고 있음 그러나 나중에 필요하게 될까봐(다른
	 * 함수의 치명적인 에러 등의 문제로) 일단 삭제하지 않았음
	 */
	@SuppressWarnings("null")
	public static int find_dest_err(Intersection current, String[] area_path, String numPlate, Intersection beforeGoal)
			throws IOException, SQLException {
		boolean firstFind = false;
		Intersection errIntersection = null;

		Statement stmt1 = conn.createStatement();

		for (int i = 0; i < parkArea.length; i++) {
			if (parkArea[i].nearIntersection == current) { // 어떤 주차 구역의 인접 인터섹션이 현재 인터섹션인 경우
				if (parkArea[i].remainNum != 0) { // 잔여구역이 있다면
					String name = parkArea[i].AreaName;
					int lot = parkArea[i].totalNum - parkArea[i].remainNum + 1;
					stmt1.executeUpdate("update cars set parkArea = '" + name + Integer.toString(lot) + "' "
							+ "where carPlate = '" + numPlate + "'");
					parkArea[i].remainNum--;
					// 할당하고 firstFind를 true로 바꿈 --> 현재 인터섹션의 배열 내 인덱스를 반환하게 됨
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
	 * 인터섹션은 차량에게 화살표로 방향 안내를 하기 위해 이전에 지나온 인터섹션을 알아야 함 따라서 각 인터섹션은 다음 인터섹션의 안내를 위해
	 * 자신의 인덱스를 데이터베이스의 column 중 beforeIntsc에 저장 모든 인터섹션은 자신에게 다가오는 차량에 대해 이 작업을
	 * 수행하며 방향 안내 이후 해당 작업을 수행함
	 */
	public static void saveIntscIndex(int myIndex, Connection conn) throws IOException, SQLException {

		System.out.println("saveIntscIndex 호출");

		Statement stmt2 = conn.createStatement();

		BufferedReader br = new BufferedReader(new FileReader("intersection_getIn\\nowEnter.txt"));
		String numPlate = br.readLine();

		stmt2.executeUpdate(
				"update cars set beforeIndex='" + Integer.toString(myIndex) + "' where carPlate ='" + numPlate + "'");

	}

	/*
	 * detect.py가 intersection에서 detect한 차량 번호판의 문자열은 intersection_getIn이라는 폴더에
	 * 저장해야함
	 */
	/* myIntersection은 각 인터섹션의 번호를 의미하며 이 부분은 하드 코딩되어 업로드 해야 한다 */

	public static void intersectionDisplay(Connection conn, Statement stmt, int myIntersection)
			throws IOException, SQLException, InterruptedException {

		System.out.println("\nintersectionDisplay 호출");

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
		// System.out.println("운전자가 길을 잘못들었다면 -1 출력 :"+driverError);

		int beforeIndex_sql = Integer.parseInt(area_path[2]);

		System.out.println("beforeIndex_sql:" + beforeIndex_sql);

		// if (beforeIndex_sql == null) {
		// beforeIndex_sql = myIntersection;
		// }
		//

		// 운전자가 길을 잘못 든 경우
		if (driverError == -1) {
			mainSerial sr = new mainSerial("COM3");
			sr.pushToArduino(510); // Send redirection message to Arduino LCD module

			String goalAreaName = area_path[0].substring(0, 1);

			Intersection goalIntersection = parkArea[goalAreaName.charAt(0) - 65].nearIntersection; // 가져옴
			AstarSearch(n[myIntersection - 1], goalIntersection, n[beforeIndex_sql - 1]);

			// List<Intersection> newPathMyIntersection = printPath(goalIntersection);
			List<Intersection> tempPath = printPathTemp(n[myIntersection - 1], goalIntersection,
					n[beforeIndex_sql - 1]);

			currentIndex = myIntersection - 1;
			// System.out.println("currentInx:" + currentIndex);
			// System.out.println("새롭게 지정된 path는 " + tempPath);
			System.out.println("임시로 만든 printPathTemp의 출력은 " + tempPath);

			String newPath = "";
			newPath = tempPath.stream().map(Object::toString).collect(Collectors.joining(", "));

			// tempPath DB의 assignPath 수정
			stmt.executeUpdate("Update cars set assignPath='" + newPath + "' where carPlate='" + numPlate + "'");

			if (tempPath.size() != 1) {
				System.out.println(
						"(0~7 사이 출력)운전자가 길을 잘못 들었으며 새롭게 지정된 Path의 길이가 1보다 길 때,그리고 새로운 Path의 길이는 " + tempPath.size());
				Intersection beforeIntsc = n[beforeIndex_sql - 1];
				// currentPath.add(beforeIntsc);
				// currentPath.addAll(tempPath);
				// 현재
				currentPath.add(n[currentIndex]);
				// 다음껄
				System.out.println("Current Index:" + currentIndex);

				// tempPath에서의 7의 index
				int indexOf8 = tempPath.indexOf(n[currentIndex]);
				System.out.println(indexOf8);

				System.out.println(tempPath.get(indexOf8 + 1).toString());
				int nextIdx = Integer.parseInt(tempPath.get(indexOf8 + 1).toString()) - 1;
				System.out.println("nextIdx:" + nextIdx);
				currentPath.add(n[nextIdx]);

				System.out.println(currentPath);

				printDisplay(currentPath, numPlate);
			} else {
				System.out.println("운전자가 길을 잘못 들었으며 새롭게 지정된 Path의 길이가 1일 때 (0~7 사이 출력)");
				System.out.println("현재 인터섹션의 번호");
				System.out.println(currentIndex);
				// 여기는 아무것도 프린트 하지 않
			}

		} else { // 운전자가 제대로 가는 경우
			String[] assignPathArr = area_path[1].split(",");
			List<String> tempPath = Arrays.asList(assignPathArr);
			currentIndex = myIntersection - 1;
			if (tempPath.indexOf(Integer.toString(myIntersection)) != 0) {
				beforeIndex = Integer.parseInt(tempPath.get(tempPath.indexOf(Integer.toString(myIntersection)) - 1))
						- 1;

				currentPath.add(n[beforeIndex]);

			} else {
				System.out.println("현재 인터섹션은 운전자에게 기존에 할당된 Path의 첫번째 인터섹션이므로 이전 인터섹션을 나타내는 beforeIndex 변수에 -1을 할당한다");
				beforeIndex = 0;

			}
			int myIndexOfOriginPath = tempPath.indexOf(Integer.toString(myIntersection));

			if (myIndexOfOriginPath + 1 != tempPath.size()) {
				System.out.println("운전자가 기존의 path대로 운전하고 있으며 현재 인터섹션 이후에도 거쳐가야할 인터섹션이 더 남아있을 때");
				nextIndex = Integer.parseInt(tempPath.get(myIndexOfOriginPath + 1)) - 1;
				System.out.println("현재 인터섹션의 번호, 이전 인터섹션의 번호, 다음에 지나가야할 인터섹션의 번호");
				System.out.println(currentIndex + " " + beforeIndex + " " + nextIndex);
				// currentPath.add(n[beforeIndex]);
				currentPath.add(n[currentIndex]);
				currentPath.add(n[nextIndex]);

				System.out.println("Current Path: " + currentPath);

				printDisplay(currentPath, numPlate);

			} else {
				System.out.println("운전자가 기존의 path대로 운전하고 있으며 현재 인터섹션 이후에 거쳐가야 할 인터섹션이 없을 때");
				System.out.println("현재 인터섹션의 번호, 이전 인터섹션의 번호");
				System.out.println(currentIndex + " " + beforeIndex);
				// 여기도 프린트 할거 없을 듯?
			}

		}
	}
	// }

	// main에서는 사용자 입력받아서 적절한 함수만 호출하는 식으로 변경
	public static void main(String[] args) throws IOException, SQLException, InterruptedException {

		Scanner sc = new Scanner(System.in);
		int user_input = -1;
		
		
		// initialize database
		String init_cars = "truncate cars";
		String init_parkings = "truncate parkingareas";
		String assigned_lot = "";

		/* 주차장의 데이터베이스와 연결 */
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

		/* 주차 구역, 인터섹션, 상위 주차 구역, 선호도에 따른 우선순위 큐, 전광판 객체, 주차 구역이 갖는 점수 초기화 */
		setParkingArea();
		setIntersection();
		setArea(conn, stmt);
		setPriorityQueue();
		setDisplay();
		setParkAreaScore();

		// gate 버전
		assigned_lot = gateUI(user_input, sc, conn, stmt);

		BufferedReader br = new BufferedReader(new FileReader("intersection_getIn/nowEnter.txt"));
		String line;
		line = br.readLine();
		String carPlate = line;

		/*
		 * 현재는 수정된 AstarSearch()와 printPathTemp의 에러 여부 확인을 위해 saveIntscIndex()를 먼저
		 * 호출하였지만 각 인터섹션에서는 intersectionDisplay()-> saveIntscIndex() 순서로 호출되어야 함
		 */

		boolean parkEnd = false;
		int nowIntscIndex = 1; // current intersection number

		System.out.println("carPlate:" + carPlate);
		
		String area_path= "";
		String goalAreaName = "";

		/* 콘솔로 차량의 방향 받아서 출력 */
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
			System.out.println("intersectionDisplay 실행 완료\n");

			saveIntscIndex(nowIntscIndex, conn);
			System.out.println("saveIntscIndex 실행 완료\n");

			System.out.print("운전자가 어느 인터섹션으로 갔는지 입력 : ");
			nowIntscIndex = sc.nextInt();
			sc.nextLine();

		}

		/*
		 * TO IMPLEMENT: [1] 주차를 완료한 차량의 칸의 상태가 booked->parked로 변경 [2] 차량이 출차하면 주차칸 상태가
		 * parked->free로 변경
		 */

		// 거리 센서 통신 포트번호: COM4
		mainSerial sr = new mainSerial("COM4");
		sr.receiveFromArduino();
//		System.out.println("Read from arduino:" + isParked);

		System.out.printf("%s 주차 완료!\n", area_path);
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

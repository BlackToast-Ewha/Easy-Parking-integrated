package manager;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Scanner;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import java.io.IOException;

public class SettingAreaScore extends JFrame{
	/* 내부 클래스 numArea의 변수들 */
	JPanel numArea_panel;
	JLabel numArea_label;
	JTextField numArea_textField;
	JButton numArea_save;
	int flag = 0;
	int lotNums;
	
	/* 내부 클래스 lotNumSet의 변수들 */
	JLabel lotNumSet_label;
	JTextField[] lotNumSet_lotNums;
	JButton lotNumSet_saveButton;
	
	/*내부 클래스 setIntscNums의 변수들 */
	JLabel sin_label;
	JTextField sin_field;
	JButton sin_save;
	
	/*내부 클래스 setRowCol의 변수들*/
	JLabel src_label;
	JTextField[][] src_field;
	JButton src_save;
	
	/*내부 클래스 setAdjIntsc의 변수들*/
	JLabel sai_label;
	JTextField[] sai_field;
	JButton sai_save;
	
	/*내부 클래스 setConnectedIntsc의 변수들*/
	JLabel sci_label;
	JTextField[] sci_field;
	JButton sci_save;
	
	/*내부 클래스 setPreferPriority의 변수들 */
	JPanel panel;
	JLabel getNumAreaLabel;
	JTextField getNumAreaField;
	JLabel getDisAreaNameLabel;
	JTextField getDisAreaNameField;
	JLabel gateLabel, elevLabel, disLabel;
	JTextField gateField, elevField, disField;
	JButton saveButton;
	JPanel buttonPanel;
	JButton saveNumButton;

	myac my = new myac();

	class myac implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			if (e.getSource() == numArea_save) {
				try {
					PrintWriter pw = new PrintWriter("numArea.txt");
					String strArea = numArea_textField.getText();
					lotNums = Integer.parseInt(strArea);
					pw.write(strArea);
					pw.close();
					
					
				} catch (Exception e1) {
					System.out.println(e1.getMessage());
				}
				
				new lotNumSet();
				
			}
			if(e.getSource() == lotNumSet_saveButton){
				try{
					PrintWriter pw = new PrintWriter("lotNumArea.txt");
					for(int i = 0; i < lotNumSet_lotNums.length; i++)
						pw.write(lotNumSet_lotNums[i].getText()+" ");
					pw.close();
				}
				catch(Exception e1){
					System.out.println(e1.getMessage());
				}
				new setIntscNum();
			}
			
			if(e.getSource() == sin_save){
				try{
					PrintWriter pw = new PrintWriter("numIntersection.txt");
					pw.write(sin_field.getText());
					pw.close();
				}
				catch(Exception e1){
					System.out.println(e1.getMessage());
				}
				
				new setRowCol();
			}
			if(e.getSource() == src_save){
				String row,col;				
				try{
					PrintWriter pw = new PrintWriter("rowcol.txt");
					for(int i = 0; i < src_field.length; i++){
						row = src_field[i][0].getText();
						col = src_field[i][1].getText();
						pw.println(row+" "+col);
						
					}
					pw.close();
					
				}
				catch(Exception e1){
					System.out.println(e1.getMessage());
				}
				new setAdjIntsc();
			}
			
			if(e.getSource() == sai_save){
				try{
					PrintWriter pw = new PrintWriter("intersectionIndex.txt");
					for(int i = 0; i < sai_field.length; i++){
						pw.println(sai_field[i].getText());
					}
					pw.close();
				}
				catch(Exception e1){
					System.out.println(e1.getMessage());
				}
				
				new setConnectedIntsc();
			}
			
			if(e.getSource() == sci_save){
				try{
					PrintWriter pw = new PrintWriter("connectedIntsc.txt");
					for(int i = 0; i < sci_field.length; i++){
						pw.println(sci_field[i].getText());
					}
					pw.close();
				}
				catch(Exception e1){
					System.out.println(e1.getMessage());
				}
				new setPreferPriority();
			}
			if (e.getSource() == saveButton) {
				try {
					PrintWriter pw1 = new PrintWriter("managerUI_Test.txt");
					PrintWriter pw2 = new PrintWriter("disabledArea_set.txt");
					String gateArea = gateField.getText();
					pw1.println(gateArea);
					String elevArea = elevField.getText();
					pw1.println(elevArea);
					String disArea = disField.getText();
					pw1.println(disArea);

					String disAreaName = getDisAreaNameField.getText();
					pw2.write(disAreaName);

					pw1.close();
					pw2.close();
				} catch (IOException e1) {
					System.out.println(e1.getMessage());
				}
				System.exit(0);
			}
		}
	}
	
	class setConnectedIntsc extends JFrame{
		int numArea;
		setConnectedIntsc(){
			setSize(300,600);
			setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			setTitle("[admin] 연결된 인터섹션 설정");
			BoxLayout layout = new BoxLayout(this.getContentPane(), BoxLayout.Y_AXIS);
			this.setLayout(layout);
			
			try{
				BufferedReader br = new BufferedReader(new FileReader("numIntersection.txt"));
				numArea = Integer.parseInt(br.readLine());
			}
			catch(Exception e1){
				System.out.println(e1.getMessage());
			}
			
			sci_label = new JLabel("직접적으로 연결된 인터섹션들을 입력해주세요");
			sci_field = new JTextField[numArea];
			sci_save = new JButton("[연결된 인터섹션 지정]저장");
			sci_save.addActionListener(my);
			
			this.add(sci_label);
			for(int i = 0; i < numArea; i++){
				sci_field[i] = new JTextField(10);
				this.add(sci_field[i]);
			}
			this.add(sci_save);
			
			setVisible(true);
		}
	}
	
	class setPreferPriority extends JFrame{
		setPreferPriority(){
			setSize(800, 300);
			setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			setTitle("[admin] 주차장 환경설정");
			BoxLayout layout = new BoxLayout(this.getContentPane(), BoxLayout.Y_AXIS);
			this.setLayout(layout);
			// setLayout(new BoxLayout(this,BoxLayout.Y_AXIS));

			 //panel = new JPanel(new GridLayout(6,1));
			buttonPanel = new JPanel(new FlowLayout());
			//getNumAreaLabel = new JLabel("주차장 구역의 개수를 입력하세요", SwingConstants.CENTER);
			getDisAreaNameLabel = new JLabel("장애인 주차 구역의 이름을 입력하세요", SwingConstants.CENTER);
			gateLabel = new JLabel("주차장 입/출구 선호도 기준으로 주차 구역을 입력하세요", SwingConstants.CENTER);
			elevLabel = new JLabel("엘리베이터 선호도 기준으로 주차 구역을 입력하세요", SwingConstants.CENTER);
			disLabel = new JLabel("장애인 구역 선호도 기준으로 주차 구역을 입력하세요", SwingConstants.CENTER);
			gateField = new JTextField(15);
			elevField = new JTextField(15);
			disField = new JTextField(15);
			//getNumAreaField = new JTextField(15);
			getDisAreaNameField = new JTextField(15);
			saveNumButton = new JButton("저장");
			gateField.setSize(2, 1);
			saveButton = new JButton("변경 사항 저장");

			//this.add(getNumAreaLabel);
			//this.add(getNumAreaField);
			//this.add(saveNumButton);
			this.add(getDisAreaNameLabel);
			this.add(getDisAreaNameField);
			this.add(gateLabel);
			this.add(gateField);
			this.add(elevLabel);
			this.add(elevField);
			this.add(disLabel);
			this.add(disField);
//			// buttonPanel.add(saveButton);
			saveNumButton.addActionListener(my);
			saveButton.addActionListener(my);
			saveButton.setHorizontalAlignment(0);
			saveButton.setLocation(400, 10);
//			// his.add(panel);
			this.add(saveButton, BorderLayout.SOUTH);

			 setVisible(true);
		}
	}
	
	class setAdjIntsc extends JFrame{
		int numOfArea;
		setAdjIntsc(){
			setSize(200,500);
			setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			setTitle("[admin]인접 인터섹션 설정");
			BoxLayout layout = new BoxLayout(this.getContentPane(),BoxLayout.Y_AXIS);
			this.setLayout(layout);
			
			try{
				BufferedReader br = new BufferedReader(new FileReader("numArea.txt"));
				numOfArea = Integer.parseInt(br.readLine());
			}catch(Exception e1){
				System.out.println(e1.getMessage());
			}
			
			sai_field = new JTextField[numOfArea];
			
			sai_label =  new JLabel("각 주차 구역에 가장 가까운 인터섹션의 번호를 입력해주세요");
			for(int i = 0; i < numOfArea; i++){
				sai_field[i] = new JTextField(4);
			}
			sai_save = new JButton("[인접 인터섹션 지정]저장");
			sai_save.addActionListener(my);
			
			this.add(sai_label);
			for(int i = 0; i < numOfArea; i++){
				this.add(sai_field[i]);
			}
			this.add(sai_save);
			
			setVisible(true);
		}
	}
	class setIntscNum extends JFrame{
		setIntscNum(){
			setSize(200,200);
			setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			setTitle("[admin]인터섹션 개수 설정");
			BoxLayout layout = new BoxLayout(this.getContentPane(), BoxLayout.Y_AXIS);
			this.setLayout(layout);
			
			sin_label = new JLabel("인터섹션의 개수를 입력하세요");
			sin_field = new JTextField(5);
			sin_save = new JButton("[인터섹션 개수]저장");
			sin_save.addActionListener(my);
			
			this.add(sin_label);
			this.add(sin_field);
			this.add(sin_save);
			
			setVisible(true);
			
		}
	}
	
	class setRowCol extends JFrame{
		int numIntsc;
		JPanel fieldPanel;
		setRowCol(){
			setSize(400,500);
			setLayout(new FlowLayout());
			try{
				BufferedReader br = new BufferedReader(new FileReader("numIntersection.txt"));
				String numStr = br.readLine();
				numIntsc = Integer.parseInt(numStr);
				
			}
			catch(Exception e1){
				System.out.println(e1.getMessage());
			}
			fieldPanel = new JPanel(new GridLayout(numIntsc,2));
			src_field = new JTextField[numIntsc][2];
			
			src_label = new JLabel("각 인터섹션의 행과 열 번호를 입력하세요");
			src_save = new JButton("[인터섹션 행렬]저장");
			src_save.addActionListener(my);
			
			for(int i = 0; i < numIntsc; i++){
				src_field[i][0] = new JTextField(2);
				src_field[i][1] = new JTextField(2);
				fieldPanel.add(src_field[i][0]);
				fieldPanel.add(src_field[i][1]);
				
			}
			
			this.add(src_label);
			this.add(fieldPanel);
			this.add(src_save);
			
			setVisible(true);
			
		}
	}

	class numArea extends JFrame {
		numArea() {
			setSize(300, 300);
			setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			setTitle("[admin] 주차 구역 개수 설정");
			BoxLayout layout = new BoxLayout(this.getContentPane(), BoxLayout.Y_AXIS);
			this.setLayout(layout);

			numArea_label = new JLabel("주차 구역의 개수를 입력하세요 ");
			numArea_textField = new JTextField(10);
			numArea_save = new JButton("[구역 개수]저장");

			numArea_save.addActionListener(my);

			this.add(numArea_label);
			this.add(numArea_textField);
			this.add(numArea_save);

			setVisible(true);
			
			if(flag == 1)
				setVisible(false);
		}
	}

	class lotNumSet extends JFrame {

		lotNumSet() {

			setSize(300, 800);
			setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			setTitle("[admin] 주차 구역 개수 환경설정");
			lotNumSet_label = new JLabel("각 주차 구역이 가지고 있는 주차 공간의 개수를 입력하세요");
			lotNumSet_lotNums = new JTextField[lotNums];

			BoxLayout layout = new BoxLayout(this.getContentPane(), BoxLayout.Y_AXIS);
			this.setLayout(layout);
			lotNumSet_saveButton = new JButton("[주차 공간 개수]저장");

			this.add(lotNumSet_label);
			for (int i = 0; i < lotNums; i++) {
				lotNumSet_lotNums[i] = new JTextField(5);
				this.add(lotNumSet_lotNums[i]);
			}
			lotNumSet_saveButton.addActionListener(my);
			this.add(lotNumSet_saveButton);

			setVisible(true);
		}

	}
	
	public SettingAreaScore(){
		new numArea();
	}
	public static void main(String[] args) {
		Scanner sc = new Scanner(System.in);
		String temp;

		SettingAreaScore admin = new SettingAreaScore();
		//new numArea();
	}

}

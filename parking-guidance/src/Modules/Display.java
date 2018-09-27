package Modules;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import com.fazecast.jSerialComm.SerialPort;

public class Display {

	public int index;
	public int dir;

	public Display() {
	}

	public Display(int i, int j) {
		this.index = i;
		dir = j;

	}

	Serial s = new Serial();

	String dirToString() {
		if (dir == 0)
			return "up";
		else if (dir == 1)
			return "left";
		else if (dir == 2)
			return "down";
		else
			return "right";
	}

	void printRight(String carPlate) throws IOException, InterruptedException {

//		if (isCalled == 1) {
			System.out.printf("\nprintRight 호출! curr=%d dir=%d\n\n", this.index, dir);

			String num = carPlate.substring(3, 7);
			System.out.print(this.index + 1 + this.dirToString() + " ->");
			String input = num.concat("0");
			s.pushToArduino(input);
//		}
	}

	void printLeft(String carPlate) throws IOException, InterruptedException {

//		if (isCalled == 1) {
			System.out.printf("\nprintLeft 호출! curr=%d dir=%d\n\n", this.index, dir);

			System.out.print(this.index + 1 + this.dirToString() + " <-");
			String num = carPlate.substring(3, 7);

			String input = num.concat("1");
			s.pushToArduino(input);
//		}
	}

	void printStraight(String carPlate) throws IOException, InterruptedException {

//		if (isCalled == 1) {
			System.out.printf("\nprintStraight 호출! curr=%d dir=%d\n\n", this.index, dir);

			System.out.print(this.index + 1 + this.dirToString() + " 직진");
			String num = carPlate.substring(3, 7);

			String input = num.concat("2");
			s.pushToArduino(input);
//		}
	}

}

class Serial {
	// static String carPlate = "10나1121";

	public static byte[] str2byteArr(String input) throws UnsupportedEncodingException {
		byte[] output = input.getBytes();

		return output;
	}

	public static void pushToArduino(String inputInt) throws IOException, InterruptedException {
		SerialPort sp = SerialPort.getCommPort("COM3");
		sp.setComPortParameters(9600, 8, 1, 0);
		sp.setComPortTimeouts(SerialPort.TIMEOUT_WRITE_BLOCKING, 0, 0);

		if (sp.openPort()) {
			System.out.println("\nPort is open :)");
		} else {
			System.out.println("\nFailed to open port :(");
			return;
		}

		for (Integer i = 0; i < 2; i++) {
			Integer space = 32;
			sp.getOutputStream().write(space.byteValue());
			sp.getOutputStream().flush();
			System.out.println("Waiting....");
			Thread.sleep(1000);
		}

		int[] temp = new int[3];
		temp[0] = Integer.parseInt(inputInt.substring(0, 2));
		temp[1] = Integer.parseInt(inputInt.substring(2, 4));
		temp[2] = Integer.parseInt(inputInt.substring(4));

		for (int idx = 0; idx < 3; idx++) {
			System.out.println(temp[idx]);
			sp.getOutputStream().write(temp[idx]);
			sp.getOutputStream().flush();
			Thread.sleep(10);

		}

		if (sp.closePort()) {
			System.out.println("Port is closed :)");
		} else {
			System.out.println("Failed to close port :(");
			return;
		}
	}
}
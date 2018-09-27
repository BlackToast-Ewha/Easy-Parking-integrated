package SimpleSerialComm;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import com.fazecast.jSerialComm.SerialPort;

class Serial {
	static String carPlate = "10나1121";
	
	public static byte[] str2byteArr(String input) throws UnsupportedEncodingException {
		byte[] output = input.getBytes();
		
		return output;
	}
	
	public  static void pushToArduino(String inputInt) throws  IOException, InterruptedException{
		SerialPort sp = SerialPort.getCommPort("COM3"); 
		sp.setComPortParameters(9600, 8, 1, 0);
		sp.setComPortTimeouts(SerialPort.TIMEOUT_WRITE_BLOCKING, 0, 0); 

		if (sp.openPort()) {
			System.out.println("Port is open :)");
		} else {
			System.out.println("Failed to open port :(");
			return;
		}
		
		
		
		for (Integer i = 0; i < 3; ++i) {
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
		
		for (int idx=0; idx<3; idx++) {
			System.out.println(temp[idx]);
			sp.getOutputStream().write(temp[idx]);
			sp.getOutputStream().flush();
			Thread.sleep(1000);

		}
		
		if (sp.closePort()) {
			System.out.println("Port is closed :)");
		} else {
			System.out.println("Failed to close port :(");
			return;
		}
	}
	

//	public static void main(String[] args) throws IOException, InterruptedException {
//		
//
//		String inputInt = "70792"; // 차번호 70 79, 방향 2 (up)
//		
//		pushToArduino(inputInt);	
//
//	}
}
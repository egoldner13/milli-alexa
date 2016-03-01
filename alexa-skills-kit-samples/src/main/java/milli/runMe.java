package milli;

import java.util.List;

import milli.ConnectSSH;

public class runMe {

	public static void main(String[] args) {
		
		ConnectSSH tmp = new ConnectSSH();
		List<String> output = tmp.executeFile("/home/ciq/getStatusOfLight.sh");
		for (String line : output){
			System.out.println(line);
		}
		

	}

}

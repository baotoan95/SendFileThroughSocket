package client.app;

import java.io.IOException;
import java.net.Socket;

import client.business.FileClientProcess;

public class ClientApp {
	public static void main(String[] args) {
		try {
			Socket socket = new Socket("localhost", 12345);
			new Thread(new FileClientProcess(socket)).start();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}

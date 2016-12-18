package server.app;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import server.business.FileProcess;

public class ServerApp {
	public static void main(String[] args) {
		try {
			// Start server on port 12345
			@SuppressWarnings("resource")
			ServerSocket server = new ServerSocket(12345);
			System.out.println("Server is ready...");
			
			// Waiting for clients
			while(true) {
				Socket socket = server.accept();
				new Thread(new FileProcess(socket)).start();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}

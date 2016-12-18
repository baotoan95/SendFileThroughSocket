package client.business;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Scanner;
import java.util.StringTokenizer;

public class FileClientProcess implements Runnable {
	private Socket socket;
	private DataOutputStream dataOutputStream;
	private DataInputStream dataInputStream;
	private static String CLIENT_DIR = "";

	public FileClientProcess(Socket socket) {
		this.socket = socket;
	}

	@Override
	public void run() {
		try {
			this.dataOutputStream = new DataOutputStream(socket.getOutputStream());
			this.dataInputStream = new DataInputStream(socket.getInputStream());
			@SuppressWarnings("resource")
			Scanner scanner = new Scanner(System.in);

			String response = this.dataInputStream.readUTF();
			System.out.println(response);

			while (true) {
				String request = scanner.nextLine();

				StringTokenizer tokens = new StringTokenizer(request);
				String command = tokens.nextToken();
				
				if (command.equalsIgnoreCase("SET_SERVER_DIR")) {
					dataOutputStream.writeUTF(command + " " + tokens.nextToken().trim());
					System.out.println(dataInputStream.readUTF());
				} else if (command.equalsIgnoreCase("SET_CLIENT_DIR")) {
					FileClientProcess.CLIENT_DIR = tokens.nextToken();
					System.out.println("Set client directory: ok");
				} else if (command.equalsIgnoreCase("SEND")) {
					String fileName = tokens.nextToken();
					String fileDestName = tokens.nextToken();
					// Start send file
					File file = new File(FileClientProcess.CLIENT_DIR + fileName);
					if (!file.exists()) {
						System.out.println("File not found!");
						continue;
					}

					this.dataOutputStream.writeUTF(command + " " + fileDestName);
					System.out.println("Sending...");
					this.dataOutputStream.writeLong(file.length());
					byte[] data = new byte[1024];
					FileInputStream fileInputStream = new FileInputStream(file);
					int byteReaded = 0;
					read: while ((byteReaded = fileInputStream.read(data)) != -1) {
						dataOutputStream.write(data, 0, byteReaded);
						continue read;
					}
					fileInputStream.close();
					// Get response from server
					System.out.println(dataInputStream.readUTF());
				} else if (command.equalsIgnoreCase("GET")) {
					String fileName = tokens.nextToken();
					String fileDestName = tokens.nextToken();

					this.dataOutputStream.writeUTF(command + " " + fileName);
					// Get info of file from server
					long fileSize = dataInputStream.readLong();
					if (fileSize == -1) {
						System.out.println("File not found!");
						continue;
					}

					// Start recieve file
					System.out.println("Getting...");
					File file = new File(FileClientProcess.CLIENT_DIR + fileDestName);
					FileOutputStream fileOutputStream = new FileOutputStream(file);
					int byteReaded = 0;
					int bytes = 0;
					byte[] data = new byte[fileSize - byteReaded > 1024 ? 1024 : (int) (fileSize - byteReaded)];
					while (fileSize - byteReaded > 0) {
						bytes = this.dataInputStream.read(data);
						fileOutputStream.write(data, 0, bytes);
						byteReaded += bytes;
						data = new byte[fileSize - byteReaded > 1024 ? 1024 : (int) (fileSize - byteReaded)];
					}
					fileOutputStream.close();
					// Get response from client
					System.out.println(dataInputStream.readUTF());
				} else if (command.equalsIgnoreCase("quit")) {
					dataOutputStream.writeUTF(command);
					if(dataInputStream.readUTF().trim().equalsIgnoreCase("bye!")) {
						System.out.println("bye!");
						break;
					}
				} else {
					System.out.println("Command invalid");
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}

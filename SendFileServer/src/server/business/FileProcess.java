package server.business;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.StringTokenizer;

public class FileProcess implements Runnable {
	private static String SERVER_DIR = "";
	private Socket socket;
	private DataOutputStream dataOutputStream;
	private DataInputStream dataInputStream;

	public FileProcess(Socket socket) {
		this.socket = socket;
	}

	@Override
	public void run() {
		try {
			dataInputStream = new DataInputStream(socket.getInputStream());
			dataOutputStream = new DataOutputStream(socket.getOutputStream());

			// Welcome to client
			dataOutputStream.writeUTF("Welcome");
			while (true) {
				// Get request from client
				String request = dataInputStream.readUTF();
				StringTokenizer tokens = new StringTokenizer(request);

				// Get command
				String command = tokens.nextToken();

				if (command.equalsIgnoreCase("SET_SERVER_DIR")) {
					try {
						// Get value
						String value = tokens.nextToken().trim();
						File file = new File(value);
						if (value.length() != 0 && file.exists() && file.isDirectory()) {
							FileProcess.SERVER_DIR = value;
							dataOutputStream.writeUTF("Set server directory: ok");
						} else {
							dataOutputStream.writeUTF("Set server directory: fail");
						}
					} catch (Exception e) {
						dataOutputStream.writeUTF("Please input directory");
					}
				} else if (command.equalsIgnoreCase("SEND")) {
					long fileSize = dataInputStream.readLong();
					String fileName = tokens.nextToken();
					File file = new File(FileProcess.SERVER_DIR + fileName);

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
					dataOutputStream.writeUTF("Sent!");
				} else if (command.equalsIgnoreCase("GET")) {
					String fileName = tokens.nextToken();
					File file = new File(FileProcess.SERVER_DIR + fileName);
					if(!file.exists()) {
						dataOutputStream.writeLong(-1);
						continue;
					}

					this.dataOutputStream.writeLong(file.length());
					byte[] data = new byte[1024];
					FileInputStream fileInputStream = new FileInputStream(file);
					int byteReaded = 0;
					while ((byteReaded = fileInputStream.read(data)) != -1) {
						dataOutputStream.write(data, 0, byteReaded);
					}
					fileInputStream.close();
					dataOutputStream.writeUTF("Done!");
				} else if (command.equalsIgnoreCase("QUIT")) {
					dataOutputStream.writeUTF("Bye!");
					break;
				} else {
					dataOutputStream.writeUTF("Bad request!");
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.file.Path;
import java.nio.file.Paths;


/**
 * @author Client Class
 *
 */
/**
 * @author ninad
 *
 */
/**
 * @author ninad
 *
 */
/**
 * @author ninad
 *
 */
public class Client extends Thread {

	private static Socket cSocket = null;

	private static ObjectOutputStream out = null;

	private static ObjectInputStream in = null;

	private static BufferedReader inputLine = null;

	private static boolean closed = false;

	private static FileOutputStream fos = null;

	private static BufferedOutputStream bos = null;

	private static int PORT = 8000;

	private static final String HOST = "localhost";

	private static String folderForFiles = "";
	
	/**
	 * Main function
	 * @param args are command line arguments
	 */
	public static void main(String[] args) {
		try {
			if (args.length != 0) {
				try {
					PORT = Integer.parseInt(args[0]);
				} catch (Exception e) {
					PORT = 8000;
				}
			}
			cSocket = new Socket(HOST, PORT);
			inputLine = new BufferedReader(new InputStreamReader(System.in));
			out = new ObjectOutputStream(cSocket.getOutputStream());
			in = new ObjectInputStream(cSocket.getInputStream());
		} catch (UnknownHostException e) {
			System.err.println("Unresolved host: " + HOST);
		} catch (IOException e) {
			System.err.println("Could not connect to " + HOST);
		}
		if (cSocket != null && out != null && in != null) {
			try {
				/* Create a thread to read from the server. */
				new Thread(new Client()).start();
				while (!closed) {
					out.writeObject(inputLine.readLine().trim());
				}
				out.close();
				in.close();
				cSocket.close();
			} catch (IOException e) {
				System.err.println("IOException:  " + e);
			}
		}
	}
	
	/* 
	 * This method is executed when a thread is started
	 */
	public void run() {
		String responseLine;
		int current = 0;
		int bytesRead;
		try {
			while (true) {
				responseLine = (String) in.readObject();
				if (responseLine.indexOf("Bye") != -1)
					break;
				if (responseLine == null)
					break;
				if (responseLine.indexOf("Folder") != -1) {
					String folder = responseLine.split(":")[1];
					File dir = new File(folder);
					if (!dir.exists()) {
						try {
							dir.mkdir();
							folderForFiles = folder;
						} catch (Exception e) {
							folderForFiles = "";
						}
					} else {
						folderForFiles = folder;
					}
					continue;
				}
				else if (responseLine.indexOf("<incomingFile>") != -1) {
					try {
						String[] response = responseLine.split("\\s");
						Path p = Paths.get(folderForFiles, response[3]);
						String fileName = p.getFileName().toString();

						String fullPath = !(folderForFiles.isEmpty()) ? (folderForFiles + "//" + fileName) : fileName;
						int fileSize = Integer.parseInt(response[5]);
						byte[] fileBytes = new byte[fileSize];
						fos = new FileOutputStream(fullPath);
						bos = new BufferedOutputStream(fos);
						bytesRead = in.read(fileBytes, 0, fileBytes.length);

						current = bytesRead;
						do {
							bytesRead = in.read(fileBytes, current, (fileBytes.length - current));
							if (bytesRead >= 0)
								current += bytesRead;
						} while (current < fileSize);
						bos.write(fileBytes, 0, current);
						bos.flush();
						System.out.println("File " + fileName + " of size of " + fileSize + " bytes read");
					} catch (Exception e) {
						System.out.println("Try again! " + e.getMessage());
					}
				} else {
					System.out.println(responseLine);
				}
			}
			closed = true;
		} catch (IOException e) {
			System.err.println("IOException:  " + e);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}
}

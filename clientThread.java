import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

/**
 * @author Ashutosh
 */

public class clientThread extends Thread 
{

	private String clientName = null;
	private ObjectInputStream in = null;
	private ObjectOutputStream out = null;
	private Socket clientSocket = null;
	private final clientThread[] threads;
	private int clientCount;

	/**
	 * @param clientSocket - Clients socket object
	 * @param threads - Each client gets access to other threads
	 */
	
	public clientThread(Socket clientSocket, clientThread[] threads) 
	{
		this.clientSocket = clientSocket;
		this.threads = threads;
		clientCount = threads.length;
	}

	public void run() 
	{
		int clientCount = this.clientCount;
		clientThread[] threads = this.threads;

		try {
			in = new ObjectInputStream(clientSocket.getInputStream());
			out = new ObjectOutputStream(clientSocket.getOutputStream());
			String name;
			while (true) {
				out.writeObject("Please enter your name.");
				out.flush();
				name = ((String) in.readObject()).trim().toLowerCase();
				System.out.println("Client " + name + " is connected!");
				break;
			}
			out.writeObject("Hello " + name + "! Your connection was successful \n");
			out.flush();
			out.writeObject("Folder:" + name);
			out.flush();
			synchronized (this) {
				for (int i = 0; i < clientCount; i++) {
					if (threads[i] != null && threads[i] == this) {
						clientName = name;
						break;
					}
				}
				for (int i = 0; i < clientCount; i++) {
					if (threads[i] != null && threads[i] != this) {
						threads[i].out.writeObject(name + " has joined the chat!");
						threads[i].out.flush();
					}
				}
			}

			while (true) {
				
				String line;
				line = (String) in.readObject();
				if(line.startsWith("/quit"))
					break;
				if (line.length() > 0) {
					String[] words = line.split("\\s", 3);
					if (words.length > 2) {
						if (words[0].toLowerCase().equals("broadcast") && words[1].toLowerCase().equals("message")) {
							if (!words[2].trim().isEmpty()) {
								synchronized (this) {
									for (int i = 0; i < clientCount; i++) {
										if (threads[i] != null && threads[i].clientName != null && threads[i] != this) {
											threads[i].out.writeObject(name + ": " + words[2].replace("\"", ""));
											threads[i].out.flush();
											System.out.println("Broadcast message sent from " + name + " to "
													+ threads[i].clientName + "!");
										}
									}
									this.out.writeObject("Message sent!");
									this.out.flush();
								}
							}
						}
						if (words[0].toLowerCase().equals("broadcast") && words[1].toLowerCase().equals("file")) {
							if (!words[2].trim().isEmpty()) {
								synchronized (this) {
									boolean success = false;
									for (int i = 0; i < clientCount; i++) 
									{
										if (threads[i] != null && threads[i].clientName != null && threads[i] != this) {
											success = sendFile(words, threads[i], this, true);
										}
									}
									if(success) 
									{
										System.out.println("Broadcast:File sent!");
										this.out.writeObject("File sent!");
										this.out.flush();
									}
									else {
										System.out.println("Broadcast:File not found!");
										this.out.writeObject("The file you wish to send does not exist!");
										this.out.flush();	
									}
								}
							}
						}
						if (words[0].toLowerCase().equals("unicast") && words[1].toLowerCase().equals("message")) {
							words = words[2].split("\\s+", 2);
							if (!words[0].trim().isEmpty()) {
								synchronized (this) {
									for (int i = 0; i < clientCount; i++) {
										if (threads[i] != null && threads[i].clientName != null && threads[i] != this
												&& threads[i].clientName.equals(words[0])) {
											threads[i].out.writeObject(name + ": " + words[1].replace("\"", ""));
											threads[i].out.flush();
											System.out.println("Unicast message sent from " + name + " to "
													+ threads[i].clientName + "!");
											break;
										}
									}
								}
							}
						}
						if (words[0].toLowerCase().equals("unicast") && words[1].toLowerCase().equals("file")) {
							words = words[2].split("\\s+", 2);
							if (!words[0].trim().isEmpty()) {
								synchronized (this) {
									boolean success = false;
									for (int i = 0; i < clientCount; i++) {
										if (threads[i] != null && threads[i].clientName != null && threads[i] != this
												&& threads[i].clientName.equals(words[0])) {
											success = sendFile(words, threads[i], this, false);
											break;
										}
									}
									if(success) {
										System.out.println("Unicast:File Sent!");
										this.out.writeObject("File sent to " + words[0]);
										this.out.flush();
									}
									else {
										System.out.println("Unicast:File not found!");
										this.out.writeObject("The file you wish to send does not exist!");
										this.out.flush();	
									}
								}
							}
						}
						if (words[0].toLowerCase().equals("blockcast") && words[1].toLowerCase().equals("message")) {
							words = words[2].split("\\s+", 2);
							if (!words[0].trim().isEmpty()) {
								synchronized (this) {
									for (int i = 0; i < clientCount; i++) {
										if (threads[i] != null && threads[i].clientName != null && threads[i] != this
												&& !threads[i].clientName.equals(words[0])) {
											threads[i].out.writeObject(name + ": " + words[1].replace("\"", ""));
											threads[i].out.flush();
											System.out.println("Blockcast message sent from " + name + " to "
													+ threads[i].clientName + "!");
										}
									}
									this.out.writeObject("Message sent to everyone except " + words[0]);
									this.out.flush();
								}
							}
						}
						if (words[0].toLowerCase().equals("blockcast") && words[1].toLowerCase().equals("file")) {
							words = words[2].split("\\s+", 2);
							if (!words[0].trim().isEmpty()) {
								synchronized (this) {
									boolean success = false;
									for (int i = 0; i < clientCount; i++) {
										if (threads[i] != null && threads[i].clientName != null && threads[i] != this
												&& !threads[i].clientName.equals(words[0])) {
											success = sendFile(words, threads[i], this, false);
										}
									}
									if(success) {
										System.out.println("Blockcast:File sent!");
										this.out.writeObject("File sent to everyone except " + words[0]);
										this.out.flush();
									}
									else {
										System.out.println("Blockcast:File not found!");
										this.out.writeObject("The file you wish to send does not exist!");
										this.out.flush();	
									}
								}
							}
						}
					}
				}
			}
			String leavingClient = "";
			synchronized (this) {
				for(int i = 0; i < clientCount; i++) {
					if (threads[i] != null && threads[i].clientName != null && threads[i] != this) {
						threads[i].out.writeObject("The client " + name + " is leaving!");
						threads[i].out.flush();
					}
					if (threads[i] != null && threads[i].clientName != null && threads[i] == this) {
						leavingClient = threads[i].clientName;
						threads[i] = null;
					}
				}
			}
			System.out.println("The client " + leavingClient + " has left!");
			this.out.writeObject("Bye");
			this.out.flush();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * @param words - contains command parameters
	 * @param sendTo - Client thread we wish to send to
	 * @param receivedFrom - Client thread who sends to sendTo
	 * @param broadcast - boolean, true if message or file type is broadcast
	 * @return - returns true if file is found
	 * @throws FileNotFoundException
	 */
	private boolean sendFile(String[] words, clientThread sendTo, clientThread receivedFrom, boolean broadcast)
			throws FileNotFoundException {
		FileInputStream fis = null;
		BufferedInputStream bis = null;
		boolean success = false;
		try {
			String fileName;
			if (broadcast)
				fileName = words[2].replace("\"", "");
			else
				fileName = words[1].replace("\"", "");
			File fileToSend = new File(fileName);
			if (fileToSend.exists()) {
				sendTo.out.writeObject(receivedFrom.clientName + " sending <incomingFile> " + fileName + " SIZE "
						+ (int) fileToSend.length());
				sendTo.out.flush();
				byte[] fileInBytes = new byte[(int) fileToSend.length()];
				fis = new FileInputStream(fileToSend);
				bis = new BufferedInputStream(fis);
				bis.read(fileInBytes, 0, fileInBytes.length);
				sendTo.out.write(fileInBytes, 0, fileInBytes.length);
				sendTo.out.flush();
				success = true;
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {

			try {
				if (bis != null)
					bis.close();
				if (fis != null)
					fis.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return success;
	}
}

import java.net.*;
import java.io.*;

public class Server
{
	private static int sPort = 8000;
	private static ServerSocket sSocket;

	private static Socket clientSocket = null;

	private static final int CLIENT_MAX = 200;
	
	private static final clientThread[] threads = new clientThread[CLIENT_MAX];
	/*** Initialize server parameters and start server
	 * @param args
	 */
	public static void main(String[] args) {
		
		try {
			if(args.length != 0)
			{
				try
				{
					sPort = Integer.parseInt(args[0]);
				}catch(Exception e) {
					sPort = 8000;
				}
			}
			sSocket = new ServerSocket(sPort);
			System.out.println("Server started at portNumber " + sPort);
		} catch (IOException e) {
			System.out.println(e);
		}

		while (true) {
			try {
				clientSocket = sSocket.accept();
				int i = 0;
				for (i = 0; i < CLIENT_MAX; i++) 
				{
					if (threads[i] == null) {
						(threads[i] = new clientThread(clientSocket, threads)).start();
						break;
					}
				}
				if (i == CLIENT_MAX) {
					ObjectOutputStream out = new ObjectOutputStream(clientSocket.getOutputStream());
					out.writeChars("Maximum number of connections reached. Please try again later");
					out.close();
					clientSocket.close();
				}
			} catch (IOException e) 
			{
				System.out.println(e);
			}
		}
	}

}

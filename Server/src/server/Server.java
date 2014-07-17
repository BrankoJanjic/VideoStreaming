package server;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedList;

public class Server {
	static LinkedList<ServerKlijentNit> klijenti = new LinkedList<ServerKlijentNit>();

	public static void main(String[] args) throws Exception {
		ServerSocket m_ServerSocket = new ServerSocket(9090);

		while (true) {
			Socket clientSocket = m_ServerSocket.accept();
			ServerKlijentNit nit = new ServerKlijentNit(clientSocket,
					clientSocket.getInetAddress());
			klijenti.addLast(nit);
			nit.start();
			

		}
	}
}
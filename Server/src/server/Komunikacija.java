package server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.LinkedList;

import server.*;



public class Komunikacija {

	static Socket clientSocket;
	static InetAddress clientId;
	boolean running = true;
	//static LinkedList<String> fajloviList = new LinkedList<String>();
	//KlijentKlijentNit klijentKlijentNit;
	static LinkedList<String> SpisakCeo = new LinkedList<>();
	static DataOutputStream outToClient;
	static DataInputStream fromClient;


	
	

	
	
	public static void obradaZahteva(String z, Socket kliSoc)
	{
		try{
			outToClient = new DataOutputStream(
					kliSoc.getOutputStream());
		//String zahtev = fromClient.readUTF();
		boolean nadjen = false;
		String ipAdresa = "ip"; 
		for (ServerKlijentNit iterable_element : Server.klijenti) {
			for (String file : iterable_element.fajloviList) {
				if (file.equals(z)) {
					nadjen = true;
					//iterable_element.clientSocket.getInetAddress()
					ipAdresa = iterable_element.clientSocket.getInetAddress().getHostAddress().toString();
					System.out.println(ipAdresa);
					break;
				}
			}
			if (nadjen) {
				break;
			} else
				continue;
		}
		if(nadjen){
		outToClient.writeUTF(ipAdresa);
		}
		else
		{
			outToClient.writeUTF("Nije nadjen.");
		}
	} catch (Exception e) {
		e.printStackTrace();
	}
	}
	
	
	
}

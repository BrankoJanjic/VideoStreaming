package server;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Writer;
import java.net.InetAddress;
import java.net.Socket;
import java.util.LinkedList;

import javax.swing.text.StyledEditorKit.BoldAction;

class ServerKlijentNit extends Thread {
	Socket clientSocket;
	InetAddress clientId;
	boolean running = false;
	LinkedList<String> fajloviList;
	DataOutputStream outToClient;
	DataInputStream fromClient;
	private boolean kraj;
	

	ServerKlijentNit(Socket s, InetAddress i) {
		clientSocket = s;
		clientId = i;
	}

	public void run() {
		fajloviList= new LinkedList<String>();
		System.out
				.println("Prihvacen klijent : ID - " + clientId
						+ " : Address - "
						+ clientSocket.getInetAddress().getHostName());
		System.out.println(Server.klijenti.size());
		try {
			outToClient = new DataOutputStream(
					clientSocket.getOutputStream());
			fromClient = new DataInputStream(
					clientSocket.getInputStream());

			//HELLO-HELLO komunikacija
			System.out.println(fromClient.readUTF());
			outToClient.writeUTF(" HELLO " + clientId);
			
			
			//prijem fajlova od klijenta
			prijemSpiskaServer(clientSocket);

			//slanje dostupnih fajlova klijentima
			for (ServerKlijentNit iterable_element : Server.klijenti) {
				iterable_element.outToClient.writeUTF("**");
			}
			slanjeDostupnihFajlovaKlijentima(clientSocket);
		//	System.out.println("Klijent je rekao: " + fromClient.readUTF());
			
			while(kraj==false){
			kraj = fromClient.readBoolean();
			System.out.println("BOOLEAN primljen");
			System.out.println(kraj);
			}
			if (kraj) {
				outToClient.writeBoolean(true);
				Server.klijenti.remove(this);
				for (ServerKlijentNit iterable_element : Server.klijenti) {
					iterable_element.outToClient.writeUTF("**");
				}
				slanjeDostupnihFajlovaKlijentima(clientSocket);
				System.out.println("poslato");
				// potvrda prijema 
				// zatvori samo tu nit
				this.outToClient.close();
				this.fromClient.close();
				System.out.println("ZATVORENO");
				System.out.println(Server.klijenti.size());
				this.clientSocket.close();
				this.join();
			}
			
			
		

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public  void prijemSpiskaServer(Socket kliSoket) throws IOException
	{
		
		fromClient = new DataInputStream(kliSoket.getInputStream());
		String odgovor = fromClient.readUTF(); // odgovor na zahtev klijenta Klijent salje da li ima ili nema fajlova
		System.out.println(odgovor);
		
		if (odgovor.indexOf("**") == 0) {

			String fajl;
			while ((fajl = fromClient.readUTF()).indexOf("END") != 0) {
				fajloviList.add(fajl);
				System.out.println("Primljen fajl");
			}
			System.out.println("Svi fajlovi primljeni");
		}
	}
	
public  void slanjeDostupnihFajlovaKlijentima(Socket kliSoket) throws IOException {

		for (ServerKlijentNit klijent : Server.klijenti) {
			System.out.println("SALJEM OD  "+klijent.clientId );
			for (String file : klijent.fajloviList) {
				for (ServerKlijentNit k : Server.klijenti) {
					if (k!=klijent) {
						System.out.println("			SALJEM KA "+file + " "+k.clientId);
						k.outToClient.writeUTF(file);
					}
					
				}
			}
		}
		for (ServerKlijentNit klijent : Server.klijenti) {
			klijent.outToClient.writeUTF("END");
		}
		System.out.println("Poslati fajlovi sa servera");
		
	}
}

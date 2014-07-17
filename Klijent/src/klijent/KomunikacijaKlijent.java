package klijent;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.LinkedList;

import javax.swing.JFileChooser;

public class KomunikacijaKlijent {

	static DataOutputStream outToServer;
	//static LinkedList<String> spisakFajlovaNaServeru = new LinkedList<>();
	static DataInputStream dis;
	static String ipAdresa;

	
	
	public static void slanjeSpiskaKlijent(Socket clientSok)
	{
		JFileChooser chooser = new JFileChooser();
		chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		chooser.showOpenDialog(null);
		
		File e = chooser.getSelectedFile();
		
		System.out.println(e);
		String[] fajlovi = e.list();
		try {
			ipAdresa = InetAddress.getLocalHost().getHostAddress();
		} catch (UnknownHostException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		try{
			outToServer = new DataOutputStream(clientSok.getOutputStream());
		if (!e.exists() || fajlovi == null) {
			outToServer.writeUTF("Nemam fajlova");
		} else {
			outToServer.writeUTF("** Pocinjem slanje **");
			for (String fajl : fajlovi) {
				outToServer.writeUTF(fajl+"*"+ipAdresa);
				System.out.println("Poslat " + fajl);
			}
			outToServer.writeUTF("END");
		}
		}catch(Exception e1)
		{
			e1.printStackTrace();
		}
		
		
	}
	
	
	public static void prijemSpiskaKlijent(Socket clientSok)
	{
		try{
		//while(dis.readUTF().equals("Saljem spisak dostupnih fajlova."))
		
		String odgovor;
		outToServer = new DataOutputStream(clientSok.getOutputStream());
		dis = new DataInputStream(clientSok.getInputStream());
		while(!(odgovor=dis.readUTF()).equals("END"))
		{
			GUILista.spisakFajlovaNaServeru.addLast(odgovor);
			
		}
		System.out.println(GUILista.spisakFajlovaNaServeru.size());
		//outToServer.writeUTF("Spisak primljen");
		
		
		}catch(Exception e)
		{
			e.printStackTrace();
		}
		
	}
	
}

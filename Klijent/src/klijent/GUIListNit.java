package klijent;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.LinkedList;

import javax.swing.DefaultListModel;

public class GUIListNit extends Thread{
	
	Socket s;
	DataInputStream dis; 
	DataOutputStream dos;
	public GUIListNit( Socket s) {
		this.s=s;
	}
	
	public void run ()
	{
		
			try {
				dis = new DataInputStream(s.getInputStream());
				dos = new DataOutputStream(s.getOutputStream());
				while (true){
					
					if(dis.readUTF().indexOf("**")==0)
					{
						GUILista.spisakFajlovaNaServeru= new LinkedList<String>();
						KomunikacijaKlijent.prijemSpiskaKlijent(s);
					}
					GUILista.model.removeAllElements();
					
					for (String string : GUILista.spisakFajlovaNaServeru) {
						System.out.println(string);
					}
					for (String fajl : GUILista.spisakFajlovaNaServeru) {
						String listaFajl = "";
						listaFajl = fajl.substring(0, fajl.indexOf("*"));
						String ekstenzija = listaFajl.substring(listaFajl.lastIndexOf(".")+1, listaFajl.length());
						if(ekstenzija.equals("mpg") || ekstenzija.equals("avi")){
							GUILista.model.addElement(listaFajl);							
						}
						
					}
					GUILista.list.setModel(GUILista.model);
				
			}
			} catch (IOException e) {
				try {
					s.close();
					return;
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
			
			
		
	}
	

}
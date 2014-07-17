package klijent;
import java.awt.EventQueue;

import javax.swing.DefaultListModel;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;

import java.awt.Font;

import javax.swing.JList;
import javax.swing.border.LineBorder;

import java.awt.Color;
import java.awt.event.MouseAdapter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.SynchronousQueue;
import java.awt.event.MouseEvent;

import javax.swing.JButton;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
public class GUILista {
	private JFrame frame;
	private static Socket clientSocket;
	boolean bool=false;
	static String ipP2PServera = null;
	static LinkedList<String> spisakFajlovaNaServeru = new LinkedList<String>();
	private static String zahtev;
	static JList<String> list;
	static DataInputStream dis;
	static DataOutputStream outToServer;
	private JButton btnSendRequest;;
	static KlijentKlijentNit KlijentKlijentNit;
	int brojac;
	Thread t = null;
	static Klijent k = null;
	static DefaultListModel<String> model ;
	
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					GUILista window = new GUILista();
					window.frame.setVisible(true);
					//192.168.0.102
					clientSocket = new Socket("localhost", 9090);
					KlijentKlijentNit = new KlijentKlijentNit();
					KlijentKlijentNit.start();
					outToServer = new DataOutputStream(clientSocket.getOutputStream());
					dis = new DataInputStream(clientSocket.getInputStream());
					
					// HELLO-HELLO komunikacija
					outToServer.writeUTF("HELLO");
					System.out.println(dis.readUTF());
					
					// slanje svojih fajlova serveru
					KomunikacijaKlijent.slanjeSpiskaKlijent(clientSocket);
					
					// prijem dostupnih fajlova na serveru i filtriranje sopstvenih fajlova
					KomunikacijaKlijent.prijemSpiskaKlijent(clientSocket);
					
					 model = new DefaultListModel<String>();
					
					for (String string : spisakFajlovaNaServeru) {
						System.out.println(string);
					}
					for (String fajl : spisakFajlovaNaServeru) {
						
						String listaFajl = "";
						listaFajl = fajl.substring(0, fajl.indexOf("*"));
						String ekstenzija = listaFajl.substring(listaFajl.lastIndexOf(".")+1, listaFajl.length());
						if(ekstenzija.equals("mpg") || ekstenzija.equals("avi")){
							model.addElement(listaFajl);							
						}
						
						
					}
					list.setModel(model);
					System.out.println("Postavio model");
					GUIListNit gln = new GUIListNit(clientSocket);
					gln.start();
					
				
					
					} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}
	public GUILista() {
		initialize();
	}
	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
			frame = new JFrame();
			frame.addWindowListener(new WindowAdapter() {
				@Override
				public void windowClosing(WindowEvent e) {
					
					try {
						outToServer.writeBoolean(true);
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
				}
			});
			
			frame.setBounds(100, 100, 295, 462);
			frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			frame.getContentPane().setLayout(null);
			JLabel lblSpisakFajlovaDostupnih = new JLabel(
					"Spisak fajlova dostupnih na severu: ");
			lblSpisakFajlovaDostupnih.setForeground(Color.DARK_GRAY);
			lblSpisakFajlovaDostupnih
					.setFont(new Font("Tahoma", Font.BOLD, 12));
			lblSpisakFajlovaDostupnih.setBounds(20, 11, 249, 14);
			frame.getContentPane().add(lblSpisakFajlovaDostupnih);
			list = new JList<String>();
			list.setBorder(new LineBorder(Color.DARK_GRAY, 2, true));
			list.setBounds(30, 36, 195, 276);
			frame.getContentPane().add(list);
			
			btnSendRequest = new JButton("Pusti video");
		
			btnSendRequest.addActionListener(new ActionListener() {
				public synchronized void actionPerformed(ActionEvent arg0) {
					zahtev = list.getSelectedValue();
					System.out.println(zahtev);
					brojac++;
					if (zahtev != null) {
						for (String fajl : spisakFajlovaNaServeru) {
							if (fajl.startsWith(zahtev)) {
								ipP2PServera = fajl.substring(fajl.indexOf("*")+1, fajl.length());
							}
						}
						System.out.println(ipP2PServera);
						if (brojac >1) {
							try {
								k.dis.close();
								k.disV.close();
								k.disA.close();
								k.clientSocketP2P.close();
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							k.mLine.close();
							GUI.frame.dispose();
						}
						
						k = new Klijent(zahtev, ipP2PServera);
						t = new Thread(k);
						t.start();
						System.out.println("startovana");
						
						
					}
				}
			});
			btnSendRequest.setBounds(72, 347, 115, 23);
			frame.getContentPane().add(btnSendRequest);
			
	}
}
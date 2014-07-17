package klijent;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.LinkedList;

import javax.imageio.ImageIO;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.xuggle.xuggler.demos.VideoImage;

import server.*;
class Klijent implements Runnable {

	boolean bool = false;
	Socket clientSocket;
	Socket clientSocketP2P;
	private String ipP2PServera = null;
	SourceDataLine mLine;
	LinkedList<String> spisakFajlovaNaServeru = new LinkedList<String>();
	static int height;
	static int width;
	private String zahtev;
	DataOutputStream outToServer;
	DataInputStream disV;
	DataInputStream disA;
	DataInputStream dis;
	boolean mute = false;

	public String getZahtev() {
		return zahtev;
	}
	
	public Klijent(){
		
	}

	public  Klijent(String zahtev, String ip){
		this.zahtev = zahtev;
		ipP2PServera = ip;
	}
	
	public static void main(String argv[]) throws Exception {}

	@Override
	public void run() {

		//SourceDataLine mLine = null;

		try {
			clientSocketP2P = new Socket(ipP2PServera, 9091);

			outToServer = new DataOutputStream(
					clientSocketP2P.getOutputStream());
			disV = new DataInputStream(
					clientSocketP2P.getInputStream());
			disA = new DataInputStream(
					clientSocketP2P.getInputStream());
			dis = new DataInputStream(
					clientSocketP2P.getInputStream());

			//InputStream odServeraBajtovi = clientSocketP2P.getInputStream();

			outToServer.writeBytes("HELLO" + '\n');

			System.out.println(disV.readUTF());
			outToServer.writeBytes(zahtev + '\n');
			height = disV.readInt();
			width = disV.readInt();
			
			int simRate = disA.readInt();
			System.out.println("Sim" + simRate);
			int iSimRate = disA.readInt();
			System.out.println("iSim" + iSimRate);
			int chanel = disA.readInt();
			System.out.println("chanel" + chanel);
			AudioFormat audioFormat = new AudioFormat(simRate, iSimRate, chanel,
					true, 
					false);
			DataLine.Info info = new DataLine.Info(SourceDataLine.class,
					audioFormat);
			try {
				mLine = (SourceDataLine) AudioSystem.getLine(info);
			} catch (LineUnavailableException e2) {
				// TODO Auto-generated catch block
				e2.printStackTrace();
			}
			/**
			 * if that succeeded, try opening the line.
			 */
			try {
				mLine.open(audioFormat);
			} catch (LineUnavailableException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			/**
			 * And if that succeed, start the line.
			 */
			//mLine.start();
			new GUI();
			ImageIcon icon;
			GUI.label.setIcon(null);
			GUI.label.setLayout(null);
			GUI.panel.add(GUI.label);
			GUI.frame.setBounds(100, 100, width+15, height +100);
			GUI.panel.setBounds(5, 5, width, height);
			GUI.label.setBounds(5, 5, width, height);
			

			//GUI.list.setBounds(width + 25 , 11 , 200, height);
			GUI.frame.getContentPane().add(GUI.panel, BorderLayout.CENTER);
			GUI.panel.setSize(new Dimension(width, height));
			GUI.label.setSize(new Dimension(width, height));
			GUI.frame.setVisible(true);
			
			GUI.btnPlaypause.setBounds(width/3,height + 25 , 32, 32);
			GUI.btnMute.setBounds(width*2/3, height + 25, 32, 32);
			

			while (true) {

				try {
					
					String odgovor = dis.readUTF();
					if (bool==true) {
						while (bool==true) {
							outToServer.writeBoolean(bool);
							mLine.stop();
						}
						outToServer.writeBoolean(bool);
					}
					else outToServer.writeBoolean(bool);
					
					System.out.println("Nastavlja");
					if (odgovor.equals("video")) {
						byte[] niz = procitajBajtove();
						System.out.println("Niz ima velicinu " + niz.length);
						Toolkit toolkit = Toolkit.getDefaultToolkit();
						Image image = toolkit.createImage(niz, 0, niz.length);
						icon = new ImageIcon(image);
						GUI.label.setIcon(icon);
						niz = null;
					} else if (odgovor.equals("audio")){
						int sampleSize = disA.readInt();
						int odg = 0;
						mLine.start();
						while ((odg = disA.readInt()) != -2) {
							byte[] nizAudio = procitajBajtoveAudio(odg);
							if (mute) {
								mLine.close();
								nizAudio = null;
							}
							
							else{
								mLine.open(audioFormat);
								mLine.write(nizAudio, 0, sampleSize);
								nizAudio = null;
							}
							
						}
						
						
					}
					

					//int sampleSize = disA.readInt();
					
					
				} catch (Exception e) { 
					try {
						clientSocketP2P.close();
						//clientSocket.close();	
						GUI.frame.dispose();
					} catch (Exception e2) {
						// TODO: handle exception
						return;
					}
					//System.out.println("Greska je: " + e);
					break;
				}

			}
		} catch (UnknownHostException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

	}

	private byte[] procitajBajtove() throws IOException {
	
		InputStream in = clientSocketP2P.getInputStream();
		DataInputStream dis = new DataInputStream(in);

		int len = dis.readInt();
		byte[] data = new byte[len];
		if (len > 0) {
			dis.readFully(data);
		}
		System.out.println("Ucitano" + len);
		return data;
	}
	
	private byte[] procitajBajtoveAudio(int i) throws IOException {
		
		InputStream in = clientSocketP2P.getInputStream();
		DataInputStream dis = new DataInputStream(in);

		int len = i;
		
		byte[] data = new byte[len];
		if (len > 0) {
			dis.readFully(data);
		}
		System.out.println("Ucitano" + len);
		return data;

	}


}

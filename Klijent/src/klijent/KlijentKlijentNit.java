package klijent;

import java.awt.image.BufferedImage;
import server.*;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedList;

import javax.imageio.ImageIO;

import com.xuggle.xuggler.Global;
import com.xuggle.xuggler.ICodec;
import com.xuggle.xuggler.IContainer;
import com.xuggle.xuggler.IPacket;
import com.xuggle.xuggler.IPixelFormat;
import com.xuggle.xuggler.IStream;
import com.xuggle.xuggler.IStreamCoder;
import com.xuggle.xuggler.IVideoPicture;
import com.xuggle.xuggler.IVideoResampler;
import com.xuggle.xuggler.Utils;

public class KlijentKlijentNit extends Thread implements Runnable{
	

	ServerSocket welcomeSocket;
	Socket connectionSocket;
	static String ip;
	static  LinkedList<KKNit> p2pKlijenti; 

	String filename;

	public void run() {
		try {
			welcomeSocket = new ServerSocket(9091);
			p2pKlijenti = new LinkedList<KKNit>();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
			while(true){
			try {
				connectionSocket = welcomeSocket.accept();
				System.out.println("Prihvacen");
				ip=connectionSocket.getInetAddress().getHostAddress();
				KKNit kkNit = new KKNit(connectionSocket);
				
				kkNit.start();
				p2pKlijenti.add(kkNit);
				System.out.println("Pokrenuta nit");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
	

	}}}


	



package klijent;

import java.awt.EventQueue;
import java.awt.List;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.util.LinkedList;

import javax.sound.sampled.SourceDataLine;
import javax.swing.DefaultListModel;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.ListModel;
import javax.swing.border.LineBorder;

import java.awt.Color;

import javax.swing.JButton;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.ImageIcon;

public class GUI {

	static JFrame frame;
	static JPanel panel;
	static JLabel label;
	static JButton btnPlaypause;
	static JButton btnMute;
	boolean pause = false;
	boolean mute = false;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					new GUI();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 * 
	 * @throws IOException
	 */
	public GUI() throws IOException {
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 * 
	 * @throws IOException
	 */
	private void initialize() throws IOException {

		frame = new JFrame();
		frame.getContentPane().setForeground(Color.WHITE);
		frame.getContentPane().setBackground(Color.WHITE);
		frame.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				// GUILista.outToServer.writeBoolean(true);
				try {
					GUILista.k.mLine.stop();
					GUILista.k.dis.close();
					GUILista.k.disV.close();
					GUILista.k.disA.close();
					GUILista.k.clientSocketP2P.close();
					frame.dispose();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				// frame.dispose();
			}
		});

		frame.setResizable(false);
		frame.getContentPane().setLayout(null);

		panel = new JPanel();
		panel.setAlignmentY(0.0f);
		panel.setAlignmentX(0.0f);
		panel.setForeground(Color.WHITE);
		panel.setBackground(Color.WHITE);
		panel.setBorder(null);
		panel.setBounds(0, 0, 310, 239);
		frame.getContentPane().add(panel);

		btnPlaypause = new JButton();
		btnPlaypause.setIcon(new ImageIcon("Pause.png"));
		btnPlaypause.addActionListener(new ActionListener() {
			public synchronized void actionPerformed(ActionEvent arg0) {
				if (pause == false) {
					GUILista.k.bool = true;
					btnPlaypause.setIcon(new ImageIcon("play.png"));

					pause = true;
				} else {
					GUILista.k.bool = false;
					btnPlaypause.setIcon(new ImageIcon("pause.png"));
					pause = false;
				}

			}
		});
		btnPlaypause.setBounds(0, 0, 89, 23);
		frame.getContentPane().add(btnPlaypause);

		btnMute = new JButton();
		btnMute.setIcon(new ImageIcon("mute_on.png"));
		btnMute.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if (mute == false) {
					GUILista.k.mute = true;
					btnMute.setIcon(new ImageIcon("mute.jpg"));
					mute = true;
				} else {
					GUILista.k.mute = false;
					btnMute.setIcon(new ImageIcon("mute_on.png"));
					mute = false;
				}

			}
		});
		btnMute.setBounds(0, 0, 89, 23);
		frame.getContentPane().add(btnMute);

		label = new JLabel();
		//frame.getContentPane().add(label);

	}
}

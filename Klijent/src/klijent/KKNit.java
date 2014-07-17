package klijent;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;

import javax.imageio.ImageIO;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

import com.xuggle.xuggler.Global;
import com.xuggle.xuggler.IAudioSamples;
import com.xuggle.xuggler.ICodec;
import com.xuggle.xuggler.IContainer;
import com.xuggle.xuggler.IPacket;
import com.xuggle.xuggler.IPixelFormat;
import com.xuggle.xuggler.IStream;
import com.xuggle.xuggler.IStreamCoder;
import com.xuggle.xuggler.IVideoPicture;
import com.xuggle.xuggler.IVideoResampler;
import com.xuggle.xuggler.Utils;

public class KKNit extends Thread {

	private Socket connectionSocket;
	private long startTimer;
	private IStreamCoder videoCoder = null;
	private long pocetakPrikazivanja = Global.NO_PTS;;
	private SourceDataLine mLine;
	private boolean end;
	private DataInputStream bool;
	private BufferedReader inFromClient;
	private DataOutputStream outToClient;
	private OutputStream outToClientBytes;

	public KKNit(Socket connectionSocket) {

		this.connectionSocket = connectionSocket;
	}

	public Socket getConnectionSocket() {
		return connectionSocket;
	}

	private long milisekundeIzmedjuPrikazivanja(IVideoPicture picture) {

		long millisecondsToSleep = 0;

		if (pocetakPrikazivanja == Global.NO_PTS) { // za prvu Sliku

			pocetakPrikazivanja = picture.getTimeStamp();

			startTimer = System.currentTimeMillis(); // startujem tajmer, kada
														// pocne
		} else {
			long sadaVremeUMillSec = System.currentTimeMillis();
			long millSecOdPocetkaVidea = sadaVremeUMillSec - startTimer;

			long vremeOdPocetkaStreama = (picture.getTimeStamp() - pocetakPrikazivanja) / 1000; // jer
																								// mi
																								// vrati
																								// u
																								// sekundama
			final long tolerancija = 50; // dajem 50 ms tolerancije, npr. 50
			millisecondsToSleep = (vremeOdPocetkaStreama - (millSecOdPocetkaVidea + tolerancija));
		}
		return millisecondsToSleep;
	}

	@SuppressWarnings("deprecation")
	@Override
	public void run() {

		try {
			outToClient = new DataOutputStream(
					connectionSocket.getOutputStream());
			bool = new DataInputStream(connectionSocket.getInputStream());

			inFromClient = new BufferedReader(new InputStreamReader(
					connectionSocket.getInputStream()));

			outToClientBytes = connectionSocket.getOutputStream();

			// **************************************************

			outToClient.writeUTF(inFromClient.readLine());

			String filename = "C:/stream/" + inFromClient.readLine();

			IContainer container = IContainer.make();

			if (container.open(filename, IContainer.Type.READ, null) < 0)
				throw new IllegalArgumentException("could not open file: "
						+ filename);

			int numStreams = container.getNumStreams();
			int videoStreamId = -1;

			int audioStreamId = -1;
			IStreamCoder audioCoder = null;
			BufferedImage image = null;

			// prolazim preko strimova i dodeljujem ID i Coder, jedan fajl moze
			// da
			// ima jedan video i jedan audio stream
			for (int i = 0; i < numStreams; i++) {
				// Find the stream object
				IStream stream = container.getStream(i);
				// Get the pre-configured decoder that can decode this stream;
				IStreamCoder coder = stream.getStreamCoder();

				if (videoStreamId == -1
						&& coder.getCodecType() == ICodec.Type.CODEC_TYPE_VIDEO) {
					videoStreamId = i;
					videoCoder = coder;
				} else if (audioStreamId == -1
						&& coder.getCodecType() == ICodec.Type.CODEC_TYPE_AUDIO) {
					audioStreamId = i;
					audioCoder = coder;
				}
			}
			if (videoStreamId == -1 && audioStreamId == -1)
				throw new RuntimeException(
						"could not find audio or video stream in container: "
								+ filename);

			// pocinje dekodiranje
			IVideoResampler resampler = null;
			if (videoCoder != null) {
				if (videoCoder.open() < 0)
					throw new RuntimeException("Ne mogu da otvorim video "
							+ filename);
				if (videoCoder.getPixelType() != IPixelFormat.Type.BGR24) {

					// mora da bude u BGR24
					resampler = IVideoResampler.make(videoCoder.getWidth(),
							videoCoder.getHeight(), IPixelFormat.Type.BGR24,
							videoCoder.getWidth(), videoCoder.getHeight(),
							videoCoder.getPixelType());
					if (resampler == null)
						throw new RuntimeException(
								"Ne mogu da napravim sliku sa 24 bita"
										+ filename);
				}

			}
			if (audioCoder != null) {
				if (audioCoder.open() < 0)
					throw new RuntimeException(
							"could not open audio decoder for container: "
									+ filename);

				/*
				 * And once we have that, we ask the Java Sound System to get
				 * itself ready.
				 */
				try {
					openJavaSound(audioCoder);
				} catch (LineUnavailableException ex) {
					throw new RuntimeException(
							"unable to open sound device on your system when playing back container: "
									+ filename);
				}
			}

			IPacket packet = IPacket.make();
			byte[] fajl = new byte[(int) container.getFileSize()];

			pocetakPrikazivanja = Global.NO_PTS;
			startTimer = 0;
			outToClient.writeInt(videoCoder.getHeight());
			outToClient.writeInt(videoCoder.getWidth());

			outToClient.writeInt(audioCoder.getSampleRate());
			outToClient.writeInt((int) IAudioSamples
					.findSampleBitDepth(audioCoder.getSampleFormat()));
			outToClient.writeInt(audioCoder.getChannels());

			// prolazim kroz sve pakete
			while (container.readNextPacket(packet) >= 0) {

				// ako je video strim pravimo sliku
				if (packet.getStreamIndex() == videoStreamId) {

					IVideoPicture picture = IVideoPicture.make(
							videoCoder.getPixelType(), videoCoder.getWidth(),
							videoCoder.getHeight());

					// dekodiramo
					int bytesDecoded = videoCoder.decodeVideo(picture, packet,
							0);
					if (bytesDecoded < 0)
						throw new RuntimeException("Greska u dekodiranju "
								+ filename);

					// prveravamo da li je kompletna, dekoderi neki ne mogu da
					// naprave kompletnu
					if (picture.isComplete()) {
						IVideoPicture newPic = picture;

						// ako nije null, nismo dobili u BGR24
						if (resampler != null) {
							// moram resemplovati
							newPic = IVideoPicture.make(
									resampler.getOutputPixelFormat(),
									picture.getWidth(), picture.getHeight());
							if (resampler.resample(newPic, picture) < 0)
								throw new RuntimeException(
										"Nekompletna slika: " + filename);
						}
						if (newPic.getPixelType() != IPixelFormat.Type.BGR24)
							throw new RuntimeException(
									"Ne mogu da dekodiram u BGR 24 " + filename);

						long kasnjenje = milisekundeIzmedjuPrikazivanja(newPic);
						try {
							// ovde ako hocu stop samo stavim da spava -1
							if (kasnjenje > 0)
								Thread.sleep(kasnjenje);
							// Thread.sleep(-1);

						} catch (InterruptedException e) {
							try {
								connectionSocket.close();
								return;
							} catch (IOException e1) {
								// TODO Auto-generated catch block
								// e.printStackTrace();
							}
							return;
						}

						// konverzacija u sliku za prikazivanje

						try {
							image = Utils.videoPictureToImage(newPic);

							ByteArrayOutputStream baos = new ByteArrayOutputStream();
							ImageIO.write(image, "jpg", baos);

							// ImageIO.write(image,"png",baos);

							baos.flush();
							byte[] bytes = baos.toByteArray();
							baos.close();

							outToClient.writeUTF("video");
							end = bool.readBoolean();
							if (end == false) {
								int duzina = bytes.length;
								outToClient.writeInt(duzina);
								System.out.println(duzina);
								outToClientBytes.write(bytes, 0, duzina);
							} else {
								while (end == true) {
									end = bool.readBoolean();
								}
								int duzina = bytes.length;
								outToClient.writeInt(duzina);
								System.out.println(duzina);
								outToClientBytes.write(bytes, 0, duzina);
							}

						} catch (Exception e) {
							try {
								connectionSocket.close();
								return;
							} catch (IOException e2) {
								// TODO Auto-generated catch block
								// e.printStackTrace();
							}
						}
					}
				} else if (packet.getStreamIndex() == audioStreamId) {

					IAudioSamples samples = IAudioSamples.make(1024,
							audioCoder.getChannels());

					int offset = 0;

					while (offset < packet.getSize()) {

						int bytesDecoded = audioCoder.decodeAudio(samples,
								packet, offset);
						if (bytesDecoded < 0)
							throw new RuntimeException(
									"got error decoding audio in: " + filename);
						offset += bytesDecoded;
						/*
						 * Some decoder will consume data in a packet, but will
						 * not be able to construct a full set of samples yet.
						 * Therefore you should always check if you got a
						 * complete set of samples from the decoder
						 */
						// outToClient.writeInt(samples.getSize());
						if (samples.isComplete()) {

							byte[] rawBytes = samples.getData().getByteArray(0,
									samples.getSize());

							outToClient.writeUTF("audio");
							boolean end = bool.readBoolean();

							if (end == false) {
								outToClient.writeInt(samples.getSize());
								outToClient.writeInt(rawBytes.length);
								outToClientBytes.write(rawBytes, 0,
										rawBytes.length);

								System.out.println("Predajem audio stream "
										+ rawBytes.length);
							} else {
								while (end == true) {
									end = bool.readBoolean();
								}

								outToClient.writeInt(samples.getSize());
								outToClient.writeInt(rawBytes.length);
								outToClientBytes.write(rawBytes, 0,
										rawBytes.length);

								System.out.println("Predajem audio stream "
										+ rawBytes.length);
							}

						}
					}
					outToClient.writeInt(-2); // ovo saljemo kad nema audio
												// paketa

				}

				else { // ako nema stream u tom paketu odbacim ga I CAO
					do {
					} while (false);
				}

			}

			if (videoCoder != null) {
				videoCoder.close();
				videoCoder = null;
			}
			if (container != null) {
				container.close();
				container = null;
			}
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			try {
				connectionSocket.close();
				return;
			} catch (IOException e) {
				// TODO Auto-generated catch block
				// e.printStackTrace();
			}
			e1.printStackTrace();
		}

	}

	private void openJavaSound(IStreamCoder aAudioCoder)
			throws LineUnavailableException {
		AudioFormat audioFormat = new AudioFormat(aAudioCoder.getSampleRate(),
				(int) IAudioSamples.findSampleBitDepth(aAudioCoder
						.getSampleFormat()), aAudioCoder.getChannels(), true, /*
																			 * xuggler
																			 * defaults
																			 * to
																			 * signed
																			 * 16
																			 * bit
																			 * samples
																			 */
				false);
		DataLine.Info info = new DataLine.Info(SourceDataLine.class,
				audioFormat);
		mLine = (SourceDataLine) AudioSystem.getLine(info);

		mLine.open(audioFormat);

		mLine.start();

	}

}
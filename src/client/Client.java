/**
 * 
 */
package client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Timer;
import java.util.TimerTask;

import headers.AckHeader;
import headers.DataHeader;

abstract class Timeout extends TimerTask {
	public static int seq;

	public Timeout(int sequenceNumber) {
		seq = sequenceNumber;
	}
}

/**
 * @authors Kushal Mandh
 *
 */
public class Client {

	String hostName;
	int port;
	String fileName;
	int windowSize;
	int MSS;
	FileInputStream fin;
	int aws;// availableWindowSize:)
	int lastACK = 0;
	int sequenceNum = 0;
	ArrayList<Buffer> buffer;
	Socket client;
	DataOutputStream out;
	DataInputStream in;

	public Client(String hostName, int port, String fileName, int windowSize, int mSS) {
		this.hostName = hostName;
		this.port = port;
		this.fileName = fileName;
		this.windowSize = windowSize;
		this.MSS = mSS;
		buffer = new ArrayList<Buffer>();
		connectToServer();
	}

	void connectToServer() {
		try {
			client = new Socket(hostName, port);
			System.out.println("Just connected to " + client.getRemoteSocketAddress());
			out = new DataOutputStream(client.getOutputStream());
			in = new DataInputStream(client.getInputStream());
		} catch (IOException e) {
			System.out.println("Exiting. Error while connecting to server: " + e);
			System.exit(1);
		}
	}

	private void openFile() {
		File file = new File(fileName);
		try {
			fin = new FileInputStream(file);
		} catch (FileNotFoundException e) {
			System.out.println("File not found" + e);
		}
	}

	private Byte rdt_send() {
		try {
			return (byte) fin.read();
		} catch (FileNotFoundException e) {
			System.out.println("File not found" + e);
		} catch (IOException ioe) {
			System.out.println("Exception while reading file " + ioe);
		}
		return '\0';
	}

	private void closeFile() {
		try {
			if (fin != null) {
				fin.close();
			}
		} catch (IOException ioe) {
			System.out.println("Error while closing stream: " + ioe);
		}
	}

	Runnable send = new Runnable() {
		public void run() {
			openFile();
			try {
				while (fin != null && fin.available() > 0) {
					// check if buffer size is less than window size.
					if (buffer.size() < windowSize) {
						ArrayList<Byte> packetData = new ArrayList<Byte>();
						Byte nextByte = rdt_send();
						int count = 0;
						while (nextByte != '\0' && count < MSS) {
							packetData.add((Byte) nextByte);
							count++;
						}
						Buffer packet = new Buffer();
						packet.setSeq(sequenceNum);
						packet.setBuffer(packetData);
						buffer.add(packet);
						sendpacket(buffer.get(buffer.size() - 1));// sends,increments seq, starts timer.
					}
					// continue looping until all bytes are sent.
				}
			} catch (IOException e1) {
				System.out.println("Error while reading available bytes or sending the read bytes: " + e1);
			}
			closeFile();
		}

		private void sendpacket(Buffer packet) throws IOException {
			DataHeader dh = new DataHeader(sequenceNum, packet.getBuffer().toString());
			out.writeUTF(dh.getPacketWithHeaders());
			packet.setTimer(startTimer(packet.getSeq()));
			sequenceNum++;
		}

		private Timer startTimer(int sequence) {
			Timer t = new Timer();
			t.schedule(new Timeout(sequence) {
				public void run() {
					// timeout has occured. send again.
					retransmitOnFailure(seq);
					System.out.println("Timeout, sequence number = " + seq);
				}
			}, 1000);
			return t;
		}

		private void retransmitOnFailure(int failedSeq) {
			sequenceNum = failedSeq;
			int failedIndex = findIndex(failedSeq);
			for (int i = failedIndex; i < buffer.size(); i++) {
				try {
					sendpacket(buffer.get(i));
				} catch (IOException e) {
					// TODO Auto-generated catch block
					System.out.println("Error while retranmission: " + e);
				}
			}
		}
	};

	private int findIndex(int Seq) {
		int ans = -1;
		for (int i = 0; i < buffer.size(); i++) {
			long bufPacketSeq = buffer.get(i).getSeq();
			if (Seq == bufPacketSeq) {
				ans = i;
				break;
			}
		}
		if (ans == -1) {
			System.out.println("Error Occured while finding index. Returning -1.");
		}
		return ans;
	}

	Runnable receive = new Runnable() {
		public void run() {
			try {
				while (fin != null && fin.available() > 0 && buffer.size() != 0) {
					AckHeader ah = new AckHeader(in.readUTF());
					if(!ah.checkValidAck()) {
						continue;
					}
					int successAck = ah.getSequenceNumber();
					stopTimer(successAck);
					removeFromBuffer(successAck);
					lastACK = successAck;
				}
				client.close();
			} catch (IOException e) {
				System.out.println("Error while receiving ACK");
			}
		}

		private void removeFromBuffer(int successAck) {
			int indexInBuffer = findIndex(successAck);
			Collection<Buffer> c = new ArrayList<Buffer>();
			for (int i=0;i<=indexInBuffer;i++) {
				c.add(buffer.get(i));
			}
			buffer.removeAll(c);
		}

		private void stopTimer(int successAck) {
			int indexInBuffer = findIndex(successAck);
			for (int i=0;i<=indexInBuffer;i++) {
				buffer.get(i).getTimer().cancel();
			}
		}
	};

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		// parse args
		// call client tx and rx
		
		Client c = new Client("localhost", 7735, "C:\\Users\\Kushal Mandhani\\Desktop\\Lab3Answers.txt", 10, 500);
		Thread sender = new Thread(c.send);
		Thread rcvr = new Thread(c.receive);
		sender.start();
		rcvr.start();
		try {
			sender.join();
			rcvr.join();
		} catch (InterruptedException e) {
			System.out.println("An Error Occurred while trying to wait on child threads. Error: " + e);
		}
		System.out.println("All done!");
		// terminate everything and exit
	}

}

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
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import headers.AckHeader;
import headers.DataHeader;

class Timeout extends TimerTask {
	public int seq;

	public Timeout(int sequenceNumber) {
		seq = sequenceNumber;
	}

	public void run() {}
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
	int lastACK = -1;
	int sequenceNum = 0;
	ArrayList<Buffer> buffer;
	Socket client;
	DataOutputStream out;
	DataInputStream in;
	Lock lock = new ReentrantLock();
	Boolean doneSending = false;

	public Client(String hostName, int port, String fileName, int windowSize, int mSS) {
		this.hostName = hostName;
		this.port = port;
		this.fileName = fileName;
		this.windowSize = windowSize;
		this.MSS = mSS;
		buffer = new ArrayList<Buffer>();
		connectToServer();
		openFile();
	}

	void connectToServer() {
		try {
			client = new Socket(hostName, port);
			//System.out.println("Just connected to " + client.getRemoteSocketAddress());
			out = new DataOutputStream(client.getOutputStream());
			in = new DataInputStream(client.getInputStream());
		} catch (IOException e) {
			//System.out.println("Exiting. Error while connecting to server: " + e);
			System.exit(1);
		}
	}

	private void openFile() {
		File file = new File(fileName);
		try {
			fin = new FileInputStream(file);
		} catch (FileNotFoundException e) {
			//System.out.println("File not found" + e);
		}
	}

	private byte rdt_send() {
		try {
			if(fin.available()>0) {
				return (byte) fin.read();
			}
		} catch (FileNotFoundException e) {
			//System.out.println("File not found" + e);
		} catch (IOException ioe) {
			//System.out.println("Exception while reading file " + ioe);
		}
		return '\0';
	}

	private void closeFile() {
		try {
			if (fin != null) {
				fin.close();
			}
		} catch (IOException ioe) {
			//System.out.println("Error while closing stream: " + ioe);
		}
	}

	Runnable send = new Runnable() {
		public void run() {
			try {
				long startTime = System.nanoTime();
				while (fin != null && fin.available() > 0) {
					// check if buffer size is less than window size.
					if (buffer.size() < windowSize) {
						//byte[] packetData = new byte[];
						ArrayList<Byte> ab = new ArrayList<Byte>();
						byte nextByte;
						for(int i=0;i<MSS;i++) {
							if((nextByte = rdt_send()) == '\0') {
								break;
							}
							ab.add(nextByte);
						}
						byte[] packetData = new byte[ab.size()];
						for(int i=0;i<ab.size();i++) {
							packetData[i] = ab.get(i);
						}
						Buffer packet = new Buffer();
						packet.setSeq(sequenceNum);
						packet.setBuffer(packetData);
						//System.out.println(sequenceNum + ": " + packetData.length);
						//System.out.println("Waiting on lock");
						lock.lock();
						//System.out.println("Lock held by send");
						//System.out.println("Not any more");
						buffer.add(packet);
						sendpacket(buffer.get(buffer.size() - 1));// sends,increments seq, starts timer.
						lock.unlock();
						//System.out.println("Lock released by send");
						sequenceNum++;
					}
					Thread.sleep(1);
					// continue looping until all bytes are sent.
				}
				while(buffer.size()>0) {
					try {
						Thread.sleep(1000); 
						
						
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					//System.out.println("In loop. Closing file now. Buffer Size: " + buffer.size());
				}
				System.out.println("Closing file now. Buffer Size: " + buffer.size());
				long endTime   = System.nanoTime();
				long totalTime = endTime - startTime;
				System.out.println("Total time for transfer: " + totalTime / (1000000000) + " seconds.");
				doneSending = true;
				closeFile();
				System.exit(0);
			} catch (IOException e1) {
				//System.out.println("Error while reading available bytes or sending the read bytes: ");
				e1.printStackTrace();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		private void sendpacket(Buffer packet) throws IOException {
			DataHeader dh = new DataHeader(packet.getSeq(), getString(packet.getBuffer()));
			//System.out.println("Sending: " + packet.getSeq() + ", Length: "+getString(packet.getBuffer()).getBytes().length);
			out.writeUTF(dh.getPacketWithHeaders());
			//out.flush();
			packet.setTimer(startTimer(packet.getSeq()));
		}
		
		private String getString(byte[] data) {
			return new String(data);
		}

		private Timer startTimer(int sequence) {
			Timer t = new Timer();
			t.schedule(new Timeout(sequence) {
				@Override
				public void run() {
					// timeout has occured. send again.
					System.out.println("Timeout, sequence number = " + seq);
					retransmitOnFailure(seq);
				}
			}, 100);
			return t;
		}

		private void retransmitOnFailure(int failedSeq) {
			//sequenceNum = failedSeq;
			//int failedIndex = findIndex(failedSeq);
			try {
				lock.lock();
				//System.out.println("Lock held by retransmit");
				for (int i = 0; i < buffer.size(); i++) {
					stopTimer(i);
				}
				for (int i = 0; i < buffer.size(); i++) {
					//System.out.println("Retransmitting: "+ buffer.get(i).getSeq());
					sendpacket(buffer.get(i));
					//Thread.sleep(300);;
				}
				lock.unlock();
				//System.out.println("Lock released by retransmit");
			}catch (IOException e) {
					//System.out.println("Error while retranmission: ");
					e.printStackTrace();
			}
		}
	};

	private int findIndex(int Seq) {
		int ans = -1;
		for (int i = 0; i < buffer.size(); i++) {
			int bufPacketSeq = buffer.get(i).getSeq();
			//System.out.println("FindingIndex: given seq: " + Seq + ", Actual Seq in buffer: " + bufPacketSeq);
			if (Seq == bufPacketSeq) {
				ans = i;
				break;
			}
		}
		if (ans == -1) {
			//System.out.println("Error Occured while finding index " + Seq + ". Returning -1.");
		}
		return ans;
	}

	Runnable receive = new Runnable() {
		public void run() {
			try {
				while (!doneSending) {
					if(in.available()>0) {
						AckHeader ah = new AckHeader(in.readUTF());
						if(!ah.checkValidAck()) {
							//System.out.println("ValidCheckFail for ACK.");
							//continue;
						}
						int successAck = ah.getSequenceNumber();
						//System.out.println("Received Successful ACK for Seq: " + (successAck-1) + "current Buffer: " + buffer.size());
						removeFromBuffer(successAck);
						lastACK = successAck;
					}
				}
			} catch (IOException e) {
				//System.out.println("Error while receiving ACK: ");
				e.printStackTrace();
			} finally {
				//System.out.println("################Closing connection now.####################");
				try {
					client.close();
				} catch (IOException e) {
					System.out.println("Error while closing: ");
					e.printStackTrace();
				}
				System.exit(0);
			}
		}

		private void removeFromBuffer(int successAck) {
			int indexInBuffer = findIndex(successAck-1);
			if(indexInBuffer == -1) {
				return;
			}
			Collection<Buffer> c = new ArrayList<Buffer>();
			lock.lock();
			//System.out.println("Lock held by remove from buffer, buffer size: " + buffer.size());
			for (int i=0;i<=indexInBuffer;i++) {
				stopTimer(i);
				c.add(buffer.get(i));
				//System.out.println("Removing from buffer: " + buffer.get(i).getSeq());
			}
			buffer.removeAll(c);
			//System.out.println("Lock released by remove from buffer: " + buffer.size());
			lock.unlock();
		}
	};

	private void stopTimer(int index) {
		buffer.get(index).getTimer().cancel();
	}

	/**
	 * @param args
	 */
	public static void main(String[] argv) {
		int port = Integer.parseInt(argv[1]);
		String host = argv[0];
		String file = argv[2];
		int N = Integer.parseInt(argv[3]);
		int MSS = Integer.parseInt(argv[4]);
		Client c = new Client(host,port,file,N,MSS);
		Thread sender = new Thread(c.send);
		Thread rcvr = new Thread(c.receive);
		sender.start();
		rcvr.start();
		try {
			sender.join();
			rcvr.join();
		} catch (InterruptedException e) {
			//System.out.println("An Error Occurred while trying to wait on child threads. Error: " + e);
		}
		//System.out.println("All done!");
		// terminate everything and exit
	}

}

package server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import headers.AckHeader;
import headers.DataHeader;

public class Server {
	int port;
	String clientName;
	String fileName;
	double probability;
	int timeout = 10000;
	Socket server;
	DataInputStream in;
	DataOutputStream out;
	int lastAck = 0;
	ServerSocket serverSocket;

	public Server(int portNum, String fname, double p) {
		port = portNum;
		fileName = fname;
		probability = p;
		// startServer();
	}

	private void startServer() {
		try {
			serverSocket = new ServerSocket(port);
			server = serverSocket.accept();
			in = new DataInputStream(server.getInputStream());
			out = new DataOutputStream(server.getOutputStream());
			//System.out.println("Connected!");
		} catch (IOException e) {
			//System.out.println("Error while connecting to client: " + e);
			System.exit(1);
		}
	}

	Runnable receive = new Runnable() {
		public void run() {
			try {
				startServer();
				while (!server.isClosed()) {
					if(in.available()>0) {
						//System.out.println("Receiving: " + lastAck);
						DataHeader dh = new DataHeader(in.readUTF());
						//System.out.println("Received: " + dh.getData());
						if(!dh.validateChecksum()) {
							//System.out.println("Checksum Validation failed");
							continue;
						}
						int currentSeq = dh.getSequenceNumber();
						//System.out.println("Received Seq:" + currentSeq + ", expectedSeq: " + lastAck);
						if(currentSeq != lastAck) {
							//System.out.println("seq number order failed");
							continue;
						}
						double r = Math.random();
						if(r <= probability) {
							System.out.println("Packet loss, sequence number = " + lastAck ); 
							continue;
						}
						lastAck = currentSeq+1;
						sendAck(lastAck);
						writeToFile(dh.getData());
					}
				}
				serverSocket.close();
				server.close();
				return;
			} catch (IOException e) {
				//System.out.println("Error while receiving packet or sending ACK : ");
				e.printStackTrace();
				return;
			}
		}

		private void writeToFile(String data) {
			File file = new File(fileName);
			try {
				FileOutputStream fout = new FileOutputStream(file, true);
				fout.write(data.getBytes());
				fout.close();
			} catch (IOException e) {
				//System.out.println("Failed to write to file: " + e);
			}
		}

		private void sendAck(int AckSeq) throws IOException {
			AckHeader ah = new AckHeader(AckSeq);
			out.writeUTF(ah.getHeader());
			//System.out.println("SentAck for :" +AckSeq);
		}
	};

	public static void main(String[] argv) {
		int prt = Integer.parseInt(argv[0]);
		String file = argv[1];
		Double p = Double.parseDouble(argv[2]);
		Server s = new Server(prt, file, p);
		Thread server = new Thread(s.receive);
		server.start();
		try {

			Thread.sleep(10000);
			while(!s.serverSocket.isClosed()) {
				Thread.sleep(1000);
			}
			server.interrupt();
			return;
		} catch (InterruptedException e) {
			System.out.println("An Error Occurred while trying to wait on child threads. Error: " + e);
		}
		//System.out.println("All done!");
	}

}

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
	int lastAck=0;
	ServerSocket serverSocket;
	
	public Server(int portNum, String fname, double p) {
		port = portNum;
		fileName = fname;
		probability = p;
		//startServer();
	}
	
	private void startServer() {
		try {
			serverSocket = new ServerSocket(port);
			server = serverSocket.accept();
			in = new DataInputStream(server.getInputStream());
			out = new DataOutputStream(server.getOutputStream());
		} catch (IOException e) {
			System.out.println("Error while connecting to client: " + e);
			System.exit(1);
		}
	}
	
	Runnable receive = new Runnable() {
		public void run() {
			try {
				startServer();
				while (!server.isClosed()) {
					DataHeader dh = new DataHeader(in.readUTF());
					if(!dh.validateChecksum()) {
						continue;
					}
					int currentSeq = dh.getSequenceNumber();
					if(currentSeq != lastAck) {
						continue;
					}
					double r = Math.random();
					if(r <= probability) {
						continue;
					}
					lastAck = currentSeq+1;
					sendAck(lastAck);
					writeToFile(dh.getData());
				}
				serverSocket.close();
				server.close();
			} catch (IOException e) {
				System.out.println("Error while receiving packet or sending ACK : " + e);
			}
		}

		private void writeToFile(String data) {
			File file = new File(fileName);
			try {
				FileOutputStream fout = new FileOutputStream(file);
				fout.write(data.getBytes());
				fout.close();
			} catch (IOException e) {
				System.out.println("Failed to write to file: " + e);
			}
		}

		private void sendAck(int AckSeq) throws IOException {
			AckHeader ah = new AckHeader(AckSeq);
			out.writeUTF(ah.getHeader());
		}
	};

	public static void main(String[] args) {
		int prt = 7735;
		Double p = Double.parseDouble("0.5");
		Server s = new Server(prt, "localhost", p);
		Thread server = new Thread(s.receive);
		server.start();
		try {
			server.join();
		} catch (InterruptedException e) {
			System.out.println("An Error Occurred while trying to wait on child threads. Error: " + e);
		}
		System.out.println("All done!");
	}

}

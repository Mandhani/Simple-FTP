package headers;

import java.nio.ByteBuffer;

public class AckHeader {
	int sequenceNumber;
	char checksum = 0;
	final char identifier = (char)Integer.parseInt("1010101010101010", 2);
	String header;
	
	public AckHeader(String packet) {
		ByteBuffer b = ByteBuffer.allocate(4);
		b.putChar(0,packet.charAt(0));
		b.putChar(2,packet.charAt(1));
		sequenceNumber = b.getInt(0);
		header = packet;
	}
	
	public int getSequenceNumber() {
		return sequenceNumber;
	}
	
	public boolean checkValidAck() {
		if(header.charAt(2) != 0) {
			return false;
		}
		if(header.charAt(3) != identifier) {
			return false;
		}
		return true;
	}
	//client methods end
	//server methods start
	public AckHeader(int seq) {
		sequenceNumber = seq;
		ByteBuffer b = ByteBuffer.allocate(4);
		b.putInt(sequenceNumber);
		char c1 = b.getChar(0);
		char c2 = b.getChar(2);
		header = String.valueOf(c1) + String.valueOf(c2) + String.valueOf(checksum) + String.valueOf(identifier);
	}
	
	public String getHeader() {
		return header;
	}
}

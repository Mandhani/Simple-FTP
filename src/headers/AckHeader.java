package headers;

public class AckHeader {
	int sequenceNumber;
	char checksum = 0;
	final char identifier = (char)Integer.parseInt("1010101010101010", 2);
	String header;
	
	public AckHeader(String packet) {
		sequenceNumber = Integer.parseInt(String.valueOf(packet.charAt(0)) + String.valueOf(packet.charAt(1)));
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
		header = String.valueOf(sequenceNumber) + String.valueOf(checksum) + String.valueOf(identifier);
	}
	
	public String getHeader() {
		return header;
	}
}

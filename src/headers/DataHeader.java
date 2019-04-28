package headers;

import java.nio.ByteBuffer;

public class DataHeader {
	int sequenceNumber;
	char checksum = 0;
	final char identifier = (char)Integer.parseInt("0101010101010101", 2);
	String data;
	
	public DataHeader(int seq, String data) {
		sequenceNumber = seq;
		this.data = data;
		checksum = calculateChecksum();
	}

	private char calculateChecksum() {
		char ans = 0;
		String csdata = getPacketWithHeaders(); //checksum initialized to 0. Using the headers to calculate the header.
		char[] sa = csdata.toCharArray();//16bytes ;P
		for (int i=0;i<sa.length;i++) {
			ans += sa[i];
		}
		return onesComplement(ans);
	}
	
	private char onesComplement(char n) 
    {
        int number_of_bits = (int)(Math.floor(Math.log(n) / Math.log(2))) + 1; 
        return (char) (((1 << number_of_bits) - 1) ^ n); 
    } 
	
	public String getPacketWithHeaders() {
		ByteBuffer b = ByteBuffer.allocate(4);
		b.putInt(sequenceNumber);
		char c1 = b.getChar(0);
		char c2 = b.getChar(2);
		//System.out.println("Length: " + (String.valueOf(c1) + String.valueOf(c2) + String.valueOf(checksum) + String.valueOf(identifier) + data).length());
		return String.valueOf(c1) + String.valueOf(c2) + String.valueOf(checksum) + String.valueOf(identifier) + data; 
	}
	//client methods end
	//server methods start
	public DataHeader(String packet) {
		ByteBuffer b = ByteBuffer.allocate(4);
		b.putChar(0,packet.charAt(0));
		b.putChar(2,packet.charAt(1));
		sequenceNumber = b.getInt(0);
		checksum = packet.charAt(2);
		data = packet.substring(4, packet.length());
	}
	
	public int getSequenceNumber() {
		return sequenceNumber;
	}
	
	public String getData() {
		return data;
	}
	
	public boolean validateChecksum() {
		//System.out.println("Checksum: " + Integer.toBinaryString(calculateChecksum()) + ", Expected: " + Integer.toBinaryString((char)Integer.parseInt("1111111111111111", 2)));
		if(calculateChecksum() == (char)Integer.parseInt("0", 2)) {
			return true;
		}
		return false;
	}
	
	
}

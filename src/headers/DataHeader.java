package headers;

import java.util.Arrays;

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
		return String.valueOf(sequenceNumber) + String.valueOf(checksum) + String.valueOf(identifier) + data; 
	}
	//client methods end
	//server methods start
	public DataHeader(String packet) {
		sequenceNumber = Integer.parseInt(String.valueOf(packet.charAt(0)) + String.valueOf(packet.charAt(1)));
		checksum = packet.charAt(2);
		char[] dataArr = Arrays.copyOfRange(packet.toCharArray(), 4, packet.toCharArray().length);
		data = dataArr.toString();
	}
	
	public int getSequenceNumber() {
		return sequenceNumber;
	}
	
	public String getData() {
		return data;
	}
	
	public boolean validateChecksum() {
		if(calculateChecksum() == (char)Integer.parseInt("1111111111111111", 2)) {
			return true;
		}
		return false;
	}
}

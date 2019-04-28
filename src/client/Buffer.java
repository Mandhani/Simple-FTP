/**
 * 
 */
package client;

import java.util.ArrayList;
import java.util.Timer;

/**
 * @author Kushal Mandhani
 *
 */
public class Buffer {
	int seq;
	ArrayList<Byte> buffer;
	Timer timer;
	
	public Timer getTimer() {
		return timer;
	}
	public void setTimer(Timer t) {
		this.timer = t;
	}
	public int getSeq() {
		return seq;
	}
	public void setSeq(int seq) {
		this.seq = seq;
	}
	public ArrayList<Byte> getBuffer() {
		return buffer;
	}
	public void setBuffer(ArrayList<Byte> buffer) {
		this.buffer = buffer;
	}
}

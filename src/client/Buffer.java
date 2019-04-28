/**
 * 
 */
package client;

import java.util.Timer;

/**
 * @author Kushal Mandhani
 *
 */
public class Buffer {
	int seq;
	byte[] buffer;
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
	public byte[] getBuffer() {
		return buffer;
	}
	public void setBuffer(byte[] buffer) {
		this.buffer = buffer;
	}
}

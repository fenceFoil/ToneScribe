/**
 * 
 */
package com.tonescribe.song;

/**
 * Represents a tone with duration and pitch.
 * 
 */
public class ToneEvent extends SongEvent {
	public double pitch;


	public ToneEvent(double timeSeconds, double durationSeconds, double pitch) {
		super(timeSeconds, durationSeconds);
		this.pitch = pitch;
	}

	public double getPitch() {
		return pitch;
	}

	public void setPitch(double pitch) {
		this.pitch = pitch;
	}

}

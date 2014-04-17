/**
 * 
 */
package com.tonescribe.song;

/**
 *
 */
public class SongEvent {


	public SongEvent(double timeSeconds, double durationSeconds) {
		super();
		this.timeSeconds = timeSeconds;
		this.durationSeconds = durationSeconds;
	}

	public double timeSeconds;
	public double durationSeconds;

	public double getTimeSeconds() {
		return timeSeconds;
	}

	public void setTimeSeconds(double timeSeconds) {
		this.timeSeconds = timeSeconds;
	}

	public double getDurationSeconds() {
		return durationSeconds;
	}

	public void setDurationSeconds(double durationSeconds) {
		this.durationSeconds = durationSeconds;
	}

}

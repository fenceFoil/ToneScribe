/**
 * 
 */
package com.tonescribe.song;

import java.util.LinkedList;

/**
 * Represents a sequence of tones and other events to be processed and played in
 * order. The events contained in an instance of this class should represent
 * "processed" tones; all timing, pitch-adjusting, and accidentals should be
 * accounted for before being written into a Song.
 * 
 */
public class Song {
	private LinkedList<SongEvent> tones = new LinkedList<SongEvent>();
	private LinkedList<String> errors = new LinkedList<String>();

	private double currLengthSec = 0;
	private double selectionStartTimeSec = 0;
	private double selectionEndTimeSec = Double.MAX_VALUE;

	public Song() {

	}

	public LinkedList<SongEvent> getTones() {
		return tones;
	}

	public void setTones(LinkedList<SongEvent> tones) {
		this.tones = tones;
	}

	public LinkedList<String> getErrors() {
		return errors;
	}

	public void setErrors(LinkedList<String> errors) {
		this.errors = errors;
	}

	public void addTone(int noteValue, double secDuration) {
		tones.add(new ToneEvent(currLengthSec, secDuration,
				getNoteFreq(noteValue)));
		incrementLength(secDuration);
	}

	public void addTone(double freq, double secDuration) {
		tones.add(new ToneEvent(currLengthSec, secDuration, freq));
		incrementLength(secDuration);
	}

	public void addRest(double secDuration) {
		tones.add(new RestEvent(currLengthSec, secDuration));
		incrementLength(secDuration);
	}

	public void addTone(int noteValue, double wholeNoteDuration, double tempoBPM) {
		double freq = getNoteFreq(noteValue);
		double quarterNoteDuration = wholeNoteDuration * 4;
		// (sec / beat) = 1 / ( (beats / min) * (1min / 60sec) )
		double tempoSecPerQuarterNote = 1.0 / (tempoBPM * (1.0 / 60.0));
		double noteDurationSec = quarterNoteDuration * tempoSecPerQuarterNote;
		tones.add(new ToneEvent(currLengthSec, noteDurationSec, freq));
		incrementLength(noteDurationSec);
	}

	public void addRest(double wholeNoteDuration, double tempoBPM) {
		double quarterNoteDuration = wholeNoteDuration * 4;
		// (sec / beat) = 1 / ( (beats / min) * (1min / 60sec) )
		double tempoSecPerQuarterNote = 1.0 / (tempoBPM * (1.0 / 60.0));
		double noteDurationSec = quarterNoteDuration * tempoSecPerQuarterNote;
		tones.add(new RestEvent(currLengthSec, noteDurationSec));
		incrementLength(noteDurationSec);
	}

	private void incrementLength(double noteDurationSec) {
		currLengthSec += noteDurationSec;
		// System.out.println (currLengthSec);
	}

	public static double getNoteFreq(int noteValue) {
		return 8.1757989156 * Math.pow(2.0, noteValue / 12.0);
	}

	public double getCurrLengthSec() {
		return currLengthSec;
	}

	public void setCurrLengthSec(double currLengthSec) {
		this.currLengthSec = currLengthSec;
	}

	public double getSelectionStartTimeSec() {
		return selectionStartTimeSec;
	}

	public void setSelectionStartTimeSec(double selectionStartTimeSec) {
		this.selectionStartTimeSec = selectionStartTimeSec;
	}

	public double getSelectionEndTimeSec() {
		return selectionEndTimeSec;
	}

	public void setSelectionEndTimeSec(double selectionEndTimeSec) {
		this.selectionEndTimeSec = selectionEndTimeSec;
	}
}

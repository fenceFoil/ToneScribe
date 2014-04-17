/**
 * 
 */
package com.tonescribe.song.linker;

import com.tonescribe.song.RestEvent;
import com.tonescribe.song.Song;
import com.tonescribe.song.SongEvent;
import com.tonescribe.song.ToneEvent;

/**
 * Converts a compiled song into a snippet of computer code that can be pasted
 * into a program to play the compiled song.
 * 
 */
public class GenericSongLinker implements SongLinker {
	public String link(Song song) {
		StringBuilder builder = new StringBuilder();
		for (SongEvent e : song.getTones()) {
			if (e.getTimeSeconds() < song.getSelectionStartTimeSec() || e.getTimeSeconds() > song.getSelectionEndTimeSec()) {
				continue;
			}
			
			if (e instanceof ToneEvent) {
				builder.append("beep(")
						.append((int) ((ToneEvent) e).getPitch()).append(", ")
						.append((int) (e.getDurationSeconds() * 1000.0))
						.append(");")
						.append(System.getProperty("line.separator"));
			} else if (e instanceof RestEvent) {
				builder.append("delayMS(")
						.append((int) (e.getDurationSeconds() * 1000.0))
						.append(");")
						.append(System.getProperty("line.separator"));
			}
		}
		return builder.toString();
	}
}

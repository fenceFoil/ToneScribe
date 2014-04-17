/**
 * 
 */
package com.tonescribe.song.compiler;

import com.tonescribe.song.Song;

/**
 * Converts a String that represents a sequence of tones in some textual format
 * into a series of event objects representing individual tones.
 * 
 */
public interface SongCompiler {
	public Song compile (String text, int selectionStart, int selectionEnd);
}

/**
 * Part of ToneScribe
 * Copyright (c) 2014, William Karnavas
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 *  * Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 * 
 *  * Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 * 
 *  * Neither the name of the {organization} nor the names of its
 *   contributors may be used to endorse or promote products derived from
 *   this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.tonescribe.song.compiler;

import com.tonescribe.song.Song;

/**
 * Interprets Ring Tone Text Transfer Language songs into an abstract format.
 * This interpreter is guaranteed to understand valid RTTTL, but is NOT
 * guaranteed to check for perfectly-formatted RTTTL; if you put something
 * else's RTTTL in here, it will work, but if you use things that work with this
 * RTTTL interpreter somewhere else, it might not understand. Standard RTTTL is
 * just a subset of what this interpreter allows.
 * 
 */
public class RTTTLSongCompiler implements SongCompiler {

	@Override
	public Song compile(String text, int selStart, int selEnd) {
		Song song = new Song();

		// Split song into Name, Settings, and Notes sections
		String[] sections = text.split(":");

		// Check for presence of all 3 sections
		if (sections.length != 3) {
			if (sections.length > 3) {
				song.getErrors()
						.add("Too many sections: remove extra colons (only 2 allowed in file)");
			} else {
				song.getErrors()
						.add("Missing sections: add either a name, settings, or notes until there are 3 sections of this song separated by colons (':')");
			}
			return song;
		}

		// Separate sections
		String nameSection = sections[0];
		String settingsSection = sections[1];
		String notesSection = sections[2];

		// Compile name
		// TODO: Pay attention to name
		if (nameSection.length() > 10) {
			song.getErrors().add("Name of song must be 10 letters or less.");
		}

		// Compile settings
		// Break down into a list of keys
		String[] settings = settingsSection.split(",");
		int defaultDuration = 4;
		int defaultOctave = 6;
		int tempo = 63;
		try {
			for (String s : settings) {
				String key = s.toLowerCase().trim();
				String value = key.substring(key.indexOf("=") + 1);
				int valueNum = Integer.parseInt(value);
				switch (key.charAt(0)) {
				case 'd':
					defaultDuration = valueNum;
					break;
				case 'o':
					defaultOctave = valueNum;
					break;
				case 'b':
					tempo = valueNum;
					break;
				default:
					song.getErrors().add(
							"Value " + s
									+ " in settings section is not allowed.");
					return song;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			song.getErrors()
					.add("Cannot read settings section. Look at a working example for help.");
			return song;
		}

		// Compile notes
		// TODO: Try/catch around
		try {
			String[] notes = notesSection.split(",");
			for (String s : notes) {
				String note = s.toLowerCase().trim();

				int duration = defaultDuration;
				int octave = defaultOctave;
				boolean dotted = false;

				int currIndex = 0;
				// Read duration number
				String durationString = "";
				for (int i = 0; i < 2; i++) {
					if (note.length() > i && Character.isDigit(note.charAt(i))) {
						durationString += note.charAt(i);
						currIndex++;
					} else {
						break;
					}
				}
				if (durationString.length() > 0) {
					duration = Integer.parseInt(durationString);
				}

				try {
					// Read special duration (NON-STANDARD)
					if (note.charAt(currIndex) == '.') {
						dotted = true;
						currIndex++;
					}
				} catch (Exception e) {
				}

				// Read note letter
				// NON-STANDARD: Allow for flats
				String firstLetter = "cdefgabp";
				String secondLetter = "#b";
				String scaleSpacing = "c d ef g a b";
				int noteValue = 0;
				boolean isRest = false;
				try {
					if (firstLetter.indexOf(note.charAt(currIndex)) > -1) {
						// Valid note
						// TODO Parse
						if (note.charAt(currIndex) == 'p') {
							// Rest
							isRest = true;
						} else {
							noteValue = scaleSpacing.indexOf(note
									.charAt(currIndex));
						}
						currIndex++;
						// look for second letter
						if (note.length() > currIndex
								&& secondLetter.indexOf(note.charAt(currIndex)) > -1) {
							// Modifier found!
							if (note.charAt(currIndex) == 'b') {
								noteValue--;
							} else {
								noteValue++;
							}
							currIndex++;
						} else {
							// No second letter. That's fine.
						}

					} else {
						// Invalid text
						song.getErrors().add("Cannot read note: " + note);
						return song;
					}
				} catch (Exception e1) {
					song.getErrors().add(
							"Cannot read note; something is wrong.");
					return song;
				}

				// TODO Read note scale
				try {
					if (note.charAt(currIndex) >= '4'
							&& note.charAt(currIndex) <= '7') {
						switch (note.charAt(currIndex)) {
						case '4':
							octave = 4;
							break;
						case '5':
							octave = 5;
							break;
						case '6':
							octave = 6;
							break;
						case '7':
							octave = 7;
							break;
						}
						currIndex++;
					} else if (Character.isDigit(note.charAt(currIndex))) {
						song.getErrors().add("RTTTL only allows octaves 4-7.");
						return song;
					}
				} catch (Exception e1) {
				}

				try {
					// Read special duration
					if (note.charAt(currIndex) == '.') {
						dotted = true;
						currIndex++;
					}
				} catch (Exception e) {
				}

				// If we have fallen down here, process the newly read note!
				double wholeNoteDuration = 1.0 / duration;
				if (dotted) {
					wholeNoteDuration *= 1.5;
				}
				// Add note to song
				if (!isRest) {
					noteValue = noteValue + (octave + 1) * 12;
					//System.out.println("noteValue = " + noteValue
					//		+ " duration = " + wholeNoteDuration);
					song.addTone(noteValue, wholeNoteDuration, tempo);
				} else {
					song.addRest(wholeNoteDuration, tempo);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			song.getErrors().add("Cannot read notes; something is wrong.");
			return song;
		}

		return song;
	}
}

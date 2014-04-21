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
package com.tonescribe.song.compiler.musicstring;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.LinkedList;

import javax.swing.JOptionPane;

import com.tonescribe.song.Song;
import com.tonescribe.song.compiler.SongCompiler;

/**
 * Compiles music in a format resembling JFugue 4's MusicString, with several
 * differences: <br>
 * * No voice or layer changes <br>
 * * No instrument changes<br>
 * * No chords <br>
 * * Default octave is 4; octave numbers follow musical standards (JFugue's
 * default is "5", sounds like 4). <br>
 * * Key signature and tempo changes are supported <br>
 * * Sharp and flat note modifiers can be placed anywhere (or at least in more
 * places) in a note token <br>
 * * Added transposition tokens (Tran+1, Tran-1, TranReset) <br>
 * * Enhanced tempo change token: <br>
 * * Relative change (T+100 T-100) <br>
 * * Change over time (T100hh, T200q.) <br>
 * * Relative change over time (T+120wwww, T-120hhh) <br>
 * * Comments start with a '#', extend to end of lines<br>
 * <br>
 * Beat is used interchangeably with "quarter note."<br>
 * 
 */
public class MusicStringSongCompiler implements SongCompiler {

	private static final HashMap<String, Integer> keySignatures = new HashMap<String, Integer>();
	static {
		BufferedReader in = new BufferedReader(new InputStreamReader(
				MusicStringSongCompiler.class
						.getResourceAsStream("keysigs.txt")));
		try {
			String lineIn = in.readLine();
			while (lineIn != null) {
				if (!lineIn.startsWith("#")) {
					String[] line = lineIn.split("=");
					keySignatures.put(line[0].trim(),
							Integer.parseInt(line[1].trim()));
				}
				lineIn = in.readLine();
			}
			in.close();
		} catch (NumberFormatException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
			JOptionPane
					.showMessageDialog(
							null,
							"Cannot load key signature tokens; cannot read keysigs.txt. Please repackage ToneScribe with keysigs.txt in the same location as MusicStringSongCompiler.java.");
		}
	}

	private int currTran;
	private double currTimeBeat;
	private double currTimeMS;
	// For a gradual tempo change, this should be set immediately to the final
	// tempo, not left as the initial tempo.
	private double currTempo;

	// Both in quarter notes
	private double tempoChangeStart;
	private double tempoChangeLength;
	// BPM values at beginning and end of change
	private double tempoChangeInitial, tempoChangeFinal;

	private int keySig = 0;

	private int selectionStart;
	private int selectionEnd;

	private void resetCompiler(int selectionStart, int selectionEnd) {
		// Reset counters and settings
		currTimeMS = 0;
		currTimeBeat = 0;
		currTran = 0;
		currTempo = 120;
		keySig = 0;

		// Reset tempo changes
		tempoChangeStart = 0;
		tempoChangeLength = 0;
		tempoChangeInitial = 0;
		tempoChangeFinal = 0;

		this.selectionStart = selectionStart;
		this.selectionEnd = selectionEnd;
	}

	@Override
	public Song compile(String text, int selectionStart, int selectionEnd) {
		resetCompiler(selectionStart, selectionEnd);

		Song song = new Song();

		// Tokenize text
		LinkedList<Token> tokens = tokenize(text);

		// Compile tokens
		// Assumes tokens are at least one character
		for (Token token : tokens) {
			String tokenText = token.getTokenText().toLowerCase();

			// Check for tran tokens (otherwise caught as tempo since they start
			// with 't')
			if (tokenText.startsWith("tran")) {
				// Tran token
				if (isValidTranToken(token)) {
					processTranToken(token);
				} else {
					song.getErrors().add("Cannot read a \"tran\" token.");
					return song;
				}
			} else {
				// Check for other types of tokens
				switch (tokenText.charAt(0)) {
				case 'a':
				case 'b':
				case 'c':
				case 'd':
				case 'e':
				case 'f':
				case 'g':
					// Note
				case 'r':
					// Rest (handled just like a note)
					String error = processNote(token, song);
					if (error != null) {
						song.getErrors().add(error);
						return song;
					}
					break;
				case 'k':
					// Key signature
					String error2 = processKeySig(token);
					if (error2 != null) {
						song.getErrors().add(error2);
						return song;
					}
					break;
				case 't':
					// Tempo
					String error3 = processTempo(token);
					if (error3 != null) {
						song.getErrors().add(error3);
						return song;
					}
					break;
				default:
					song.getErrors().add(
							"Cannot read this word: " + token.getTokenText()
									+ " . Try putting a '#' in front of it.");
					return song;
				}
			}
		}

		return song;
	}

	private boolean isValidTranToken(Token token) {
		String tokenStr = token.getTokenText().toLowerCase();
		return tokenStr.matches("tran[\\+-]?\\d+")
				|| tokenStr.matches("tranreset");
	}

	private void processTranToken(Token token) {
		String tokenStr = token.getTokenText().toLowerCase();
		// Remove "tran"
		tokenStr = tokenStr.substring(4);
		// Look for "reset"
		if (tokenStr.equals("reset")) {
			// Reset transposition
			currTran = 0;
		} else {
			// Parse number
			int tranAmt = Integer.parseInt(tokenStr);
			currTran += tranAmt;
		}
	}

	/**
	 * Adds to current time in MS and Beats, accounting for current and changing
	 * tempo.
	 * 
	 * @param beats
	 * @return ms that time was incremented
	 */
	private double incrementCurrTime(double beats) {
		// If tempo still changing, increment time to "beats" or end of change,
		// whichever comes first
		double oldCurrTimeMS = currTimeMS;
		double beatsAtConstantTempo = beats;
		if (currTimeBeat < getTempoChangeEnd()) {
			// System.out.println("---" + "currTime=" + currTimeBeat);
			// System.out.println(tempoChangeStart + " " + tempoChangeLength
			// + " / " + tempoChangeInitial + " " + tempoChangeFinal);

			beatsAtConstantTempo = (currTimeBeat + beats) - getTempoChangeEnd();

			double endBeat = Math
					.min(currTimeBeat + beats, getTempoChangeEnd());

			// System.out.println(endBeat);

			// Plug into formula for integral from start beat to end beat to
			// find length of "beats" in minutes
			double lowerBound = currTimeBeat - tempoChangeStart;
			double upperBound = endBeat - tempoChangeStart;
			double beatLength = tempoChangeLength;
			// System.out.println(lowerBound + " to " + upperBound +
			// " length is "
			// + beatLength);
			double minutesOfBeats = tempoTimeIntegralTerm(upperBound,
					beatLength, tempoChangeInitial, tempoChangeFinal)
					- tempoTimeIntegralTerm(lowerBound, beatLength,
							tempoChangeInitial, tempoChangeFinal);
			double msOfBeats = minutesOfBeats * 60 * 1000;
			currTimeMS += msOfBeats;
		}

		// Add time at a constant tempo
		if (beatsAtConstantTempo > 0) {
			currTimeMS += beatsAtConstantTempo * (1 / currTempo) * 60 * 1000;
		}

		// Increment current time in beats; dead simple
		currTimeBeat += beats;

		// Return number of MS that time was advanced
		return currTimeMS - oldCurrTimeMS;
	}

	/**
	 * Used in incrementCurrTime()
	 * 
	 * @return
	 */
	private double tempoTimeIntegralTerm(double beat, double beatLength,
			double initialTempo, double finalTempo) {
		return (beat / initialTempo)
				+ ((1 / finalTempo - 1 / initialTempo) / (2 * beatLength))
				* (beat) * (beat);
	}

	private double getTempoChangeEnd() {
		return tempoChangeStart + tempoChangeLength;
	}

	/**
	 * Tempo token forms:<br>
	 * T120 -- Sets tempo to 120 BPM<br>
	 * T+120 -- Increases tempo by 120 BPM<br>
	 * T-120 -- Decreases tempo by 120 BPM<br>
	 * T120www -- Gradually sets tempo to 120 BPM over 3 whole notes<br>
	 * T+120www / T-120www -- Gradually changes tempo by 120 BPM over 3 whole
	 * notes <br>
	 * 
	 * @param token
	 * @return
	 */
	private String processTempo(Token token) {
		String tokenStr = token.getTokenText().toLowerCase();
		if (!tokenStr.matches("t[\\+-]?\\d+([whqistxo]\\.?)*[\\*]?")) {
			// Invalid token
			return "Cannot read tempo change: " + token.getTokenText();
		}

		tokenStr = tokenStr.substring(1);

		// Process optional +/- before tempo change amount
		boolean relative = false;
		boolean negativeChange = false;
		if (tokenStr.charAt(0) == '+' || tokenStr.charAt(0) == '-') {
			if (tokenStr.charAt(0) == '-') {
				negativeChange = true;
			}

			tokenStr = tokenStr.substring(1);
			relative = true;
		}

		// Process tempo change
		// Loop through string until the end of the string or the end of the
		// digits
		StringBuilder tempoAmt = new StringBuilder();
		int lastDigitIndex = 0;
		for (int i = 0; i < tokenStr.length()
				&& Character.isDigit(tokenStr.charAt(i)); i++) {
			tempoAmt.append(tokenStr.charAt(i));
			lastDigitIndex = i;
		}
		double tempoChange = Integer.parseInt(tempoAmt.toString());
		// Remove everything up to here
		tokenStr = tokenStr.substring(lastDigitIndex);

		// Process tempo length
		double wholeNoteDuration = 0;
		boolean triplet = false;
		for (int i = 0; i < tokenStr.length(); i++) {
			char c = tokenStr.charAt(i);

			if (c == '*') {
				triplet = true;
			}

			// Handle duration letters
			String durationLetters = "whqistxo";
			if (durationLetters.indexOf(c) >= 0) {
				double d = 1d / Math.pow(2, (durationLetters.indexOf(c)));
				// Check for dot
				if (i + 1 < tokenStr.length()) {
					if (tokenStr.charAt(i + 1) == '.') {
						// Found; dotted duration
						d *= 1.5;
						// Move loop to after the dot
						i++;
					}
				}
				wholeNoteDuration += d;
			}
		}

		if (triplet) {
			wholeNoteDuration *= 2d / 3d;
		}

		// Apply tempo change
		double oldTempo = currTempo;
		if (!relative) {
			currTempo = tempoChange;
		} else {
			if (negativeChange) {
				tempoChange = -tempoChange;
			}
			currTempo += tempoChange;
		}

		if (wholeNoteDuration > 0) {
			// Set up gradual change
			tempoChangeStart = currTimeBeat;
			tempoChangeLength = wholeNoteDuration * 4;
			tempoChangeInitial = oldTempo;
			tempoChangeFinal = currTempo;
		}

		// Return success
		return null;
	}

	private String processKeySig(Token token) {
		String tokenStr = token.getTokenText().toLowerCase();
		if (!keySignatures.containsKey(tokenStr)) {
			// Not a valid token
			return "Could not read key signature: " + token.getTokenText();
		}

		keySig = keySignatures.get(tokenStr);

		// Successful
		return null;
	}

	private String processNote(Token token, Song song) {
		String tokenStr = token.getTokenText().toLowerCase();

		// Check for valid note before beginning
		if (tokenStr
				.matches("[abcdefg][#bn]?[\\d]?([whqistxo]\\.?)*[\\*]?[!]?")
				|| tokenStr
						.matches("[abcdefg][\\d]?[#bn]?([whqistxo]\\.?)*[\\*]?[!]?")
				|| tokenStr.matches("r([whqistxo]\\.?)*[\\*]?")) {
			// Good note
		} else {
			// Bad note -- add error
			return "Cannot read note: " + token.getTokenText();
		}

		int noteValue = 0;
		double wholeNoteDuration = 0;
		boolean accidental = false;
		boolean triplet = false;
		boolean tremolo = false;
		int octave = -1;
		boolean isRest = false;
		// Process first letter
		switch (tokenStr.charAt(0)) {
		case 'c':
			noteValue = 0;
			break;
		case 'd':
			noteValue = 2;
			break;
		case 'e':
			noteValue = 4;
			break;
		case 'f':
			noteValue = 5;
			break;
		case 'g':
			noteValue = 7;
			break;
		case 'a':
			noteValue = 9;
			break;
		case 'b':
			noteValue = 11;
			break;
		case 'r':
			isRest = true;
			break;
		}
		// Check for all other parts of a note
		for (int i = 1; i < tokenStr.length(); i++) {
			char c = tokenStr.charAt(i);
			switch (c) {
			case 'b':
				// Flat
				noteValue--;
				accidental = true;
				break;
			case 'n':
				// Natural
				accidental = true;
				break;
			case '#':
				// Sharp
				noteValue++;
				accidental = true;
				break;
			case '*':
				triplet = true;
				break;
			case '!':
				tremolo = true;
				break;
			}

			// Handle octave
			if (Character.isDigit(c)) {
				octave = Integer.parseInt("" + c);
			}

			// Handle duration letters
			String durationLetters = "whqistxo";
			if (durationLetters.indexOf(c) >= 0) {
				double d = 1d / Math.pow(2, (durationLetters.indexOf(c)));
				// Check for dot
				if (i + 1 < tokenStr.length()) {
					if (tokenStr.charAt(i + 1) == '.') {
						// Found; dotted duration
						d *= 1.5;
						// Move loop to after the dot
						i++;
					}
				}
				wholeNoteDuration += d;
			}
		}

		// Handle results of parsing

		if (wholeNoteDuration == 0) {
			// Set a default value
			wholeNoteDuration = 0.25;
		}
		if (triplet) {
			wholeNoteDuration *= 2d / 3d;
		}

		// Adjust for key signature
		if (!accidental) {
			if ((keySig <= -1) && (noteValue % 12) == 11)
				noteValue--;
			if ((keySig <= -2) && (noteValue % 12) == 4)
				noteValue--;
			if ((keySig <= -3) && (noteValue % 12) == 9)
				noteValue--;
			if ((keySig <= -4) && (noteValue % 12) == 2)
				noteValue--;
			if ((keySig <= -5) && (noteValue % 12) == 7)
				noteValue--;
			if ((keySig <= -6) && (noteValue % 12) == 0)
				noteValue--;
			if ((keySig <= -7) && (noteValue % 12) == 5)
				noteValue--;
			if ((keySig >= +1) && (noteValue % 12) == 5)
				noteValue++;
			if ((keySig >= +2) && (noteValue % 12) == 0)
				noteValue++;
			if ((keySig >= +3) && (noteValue % 12) == 7)
				noteValue++;
			if ((keySig >= +4) && (noteValue % 12) == 2)
				noteValue++;
			if ((keySig >= +5) && (noteValue % 12) == 9)
				noteValue++;
			if ((keySig >= +6) && (noteValue % 12) == 4)
				noteValue++;
			if ((keySig >= +7) && (noteValue % 12) == 11)
				noteValue++;
		}

		// Add octave to note's value
		if (octave == -1) {
			// Set a default value
			octave = 4;
		}
		noteValue += (octave + 1) * 12;

		// Adjust for transposition
		noteValue += currTran;

		// Add to song and increment current time
		if (token.getTextPos() >= selectionStart
				&& (token.getTextEndPos() <= selectionEnd)) {
			if (isRest) {
				song.addRest(incrementCurrTime(wholeNoteDuration * 4) / 1000);
			} else {
				if (!tremolo) {
					song.addTone(noteValue,
							incrementCurrTime(wholeNoteDuration * 4) / 1000);
				} else {
					// Tremolo note
					double trembles = wholeNoteDuration * 4d * 8d;
					for (int i = 0; i < trembles; i++) {
						song.addTone(Song.getNoteFreq(noteValue)
								* (1.0 + 0.01 * (i % 2)),
								incrementCurrTime(wholeNoteDuration * 4)
										/ trembles / 1000);
					}
				}
			}
		} else {
			// Just increment current time
			incrementCurrTime(wholeNoteDuration * 4);
		}

		// Return success
		return null;
	}

	private static LinkedList<Token> tokenize(String text) {
		LinkedList<Token> tokens = new LinkedList<Token>();
		Token currToken = new Token("", 0);
		boolean firstCharOfToken = true;
		textCharIterator: for (int i = 0; i < text.length(); i++) {
			char c = text.charAt(i);
			if (Character.isWhitespace(c)) {
				// Whitespace separates tokens
				tokens.add(currToken);
				currToken = new Token("", i + 1);
				firstCharOfToken = true;
			} else {
				if (firstCharOfToken && c == '#') {
					// Inside comment; must skip over characters until the end
					// of the current line

					// Redundant
					// // End current token
					// currToken = null;

					// Scan to end of line; stop scanning at char after end of
					// line character (or chars; thanks Windows!!!)
					boolean newlineCharRead = false;
					while (true) {
						// Scan forwards a character
						i++;
						// If at end of text, stop looping, break outer for loop
						if (i >= text.length()) {
							break textCharIterator;
						}
						c = text.charAt(i);
						// If currently at a \n or \r, keep scanning, but set a
						// flag
						if (c == '\n' || c == '\r') {
							newlineCharRead = true;
						} else if (newlineCharRead) {
							// If not a \n or \r anymore, end of line!
							// We may start reading in regular tokens again
							break;
						}
					}

					// Start new token
					// Backtrack a character so that the regular character
					// scanner starts at the first character
					i--;
					// Leave firstCharOfToken true
					currToken = new Token("", i + 1);
				} else {
					currToken.addChar(c);
					firstCharOfToken = false;
				}
			}
		}
		// Add last token at end of file
		// Could be null due to scanning for comments aborting outer for loop
		if (currToken != null) {
			tokens.add(currToken);
		}

		// Remove empty tokens created by things like /n/r pairs or multiple
		// spaces
		// Not worried about absolute efficiency above, so lets just do cleanup
		// here instead of cluttering our tokenizer loops above
		for (int i = 0; i < tokens.size(); i++) {
			if (tokens.get(i).getTokenText().length() <= 0) {
				tokens.remove(i);
				i--;
			}
		}
		return tokens;
	}

	// Testing driver for tokenizer
	public static void main(String[] args) throws FileNotFoundException {
		// @SuppressWarnings("resource")
		// String in = new Scanner(
		// new File(
		// "C:/Users/BJ/java/jmeggy-slick2d/ToneScribe/tokenizerTest1.txt"))
		// .useDelimiter("\\Z").next();
		// int nlCount = 0;
		// for (char c : in.toCharArray()) {
		// if (c == '\n' || c == '\r') {
		// nlCount++;
		// }
		// }
		// System.out.println(nlCount + " newline characters found.");
		// LinkedList<Token> tokens = tokenize(in);
		// for (Token t : tokens) {
		// System.out.println(t);
		// }

		MusicStringSongCompiler c = new MusicStringSongCompiler();

		String[] tokens = { "ccc", "tran", "tran 10", "tran10", "tran1",
				"tran+10", "tran-10", "tranreset" };
		for (String t : tokens) {
			System.out.println(t + " isValid? "
					+ c.isValidTranToken(new Token(t, 0)));
		}
	}

}

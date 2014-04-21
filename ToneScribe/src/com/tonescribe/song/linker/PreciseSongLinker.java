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
package com.tonescribe.song.linker;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedList;

import com.tonescribe.ToneScribe;
import com.tonescribe.song.RestEvent;
import com.tonescribe.song.Song;
import com.tonescribe.song.SongEvent;
import com.tonescribe.song.ToneEvent;

public class PreciseSongLinker implements SongLinker {

	private static String templateStart, templateEnd;
	static {
		templateStart = loadFile("templateStart.txt");
		templateEnd = loadFile("templateEnd.txt");
	}

	private static String loadFile(String string) {
		StringBuffer sb = new StringBuffer();
		BufferedReader in = new BufferedReader(new InputStreamReader(
				PreciseSongLinker.class.getResourceAsStream(string)));
		try {
			String lineIn = in.readLine();
			while (lineIn != null) {
				sb.append(lineIn).append("\n");
				lineIn = in.readLine();
			}
			in.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return sb.toString();
	}

	@Override
	public String link(Song song) {
		// Prepare beep statements
		HashSet<Integer> freqsUsed = new HashSet<Integer>();
		StringBuilder bodyBuilder = new StringBuilder();
		// Add beep statements
		for (SongEvent e : song.getTones()) {
			if (e.getTimeSeconds() < song.getSelectionStartTimeSec()
					|| e.getTimeSeconds() > song.getSelectionEndTimeSec()) {
				continue;
			}

			if (e instanceof ToneEvent) {
				int freq = (int) ((ToneEvent) e).getPitch();
				freqsUsed.add(freq);
				bodyBuilder.append("\tbeep(").append(freq).append(", ")
						.append((int) (e.getDurationSeconds() * 1000.0))
						.append(");")
						.append(System.getProperty("line.separator"));
			} else if (e instanceof RestEvent) {
				bodyBuilder.append("\t__delay_cycles(")
						.append((int) (e.getDurationSeconds() * 1000000.0))
						.append(");")
						.append(System.getProperty("line.separator"));
			}
		}

		// Create frequency delays switch statement. List with highest
		// frequencies first; these are most sensitive to extra cycles consumed
		// by switch statement
		StringBuilder freqSwitchBuilder = new StringBuilder();
		freqSwitchBuilder.append("switch (freq) {\n");
		LinkedList<Integer> freqsUsedSorted = new LinkedList<Integer>(freqsUsed);
		Collections.sort(freqsUsedSorted, new Comparator<Integer>() {

			@Override
			public int compare(Integer o1, Integer o2) {
				return o2 - o1;
			}
		});
		for (Integer freq : freqsUsedSorted) {
			// Delay for 1M cycles divided by frequency
			// Cut in half again to delay a semiperiod
			int delayCycles = (1000000 / freq) / 2;

			freqSwitchBuilder.append("\t\tcase ").append(freq).append(": \n");
			freqSwitchBuilder.append("\t\t\t__delay_cycles(").append(delayCycles)
					.append(");\n");
			freqSwitchBuilder.append("\t\t\tbreak;\n");
		}
		freqSwitchBuilder.append("\t\t}\n");

		// Prepare beginning of script
		StringBuilder builder = new StringBuilder();
		builder.append(templateStart.replace("<FREQSWITCH>",
				freqSwitchBuilder.toString()).replace("<VERSION>",
				ToneScribe.CURRENT_VERSION));
		// add beeps into script
		builder.append(bodyBuilder);
		// Prepare end of script
		builder.append(templateEnd);
		return builder.toString();
	}

}

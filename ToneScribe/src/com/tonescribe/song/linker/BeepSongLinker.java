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

import com.tonescribe.song.RestEvent;
import com.tonescribe.song.Song;
import com.tonescribe.song.SongEvent;
import com.tonescribe.song.ToneEvent;

/**
 * Converts a compiled song into a snippet of computer code that can be pasted
 * into a program to play the compiled song.
 * 
 */
public class BeepSongLinker implements SongLinker {
	public String link(Song song) {
		StringBuilder builder = new StringBuilder();
		for (SongEvent e : song.getTones()) {
			if (e.getTimeSeconds() < song.getSelectionStartTimeSec()
					|| e.getTimeSeconds() > song.getSelectionEndTimeSec()) {
				continue;
			}

			if (e instanceof ToneEvent) {
				builder.append("beep(")
						.append((int) ((ToneEvent) e).getPitch()).append(", ")
						.append((int) (e.getDurationSeconds() * 1000.0))
						.append(");")
						.append(System.getProperty("line.separator"));
			} else if (e instanceof RestEvent) {
				builder.append("__delay_cycles(")
						.append((int) (e.getDurationSeconds() * 1000000.0))
						.append(");")
						.append(System.getProperty("line.separator"));
			}
		}
		return builder.toString();
	}
}

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
package com.tonescribe.song;

import java.awt.Container;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.LinkedList;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineEvent;
import javax.sound.sampled.LineEvent.Type;
import javax.sound.sampled.LineListener;
import javax.sound.sampled.LineUnavailableException;
import javax.swing.JOptionPane;

import com.tonescribe.ToneScribe;

/**
 * Takes a song and converts it to sound over the speakers and savable sound
 * files.
 * 
 */
public class SongPlayer {
	private static final int SAMPLE_RATE = 44100;

	private static final AudioFormat AUDIO_FORMAT = new AudioFormat(
			AudioFormat.Encoding.PCM_SIGNED, SAMPLE_RATE, 8, 2, 2, SAMPLE_RATE,
			true);

	private boolean lowWave = false;

	private int volume = 127 / 2;

	protected LinkedList<Clip> clips = new LinkedList<Clip>();

	public void playSong(Song song) {
		try {
			final Clip clip = AudioSystem.getClip();
			byte[] songData = generateSong(song);
			clip.open(AUDIO_FORMAT, songData, 0, songData.length);
			clip.start();
			clips.add(clip);
			clip.addLineListener(new LineListener() {

				@Override
				public void update(LineEvent arg0) {
					if (arg0.getType() == Type.STOP) {
						clips.remove(clip);
						clip.close();
					}
				}
			});
		} catch (LineUnavailableException e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(ToneScribe.frame,
					"Cannot play preview.", ToneScribe.frame.getTitle(),
					JOptionPane.ERROR_MESSAGE);
		}

	}

	public void stopSongs() {
		for (int i = 0; i < clips.size(); i++) {
			Clip c = clips.get(i);
			c.stop();
			c.close();
		}
		clips.clear();
	}

	public void saveSong(Song song, File outFile) {
		byte[] songData = generateSong(song);
		AudioInputStream songDataIn = new AudioInputStream(
				new ByteArrayInputStream(songData), AUDIO_FORMAT,
				songData.length);
		try {
			AudioSystem.write(songDataIn, AudioFileFormat.Type.WAVE, outFile);
		} catch (IOException e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(ToneScribe.frame,
					"Could not save preview.", ToneScribe.frame.getTitle(),
					JOptionPane.ERROR_MESSAGE);
		}
	}

	public byte[] generateSong(Song song) {
		int channels = 2;
		// Allocate the length of the song + one second for safety and rounding
		// errors
		byte[] songBuffer = new byte[(int) ((float) (channels * SAMPLE_RATE)
				* song.getCurrLengthSec() * 1.05)
				+ SAMPLE_RATE];

		LinkedList<SongEvent> songEvents = song.getTones();
		for (SongEvent e : songEvents) {
			if (e instanceof ToneEvent) {
				generateTone(
						songBuffer,
						// TODO: Arbitrary delay
						(int) ((double) SAMPLE_RATE * (e.getDurationSeconds())),
						(int) ((double) SAMPLE_RATE * e.getTimeSeconds() * 1.05),
						channels, 1.0 / ((ToneEvent) e).getPitch()
								* (double) SAMPLE_RATE);
			}
		}

		return songBuffer;
	}

	/**
	 * Generates tone; stereo
	 * 
	 * @param samples
	 * @return
	 */
	private void generateTone(byte[] buffer, int samples, int offset,
			int channels, double period) {
		float periodSamplesRemaining = 0;
		// Write samples
		for (int i = 0; i < samples; i++) {
			byte sampleValue = (byte) ((lowWave) ? -volume - 1 : volume);

			periodSamplesRemaining -= 1f;
			if (periodSamplesRemaining < -1) {
				periodSamplesRemaining = -1;
			}

			if (periodSamplesRemaining <= 0) {
				periodSamplesRemaining += period;
				lowWave = !lowWave;
			}

			// Write sample into buffer
			for (int channel = 0; channel < channels; channel++) {
				buffer[offset * channels + i * channels + channel] = sampleValue;
			}
		}
	}
}

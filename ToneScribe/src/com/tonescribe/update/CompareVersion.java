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
package com.tonescribe.update;

import com.tonescribe.ToneScribe;

/**
 * Code from:
 * http://ramakrsna.wordpress.com/2008/07/05/comparing-version-numbers/ <br>
 * A suite of methods for processing basic version numbers. Formats supported:
 * ##.##.##.##, ##.##.##, ##.##, ##<br>
 * <br>
 * No license given; code modified.
 */
public class CompareVersion {

	public final static int LESSER = -1; // versionA is lesser than versionB
	public final static int EQUALS = 0; // versionA equal to versionB
	public final static int GREATER = 1; // versionA is greater then versionB

	public static final int INVALID_ARGS = 0;

	public static int compareVersions(String versionA, String versionB) {
		// Check for valid version numbers
		if (!isVersionNumber(versionA) || !isVersionNumber(versionB)) {
			return INVALID_ARGS;
		}

		try {
			String[] a = versionA.split("\\.");
			String[] b = versionB.split("\\.");
			int i, j;
			int index = 0;
			while ((index < a.length) && (index < b.length)) {
				i = Integer.parseInt(a[index]);
				j = Integer.parseInt(b[index]);
				if (i > j) {
					return CompareVersion.GREATER;
				} else if (i < j) {
					return CompareVersion.LESSER;
				}
				index++;
			}
			if ((index < a.length) && (index == b.length)) {
				return CompareVersion.GREATER;
			} else if ((index == a.length) && (index < b.length)) {
				return CompareVersion.LESSER;
			} else {
				return CompareVersion.EQUALS;
			}
		} catch (NumberFormatException e) {
			// e.printStackTrace();
			return INVALID_ARGS;
		}
	}

	/**
	 * Proper syntax: One to four parts, separated by dots: each part can be
	 * numbers or a *
	 */
	public static boolean isVersionNumber(String part) {
		return part
				.matches("^(?:(\\d+)\\.)?(?:(\\d+)\\.)?(?:(\\d+)\\.)?(\\*|\\d+)$");
	}

	public static boolean isVersionNewerThanCurrent(String version) {
		int result = compareVersions(version, ToneScribe.CURRENT_VERSION);
		if (result == GREATER) {
			return true;
		} else {
			return false;
		}
	}

	public static boolean isVersionOlderThanCurrent(String version) {
		int result = compareVersions(version, ToneScribe.CURRENT_VERSION);
		if (result == LESSER) {
			return true;
		} else {
			return false;
		}
	}
}

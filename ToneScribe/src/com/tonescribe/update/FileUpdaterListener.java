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

/**
 * Most of the time, you'll want the following implementation of this interface
 * (in whatever class is conveneint, or your mod's central class):<br>
 * <br>
 * onUpdaterEvent() {<br>
 * if level is "info" { <br>
 * show on the chat (possibly with [MineTunes] in front.)<br>
 * } else if level is warning or error {<br>
 * same as info, but with a yellow or red color code in front.<br>
 * }}<br>
 * <br>
 * Another option is to write the messages into a gui (preferably the one where
 * the user presses the "update" button)
 * 
 */
public interface FileUpdaterListener {

	/**
	 * Called whenever progress is made or an error is thrown during an update
	 * operation.
	 * 
	 * @param stage
	 * @param event
	 */
	public void onUpdaterEvent(UpdateEventLevel level, String stage,
			String event);
}

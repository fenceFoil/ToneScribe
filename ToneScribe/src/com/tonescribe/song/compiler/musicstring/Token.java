package com.tonescribe.song.compiler.musicstring;

/**
 * Represents a single token of the string to compile, preserving the location
 * of the token within the string given to the compiler.
 */
public class Token {
	private String tokenText;
	private int textPos;

	// boolean isComment;

	public Token(String tokenText, int textPos) {
		super();
		this.tokenText = tokenText;
		this.textPos = textPos;
	}

	public void addChar(char c) {
		tokenText += c;
	}

	public String getTokenText() {
		return tokenText;
	}

	public void setTokenText(String tokenText) {
		this.tokenText = tokenText;
	}

	public int getTextPos() {
		return textPos;
	}

	public void setTextPos(int textPos) {
		this.textPos = textPos;
	}

	/**
	 * 
	 * @return the index after the index of the last character in the token
	 */
	public int getTextEndPos() {
		return textPos + tokenText.length();
	}

	@Override
	public String toString() {
		return "Token [tokenText=" + tokenText + ", textPos=" + textPos + "]";
	}
}

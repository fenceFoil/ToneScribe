package com.tonescribe;

import java.util.prefs.Preferences;

public class ToneScribePreferences {
	private static Preferences prefs = Preferences.userRoot().node("com")
			.node("tonescribe");

	public static String getLastOpened() {
		return prefs.get("lastOpened", null);
	}

	public static void setLastOpened(String filename) {
		if (filename != null) {
			prefs.put("lastOpened", filename);
		}
	}
}

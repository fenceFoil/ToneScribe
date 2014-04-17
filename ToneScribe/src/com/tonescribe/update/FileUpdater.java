/**
 * Copyright (c) 2012-2013 William Karnavas 
 * All Rights Reserved
 * 
 * Modified April 17, 2014 by William Karnavas for ToneScribe
 */
package com.tonescribe.update;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.HashMap;
import java.util.HashSet;
import java.util.InvalidPropertiesFormatException;
import java.util.LinkedList;
import java.util.Properties;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

/**
 * Handles checking a file for updates from the Internet and downloading it. The
 * file's current version is held in a java.util.Properties file at a given URL.
 * The properties file is cached to ensure minimum downloads, ideally just one.
 * This allows operations like getting the latest version and url to be nigh
 * instant after the first call.<br>
 * <br>
 * You can attach a FileUpdaterListener to a FileUpdater, to get some progress
 * messages as a file is updated.<br>
 * <br>
 * The properties file should be laid out like this: If you "fileName" is "mod":<br>
 * mod.latest=0.9.2
 * mod.download=http://dl.dropbox.com/gibberishgibbersih
 * /MineTunes-0_9_2-MC1_4_6.zip
 */
public class FileUpdater {

	private HashSet<FileUpdaterListener> listeners = new HashSet<FileUpdaterListener>();
	private String versionInfoURL;
	private Properties versionInfos = null;
	protected String fileTitle = "";

	/**
	 * If another instance of FileUpdater is created with the same URL, it uses
	 * a cached Properties from this map instead of downloading it again
	 */
	private static HashMap<String, Properties> cachedProperties = new HashMap<String, Properties>();

	/**
	 * Clears the cache of downloaded file update properties, to be re-updated
	 * next check
	 */
	public static void clearStaticCache() {
		cachedProperties.clear();
	}

	/**
	 * Clears this instance's cached properties
	 */
	public void clearCache() {
		versionInfos = null;
	}

	/**
	 * Sets up a FileUpdater
	 * 
	 * @param versionInfoURL
	 *            the url to the java.util.Properties file with update info
	 * @param fileTitle
	 *            the name of the file being updated
	 */
	public FileUpdater(String versionInfoURL, String fileTitle) {
		this.versionInfoURL = versionInfoURL;
		this.fileTitle = fileTitle;
	}

	/**
	 * Returns either versionInfos, or if it is null this downloads versionInfos
	 * from the internet. Instant after first call under most circumstances.
	 * (failure to download Properties may result in download each call?) You
	 * should only be calling this in a thread, so no problem, though.
	 * 
	 * @return
	 */
	protected Properties getVersionInfos() {
		if (versionInfos != null) {
			// If version info already found, return it
			return versionInfos;
		} else {
			// If version info need to be found, find it wherever it may be

			// Try to get a cached version first
			Properties cached = cachedProperties.get(versionInfoURL);
			if (cached == null) {
				// No cached version yet. Download the properties, and add to
				// the cache
				versionInfos = FileUpdater.downloadProperties(versionInfoURL);
				cachedProperties.put(versionInfoURL, versionInfos);
			} else {
				// Use the cached version
				versionInfos = cached;
			}

			// Wherever it came from, return the properties
			return versionInfos;
		}
	}

	/**
	 * Retrieves a list of ZipEntiries from a given file.
	 * 
	 * @param zipFile
	 * @return a list, or null for failure
	 */
	public LinkedList<ZipEntry> loadZipEntries(File zipFile) {
		LinkedList<ZipEntry> newVersionZipEntries = new LinkedList<ZipEntry>();
		try {
			// Set up to read new version zip
			ZipFile newVersionZipFile = new ZipFile(zipFile);
			ZipInputStream newVersionZipInputStream = new ZipInputStream(
					new FileInputStream(zipFile));
			// Read in a list of entries in the new version's zip file
			while (true) {
				ZipEntry entry = newVersionZipInputStream.getNextEntry();
				if (entry == null) {
					break;
				}
				newVersionZipEntries.add(entry);
			}
			newVersionZipInputStream.close();
			newVersionZipFile.close();
		} catch (ZipException e1) {
			e1.printStackTrace();
			fireFileUpdaterEvent(UpdateEventLevel.ERROR, "Mixing",
					"Could not read zip file. (ZipException)");
			return null;
		} catch (IOException e1) {
			e1.printStackTrace();
			fireFileUpdaterEvent(UpdateEventLevel.ERROR, "Mixing",
					"Could not read zip file. (IOException)");
			return null;
		}
		return newVersionZipEntries;
	}

	/**
	 * Downloads the latest version of the file for Minecraft version mcVersion
	 * into destFile
	 * 
	 * @param destFile
	 * @param mcVersion
	 */
	public void downloadToFile(File destFile) {
		fireFileUpdaterEvent(UpdateEventLevel.INFO, "Download", "Downloading "
				+ destFile.getName());
		String latestURL = getLatestURL();
		if (latestURL != null) {
			FileUpdater.downloadFile(latestURL, destFile.getPath());
			fireFileUpdaterEvent(UpdateEventLevel.INFO, "Download",
					"Downloaded successfully.");
		} else {
			fireFileUpdaterEvent(UpdateEventLevel.ERROR, "Download",
					"No URL available for file.");
		}
	}

	/**
	 * Instant given a successful download of the version info before. The
	 * properties key looked at is [fileName].latest
	 * 
	 * @return If successful, the version from the file. If not found or not
	 *         downloaded, null.
	 */
	public String getLatestVersion() {
		// Get the mod version for this version of Minecraft
		return getVersionInfos().getProperty(fileTitle + ".latest");
	}

	/**
	 * Instant given a successful download of the version info before. The
	 * properties key looked at is [fileName].download
	 * 
	 * @return If successful, the url to this file. If not found or not
	 *         downloaded, null.
	 */
	public String getLatestURL() {
		return getVersionInfos().getProperty(fileTitle + ".download");
	}

	public void addFileUpdaterListener(FileUpdaterListener l) {
		listeners.add(l);
	}

	public void removeFileUpdaterListener(FileUpdaterListener l) {
		listeners.remove(l);
	}

	public void removeAllFileUpdaterListeners() {
		listeners.clear();
	}

	protected void fireFileUpdaterEvent(UpdateEventLevel level, String stage,
			String event) {
		for (FileUpdaterListener l : listeners) {
			l.onUpdaterEvent(level, stage, event);
		}
	}

	/**
	 * Genearl method to download a Propeties file from a URL.
	 * 
	 * @param url
	 * @return null if failed
	 */
	public static Properties downloadProperties(String url) {
		InputStream propsIn = null;
		try {
			URL propsURL = new URL(url);
			propsIn = propsURL.openStream();
		} catch (MalformedURLException e) {
			e.printStackTrace();
			return null;
		} catch (IOException e) {
			// Could not open props file
			e.printStackTrace();
			return null;
		}

		// Read a properties file from the internet stream
		Properties prop = new Properties();
		try {
			prop.load(propsIn);
			propsIn.close();
		} catch (InvalidPropertiesFormatException e) {
			e.printStackTrace();
			try {
				propsIn.close();
			} catch (IOException e1) {
			}
			return null;
		} catch (IOException e) {
			e.printStackTrace();
			try {
				propsIn.close();
			} catch (IOException e1) {
			}
			return null;
		}
		return prop;
	}

	/**
	 * General method to download a file from a url
	 * 
	 * @param url
	 * @param destFilename
	 * @return
	 */
	public static File downloadFile(String url, String destFilename) {
		// Download file
		try {
			URL versionDownloadURL = new URL(url);
			File destFile = new File(destFilename);
			ReadableByteChannel downloadByteChannel = Channels
					.newChannel(versionDownloadURL.openStream());
			FileOutputStream newVersionZipFileOutputStream = new FileOutputStream(
					destFile);
			newVersionZipFileOutputStream.getChannel().transferFrom(
					downloadByteChannel, 0, Long.MAX_VALUE);
			downloadByteChannel.close();
			newVersionZipFileOutputStream.close();
			return destFile;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

}

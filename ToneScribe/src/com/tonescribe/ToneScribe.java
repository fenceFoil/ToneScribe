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

package com.tonescribe;

import java.awt.BorderLayout;
import java.awt.Desktop;
import java.awt.EventQueue;
import java.awt.FileDialog;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

import com.tonescribe.song.Song;
import com.tonescribe.song.SongPlayer;
import com.tonescribe.song.compiler.RTTTLSongCompiler;
import com.tonescribe.song.compiler.SongCompiler;
import com.tonescribe.song.compiler.musicstring.MusicStringSongCompiler;
import com.tonescribe.song.linker.BeepSongLinker;
import com.tonescribe.song.linker.GenericSongLinker;
import com.tonescribe.song.linker.PreciseSongLinker;
import com.tonescribe.song.linker.SongLinker;
import com.tonescribe.song.linker.TabTableLinker;
import com.tonescribe.update.CompareVersion;
import com.tonescribe.update.FileUpdater;

@SuppressWarnings("serial")
public class ToneScribe extends JFrame implements ClipboardOwner {

	public static String CURRENT_VERSION = "1.0.0";
	public static String UPDATE_INFO_URL = "https://dl.dropbox.com/s/7lhpka9t1inz8qb/currentVersion.txt";
	public static String WEBSITE_URL = "http://tonescribe.weebly.com/";
	public static String OUTPUT_WEBSITE_URL = "http://tonescribe.weebly.com/output.html";

	private JPanel contentPane;
	private Thread updateOutputThread;
	private JTextArea editorTextArea;
	private JTextArea outputTextArea;
	protected SongCompiler songCompiler = new MusicStringSongCompiler();
	protected SongLinker songLinker = new GenericSongLinker();
	protected SongPlayer songPlayer = new SongPlayer();
	protected File openFile;
	public static ToneScribe frame;
	private JMenu mnInsert;
	private JRadioButtonMenuItem rdbtnmntmRtttl;
	private JRadioButtonMenuItem rdbtnmntmTonescribe;
	protected static boolean openFileChanged = false;
	private static FileUpdater updater = new FileUpdater(UPDATE_INFO_URL,
			"toneScribe");

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					frame = new ToneScribe();
					frame.setVisible(true);

					if (ToneScribePreferences.getLastOpened() != null) {
						openFileChanged = false;
						frame.openFile(new File(ToneScribePreferences
								.getLastOpened()));
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the frame.
	 */
	public ToneScribe() {
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent arg0) {
				askForSave();
			}
		});
		setIconImage(Toolkit.getDefaultToolkit().getImage(
				ToneScribe.class.getResource("/com/tonescribe/music.png")));
		setTitle("ToneScribe");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 827, 516);

		menuBar = new JMenuBar();
		setJMenuBar(menuBar);

		JMenu mnFile = new JMenu("File");
		menuBar.add(mnFile);

		JMenuItem mntmNew = new JMenuItem("New");
		mnFile.add(mntmNew);
		mntmNew.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				resetOpenFile();
			}
		});

		JMenuItem mntmOpen = new JMenuItem("Open");
		mntmOpen.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O,
				InputEvent.CTRL_MASK));
		mnFile.add(mntmOpen);
		mntmOpen.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				FileDialog fd = new FileDialog(frame, "Open Song",
						FileDialog.LOAD);
				if (openFile != null) {
					fd.setDirectory(openFile.getParent());
				}
				fd.show();
				String fileSelected = fd.getFile();
				if (fileSelected != null) {
					File selectedFile = new File(fd.getDirectory()
							+ fileSelected);
					openFile(selectedFile);
				}

				updateWindowTitle();
			}
		});

		JMenuItem mntmSave = new JMenuItem("Save");
		mntmSave.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S,
				InputEvent.CTRL_MASK));
		mnFile.add(mntmSave);
		mntmSave.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				save();
			}
		});

		JMenuItem mntmSaveAs = new JMenuItem("Save As...");
		mnFile.add(mntmSaveAs);
		mntmSaveAs.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				saveAs();
			}
		});

		JMenuItem mntmExit = new JMenuItem("Exit");
		mnFile.add(mntmExit);
		mntmExit.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				closeProgram();
			}
		});

		mnInsert = new JMenu("Insert");
		// XXX: menuBar.add(mnInsert);

		JMenuItem mntmNote = new JMenuItem("Note...");
		mntmNote.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N,
				InputEvent.CTRL_MASK));
		mnInsert.add(mntmNote);

		JMenuItem mntmRest = new JMenuItem("Rest...");
		mntmRest.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R,
				InputEvent.CTRL_MASK));
		mnInsert.add(mntmRest);

		JMenuItem mntmTempoChange = new JMenuItem("Tempo Change...");
		mntmTempoChange.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_T,
				InputEvent.CTRL_MASK));
		mnInsert.add(mntmTempoChange);

		JMenuItem mntmKeySignature = new JMenuItem("Key Signature...");
		mntmKeySignature.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_K,
				InputEvent.CTRL_MASK));
		mnInsert.add(mntmKeySignature);

		JMenu mnPlay = new JMenu("Play");
		menuBar.add(mnPlay);

		JMenuItem mntmPlayPreview = new JMenuItem("Play Selection");
		mntmPlayPreview.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_P,
				InputEvent.CTRL_MASK));
		mnPlay.add(mntmPlayPreview);
		mntmPlayPreview.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				// Get song
				Song song = compileSong();
				if (song != null) {
					// Play!
					songPlayer.playSong(song);
				}
			}
		});

		JMenuItem mntmStopPreview = new JMenuItem("Mute");
		mntmStopPreview.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_M,
				InputEvent.CTRL_MASK));
		mntmStopPreview.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				songPlayer.stopSongs();
			}
		});

		JMenuItem mntmPlayFromCursor = new JMenuItem("Play From Cursor");
		mntmPlayFromCursor.setAccelerator(KeyStroke.getKeyStroke(
				KeyEvent.VK_SPACE, InputEvent.CTRL_MASK));
		mntmPlayFromCursor.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				// Get song
				Song song = compileSongFromCursor();
				if (song != null) {
					// Play!
					songPlayer.playSong(song);
				}
			}
		});
		mnPlay.add(mntmPlayFromCursor);
		mnPlay.add(mntmStopPreview);

		JSeparator separator_2 = new JSeparator();
		mnPlay.add(separator_2);

		JMenuItem mntmSavePreviewAs = new JMenuItem("Save Selection As WAV...");
		mnPlay.add(mntmSavePreviewAs);
		mntmSavePreviewAs.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				FileDialog fd = new FileDialog(frame, "Save Preview",
						FileDialog.SAVE);
				fd.show();
				String fileSelected = fd.getFile();
				if (fileSelected != null) {
					if (!fileSelected.toLowerCase().endsWith(".wav")) {
						fileSelected += ".wav";
					}
					File selectedFile = new File(fd.getDirectory()
							+ fileSelected);
					if (!selectedFile.exists()) {
						try {
							selectedFile.createNewFile();
						} catch (IOException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
					}
					songPlayer.saveSong(compileSong(), selectedFile);
				}
			}
		});

		JMenu mnSongFormat = new JMenu("Mode");
		menuBar.add(mnSongFormat);

		rdbtnmntmTonescribe = new JRadioButtonMenuItem("ToneScribe");
		mnSongFormat.add(rdbtnmntmTonescribe);
		rdbtnmntmTonescribe.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				if (rdbtnmntmTonescribe.isSelected()) {
					changeCompiler(new MusicStringSongCompiler());
				}
			}
		});

		rdbtnmntmRtttl = new JRadioButtonMenuItem("RTTTL");
		mnSongFormat.add(rdbtnmntmRtttl);
		rdbtnmntmRtttl.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				if (rdbtnmntmRtttl.isSelected()) {
					changeCompiler(new RTTTLSongCompiler());
				}
			}
		});
		if (songCompiler instanceof RTTTLSongCompiler) {
			rdbtnmntmRtttl.setSelected(true);
		}
		if (songCompiler instanceof MusicStringSongCompiler) {
			rdbtnmntmTonescribe.setSelected(true);
		}

		ButtonGroup songFormatGroup = new ButtonGroup();
		songFormatGroup.add(rdbtnmntmRtttl);
		songFormatGroup.add(rdbtnmntmTonescribe);

		JMenu mnOutput = new JMenu("Output");
		menuBar.add(mnOutput);

		JRadioButtonMenuItem rdbtnmntmGeneric = new JRadioButtonMenuItem(
				"Generic");
		mnOutput.add(rdbtnmntmGeneric);
		rdbtnmntmGeneric.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				changeLinker(new GenericSongLinker());
			}
		});
		

		JRadioButtonMenuItem rdbtnmntmTabs = new JRadioButtonMenuItem(
				"Tab Separated Values");
		mnOutput.add(rdbtnmntmTabs);
		rdbtnmntmTabs.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				changeLinker(new TabTableLinker());
			}
		});

		JRadioButtonMenuItem rdbtnmntmStandardBeepStatements = new JRadioButtonMenuItem(
				"TI beep() & __delay_cycles()");
		mnOutput.add(rdbtnmntmStandardBeepStatements);
		rdbtnmntmStandardBeepStatements.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				changeLinker(new BeepSongLinker());
			}
		});

		JRadioButtonMenuItem rdbtnmntmPreciseTones = new JRadioButtonMenuItem(
				"TI Complete Code");
		mnOutput.add(rdbtnmntmPreciseTones);
		rdbtnmntmPreciseTones.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				changeLinker(new PreciseSongLinker());
			}
		});

		if (songLinker instanceof GenericSongLinker) {
			rdbtnmntmGeneric.setSelected(true);
		}
		if (songLinker instanceof BeepSongLinker) {
			rdbtnmntmStandardBeepStatements.setSelected(true);
		}
		if (songLinker instanceof PreciseSongLinker) {
			rdbtnmntmPreciseTones.setSelected(true);
		}

		ButtonGroup songLinkerGroup = new ButtonGroup();
		songLinkerGroup.add(rdbtnmntmGeneric);
		songLinkerGroup.add(rdbtnmntmStandardBeepStatements);
		songLinkerGroup.add(rdbtnmntmPreciseTones);
		songLinkerGroup.add(rdbtnmntmTabs);

		JSeparator separator_3 = new JSeparator();
		mnOutput.add(separator_3);

		JMenuItem mntmMoreInfo = new JMenuItem("More Info...");
		mntmMoreInfo.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if (Desktop.isDesktopSupported()) {
					try {
						Desktop.getDesktop()
								.browse(new URI(OUTPUT_WEBSITE_URL));
					} catch (IOException e) {
						e.printStackTrace();
					} catch (URISyntaxException e) {
						e.printStackTrace();
					}
				}
			}
		});
		mnOutput.add(mntmMoreInfo);

		JMenu mnView = new JMenu("View");
		menuBar.add(mnView);

		final JCheckBoxMenuItem chckbxmntmWrapLines = new JCheckBoxMenuItem(
				"Wrap Lines");
		mnView.add(chckbxmntmWrapLines);
		chckbxmntmWrapLines.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				if (chckbxmntmWrapLines.getSelectedObjects() == null) {
					// Checkbox not selected
					editorTextArea.setLineWrap(false);
				} else {
					// Checkbox selectedd
					editorTextArea.setLineWrap(true);
				}
			}
		});

		JMenu mnHelp = new JMenu("Help");
		menuBar.add(mnHelp);

		JMenuItem mntmShowCheatSheet = new JMenuItem("RTTTL Cheat Sheet");
		mnHelp.add(mntmShowCheatSheet);
		final JFrame thisFrame = this;
		mntmShowCheatSheet.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				JDialog dialog = new JDialog(thisFrame);
				dialog.getContentPane().add(
						new JLabel(new ImageIcon(ToneScribe.class
								.getResource("RTTTLCheatSheet493p.jpg"))));
				dialog.setVisible(true);
				dialog.pack();
				dialog.setResizable(false);
				dialog.setLocationRelativeTo(null);
				dialog.setTitle("RTTTL Cheat Sheet");
				dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
			}
		});

		JMenuItem mntmCheckForUpdates = new JMenuItem("Check for Updates");
		mntmCheckForUpdates.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				Thread t = new Thread(new Runnable() {

					@Override
					public void run() {
						checkForUpdates(false);
					}
				});
				t.start();
			}
		});

		JSeparator separator = new JSeparator();
		mnHelp.add(separator);

		JMenuItem mntmVisitWebsite = new JMenuItem("Visit Website");
		mntmVisitWebsite.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if (Desktop.isDesktopSupported()) {
					try {
						Desktop.getDesktop().browse(new URI(WEBSITE_URL));
					} catch (IOException e) {
						e.printStackTrace();
					} catch (URISyntaxException e) {
						e.printStackTrace();
					}
				}
			}
		});
		mnHelp.add(mntmVisitWebsite);
		mnHelp.add(mntmCheckForUpdates);

		JSeparator separator_1 = new JSeparator();
		mnHelp.add(separator_1);

		JMenuItem mntmAbout = new JMenuItem("About ToneScribe "
				+ CURRENT_VERSION);
		mnHelp.add(mntmAbout);
		mntmAbout.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				showAboutBox();
			}
		});

		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		contentPane.setLayout(new BorderLayout(0, 0));
		setContentPane(contentPane);

		JPanel mainPanel = new JPanel();
		contentPane.add(mainPanel, BorderLayout.CENTER);

		JSplitPane splitPane = new JSplitPane();
		splitPane.setResizeWeight(0.5);

		JScrollPane editorScrollPane = new JScrollPane();
		splitPane.setLeftComponent(editorScrollPane);

		editorTextArea = new JTextArea();
		editorTextArea.setFont(new Font("Monospaced", Font.PLAIN, 13));
		editorTextArea.setLineWrap(true);
		// Set up the line wrap menu item
		chckbxmntmWrapLines.setSelected(editorTextArea.getLineWrap());

		editorTextArea.setWrapStyleWord(true);
		editorScrollPane.setViewportView(editorTextArea);

		GroupLayout gl_mainPanel = new GroupLayout(mainPanel);
		gl_mainPanel.setHorizontalGroup(gl_mainPanel.createParallelGroup(
				Alignment.LEADING).addGroup(
				gl_mainPanel.createSequentialGroup().addContainerGap()
						.addComponent(splitPane).addContainerGap()));
		gl_mainPanel.setVerticalGroup(gl_mainPanel.createParallelGroup(
				Alignment.LEADING).addGroup(
				gl_mainPanel.createSequentialGroup().addContainerGap()
						.addComponent(splitPane).addContainerGap()));

		JPanel outputPanel = new JPanel();
		splitPane.setRightComponent(outputPanel);
		outputPanel.setLayout(new BorderLayout(0, 0));

		JScrollPane outputScrollPane = new JScrollPane();

		outputTextArea = new JTextArea();
		outputTextArea.setTabSize(3);
		outputTextArea.setWrapStyleWord(true);
		outputTextArea.setFont(new Font("Monospaced", Font.PLAIN, 13));
		outputTextArea.setEditable(false);
		outputScrollPane.setViewportView(outputTextArea);
		outputPanel.add(outputScrollPane);

		JPanel panel = new JPanel();
		outputPanel.add(panel, BorderLayout.SOUTH);
		panel.setLayout(new BorderLayout(0, 0));

		JButton copyButton = new JButton("Copy to Clipboard");
		panel.add(copyButton);

		statusBarLabel = new JLabel("ToneScribe started");
		panel.add(statusBarLabel, BorderLayout.SOUTH);
		statusBarLabel.setVerticalAlignment(SwingConstants.BOTTOM);
		statusBarLabel.setHorizontalAlignment(SwingConstants.RIGHT);
		statusBarLabel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

		copyButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				putTextInClipboard(outputTextArea.getText());
			}
		});
		mainPanel.setLayout(gl_mainPanel);

		updateOutputThread = new Thread(new Runnable() {

			String lastSong = "";
			SongCompiler lastCompiler = null;
			SongLinker lastSongLinker = null;

			@Override
			public void run() {
				// Call the "update compiler" method once to disable menu items
				// etc. for current compiler at start of program (compiler
				// unchanged)
				changeCompiler(songCompiler);

				while (true) {
					try {
						Thread.sleep(50);
					} catch (InterruptedException e) {
						// When interrupted, stop checking and exit
						break;
					}

					String currentEditorText = editorTextArea.getSelectedText();
					if (currentEditorText == null) {
						currentEditorText = editorTextArea.getText();
					}
					if (!currentEditorText.equals(lastSong)
							|| songCompiler != lastCompiler
							|| songLinker != lastSongLinker) {

						Song compiledSong = compileSong();
						if (compiledSong != null
								&& compiledSong.getErrors().size() <= 0) {
							outputTextArea.setSelectionStart(0);
							outputTextArea.setSelectionEnd(0);
							outputTextArea.setText(songLinker
									.link(compiledSong));
						} else if (compiledSong != null) {
							outputTextArea.setSelectionStart(0);
							outputTextArea.setSelectionEnd(0);
							outputTextArea.setText(compiledSong.getErrors()
									.getFirst());
						} else {
							outputTextArea.setSelectionStart(0);
							outputTextArea.setSelectionEnd(0);
							outputTextArea.setText("(Could not read tune)");
							// TODO: Show errors
						}

						lastSong = currentEditorText;
						lastCompiler = songCompiler;
						lastSongLinker = songLinker;

						openFileChanged = true;
						updateWindowTitle();

						// Delay between compilations
						try {
							Thread.sleep(500);
						} catch (InterruptedException e) {
							// When interrupted, stop checking and exit
							break;
						}
					}

				}
			}

		});
		updateOutputThread.start();

		// Check for updates thread
		Thread checkForUpdatesThread = new Thread(new Runnable() {

			@Override
			public void run() {
				checkForUpdates(true);
			}
		});
		checkForUpdatesThread.start();
	}

	public void putTextInClipboard(String text) {
		StringSelection stringSelection = new StringSelection(text);
		Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
		clipboard.setContents(stringSelection, this);
	}

	protected void showAboutBox() {
		AboutBox b = new AboutBox();
		b.setVisible(true);
	}

	protected void changeCompiler(SongCompiler comp) {
		// Update "Add" menu, which only works with MusicString songs
		if (comp instanceof MusicStringSongCompiler) {
			mnInsert.setEnabled(true);
		} else {
			mnInsert.setEnabled(false);
		}

		songCompiler = comp;
	}

	protected void changeLinker(SongLinker linker) {
		songLinker = linker;
	}

	protected void closeProgram() {
		askForSave();

		updateOutputThread.interrupt();
		frame.dispose();
	}

	protected void saveAs() {
		openFile = null;
		updateWindowTitle();
		save();
	}

	private static boolean updateAvailableMenuAdded = false;
	private static JMenuBar menuBar;
	private static JLabel statusBarLabel = new JLabel();// To ensure that this
														// is never null

	// Run in a separate thread -- will block for downloading!!!
	protected static void checkForUpdates(boolean quiet) {
		String latestVer = updater.getLatestVersion();
		if (latestVer != null) {
			if (CompareVersion.isVersionNewerThanCurrent(latestVer)) {
				// TODO: New version available!
				// Add menu bar item
				if (!updateAvailableMenuAdded) {
					addUpdatesMenuItem();
					updateAvailableMenuAdded = true;
				}

				if (!quiet) {
					statusBarLabel.setText("New version available: "
							+ latestVer);
				}
			} else {
				// Up to date.
				if (!quiet) {
					JOptionPane.showMessageDialog(frame,
							"Up to date; latest version is " + latestVer,
							"Check for Updates",
							JOptionPane.INFORMATION_MESSAGE);
				}
			}

		} else {
			// Could not get version!
			if (!quiet) {
				JOptionPane.showMessageDialog(frame,
						"Could not check for new versions.",
						"Check for Updates", JOptionPane.INFORMATION_MESSAGE);
			}
		}
	}

	private static void addUpdatesMenuItem() {
		if (menuBar == null) {
			return;
		}

		JMenu menu = new JMenu("Get Update");

		JMenuItem mntmVisitWebsite = new JMenuItem("Visit Website");
		mntmVisitWebsite.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if (Desktop.isDesktopSupported()) {
					try {
						Desktop.getDesktop().browse(new URI(WEBSITE_URL));
					} catch (IOException e) {
						e.printStackTrace();
					} catch (URISyntaxException e) {
						e.printStackTrace();
					}
				}
			}
		});
		menu.add(mntmVisitWebsite);

		JSeparator sep = new JSeparator();
		menu.add(sep);

		JMenuItem newVersionMenuItem = new JMenuItem("New Version: "
				+ updater.getLatestVersion());
		newVersionMenuItem.setEnabled(false);
		menu.add(newVersionMenuItem);
		JMenuItem newVersionMenuItem2 = new JMenuItem("Current Version: "
				+ CURRENT_VERSION);
		newVersionMenuItem2.setEnabled(false);
		menu.add(newVersionMenuItem2);

		JSeparator menuSep = new JSeparator();
		menuSep.setForeground(menuBar.getBackground());
		menuBar.add(menuSep);
		menuBar.add(menu);
		menuBar.validate();
	}

	private void askForSave() {
		if (openFileChanged) {
			// Ask user whether they want to save their work first
			int choice = JOptionPane.showConfirmDialog(
					frame,
					"Save changes to "
							+ ((openFile == null) ? "current song" : openFile
									.getName()) + "?", "Save Changes",
					JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
			if (choice == JOptionPane.YES_OPTION) {
				save();
			}
		}
	}

	protected void save() {
		boolean errors = false;

		if (openFile == null) {
			// Ask for file
			FileDialog fd = new FileDialog(frame, "Save Song", FileDialog.SAVE);
			fd.show();
			String fileSelected = fd.getFile();
			if (fileSelected != null) {
				if (!fileSelected.toLowerCase().endsWith(".txt")) {
					fileSelected += ".txt";
				}
				File selectedFile = new File(fd.getDirectory() + fileSelected);
				openFile = selectedFile;
			} else {
				return;
			}
		}

		// Save file
		try {
			openFile.createNewFile();
			BufferedWriter out = new BufferedWriter(new FileWriter(openFile));
			String saveText = editorTextArea.getText();
			out.write(saveText.toCharArray());
			out.close();
		} catch (IOException e) {
			errors = true;
			e.printStackTrace();
		}

		if (!errors) {
			statusBarLabel.setText("Saved " + openFile.getName());
			openFileChanged = false;
		} else {
			statusBarLabel.setText("Could note save " + openFile.getName());
		}

		ToneScribePreferences.setLastOpened(openFile.getPath());

		updateWindowTitle();
	}

	protected String getTextFromFile(File inFile) throws IOException {
		StringBuilder sb = new StringBuilder();

		BufferedReader in = new BufferedReader(new FileReader(inFile));
		String lineIn = in.readLine();
		while (lineIn != null) {
			sb.append(lineIn).append(System.getProperty("line.separator"));
			lineIn = in.readLine();
		}
		in.close();

		return sb.toString().replace("\r", "");
	}

	private Song compileSong(boolean fromCursor) {
		if (fromCursor) {
			// Compile from cursor onwards
			return songCompiler.compile(editorTextArea.getText(),
					editorTextArea.getSelectionStart(), editorTextArea
							.getText().length());
		} else if (editorTextArea.getSelectionStart() != editorTextArea
				.getSelectionEnd()) {
			// If selection highlighted
			return songCompiler.compile(editorTextArea.getText(),
					editorTextArea.getSelectionStart(),
					editorTextArea.getSelectionEnd());
		} else {
			// Compile whole song
			return songCompiler.compile(editorTextArea.getText(), 0,
					editorTextArea.getText().length());
		}
	}

	protected Song compileSong() {
		return compileSong(false);
	}

	protected Song compileSongFromCursor() {
		return compileSong(true);
	}

	protected void resetOpenFile() {
		openFile = null;
		editorTextArea.setSelectionStart(0);
		editorTextArea.setSelectionEnd(0);
		editorTextArea.setText("");

		statusBarLabel.setText("");

		ToneScribePreferences.setLastOpened(null);

		updateWindowTitle();
	}

	protected void updateWindowTitle() {
		StringBuilder title = new StringBuilder();
		title.append("ToneScribe");
		if (openFile != null) {
			title.append(" - ").append(openFile.getName());
			if (openFileChanged) {
				title.append(" * ");
			}
		}
		frame.setTitle(title.toString());
	}

	@Override
	public void lostOwnership(Clipboard arg0, Transferable arg1) {
		// Don't care.
	}

	protected void openFile(File selectedFile) {
		askForSave();
		// Open file
		resetOpenFile();
		openFile = selectedFile;
		try {
			editorTextArea.setText(getTextFromFile(openFile));
			statusBarLabel.setText("Opened " + selectedFile.getName());
		} catch (IOException e) {
			// Cannot find file or it moved? Open nothing instead.
			e.printStackTrace();
			resetOpenFile();
			statusBarLabel.setText("Cannot open " + selectedFile.getName());
			return;
		}
		ToneScribePreferences.setLastOpened(openFile.getPath());
		updateWindowTitle();
		editorTextArea.setCaretPosition(0);
	}
}

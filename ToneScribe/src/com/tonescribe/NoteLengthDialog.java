package com.tonescribe;

import java.awt.BorderLayout;
import java.awt.FlowLayout;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.JLabel;

import java.awt.Font;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.event.WindowStateListener;

import javax.swing.SwingConstants;

public class NoteLengthDialog extends JDialog {

	private final JPanel contentPanel = new JPanel();

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		try {
			NoteLengthDialog dialog = new NoteLengthDialog();
			dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
			dialog.setVisible(true);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Create the dialog.
	 */
	public NoteLengthDialog() {
		setTitle("Select Note Length");
		setBounds(100, 100, 196, 549);
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);

		JButton btnAddWholeNote = new JButton("Add Whole Note");

		JButton btnAddHalfNote = new JButton("Add Half Note");

		JButton btnAddQuarterNote = new JButton("Add Quarter Note");

		JButton btnAddEighthNote = new JButton("Add Eighth Note");

		JButton btnAddSixteenthNote = new JButton("Add Sixteenth Note");

		JButton btnAddthNote = new JButton("Add 1/128th Note");

		JButton btnAddthNote_1 = new JButton("Add 1/64th Note");

		JButton btnAddndNote = new JButton("Add 1/32nd Note");

		JLabel lblPreview = new JLabel("Preview");
		lblPreview.setHorizontalAlignment(SwingConstants.CENTER);
		lblPreview.setFont(new Font("Monospaced", Font.BOLD, 14));

		JLabel lblCurrentLengthXx = new JLabel("Current Length: xx");
		lblCurrentLengthXx.setHorizontalAlignment(SwingConstants.CENTER);

		JButton btnClear = new JButton("Clear");
		
		JButton btnMakeTriplet = new JButton("Make Triplet");
		GroupLayout gl_contentPanel = new GroupLayout(contentPanel);
		gl_contentPanel.setHorizontalGroup(
			gl_contentPanel.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_contentPanel.createSequentialGroup()
					.addGroup(gl_contentPanel.createParallelGroup(Alignment.LEADING)
						.addGroup(gl_contentPanel.createSequentialGroup()
							.addContainerGap()
							.addGroup(gl_contentPanel.createParallelGroup(Alignment.LEADING)
								.addComponent(btnAddSixteenthNote, GroupLayout.DEFAULT_SIZE, 146, Short.MAX_VALUE)
								.addComponent(btnAddWholeNote, GroupLayout.DEFAULT_SIZE, 146, Short.MAX_VALUE)
								.addComponent(btnAddHalfNote, GroupLayout.DEFAULT_SIZE, 146, Short.MAX_VALUE)
								.addComponent(btnAddQuarterNote, GroupLayout.DEFAULT_SIZE, 146, Short.MAX_VALUE)
								.addComponent(btnAddEighthNote, GroupLayout.DEFAULT_SIZE, 146, Short.MAX_VALUE)
								.addComponent(btnAddndNote, GroupLayout.DEFAULT_SIZE, 146, Short.MAX_VALUE)
								.addComponent(btnAddthNote_1, GroupLayout.DEFAULT_SIZE, 146, Short.MAX_VALUE)
								.addComponent(btnAddthNote, GroupLayout.DEFAULT_SIZE, 146, Short.MAX_VALUE)
								.addComponent(lblPreview, Alignment.TRAILING, GroupLayout.DEFAULT_SIZE, 146, Short.MAX_VALUE)
								.addComponent(lblCurrentLengthXx, GroupLayout.DEFAULT_SIZE, 146, Short.MAX_VALUE)
								.addGroup(gl_contentPanel.createSequentialGroup()
									.addGap(41)
									.addComponent(btnClear))))
						.addGroup(gl_contentPanel.createSequentialGroup()
							.addGap(34)
							.addComponent(btnMakeTriplet)))
					.addContainerGap())
		);
		gl_contentPanel.setVerticalGroup(
			gl_contentPanel.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_contentPanel.createSequentialGroup()
					.addContainerGap()
					.addComponent(btnAddWholeNote)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(btnAddHalfNote)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(btnAddQuarterNote)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(btnAddEighthNote)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(btnAddSixteenthNote)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(btnAddndNote)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(btnAddthNote_1)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(btnAddthNote)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(btnMakeTriplet)
					.addGap(44)
					.addComponent(lblPreview, GroupLayout.PREFERRED_SIZE, 30, GroupLayout.PREFERRED_SIZE)
					.addPreferredGap(ComponentPlacement.UNRELATED)
					.addComponent(lblCurrentLengthXx)
					.addGap(31)
					.addComponent(btnClear)
					.addContainerGap())
		);
		contentPanel.setLayout(gl_contentPanel);
		{
			JPanel buttonPane = new JPanel();
			buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
			getContentPane().add(buttonPane, BorderLayout.SOUTH);
			{
				JButton okButton = new JButton("OK");
				okButton.setActionCommand("OK");
				buttonPane.add(okButton);
				getRootPane().setDefaultButton(okButton);
			}
			{
				JButton cancelButton = new JButton("Cancel");
				cancelButton.setActionCommand("Cancel");
				buttonPane.add(cancelButton);
			}
		}

		addWindowListener(new WindowListener() {

			@Override
			public void windowOpened(WindowEvent arg0) {
				// TODO Auto-generated method stub

			}

			@Override
			public void windowIconified(WindowEvent arg0) {
				// TODO Auto-generated method stub

			}

			@Override
			public void windowDeiconified(WindowEvent arg0) {
				// TODO Auto-generated method stub

			}

			@Override
			public void windowDeactivated(WindowEvent arg0) {
				// TODO Auto-generated method stub

			}

			@Override
			public void windowClosing(WindowEvent arg0) {
				dialogClosed = true;
			}

			@Override
			public void windowClosed(WindowEvent arg0) {
				// TODO Auto-generated method stub

			}

			@Override
			public void windowActivated(WindowEvent arg0) {
				// TODO Auto-generated method stub

			}
		});
	}

	private static boolean dialogClosed = false;
	private static double length;

	public static double getNoteLength(double currentLength) {
		dialogClosed = false;
		Thread dialogThread = new Thread(new Runnable() {
			public void run() {
				NoteLengthDialog d = new NoteLengthDialog();
				d.show();
			}
		});
		dialogThread.start();
		while (!dialogClosed) {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return length;
	}
}

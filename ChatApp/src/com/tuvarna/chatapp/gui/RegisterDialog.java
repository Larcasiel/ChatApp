package com.tuvarna.chatapp.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import com.tuvarna.chatapp.client.ChatClient;

public class RegisterDialog extends JDialog {

	private static final long serialVersionUID = 1L;

	private final JLabel lblUsername = new JLabel("Username");
	private final JLabel lblPassword = new JLabel("Password");

	private final JTextField txtFieldUsername = new JTextField(15);
	private final JPasswordField fieldPassword = new JPasswordField();

	private final JButton btnRegister = new JButton("Register");
	private final JButton btnCancel = new JButton("Cancel");

	private final JLabel lblStatus = new JLabel(" ");

	private ChatClient cl = null;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		try {
			RegisterDialog dialog = new RegisterDialog();
			dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
			dialog.setVisible(true);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Create the dialog.
	 */
	public RegisterDialog(final JFrame parent, boolean modal, ChatClient client) {
		super(parent, modal);
		
		cl = client;

		setTitle("Register");

		JPanel p3 = new JPanel(new GridLayout(2, 1));
		p3.add(lblUsername);
		p3.add(lblPassword);

		JPanel p4 = new JPanel(new GridLayout(2, 1));
		p4.add(txtFieldUsername);
		p4.add(fieldPassword);

		JPanel p1 = new JPanel();
		p1.add(p3);
		p1.add(p4);

		JPanel p2 = new JPanel();
		p2.add(btnRegister);
		p2.add(btnCancel);

		JPanel p5 = new JPanel(new BorderLayout());
		p5.add(p2, BorderLayout.CENTER);
		p5.add(lblStatus, BorderLayout.NORTH);
		lblStatus.setForeground(Color.RED);
		lblStatus.setHorizontalAlignment(SwingConstants.CENTER);

		getContentPane().setLayout(new BorderLayout());
		getContentPane().add(p1, BorderLayout.CENTER);
		getContentPane().add(p5, BorderLayout.SOUTH);
		pack();
		setLocationRelativeTo(null);

		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				setVisible(false);
				dispose();
			}
		});
		
		btnRegister.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				if (txtFieldUsername.getText().trim().equals("") || new String(fieldPassword.getPassword()).equals("")) {
					lblStatus.setText("Please fill in all information.");
				} else {
					lblStatus.setText("Processing request, please wait...");
					
					(new Thread() {
						public void run() {
							btnRegister.setEnabled(false);
							
							String username = txtFieldUsername.getText().trim();
							
							boolean registered = register(username, new String(fieldPassword.getPassword()));

							btnRegister.setEnabled(true);
							
							if (registered) {
								setVisible(false);
								dispose();
								
								LoginDialog loginDialog = new LoginDialog(parent, true, cl);
								loginDialog.setUsername(username);
								loginDialog.setPassword(new String(fieldPassword.getPassword()));
								loginDialog.setVisible(true);
							}
						}
					}).start();
				}
			}

		});

		btnCancel.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				setVisible(false);
				dispose();
			}
		});

		txtFieldUsername.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				if (txtFieldUsername.getText().trim().equals("") || new String(fieldPassword.getPassword()).equals("")) {
					lblStatus.setText("Please fill in all information.");
				} else {
					btnRegister.doClick();
				}
			}
		});

		fieldPassword.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				if (txtFieldUsername.getText().trim().equals("") || new String(fieldPassword.getPassword()).equals("")) {
					lblStatus.setText("Please fill in all information.");
				} else {
					btnRegister.doClick();
				}
			}
		});
	}

	public RegisterDialog() {
		this(null, true, null);
	}
	
	private boolean register(String username, String password) {
		boolean result = false;

		cl.setLblRegisterStatus(lblStatus);
		result = cl.connect(username, password, false);

		return result;
	}

}

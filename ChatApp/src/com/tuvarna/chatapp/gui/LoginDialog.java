package com.tuvarna.chatapp.gui;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

import com.tuvarna.chatapp.client.ChatClient;

public class LoginDialog extends JDialog {
	private static final long serialVersionUID = 1L;

	private final JLabel lblUsername = new JLabel("Username");
	private final JLabel lblPassword = new JLabel("Password");

	private final JTextField txtFieldUsername = new JTextField(15);
	private final JPasswordField fieldPassword = new JPasswordField();

	private final JButton btnLogin = new JButton("Login");
	private final JButton btnCancel = new JButton("Cancel");

	private final JLabel lblStatus = new JLabel(" ");
	private final JButton btnRegister = new JButton("Register");

	private ChatClient cl = null;

	public LoginDialog() {
		this(null, true, null);
	}
	
	public void setUsername(String username) {
		txtFieldUsername.setText(username);
	}

	public void setPassword(String password) {
		fieldPassword.setText(password);
	}
	
	public static void main(String[] args) {
		try {
			LoginDialog dialog = new LoginDialog();
			dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
			dialog.setVisible(true);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public LoginDialog(final JFrame parent, boolean modal, ChatClient client) {
		super(parent, modal);

		cl = client;

		setTitle("Login");

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
		p2.add(btnLogin);
		btnRegister.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				RegisterDialog registerDialog = new RegisterDialog(parent, true, cl);
				registerDialog.setVisible(true);

				setVisible(false);
				dispose();
			}
		});
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

		btnLogin.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				lblStatus.setText("Processing request, please wait...");

				(new Thread() {
					public void run() {
						btnLogin.setEnabled(false);
						btnRegister.setEnabled(false);
						
						boolean login = logIn(txtFieldUsername.getText(), new String(fieldPassword.getPassword()));
						
						btnLogin.setEnabled(true);
						btnRegister.setEnabled(true);
						
						if (login) {
							parent.setVisible(true);
							setVisible(false);
							dispose();
						}
					}
				}).start();

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
				btnLogin.doClick();
			}
		});
	}

	private boolean logIn(String username, String password) {
		boolean result = false;

		cl.setLblLoginStatus(lblStatus);
		result = cl.connect(username, password, true);

		return result;
	}

}

package com.tuvarna.chatapp.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;

public class ChatClient {
	public static final int SERVER_PORT = 4321;

	// �����: ������������ ����������:
	// private static final String SERVER_HOSTNAME = "localhost";

	private Socket m_Socket = null;
	private BufferedReader in = null;
	private PrintWriter out = null;
	private GUI m_gui;
	private GUI2 m_gui2;

	// �����: ���� ����� Socket m_Socket, ���� ����� �� ��� ����:
	// private static Socket socket;

	public ChatClient(GUI gui) {
		this.m_gui = gui;
	}

	public ChatClient(GUI2 gui2) {
		this.setM_gui2(gui2);
	}

	public PrintWriter getOutput() {
		return out;
	}

	// �����: ��������, ���� �� �� ������ main �� �������. ���������� ���
	// ������� ����� � connect().

	// public static void main(String[] args, BufferedReader mSocketReader,
	// PrintWriter mSocketWriter) {
	//
	// // Connect to the chat server
	//
	// try {
	//
	// socket = new Socket(SERVER_HOSTNAME, SERVER_PORT);
	// mSocketReader = new BufferedReader(new
	//
	// InputStreamReader(socket.getInputStream()));
	//
	// mSocketWriter = new PrintWriter(new
	//
	// OutputStreamWriter(socket.getOutputStream()));
	//
	// System.out.println("Connected to server " +
	//
	// SERVER_HOSTNAME + ":" + SERVER_PORT);
	//
	// } catch (IOException ioe) {
	//
	// System.err.println("Can not connect to " +
	//
	// SERVER_HOSTNAME + ":" + SERVER_PORT);
	//
	// ioe.printStackTrace();
	//
	// System.exit(-1);
	// }
	// }

	public BufferedReader getInput() {
		return in;
	}

	public boolean connect() {
		boolean successfull = true;

		// �����: �� �������� �������, ������ ������� set-���� serverHost ��
		// "localhost":
		String serverHost = "localhost"; // (m_gui.getCodeBase()).getHost();

		// �����: ��� ������ ����� ���������, �������� � ������. ��� ���� ����
		// �� �������� ������,
		// �� ������������ ������� ������:
		// if (serverHost.length() == 0) {
		// m_gui.addSystemMessage("Warning: Applet is loaded from a local
		// file,");
		// m_gui.addSystemMessage("not from a web server. Web browser's
		// security");
		// m_gui.addSystemMessage("policy will probably disable socket
		// connections.");
		// serverHost = "localhost";
		// }

		try {
			m_Socket = new Socket(serverHost, SERVER_PORT);
			in = new BufferedReader(new InputStreamReader(m_Socket.getInputStream()));
			out = new PrintWriter(new OutputStreamWriter(m_Socket.getOutputStream()));
			m_gui.addSystemMessage("Connected to server " + serverHost + ": " + SERVER_PORT);
		} catch (SecurityException se) {
			m_gui.addSystemMessage(
					"Security policy does not allow " + "connection to " + serverHost + ": " + SERVER_PORT);
			successfull = false;
		} catch (IOException e) {
			m_gui.addSystemMessage("Can not establish connection to " + serverHost + ": " + SERVER_PORT);
			successfull = false;
		}

		// Create and start Listener thread
		Listener listener = new Listener(m_gui, in);
		listener.setDaemon(true);
		listener.start();

		return successfull;
	}

	public void disconnect() {
		if (!m_gui.getConnected()) {
			m_gui.addSystemMessage("Can not disconnect. Not connected.");
			return;
		}

		// �����: ��-����� � �� �� ������� ���� ��� m_Socket.close():
		// m_gui.setConnected(false);

		try {
			m_Socket.close();

			// �����: ������ ����� �� ��������, �� �� �� ������� ������� �
			// ������ �����������:
			m_gui.removeAllUsers();
			m_gui.setConnected(false);
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}

		m_gui.addSystemMessage("Disconnected.");
	}

	public GUI2 getM_gui2() {
		return m_gui2;
	}

	public void setM_gui2(GUI2 m_gui2) {
		this.m_gui2 = m_gui2;
	}

	class Listener extends Thread {
		private BufferedReader mIn;
		private GUI mCA;

		public Listener(GUI m_gui, BufferedReader aIn) {
			mCA = m_gui;
			mIn = aIn;
		}

		public void run() {
			try {
				while (!isInterrupted()) {
					String message = mIn.readLine();

					// �����: ������� ��������������, ����� �� ������ ���������
					// �� ������� �� ���������� �� ������� � �������:
					if (message.startsWith("####")) {
						message = message.substring(5);

						String[] users = message.split("%");

						mCA.removeAllUsers();

						for (int i = 0; i < users.length; i++) {
							mCA.addUser(users[i]);
						}
					} else {
						// �����: ������ ":" �� "-".
						int colon2index = message.indexOf("-", message.indexOf("-") + 1);
						String user = message.substring(0, colon2index - 1);
						mCA.addText(message, user);
						// mCA.addUser(user);
					}
				}
			} catch (Exception ioe) {
				if (m_gui.getConnected())
					m_gui.addSystemMessage("Communication error.");
			}
			m_gui.setConnected(false);
		}

	}
}

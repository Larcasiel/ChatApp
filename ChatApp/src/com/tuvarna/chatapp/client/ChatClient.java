package com.tuvarna.chatapp.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.HashMap;

import javax.swing.JLabel;

import com.tuvarna.chatapp.gui.GUI;
import com.tuvarna.chatapp.gui.GUI2;

import flexjson.*;

public class ChatClient {
	public static final int SERVER_PORT = 4321;
	private static final String SERVER_HOSTNAME = "localhost";

	private Socket m_Socket = null;
	private BufferedReader in = null;
	private PrintWriter out = null;
	private GUI m_gui;
	private GUI2 m_gui2;

	// Йоана: Label на LoginDialog и RegisterDialog:
	private JLabel lblLoginStatus;
	private JLabel lblRegisterStatus;

	// Йоана: Вече имаме Socket m_Socket, няма нужда от още един:
	// private static Socket socket;

	// Йоана: Добавям нов флаг: isLoggedIn, както и String username:
	private boolean isLoggedIn = false;
	private String username = "";

	public ChatClient(GUI gui) {
		this.m_gui = gui;
	}

	public ChatClient(GUI2 gui2) {
		this.setM_gui2(gui2);
	}

	public PrintWriter getOutput() {
		return out;
	}

	// Йоана: Всъщност, явно не ни трябва main за клиента. Връзването към
	// сървъра става в connect().

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

	public boolean connect(String username, String password, boolean logIn) {
		boolean result = false;

		// Йоана: Ще използваме SERVER_HOSTNAME вместо тази променлива:
		// String serverHost = (m_gui.getCodeBase()).getHost();

		// Йоана: Тук виждам разни съобщения, свързани с аплети. Тъй като няма
		// да ползваме аплети,
		// ще закоментирам долните редове:
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
			m_Socket = new Socket(SERVER_HOSTNAME, SERVER_PORT);
			in = new BufferedReader(new InputStreamReader(m_Socket.getInputStream()));
			out = new PrintWriter(new OutputStreamWriter(m_Socket.getOutputStream()));

			if (logIn) {
				if (logIn(username, password)) {
					m_gui.addSystemMessage("Connected to server " + SERVER_HOSTNAME + ": " + SERVER_PORT + ".");
					m_gui.setLoggedIn(true);
					isLoggedIn = true;

					this.username = username;

					requestOnlineUsers();

					result = true;
				} else {
					result = false;
				}
			} else {
				result = register(username, password);
			}
		} catch (SecurityException se) {
			m_gui.addSystemMessage(
					"Security policy does not allow " + "connection to " + SERVER_HOSTNAME + ": " + SERVER_PORT);
			result = false;

			lblLoginStatus.setText("Security error.");
		} catch (IOException e) {
			// Йоана: Долната информация ще се появява в Login прозореца:
			// m_gui.addSystemMessage("Can not establish connection to " +
			// serverHost + ": " + SERVER_PORT);
			result = false;

			lblLoginStatus.setText("Server is down. Try again later.");
		}

		if (logIn) {
			// Create and start Listener thread
			Listener listener = new Listener(m_gui, in);
			listener.setDaemon(true);
			listener.start();
		}

		return result;
	}

	public void disconnect() {
		if (!m_gui.getLoggedIn()) {
			m_gui.addSystemMessage("Can not disconnect. Not connected.");
			return;
		}

		// Йоана: По-добре е да се измести това под m_Socket.close():
		// m_gui.setConnected(false);

		try {
			// Йоана: logout logic

			HashMap<String, String> operationMsg = new HashMap<String, String>();

			operationMsg.put("operation", "logOutRequest");
			operationMsg.put("username", username);
			operationMsg.put("ipAddress", m_Socket.getInetAddress().toString());

			JSONSerializer serializer = new JSONSerializer();

			out.println(serializer.serialize(operationMsg).toString());
			out.flush();

			m_Socket.close();

			// Йоана: Когато някой се разлогне, да не се запазва списъка с
			// онлайн потребители:
			m_gui.removeAllUsers();
			m_gui.setLoggedIn(false);
			isLoggedIn = false;
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}

		m_gui.addSystemMessage("Disconnected.");
	}

	// Йоана: Добавям метод за проверка на username и парола:
	private boolean logIn(String username, String password) {
		boolean result = false;

		HashMap<String, String> operationMsg = new HashMap<String, String>();

		operationMsg.put("operation", "logInRequest");
		operationMsg.put("username", username);
		operationMsg.put("password", password);
		operationMsg.put("ipAddress", m_Socket.getInetAddress().toString());
		operationMsg.put("port", "" + m_Socket.getLocalPort());

		JSONSerializer serializer = new JSONSerializer();

		out.println(serializer.serialize(operationMsg).toString());
		out.flush();

		String message = "";

		try {
			while (true) {
				message = in.readLine();

				HashMap<String, String> parsedMessage = new JSONDeserializer<HashMap<String, String>>()
						.deserialize(message);

				String operation = parsedMessage.get("operation");

				if (operation.equals("logInResponse")) {
					if (parsedMessage.get("success").equals("true")) {
						result = true;
					} else {
						lblLoginStatus.setText(parsedMessage.get("statusMessage"));
					}

					break;
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return result;
	}

	public void requestOnlineUsers() {
		HashMap<String, String> operationMsg = new HashMap<String, String>();

		operationMsg.put("operation", "onlineUsersRequest");

		JSONSerializer serializer = new JSONSerializer();

		out.println(serializer.serialize(operationMsg).toString());
		out.flush();
	}

	public boolean register(String username, String password) {
		boolean result = false;

		HashMap<String, String> operationMsg = new HashMap<String, String>();

		operationMsg.put("operation", "registerRequest");
		operationMsg.put("username", username);
		operationMsg.put("password", password);
		operationMsg.put("ipAddress", m_Socket.getInetAddress().toString());
		operationMsg.put("port", "" + m_Socket.getLocalPort());

		JSONSerializer serializer = new JSONSerializer();

		out.println(serializer.serialize(operationMsg).toString());
		out.flush();

		String message = "";

		try {
			while (true) {
				message = in.readLine();

				HashMap<String, String> parsedMessage = new JSONDeserializer<HashMap<String, String>>()
						.deserialize(message);

				String operation = parsedMessage.get("operation");

				if (operation.equals("registerResponse")) {
					if (parsedMessage.get("success").equals("true")) {
						result = true;
					} else {
						lblRegisterStatus.setText(parsedMessage.get("statusMessage"));
					}

					break;
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return result;
	}

	public GUI2 getM_gui2() {
		return m_gui2;
	}

	public void setM_gui2(GUI2 m_gui2) {
		this.m_gui2 = m_gui2;
	}

	public JLabel getLblLoginStatus() {
		return lblLoginStatus;
	}

	public void setLblLoginStatus(JLabel lblLoginStatus) {
		this.lblLoginStatus = lblLoginStatus;
	}

	public JLabel getLblRegisterStatus() {
		return lblRegisterStatus;
	}

	public void setLblRegisterStatus(JLabel lblRegisterStatus) {
		this.lblRegisterStatus = lblRegisterStatus;
	}

	public String getUsername() {
		return this.username;
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
					// Йоана: Преместих част от кода в метод handleMessage:
					handleMessage(message);
				}
			} catch (Exception e) {
				// Йоана: Тествам нещо, ще закоментирам малко неща:
				// e.printStackTrace();
				// if (m_gui.getLoggedIn())
				// m_gui.addSystemMessage("Communication error.");
			}
			m_gui.setLoggedIn(false);
			isLoggedIn = false;
		}

		private void handleMessage(String message) {
			// Йоана: Съобщенията от сървъра ще са опаковани в JSON. Правя
			// промени, които да
			// parse-ват json съобщенията:

			HashMap<String, String> parsedMessage = new JSONDeserializer<HashMap<String, String>>()
					.deserialize(message);

			String operation = parsedMessage.get("operation");

			if (operation.equals("onlineUsersResponse") && isLoggedIn) {

				String[] users = parsedMessage.get("users").split("%");

				mCA.removeAllUsers();

				for (int i = 0; i < users.length; i++) {
					if (!users[i].equals(username)) {
						mCA.addUser(users[i]);
					}
				}
			} else if (operation.equals("receiveMessage")) {
				String text = "[" + parsedMessage.get("time") + "] " + parsedMessage.get("sender") + " says: "
						+ parsedMessage.get("message");
				mCA.addText(text, null);
			}

			// Йоана: Вече няма нужда от долния код, тъй като ще ползваме
			// JSON-и.
			// Йоана: Смених ":" на "-".
			// int colon2index = message.indexOf("-", message.indexOf("-") + 1);
			// String user = message.substring(0, colon2index - 1);
			// mCA.addText(message, user);
			// mCA.addUser(user);
		}
	}
}

package com.tuvarna.chatapp.server;

import java.util.*;

import com.tuvarna.chatapp.general.DatabaseInteraction;

import flexjson.*;

//Funkciqta na klasa e da slusha za suobshteniq polucheni ot daden klient i da gi razprashta do ostanalite svurzani klienti 
public class Dispatcher extends Thread {
	private Vector<String> messages = new Vector<String>();
	private Vector<User> users = new Vector<User>();

	// Йоана: Връзката с базата ще се осъществява от този обект:
	private static DatabaseInteraction db = null;

	// Dobavq novi klienti kum lista na server-a
	public synchronized void addUsers(User mUser) {
		users.add(mUser);

		// Йоана: при добавяне на нов user, се уведомяват всички клиенти,
		// за да могат да ъпдейтнат списъка си с онлайн user-и:
		notifyClientsForUserChange();
		notify();
	}

	// premahva daden klient v spisuka, ako toi e v nego
	public synchronized void deleteUsers(User mUser) {
		int userIndex = users.indexOf(mUser);

		if (userIndex != -1) {
			if (mUser.username != null) {
				db.updateUserStatus(mUser.username, "localhost" + mUser.uSocket.getInetAddress(), 0);
			}
			users.removeElementAt(userIndex);

			// Йоана: при премахване на user, се уведомяват всички клиенти,
			// за да могат да ъпдейтнат списъка си с онлайн user-и:
			notifyClientsForUserChange();
			notify();
		}
	}

	// vzimame vektora s userite
	public synchronized Vector<User> getOnlineUsers() {
		return users;
	}

	// Йоана: Ще опаковаме всички съобщения от сървъра в JSON, правя промени във
	// връзка с това:
	// dobavq dadadeno suobshtenie i uvedomqva nextMessage() che ima suobhstenie
	public synchronized void addMessage(User mUser, String message) {
		// Socket s = mUser.uSocket;
		// String sIP = s.getInetAddress().getHostAddress();
		// String sPort = "" + s.getPort();

		// message = sIP + "-" + sPort + "-" + message;

		// Йоана: Структура на съобщението в JSON формат:
		// HashMap<String, String> operationMsg = new HashMap<String, String>();
		//
		// SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyy HH:mm:ss");
		//
		// Date now = new Date();
		// String date = sdf.format(now);
		//
		// operationMsg.put("operation", "receiveMessage");
		// operationMsg.put("message", message);
		// operationMsg.put("sender", mUser.username);
		// operationMsg.put("time", date);
		//
		// JSONSerializer serializer = new JSONSerializer();
		//
		// messages.add(serializer.serialize(operationMsg).toString());

		messages.add(message);

		notify();
	}

	// Iztriva suobshtenieto ot opashkata i ako nqma sledvashti zaspiva dokato
	// ne bude uvedomen ot addMessage
	public synchronized String nextMessage() throws InterruptedException {
		while (messages.size() == 0) {
			wait();
		}

		String message = (String) messages.get(0);
		messages.removeElementAt(0);

		return message;
	}

	// Izprashta suobshtenieto kum vsichki klienti v spisuka(dobaveno e v
	// opashkata na izprashtacha
	// i nishkata na suotvetniq klient poema natatuk
	public void sendToAll(String message) {
		for (int i = 0; i < users.size(); i++) {
			User user = (User) users.get(i);
			user.uSender.addMessage(message);
		}
	}

	// Izprashta suobshtenie samo po nishtakata na opredelen user
	public void sentToOne(String message, User rUser) {
		rUser.uSender.addMessage(message);
	}

	// Йоана: Всеки път, когато някой user се connect-не или disconnect-не към
	// сървъра,
	// се уведомяват всички клиенти.
	public void notifyClientsForUserChange() {
		HashMap<String, String> operationMsg = new HashMap<String, String>();

		String userList = "";

		System.out.println("Users: " + users.size());

		for (int i = 0; i < users.size(); i++) {
			User user = (User) users.get(i);

			if (user.username != null) {
				userList += user.username + "%";
			}
		}

		operationMsg.put("operation", "onlineUsersResponse");
		operationMsg.put("users", userList);

		JSONSerializer serializer = new JSONSerializer();

		messages.add(serializer.serialize(operationMsg).toString());
	}

	// cikul za chetene i izprashtane na suobshteniqta
	// Йоана: Аналогично на Receiver.java - проемних името на метода от
	// process() на run()
	// public void process() {
	public void run() {
		try {
			while (true) {
				String message = nextMessage();

				// Йоана: В нов метод (handleMessage) добавям малко повече
				// логика относно съобщенията, които получава сървъра.
				// sendToAll(message);
				handleMessage(message);
			}
		} catch (InterruptedException e) {
			System.out.println("Thread interrupted");
		}
	}

	// Йоана: Разширена логика за приемане на съобщения:
	private void handleMessage(String message) {
		HashMap<String, String> parsedMessage = new JSONDeserializer<HashMap<String, String>>().deserialize(message);

		String operation = parsedMessage.get("operation");
		
		JSONSerializer serializer = new JSONSerializer();

		if (operation.equals("logInRequest")) {
			String username = parsedMessage.get("username");
			String password = parsedMessage.get("password");

			String ipAddress = parsedMessage.get("ipAddress");
			String port = parsedMessage.get("port");

			HashMap<String, String> loginOperationMsg = new HashMap<String, String>();

			loginOperationMsg.put("operation", "logInResponse");

			switch (db.logIn(username, password, ipAddress)) {
			case ALREADY_LOGGED:
				loginOperationMsg.put("success", "false");
				loginOperationMsg.put("statusMessage", "Already logged in from this computer.");
				removeDeadUsers(ipAddress, port);
				break;
			case DB_PROBLEMS:
				loginOperationMsg.put("success", "false");
				loginOperationMsg.put("statusMessage", "DB problems, try again later.");
				removeDeadUsers(ipAddress, port);
				break;
			case INVALID_USER_OR_PASS:
				loginOperationMsg.put("success", "false");
				loginOperationMsg.put("statusMessage", "Invalid username or password.");
				removeDeadUsers(ipAddress, port);
				break;
			case LOGIN_SUCCESS:
				loginOperationMsg.put("success", "true");
				loginOperationMsg.put("statusMessage", "Success");

				for (int i = 0; i < users.size(); i++) {
					User user = (User) users.get(i);

					String currentIP = user.uSocket.getInetAddress().toString();
					currentIP = "localhost" + currentIP;
					String currentPort = "" + user.uSocket.getPort();

					if (currentIP.equals(ipAddress) && (currentPort.equals(port))) {
						user.username = username;

						break;
					}
				}

				break;
			default:
				loginOperationMsg.put("success", "false");
				loginOperationMsg.put("statusMessage", "Unknown error.");
				removeDeadUsers(ipAddress, port);
				break;
			}

			addMessage(null, serializer.serialize(loginOperationMsg).toString());
		} else if (operation.equals("logOutRequest")) {
			String username = parsedMessage.get("username");
			String ipAddress = parsedMessage.get("ipAddress");

			db.updateUserStatus(username, ipAddress, 0);
		} else if (operation.equals("registerRequest")) {
			String username = parsedMessage.get("username");
			String password = parsedMessage.get("password");

			String ipAddress = parsedMessage.get("ipAddress");
			String port = parsedMessage.get("port");

			HashMap<String, String> registerOperationMsg = new HashMap<String, String>();

			registerOperationMsg.put("operation", "registerResponse");

			switch (db.register(username, password)) {
			case DB_PROBLEMS:
				registerOperationMsg.put("success", "false");
				registerOperationMsg.put("statusMessage", "DB problems, try again later.");
				removeDeadUsers(ipAddress, port);
				break;
			case REGISTER_SUCCESS:
				registerOperationMsg.put("success", "true");
				registerOperationMsg.put("statusMessage", "Success");
				break;
			case USER_ALREADY_EXISTS:
				registerOperationMsg.put("success", "false");
				registerOperationMsg.put("statusMessage", "User with same username already exists.");
				removeDeadUsers(ipAddress, port);
				break;
			default:
				registerOperationMsg.put("success", "false");
				registerOperationMsg.put("statusMessage", "Unknown error.");
				removeDeadUsers(ipAddress, port);
				break;

			}

			addMessage(null, serializer.serialize(registerOperationMsg).toString());
		} else if (operation.equals("onlineUsersRequest")) {
			notifyClientsForUserChange();
		} else {// if (operation.equals("receiveMessage")) {
			if (operation.equals("sendMessage")) {
				String sender = parsedMessage.get("sender");
				
				if (sender != null) {
					db.logChatMessage(sender, 1, parsedMessage.get("message"));
				}
				
				parsedMessage.replace("operation", "receiveMessage");
			}
			
			sendToAll(serializer.serialize(parsedMessage).toString());
		}
	}

	// Йоана: Setter за db:
	public void setDb(DatabaseInteraction db) {
		Dispatcher.db = db;
	}

	private void removeDeadUsers(String ipAddress, String port) {
		for (int i = 0; i < users.size(); i++) {
			User user = (User) users.get(i);

			String currentIP = user.uSocket.getInetAddress().toString();
			currentIP = "localhost" + currentIP;
			String currentPort = "" + user.uSocket.getPort();

			if (currentIP.equals(ipAddress) && (currentPort.equals(port))) {
				(new Thread() {
					public void run() {
						try {
							Thread.sleep(1000);
						} catch (InterruptedException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}

						users.remove(user);
					}
				}).start();

				break;
			}
		}
	}
}
package com.tuvarna.chatapp.server;

import java.util.*;

import com.tuvarna.chatapp.general.*;

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

		for (int i = 0; i < users.size(); i++) {
			User user = (User) users.get(i);

			userList += user.username + "%";
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

		if (operation.equals("logInRequest")) {
			String username = parsedMessage.get("username");
			String password = parsedMessage.get("password");

			String ipAddress = parsedMessage.get("ipAddress");
			String port = parsedMessage.get("port");
			
			HashMap<String, String> loginOperationMsg = new HashMap<String, String>();
			
			loginOperationMsg.put("operation", "logInResponse");

			if (db.logIn(username, password)) {				
				loginOperationMsg.put("success", "true");

				JSONSerializer serializer = new JSONSerializer();

				addMessage(null, serializer.serialize(loginOperationMsg).toString());

				for (int i = 0; i < users.size(); i++) {
					User user = (User) users.get(i);

					String currentIP = user.uSocket.getInetAddress().toString();
					currentIP = "localhost" + currentIP;
					String currentPort = "" + user.uSocket.getPort();

					if (currentIP.equals(ipAddress) && (currentPort.equals(port))) {
						user.username = username;
					}
				}
			} else {
				loginOperationMsg.put("success", "false");
				
				JSONSerializer serializer = new JSONSerializer();

				addMessage(null, serializer.serialize(loginOperationMsg).toString());
			}
		} else if (operation.equals("onlineUsersRequest")) {
			notifyClientsForUserChange();
		} else { // if (operation.equals("sendMessage")) {
			sendToAll(message);
		}
	}

	// Йоана: Setter за db:
	public void setDb(DatabaseInteraction db) {
		Dispatcher.db = db;
	}
}
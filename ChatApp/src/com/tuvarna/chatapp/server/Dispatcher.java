package com.tuvarna.chatapp.server;

import java.util.*;
import java.net.*;

//Funkciqta na klasa e da slusha za suobshteniq polucheni ot daden klient i da gi razprashta do ostanalite svurzani klienti 
public class Dispatcher extends Thread {
	private Vector<String> messages = new Vector<String>();
	private Vector<User> users = new Vector<User>();

	// Dobavq novi klienti kum lista na server-a
	public synchronized void addUsers(User mUser) {
		users.add(mUser);
		
		//Йоана: при добавяне на нов user, се уведомяват всички клиенти,
		//за да могат да ъпдейтнат списъка си с онлайн user-и:
		notifyClientsForUserChange();
		notify();
	}

	// premahva daden klient v spisuka, ako toi e v nego
	public synchronized void deleteUsers(User mUser) {
		int userIndex = users.indexOf(mUser);

		if (userIndex != -1) {
			users.removeElementAt(userIndex);
			
			//Йоана: при премахване на user, се уведомяват всички клиенти,
			//за да могат да ъпдейтнат списъка си с онлайн user-и:
			notifyClientsForUserChange();
			notify();
		}
	}

	// vzimame vektora s userite
	public synchronized Vector<User> getOnlineUsers() {
		return users;
	}

	// dobavq dadadeno suobshtenie i uvedomqva nextMessage() che ima suobhstenie
	public synchronized void addMessage(User mUser, String message) {
		Socket s = mUser.uSocket;
		String sIP = s.getInetAddress().getHostAddress();
		String sPort = "" + s.getPort();

		message = sIP + "-" + sPort + "-" + message;

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

	//Йоана: Всеки път, когато някой user се connect-не или disconnect-не към сървъра,
	//се уведомяват всички клиенти.
	public void notifyClientsForUserChange(){
		String notificationMsg = "####%";
		
		for (int i = 0; i < users.size(); i++) {
			User user = (User) users.get(i);
			
			notificationMsg += user.username + "%";
		}
		
		messages.add(notificationMsg);
	}
	// cikul za chetene i izprashtane na suobshteniqta
	// Йоана: Аналогично на Receiver.java - проемних името на метода от process() на run()
	// public void process() {
	public void run() {
		try {
			while (true) {
				String message = nextMessage();
				sendToAll(message);
			}
		} catch (InterruptedException e) {
			System.out.println("Thread interrupted");
		}
	}
}
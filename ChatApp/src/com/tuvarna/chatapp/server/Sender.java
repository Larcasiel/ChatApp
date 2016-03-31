package com.tuvarna.chatapp.server;

import java.net.*;
import java.io.*;
import java.util.*;

//Izprashta supbshteniqta do usera,chakashtite suobshteniq se suhranqvat v opashkata
//Kogato tq e prazna toi zaspiva,kogato e budna izprashta suobshteniqta do nishkata na klienta
public class Sender extends Thread {
	private Dispatcher dispatcher;
	private User user;
	private PrintWriter writer;
	private Vector<String> messages = new Vector<String>();

	public Sender(User usr, Dispatcher dsptchr) throws IOException {
		user = usr;
		dispatcher = dsptchr;
		Socket socket = usr.uSocket;
		writer = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()));
	}

	public synchronized void addMessage(String message) {
		messages.add(message);
		notify();
	}

	public synchronized String nextMessage() throws InterruptedException {
		while (messages.size() == 0) {
			wait();
		}

		String message = (String) messages.get(0);

		messages.removeElementAt(0);

		return message;
	}

	private void sendToUser(String message) {
		writer.println(message);
		writer.flush();
	}

	// Йоана: Аналогично на Receiver.java - проемних името на метода от
	// process() на run()
	// public void process() {
	public void run() {
		try {
			while (!isInterrupted()) {
				String message = nextMessage();
				sendToUser(message);
			}
		} catch (Exception e) {
			System.out.println("Connection false, please reconnect");
		}

		user.uReceiver.interrupt();
		dispatcher.deleteUsers(user);
	}

}
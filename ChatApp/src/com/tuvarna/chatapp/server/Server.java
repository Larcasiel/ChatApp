package com.tuvarna.chatapp.server;

import java.net.*;
import java.io.*;

import com.tuvarna.chatapp.general.*;

//Slusha porta i priema novi klienti
public class Server {
	// Йоана: Смених порта от 8080 на 4321:
	public static final int PORT = 4321;
	private static ServerSocket socket;
	private static Dispatcher dispatcher;

	// Svurzvane kum porta
	private static void connect() {
		try {
			socket = new ServerSocket(PORT);
			System.out.println("Server started on Port: " + PORT + ".");
		} catch (IOException e) {
			System.err.println("Problem with Port: " + PORT + "! Try to reconnect or contact to administrator.");
			e.printStackTrace();
			System.exit(-1);
		}
	}

	// Upravlqva vruzkata s klientite
	private static void manageConnection() {
		while (true) {
			try {
				Socket s = socket.accept();
				s.setSoTimeout(20000);

				User user = new User();
				user.uSocket = s;
				
				System.out.println(s.toString());

				Receiver receiver = new Receiver(user, dispatcher);
				Sender sender = new Sender(user, dispatcher);

				user.uReceiver = receiver;
				receiver.start();

				user.uSender = sender;
				sender.start();

				dispatcher.addUsers(user);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public static void main(String[] args) {
		connect();

		dispatcher = new Dispatcher();
		//Йоана: Връзка между базата и диспечера:
		dispatcher.setDb(new DatabaseInteraction());
		dispatcher.start();

		manageConnection();
	}
}
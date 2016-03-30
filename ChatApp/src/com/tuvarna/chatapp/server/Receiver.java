package com.tuvarna.chatapp.server;

import java.net.*;
import java.io.*;

//Slusha za suobshteniq ot klienta i gi preprashta na dispechera
public class Receiver extends Thread {
	private Dispatcher dispatcher;
	private User user;
	private BufferedReader reader;

	public Receiver(User usr, Dispatcher dsptchr) throws IOException {
		user = usr;
		dispatcher = dsptchr;
		Socket socket = usr.uSocket;
		reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
	}

	// �����: �������� ����� �� ������ �� process() �� run(). Receiver ��������� Thread,
	// ������ run() �� ����� �� �� ���������, ������ �� ������ start() �� ����� �� ���� Receiver.
	//public void process() {
	public void run() {
		try {
			while (!isInterrupted()) {
				String message = reader.readLine();
				
				if (message == null)
				{
					break;
				}
				
				dispatcher.addMessage(user, message);
			}
		} catch (IOException e) {
			System.out.println("Broken connection. Please reconnect");
		}
		
		user.uSender.interrupt();
		
		dispatcher.deleteUsers(user);
	}

}
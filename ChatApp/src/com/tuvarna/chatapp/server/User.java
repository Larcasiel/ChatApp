package com.tuvarna.chatapp.server;

import java.net.*;
import java.util.concurrent.atomic.AtomicInteger;

//Informaciq za vurzanite klienti kum survura
public class User {
	private static AtomicInteger pass = new AtomicInteger(1000);
	public String username;
	public Socket uSocket = null;
	public Receiver uReceiver = null;
	public Sender uSender = null;

	// �����: ����� ��������������, �� ����� ������������� ��������� �� username
	// � password:
	public User() {
		//Random rand = new Random();
		//int n = rand.nextInt(999) + 1;

		//username = "User" + n;
		//pass.getAndIncrement();
	}

	public User(String UN, int id) {
		UN = username;
		id = pass.getAndIncrement();
	}

}
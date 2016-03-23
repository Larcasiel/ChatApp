package com.tuvarna.chatapp;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

public class Server {
	private static ServerSocket srvSocket;
	private static final int PORT=3456;
	
	private static DatabaseInteraction db = null;
	
	private static void handleClient(){
		Socket clientConnection = null;
		
		Scanner input = null;
		PrintWriter output = null;
		
		try {	
			clientConnection = srvSocket.accept();
			
			System.out.println("New connection: " + clientConnection.getRemoteSocketAddress());
			
			try{
				input = new Scanner(clientConnection.getInputStream());
				output = new PrintWriter(clientConnection.getOutputStream());
				
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				int n = input.nextInt();
				
				for(int i=0; i<n; i++){
					output.println((i+1) + "Message.");
				}
			} catch (IOException e) {
				System.err.println("Error opening input & output socket streams.");
			} finally {
				if(input != null){
					input.close();
				}
				
				if(output != null){
					output.close();
				}
				
				if(clientConnection != null){
					clientConnection.close();
				}
			}
			
			clientConnection.close();
			srvSocket.close();
		} catch (IOException e) {
			System.err.println("Problem accepting new connection.");
		} 
	}
	
	public static void main(String[] args) {
		db = new DatabaseInteraction();
		
		try {
			System.out.println("Starting server on port " + PORT);
			srvSocket = new ServerSocket(PORT);
			
			//db.getAllMessages();
			db.getAllMessagesFromChat(1);
			
		} catch (IOException e) {
			System.err.println("Problem creating server socket.");
		}
		
		do
		{
			handleClient();
		} while(true);
	}

}

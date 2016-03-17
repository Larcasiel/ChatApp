package com.tuvarna.chatapp;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Random;
import java.util.Scanner;

public class Client {
	private static final int PORT=3456;
	
	public static void main(String[] args){
		Socket connection = null;
		Scanner input = null;
		PrintWriter output = null;
		
		try {
			connection = new Socket("localhost", PORT);
			input = new Scanner(connection.getInputStream());
			output = new PrintWriter(connection.getOutputStream());
			
			Random rand = new Random();
			
			output.println(rand.nextInt(15));
			
			while(input.hasNextLine()){
				System.out.println(input.nextLine());
			}
		} catch (IOException e) {
			System.err.println("Problem connecting to server.");
		} finally {
			if(input != null){
				input.close();
			}
			
			if(output != null){
				output.close();
			}
			
			if(connection != null){
				try {
					connection.close();
				} catch (IOException e) {
					System.err.println("Problem closing socket.");
				}
			}
		}
		
	}
}

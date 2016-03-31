package com.tuvarna.chatapp.client;

import java.io.*;
import java.net.*;

public class ConsoleClient {
	private static final int PORT = 4321;

	private static BufferedReader mSocketReader;
	private static PrintWriter mSocketWriter;

	public static void main(String[] args) {
		// Connect to the chat server
		try {
			Socket socket = new Socket("localhost", PORT);
			mSocketReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			mSocketWriter = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()));
			System.out.println("Connected to server ");
		} catch (IOException ioe) {
			System.err.println("Can not connect to ");
			ioe.printStackTrace();
			System.exit(-1);
		}
		// Start socket --> console transmitter thread
		PrintWriter consoleWriter = new PrintWriter(System.out);
		TextDataTransmitter socketToConsoleTransmitter = new TextDataTransmitter(mSocketReader, consoleWriter);
		socketToConsoleTransmitter.setDaemon(false);
		socketToConsoleTransmitter.start();
		// Start console --> socket transmitter thread
		BufferedReader consoleReader = new BufferedReader(new InputStreamReader(System.in));
		TextDataTransmitter consoleToSocketTransmitter = new TextDataTransmitter(consoleReader, mSocketWriter);
		consoleToSocketTransmitter.setDaemon(false);
		consoleToSocketTransmitter.start();
	}

	static class TextDataTransmitter extends Thread {
		private BufferedReader mReader;
		private PrintWriter mWriter;

		public TextDataTransmitter(BufferedReader aReader, PrintWriter aWriter) {
			mReader = aReader;
			mWriter = aWriter;
		}

		/**
		 * Until interrupted reads a text line from the reader and sends it to
		 * the writer.
		 */
		public void run() {
			try {
				while (!isInterrupted()) {
					String data = mReader.readLine();
					if (!data.equals("KeepAlive")) {
						mWriter.println(data);
						mWriter.flush();
					}
				}
			} catch (IOException ioe) {
				System.err.println("Lost connection to server.");
				System.exit(-1);
			}
		}
	}
}

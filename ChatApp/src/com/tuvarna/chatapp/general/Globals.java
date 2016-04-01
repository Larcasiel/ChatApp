package com.tuvarna.chatapp.general;

public class Globals {
	public static enum CONNECTION_STATUS { CONNECTED, SECURITY_ERROR, SERVER_DOWN, INVALID_USER_OR_PASS }
	
	public static final String DB_URL = "jdbc:mysql://localhost:3306/chatappdb?autoReconnect=true&useSSL=false";
	public static final String DB_USER = "chatapp";
	public static final String DB_PASSWORD = "chatapppass";
	
	public static final String GET_ALL_MESASGES = "SELECT Message FROM ChatMessage;";
	public static final String ADD_NEW_MESSAGE = "INSERT INTO ChatMessage(SenderId, ChatId, MessageTime, Message) VALUES(?, ?, ?, ?);";
	
	public static String getAllMessagesFromChat(int chatId){
		return "SELECT a.MessageTime, a.Message, b.Username FROM ChatMessage AS a, ChatUser AS b " +
		 "WHERE a.ChatId = " + chatId + " AND a.SenderId = b.Id;";
	}
	
	public static String logIn(String username, String password){
		return "SELECT COUNT(*) FROM ChatUser WHERE username = \'" + username + "\' AND password = \'" + password + "\';";
	}
}

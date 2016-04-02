package com.tuvarna.chatapp.general;

public class Globals {
	public static enum LOGIN_STATUS { INVALID_USER_OR_PASS, ALREADY_LOGGED, DB_PROBLEMS, SUCCESS }
	
	public static final String DB_URL = "jdbc:mysql://localhost:3306/chatappdb?autoReconnect=true&useSSL=false";
	public static final String DB_USER = "chatapp";
	public static final String DB_PASSWORD = "chatapppass";
	
	public static final String GET_ALL_MESASGES = "SELECT Message FROM ChatMessage;";
	public static final String ADD_NEW_MESSAGE = "INSERT INTO ChatMessage(SenderId, ChatId, MessageTime, Message) VALUES(?, ?, ?, ?);";
	public static final String UPDATE_USER_STATUS = "UPDATE ChatUser SET Online = ?, IPAddress = ? WHERE Username = ?";
	
	public static String getAllMessagesFromChat(int chatId){
		return "SELECT a.MessageTime, a.Message, b.Username FROM ChatMessage AS a, ChatUser AS b " +
		 "WHERE a.ChatId = " + chatId + " AND a.SenderId = b.Id;";
	}
	
	public static String checkPassword(String username, String password){
		return "SELECT COUNT(*) FROM ChatUser WHERE Username = \'" + username + "\' AND Password = \'" + password + "\';";
	}
	
	public static String isLoggedIn(String username, String ipAddress) {
		return "SELECT COUNT(*) FROM ChatUser WHERE Username = \'" + username + "\' AND IPAddress = \'" + ipAddress + "\' " +
				"AND Online = 1;";
	}
}

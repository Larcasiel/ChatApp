package com.tuvarna.chatapp;

public class Globals {
	public static final String DB_URL = "jdbc:mysql://localhost:3306/chatappdb?autoReconnect=true&useSSL=false";
	public static final String DB_USER = "chatapp";
	public static final String DB_PASSWORD = "chatapppass";
	
	public static final String GET_ALL_MESASGES = "SELECT Message FROM ChatMessage";
	
	public static String GetAllMessagesFromChat(int chatId){
		return "SELECT a.MessageTime, a.Message, b.Username FROM ChatMessage AS a, ChatUser AS b " +
		 "WHERE a.ChatId = " + chatId + " AND a.SenderId = b.Id;";
	}
}

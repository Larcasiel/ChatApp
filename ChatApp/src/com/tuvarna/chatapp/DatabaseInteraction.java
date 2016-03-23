package com.tuvarna.chatapp;

import java.sql.*;

import com.mysql.jdbc.Connection;

public class DatabaseInteraction {
	private String url = Globals.DB_URL;
	private String user = Globals.DB_USER;
	private String password = Globals.DB_PASSWORD;
	
	private Connection conn = null;
	
	public DatabaseInteraction(){
		
	}
	
	public DatabaseInteraction(String url, String user, String password){
		this.url = url;
		this.user = user;
		this.password = password;
	}
	
	private void connect(){
		conn = null;
		
	    try{
	    	conn = (Connection) DriverManager.getConnection(url, user, password);
	    }
	    catch(SQLException e){
	      e.printStackTrace();
	    }
	    finally{
	    	if(conn == null){
	    		System.out.println("Can't establish a connection to the database.");
	    	}
	    	else{
	    		System.out.println("Connected to the database.");
	    	}
	    }
	}
	
	public void getAllMessagesFromChat(int chatId){
		connect();
		
		Statement stmt = null;
		ResultSet resultSet = null;
		
		try{
			stmt = conn.createStatement();
			resultSet = stmt.executeQuery(Globals.GetAllMessagesFromChat(chatId));
			
			System.out.println("Messages from chat " + chatId + ":");
			
			while(resultSet.next()){
				System.out.println("[" + resultSet.getString("MessageTime") + "] " + resultSet.getString("Username") + " says: " +
								    resultSet.getString("Message"));
			}
		}
		catch(SQLException e1){
			e1.printStackTrace();
		}
		finally{
			try{
				if(stmt != null){
					stmt.close();
				}
				
				if(resultSet != null){
					resultSet.close();
				}
				
				if(conn != null){
					conn.close();
				}
			}
			catch(SQLException e2){
				e2.printStackTrace();
			}
		}
	}
	
	public void getAllMessages(){
		connect();
		
		Statement stmt = null;
		ResultSet resultSet = null;
		
		try{
			stmt = conn.createStatement();
			resultSet = stmt.executeQuery(Globals.GET_ALL_MESASGES);
			
			while(resultSet.next()){
				System.out.println("Message: " + resultSet.getString("Message"));
			}
		}
		catch(SQLException e1){
			e1.printStackTrace();
		}
		finally{
			try{
				if(stmt != null){
					stmt.close();
				}
				
				if(resultSet != null){
					resultSet.close();
				}
				
				if(conn != null){
					conn.close();
				}
			}
			catch(SQLException e2){
				e2.printStackTrace();
			}
		}
	}
}

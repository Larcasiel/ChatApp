package com.tuvarna.chatapp.general;

import java.sql.*;
import java.util.Date;
import java.text.*;

import com.mysql.jdbc.Connection;
import com.tuvarna.chatapp.general.Globals.*;

public class DatabaseInteraction {
	private String url = Globals.DB_URL;
	private String user = Globals.DB_USER;
	private String password = Globals.DB_PASSWORD;

	private Connection conn = null;

	public DatabaseInteraction() {

	}

	public DatabaseInteraction(String url, String user, String password) {
		this.url = url;
		this.user = user;
		this.password = password;
	}

	private boolean connect() {
		boolean result = false;

		conn = null;

		try {
			conn = (Connection) DriverManager.getConnection(url, user, password);

			result = true;
		} catch (SQLException e) {
			//e.printStackTrace();
		} finally {
			if (conn == null) {
				System.out.println("Can't establish a connection to the database.");
			} else {
				System.out.println("Connected to the database.");
			}
		}

		return result;
	}

	public void updateUserStatus(String username, String ipAddress, int online) {
		if (conn == null) {
			connect();
			
			PreparedStatement preparedStmt;

			try {
				preparedStmt = conn.prepareStatement(Globals.UPDATE_USER_STATUS);
				preparedStmt.setInt(1, online);
				preparedStmt.setString(2, ipAddress);
				preparedStmt.setString(3, username);

				int rowsAffected = preparedStmt.executeUpdate();

				System.out.println(rowsAffected + " rows affected.");
			} catch (SQLException e1) {
				e1.printStackTrace();
			}
			
			if (conn != null) {
				try {
					conn.close();
					conn = null;
				} catch (SQLException e) {

					e.printStackTrace();
				}
			}
		} else {
			PreparedStatement preparedStmt;

			try {
				preparedStmt = conn.prepareStatement(Globals.UPDATE_USER_STATUS);
				preparedStmt.setInt(1, online);
				preparedStmt.setString(2, ipAddress);
				preparedStmt.setString(3, username);

				int rowsAffected = preparedStmt.executeUpdate();

				System.out.println(rowsAffected + " rows affected.");
			} catch (SQLException e1) {
				e1.printStackTrace();
			}
		}
	}
	
	public REGISTER_STATUS register(String username, String password) {
		REGISTER_STATUS result = REGISTER_STATUS.DB_PROBLEMS;
		
		if (connect()) {

			PreparedStatement preparedStmtUserExists = null;
			PreparedStatement preparedStmtRegister = null;
			ResultSet resultSetUserExists = null;

			try {
				preparedStmtUserExists = conn.prepareStatement(Globals.USER_EXISTS);
				preparedStmtUserExists.setString(1, username);
				resultSetUserExists = preparedStmtUserExists.executeQuery();

				int userExists = 0;

				while (resultSetUserExists.next()) {
					userExists = resultSetUserExists.getInt(1);
				}

				if (userExists > 0) {
					result = REGISTER_STATUS.USER_ALREADY_EXISTS;
				} else {
					preparedStmtRegister = conn.prepareStatement(Globals.REGISTER_USER);
					preparedStmtRegister.setString(1, username);
					preparedStmtRegister.setString(2, password);
					
					int rowsAffected = preparedStmtRegister.executeUpdate();

					if (rowsAffected > 0) {
						result = REGISTER_STATUS.REGISTER_SUCCESS;
					}
				}
			} catch (SQLException e1) {
				e1.printStackTrace();
			} finally {
				try {
					if (preparedStmtUserExists != null) {
						preparedStmtUserExists.close();
					}
					
					if (preparedStmtRegister != null) {
						preparedStmtRegister.close();
					}

					if (resultSetUserExists != null) {
						resultSetUserExists.close();
					}

					if (conn != null) {
						conn.close();
						conn = null;
					}
				} catch (SQLException e2) {
					e2.printStackTrace();
				}
			}
		} else {
			result = REGISTER_STATUS.DB_PROBLEMS;
		}

		return result;
	}

	public LOGIN_STATUS logIn(String username, String password, String ipAddress) {
		LOGIN_STATUS result = LOGIN_STATUS.DB_PROBLEMS;

		if (connect()) {

			Statement stmt = null;
			ResultSet resultSetLoggedIn = null;
			ResultSet resultSetPasswordCheck = null;

			try {
				stmt = conn.createStatement();
				resultSetLoggedIn = stmt.executeQuery(Globals.isLoggedIn(username, ipAddress));

				int isLoggedIn = 0;

				while (resultSetLoggedIn.next()) {
					isLoggedIn = resultSetLoggedIn.getInt(1);
				}

				if (isLoggedIn > 0) {
					result = LOGIN_STATUS.ALREADY_LOGGED;
				} else {
					resultSetPasswordCheck = stmt.executeQuery(Globals.checkPassword(username, password));

					int count = 0;

					while (resultSetPasswordCheck.next()) {
						count = resultSetPasswordCheck.getInt(1);
					}

					if (count > 0) {
						result = LOGIN_STATUS.LOGIN_SUCCESS;

						updateUserStatus(username, ipAddress, 1);

					} else {
						result = LOGIN_STATUS.INVALID_USER_OR_PASS;
					}
				}
			} catch (SQLException e1) {
				e1.printStackTrace();
			} finally {
				try {
					if (stmt != null) {
						stmt.close();
					}

					if (resultSetLoggedIn != null) {
						resultSetLoggedIn.close();
					}

					if (resultSetPasswordCheck != null) {
						resultSetPasswordCheck.close();
					}

					if (conn != null) {
						conn.close();
						conn = null;
					}
				} catch (SQLException e2) {
					e2.printStackTrace();
				}
			}
		} else {
			result = LOGIN_STATUS.DB_PROBLEMS;
		}

		return result;
	}

	public void addNewMessage(int senderId, int chatId, String message) {
		connect();

		Date dt = new Date();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

		PreparedStatement preparedStmt;

		try {
			preparedStmt = conn.prepareStatement(Globals.ADD_NEW_MESSAGE);
			preparedStmt.setInt(1, senderId);
			preparedStmt.setInt(2, chatId);
			preparedStmt.setString(3, sdf.format(dt));
			preparedStmt.setString(4, message);

			int rowsInserted = preparedStmt.executeUpdate();

			System.out.println(rowsInserted + " rows inserted.");
		} catch (SQLException e1) {
			e1.printStackTrace();
		}

		if (conn != null) {
			try {
				conn.close();
				conn = null;
			} catch (SQLException e) {

				e.printStackTrace();
			}
		}
	}

	public void getAllMessagesFromChat(int chatId) {
		connect();

		Statement stmt = null;
		ResultSet resultSet = null;

		try {
			stmt = conn.createStatement();
			resultSet = stmt.executeQuery(Globals.getAllMessagesFromChat(chatId));

			System.out.println("Messages from chat " + chatId + ":");

			while (resultSet.next()) {
				System.out.println("[" + resultSet.getString("MessageTime") + "] " + resultSet.getString("Username")
						+ " says: " + resultSet.getString("Message"));
			}
		} catch (SQLException e1) {
			e1.printStackTrace();
		} finally {
			try {
				if (stmt != null) {
					stmt.close();
				}

				if (resultSet != null) {
					resultSet.close();
				}

				if (conn != null) {
					conn.close();
					conn = null;
				}
			} catch (SQLException e2) {
				e2.printStackTrace();
			}
		}
	}

	public void getAllMessages() {
		connect();

		Statement stmt = null;
		ResultSet resultSet = null;

		try {
			stmt = conn.createStatement();
			resultSet = stmt.executeQuery(Globals.GET_ALL_MESASGES);

			while (resultSet.next()) {
				System.out.println("Message: " + resultSet.getString("Message"));
			}
		} catch (SQLException e1) {
			e1.printStackTrace();
		} finally {
			try {
				if (stmt != null) {
					stmt.close();
				}

				if (resultSet != null) {
					resultSet.close();
				}

				if (conn != null) {
					conn.close();
					conn = null;
				}
			} catch (SQLException e2) {
				e2.printStackTrace();
			}
		}
	}
}

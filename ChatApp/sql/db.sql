CREATE DATABASE chatappdb;

USE chatappdb;

CREATE TABLE ChatUser(
		Id INT NOT NULL AUTO_INCREMENT,
		Username VARCHAR(30) NOT NULL,
		Password VARCHAR(30) NOT NULL,
		Age INT NULL,
		About VARCHAR(140) NULL,
		PRIMARY KEY (Id)
	) ENGINE=InnoDB;
	
CREATE TABLE Chat(
		Id INT NOT NULL AUTO_INCREMENT,
		Title VARCHAR(100) NULL,
		PRIMARY KEY (Id)
	) ENGINE=InnoDB;

CREATE TABLE UserInChat(
		Id INT NOT NULL AUTO_INCREMENT,
		ChatId INT NOT NULL,
		UserId INT NOT NULL,
		PRIMARY KEY (Id),
		FOREIGN KEY (ChatId)
			REFERENCES Chat(Id)
			ON DELETE CASCADE,
		FOREIGN KEY (UserId)
			REFERENCES ChatUser(Id)
			ON DELETE CASCADE
	) ENGINE=InnoDB;
	
CREATE TABLE ChatMessage(
		Id int NOT NULL AUTO_INCREMENT,
		SenderId INT NOT NULL,
		ChatId INT NOT NULL,
		MessageTime DATETIME NOT NULL,
		Message TEXT NOT NULL,
		PRIMARY KEY (Id),
		FOREIGN KEY (SenderId)
			REFERENCES ChatUser(Id)
			ON DELETE CASCADE,
		FOREIGN KEY (ChatId)
			REFERENCES Chat(Id)
			ON DELETE CASCADE
	) ENGINE=InnoDB;
	
GRANT ALL
ON chatappdb.*
TO 'chatapp'@'localhost'
IDENTIFIED BY 'chatapppass';

INSERT INTO ChatUser (Username, Password, Age, About)
VALUES ("Petko77", "password", 22, "Some info."),
	   ("AnotherUser", "pass", 19, "Info."),
	   ("User3", "parola", 13, "More info.");
	  
INSERT INTO Chat (Title)
VALUES (NULL),
	   ("Group Chat Between 3 Users."),
	   (NULL);

INSERT INTO UserInChat (ChatId, UserId)
VALUES (1, 1),
	   (1, 2),
	   (2, 1),
	   (2, 2),
	   (2, 3),
	   (3, 2),
	   (3, 3);

INSERT INTO ChatMessage (SenderId, ChatId, MessageTime, Message)
VALUES (1, 1, NOW(), "Hello, AnotherUser!"),
	   (2, 1, NOW(), "Hi, Petko77."),
	   (3, 2, NOW(), "Hello, everyone!"),
	   (3, 3, NOW(), "Message from User3.");
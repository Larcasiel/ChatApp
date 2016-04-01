package com.tuvarna.chatapp.gui;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import com.tuvarna.chatapp.client.ChatClient;

import flexjson.JSONSerializer;

import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.Color;
import java.awt.EventQueue;
import java.awt.List;
import java.awt.Rectangle;
import java.awt.TextArea;
import java.awt.TextField;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Vector;

//Йоана: Класът вече наследява JFrame, не JApplet.
public class GUI extends JFrame implements ActionListener, MouseListener {

	private static final long serialVersionUID = 1L;

	private JPanel contentPane;

	// Йоана: Ненужна променлива, никъде не се използва. Закоментирах я:
	// boolean isStandalone = false;

	private ChatClient cl = new ChatClient(this);

	// Йоана: Май не сме се разбирали да има PopupMenu никъде, закоментирам
	// засега:
	// private PopupMenu popupMenu1 = new PopupMenu();
	// private MenuItem menuItem1 = new MenuItem();
	// private MenuItem menuItem2 = new MenuItem();

	private Vector<String> allUsers = new Vector<String>();

	// Йоана: Не сме говорили за deniedUsers, засаега се коментира:
	// private Vector<String> deniedUsers = new Vector<String>();

	//Йоана: Промених името на флага от isConnected на isLoggedIn:
	private boolean isLoggedIn = false;
	private Button connectButton = new Button();
	private Button disconnectButton = new Button();
	private Button exitButton = new Button();

	// Йоана: Преименувах textArea1 на chatTextArea
	// public TextArea textArea1 = new TextArea();
	public TextArea chatTextArea = new TextArea();

	// Йоана: Преименувах list1 на userList
	// public List list1 = new List();
	public List userList = new List();

	// Йоана: Преименувах textField1 на msgTextField
	// private TextField textField1 = new TextField();
	private TextField msgTextField = new TextField();

	private Button sendButton = new Button();

	// Йоана: Тъй като работим с JFrame, а не с JApplet, е необходимо
	// инициализацията да се извършва в
	// конструктора, не в jbInit():
	// private void jbInit() throws Exception {
	public GUI() {
		setTitle("ChatApp");
		// Йоана: Добавих нов JPanel - contentPlane.
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 474, 322);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		contentPane.setLayout(null);
		setContentPane(contentPane);

		contentPane.setLayout(null);

		connectButton.setLabel("Login");
		connectButton.setBounds(new Rectangle(10, 213, 95, 22));
		connectButton.addActionListener(this);
		contentPane.add(connectButton, null);

		disconnectButton.addActionListener(this);
		disconnectButton.setBounds(new Rectangle(10, 241, 95, 22));
		disconnectButton.setLabel("Logout");
		contentPane.add(disconnectButton, null);

		sendButton.setLabel("Send");
		sendButton.setBounds(new Rectangle(395, 222, 45, 23));
		sendButton.addActionListener(this);
		contentPane.add(sendButton);

		exitButton.setBackground(Color.lightGray);
		exitButton.setForeground(Color.black);
		exitButton.setLabel("Exit");
		exitButton.setBounds(new Rectangle(10, 3, 31, 22));
		exitButton.addActionListener(this);
		contentPane.add(exitButton, null);

		msgTextField.setBounds(new Rectangle(138, 222, 252, 23));
		msgTextField.addActionListener(this);
		msgTextField.setBackground(Color.lightGray);
		contentPane.add(msgTextField, null);
		// -- End edit controls

		// Йоана: Тъй като не сме говорили за такива неща, ги закоментирам
		// засега:
		// -- Begin menus section
		// popupMenu1.setLabel("User menu");
		// menuItem1.setLabel("Ignore user");
		// menuItem1.setActionCommand("BAN");
		// menuItem1.addActionListener(this);
		// menuItem2.setLabel("Deignore user");
		// menuItem2.setActionCommand("UNBAN");
		// menuItem2.addActionListener(this);
		// popupMenu1.add(menuItem1);
		// popupMenu1.addSeparator();
		// popupMenu1.add(menuItem2);
		// -- End menus section

		userList.setBounds(new Rectangle(10, 31, 111, 176));

		// Йоана: Не сме говорили за PopupMenu, закоментирам:
		// list1.add(popupMenu1);

		userList.addActionListener(this);
		userList.addMouseListener(this);
		userList.setBackground(Color.lightGray);

		// Йоана: Не сме говорили за PopupMenu, закоментирам:
		// getContentPane().add(popupMenu1);

		getContentPane().add(userList, null);
		chatTextArea.setBounds(138, 31, 302, 176);
		getContentPane().add(chatTextArea, BorderLayout.EAST);
		chatTextArea.setBackground(Color.lightGray);
		chatTextArea.setBackground(Color.lightGray);

		this.setBackground(Color.gray);
	}

	public void setLoggedIn(boolean loggedIn) {
		isLoggedIn = loggedIn;
	}

	public boolean getLoggedIn() {
		return isLoggedIn;
	}

	/**
	 * Method is called from ChatClient to get reference to
	 * ChatApplet.textArea1.
	 * 
	 * @return java.awt.TextArea
	 */
	public TextArea getTextArea() {
		return chatTextArea;
	}

	/**
	 * Method is called from ChatClient to get reference to ChatApplet.list1.
	 * 
	 * @return java.awt.List
	 */
	public List getList() {
		return userList;
	}

	/**
	 * Method is called from ChatClient to register anUser in allUsers vector
	 * and list1 visual control.
	 * 
	 * @param anUser
	 *            - user to be included.
	 */
	public synchronized void addUser(String anUser) {
		if (!allUsers.contains(anUser)) {
			allUsers.addElement(anUser);
			userList.add(anUser);
		}
	}

	// Йоана: Добавих метод за изтриване на всички users в списъка:
	public synchronized void removeAllUsers() {
		allUsers.removeAllElements();
		userList.removeAll();
	}

	/**
	 * Method is called from ChatClient to append given message to the
	 * ChatApplet's TextArea. It also checks whether anUser is in our Ignore
	 * list and ignores the message in this case.
	 * 
	 * @param anUser
	 *            - user that have sent the message
	 * @param aText
	 *            - message to be appened to the applet's TextArea
	 */
	public synchronized void addText(String aText, String anUser) {
		// Йоана: Не сме говорили за deniedUsers, закоментирам:
		// if (!deniedUsers.contains(anUser)) {
		chatTextArea.append(aText + "\n");
		// }
	}

	public synchronized void addSystemMessage(String aText) {
		chatTextArea.append(aText + "\n");
	}

	// Йоана: Не работим с JApplet, този init() не ни трябва:
	// public void init() {
	// try {
	// jbInit();
	// }
	// catch(Exception e) {
	// e.printStackTrace();
	// }
	// }

	// Йоана: Тъй като вече работим с JFrame, трябва да го инициализираме оттук:
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					GUI frame = new GUI();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Method sends aMessage to server through ChatClient.
	 * 
	 * @param aMessage
	 */
	public void sendMessage(String aMessage) {
		
		HashMap<String, String> operationMsg = new HashMap<String, String>();

		SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyy HH:mm:ss");

		Date now = new Date();
		String date = sdf.format(now);

		operationMsg.put("operation", "receiveMessage");
		operationMsg.put("message", aMessage);
		operationMsg.put("sender", cl.getUsername());
		operationMsg.put("time", date);

		JSONSerializer serializer = new JSONSerializer();
		
		cl.getOutput().println(serializer.serialize(operationMsg).toString());
		cl.getOutput().flush();
	}

	/**
	 * Method handles ActionEvent event, registered by "this" Action listener.
	 * 
	 * @param ae
	 *            - ActionEvent which we used to indicate "sender".
	 */
	public void actionPerformed(ActionEvent ae) {
		if (ae.getSource().equals(msgTextField)) { // catch ActionEvents coming
													// from textField1
			sendButtonPressed();
		} else if (ae.getSource().equals(sendButton)) { // catch ActionEvents
														// coming send button
														// from textField1
			sendButtonPressed();
		}
		if (ae.getSource().equals(connectButton)) { // catch ActionEvents coming
													// connect button from
													// textField1
			// Йоана: Добавям допълнителна логика за логване зад Login бутона:
			if (!isLoggedIn) {
				//addSystemMessage("Connecting...");
				//isLoggedIn = cl.connect();

				LoginDialog loginDialog = new LoginDialog(this, true, cl);
				loginDialog.setVisible(true);
			} else {
				addSystemMessage("Already logged in.");
			}
		}

		// Йоана: Тези неща са свързани с PopupMenu-то, закоментирам ги засега:
		// if (ae.getSource().equals(menuItem1)) { // catch ActionEvents comming
		// // from
		// // popupMenu->menuItem1->"Ignore"
		// String selectedUser = list1.getSelectedItem();
		// deniedUsers.addElement(selectedUser);
		// addSystemMessage("User added to ban list.");
		// } else if (ae.getSource().equals(menuItem2)) { // catch ActionEvents
		// // comming from
		// // popupMenu->menuItem1->"Deignore"
		// String selectedUser = list1.getSelectedItem();
		// if (!deniedUsers.removeElement(selectedUser))
		// addSystemMessage("No such user in ban list.");
		// else
		// addSystemMessage("User removed from ban list.");
		// } else
		if (ae.getSource().equals(disconnectButton)) { // catch
														// ActionEvents
														// comming from
														// disconnect
														// button
			cl.disconnect();
		} else if (ae.getSource().equals(exitButton)) { // catch ActionEvents
														// comming from exit
														// button
			System.exit(0);
		}
	}

	private void sendButtonPressed() {
		if (!isLoggedIn) {
			chatTextArea.append("Please connect first.\n");
			return;
		}
		String text = msgTextField.getText();
		if (text.equals(""))
			return;
		sendMessage(text);
		msgTextField.setText("");
	}

	public void mouseClicked(MouseEvent e) {
	}

	public void mouseReleased(MouseEvent e) {
	}

	public void mouseEntered(MouseEvent e) {
	}

	public void mouseExited(MouseEvent e) {
	}

	/**
	 * Method handles mousePressed event, registered by "this" MouseListener.
	 * 
	 * @param e
	 *            MouseEvent
	 */
	// Йоана: Този @SupressWarnings не ни трябва засега:
	// @SuppressWarnings("static-access")
	public void mousePressed(MouseEvent e) {
		// Йоана: Не сме говорили за PopupMenu, закоментирам:
		// register when user pressed right mouse button
		// - e.getModifiers()==e.BUTTON3_MASK -
		// and when is "mouse down" - e.MOUSE_PRESSED==501.
		// if ((e.getModifiers() == InputEvent.BUTTON3_MASK) && (e.MOUSE_PRESSED
		// == 501)) {
		// popupMenu1.show(msgTextField, e.getX(), e.getY());
		// }
	}
}

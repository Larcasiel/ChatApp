package com.tuvarna.chatapp.client;

import javax.swing.JApplet;

import java.awt.Button;
import java.awt.Color;
import java.awt.List;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.Rectangle;
import java.awt.TextArea;
import java.awt.TextField;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Vector;


public class GUI extends JApplet implements ActionListener, MouseListener{

	private static final long serialVersionUID = 1L;

	public GUI() {
	}

		boolean isStandalone = false;
		private ChatClient cl           = new ChatClient (this);
		private PopupMenu popupMenu1    = new PopupMenu ();
		private MenuItem menuItem1      = new MenuItem ();
		private MenuItem menuItem2      = new MenuItem ();
		private Vector<String> allUsers = new Vector<String> ();
		private Vector<String> deniedUsers = new Vector<String> ();
		private boolean isConnected     = false;
		private Button connectButton    = new Button ();
		private Button disconnectButton = new Button ();
		private Button exitButton       = new Button ();
		public TextArea textArea1      = new TextArea();
		public List list1              = new List();
		private TextField textField1    = new TextField();
		private Button sendButton       = new Button ();
		
		public void setConnected(boolean aConnected) {
			isConnected = aConnected;
		}

		public boolean getConnected()	{
			return isConnected;
		}

		/**
		 * Method is called from ChatClient to get reference to ChatApplet.textArea1.
		 * @return java.awt.TextArea
		 */
		public TextArea getTextArea () {
			return textArea1;
		}

		/**
		 * Method is called from ChatClient to get reference to ChatApplet.list1.
		 * @return java.awt.List
		 */
		public List getList () {
			return list1;
		}

		/**
		 * Method is called from ChatClient to register anUser in allUsers vector
		 * and list1 visual control.
		 * @param anUser - user to be included.
		 */
		public synchronized void addUser (String anUser) {
			if (!allUsers.contains(anUser)) {
				allUsers.addElement (anUser);
				list1.add (anUser);
			}
		}

		/**
		 * Method is called from ChatClient to append given message to the
		 * ChatApplet's TextArea. It also checks whether anUser is in our
		 * Ignore list and ignores the message in this case.
		 * @param anUser - user that have sent the message
		 * @param aText - message to be appened to the applet's TextArea
		 */
		public synchronized void addText (String aText, String anUser) {
			if (!deniedUsers.contains(anUser)) {
				textArea1.append (aText + "\n");
			}
		}

		public synchronized void addSystemMessage(String aText) {
			textArea1.append(aText + "\n");
		}

		public void init() {
			try {
				jbInit();
			}
			catch(Exception e) {
				e.printStackTrace();
			}
		}

		/**
		 * Component initialization.
		 * @throws Exception
		 */
		private void jbInit() throws Exception {
			getContentPane().setLayout (null);

			connectButton.setLabel("connect");
			connectButton.setBounds(new Rectangle(10, 213, 95, 22));
			connectButton.addActionListener(this);
			getContentPane().add(connectButton, null);

			disconnectButton.addActionListener(this);
			disconnectButton.setBounds(new Rectangle(10, 241, 95, 22));
			disconnectButton.setLabel("disconnect");
			getContentPane().add(disconnectButton, null);

			sendButton.setLabel("Send");
			sendButton.setBounds (new Rectangle(395, 222, 45, 23));
			sendButton.addActionListener (this);
			getContentPane().add(sendButton);

			exitButton.setBackground(Color.lightGray);
			exitButton.setForeground(Color.black);
			exitButton.setLabel("Exit");
			exitButton.setBounds(new Rectangle(10, 3, 31, 22));
			exitButton.addActionListener(this);
			getContentPane().add(exitButton, null);

			textField1.setBounds(new Rectangle(138, 222, 252, 23));
			textField1.addActionListener(this);
			textField1.setBackground(Color.lightGray);
			getContentPane().add(textField1, null);
			// -- End edit controls


			// -- Begin menus section
			popupMenu1.setLabel("User menu");
			menuItem1.setLabel("Ignore user");
			menuItem1.setActionCommand ("BAN");
			menuItem1.addActionListener (this);
			menuItem2.setLabel("Deignore user");
			menuItem2.setActionCommand ("UNBAN");
			menuItem2.addActionListener (this);
			popupMenu1.add(menuItem1);
			popupMenu1.addSeparator();
			popupMenu1.add(menuItem2);
			// -- End menus section

			list1.setBounds(new Rectangle(20, 31, 100, 176));
			list1.add (popupMenu1);
			list1.addActionListener (this);
			list1.addMouseListener (this);
			list1.setBackground(Color.lightGray);
			getContentPane().add (popupMenu1);
			getContentPane().add(list1, null);
			textArea1.setBounds(138, 31, 279, 176);
			getContentPane().add(textArea1);
			textArea1.setBackground(Color.lightGray);
			textArea1.setBackground(Color.lightGray);

			this.setBackground(Color.gray);
		}

		/**
		 * Method sends aMessage to server through ChatClient.
		 * @param aMessage
		 */
		public void sendMessage (String aMessage) {
			cl.getOutput().println (aMessage);
			cl.getOutput().flush();
		}

		/**
		 * Method handles ActionEvent event, registered by "this" Action listener.
		 * @param ae - ActionEvent which we used to indicate "sender".
		 */
		public void actionPerformed (ActionEvent ae) {
			if (ae.getSource().equals(textField1)) { // catch ActionEvents coming from textField1
				sendButtonPressed();}
				else if (ae.getSource().equals(sendButton)) { // catch ActionEvents coming send button from textField1
					sendButtonPressed(); 
				}
			if (ae.getSource().equals(connectButton)) { // catch ActionEvents coming connect button from textField1
			 if (!isConnected) {
					addSystemMessage("Connecting...");
					isConnected = cl.connect();
				} else {
					addSystemMessage("Already connected.");
				}
			} if (ae.getSource().equals(menuItem1)) { // catch ActionEvents comming from popupMenu->menuItem1->"Ignore"
				String selectedUser = list1.getSelectedItem();
				deniedUsers.addElement (selectedUser);
				addSystemMessage("User added to ban list.");
			} else if (ae.getSource().equals(menuItem2)) { // catch ActionEvents comming from popupMenu->menuItem1->"Deignore"
				String selectedUser = list1.getSelectedItem();
				if (!deniedUsers.removeElement (selectedUser))
					addSystemMessage("No such user in ban list.");
				else
					addSystemMessage("User removed from ban list.");
			} else if (ae.getSource().equals(disconnectButton)) { // catch ActionEvents comming from disconnect button
				cl.disconnect();
			} else if (ae.getSource().equals(exitButton)) { // catch ActionEvents comming from exit button
				System.exit(0);
			}
			}
			private void sendButtonPressed() {
				if (!isConnected) {
					textArea1.append("Please connect first.\n");
					return;
				}
				String text = textField1.getText();
				if (text.equals(""))
					return;
				sendMessage (text);
				textField1.setText ("");
			}
		


		public void mouseClicked(MouseEvent e) {}
		public void mouseReleased(MouseEvent e) {}
		public void mouseEntered(MouseEvent e) {}
		public void mouseExited(MouseEvent e) {}

		/**
		 * Method handles mousePressed event, registered by "this" MouseListener.
		 * @param e MouseEvent
		 */
		@SuppressWarnings("static-access")
		public void mousePressed(MouseEvent e) {
			// register when user pressed right mouse button
			// - e.getModifiers()==e.BUTTON3_MASK -
			// and when is "mouse down" - e.MOUSE_PRESSED==501.
			if ((e.getModifiers()==InputEvent.BUTTON3_MASK)&&(e.MOUSE_PRESSED==501)) {
				popupMenu1.show (textField1, e.getX(), e.getY());
			}
		}
		}
	

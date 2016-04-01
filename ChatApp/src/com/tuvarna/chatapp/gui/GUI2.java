package com.tuvarna.chatapp.gui;

import javax.swing.JApplet;

import com.tuvarna.chatapp.client.ChatClient;

import java.awt.Button;
	import java.awt.Color;
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


public class GUI2 extends JApplet implements ActionListener, MouseListener{

	public GUI2() {
	}
		private static final long serialVersionUID = 1L;
		boolean isStandalone = false;
		private ChatClient cl           = new ChatClient (this);
		private PopupMenu popupMenu1    = new PopupMenu ();
		private MenuItem menuItem1      = new MenuItem ();
		private MenuItem menuItem2      = new MenuItem ();
		private boolean isConnected     = false;
		private TextField textField1    = new TextField();
		private Button sendButton       = new Button ();
		private Button exitButton       = new Button ();
		public TextArea textArea1      = new TextArea();

		
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
		 * Method is called from ChatClient to append given message to the
		 * ChatApplet's TextArea. It also checks whether anUser is in our
		 * Ignore list and ignores the message in this case.
		 * @param anUser - user that have sent the message
		 * @param aText - message to be appened to the applet's TextArea
		 */


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

			// -- Begin buttons section
			sendButton.setLabel("Send");
			sendButton.setBounds (new Rectangle(372, 267, 68, 23));
			sendButton.addActionListener (this);
			getContentPane().add(sendButton);

			

			exitButton.setBackground(Color.lightGray);
			exitButton.setForeground(Color.black);
			exitButton.setLabel("Exit");
			exitButton.setBounds(new Rectangle(10, 10, 24, 23));
			exitButton.addActionListener(this);
			getContentPane().add(exitButton, null);
			// -- End buttons section

			// -- Begin edit controls
			textField1.setBounds(new Rectangle(61, 267, 303, 23));
			textField1.addActionListener(this);
			textField1.setBackground(Color.lightGray);
			getContentPane().add(textField1, null);

			textArea1.setBounds(new Rectangle(61, 28, 303, 233));
			textArea1.setBackground(Color.lightGray);
			getContentPane().add(textArea1, null);
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
			getContentPane().add (popupMenu1);

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
				sendButtonPressed();
			} else if (ae.getSource().equals(sendButton)) { // catch ActionEvents coming send button from textField1
				sendButtonPressed(); 
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


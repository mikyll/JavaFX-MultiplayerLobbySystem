package networking;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.BindException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;

import controller.Controller;
import javafx.scene.control.Alert.AlertType;
import model.User;
import model.chat.Message;
import model.chat.MessageType;

public class ServerStream implements IServer{
	
	private static final int PORT = 9001;
	private int maxNumUsers = 6;
	
	private Controller controller;
	private String nickname;
	private ServerListener serverListener;
	
	private ArrayList<User> users;
	private ArrayList<ObjectOutputStream> writers;
	
	public ServerStream(Controller controller, String nickname)
	{
		this.controller = controller;
		this.nickname = nickname;
		
		this.users = new ArrayList<User>();
		this.users.add(new User(nickname));
		this.writers = new ArrayList<ObjectOutputStream>();
		this.writers.add(null);
		
		try {
			this.serverListener = new ServerListener(PORT);
			this.serverListener.start();
			this.controller.switchToChatS();
		} catch (IOException e) {
			System.out.println("Server: ServerSocket creation failed");
			if(e instanceof BindException)
			{
				System.out.println("Server: another socket is already binded to this address and port");
				try {
					this.controller.showAlert(AlertType.ERROR, "Room creation failed", "Another socket is already binded to " + InetAddress.getLocalHost().toString().split("/")[1] + ":" + PORT);
				} catch (UnknownHostException e1) {
					e1.printStackTrace();
				}
			}
		}
	}
	
	private class ServerListener extends Thread {
		
		private ServerSocket listener;
		
		public ServerListener(int port) throws IOException
		{
			this.listener = new ServerSocket(port);
			System.out.println("Server: listening for connections on port " + PORT);
			
		}
		
		@Override
		public void run()
		{
			try {
				while(true)
				{
					new Handler(this.listener.accept()).start();
				}
			} catch (IOException e) {
				System.out.println("Server: error while trying to accept a new connection");
				e.printStackTrace();
			} finally {
				try {
					this.listener.close(); // CHECK
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	private class Handler extends Thread {
		private Socket socket;
		
		private InputStream is;
		private ObjectInputStream input;
		private OutputStream os;
        private ObjectOutputStream output;
        
        
		public Handler(Socket socket)
		{
			System.out.println("Server: connection accepted");
			this.socket = socket;
		}
		
		@Override
		public void run()
		{
			try {
				// NB: the order of these is important (server: input -> output)
				this.is = this.socket.getInputStream();
				this.input = new ObjectInputStream(this.is);
				this.os = this.socket.getOutputStream();
				this.output = new ObjectOutputStream(this.os);
				
				while(this.socket.isConnected())
				{
					Message incomingMsg = (Message) this.input.readObject();
					if(incomingMsg != null)
					{
						Message.printMessage(incomingMsg); // test
						switch(incomingMsg.getMsgType())
						{
							case CONNECT:
							{
								System.out.println("Server: connect message received");
								
								Message mReply;
								
								// check if the connection can happen
								if(users.size() == maxNumUsers)
								{
									mReply = new Message(MessageType.CONNECT_FAILED, controller.getCurrentTimestamp(), "", "The room is full");
									this.output.writeObject(mReply);
								}
								else if(checkDuplicateNickname(incomingMsg.getNickname()))
								{
									mReply = new Message(MessageType.CONNECT_FAILED, controller.getCurrentTimestamp(), "", "Nickname '" + incomingMsg.getNickname() + "' already present");
									this.output.writeObject(mReply);
								}
								/*else if() // room is closed
								{
								
								}
								*/
								// the connection can be accepted
								else
								{
									// add user and writer to list
									User u = new User(incomingMsg.getNickname());
									users.add(u);
									writers.add(this.output);
									controller.addUser(u);
									
									// send back OK message, containing the updated user list
									mReply = new Message(MessageType.CONNECT_OK, controller.getCurrentTimestamp(), nickname, getUserList());
									this.output.writeObject(mReply);
									Message.printMessage(mReply); // test
									
									// add the message to the chat textArea
									controller.addToTextArea(mReply.getTimestamp() + " " + incomingMsg.getNickname() + " has joined the room");
									
									// forward to other users the new user joined
									mReply.setMsgType(MessageType.USER_JOINED);
									mReply.setNickname(incomingMsg.getNickname());
									forwardMessage(mReply);
								}
								
								break;
							}
							case CHAT:
							{
								// add the message to the chat textArea
								controller.addToTextArea(incomingMsg);
								
								// forward the chat message
								forwardMessage(incomingMsg);
								
								break;
							}
							case READY:
							{
								// upadate ready user
								controller.updateReady(incomingMsg.getNickname(), Boolean.parseBoolean(incomingMsg.getContent()));
								
								// update the user
								for(User u : users)
								{
									if(u.getNickname().equals(incomingMsg.getNickname()))
									{
										u.setReady(Boolean.parseBoolean(incomingMsg.getContent()));
										break;
									}
								}
								
								// forward the reaady to other users
								forwardMessage(incomingMsg);
								
								break;
							}
							case DISCONNECT:
							{
								// add the message to the chat textArea
								controller.addToTextArea(incomingMsg.getTimestamp() + " " + nickname + " has left the room");
								
								// forward disconnection to others
								
								// remove user and writer from the list
								
								// update controller list view
								controller.removeUser(nickname);
								
								// close the connection(?) & writer
								
								// stop the thread(?)
								
								break;
							}
							default:
							{
								System.out.println("Server: invalid message type received: " + incomingMsg.getMsgType().toString());
								break;
							}
						}
					}
				}
				
			} catch(SocketException e) {
				// "Connection reset" when the other endpoint disconnects
				e.printStackTrace();
			} catch (IOException e) {
				System.out.println("Errore stream");
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
			
		}
	}

	@Override
	public void sendMessage(String content)
	{
		Message msg = new Message(MessageType.CHAT, this.controller.getCurrentTimestamp(), this.nickname, content);
		this.controller.addToTextArea(msg);		
		
		// send the message to each user except the server
		for(int i = 1; i < this.users.size(); i++)
		{
			try {
				this.writers.get(i).writeObject(msg);
			} catch (IOException e) {
				// remove the writer at index i?
				e.printStackTrace();
			}
		}
	}
	
	@Override
	public void kickUser(String nickname)
	{
		// send kick to everyone (the nickname indicates which user is getting kicked)
		Message msg = new Message(MessageType.KICK, controller.getCurrentTimestamp(), this.nickname, "You have been kicked out from the server");
		for(int i = 1; i < this.writers.size(); i++)
		{
			try {
				this.writers.get(i).writeObject(msg);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		// remove user and writer
		int i;
		for(i = 0; i < this.users.size(); i++)
		{
			if(this.users.get(i).getNickname().equals(nickname))
			{
				this.users.remove(i);
				this.writers.remove(i);
				break;
			}
		}
		
		this.controller.removeUser(nickname);
		
		this.controller.addToTextArea(msg.getTimestamp() + " User " + msg.getNickname() + " has been kicked out");
	}
	
	// forward the message to each connected client, except the one that sent the message first
	private void forwardMessage(Message msg)
	{
		for(int i = 1; i < this.users.size(); i++)
		{
			if(!msg.getNickname().equals(this.users.get(i).getNickname()))
			{
				try {
					this.writers.get(i).writeObject(msg);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	private boolean checkDuplicateNickname(String nickname)
	{
		for(User u : this.users)
		{
			if(u.getNickname().equals(nickname))
				return true; // nickname already present
		}
		return false;
	}
	
	private String getUserList()
	{
		String list = "";
		for(int i = 0; i < this.users.size(); i++)
		{
			User u = this.users.get(i);
			list += u.getNickname() + "," + u.isReady();
			list += (i == this.users.size() - 1 ? "" : ";");
		}
		
		return list;
	}

	@Override
	public void sendClose()
	{
		Message msg = new Message(MessageType.KICK, controller.getCurrentTimestamp(), this.nickname, "You've been disconnected: server room closed");
		this.forwardMessage(msg);		
	}
}
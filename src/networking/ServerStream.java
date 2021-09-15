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
			System.out.println("Server (" + this.getId() + "): listening for connections on port " + PORT);
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
		
		public void closeSocket()
		{
			try {
				this.listener.close();
			} catch (IOException e) {
				System.out.println("IOException while closing the socket");
				e.printStackTrace();
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
			System.out.println("Server (" + this.getId() + "): connection accepted");
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
						System.out.println("Server (" + this.getId() + "): received " + incomingMsg.toString()); // test
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
								
								// forward the ready to other users
								forwardMessage(incomingMsg);
								
								break;
							}
							case DISCONNECT:
							{
								// add the message to the chat textArea
								controller.addToTextArea(incomingMsg.getTimestamp() + " " + incomingMsg.getNickname() + " has left the room");
								
								// forward disconnection to others
								forwardMessage(incomingMsg);
								
								// update controller list view
								controller.removeUser(incomingMsg.getNickname());
								
								// remove user and writer from the list
								for(int i = 1; i < users.size(); i++)
								{
									if(users.get(i).getNickname().equals(incomingMsg.getNickname()))
									{
										users.remove(i);
										writers.remove(i);
										break;
									}
								}
								
								// close the connection(?)
								socket.close();
								
								// stop the thread(?)
								//interrupt();
								
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
				
				// "java.net.SocketException: Socket closed" - received DISCONNECT
				if(e.getMessage().contains("Socket closed"))
					System.out.println("Socket closed");
				else e.printStackTrace();
			} catch (IOException e) {
				// if we close the socket when kick/disconnect is received, then:
				// when the server kicks an user, IOException is thrown because the thread which is listening, tries to read from the stream, but the socket has been closed from the other endpoint
				System.out.println("Errore stream (" + this.getId() + ")");
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
			
		}
	}
	
	private void sendMessage(Message message)
	{
		// send the message to each user except the server
		for(int i = 1; i < this.users.size(); i++)
		{
			try {
				this.writers.get(i).writeObject(message);
			} catch (IOException e) {
				System.out.println("IOException while trying to send message to client");
				e.printStackTrace();
			}
		}
	}
	
	@Override
	public void sendChatMessage(String content)
	{
		Message msg = new Message(MessageType.CHAT, this.controller.getCurrentTimestamp(), this.nickname, content);
		
		// send the chat message to everyone
		this.sendMessage(msg);
		
		// add the chat message to the textArea
		this.controller.addToTextArea(msg);
	}
	
	@Override
	public void sendKickUser(String kickNickname)
	{
		Message msg = new Message(MessageType.KICK, controller.getCurrentTimestamp(), kickNickname, "You have been kicked out from the server");
		
		// send kick to everyone (the nickname indicates which user is getting kicked)
		this.sendMessage(msg);
		
		// remove user and writer
		for(int i = 1; i < this.users.size(); i++)
		{
			if(this.users.get(i).getNickname().equals(kickNickname))
			{
				this.users.remove(i);
				this.writers.remove(i);
				break;
			}
		}
		
		// remove user from the listView
		this.controller.removeUser(kickNickname);
		
		// add kick message to the textArea
		this.controller.addToTextArea(msg.getTimestamp() + " " + msg.getNickname() + " has been kicked out");
	}
	
	@Override
	public void sendClose()
	{
		Message msg = new Message(MessageType.DISCONNECT, controller.getCurrentTimestamp(), this.nickname, "Server room closed");

		// send the message to each user except the server (NB: it's not a normal sendMessage
		for(int i = 1; i < this.users.size(); i++)
		{
			msg.setNickname(this.users.get(i).getNickname());
			try {
				this.writers.get(i).writeObject(msg);
			} catch (IOException e) {
				// remove the writer at index i?
				e.printStackTrace();
			}
		}
		
		// close the socket
		this.serverListener.closeSocket();
	}
	
	private void forwardMessage(Message msg)
	{
		// forward the message to each connected client, except the one that sent the message first
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
}
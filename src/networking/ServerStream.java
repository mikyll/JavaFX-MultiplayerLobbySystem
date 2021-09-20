package networking;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.BindException;
import java.net.DatagramSocket;
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
	private int minToStartGame = 2;
	private int maxNumUsers = 6;
	private Controller controller;
	private String nickname;
	private boolean users_rejoin;
	
	private ServerListener serverListener;
	
	private ArrayList<User> users;
	private ArrayList<ObjectOutputStream> writers;
	// private ArrayList<User> banned_users;
	
	public ServerStream(Controller controller, String nickname, int usersRequired, int maxCapacity, boolean rejoin)
	{
		this.controller = controller;
		this.nickname = nickname;
		this.minToStartGame = usersRequired;
		this.maxNumUsers = maxCapacity;
		this.users_rejoin = rejoin;
		
		this.users = new ArrayList<User>();
		User u = new User(nickname);
		u.setReady(true); // the server is always ready
		this.users.add(u);
		this.writers = new ArrayList<ObjectOutputStream>();
		this.writers.add(null);
		
		try {
			this.serverListener = new ServerListener(PORT);
			this.serverListener.start();
			this.controller.switchToServerRoom(); // if everything is ok, we can switch to Server Room View
		} catch (IOException e) {
			System.out.println("Server: ServerSocket creation failed");
			if(e instanceof BindException)
			{
				System.out.println("Server: another socket is already binded to this address and port");
				try(final DatagramSocket socket = new DatagramSocket()) {
					socket.connect(InetAddress.getByName("8.8.8.8"), 10002);
					String privateIP = socket.getLocalAddress().getHostAddress();
					this.controller.showAlert(AlertType.ERROR, "Room creation failed", "Another socket is already binded to " + privateIP + ":" + PORT);
				} catch (SocketException e1) {
					e.printStackTrace();
				} catch (UnknownHostException e1) {
					e.printStackTrace();
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
			} catch(SocketException e) {
				if(e.getMessage().contains("accept failed"))
					System.out.println("Server (" + this.getId() + "): stopped listening for connections");
				else e.printStackTrace();
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
						System.out.println("Server (" + this.getId() + "): received " + incomingMsg.toString()); // 
						switch(incomingMsg.getMsgType())
						{
							case CONNECT:
							{
								System.out.println("Server: connect message received");
								
								Message mReply = new Message();
								mReply.setTimestamp(controller.getCurrentTimestamp());
								
								// check if the connection can happen
								// the room is closed
								if(!controller.isRoomOpen())
								{
									mReply.setMsgType(MessageType.CONNECT_FAILED);
									mReply.setNickname("");
									mReply.setContent("The room is closed");
								}
								// the room is full
								else if(users.size() == maxNumUsers)
								{
									mReply.setMsgType(MessageType.CONNECT_FAILED);
									mReply.setNickname("");
									mReply.setContent("The room is full");
								}
								// the user is banned
								/*else if()
								{
									mReply.setMsgType(MessageType.CONNECT_FAILED);
									mReply.setNickname("");
									mReply.setContent("You've been banned from this room.");
								}*/
								// the user cannot rejoin after being kicked
								/*else if(!users_rejoin)
								{
									
								}*/
								// a username with the same nickname is already inside the room
								else if(checkDuplicateNickname(incomingMsg.getNickname()))
								{
									mReply.setMsgType(MessageType.CONNECT_FAILED);
									mReply.setNickname("");
									mReply.setContent("Nickname '" + incomingMsg.getNickname() + "' already present");
								}
								// the connection can be accepted
								else
								{
									System.out.println("InetAddress: " + this.socket.getRemoteSocketAddress() + ", " + this.socket.getInetAddress() + ", " + this.socket.getLocalAddress() + ", " + this.socket.getLocalSocketAddress()); // test
									// add user and writer to list
									User u = new User(incomingMsg.getNickname());
									users.add(u);
									writers.add(this.output);
									controller.addUser(u);
									
									// forward to other users the new user joined
									mReply.setMsgType(MessageType.USER_JOINED);
									mReply.setNickname(incomingMsg.getNickname());
									forwardMessage(mReply);
									
									// create OK message, containing the updated user list
									mReply.setMsgType(MessageType.CONNECT_OK);
									mReply.setNickname(nickname);
									mReply.setContent(getUserList());
									
									// add the message to the chat textArea
									controller.addToTextArea(mReply.getTimestamp() + " " + incomingMsg.getNickname() + " has joined the room");
								}
								// send back a reply for the CONNECT request
								this.output.writeObject(mReply);
								
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
								
								// enable start button if everyone is ready & there are enough users
								controller.enableStartGame(checkCanStartGame());
								
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
								
								// enable start button if everyone is ready
								controller.enableStartGame(checkCanStartGame());
								
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
		Message msg = new Message(MessageType.KICK, controller.getCurrentTimestamp(), kickNickname, "You have been kicked out from the room");
		
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
	
	@Override
	public boolean checkCanStartGame()
	{
		for(User u : this.users)
		{
			if(!u.isReady())
				return false;
		}
		return this.users.size() >= this.minToStartGame ? true : false;
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
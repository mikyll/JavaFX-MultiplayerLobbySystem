package networking;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;

import controller.Controller;
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
		
		this.serverListener = new ServerListener(PORT);
		this.serverListener.start();
		
	}
	
	private class ServerListener extends Thread {
		
		private ServerSocket listener;
		
		public ServerListener(int port)
		{
			System.out.println("Server: listening for connections on port " + PORT);
			try {
				this.listener = new ServerSocket(port);
			} catch (IOException e) {
				System.out.println("Server: ServerSocket creation failed");
				e.printStackTrace();
			}
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
					this.listener.close(); // check if it's ok (try/catch)
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
			System.out.println("Listening");
			try {
				this.is = this.socket.getInputStream();
				this.input = new ObjectInputStream(this.is);
				this.os = this.socket.getOutputStream();
				this.output = new ObjectOutputStream(this.os);
				
				// check qui oppure nel while (forse meglio nel while?)
				
				/*Message m = (Message) this.input.readObject();
				System.out.println("Server messaggio ricevuto");*/
				
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
								else 
								{
									// add user and writer to list
									User u = new User(incomingMsg.getNickname());
									users.add(u);
									writers.add(this.output);
									controller.addUser(u);
									
									// send back connect_ok, containing the updated user list
									mReply = new Message(MessageType.CONNECT_OK, controller.getCurrentTimestamp(), nickname, getUserList());
									this.output.writeObject(mReply);
									Message.printMessage(mReply); // test
									// add message to chat
									controller.addToTextArea(mReply.getTimestamp() + " " + incomingMsg.getNickname() + " has joined the room");
									
									// forward to other users the new user joined
									mReply.setMsgType(MessageType.USER_JOINED);
									mReply.setNickname(incomingMsg.getNickname());
									forwardMessage(mReply);
								}
								
								
								
								
								// update text area "User has successfully connected"
								// reply to the user with CONNECTION_OK
								// send updated USER_LIST to everyone
								
								break;
							}
							case CHAT:
							{
								// update textArea
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
								// remove the user
								// remove the writer
								
								// update controller list view
								
								// close the connection
								
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
	
	// forward the message to each connected client, except the one that sent the message first
	public void forwardMessage(Message msg)
	{
		for(int i = 1; i < this.users.size(); i++)
		{
			if(!msg.getNickname().equals(this.users.get(i).getNickname()))
				try {
					this.writers.get(i).writeObject(msg);
				} catch (IOException e) {
					e.printStackTrace();
				}
		}
	}
	
	public void sendUserList()
	{
		String list = "";
		for(int i = 0; i < this.users.size(); i++)
		{
			User u = this.users.get(i);
			list += u.getNickname() + "," + u.isReady();
			list += (i == this.users.size() - 1 ? ";" : "");
		}
		Message msg = new Message(MessageType.USER_LIST, this.controller.getCurrentTimestamp(), this.nickname, list);
		Message.printMessage(msg);
	}
	
	public void kickUser(String nickname)
	{
		// send kick to everyone (the nickname indicates which user is getting kicked)
		Message msg = new Message(MessageType.KICK, controller.getCurrentTimestamp(), nickname, "You have been kicked out from the server");
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
	
	/* "[] User Mikyll has been kicked from the room"
	 * "[] User Mikyll has joined the room"
	 * "[] Mikyll: ciao"
	 * "[] User Mikyll has left the room"
	 * "[] "-" user ready
	 * ""
	 */
}
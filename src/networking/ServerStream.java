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
		this.users.add(new User(nickname, 1));
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
		private User user;
		
		private ObjectInputStream input;
		private OutputStream os;
        private ObjectOutputStream output;
        private InputStream is;
        
		public Handler(Socket socket)
		{
			System.out.println("Server: connection accepted");
			this.socket = socket;
		}
		
		@Override
		public void run()
		{
			try {
				this.is = this.socket.getInputStream();
				this.input = new ObjectInputStream(this.is);
				this.os = this.socket.getOutputStream();
				this.output = new ObjectOutputStream(this.os);
				
				// test
				Message msg = (Message) this.input.readObject();
				printMessage(msg);
				
				
				
				// check qui oppure nel while (forse meglio nel while?)
				
				while(this.socket.isConnected())
				{
					Message incomingMsg = (Message) this.input.readObject();
					if(incomingMsg != null)
					{
						switch(incomingMsg.getMsgType())
						{
							case CONNECT:
							{
								// check if the connection can happen
								// add user and writer
								// update user list
								// update text area "User has successfully connected"
								// reply to the user with CONNECTION_OK
								// send updated USER_LIST to everyone
								
								break;
							}
							case CHAT:
							{
								// update textArea
								// forward the chat message
								
								break;
							}
							case READY:
							{
								// update user list
								// send the updated user list
								
								break;
							}
							case DISCONNECT:
							{
								// remove the user
								// remove the writer
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
					printMessage(incomingMsg);
				}
				
			} catch(SocketException socketException) {
				System.out.println("");
			} catch (IOException e) {
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
		this.controller.addToTextAreaChat(msg);		
	}
	
	// forward the message to each connected client, except the one that sent the message first
	public void forwardMessage(String nickname, Message msg)
	{
		
	}
	
	private boolean checkDuplicateNickname(String nickname)
	{
		for(User u : this.users)
		{
			if(u.getNickname().equals(nickname))
				return true;
		}
		return false;
	}
	
	private void printMessage(Message msg)
	{
		System.out.println(msg.getTimestamp() + " " + msg.getNickname() + "(" + msg.getMsgType().toString() + "): " + msg.getContent());
	}
	
	
}
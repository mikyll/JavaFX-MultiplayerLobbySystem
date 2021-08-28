package networking;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import model.User;
import model.chat.*;

import controller.Controller;

public class ClientStream implements IClient {
	private Controller controller;
	private ClientListener clientListener;
	
	private String address;
	private int port;
	private Socket socket;
	private String nickname;
	
	private ObjectOutputStream oos;
    private InputStream is;
    private ObjectInputStream input;
    private OutputStream outputStream;
	
	public ClientStream(Controller controller, String address, int port, String nickname)
	{
		this.controller = controller;
		this.address = address;
		this.port = port;
		
		this.clientListener = new ClientListener(address, port);
		this.clientListener.start();
	}
	
	// provare a usare una classe privata come questa
	private class ClientListener extends Thread {
		private Socket socket;
		private String address;
		private int port;
		
		private OutputStream os;
        private ObjectOutputStream output;
        private InputStream is;
        private ObjectInputStream input;
		
        // riguardare e sistemare!!!
		public ClientListener(String address, int port)
		{
			this.address = address;
			this.port = port;
			/*try {
				this.socket = new Socket(address, port);
				
			} catch (UnknownHostException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}*/
		}
		@Override
		public void run()
		{
			System.out.println("Client: run");
			try {
				this.socket = new Socket(address, port);
				
				this.os = this.socket.getOutputStream();
				this.output = new ObjectOutputStream(this.os);
				this.is = this.socket.getInputStream();
				this.input = new ObjectInputStream(this.is);
				
				System.out.println("Client: scrivo messaggio sulla socket");
				Message msg = new Message(MessageType.CONNECT, controller.getCurrentTimestamp(), nickname, "");
				this.output.writeObject(msg);
				System.out.println("Client: messaggio scritto sulla socket");
				
				
				while(this.socket.isConnected())
				{
					Message incomingMsg = (Message) this.input.readObject();
					if(incomingMsg != null)
					{
						switch(incomingMsg.getMsgType())
						{
							case CONNECT_FAILED:
							{
								// stop loading icon
								// show alert
								break;
							}
							case CONNECT_OK:
							{
								// show message in chat
								controller.addToTextArea(incomingMsg.getTimestamp() + " " + nickname + " has joined the room");
								controller.updateUserList(extractUserList(incomingMsg.getContent()));
								
								// get user list from OK
								
								// stop loading icon
								// switch view
								
								break;
							}
							case CHAT:
							{
								
							}
							case USER_JOINED:
							{
								controller.addToTextArea(incomingMsg.getTimestamp() + " " + incomingMsg.getNickname() + " has joined the room");;
								controller.addUser(new User(incomingMsg.getNickname()));
								
								break;
							}
							default:
							{
								System.out.println("Client: invalid message type received: " + incomingMsg.getMsgType());
								break;
							}
						}
					}
				}
			} catch(SocketException e) {
				System.out.println("Socket exception");
				e.printStackTrace();
			} catch (IOException e) {
				System.out.println("Errore stream");
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
		}
	}
	/*@Override
	public void run()
	{
		try {
			this.socket = new Socket(this.address, this.port);
			
			this.outputStream = this.socket.getOutputStream();
			this.oos = new ObjectOutputStream(this.outputStream);
			this.is = this.socket.getInputStream();
            this.input = new ObjectInputStream(this.is);
        } catch (IOException e) {
        	System.out.println("Client: connection to server failed");
        }
		
		// ricezione primo messaggio mi deve dire l'ID
		try {
			Message msg = (Message) this.input.readObject();
			if(msg.getMsgType().equals(MessageType.CONNECT_FAILED))
			{
				// alert
			}
		} catch (ClassNotFoundException e1) {
			e1.printStackTrace();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		
		System.out.println("Connection accepted " + this.socket.getInetAddress() + ":" + this.socket.getPort());
		
		try {
			this.connectToServer();
			System.out.println("Client: input and output streams ready");
			
			// message receiving loop
			while (socket.isConnected()) {
                Message message = (Message) this.input.readObject();

                if (message != null) {
                	System.out.println("Client: message recieved. " + message.getMsgType().toString() + ", " + message.getTimestamp() + ", " + message.getNickname() + ", " + message.getContent());
                	switch (message.getMsgType()) {
                        case CONNECT_FAILED:
                        {
                        	// casi: il server è pieno, la room è private, il nickname è già utilizzato, oppure c'è già un client connesso con tale IP
                        	this.controller.connectionFailed(message);
                            break;
                        }
                        case CONNECT_OK:
                        {
                        	// successfully connected to server room, show message in chat and send players List
                        	this.controller.switchToChatC();
                            break;
                        }
                        case USER_LIST:
                        {
                        	// update user list
                        	this.controller.updateUserList(message);
                        	break;
                        }
                        case CHAT:
                        {
                        	this.controller.addToTextAreaChat(message);
                        	break;
                        }
                        case KICK:
                        {
                        	// server kicked the client from the room
                        	break;
                        }
                        case START_GAME:
                        {
                        	// server started the game
                        	break;
                        }
                        default:
                        {
                        	break;
                        }
                    }
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            // controller.logoutScene();
            // put there the disconnection(?)
        }
	}*/
	
	public void connectToServer() throws IOException
	{
		Message msg = new Message(MessageType.CONNECT, this.controller.getCurrentTimestamp(), this.nickname, "");
		this.oos.writeObject(msg);
	}
	

	
	// sends a message to the server
	public void sendMessage(String content)
	{
		Message msg = new Message(MessageType.CHAT, this.controller.getCurrentTimestamp(), this.nickname, content);
		try {
			this.oos.writeObject(msg);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void sendReady(boolean ready)
	{
		Message msg = new Message(MessageType.READY, this.controller.getCurrentTimestamp(), this.nickname, "" + ready);
		try {
			this.oos.writeObject(msg);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public List<User> extractUserList(String s)
	{
		List<User> list = new ArrayList<User>();
		
		String[] sTmp = s.split(";");
		for(int i = 0; i < sTmp.length; i++)
		{
			String[] sNickReady = sTmp[i].split(",");
			User u = new User(sNickReady[0]);
			u.setReady(Boolean.parseBoolean(sNickReady[1]));
			list.add(u);
		}
		
		return list;
	}
}

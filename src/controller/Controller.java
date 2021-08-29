package controller;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import model.User;
import model.chat.Message;
import networking.ClientStream;
import networking.IClient;
import networking.IServer;
import networking.ServerStream;

public class Controller {
	// login
	@FXML private VBox vboxLogin;
	@FXML private TextField textFieldNickname;
	@FXML private Button buttonCNR;
	@FXML private TextField textFieldIP;
	@FXML private Button buttonJER;
	@FXML private HBox hboxC; // hbox connection
	
	// client
	@FXML private VBox vboxChatClient;
	@FXML private TextArea textAreaChatC;
	@FXML private TextField textFieldChatC;
	@FXML private Button buttonChatC;
	@FXML private Button buttonReady;
	private IClient client;
	
	// server
	@FXML private VBox vboxChatServer;
	@FXML private Label labelServerIP;
	@FXML private TextArea textAreaChatS;
	@FXML private TextField textFieldChatS;
	@FXML private Button buttonChatS;
	private IServer server;
	
	@FXML private ListView<String> listViewUsersC;
	@FXML private ListView<Label> listViewReadyC;
	
	@FXML private ListView<String> listViewUsersS;
	@FXML private ListView<Label> listViewReadyS;
	@FXML private ListView<Button> listViewKickS;
	
	private SimpleDateFormat tformatter;
	
	public Controller() {}
	
	public void initialize()
	{
		this.tformatter = new SimpleDateFormat("[HH:mm:ss]");
		this.switchToMP();
		this.showConnectingBox(false);
	}
	
	@FXML public void selectCNR(ActionEvent event) 
	{
		this.textAreaChatS.setText(this.getCurrentTimestamp() + " " + this.textFieldNickname.getText() + " created the room");
		this.setServerAddress();
		
		// create new room -> start server
		this.server = new ServerStream(this, this.textFieldNickname.getText());
		
		this.listViewUsersS.getItems().add(this.textFieldNickname.getText());
		Label l;
		Button b;
		// popolate the listView with the controls but set them invisibile
		for(int i = 0; i < 6; i++)
		{
			l = new Label();
			l.setPrefSize(25, 25);
			l.setStyle("-fx-background-color: red");
			l.setVisible(false);
			
			this.listViewReadyS.getItems().add(l);
			b = new Button("Kick");
			b.setPrefSize(70, 20);
			b.setStyle("-fx-font-size: 15.0");
			b.setVisible(false);
			b.setOnAction(this::kickUser);
			this.listViewKickS.getItems().add(b);
		}
		
		this.switchToChatS();
	}
	
	@FXML public void selectJER(ActionEvent event) 
	{
		// connect to existing room -> start client
		this.client = new ClientStream(this, this.textFieldIP.getText(), 9001, this.textFieldNickname.getText());
		
		Label l;
		Button b;
		// popolate the listView with the controls but set them invisibile
		for(int i = 0; i < 6; i++)
		{
			l = new Label();
			l.setPrefSize(25, 25);
			l.setStyle("-fx-background-color: red");
			l.setVisible(false);
			
			this.listViewReadyC.getItems().add(l);
		}
		
		this.showConnectingBox(true);
	}
	@FXML public void toggleReady(ActionEvent event)
	{
		int i;
		for(i = 0; i < this.listViewUsersC.getItems().size(); i++)
		{
			if(this.listViewUsersC.getItems().get(i).equals(this.textFieldNickname.getText()))
				break;
		}
		if(this.buttonReady.getText().equalsIgnoreCase("Ready"))
		{
			this.buttonReady.setText("Not ready");
			this.buttonReady.setStyle("-fx-background-color: red");
			this.client.sendReady(false);
			this.listViewReadyC.getItems().get(i).setStyle("-fx-background-color: red");;
		}
		else
		{
			this.buttonReady.setText("Ready");
			this.buttonReady.setStyle("-fx-background-color: lime");
			this.client.sendReady(true);
			this.listViewReadyC.getItems().get(i).setStyle("-fx-background-color: lime");;
			
		}
		// set a 5 sec timer that disables the button, so that users can't spam the toggle
	}
	@FXML public void sendMessageC(ActionEvent event) 
	{
		String msg = this.textFieldChatC.getText();
		if(!msg.isEmpty() && !msg.isBlank())
		{
			this.client.sendMessage(msg);
		}
		this.textFieldChatC.setText("");
	}
	@FXML public void kickUser(ActionEvent event)
	{
		System.out.println(event.getSource());
	}
	@FXML public void sendMessageS(ActionEvent event) 
	{
		String msg = this.textFieldChatS.getText();
		if(!msg.isEmpty() && !msg.isBlank())
		{
			this.server.sendMessage(msg);
		}
		this.textFieldChatS.setText("");
	}
	
	public void addToTextArea(String text)
	{
		// client
		if(this.client != null)
		{
			this.textAreaChatC.setText(this.textAreaChatC.getText() + "\n" + text);
			
		}
		// server
		else if(this.server != null)
		{
			this.textAreaChatS.setText(this.textAreaChatS.getText() + "\n" + text);
		}
	}
	public void addToTextArea(Message message)
	{
		// client
		if(this.client != null)
		{
			this.textAreaChatC.setText(this.textAreaChatC.getText() + "\n" + message.getTimestamp() + " " + message.getNickname() + ": " + message.getContent());
			
		}
		// server
		else if(this.server != null)
		{
			this.textAreaChatS.setText(this.textAreaChatS.getText() + "\n" + message.getTimestamp() + " " + message.getNickname() + ": " + message.getContent());
			
		}
	}
	public void switchToMP()
	{
		this.vboxChatClient.setVisible(false);
		this.vboxChatServer.setVisible(false);
		this.vboxLogin.setVisible(true);
	}
	public void switchToChatC()
	{
		this.vboxLogin.setVisible(false);
		this.vboxChatClient.setVisible(true);
	}
	public void switchToChatS()
	{
		this.vboxLogin.setVisible(false);
		this.vboxChatServer.setVisible(true);
	}
	
	
	
	public void connectionFailed(Message message)
	{
		System.out.println("Client: connection to server failed. " + message.getContent());
		
		// alert (show reply message content, example room full)
		// set not visibile loading hbox
	}
	public void updateReady(String nickname, boolean ready)
	{
		// update user list
		if(this.client != null)
		{
			for(int i = 0; i < this.listViewUsersC.getItems().size(); i++)
			{
				if(nickname.equals(this.listViewUsersC.getItems().get(i)))
				{
					this.listViewReadyC.getItems().get(i).setStyle(ready ? "-fx-background-color: lime" : "-fx-background-color: red");
					break;
				}
			}
		}
		else if(this.server != null)
		{
			for(int i = 0; i < this.listViewUsersS.getItems().size(); i++)
			{
				if(nickname.equals(this.listViewUsersS.getItems().get(i)))
				{
					this.listViewReadyS.getItems().get(i).setStyle(ready ? "-fx-background-color: lime" : "-fx-background-color: red");
					break;
				}
			}
		}
	}
	public void kickedFromServer(Message message)
	{
		// alert
		// switch view
	}
	public void setServerAddress()
	{
		try {
			this.labelServerIP.setText("Server IP address: " + InetAddress.getLocalHost().toString().split("/")[1]);
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
	}
	public String getCurrentTimestamp()
	{
		Date date = new Date(System.currentTimeMillis());
		String timestamp = this.tformatter.format(date);
		
		return timestamp;
	}
	public void showConnectingBox(boolean value)
	{
		this.hboxC.setVisible(value);
	}
	
	public void addUser(User u)
	{
		Platform.runLater(() -> {
			if(this.client != null)
			{
				this.listViewUsersC.getItems().add(u.getNickname());
				int i = this.listViewUsersC.getItems().size();
				Label l = this.listViewReadyC.getItems().get(i - 1);
				l.setStyle("-fx-background-color: red");
				l.setVisible(true);
			}
			else if(this.server != null)
			{
				this.listViewUsersS.getItems().add(u.getNickname());
				int i = this.listViewUsersS.getItems().size();
				Label l = this.listViewReadyS.getItems().get(i - 1);
				l.setStyle("-fx-background-color: red");
				l.setVisible(true);
				this.listViewKickS.getItems().get(i - 1).setVisible(true);
			}
		});
		
	}
	public void removeUser(User u)
	{
		if(this.client != null)
		{
			for(int i = 0; i < this.listViewUsersC.getItems().size(); i++)
			{
				if(this.listViewUsersC.getItems().get(i).equals(u.getNickname()))
				{
					this.listViewUsersC.getItems().remove(i);
					Label l = this.listViewReadyC.getItems().get(i);
					l.setStyle("-fx-background-color: red");
					l.setVisible(false);
				}
			}
		}
		else if(this.server != null)
		{
			for(int i = 0; i < this.listViewUsersS.getItems().size(); i++)
			{
				if(this.listViewUsersS.getItems().get(i).equals(u.getNickname()))
				{
					this.listViewUsersS.getItems().remove(i);
					Label l = this.listViewReadyS.getItems().get(i);
					l.setStyle("-fx-background-color: red");
					l.setVisible(false);
					this.listViewKickS.getItems().get(i).setVisible(false);
				}
			}
		}
	}
	public void updateUserList(List<User> users)
	{
		if(this.client != null)
		{
			Platform.runLater(() -> {
				for(int i = 0; i < users.size(); i++)
				{
					this.listViewUsersC.getItems().add(users.get(i).getNickname());
					Label l = this.listViewReadyC.getItems().get(i);
					l.setStyle(users.get(i).isReady() ? "-fx-background-color: lime" : "-fx-background-color: red");
					l.setVisible(i == 0 ? false : true);
				}
			});
		}
		else if(this.server != null)
		{
			
		}
	}
	public void showAlert(Message msg)
	{
		Platform.runLater(() -> {
				String header;
				switch(msg.getMsgType())
				{
					case CONNECT_FAILED:
					{
						header = "Connection failed";
						break;
					}
					case KICK:
					{
						header = "Disconnected from server";
						break;
					}
					default:
					{
						header = "";
					}
				}
				Alert a = new Alert(AlertType.INFORMATION);
				a.setTitle("Information Dialog");
				a.setHeaderText(header);
				a.setContentText(msg.getContent());
				a.show();
		});
		
	}
}

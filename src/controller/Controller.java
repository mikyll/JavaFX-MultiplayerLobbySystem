package controller;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;

import javafx.application.Platform;
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
	
	private static final Pattern PATTERN_NICKNAME = Pattern.compile("^[a-zA-Z0-9]{3,15}$");
	private static final Pattern PATTERN_IP = Pattern.compile("^(([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.){3}([01]?\\d\\d?|2[0-4]\\d|25[0-5])$");
	
	// login
	@FXML private VBox vboxBack;
	@FXML private VBox vboxLogin;
	@FXML private TextField textFieldNickname;
	@FXML private Button buttonCNR;
	@FXML private TextField textFieldIP;
	@FXML private Button buttonJER;
	@FXML private Label labelErrorIP;
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
		
		this.buttonCNR.setDisable(true);
		this.buttonJER.setDisable(true);
		this.labelErrorIP.setVisible(false);
	}
	
	@FXML public void validateNickname()
	{
		// nickname OK
		if(checkNickname(this.textFieldNickname.getText()))
		{
			this.buttonCNR.setDisable(false);
			// & address OK
			if(this.checkIP(this.textFieldIP.getText()))
				this.buttonJER.setDisable(false);
		}
		// nickname NOT
		else
		{
			this.buttonCNR.setDisable(true);
			this.buttonJER.setDisable(true);
		}
	}
	private boolean checkNickname(String text)
	{
		// if OK
		if(PATTERN_NICKNAME.matcher(text).matches())
			return true;
		// if NOT
		else return false;
	}
	@FXML public void validateAddress()
	{
		// address OK, nickname OK
		if(checkIP(this.textFieldIP.getText()) && checkNickname(this.textFieldNickname.getText()))
		{
			this.buttonJER.setDisable(false);
			this.labelErrorIP.setVisible(false);
		}
		// address NOT, nickname OK
		else if(!checkIP(this.textFieldIP.getText()))
		{
			this.buttonJER.setDisable(true);
			this.labelErrorIP.setVisible(true);
		}
		// address OK, nickname NOT
		else
		{
			this.labelErrorIP.setVisible(false);
		}
	}
	private boolean checkIP(String text)
	{
		// if OK
		if(PATTERN_IP.matcher(text).matches())
			return true;
		// if NOT
		else return false;
	}
	
	@FXML public void selectCNR(ActionEvent event) 
	{
		this.vboxBack.setVisible(true);
		
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
	}
	
	@FXML public void selectJER(ActionEvent event) 
	{
		this.vboxBack.setVisible(true);
		
		// connect to existing room -> start client
		this.client = new ClientStream(this, this.textFieldIP.getText(), 9001, this.textFieldNickname.getText());
		
		Label l;
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
	
	@FXML public void goBack(ActionEvent event)
	{
		this.vboxBack.setVisible(false);
		this.switchToMP();
		
		if(this.client != null)
		{
			this.client.sendClose();
		}
		else if(this.server != null)
		{
			this.server.sendClose();
		}
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
			this.listViewReadyC.getItems().get(i).setStyle("-fx-background-color: red");
		}
		else
		{
			this.buttonReady.setText("Ready");
			this.buttonReady.setStyle("-fx-background-color: lime");
			this.client.sendReady(true);
			this.listViewReadyC.getItems().get(i).setStyle("-fx-background-color: lime");
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
		this.vboxBack.setVisible(false);
		
		// get the button index
		for(int i = 1; i < this.listViewUsersS.getItems().size(); i++)
		{
			if(this.listViewKickS.getItems().get(i).equals(event.getTarget()))
			{
				System.out.println("Server: kicked user " + this.listViewUsersS.getItems().get(i));
				
				// send Kick message
				this.server.kickUser(this.listViewUsersS.getItems().get(i));
				break;
			}
		}
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
	private void setServerAddress()
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
	public void removeUser(String nickname)
	{
		Platform.runLater(() -> {
			if(this.client != null)
			{
				for(int i = 0; i < this.listViewUsersC.getItems().size(); i++)
				{
					if(this.listViewUsersC.getItems().get(i).equals(nickname))
					{
						this.listViewUsersC.getItems().remove(i);
						break;
					}
				}
				Label l = this.listViewReadyC.getItems().get(this.listViewUsersC.getItems().size());
				l.setStyle("-fx-background-color: red");
				l.setVisible(false);
			}
			else if(this.server != null)
			{
				for(int i = 0; i < this.listViewUsersS.getItems().size(); i++)
				{
					if(this.listViewUsersS.getItems().get(i).equals(nickname))
					{
						this.listViewUsersS.getItems().remove(i);
						break;
					}
				}
				Label l = this.listViewReadyS.getItems().get(this.listViewUsersS.getItems().size());
				l.setStyle("-fx-background-color: red");
				l.setVisible(false);
				this.listViewKickS.getItems().get(this.listViewUsersS.getItems().size()).setVisible(false);
			}
		});
		
	}
	public void updateUserList(List<User> users)
	{
		Platform.runLater(() -> {
			if(this.client != null)
			{
				for(int i = 0; i < users.size(); i++)
				{
					this.listViewUsersC.getItems().add(users.get(i).getNickname());
					Label l = this.listViewReadyC.getItems().get(i);
					l.setStyle(users.get(i).isReady() ? "-fx-background-color: lime" : "-fx-background-color: red");
					l.setVisible(i == 0 ? false : true);
				}
			}
			else if(this.server != null)
			{
				// for the moment it's never used from the server
			}
		});
	}
	public void showAlert(AlertType aType, String header, String content)
	{
		Platform.runLater(() -> {
			Alert a = new Alert(aType);
			a.setTitle("Information Dialog");
			a.setHeaderText(header);
			a.setContentText(content);
			a.show();
		});
	}
	
	public void closeConnection()
	{
		if(this.client != null)
		{
			this.client.sendClose();
		}
		else if(this.server != null)
		{
			this.server.sendClose();
		}
        Platform.exit();
        System.exit(0);
    }
}

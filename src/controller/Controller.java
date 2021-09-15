package controller;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
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
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Paint;
import model.User;
import model.chat.Message;
import networking.ClientStream;
import networking.IClient;
import networking.IServer;
import networking.ServerStream;

public class Controller {
	private static final int ROOM_CAPACITY = 6;
	private static final int USERS_REQUIRED_TO_START = 2;
	
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
	@FXML private ListView<HBox> listViewUsersC;
	private IClient client;
	private ArrayList<Label> listUsernameC;
	private ArrayList<Label> listReadyC;
	private ArrayList<ImageView> listImage;
	
	// server
	@FXML private VBox vboxChatServer;
	@FXML private Label labelServerIP;
	@FXML private TextArea textAreaChatS;
	@FXML private TextField textFieldChatS;
	@FXML private Button buttonChatS;
	@FXML private Button buttonStartGame;
	@FXML private Button buttonOpenClose;
	@FXML private Label labelOpenClose;
	@FXML private ListView<HBox> listViewUsersS;
	private IServer server;
	private ArrayList<Label> listUsernameS;
	private ArrayList<Label> listReadyS;
	private ArrayList<Button> listKick;
	
	private int connectedUsers;
	
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
		
		this.listUsernameC = new ArrayList<Label>();
		this.listUsernameS = new ArrayList<Label>();
		this.listReadyC = new ArrayList<Label>();
		this.listReadyS = new ArrayList<Label>();
		this.listImage = new ArrayList<ImageView>();
		this.listKick = new ArrayList<Button>();
		
		// popolate the ListView with HBox and set them not visible
		for(int i = 0; i < ROOM_CAPACITY; i++)
		{
			// hbox client
			HBox hbox = new HBox();
			hbox.setPrefSize(280, 25);
			hbox.setSpacing(20);
			hbox.setVisible(false);
			// username client
			Label l = new Label("");
			l.setPrefWidth(150);
			l.setTextFill(Paint.valueOf("white"));
			hbox.getChildren().add(l);
			this.listUsernameC.add(l);
			// ready client
			l = new Label("");
			l.setPrefSize(25, 25);
			l.setStyle("-fx-background-color: red");
			l.setVisible(i == 0 ? false : true);
			hbox.getChildren().add(l);
			this.listReadyC.add(l);
			// identifier image
			ImageView iv = new ImageView();
			try {
				iv.setImage(new Image(new FileInputStream("src/view/icon-user.png")));
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
			iv.resize(25, 25);
			hbox.getChildren().add(iv);
			this.listImage.add(iv);
			
			this.listViewUsersC.getItems().add(hbox);
			
			// hbox server
			hbox = new HBox();
			hbox.setPrefSize(300, 25);
			hbox.setSpacing(20);
			hbox.setVisible(false);
			// username server
			l = new Label("");
			l.setPrefWidth(150);
			l.setTextFill(Paint.valueOf("white"));
			hbox.getChildren().add(l);
			this.listUsernameS.add(l);
			// ready server
			l = new Label("");
			l.setPrefSize(25, 25);
			l.setStyle(i == 0 ? "-fx-background-color: lime" : "-fx-background-color: red");
			l.setVisible(i == 0 ? false : true);
			hbox.getChildren().add(l);
			this.listReadyS.add(l);
			// kick button
			Button b = new Button("Kick");
			b.setPrefSize(70, 20);
			b.setStyle("-fx-font-size: 15.0");
			b.setOnAction(this::kickUser);
			b.setVisible(i == 0 ? false : true); // NB: visible only if i >= 1
			hbox.getChildren().add(b);
			this.listKick.add(b);
			
			this.listViewUsersS.getItems().add(hbox);
		}
		
		connectedUsers = 0;
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
		this.textAreaChatS.setText(this.getCurrentTimestamp() + " " + this.textFieldNickname.getText() + " created the room");
		this.setServerAddress();
		
		// create new room -> start server
		this.server = new ServerStream(this, this.textFieldNickname.getText(), USERS_REQUIRED_TO_START);
		this.client = null;
		
		// reset the user list
		this.resetList();
		
		// reset buttons
		this.buttonStartGame.setDisable(true);
		this.buttonOpenClose.setText("Open");
		this.labelOpenClose.setStyle("-fx-background-color: lime");
		
		// set the first list element (the server) to visibile
		this.listUsernameS.get(0).setText(this.textFieldNickname.getText());
		this.listViewUsersS.getItems().get(0).setVisible(true);
		
		this.connectedUsers = 1;
	}
	
	@FXML public void selectJER(ActionEvent event) 
	{
		// connect to existing room -> start client
		this.client = new ClientStream(this, this.textFieldIP.getText(), 9001, this.textFieldNickname.getText());
		this.server = null;
		
		// reset the user list
		this.resetList();
		
		// reset ready button
		this.buttonReady.setText("Not ready");
		this.buttonReady.setStyle("-fx-background-color: red");
		
		// show loading box
		this.showConnectingBox(true);
	}
	
	@FXML public void goBack(ActionEvent event)
	{
		this.vboxBack.setVisible(false);
		this.switchToMP();
		
		this.closeConnection();
	}
	
	@FXML public void toggleReady(ActionEvent event)
	{
		if(this.buttonReady.getText().equalsIgnoreCase("Ready"))
		{
			this.buttonReady.setText("Not ready");
			this.buttonReady.setStyle("-fx-background-color: red");
			this.client.sendReady(false);
			this.updateReady(this.textFieldNickname.getText(), false);
		}
		else
		{
			this.buttonReady.setText("Ready");
			this.buttonReady.setStyle("-fx-background-color: lime");
			this.client.sendReady(true);
			this.updateReady(this.textFieldNickname.getText(), true);
		}
		// TO-DO: set a 5 sec timer that disables the button, so that users can't spam the toggle
	}
	@FXML public void sendMessageC(ActionEvent event) 
	{
		String msg = this.textFieldChatC.getText();
		if(!msg.isEmpty() && !msg.isBlank())
		{
			this.client.sendChatMessage(msg);
		}
		this.textFieldChatC.setText("");
	}
	@FXML public void sendMessageS(ActionEvent event) 
	{
		String msg = this.textFieldChatS.getText();
		if(!msg.isEmpty() && !msg.isBlank())
		{
			this.server.sendChatMessage(msg);
		}
		this.textFieldChatS.setText("");
	}
	@FXML public void enterChatHandle(KeyEvent e)
	{
		if(e.getCode().equals(KeyCode.ENTER))
		{
			if(this.client != null)
			{
				String msg = this.textFieldChatC.getText();
				if(!msg.isEmpty() && !msg.isBlank())
				{
					this.client.sendChatMessage(msg);
				}
				this.textFieldChatC.setText("");
			}
			else if(this.server != null)
			{
				String msg = this.textFieldChatS.getText();
				if(!msg.isEmpty() && !msg.isBlank())
				{
					this.server.sendChatMessage(msg);
				}
				this.textFieldChatS.setText("");
			}
		}
	}
	@FXML public void kickUser(ActionEvent event)
	{
		// get the button index
		for(int i = 1; i < this.connectedUsers; i++)
		{
			if(this.listKick.get(i).equals(event.getTarget()))
			{
				System.out.println("Server: kicked user " + this.listUsernameS.get(i).getText());
				
				// remove user from the listView
				this.removeUser(this.listUsernameS.get(i).getText());
				
				// add kick message to the textArea
				this.addToTextArea(this.getCurrentTimestamp() + " " + this.listUsernameS.get(i).getText() + " has been kicked out");
				
				// send Kick message
				this.server.sendKickUser(this.listUsernameS.get(i).getText());
				
				break;
			}
		}
	}
	@FXML public void toggleOpenClose(ActionEvent event)
	{
		// close the room
		if(this.buttonOpenClose.getText().equalsIgnoreCase("Open"))
		{
			this.buttonOpenClose.setText("Closed");
			this.labelOpenClose.setStyle("-fx-background-color: red");
		}
		// open the room
		else
		{
			this.buttonOpenClose.setText("Open");
			this.labelOpenClose.setStyle("-fx-background-color: lime");
		}
	}
	@FXML public void startGame(ActionEvent event)
	{
		System.out.println("Start game");
	}
	
	public void addToTextArea(String text)
	{
		// client
		if(this.client != null)
		{
			if(this.textAreaChatC.getText().isEmpty())
				this.textAreaChatC.setText(text);
			else this.textAreaChatC.setText(this.textAreaChatC.getText() + "\n" + text);
		}
		// server
		else if(this.server != null)
		{
			this.textAreaChatS.setText(this.textAreaChatS.getText() + "\n" + text);
		}
	}
	public void addToTextArea(Message message)
	{
		this.addToTextArea(message.getTimestamp() + " " + message.getNickname() + ": " + message.getContent());
	}
	public void switchToMP()
	{
		this.vboxBack.setVisible(false);
		this.vboxChatClient.setVisible(false);
		this.vboxChatServer.setVisible(false);
		this.vboxLogin.setVisible(true);
	}
	public void switchToChatC()
	{
		this.vboxLogin.setVisible(false);
		this.vboxChatClient.setVisible(true);
		this.vboxBack.setVisible(true);
	}
	public void switchToChatS()
	{
		this.vboxLogin.setVisible(false);
		this.vboxChatServer.setVisible(true);
		this.vboxBack.setVisible(true);
	}
	
	public void updateReady(String nickname, boolean ready)
	{
		if(this.client != null)
		{
			for(int i = 0; i < this.listViewUsersC.getItems().size(); i++)
			{
				if(nickname.equals(this.listUsernameC.get(i).getText()))
				{
					this.listReadyC.get(i).setStyle(ready ? "-fx-background-color: lime" : "-fx-background-color: red");
					break;
				}
			}
		}
		else if(this.server != null)
		{
			for(int i = 0; i < this.listViewUsersS.getItems().size(); i++)
			{
				if(nickname.equals(this.listUsernameS.get(i).getText()))
				{
					this.listReadyS.get(i).setStyle(ready ? "-fx-background-color: lime" : "-fx-background-color: red");
					break;
				}
			}
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
		System.out.println("Controller: add user"); // test
		Platform.runLater(() -> {
			if(this.client != null)
			{
				this.listUsernameC.get(this.connectedUsers).setText(u.getNickname());
				this.listViewUsersC.getItems().get(this.connectedUsers).setVisible(true);
				this.connectedUsers++;
			}
			else if(this.server != null)
			{
				this.listUsernameS.get(this.connectedUsers).setText(u.getNickname());
				this.listViewUsersS.getItems().get(this.connectedUsers).setVisible(true);
				this.connectedUsers++;
				
				this.buttonStartGame.setDisable(true); // when a new user connects it's always not ready
			}
		});
		
	}
	public void removeUser(String nickname)
	{
		System.out.println("Controller: remove user"); // test
		Platform.runLater(() -> {
			boolean found = false;
			if(this.client != null)
			{
				// NB: we have to move by one position back every user, to fill the empty space left by the removed one
				for(int i = 1; i < this.connectedUsers; i++)
				{
					if(found)
					{
						this.listUsernameC.get(i - 1).setText(this.listUsernameC.get(i).getText());
						this.listReadyC.get(i - 1).setStyle(this.listReadyC.get(i).getStyle());
						this.listImage.get(i - 1).setVisible(this.listImage.get(i).isVisible());
					}
					if(this.listUsernameC.get(i).getText().equals(nickname))
						found = true;
				}
				this.listViewUsersC.getItems().get(this.connectedUsers - 1).setVisible(false);
				this.listUsernameC.get(this.connectedUsers - 1).setText("");
				this.listReadyC.get(this.connectedUsers - 1).setStyle("-fx-background-color: red");
				this.listImage.get(this.connectedUsers - 1).setVisible(false);
				this.connectedUsers--;
			}
			else if(this.server != null)
			{
				// NB: we have to move by one position back every user, to fill the empty space left by the removed one
				for(int i = 1; i < this.connectedUsers; i++)
				{
					if(found)
					{
						this.listUsernameS.get(i - 1).setText(this.listUsernameS.get(i).getText());
						this.listReadyS.get(i - 1).setStyle(this.listReadyS.get(i).getStyle());
					}
					if(this.listUsernameS.get(i).getText().equals(nickname))
						found = true;
				}
				this.listViewUsersS.getItems().get(this.connectedUsers - 1).setVisible(false);
				this.listUsernameS.get(this.connectedUsers - 1).setText("");
				this.listReadyS.get(this.connectedUsers - 1).setStyle("-fx-background-color: red");
				this.connectedUsers--;
				
				this.buttonStartGame.setDisable(!this.server.checkCanStartGame());
			}
		});
	}
	
	public void updateUserList(List<User> users)
	{
		System.out.println("Controller: update user list"); // test
		Platform.runLater(() -> {
			if(this.client != null)
			{
				for(int i = 0; i < users.size(); i++)
				{
					User u = users.get(i);
					this.listUsernameC.get(i).setText(u.getNickname());
					this.listViewUsersC.getItems().get(i).setVisible(true);
					this.listReadyC.get(i).setStyle(u.isReady() ? "-fx-background-color: lime" : "-fx-background-color: red");
					this.listImage.get(i).setVisible(this.textFieldNickname.getText().equals(users.get(i).getNickname()) ? true : false);
				}
				this.connectedUsers = users.size();
			}
			else if(this.server != null)
			{
				// for the moment it's never used from the server
			}
		});
	}
	
	public boolean isRoomOpen()
	{
		return this.buttonOpenClose.getText().equalsIgnoreCase("Open") ? true : false;
	}
	
	public void enableStartGame(boolean value)
	{
		this.buttonStartGame.setDisable(!value);
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
			this.client = null;
		}
		else if(this.server != null)
		{
			this.server.sendClose();
			this.server = null;
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
	
	private void resetList()
	{
		System.out.println("Controller: reset user list"); // test
		if(this.client != null)
		{
			for(int i = 0; i < ROOM_CAPACITY; i++)
			{
				this.listViewUsersC.getItems().get(i).setVisible(false);
				this.listUsernameC.get(i).setText("");
				this.listReadyC.get(i).setStyle("-fx-background-color: red");
				this.listImage.get(i).setVisible(false);
			}
		}
		else if(this.server != null)
		{
			for(int i = 0; i < ROOM_CAPACITY; i++)
			{
				this.listViewUsersS.getItems().get(i).setVisible(false);
				this.listUsernameS.get(i).setText("");
				this.listReadyS.get(i).setStyle("-fx-background-color: red");
			}
		}
	}
}

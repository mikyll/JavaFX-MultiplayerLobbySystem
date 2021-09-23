package controller;

import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Paint;
import javafx.scene.text.TextFlow;
import model.NavState;
import model.User;
import model.chat.Message;
import networking.ClientStream;
import networking.IClient;
import networking.IServer;
import networking.ServerStream;

public class Controller {
	private static final int MIN_USERS = 2; // default min users required to start
	private static final int ROOM_CAPACITY = 6; // default max room capacity
	
	private static final Pattern PATTERN_NICKNAME = Pattern.compile("^[a-zA-Z0-9]{3,15}$");
	private static final Pattern PATTERN_IP = Pattern.compile("^(([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.){3}([01]?\\d\\d?|2[0-4]\\d|25[0-5])$");
	
	private NavState state;
	
	@FXML private VBox vboxBack;
	@FXML private Button buttonBack;
	
	// MultiPlayer
	@FXML private VBox vboxMP;
	@FXML private Button buttonC; // button Create
	@FXML private Button buttonJ; // button Join
	
	// MultiPlayer: Create New Room
	@FXML private VBox vboxCreateRoom;
	@FXML private TextField textFieldNicknameS;
	@FXML private TextFlow labelErrorNicknameS;
	@FXML private Label labelMinRoom;
	@FXML private Label labelMaxRoom;
	@FXML private ImageView buttonIncreaseMinRoom;
	@FXML private ImageView buttonDecreaseMinRoom;
	@FXML private ImageView buttonIncreaseMaxRoom;
	@FXML private ImageView buttonDecreaseMaxRoom;
	private Image arrowUp;
	private Image arrowUpDisabled;
	private Image arrowDown;
	private Image arrowDownDisabled;
	@FXML private CheckBox checkBoxRejoin;
	@FXML private Button buttonCNR; // button Create New Room
	
	// MultiPlayer: Join Existing Room
	@FXML private VBox vboxJoinRoom;
	@FXML private TextField textFieldNicknameC;
	@FXML private TextFlow labelErrorNicknameC;
	@FXML private TextField textFieldIP;
	@FXML private Label labelErrorIP;
	@FXML private HBox hboxConnection; // hbox connection
	@FXML private Button buttonJER;
	
	// MultiPlayer: Server
	private IServer server;
	@FXML private VBox vboxServerRoom;
	@FXML private Label labelServerIP;
	@FXML private VBox vboxLabelServerControls;
	@FXML private ListView<HBox> listViewUsersS;
	private ArrayList<Label> listNicknameS;
	private ArrayList<Label> listReadyS;
	private ArrayList<Label> listLabelKick;
	private ArrayList<Label> listLabelBan;
	@FXML private Button buttonRoomSettings;
	@FXML private Button buttonStartGame;
	@FXML private Button buttonOpenClose;
	@FXML private Label labelOpenClose;
	@FXML private TextArea textAreaChatS;
	@FXML private TextField textFieldChatS;
	@FXML private Button buttonChatSendS;
	
	// MultiPlayer: RoomSettings
	@FXML private VBox vboxRoomSettings;
	@FXML private ListView<HBox> listViewBannedUsers;
	private ArrayList<Label> listBannedNickname;
	private ArrayList<Label> listBannedAddress;
	private ArrayList<Label> listRemoveBan;
	@FXML private TextField textFieldBanNickname;
	@FXML private TextField textFieldBanAddress;
	@FXML private Button buttonBan;
	
	// MultiPlayer: Client
	private IClient client;
	@FXML private VBox vboxClientRoom;
	@FXML private ListView<HBox> listViewUsersC;
	private ArrayList<Label> listNicknameC;
	private ArrayList<Label> listReadyC;
	private ArrayList<ImageView> listImagePlayer;
	@FXML private Button buttonReady;
	@FXML private TextArea textAreaChatC;
	@FXML private TextField textFieldChatC;
	@FXML private Button buttonChatSendC;
	
	private int connectedUsers;
	
	private SimpleDateFormat tformatter;
	
	public Controller() {}
	
	public void initialize()
	{
		this.state = NavState.MULTIPLAYER;
		
		this.vboxBack.setVisible(true);
		this.vboxMP.setVisible(true);
		this.vboxCreateRoom.setVisible(false);
		this.vboxJoinRoom.setVisible(false);
		this.vboxServerRoom.setVisible(false);
		this.vboxLabelServerControls.setVisible(false);
		this.vboxRoomSettings.setVisible(false);
		this.vboxClientRoom.setVisible(false);
		
		this.tformatter = new SimpleDateFormat("[HH:mm:ss]");
		this.showConnectingBox(false);
		
		this.buttonCNR.setDisable(true);
		this.buttonJER.setDisable(true);
		this.labelErrorIP.setVisible(false);
		
		this.listNicknameC = new ArrayList<Label>();
		this.listNicknameS = new ArrayList<Label>();
		this.listReadyC = new ArrayList<Label>();
		this.listReadyS = new ArrayList<Label>();
		this.listImagePlayer = new ArrayList<ImageView>();
		this.listLabelKick = new ArrayList<Label>();
		this.listLabelBan = new ArrayList<Label>();
		
		this.labelMinRoom.setText("" + MIN_USERS);
		this.labelMaxRoom.setText("" + ROOM_CAPACITY);
		
		this.arrowUp = new Image(this.getClass().getResource("/resources/icon-arrow-up.png").toString());
		this.arrowUpDisabled = new Image(this.getClass().getResource("/resources/icon-arrow-up-disabled.png").toString());
		this.arrowDown = new Image(this.getClass().getResource("/resources/icon-arrow-down.png").toString());
		this.arrowDownDisabled = new Image(this.getClass().getResource("/resources/icon-arrow-down-disabled.png").toString());
		
		this.buttonDecreaseMinRoom.setDisable(true);
		this.buttonIncreaseMaxRoom.setDisable(true);
		this.buttonIncreaseMinRoom.setImage(this.arrowUp);
		this.buttonDecreaseMinRoom.setImage(this.arrowDownDisabled);
		this.buttonIncreaseMaxRoom.setImage(this.arrowUpDisabled);
		this.buttonDecreaseMaxRoom.setImage(this.arrowDown);
		
		// set automatic chat scrolling to bottom
		this.textAreaChatS.textProperty().addListener(new ChangeListener<Object>() {
		    @Override
		    public void changed(ObservableValue<?> observable, Object oldValue,
		            Object newValue) {
		    	textAreaChatS.setScrollTop(Double.MAX_VALUE); //this will scroll to the bottom
		    }
		});
		this.textAreaChatC.textProperty().addListener(new ChangeListener<Object>() {
		    @Override
		    public void changed(ObservableValue<?> observable, Object oldValue,
		            Object newValue) {
		    	textAreaChatC.setScrollTop(Double.MAX_VALUE); //this will scroll to the bottom
		    }
		});
		
		// popolate the ListView with HBox and set them not visible
		for(int i = 0; i < ROOM_CAPACITY; i++)
		{
			// hbox client
			HBox hbox = new HBox();
			hbox.setPrefSize(280, 25);
			hbox.setSpacing(10);
			hbox.setVisible(false);
			// nickname client
			Label l = new Label("");
			l.setPrefWidth(200);
			l.setTextFill(Paint.valueOf("white"));
			hbox.getChildren().add(l);
			this.listNicknameC.add(l);
			// ready client
			l = new Label("");
			l.setPrefSize(25, 25);
			l.setStyle("-fx-background-color: red");
			l.setVisible(i == 0 ? false : true);
			hbox.getChildren().add(l);
			this.listReadyC.add(l);
			// identifier image
			ImageView iv = new ImageView(new Image(this.getClass().getResource("/resources/icon-user.png").toString()));
			iv.resize(25, 25);
			hbox.getChildren().add(iv);
			this.listImagePlayer.add(iv);
			
			this.listViewUsersC.getItems().add(hbox);
			
			// hbox server
			hbox = new HBox();
			hbox.setPrefSize(300, 25);
			hbox.setSpacing(10);
			hbox.setVisible(false);
			// nickname server
			l = new Label("");
			l.setPrefWidth(180);
			l.setTextFill(Paint.valueOf("white"));
			hbox.getChildren().add(l);
			this.listNicknameS.add(l);
			// ready server
			l = new Label("");
			l.setPrefSize(25, 25);
			l.setStyle(i == 0 ? "-fx-background-color: lime" : "-fx-background-color: red");
			l.setTooltip(new Tooltip("is ready?"));
			l.setVisible(i == 0 ? false : true);
			hbox.getChildren().add(l);
			this.listReadyS.add(l);
			// kick button
			l = new Label();
			iv = new ImageView(new Image(this.getClass().getResource("/resources/icon-kick.png").toString()));
			iv.resize(25, 25);
			l.setGraphic(iv);
			l.setOnMouseClicked(this::kickUser);
			l.setTooltip(new Tooltip("kick the user"));
			l.setVisible(i == 0 ? false : true);
			hbox.getChildren().add(l);
			this.listLabelKick.add(l);
			// ban button
			l = new Label();
			iv = new ImageView(new Image(this.getClass().getResource("/resources/icon-ban.png").toString()));
			iv.resize(25, 25);
			l.setGraphic(iv);
			l.setOnMouseClicked(this::banUser);
			l.setTooltip(new Tooltip("ban the user"));
			l.setVisible(i == 0 ? false : true);
			hbox.getChildren().add(l);
			this.listLabelBan.add(l);
			
			this.listViewUsersS.getItems().add(hbox);
		}
		
		connectedUsers = 0;
	}
	// Multiplayer callbacks
	@FXML public void goBack(ActionEvent event)
	{
		switch(this.state)
		{
			case MULTIPLAYER:
			{
				// nothing
				
				break;
			}
			case MP_CREATE:
			{
				this.vboxCreateRoom.setVisible(false);
				this.vboxMP.setVisible(true);
				
				this.state = NavState.MULTIPLAYER;
				
				break;
			}
			case MP_JOIN:
			{
				this.vboxJoinRoom.setVisible(false);
				this.vboxMP.setVisible(true);
				
				this.state = NavState.MULTIPLAYER;
				
				break;
			}
			case MP_SERVER:
			{
				this.closeConnection();
				this.vboxServerRoom.setVisible(false);
				this.vboxLabelServerControls.setVisible(false);
				this.vboxMP.setVisible(true);
				
				this.state = NavState.MULTIPLAYER;
				
				break;
			}
			case MP_CLIENT:
			{
				this.closeConnection();
				this.vboxClientRoom.setVisible(false);
				this.vboxMP.setVisible(true);
				
				this.state = NavState.MULTIPLAYER;
				
				break;
			}
			default:
			{
				break;
			}
		}
	}
	@FXML public void selectCNR(ActionEvent event)
	{
		this.vboxMP.setVisible(false);
		this.vboxCreateRoom.setVisible(true);
		
		// reset CNR fields?
		
		this.state = NavState.MP_CREATE;
	}
	@FXML public void selectJER(ActionEvent event)
	{
		this.vboxMP.setVisible(false);
		this.vboxJoinRoom.setVisible(true);
		
		this.hboxConnection.setVisible(false);
		
		// reset JER fields?
		
		this.state = NavState.MP_JOIN;
	}
	
	// MultiPlayer: Create New Room callbacks
	@FXML public void validateNicknameS()
	{
		// nickname OK
		if(this.checkNickname(this.textFieldNicknameS.getText()))
		{
			this.buttonCNR.setDisable(false);
			this.labelErrorNicknameS.setVisible(false);
			this.textFieldNicknameS.setStyle("-fx-border-width: 0px; -fx-focus-color: #039ED3;");
		}
		// nickname NOT
		else
		{
			this.buttonCNR.setDisable(true);
			this.labelErrorNicknameS.setVisible(true);
			this.textFieldNicknameS.setStyle("-fx-text-box-border: red; -fx-focus-color: red;");
		}
	}
	@FXML public void increaseMinRoom(MouseEvent event)
	{
		this.buttonDecreaseMinRoom.setDisable(false);
		this.buttonDecreaseMinRoom.setImage(this.arrowDown);
		
		int min = Integer.parseInt(this.labelMinRoom.getText());
		int max = Integer.parseInt(this.labelMaxRoom.getText());
		
		this.labelMinRoom.setText("" + ++min);
		
		if(min == max)
		{
			this.buttonIncreaseMinRoom.setDisable(true);
			this.buttonIncreaseMinRoom.setImage(this.arrowUpDisabled);
			this.buttonDecreaseMaxRoom.setDisable(true);
			this.buttonDecreaseMaxRoom.setImage(this.arrowDownDisabled);
		}
	}
	@FXML public void decreaseMinRoom(MouseEvent event)
	{
		this.buttonIncreaseMinRoom.setDisable(false);
		this.buttonIncreaseMinRoom.setImage(this.arrowUp);
		this.buttonDecreaseMaxRoom.setDisable(false);
		this.buttonDecreaseMaxRoom.setImage(this.arrowDown);
		
		int value = Integer.parseInt(this.labelMinRoom.getText());
		if(value != MIN_USERS)
		{
			this.labelMinRoom.setText("" + --value);
			if(value == MIN_USERS)
			{
				this.buttonDecreaseMinRoom.setDisable(true);
				this.buttonDecreaseMinRoom.setImage(this.arrowDownDisabled);
			}
		}
	}
	@FXML public void increaseMaxRoom(MouseEvent event)
	{
		this.buttonIncreaseMinRoom.setDisable(false);
		this.buttonIncreaseMinRoom.setImage(this.arrowUp);
		this.buttonDecreaseMaxRoom.setDisable(false);
		this.buttonDecreaseMaxRoom.setImage(this.arrowDown);
		
		int value = Integer.parseInt(this.labelMaxRoom.getText());
		if(value != ROOM_CAPACITY)
		{
			this.labelMaxRoom.setText("" + ++value);
			if(value == ROOM_CAPACITY)
			{
				this.buttonIncreaseMaxRoom.setDisable(true);
				this.buttonIncreaseMaxRoom.setImage(this.arrowUpDisabled);
			}
		}
	}
	@FXML public void decreaseMaxRoom(MouseEvent event)
	{
		this.buttonIncreaseMaxRoom.setDisable(false);
		this.buttonIncreaseMaxRoom.setImage(this.arrowUp);
		
		int min = Integer.parseInt(this.labelMinRoom.getText());
		int max = Integer.parseInt(this.labelMaxRoom.getText());
		
		this.labelMaxRoom.setText("" + --max);
		
		if(min == max)
		{
			this.buttonIncreaseMinRoom.setDisable(true);
			this.buttonIncreaseMinRoom.setImage(this.arrowUpDisabled);
			this.buttonDecreaseMaxRoom.setDisable(true);
			this.buttonDecreaseMaxRoom.setImage(this.arrowDownDisabled);
		}
	}
	@FXML public void createNewRoom(ActionEvent event) 
	{
		if(!this.checkNickname(this.textFieldNicknameS.getText()))
		{
			this.showAlert(AlertType.ERROR, "Invalid nickname", "The nickname bust be from 3 to 15 alphanumeric char long.");
			this.buttonCNR.setDisable(true);
			this.labelErrorNicknameS.setVisible(true);
			this.textFieldNicknameS.setStyle("-fx-text-box-border: red; -fx-focus-color: red;");
			return;
		}
		
		this.setServerAddress();
		this.textAreaChatS.setText(this.getCurrentTimestamp() + " " + this.textFieldNicknameS.getText() + " created the room");
		
		// create new room -> start server (if OK switch to Server Room View)
		this.server = new ServerStream(this, this.textFieldNicknameS.getText(), Integer.parseInt(this.labelMinRoom.getText()), Integer.parseInt(this.labelMaxRoom.getText()), this.checkBoxRejoin.isSelected());
		this.client = null;
		
		// reset the user list
		this.resetList();
		
		// reset buttons
		this.buttonStartGame.setDisable(true);
		this.buttonOpenClose.setText("Open");
		this.labelOpenClose.setStyle("-fx-background-color: lime");
		
		// set the first list element (the server) to visibile
		this.listNicknameS.get(0).setText(this.textFieldNicknameS.getText());
		this.listViewUsersS.getItems().get(0).setVisible(true);
		
		this.connectedUsers = 1;
		
		// reset Banned Users list
		this.listViewBannedUsers.getItems().clear();
		this.listBannedNickname = new ArrayList<Label>();
		this.listBannedAddress = new ArrayList<Label>();
		this.listRemoveBan = new ArrayList<Label>();
	}
	
	// MultiPlayer: Join Existing Room callbacks
	@FXML public void validateNicknameAddressC()
	{
		// nickname OK & address OK (or empty)
		if(this.checkNickname(this.textFieldNicknameC.getText()) && (this.checkIP(this.textFieldIP.getText()) || this.textFieldIP.getText().isEmpty()))
		{
			this.buttonJER.setDisable(false);
			this.labelErrorNicknameC.setVisible(false);
			this.labelErrorIP.setVisible(false);
			// reset borders & focus (nickname)
			this.textFieldNicknameC.setStyle("-fx-border-width: 0px; -fx-focus-color: #039ED3;");
			// reset borders & focus (address)
			this.textFieldIP.setStyle("-fx-border-width: 0px; -fx-focus-color: #039ED3;");			
		}
		// nickname OK & address NOT (nor empty)
		else if(this.checkNickname(this.textFieldNicknameC.getText()) && !(checkIP(this.textFieldIP.getText()) || this.textFieldIP.getText().isEmpty()))
		{
			this.buttonJER.setDisable(true);
			this.labelErrorNicknameC.setVisible(false);
			this.labelErrorIP.setVisible(true);
			// reset borders & focus (nickname)
			this.textFieldNicknameC.setStyle("-fx-border-width: 0px; -fx-focus-color: #039ED3;");
			// reset borders & focus (address)
			this.textFieldIP.setStyle("-fx-text-box-border: red; -fx-focus-color: red;");		
		}
		// nickname NOT & address OK (or empty)
		else if(!this.checkNickname(this.textFieldNicknameC.getText()) && (checkIP(this.textFieldIP.getText()) || this.textFieldIP.getText().isEmpty()))
		{
			this.buttonJER.setDisable(true);
			this.labelErrorNicknameC.setVisible(true);
			this.labelErrorIP.setVisible(false);
			// red borders & focus (nickname)
			this.textFieldNicknameC.setStyle("-fx-text-box-border: red; -fx-focus-color: red;");
			// reset borders & focus (address)
			this.textFieldIP.setStyle("-fx-border-width: 0px; -fx-focus-color: #039ED3;");
		}
		// nickname NOT & address NOT (nor empty)
		else
		{
			this.buttonJER.setDisable(true);
			this.labelErrorNicknameC.setVisible(true);
			this.labelErrorIP.setVisible(true);
			// red borders & focus (nickname)
			this.textFieldNicknameC.setStyle("-fx-text-box-border: red; -fx-focus-color: red;");
			// red borders & focus (address)
			this.textFieldIP.setStyle("-fx-text-box-border: red; -fx-focus-color: red;");
					
		}
	}
	@FXML public void joinExistingRoom(ActionEvent event) 
	{
		if(!this.checkNickname(this.textFieldNicknameC.getText()))
		{
			this.showAlert(AlertType.ERROR, "Invalid nickname", "The nickname bust be from 3 to 15 alphanumeric char long.");
			this.buttonJER.setDisable(true);
			this.labelErrorNicknameC.setVisible(true);
			this.textFieldNicknameC.setStyle("-fx-text-box-border: red; -fx-focus-color: red;");
			return;
		}
		if(!this.checkIP(this.textFieldIP.getText()) && !this.textFieldIP.getText().isEmpty())
		{
			this.showAlert(AlertType.ERROR, "Invalid IP Address", "The address must be X.X.X.X or empty (localhost).");
			this.buttonJER.setDisable(true);
			this.labelErrorIP.setVisible(true);
			this.textFieldIP.setStyle("-fx-text-box-border: red; -fx-focus-color: red;");
			return;
		}
		
		// show loading box
		this.showConnectingBox(true);
		
		// reset ready button
		this.buttonReady.setText("Not ready");
		this.buttonReady.setStyle("-fx-background-color: red");
		
		// reset textArea
		this.textAreaChatC.setText("");
		
		// connect to existing room -> start client (if OK switch to Client Room View)
		this.client = new ClientStream(this, this.textFieldIP.getText(), 9001, this.textFieldNicknameC.getText());
		this.server = null;
	}
	
	// MultiPlayer: Server callbacks
	@FXML public void kickUser(MouseEvent event)
	{
		// get the button index
		for(int i = 1; i < this.connectedUsers; i++)
		{
			if(this.listLabelKick.get(i).equals(event.getTarget()))
			{
				System.out.println("Server: kicked user " + this.listNicknameS.get(i).getText());
				
				// remove user from the listView
				this.removeUser(this.listNicknameS.get(i).getText());
				
				// add kick message to the textArea
				this.addToTextArea(this.getCurrentTimestamp() + " " + this.listNicknameS.get(i).getText() + " has been kicked out");
				
				// send Kick message
				this.server.sendKickUser(this.listNicknameS.get(i).getText());
				
				break;
			}
		}
	}
	@FXML public void banUser(MouseEvent event)
	{
		// check if there is already an entry with that address, in case just kick him out
		// get the button index
		for(int i = 1; i < this.connectedUsers; i++)
		{
			if(this.listLabelBan.get(i).equals(event.getTarget()))
			{
				// remove user from the listView
				this.removeUser(this.listNicknameS.get(i).getText());
				
				// add ban message to the textArea
				this.addToTextArea(this.getCurrentTimestamp() + " " + this.listNicknameS.get(i).getText() + " has been banned");
				
				// ban and send Kick message
				User u = this.server.sendBanUser(this.listNicknameS.get(i).getText());
				this.addBannedUser(u.getNickname(), u.getAddress().getHostAddress());
				
				System.out.println("Server: banned user " + u.getNickname() + " (" + u.getAddress().getHostAddress() + ")");
				
				break;
			}
		}
	}
	@FXML public void openRoomSettings()
	{
		System.out.println("Open settings");
		
		this.buttonBack.setDisable(true);
		this.buttonRoomSettings.setDisable(true);
		this.buttonStartGame.setDisable(true);
		this.buttonOpenClose.setDisable(true);
		this.textFieldChatS.setDisable(true);
		this.buttonChatSendS.setDisable(true);
		
		this.vboxRoomSettings.setVisible(true);
		this.textFieldBanNickname.setText("");
		this.textFieldBanNickname.setStyle("-fx-border-width: 0px; -fx-focus-color: #039ED3;");
		this.textFieldBanAddress.setText("");
		this.textFieldBanAddress.setStyle("-fx-border-width: 0px; -fx-focus-color: #039ED3;");
		this.buttonBan.setDisable(true);
	}
	@FXML public void removeBan(MouseEvent event)
	{
		System.out.println("Remove ban selected"); // test
		// get the button index
		for(int i = 0; i < this.listViewBannedUsers.getItems().size(); i++)
		{
			if(this.listRemoveBan.get(i).equals(event.getTarget()))
			{
				System.out.println("Server: user " + this.listBannedNickname.get(i).getText() + " (" + this.listBannedAddress.get(i).getText() + ") is no longer banned");
				
				// remove banned user from server
				boolean result = this.server.removeBan(this.listBannedAddress.get(i).getText());
				
				if(result)
				{
					// add ban message to the textArea
					this.addToTextArea(this.getCurrentTimestamp() + " " + this.listBannedNickname.get(i).getText() + " has is no longer banned");
				}
				
				// remove labels
				this.listBannedNickname.remove(i);
				this.listBannedAddress.remove(i);
				
				// remove  banned user from the listView
				this.listViewBannedUsers.getItems().remove((HBox) this.listRemoveBan.get(i).getParent());
				
				// remove button
				this.listRemoveBan.remove(i);
				
				break;
			}
		}
	}
	@FXML public void validateBanNicknameAndAddress()
	{
		// nickname OK & address OK (or empty)
		if(this.checkNickname(this.textFieldBanNickname.getText()) && (this.checkIP(this.textFieldBanAddress.getText()) || this.textFieldBanAddress.getText().isEmpty()))
		{
			this.buttonBan.setDisable(false);
			this.textFieldBanNickname.setStyle("-fx-border-width: 0px; -fx-focus-color: #039ED3;");
			this.textFieldBanAddress.setStyle("-fx-border-width: 0px; -fx-focus-color: #039ED3;");			
		}
		// nickname OK & address NOT (nor empty)
		else if(this.checkNickname(this.textFieldBanNickname.getText()) && !(checkIP(this.textFieldBanAddress.getText()) || this.textFieldBanAddress.getText().isEmpty()))
		{
			this.buttonBan.setDisable(true);
			this.textFieldBanNickname.setStyle("-fx-border-width: 0px; -fx-focus-color: #039ED3;");
			this.textFieldBanAddress.setStyle("-fx-text-box-border: red; -fx-focus-color: red;");		
		}
		// nickname NOT & address OK (or empty)
		else if(!this.checkNickname(this.textFieldBanNickname.getText()) && (checkIP(this.textFieldBanAddress.getText()) || this.textFieldBanAddress.getText().isEmpty()))
		{
			this.buttonBan.setDisable(true);
			this.textFieldBanNickname.setStyle("-fx-text-box-border: red; -fx-focus-color: red;");
			this.textFieldBanAddress.setStyle("-fx-border-width: 0px; -fx-focus-color: #039ED3;");
		}
		// nickname NOT & address NOT (nor empty)
		else
		{
			this.buttonBan.setDisable(true);
			this.textFieldBanNickname.setStyle("-fx-text-box-border: red; -fx-focus-color: red;");
			this.textFieldBanAddress.setStyle("-fx-text-box-border: red; -fx-focus-color: red;");
		}
	}
	@FXML public void addManualBan(ActionEvent event)
	{
		if(!this.checkNickname(this.textFieldBanNickname.getText()))
		{
			this.showAlert(AlertType.ERROR, "Invalid nickname", "The nickname bust be from 3 to 15 alphanumeric char long.");
			this.buttonBan.setDisable(true);
			this.textFieldBanNickname.setStyle("-fx-text-box-border: red; -fx-focus-color: red;");
			return;
		}
		if(!this.checkIP(this.textFieldBanAddress.getText()) && !this.textFieldBanAddress.getText().isEmpty())
		{
			this.showAlert(AlertType.ERROR, "Invalid IP Address", "The address must be X.X.X.X or empty (localhost).");
			this.buttonBan.setDisable(true);
			this.textFieldBanAddress.setStyle("-fx-text-box-border: red; -fx-focus-color: red;");
			return;
		}
		
		String banAddress = this.textFieldBanAddress.getText().isEmpty() ? "127.0.0.1" : this.textFieldBanAddress.getText();
		if(this.server.sendBanUser(this.textFieldBanNickname.getText(), banAddress))
		{
			this.addBannedUser(this.textFieldBanNickname.getText(), banAddress);
			this.addToTextArea(this.getCurrentTimestamp() + " " + this.textFieldBanNickname.getText() + " has been banned");
		}
		else
		{
			this.showAlert(AlertType.ERROR, "Failed to add ban entry", "The entry already exist");
			this.buttonBan.setDisable(true);
		}
	}
	@FXML public void closeRoomSettings()
	{
		System.out.println("Close settings");
		
		this.buttonBack.setDisable(false);
		this.buttonRoomSettings.setDisable(false);
		this.buttonStartGame.setDisable(!this.server.checkCanStartGame());
		this.buttonOpenClose.setDisable(false);
		this.textFieldChatS.setDisable(false);
		this.buttonChatSendS.setDisable(false);
		
		this.vboxRoomSettings.setVisible(false);
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
	@FXML public void sendMessageS(ActionEvent event) 
	{
		String msg = this.textFieldChatS.getText();
		if(!msg.isEmpty() && !msg.isBlank())
			this.server.sendChatMessage(msg);
		this.textFieldChatS.setText("");
	}
	@FXML public void enterChatHandleS(KeyEvent event)
	{
		if(this.state == NavState.MP_SERVER && event.getCode().equals(KeyCode.ENTER))
		{
			String msg = this.textFieldChatS.getText();
			if(!msg.isEmpty() && !msg.isBlank())
				this.server.sendChatMessage(msg);
			this.textFieldChatS.setText("");
		}
	}
	@FXML public void startGame(ActionEvent event)
	{
		System.out.println("Start game");
	}
	
	// MultiPlayer: Client callbacks
	@FXML public void toggleReady(ActionEvent event)
	{
		if(this.buttonReady.getText().equalsIgnoreCase("Ready"))
		{
			this.buttonReady.setText("Not ready");
			this.buttonReady.setStyle("-fx-background-color: red");
			this.client.sendReady(false);
			this.updateReady(this.textFieldNicknameC.getText(), false);
		}
		else
		{
			this.buttonReady.setText("Ready");
			this.buttonReady.setStyle("-fx-background-color: lime");
			this.client.sendReady(true);
			this.updateReady(this.textFieldNicknameC.getText(), true);
		}
		// TO-DO: set a 5 sec timer that disables the button, so that users can't spam the toggle
	}
	@FXML public void sendMessageC(ActionEvent event) 
	{
		String msg = this.textFieldChatC.getText();
		if(!msg.isEmpty() && !msg.isBlank())
			this.client.sendChatMessage(msg);
		this.textFieldChatC.setText("");
	}
	@FXML public void enterChatHandleC(KeyEvent event)
	{
		if(this.state == NavState.MP_CLIENT && event.getCode().equals(KeyCode.ENTER))
		{
			String msg = this.textFieldChatC.getText();
			if(!msg.isEmpty() && !msg.isBlank())
				this.client.sendChatMessage(msg);
			this.textFieldChatC.setText("");
		}
	}
	
	// utilities
	private boolean checkNickname(String text)
	{
		// if OK return true
		return PATTERN_NICKNAME.matcher(text).matches() ? true : false;
	}
	private boolean checkIP(String text)
	{
		// if OK return true
		return PATTERN_IP.matcher(text).matches() ? true : false;
	}
	public void switchToMP()
	{
		if(this.state == NavState.MP_CLIENT)
		{
			this.vboxClientRoom.setVisible(false);
			this.vboxMP.setVisible(true);
			
			this.state = NavState.MULTIPLAYER;
		}
		else if (this.state == NavState.MP_SERVER)
		{
			this.vboxServerRoom.setVisible(false);
			this.vboxLabelServerControls.setVisible(false);
			this.vboxMP.setVisible(true);
			
			this.state = NavState.MULTIPLAYER;
		}
	}
	public void switchToServerRoom()
	{
		this.vboxCreateRoom.setVisible(false);
		this.vboxServerRoom.setVisible(true);
		this.vboxLabelServerControls.setVisible(true);
		
		this.state = NavState.MP_SERVER;
	}
	public void switchToClientRoom()
	{
		this.vboxJoinRoom.setVisible(false);
		this.vboxClientRoom.setVisible(true);
		
		this.state = NavState.MP_CLIENT;
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
	private void setServerAddress()
	{
		try(final DatagramSocket socket = new DatagramSocket()) {
			socket.connect(InetAddress.getByName("8.8.8.8"), 10002);
			String privateIP = socket.getLocalAddress().getHostAddress();
			this.labelServerIP.setText("Private Server IP address: " + privateIP);
		} catch (SocketException e) {
			e.printStackTrace();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
	}
	public void showConnectingBox(boolean value)
	{
		this.hboxConnection.setVisible(value);
	}
	public boolean isRoomOpen()
	{
		return this.buttonOpenClose.getText().equalsIgnoreCase("Open") ? true : false;
	}
	public String getCurrentTimestamp()
	{
		Date date = new Date(System.currentTimeMillis());
		String timestamp = this.tformatter.format(date);
		
		return timestamp;
	}
	public void addToTextArea(String text)
	{
		// client
		if(this.state == NavState.MP_CLIENT)
		{
			if(this.textAreaChatC.getText().isEmpty())
				this.textAreaChatC.setText(text);
			else this.textAreaChatC.appendText("\n" + text);
		}
		// server
		else if(this.state == NavState.MP_SERVER)
		{
			this.textAreaChatS.appendText("\n" + text);
		}
	}
	public void addToTextArea(Message message)
	{
		this.addToTextArea(message.getTimestamp() + " " + message.getNickname() + ": " + message.getContent());
	}
	public void updateReady(String nickname, boolean ready)
	{
		if(this.state == NavState.MP_CLIENT)
		{
			for(int i = 0; i < this.listViewUsersC.getItems().size(); i++)
			{
				if(nickname.equals(this.listNicknameC.get(i).getText()))
				{
					this.listReadyC.get(i).setStyle(ready ? "-fx-background-color: lime" : "-fx-background-color: red");
					break;
				}
			}
		}
		else if(this.state == NavState.MP_SERVER)
		{
			for(int i = 0; i < this.listViewUsersS.getItems().size(); i++)
			{
				if(nickname.equals(this.listNicknameS.get(i).getText()))
				{
					this.listReadyS.get(i).setStyle(ready ? "-fx-background-color: lime" : "-fx-background-color: red");
					break;
				}
			}
		}
	}
	public void resetList()
	{
		if(this.state == NavState.MP_CLIENT)
		{
			Platform.runLater(() -> {
				for(int i = 0; i < ROOM_CAPACITY; i++)
				{
					this.listViewUsersC.getItems().get(i).setVisible(false);
					this.listNicknameC.get(i).setText("");
					this.listReadyC.get(i).setStyle("-fx-background-color: red");
					this.listImagePlayer.get(i).setVisible(false);
				}
			});
		}
		else if(this.state == NavState.MP_SERVER)
		{
			for(int i = 0; i < ROOM_CAPACITY; i++)
			{
				this.listViewUsersS.getItems().get(i).setVisible(false);
				this.listNicknameS.get(i).setText("");
				this.listReadyS.get(i).setStyle("-fx-background-color: red");
			}
		}
	}
	public void addUser(User u)
	{
		Platform.runLater(() -> {
			if(this.state == NavState.MP_CLIENT)
			{
				this.listNicknameC.get(this.connectedUsers).setText(u.getNickname());
				this.listViewUsersC.getItems().get(this.connectedUsers).setVisible(true);
				this.connectedUsers++;
			}
			else if(this.state == NavState.MP_SERVER)
			{
				this.listNicknameS.get(this.connectedUsers).setText(u.getNickname());
				this.listViewUsersS.getItems().get(this.connectedUsers).setVisible(true);
				this.connectedUsers++;
				
				this.buttonStartGame.setDisable(true); // when a new user connects it's always not ready
			}
		});
		
	}
	public void removeUser(String nickname)
	{
		Platform.runLater(() -> {
			boolean found = false;
			if(this.state == NavState.MP_CLIENT)
			{
				// NB: we have to move by one position back every user, to fill the empty space left by the removed one
				for(int i = 1; i < this.connectedUsers; i++)
				{
					if(found)
					{
						// we move every entry up by 1, overriding the one to remove
						this.listNicknameC.get(i - 1).setText(this.listNicknameC.get(i).getText());
						this.listReadyC.get(i - 1).setStyle(this.listReadyC.get(i).getStyle());
						this.listImagePlayer.get(i - 1).setVisible(this.listImagePlayer.get(i).isVisible());
					}
					if(this.listNicknameC.get(i).getText().equals(nickname))
						found = true;
				}
				// we hide the last entry
				this.listViewUsersC.getItems().get(this.connectedUsers - 1).setVisible(false);
				this.listNicknameC.get(this.connectedUsers - 1).setText("");
				this.listReadyC.get(this.connectedUsers - 1).setStyle("-fx-background-color: red");
				this.listImagePlayer.get(this.connectedUsers - 1).setVisible(false);
				this.connectedUsers--;
			}
			else if(this.state == NavState.MP_SERVER)
			{
				// NB: we have to move by one position back every user, to fill the empty space left by the removed one
				for(int i = 1; i < this.connectedUsers; i++)
				{
					if(found)
					{
						// we move every entry up by 1, overriding the one to remove
						this.listNicknameS.get(i - 1).setText(this.listNicknameS.get(i).getText());
						this.listReadyS.get(i - 1).setStyle(this.listReadyS.get(i).getStyle());
					}
					if(this.listNicknameS.get(i).getText().equals(nickname))
						found = true;
				}
				// we hide the last entry
				this.listViewUsersS.getItems().get(this.connectedUsers - 1).setVisible(false);
				this.listNicknameS.get(this.connectedUsers - 1).setText("");
				this.listReadyS.get(this.connectedUsers - 1).setStyle("-fx-background-color: red");
				this.connectedUsers--;
				
				this.buttonStartGame.setDisable(!this.server.checkCanStartGame());
			}
		});
	}
	public void updateUserList(List<User> users)
	{
		Platform.runLater(() -> {
			if(this.state == NavState.MP_CLIENT)
			{
				for(int i = 0; i < users.size(); i++)
				{
					User u = users.get(i);
					this.listNicknameC.get(i).setText(u.getNickname());
					this.listViewUsersC.getItems().get(i).setVisible(true);
					this.listReadyC.get(i).setStyle(u.isReady() ? "-fx-background-color: lime" : "-fx-background-color: red");
					this.listImagePlayer.get(i).setVisible(this.textFieldNicknameC.getText().equals(users.get(i).getNickname()) ? true : false);
				}
				this.connectedUsers = users.size();
			}
			else if(this.state == NavState.MP_SERVER)
			{
				// for the moment it's never used from the server
			}
		});
	}
	public void enableStartGame(boolean value)
	{
		this.buttonStartGame.setDisable(!value || this.vboxRoomSettings.isVisible());
	}
	public void closeConnection()
	{
		if(this.state == NavState.MP_CLIENT)
		{
			this.client.sendClose();
			this.client = null;
		}
		else if(this.state == NavState.MP_SERVER)
		{
			this.server.sendClose();
			this.server = null;
		}
    }
	private void addBannedUser(String nickname, String address)
	{
		HBox hbox = new HBox();
		hbox.setPrefSize(400, 25);
		hbox.setSpacing(10);
		// banned nickname
		Label l = new Label(nickname);
		l.setPrefSize(130, 25);
		l.setTextFill(Paint.valueOf("white"));
		hbox.getChildren().add(l);
		this.listBannedNickname.add(l);
		// banned address
		l = new Label(address);
		l.setPrefSize(130, 25);
		l.setTextFill(Paint.valueOf("white"));
		hbox.getChildren().add(l);
		this.listBannedAddress.add(l);
		// remove ban
		l = new Label();
		ImageView iv = new ImageView(new Image(this.getClass().getResource("/resources/icon-trash-bin.png").toString()));
		iv.resize(25, 25);
		l.setGraphic(iv);
		l.setPrefSize(25, 25);
		l.setTooltip(new Tooltip("remove the ban for that user"));
		hbox.getChildren().add(l);
		l.setOnMouseClicked(this::removeBan);
		l.setOnMouseEntered(this::setBinAnimationOn);
		l.setOnMouseExited(this::setBinAnimationOff);
		this.listRemoveBan.add(l);
		
		this.listViewBannedUsers.getItems().add(hbox);
	}
	private void setBinAnimationOn(MouseEvent event)
	{
		ImageView iv = (ImageView) ((Label) event.getTarget()).getChildrenUnmodifiable().get(0);
		iv.setImage(new Image(this.getClass().getResource("/resources/icon-trash-bin-animated.gif").toString()));
	}
	private void setBinAnimationOff(MouseEvent event)
	{
		ImageView iv = (ImageView) ((Label) event.getTarget()).getChildrenUnmodifiable().get(0);
		iv.setImage(new Image(this.getClass().getResource("/resources/icon-trash-bin.png").toString()));
	}
}

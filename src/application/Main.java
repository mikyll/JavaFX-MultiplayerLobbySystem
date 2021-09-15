package application;

import controller.Controller;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

public class Main extends Application{
	private Controller controller;
	@Override
	public void start(Stage stage) {
		try {
			FXMLLoader loader = new FXMLLoader(Main.class.getResource("/view/ViewChat.fxml"));
			AnchorPane homeUtente = (AnchorPane) loader.load();
			Scene scene = new Scene(homeUtente);
			scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
			stage.setTitle("Chat");
			stage.setScene(scene);
			//stage.setResizable(false);
			stage.show();
			this.controller = (Controller) loader.getController();
			
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void stop()
	{
		this.controller.closeConnection();
		Platform.exit();
	    System.exit(0);
	}
	
	public static void main(String[] args) {
		launch(args);
		System.out.println("ciao");
	}
}

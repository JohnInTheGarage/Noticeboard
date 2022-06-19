package driverway.nb.controllers;

import driverway.nb.screens.AdminPane;
import java.net.URL;
import java.net.http.HttpClient;
import java.time.Duration;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.Stage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author john
 */
public class ExitPaneController implements Initializable {

	private static final Logger LOGGER = LogManager.getLogger();

	@FXML
	public Button btnAdmin;
	
	@Override
	public void initialize(URL location, ResourceBundle resources) {
	}

	public void showAdmin(ActionEvent event) {
		try {
			Scene thisScene = ((Node) event.getSource()).getScene();
			Stage window = (Stage) thisScene.getWindow();
			AdminPane adminScreen = new AdminPane(thisScene);

			window.setScene(new Scene(adminScreen, 800, 480));
			if (System.getProperty("os.arch").equals("arm")) {
				window.setFullScreen(true);
			}
			window.show();
		} catch (Exception e) {
			LOGGER.error("Show Periods problem", e.getMessage());
		}

	}


}

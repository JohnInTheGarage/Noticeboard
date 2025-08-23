package driverway.nb.controllers;

import driverway.nb.screens.AdminPane;
import driverway.nb.utils.MqttHelper;
import java.net.URL;
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
            //LOGGER.trace("ShowAdmin >> ");
			Scene thisScene = ((Node) event.getSource()).getScene();
			//LOGGER.trace("ShowAdmin >> got the scene");
            Stage window = (Stage) thisScene.getWindow();
			//LOGGER.trace("ShowAdmin >> got the window");
            AdminPane adminScreen = new AdminPane(thisScene);
            //LOGGER.trace("ShowAdmin >> got admin pane");
            window.setScene(new Scene(adminScreen, 800, 480));
            //LOGGER.trace("ShowAdmin >> set the scene");
			if ("arm-aarch64".contains(System.getProperty("os.arch")) ) {
				window.setFullScreen(true);
                //LOGGER.trace("ShowAdmin >> set fullscreen");
			}
            
			window.show();
            //LOGGER.trace("ShowAdmin >> Showing the window");
            
		} catch (Exception e) {
			LOGGER.error("Show Admin problem :" + e.getMessage());
		}

	}


}

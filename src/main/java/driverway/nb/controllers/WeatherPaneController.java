package driverway.nb.controllers;

import driverway.nb.weatherfinder.WeatherAlert;
import driverway.nb.weatherfinder.Forecast;
import driverway.nb.screens.PeriodsPane;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.shape.SVGPath;
import javafx.stage.Stage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author john
 */
public class WeatherPaneController implements Initializable {

	private static final Logger LOGGER = LogManager.getLogger();

	@FXML
	public Label lblMaxTemp;
	@FXML
	public Label lblMaxRain;
	@FXML
	public Label lblMessage;
	@FXML
	public SVGPath svgRain;
	@FXML
	public SVGPath svgThermo;
	@FXML
	public Button btnDetails;
	@FXML
	public Button btnWarnings;

	private Forecast fc;

	@Override
	public void initialize(URL location, ResourceBundle resources) {

	}

	public void showPeriods(ActionEvent event) {
		try {
			Scene thisScene = ((Node) event.getSource()).getScene();
			Stage window = (Stage) thisScene.getWindow();
			PeriodsPane periodsScreen = new PeriodsPane(fc, thisScene);

			window.setScene(new Scene(periodsScreen, 800, 480));
			window.setFullScreen(true);
			window.show();
		} catch (Exception e) {
			LOGGER.error("Show Periods problem", e.getMessage());
		}

	}

	public void showWarnings(ActionEvent event) {

		StringBuilder sb = new StringBuilder();
		/*
		var popup = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.WARNING);
		popup.setTitle("Weather");
		popup.setHeaderText("Alerts");
		 */
		for (WeatherAlert a : fc.getAlerts()) {
			//sb.append(String.format("\ntext=%s area=(%s) level=%s \n", a.getText(), a.getArea(), a.getLevel() ));
			sb.append(String.format("\n%s\n", a.getText()));
		}

		//popup.setContentText(sb.toString());
		//popup.showAndWait().ifPresent((btnType) -> {
		//}); //effing lambdas
		Alert alert = new Alert(AlertType.WARNING);
		alert.setTitle("Weather Alerts");
		alert.setHeaderText("Alerts active :" + fc.getAlerts().size());
		//alert.setContentText("Alert ");

//Label label = new Label("3 ????????");
		TextArea textArea = new TextArea(sb.toString());
		textArea.setEditable(false);
		textArea.setWrapText(true);

		textArea.setMaxWidth(Double.MAX_VALUE);
		textArea.setMaxHeight(Double.MAX_VALUE);
		GridPane.setVgrow(textArea, Priority.ALWAYS);
		GridPane.setHgrow(textArea, Priority.ALWAYS);

		GridPane expContent = new GridPane();
		expContent.setMaxWidth(Double.MAX_VALUE);
//expContent.add(label, 0, 0);
		expContent.add(textArea, 0, 1);

// Set expandable Exception into the dialog pane.
		alert.getDialogPane().setExpandableContent(expContent);

		alert.showAndWait();

	}

	public void setForecast(Forecast latest) {
		fc = latest;
		if (fc == null) {
			LOGGER.trace("forecast is null - ???");
			return;
		}

		//LOGGER.trace("max rain " + fc.getTodaysMaxRainProb());
		//LOGGER.trace("max temp " + fc.getTodaysMaxTemp());
		//LOGGER.trace("run date " + fc.getHumanReadableRunDate());
		lblMaxRain.setText(fc.getTodaysMaxRainProb() + "%");
		lblMaxTemp.setText(String.format("%.1f", fc.getTodaysMaxTemp()));
		lblMessage.setText(fc.getHumanReadableRunDate());

		btnWarnings.setVisible(false);
		if (fc.getAlerts().size() > 0) {
			boolean redAlert = false;
			for (WeatherAlert a : fc.getAlerts()) {
				if (a.getLevel().equals("severe")) {
					redAlert = true;
					break;
				}
			}
			String icon = "/alert-yellow.gif";
			if (redAlert) {
				icon = "/alert-red.gif";
			}
			var a = getClass().getResource(icon);
			var b = a.toExternalForm();

			String absPath = getClass().getResource(icon).toExternalForm();
			Image warning = new Image(absPath);
			btnWarnings.setGraphic(new ImageView(warning));
			btnWarnings.setVisible(true);
		}

	}

}

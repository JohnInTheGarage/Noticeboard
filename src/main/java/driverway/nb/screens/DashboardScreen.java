package driverway.nb.screens;

import driverway.nb.controllers.UpdateFetcher;
import driverway.nb.controllers.ApptsPaneController;
import driverway.nb.controllers.WeatherPaneController;
import driverway.nb.controllers.ClockPaneController;
import driverway.nb.controllers.CalendarGridPaneController;
import driverway.nb.utils.PropertyLoader;
import java.io.IOException;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import javafx.scene.control.Alert;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * The graphical user interface.
 */
public class DashboardScreen extends HBox {

	private static final Logger LOGGER = LogManager.getLogger();
	private Pane clockPane = null;
	private Pane weatherPane = null;
	private Pane exitPane = null;
	private Pane calendarGridPane = null;
	private Pane apptsPane = null;
	private int prevAppointments = -1;
	private int prevForecast = -1;

	private ClockPaneController clockController;
	private CalendarGridPaneController calendarController;
	private ApptsPaneController apptsController;
	private WeatherPaneController weatherController;
	public UpdateFetcher updateFetcher;

	@SuppressWarnings("unchecked")
	public DashboardScreen(PropertyLoader pl) throws IOException {

		try {
			SetupPanes();
		} catch (IOException ex) {
			LOGGER.error("setting up panes failed ", ex);
			errorPopup("unexpected error setting up", ex.getMessage());
		}

		
		VBox tilesColumn1 = new VBox(clockPane, weatherPane, exitPane);
		tilesColumn1.setMinWidth(230);

		VBox tilesColumn2 = new VBox(calendarGridPane, apptsPane);
		this.getChildren().add(tilesColumn1);
		this.getChildren().add(tilesColumn2);

		updateFetcher = new UpdateFetcher(pl);
		Thread updThread = new Thread(updateFetcher);
		updThread.start();

		setId("nb"); // anything different from satId used by the Satellite pane
		LOGGER.trace("Noticeboard started");

	}

	private void SetupPanes() throws IOException {
		LOGGER.trace("");
		LOGGER.trace("-----------------Dashboard begins ----------------");
		
		FXMLLoader clockLoader = new FXMLLoader(getClass().getResource("/clockPane.fxml"));
		clockController = new ClockPaneController();
		clockLoader.setController(clockController);
		clockPane = clockLoader.load();

        URL	fxmlResource = getClass().getResource("/exitPane.fxml");
        exitPane = FXMLLoader.load(fxmlResource );   

		FXMLLoader weatherLoader = new FXMLLoader(getClass().getResource("/weatherPane.fxml"));
		weatherController = new WeatherPaneController();
		weatherLoader.setController(weatherController);
		weatherPane = weatherLoader.load();

		FXMLLoader calendarLoader = new FXMLLoader(getClass().getResource("/calendarGridPane.fxml"));
		calendarController = new CalendarGridPaneController();
		calendarController.setDate(LocalDate.now());
		calendarLoader.setController(calendarController);
		calendarGridPane = calendarLoader.load();
		calendarController.showCalendar();

		FXMLLoader apptsLoader = new FXMLLoader(getClass().getResource("/appointmentsPane.fxml"));
		apptsController = new ApptsPaneController();
		apptsController.setDate(LocalDate.now());
		apptsLoader.setController(apptsController);
		apptsPane = apptsLoader.load();
	}

	private void errorPopup(String title, String message) {
		var alert = new Alert(Alert.AlertType.ERROR);
		alert.setTitle(title);
		alert.setHeaderText("Error");
		alert.setContentText(message);

		alert.showAndWait().ifPresent((btnType) -> {
		});

	}

	public void setClock(LocalDateTime timestamp) {
		clockController.setClock(timestamp);
		if (clockController.isNewDay()) {
			calendarController.setDate(LocalDate.now());
			calendarController.changeDay();
			calendarController.showCalendar();
		}
        
	}

	public void checkForecast() {
		if (updateFetcher.getLastForecast() != prevForecast) {
			LOGGER.trace("New forecast found");
			weatherController.setForecast(updateFetcher.getForecast());
			prevForecast = updateFetcher.getLastForecast();
		}
	}

	public void checkAppointments() {
		if (updateFetcher.getLastAppointments() != prevAppointments) {
			//LOGGER.trace("Appointments might be updated");
			calendarController.setAppointments(updateFetcher.getApptsData());
			apptsController.setAppointments(updateFetcher.getApptsData());
			prevAppointments = updateFetcher.getLastAppointments();
		}

	}

	
}


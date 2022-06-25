package driverway.nb.screens;

import driverway.nb.utils.PreferenceHelper;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.MapValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author john
 */
public class AdminPane extends VBox {

	private static final Logger LOGGER = LogManager.getLogger();
	private Scene callerScene;
	
	public Button btnQuit = new Button("Quit");
	public Button btnBack = new Button("Back");
	// Switch on/off two sets of Christmas decorations on another pi
	public Button btnSwitch1  = new Button("Xmas-Front");
	public Button btnSwitch2  = new Button("Xmas-North");
	private boolean lightsNorth = false;
	private boolean lightsFront = false;
	private String command;
	private HttpClient httpClient;
	private String webapi = "http://music.local:8080/GLSRest/webapi/lights/";
	
	@SuppressWarnings("unchecked")
	public AdminPane(Scene thisScene) throws IOException {
				httpClient = HttpClient.newBuilder()
				.version(HttpClient.Version.HTTP_1_1)
				.connectTimeout(Duration.ofSeconds(30))
				.build();
		btnSwitch1.setDisable(true);
		btnSwitch2.setDisable(true);

		callerScene = thisScene;
		PreferenceHelper ph = PreferenceHelper.getInstance();
		
		this.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());

		EventHandler<MouseEvent> eventBackHandler = new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				Scene thisScene = ((Node) event.getSource()).getScene();
				Stage window = (Stage) thisScene.getWindow();
				window.setScene(callerScene);
				window.setFullScreen(true);
				window.show();
			}
		};

		EventHandler<MouseEvent> eventQuitHandler = new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				Platform.exit();
				System.exit(0);
			}
		};

		EventHandler<MouseEvent> eventSwitch1Handler = new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				Scene thisScene = ((Node) event.getSource()).getScene();
				Stage window = (Stage) thisScene.getWindow();
				window.setScene(callerScene);
				window.setFullScreen(true);
				window.show();
			}
		};

		EventHandler<MouseEvent> eventSwitch2Handler = new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				Scene thisScene = ((Node) event.getSource()).getScene();
				Stage window = (Stage) thisScene.getWindow();
				window.setScene(callerScene);
				window.setFullScreen(true);
				window.show();
			}
		};
		
		HBox buttonsPane = new HBox();
		buttonsPane.setMaxSize(500, 50);
		buttonsPane.setMinSize(500, 50);

		buttonsPane.getChildren().addAll(btnBack, btnQuit, btnSwitch1, btnSwitch2);
		
		btnBack.setMinSize(100, 50);
		btnQuit.setMinSize(100, 50);
		btnSwitch1.setMinSize(100, 50);
		btnSwitch2.setMinSize(100, 50);
		
		Label label = new Label("Admin / Status page");

		TableView tableView = new TableView();
		tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

		TableColumn<Map, String> paramColumn = new TableColumn<>("Parameter");
		paramColumn.setCellValueFactory(new MapValueFactory<>("parameter"));
		TableColumn<Map, String> valueColumn = new TableColumn<>("Value");
		valueColumn.setCellValueFactory(new MapValueFactory<>("value"));

		tableView.getColumns().add(paramColumn);
		tableView.getColumns().add(valueColumn);

		ObservableList<Map<String, Object>> items
				= FXCollections.<Map<String, Object>>observableArrayList();

		Map<String, Object> item1 = new HashMap<>();
		item1.put("parameter", "Last Appointments fetch");
		item1.put("value", ph.getItem("lastAppointments"));
		items.add(item1);

		Map<String, Object> item2 = new HashMap<>();
		item2.put("parameter", "Appointments Interval");
		item2.put("value", ph.getItem("GoogleRequestInterval"));
		items.add(item2);

		Map<String, Object> item3 = new HashMap<>();
		item3.put("parameter", "Last Forecast fetch");
		item3.put("value", ph.getItem("lastForecast"));
		items.add(item3);

		Map<String, Object> item4 = new HashMap<>();
		item4.put("parameter", "Forecast Interval");
		item4.put("value", ph.getItem("ForecastRequestInterval"));
		items.add(item4);
		
		Map<String, Object> item5 = new HashMap<>();
		item5.put("parameter", "Last Satellite image fetch");
		item5.put("value", ph.getItem("lastSatellite"));
		items.add(item5);

		Map<String, Object> item6 = new HashMap<>();
		item6.put("parameter", "Satellite Interval");
		item6.put("value", ph.getItem("EUmetRequestInterval"));
		items.add(item6);

		Map<String, Object> item7 = new HashMap<>();
		item7.put("parameter", "Satellite Image retention (days)");
		item7.put("value", ph.getItem("SatelliteImageRetention"));
		items.add(item7);

		Map<String, Object> item = new HashMap<>();
		item.put("parameter", "Forecast provider");
		item.put("value", ph.getItem("forecastProvider"));
		items.add(item);

		tableView.getItems().addAll(items);

		this.getChildren().add(buttonsPane);
		this.getChildren().add(tableView);
		btnBack.addEventFilter(MouseEvent.MOUSE_CLICKED, eventBackHandler);
		btnQuit.addEventFilter(MouseEvent.MOUSE_CLICKED, eventQuitHandler);
		
		btnSwitch1.addEventFilter(MouseEvent.MOUSE_CLICKED, eventSwitch1Handler);
		btnSwitch2.addEventFilter(MouseEvent.MOUSE_CLICKED, eventSwitch2Handler);
	}

	public void toggleFront(ActionEvent event) {
		if (lightsFront) {
			command = "garden?switch=off";
		} else {
			command = "garden?switch=on";
		}
		lightsFront = sendCommand(command);

		if (lightsFront) {
			btnSwitch1.setStyle("-fx-background-color:limegreen; ");
		} else {
			btnSwitch1.setStyle("-fx-background-color:coral; ");
		}

	}

	public void toggleNorth(ActionEvent event) {
		if (lightsNorth) {
			command = "garage?switch=off";
		} else {
			command = "garage?switch=on";
		}
		lightsNorth = sendCommand(command);
		if (lightsNorth) {
			btnSwitch2.setStyle("-fx-background-color:limegreen; ");
		} else {
			btnSwitch2.setStyle("-fx-background-color:coral; ");
		}

	}

	private boolean sendCommand(String url) {
		String reply="";
				
		LOGGER.info("Sending " +webapi+url);
		
		HttpRequest request = HttpRequest.newBuilder()
				.GET()
				.uri(URI.create(webapi + url))
				//.setHeader("User-Agent", "NoticeBoard")
				.build();

		HttpResponse<String> response = null;
		try {
			
			response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
			LOGGER.info("response code:" +response.statusCode());
			reply = "" +response.body();

		} catch (IOException ex) {
			LOGGER.error("IO exception " + ex);
		} catch (InterruptedException ex) {
			LOGGER.error("InterruptedException " + ex);
		}

		boolean result = false;
		
		if (reply.contains("OK, ") ){
			if (reply.contains(" lights on")){
				return true;
			}
			if (reply.contains(" lights off")){
				return false;
			}
			
		} 
		return true;
	}

}

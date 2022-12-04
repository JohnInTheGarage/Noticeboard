package driverway.nb.screens;

import driverway.nb.utils.PreferenceHelper;
import driverway.nb.utils.Xmas;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
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
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

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
    public Button btnSwitch1 = new Button("Xmas-Front");
    public Button btnSwitch2 = new Button("Xmas-Side");
    private boolean lightsSide = false;
    private boolean lightsFront = false;
    
    // MQTT parameters for switch buttons
    private MqttClient lightingClient = null;
    private MqttClient statusClient = null;
    private String command;
    private String mqttServer;
    private String topicLighting;
    private String topicStatus;
    private String username;
    private String password;
    private String lightingId;
    private String statusId;
    private int qos = 0;

    @SuppressWarnings("unchecked")
    public AdminPane(Scene thisScene) throws IOException {

        callerScene = thisScene;
        PreferenceHelper ph = PreferenceHelper.getInstance();
        
        if (Xmas.SOON) {
            try {
                if (statusClient == null) {
                    mqttServer = ph.getItem("mqttServer");
                    topicLighting = ph.getItem("topicLighting");
                    topicStatus = ph.getItem("topicStatus");
                    username = ph.getItem("username");
                    password = ph.getItem("password");
                    lightingId = ph.getItem("lightingId");
                    statusId = ph.getItem("statusId");
                    
                    lightingClient = new MqttClient(mqttServer, lightingId, new MemoryPersistence());
                    statusClient = new MqttClient(mqttServer, statusId, new MemoryPersistence());
                    // connect options
                    MqttConnectOptions options = new MqttConnectOptions();
                    options.setUserName(username);
                    options.setPassword(password.toCharArray());
                    options.setConnectionTimeout(60);
                    options.setKeepAliveInterval(60);
                    options.setAutomaticReconnect(true);
                    options.setCleanSession(true);
                    options.setConnectionTimeout(10);
                    lightingClient.connect(options);
                    setUpStatusClient();
                    statusClient.connect(options);
                    statusClient.subscribe(topicStatus, qos);

                    LOGGER.info("Lighting Client connected :" + lightingClient.isConnected());
                    LOGGER.info("Status Client connected :" + statusClient.isConnected());
                }
            } catch (MqttException ex) {
                LOGGER.info("MTQQ problems initialising :" + ex.getMessage());
            }
        } else {
            //Hide buttons if not using as switches; in my case exterior xmas lights.
            btnSwitch1.setDisable(true);
            btnSwitch2.setDisable(true);
        }

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
                toggleFront();
            }
        };

        EventHandler<MouseEvent> eventSwitch2Handler = new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                toggleSide();
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

    public void toggleFront() {
        if (lightsFront) {
            command = "front off";
        } else {
            command = "front on";
        }
        sendLightsCommand(command);

    }

    public void toggleSide() {
        if (lightsSide) {
            command = "side off";
        } else {
            command = "side on";
        }
        sendLightsCommand(command);
    }

    /*
    * Changing from HTTP to MQTT means no immeadiate response is available
    * so no return-type
     */
    private void sendLightsCommand(String command) {
        //LOGGER.info("Sending lighting command :" + command + " via MQTT");

        //MqttMessage 
        try {
            byte[] payload = command.getBytes();
            MqttMessage msg = new MqttMessage(payload);
            msg.setQos(qos);
            msg.setRetained(false);
            lightingClient.publish(topicLighting, msg);
        } catch (MqttException ex) {
            LOGGER.info("MQTT problem while sending :" + ex.getMessage());
        }
    }

    private void setUpStatusClient() {
        statusClient.setCallback(new MqttCallback() {

            @Override
            public void connectionLost(Throwable cause) {
                LOGGER.info("MQTT connectionLost: " + cause.getMessage());
            }

            @Override
            public void messageArrived(String topic, MqttMessage message) {
                String messageContent = new String(message.getPayload());
                //LOGGER.info("MQTT message received :" + messageContent);
                decodeMessage(messageContent);
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {
            }

            private void decodeMessage(String status) {

                if (status.contains("front") || status.contains("side")) {
                    //OK then
                } else {
                    LOGGER.info("MQTT status missing place name " + status);
                    return;
                }

                if (status.contains(" on") || status.contains(" off")) {
                    //OK then
                } else {
                    LOGGER.info("MQTT status missing switch status " + status);
                    return;
                }

                boolean lit = status.contains(" on");
                String colour = lit ? "-fx-background-color:limegreen; " : "-fx-background-color:coral; ";

                if (status.contains("front")) {
                    btnSwitch1.setStyle(colour);
                    lightsFront = lit;
                }

                if (status.contains("side")) {
                    btnSwitch2.setStyle(colour);
                    lightsSide = lit;
                }

            }

        });
    }

}

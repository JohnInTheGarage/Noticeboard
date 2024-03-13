/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package driverway.nb.utils;

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
public final class MqttHelper {

    private static final Logger LOGGER = LogManager.getLogger();
    
    //Using singleton pattern to avoid confusing MQTT with multiple instances
    private static MqttHelper INSTANCE;
    
    // MQTT parameters for switch buttons
    private MqttClient lightingClient = null;
    private MqttClient statusClient = null;
    private String mqttServer;
    private String topicLighting;
    private String topicStatus;
    private String username;
    private String password;
    private String lightingId;
    private String statusId;
    private int qos = 0;

    // indexing from 1 to match relay numbers
    boolean[] lights = new boolean[4];

    public static MqttHelper getInstance(){
		if (INSTANCE == null)
            INSTANCE = new MqttHelper();
		
        return INSTANCE;
	}
    
    private MqttHelper() {

        PreferenceHelper ph = PreferenceHelper.getInstance();
        if (ph.getItem("mqttSwitches").isBlank()) {
            LOGGER.info("MTQQ Not used, mqttSwitches.isBlank() in properties file");
            return;
        }
        try {
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
            lightingClient.subscribe(topicLighting, qos);
            
            setUpStatusClient();
            statusClient.connect(options);
            statusClient.subscribe(topicStatus, qos);
            
            LOGGER.info("Lighting Client connected :" + lightingClient.isConnected());
            LOGGER.info("Status Client connected :" + statusClient.isConnected());

        } catch (MqttException ex) {
            LOGGER.info("MTQQ problems initialising :" + ex.getMessage());
        }

    }

    public boolean[] queryLights() {
        return lights;
    }

    /*
    * Changing from HTTP to MQTT means no immeadiate response is available
    * so no return-type
     */
    public void sendLightsCommand(String command) {
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
                LOGGER.info("lights status message received :" + messageContent);
                decodeMessage(messageContent);
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {
            }

            private void decodeMessage(String status) {

                if (status.contains("front") 
                || status.contains("side")
                || status.contains("garage")) {
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

                if (status.contains("front")) {
                    lights[1] = lit;
                }

                if (status.contains("side")) {
                    lights[2] = lit;
                }

                if (status.contains("garage")) {
                    lights[3] = lit;
                }

            }

        });
    }

}

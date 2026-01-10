package driverway.nb.launch;

import driverway.nb.utils.PropertyLoader;
import driverway.nb.screens.DashboardScreen;
import driverway.nb.screens.SatelliteImagePane;
import java.io.IOException;
import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.Optional;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.concurrent.ScheduledService;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.EventHandler;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TouchEvent;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.spi.LoggerContext;
import org.apache.logging.log4j.core.config.xml.XmlConfiguration;

/**
 * Needs Bellsoft full JDK for on development PC which has JavaFX built-in for
 * building, And Bellsoft full JRE for Arm (also containing JavaFX) for
 * deployment on Raspberry Pi.
 * 
 * Since Wayland, run from a systemd service; e.g. 
 *      for an installation under a user called john,
 *      in a directory called NB 
 *      run by a service called NB.service
 *      with an environment variable (for the proprty files directory) of NBPROPERTIES
 * 
 * create a file with :
 * sudo nano /etc/systemd/system/NB.service
 * and insert lines such as these

[Unit]
Description=NoticeBoard (NB) app
After=graphical.target
Wants=graphical.target

[Service]
User=john
WorkingDirectory=/home/john/NB
Environment=NBPROPERTIES=/media/john/SATELLITE/properties
Environment=DISPLAY=:0
Environment=XAUTHORITY=/home/john/.Xauthority
ExecStart=/usr/bin/java -Xmx75M -jar /home/john/NB/NoticeBoard.jar
# (-Xmx75M added to stop it using too much available memory on Pi 3a+)

#Restart=on-failure
#RestartSec=10
TimeoutStartSec=240

# Send stdout and stderr to the journal
StandardOutput=journal
StandardError=journal
# Optional: tag log lines so you can filter just your app
SyslogIdentifier=NB

[Install]
WantedBy=graphical.target


 * ---------------------------------------------------------- 
 * Set up "crontab -e" (i.e. don't use sudo to set it up) with these lines to 
 * reboot pi daily and turn screen off overnight 
 * (out-of-date, see below)
 * 59 23 * * * /usr/bin/sudo sh -c "echo 1 > /sys/class/backlight/rpi_backlight/bl_power" 
 * 00 06 * * * /usr/bin/sudo sh -c "echo 0 > /sys/class/backlight/rpi_backlight/bl_power"
 *
 * With Raspian Buster & Bookworm this changes to 
 * 59 23 * * * /usr/bin/sudo sh -c "echo 1 > /sys/class/backlight/10-0045/bl_power" 
 * 00 06 * * * /usr/bin/sudo sh -c "echo 0 > /sys/class/backlight/10-0045/bl_power"
 * Which seems like a shit idea to me, and its not in the official docs.
 *
 * The Property files directory is stored in environment variable for use by
 * System.getenv("NBPROPERTIES") the variable is created with text in a .sh file
 * kept in directory /etc/profile.d   e.g. 
 * export NBPROPERTIES=<your preferred location>
 *
 *
 */
public class App extends Application {

    private static final Logger LOGGER = LogManager.getLogger();
    //private ScheduledService<Integer> clockService;
    private StackPane stack;
    private LocalDateTime hideTimestamp = null;
    //private static final String CLOCKSERVICEFAIL = ">>>>>>>>>>>>> Clock service failed <<<<<<<<<<<<<";
    private static final String SAT_ID = "satellite";
    private SatelliteImagePane satImagePane;
    private static final String PI_ARCH = "arm-aarch64";


    static {
        if (PI_ARCH.contains(System.getProperty("os.arch"))) {
            String externalConfigPath = "./log4j2.xml";
            java.io.File externalConfig = new java.io.File(externalConfigPath);

            if (!externalConfig.exists()) {
                // Copy packaged config to external location on first run
                try (java.io.InputStream is = App.class.getResourceAsStream("/log4j2.xml"); java.io.FileOutputStream fos = new java.io.FileOutputStream(externalConfig)) {

                    if (is != null) {
                        is.transferTo(fos);
                        System.out.println("Created log4j2.xml at " + externalConfigPath);
                    }
                } catch (Exception e) {
                    System.err.println("Failed to copy log4j2.xml: " + e.getMessage());
                }
            }

            // Use external config if it exists
            if (externalConfig.exists()) {
                System.setProperty("log4j.configurationFile", externalConfigPath);
            }
        }
    }

    @Override
    public void start(Stage stage) throws IOException {

        try {
            LoggerContext logContext = LogManager.getContext();
            Field f = logContext.getClass().getDeclaredField("configuration");
            f.setAccessible(true);
            XmlConfiguration configFile = (XmlConfiguration) f.get(logContext);
            LOGGER.info("Config File: " + configFile.getName());
        } catch (Exception e) {
            //Oh well nevermind...
        }

        PropertyLoader pl = new PropertyLoader();
        DashboardScreen noticeboard = new DashboardScreen(pl);

        satImagePane = new SatelliteImagePane(pl);
        satImagePane.setId(SAT_ID);

        //  Touch equates to Mouse-click on my touchscreen
        satImagePane.setOnTouchPressed(new EventHandler<TouchEvent>() {
            public void handle(TouchEvent event) {
                ObservableList<Node> childs = stack.getChildren();
                var backPane = childs.get(0);
                backPane.toFront();
                hideTimestamp = null;
                //LOGGER.trace("++++++++ this was Touch Event +++++++++");
            }

        });

        satImagePane.setOnMouseClicked(new EventHandler<MouseEvent>() {
            public void handle(MouseEvent event) {
                ObservableList<Node> childs = stack.getChildren();
                var backPane = childs.get(0);
                backPane.toFront();
                hideTimestamp = null;
                //LOGGER.trace("++++++++ this was Mouse Event +++++++++");
            }

        });

        stack = new StackPane();
        stack.getChildren().addAll(noticeboard, satImagePane);

        var scene = new Scene(stack, 800, 480);
        scene.setCursor(Cursor.NONE);
        stage.setFullScreenExitHint("");
        if (PI_ARCH.contains(System.getProperty("os.arch"))) {
            stage.setFullScreen(true);
        }
        LOGGER.trace("Architecture is " + System.getProperty("os.arch"));

        stage.setOnCloseRequest(e -> {
            e.consume();
            closeProgram(stage);
        });

        stage.setScene(scene);
        scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());
        stage.show();

        Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(10), event -> {
            LocalDateTime timestamp = LocalDateTime.now();
            noticeboard.setClock(timestamp);
            checkSatPane(timestamp);
            noticeboard.checkForecast();
            noticeboard.checkAppointments();
        }));
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();

        /* /======================================
        clockService = new ScheduledService<Integer>() {
            @Override
            protected Task<Integer> createTask() {
                Task<Integer> tickTock = null;
                return new TickTockTask();
            }
        };

        //===============================================================
        clockService.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
            @Override
            public void handle(WorkerStateEvent t) {
                LocalDateTime timestamp = LocalDateTime.now();
                noticeboard.setClock(timestamp);
                checkSatPane(timestamp);
                noticeboard.checkForecast();
                noticeboard.checkAppointments();
            }

            / *
			* Check if its time to re-show the satellite image instead of the noticeboard
			* And if the 1-th (i.e. visible) pane is the satellite, slide the image around a bit
            * /
            private void checkSatPane(LocalDateTime timestamp) {
                ObservableList<Node> childs = stack.getChildren();
                var backPane = childs.get(0);
                var test = childs.get(1);
                if (backPane.getId().equals(SAT_ID)) {
                    if (hideTimestamp == null) {
                        hideTimestamp = timestamp;
                    } else {
                        if (hideTimestamp.isBefore(timestamp.minusMinutes(5))) {
                            backPane.toFront();
                        }
                    }
                }
                if (childs.get(1).getId().equals(SAT_ID)) {
                    satImagePane.slideImage3(timestamp);
                }
            }
        });

        //===============================================================
        clockService.setOnFailed(new EventHandler<WorkerStateEvent>() {
            @Override
            public void handle(WorkerStateEvent t) {
                System.err.println(CLOCKSERVICEFAIL);
                LOGGER.fatal(CLOCKSERVICEFAIL);
            }
        });

        clockService.setDelay(Duration.seconds(25));
        clockService.setPeriod(Duration.seconds(10));
        clockService.setRestartOnFailure(true);
        clockService.start();
         */
        //=========================================
    }

    /*
	 * Check if its time to re-show the satellite image instead of the noticeboard
	 * And if the 1-th (i.e. visible) pane is the satellite, slide the image around a bit
     */
    private void checkSatPane(LocalDateTime timestamp) {
        ObservableList<Node> childs = stack.getChildren();
        var backPane = childs.get(0);
        //var test = childs.get(1);
        if (backPane.getId().equals(SAT_ID)) {
            if (hideTimestamp == null) {
                hideTimestamp = timestamp;
            } else {
                if (hideTimestamp.isBefore(timestamp.minusMinutes(5))) {
                    backPane.toFront();
                }
            }
        }
        if (childs.get(1).getId().equals(SAT_ID)) {
            satImagePane.slideImage3(timestamp);
        }
    }

    public static void main(String[] args) {
        launch();
    }

    private void closeProgram(Stage stage) {
        Alert alert = new Alert(AlertType.CONFIRMATION);
        alert.setTitle("Confirmation");
        alert.setHeaderText("OK to close?");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.get() == ButtonType.OK) {
            stage.close();
            Platform.exit();
            System.exit(0);
        }
    }

    /*
	Just something to activate at the clock-tick interval
     */
    private class TickTockTask extends Task<Integer> {

        @Override
        protected Integer call() throws Exception {
            return 0;
        }
    }

}

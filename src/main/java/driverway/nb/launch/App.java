package driverway.nb.launch;

import driverway.nb.utils.PropertyLoader;
import driverway.nb.screens.DashboardScreen;
import driverway.nb.screens.SatelliteImagePane;
import java.io.IOException;
import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.Optional;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.concurrent.ScheduledService;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.input.MouseEvent;
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
 * Autostart the application by these 4 lines in
 * /etc/xdg/lxsession/LXDE-pi/autostart
 *
 * @lxpanel --profile LXDE-pi
 * @pcmanfm --desktop --profile LXDE-pi
 * @xscreensaver -no-splash
 * @/usr/bin/java -jar /home/pi/NB/NoticeBoard.jar > /home/pi/start.txt
 *
 * ---------------------------------------------------------- 
 * Set up "crontab -e" (i.e. don't use sudo to set it up) with these lines to 
 * reboot pi daily and turn screen off overnight 
 * 59 23 * * * /usr/bin/sudo sh -c "echo 1 > /sys/class/backlight/rpi_backlight/bl_power" 
 * 00 06 * * * /usr/bin/sudo sh -c "echo 0 > /sys/class/backlight/rpi_backlight/bl_power"
 *
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
    private ScheduledService<Integer> clockService;
    private StackPane stack;
    private LocalDateTime hideTimestamp = null;
    private static final String CLOCKSERVICEFAIL = ">>>>>>>>>>>>> Clock service failed <<<<<<<<<<<<<";
    private static final String SAT_ID = "satellite";

    static {
        if (System.getProperty("os.arch").equals("arm")) {
            System.setProperty("log4j.configurationFile", "/home/pi/log4j2.xml");
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

        SatelliteImagePane satImagePane = new SatelliteImagePane(pl);
        satImagePane.setId(SAT_ID);

        /*  Touch equates to Mouse-click on my touchscreen
		satImagePane.setOnTouchPressed(new EventHandler<TouchEvent>() {
			public void handle(TouchEvent event) {
				ObservableList<Node> childs = stack.getChildren();
				var backPane = childs.get(0);
				backPane.toFront();
				hideTimestamp = null;
				LOGGER.trace("++++++++ this was Touch Event +++++++++");
			}

		});
         */
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
        stage.setFullScreenExitHint("");
        if (System.getProperty("os.arch").equals("arm")) {
            stage.setFullScreen(true);
        }

        stage.setOnCloseRequest(e -> {
            e.consume();
            closeProgram(stage);
        });

        stage.setScene(scene);
        scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());
        stage.show();

        //======================================
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

            /*
			* Check if its time to re-show the satellite image instead of the noticeboard
			* And if the 1-th (i.e. visible) pane is the satellite, slide the image around a bit
             */
            private void checkSatPane(LocalDateTime timestamp) {
                ObservableList<Node> childs = stack.getChildren();
                var backPane = childs.get(0);
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

        clockService.setDelay(Duration.seconds(10));
        clockService.setPeriod(Duration.seconds(10));
        clockService.setRestartOnFailure(true);
        clockService.start();

        //=========================================
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

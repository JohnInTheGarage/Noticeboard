package driverway.nb.controllers;

import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import org.apache.logging.log4j.LogManager;

/**
 *
 * @author john
 */
public class ClockPaneController implements Initializable {

    private static final org.apache.logging.log4j.Logger LOGGER = LogManager.getLogger();

    @FXML
    public Label timeLabel;
    @FXML
    public Label dateLabel;

    private String prevTime = " ";
    private String prevDate;
    private boolean newDay;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
    }

    public void setClock(LocalDateTime rightNow) {

        String hhmm = rightNow.format(DateTimeFormatter.ofPattern("HH:mm"));
        String date = rightNow.format(DateTimeFormatter.ofPattern("E, d MMM uuuu"));
        if (!hhmm.equals(prevTime)) {
            timeLabel.setText(hhmm);
        }

        setNewDay(false);
        if (!date.equals(prevDate)) {
            LOGGER.trace("new Date:" + date);
            setNewDay(true);
            dateLabel.setText(date);
        }

        prevTime = hhmm;
        prevDate = date;

    }

    /**
     * @return the newDay
     */
    public boolean isNewDay() {
        return newDay;
    }

    /**
     * @param newDay the newDay to set
     */
    private void setNewDay(boolean newDay) {
        this.newDay = newDay;
    }

}

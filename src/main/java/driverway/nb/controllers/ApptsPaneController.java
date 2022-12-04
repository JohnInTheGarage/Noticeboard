package driverway.nb.controllers;

import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ListView;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author john
 */
public class ApptsPaneController implements Initializable {

	
	private static final Logger LOGGER = LogManager.getLogger();
	
	@FXML public ListView apptsList;

	private LocalDate today;
	private Appointments apptsData;

	public ApptsPaneController (){
	}

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		
	}
	

	public void setDate(LocalDate suppliedDate) {
		today = suppliedDate;
	}


	public void showList() {
		Map<LocalDateTime, String> data = apptsData.getApptsByList(today);

		ObservableList elements = FXCollections.observableArrayList();

        String next = "";
		for (LocalDateTime ts : data.keySet()) {
			String hhmm = ts.format(DateTimeFormatter.ofPattern("HH:mm"));
			int day = ts.getDayOfMonth();
			String subject = data.get(ts);
            if (ts.getMonth() != today.getMonth()){
                next = ts.getMonth().getDisplayName(TextStyle.SHORT, Locale.UK)+" ";
            }

			//LOGGER.trace("found "+subject);
			if (hhmm.equals("00:00")) {
				elements.add(next + day + abbrv(day) + subject);
			} else {
				elements.add(next + day + abbrv(day) + hhmm + " " + subject);

			}

		}
		apptsList.setItems(elements);
		
	}

	private String abbrv(int day) {

		switch (day) {
			case (1):
			case (21):
			case (31):
				return "st ";

			case (2):
			case (22):
				return "nd ";

			case (3):
			case (23):
				return "rd ";

			default:
				return "th ";
		}
	}

	/**
	 * @param apptsData the apptsData to set
	 */
	public void setAppointments(Appointments apptsData) {
		LOGGER.trace("New appointments");
		this.apptsData = apptsData;
		showList();
	}

}

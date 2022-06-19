package driverway.nb.controllers;

import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.ResourceBundle;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


/**
 *
 * @author john
 */
public class CalendarGridPaneController implements Initializable {

	private static final Logger LOGGER = LogManager.getLogger();
	
	@FXML public GridPane calenderGrid;
	
	private LocalDate CalendarbaseDate;
	private Appointments apptsData;
	private ArrayList<Integer> decoratedDays = new ArrayList<>();
	private String weekdayColour = "blue";
	private int todayNum;
	
	public CalendarGridPaneController(){
	}
			
	@Override
	public void initialize(URL location, ResourceBundle resources) {
	
	}

	public void changeDay() {
		//LOGGER.trace("Blanking calendar for day change");
		ObservableList<Node> days = calenderGrid.getChildren();
		//LOGGER.trace("did calenderGrid.getChildren()");
		try {
			calenderGrid.getChildren().removeAll(days);
			LOGGER.trace("calendar empty");
		}
		catch (Exception wtf){
			LOGGER.error("Unable to clear calendar ", wtf.getMessage());
		}
	}

	public void setDate(LocalDate suppliedDate) {
		CalendarbaseDate = suppliedDate;
	}

	public void showCalendar(){
		//LOGGER.trace("Showing Calendar");
		if (apptsData != null){
			decoratedDays = apptsData.getDatesThisMonth(CalendarbaseDate);
		}
		todayNum = CalendarbaseDate.getDayOfMonth();
		int lastDayOfMonth = CalendarbaseDate.lengthOfMonth();
		LocalDate monthStart = CalendarbaseDate.minusDays(todayNum - 1);
		
		setDayNames();
		
		int week = 1;
		int displayDay = 1;
		int column = monthStart.getDayOfWeek().getValue();
		while (column < 8){
			decorateDay(displayDay, column-1, week);
			column ++;
			displayDay++;
		}

		// column=8 here
		while (displayDay <= lastDayOfMonth) {
			if (column > 7){
				column=1;
				week++;
			}
			decorateDay(displayDay, column-1, week);
			column ++;
			displayDay++;
		}
	
	}
	
	private void setDayNames(){
		for (int col = 0; col < 7; col ++){  
			int start = (col+1) * 3;
			Label lab = new Label("...MonTueWedThuFriSatSun---".substring(start, start+3));	
			lab.setStyle("-fx-font-size: 12pt;");
			calenderGrid.add(lab, col, 0);
		}
	}
	
	private void decorateDay(int displayDay, int col, int week) {
		String textColourCSS = "";
		String borderCSS = "";
		String backgroundCSS= "";
		Label lab = new Label("  " +displayDay+ "  ");
		
		if (decoratedDays.contains(displayDay)){
			borderCSS = "-fx-border-color: rgb(255, 255, 255); -fx-border-radius: 5;";
		}
		if (displayDay == todayNum){
			textColourCSS = "-fx-text-fill: yellow;";
			backgroundCSS = "-fx-background-color:blue;";
		}
		if (col > 4){
			textColourCSS = "-fx-text-fill:darkred;";
		} else {
			if (displayDay != todayNum){
				textColourCSS = "-fx-text-fill:" +weekdayColour+";";
			}
		}
		
		lab.setStyle(textColourCSS + backgroundCSS + borderCSS + "-fx-font-size: 20pt;");
		calenderGrid.add(lab, col, week);
		
	}

	private String showHHMM(LocalDateTime when){
		return when.format(DateTimeFormatter.ofPattern("HH:mm"));
	}

	/**
	 * @param apptsData the apptsData to set
	 */
	public void setAppointments(Appointments apptsData) {
		//LOGGER.trace("new Appointments");
		this.apptsData = apptsData;
		showCalendar();
		
	}

}

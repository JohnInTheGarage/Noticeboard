package driverway.nb.controllers;

import driverway.nb.calendar.CalendarScanner;
import driverway.nb.utils.SambaCaller;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.security.GeneralSecurityException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author john 
 * Holds appointments, supplies a list of them to calendarPane and
 * ApptsListPane, by day for calendar, and by date+time for the List.
 *
 */
public class Appointments {

	private static final Logger LOGGER = LogManager.getLogger();

	private String problem;
	private boolean OK;
	private final String urlSMB = "smb://pihole.local/backupshare/appts.txt";
	private TreeMap<LocalDateTime, String> items = new TreeMap<>();
	private CalendarScanner calendarScanner;

	public Appointments(String _googleId) {
		calendarScanner = new CalendarScanner(_googleId);
	}

	/*
	* Reads your Google calendar
	*/
	public void fetchAppointmentsGoogle(){
		try {
			items =calendarScanner.scanEvents();
			setOK(true);
		} catch (IOException | GeneralSecurityException ex) {
			LOGGER.error("Exception calling Google", ex);
			setOK(false);
		}
		
	}
	
	
	/*
	* Expected format 
	yyyy-MM-dd HH:mm <text> with time=zeros meaning all-day
	or
	repeat monthly date=nn HH:mm <text>
	or 
	repeat monthly day=mon,tue,etc freq=1,2,3,4,9  HH:mm <text>
	(where 9 = last <day> of month)
	Possible weekly repeats t.b.a. (but they seem a bit pointless)
	 */
	@Deprecated
	public void fetchAppointmentsSamba() {
		setOK(true);
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
		LocalDateTime repeatDate;

		try {
			SambaCaller s = new SambaCaller();
			ArrayList<String> lines = (ArrayList<String>) s.fetch(urlSMB);
			for (String inputLine : lines) {
				if (inputLine != null && inputLine.length() > 20) {
					String itemDate = inputLine.substring(0, 16);
					if (itemDate.contains("repeat") == false) {
						LocalDateTime tempDate = LocalDateTime.parse(itemDate, formatter);
						items.put(tempDate, inputLine.substring(16));
						continue;
					}
					// repeating dates
					int cluePos = inputLine.indexOf("date=");
					if (cluePos > 0) {
						int targetDate = Integer.parseInt(inputLine.substring(cluePos + 5, cluePos + 7), 10);
						LocalTime repeatTime = findTime(inputLine);
						repeatDate = LocalDate.now().withDayOfMonth(targetDate).atTime(repeatTime);
						if (repeatDate.getMonth() == LocalDate.now().getMonth()){
							items.put(repeatDate, inputLine.substring(cluePos +14));
						}
						continue;
					}
					
					cluePos = inputLine.indexOf("day=");
					if (cluePos > 0) {
						int[] repDays = new int[10];
						int index = 0;
						// get Day of week, 1=Mon, 7=Sun
						int dow = Integer.parseInt(inputLine.substring(cluePos + 4, cluePos + 5), 10);
						DayOfWeek targetDOW = DayOfWeek.of(dow);
						//set start point to start of month -1 day (in case day1 is target day-of-week)
						repeatDate = LocalDateTime.now().withDayOfMonth(01).minusDays(1L);
						// find next same day-of-week
						LocalDateTime nextDate = repeatDate.with(TemporalAdjusters.next(targetDOW));
						repeatDate = LocalDateTime.now().withDayOfMonth(01);
						int currentMonth = repeatDate.getMonthValue();
						//if its still in this month, keep it
						while(currentMonth == nextDate.getMonthValue()){
							index++;
							repDays[index] = nextDate.getDayOfMonth();
							repDays[9] = repDays[index];
							nextDate = nextDate.with(TemporalAdjusters.next(targetDOW));
						}
						cluePos = inputLine.indexOf("freq=");
						int freq = Integer.parseInt(inputLine.substring(cluePos + 5, cluePos + 6), 10);
						// which occurance do we want to use?
						if (freq > 5){
							freq=9;
						}
						LocalTime repeatTime = findTime(inputLine);
						repeatDate = LocalDate.now().withDayOfMonth(repDays[freq]).atTime(repeatTime);
						items.put(repeatDate, inputLine.substring(cluePos +13));
						continue;
					}


				}

			}
		} catch (Exception e) {
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			e.printStackTrace(pw);
			setProblem(e.getMessage() + " : " + sw.toString());
			LOGGER.error("Error with Samba file acess" + e);
			setOK(false);
		}
	}

	/*
	* List of days this month with appointments (For monthly calendar)
	 */
	public ArrayList<Integer> getDatesThisMonth(LocalDate whichMonth) {
		LocalDate date1 = whichMonth.with(TemporalAdjusters.firstDayOfMonth());
		LocalDate date2 = whichMonth.with(TemporalAdjusters.lastDayOfMonth());
		ArrayList<Integer> reply = new ArrayList<>();

		for (LocalDateTime timestamp : items.keySet()) {
			if (timestamp.toLocalDate().isBefore(date1) || timestamp.toLocalDate().isAfter(date2)) {
				continue;
			}
			reply.add(timestamp.getDayOfMonth());
		}

		return reply;
	}

	/*
	*	Map of timestamps and Appts this month excluding those in the past
	*	(for list of appointments this month)
	 */
	public Map<LocalDateTime, String> getApptsByList(LocalDate whichMonth) {
		Map<LocalDateTime, String> reply = new TreeMap<>();
		LocalDate date1 = LocalDate.now();
		LocalDate date2 = whichMonth.with(TemporalAdjusters.lastDayOfMonth());

		for (LocalDateTime timestamp : items.keySet()) {
			if (timestamp.toLocalDate().isBefore(date1) || timestamp.toLocalDate().isAfter(date2)) {
				continue;
			}
			reply.put(timestamp, items.get(timestamp));
			LOGGER.trace("Found "+items.get(timestamp));
		}

		return reply;
	}

	private LocalTime findTime(String text) {
		int pos = text.indexOf(":");
		pos = pos -2;
		return LocalTime.parse(text.substring(pos, pos+5), DateTimeFormatter.ofPattern("HH:mm"));
	}

	/**
	 * @return the problem
	 */
	public String getProblem() {
		return problem;
	}

	/**
	 * @param problem the problem to set
	 */
	public void setProblem(String problem) {
		this.problem = problem;
	}

	/**
	 * @return the OK
	 */
	public boolean isOK() {
		return OK;
	}

	/**
	 * @param OK the OK to set
	 */
	public void setOK(boolean OK) {
		this.OK = OK;
	}


}

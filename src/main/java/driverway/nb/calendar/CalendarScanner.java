package driverway.nb.calendar;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.util.DateTime;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.CalendarScopes;
import com.google.api.services.calendar.model.CalendarListEntry;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.Events;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author john
 *
 *
 *
 * N.B. service accounts now have to "accept" the other calendar :
 * https://developers.google.com/calendar/v3/reference/calendarList/insert
 *
 *
 *
 */
public class CalendarScanner {

	private static final String APPLICATION_NAME = "Noticeboard";
	private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
	private static final Logger LOGGER = LogManager.getLogger();

	/**
	 * Global instance of the scopes required by this code. If modifying these
	 * scopes, delete your previously saved tokens/ folder.
	 */
	private static final List<String> SCOPES = new ArrayList<>();
	private static final String CREDENTIALS_FILE_PATH = System.getenv("NBPROPERTIES") + System.getProperty("file.separator")
			+ "GoogleServiceCredentials.json";
	private String calendarId;

	public CalendarScanner(String _calendarId){
		calendarId = _calendarId;
	}
	
	/*
	 * Find the next 10 events from now
	 */
	public TreeMap<LocalDateTime, String> scanEvents() throws FileNotFoundException, IOException, GeneralSecurityException {
		TreeMap<LocalDateTime, String> calendarEvents = null;
		SCOPES.add(CalendarScopes.CALENDAR_READONLY);
		SCOPES.add(CalendarScopes.CALENDAR);
		HttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();

		//Build service account credential
		GoogleCredentials googleCredentials = GoogleCredentials
				.fromStream(new FileInputStream(CREDENTIALS_FILE_PATH))
				.createScoped(SCOPES);
		HttpRequestInitializer requestInitializer = new HttpCredentialsAdapter(googleCredentials);

		Calendar service = new Calendar.Builder(httpTransport, JSON_FACTORY, requestInitializer)
				.setApplicationName(APPLICATION_NAME)
				.build();

		// Create a new calendar list entry
		CalendarListEntry calendarListEntry = new CalendarListEntry();
		calendarListEntry.setId(calendarId);

		// Insert the new calendar list entry
		// often get network unreachable here, so repeat till OK
		CalendarListEntry createdCalendarListEntry = null;
		while (createdCalendarListEntry == null) {
			try {
				createdCalendarListEntry = service.calendarList().insert(calendarListEntry).execute();
				LOGGER.trace("Inserted calendarList");
			} catch (Exception e) {
				LOGGER.error("Exception fetching calendar list ",e.getMessage());
				try {
					Thread.sleep(10000);
				} catch (InterruptedException ex) {
					
				}
			}
		}

		DateTime now = new DateTime(System.currentTimeMillis());
		Events events = service.events().list(calendarId)
				.setMaxResults(10)
				.setTimeMin(now)
				.setOrderBy("startTime")
				.setSingleEvents(true)
				.execute();
		List<Event> items = events.getItems();
		DateTimeFormatter formatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME;

		calendarEvents = new TreeMap<>();
		for (Event e : items) {
			String start;
			if (e.getStart().getDateTime() != null){
				start = e.getStart().getDateTime().toString();
				//2021-05-26T00:00:00.000+00:00
			} else {
				// All-day events
				String tz = e.getStart().getTimeZone();
				start = e.getStart().getDate() +"T00:00:00.000+00:00";
			}
			LocalDateTime ldt = LocalDateTime.parse(start, formatter);
			calendarEvents.put(ldt, e.getSummary());
			LOGGER.trace("From Google :" +ldt +" === "+e.getSummary()+ " ("+e.getStatus()+")");
		}
		return calendarEvents;
	}

}

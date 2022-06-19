package driverway.nb.weatherfinder;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;

/**
 *
 * @author john
 */
public abstract class AbstractWeatherDecoder {

	private ZoneId localTimezone;
	private Forecast forecast;

	abstract Forecast decodeJSON(String json);

	AbstractWeatherDecoder(String tzName) {
		localTimezone = ZoneId.of(tzName);
	}

	LocalDateTime parseDate(String value) {
		LocalDateTime ldt;
		try {
			ZonedDateTime zdt = ZonedDateTime.parse(value);
			ldt = convertToLocal(zdt);
		} catch (Exception e) {
			// maybe already local
			ldt = LocalDateTime.parse(value);
		}

		return ldt;
	}

	/*
	* Use your local timezone for UTC timestamps
	 */
	LocalDateTime convertToLocal(ZonedDateTime utcZoned) {
		ZonedDateTime spainZoned = utcZoned.withZoneSameInstant(localTimezone);
		return spainZoned.toLocalDateTime();
	}

	LocalDateTime fixUnixDate(int digits) {
		Date date = new Date((long)digits * 1000l);
		return date.toInstant()
				.atZone(localTimezone)
				.toLocalDateTime();
	}
}

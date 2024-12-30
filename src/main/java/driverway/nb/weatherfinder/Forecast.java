package driverway.nb.weatherfinder;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author john
 */
public class Forecast {

	private LocalDateTime modelRunDate;
	private ArrayList<Period> periods;
	private ArrayList<WeatherAlert> alerts;
	private int todaysMaxRainProb;
	private Double todaysMaxTemp;
    private double moonAge;
	private final ZoneId zoneUTC = ZoneId.of("UTC");
	private final ZoneId zoneSpain = ZoneId.of("Europe/Madrid");
	private boolean OK;
	private static Logger LOGGER = LogManager.getLogger();

	Forecast() {
		todaysMaxRainProb = -1;
		todaysMaxTemp = -99.9;
		periods = new ArrayList<>();
		alerts = new ArrayList<>();
		setOK(false);
	}

	/*
	* Store a period but only if it starts in the future
	* Done here to centralise it instead of repeating in all the 
	* different Decoder methods there may be (3 at present).
	*/
	public void storePeriod(Period p) {
		// Allow some hours earlier than "now" so that the earliest period
		// can include current time
		if (p.getStartTime().isAfter( LocalDateTime.now().minusHours(3) )){
			periods.add(p);
		} else{
			LOGGER.debug(" - not storing period "+p.getStartTimeText());
		}
	}

	
	/**
	 * @return the alerts
	 */
	public ArrayList<WeatherAlert> getAlerts() {
		return alerts;
	}

	/**
	 * @param alerts the alerts to set
	 */
	public void setAlerts(ArrayList<WeatherAlert> alerts) {
		this.alerts = alerts;
	}

	/**
	 * @return the modelRunDate
	 */
	public LocalDateTime getModelRunDate() {
		return modelRunDate;
	}

	/**
	 * @return the modelRunDate in human form
	 */
	public String getHumanReadableRunDate() {
		return String.format("From %s", modelRunDate.format(DateTimeFormatter.ofPattern("HH:mm")));
	}

	/**
	 * @param modelRunDate the modelRunDate to set
	 */
	public void setModelRunDate(LocalDateTime modelRunDate) {
		this.modelRunDate = modelRunDate;
	}

	/*
	* Returns max index allowed, not count of periods
	*/
	public int getPeriodMax() {
		return periods.size() - 1;
	}

	public Period getPeriod(int p) {
		return periods.get(p);
	}

	public int getTodaysMaxRainProb() {
		return todaysMaxRainProb;
	}

	public Double getTodaysMaxTemp() {
		return todaysMaxTemp;
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

	/*
	* Throw away periods that are too old.  
	* Spanish forecast often has data in the past for "today"
	* Then, find the max temp and max rain probability for today.
	 */
	public void setTodaysNumbers() {

//		LOGGER.debug("Set today's numbers...");
//		LocalDateTime timeStampStart = LocalDateTime.now();
//		for(Period p : periods) {
//			if (p.getStartTime().isBefore(timeStampStart)) {
//				LOGGER.debug("removed period "+p.getStartTimeText());
//				periods.remove(p);
//			}
//		}
//		LOGGER.debug("After Set today's numbers.");

		if (!periods.isEmpty()) {
			// in case we removed them all as being too old - i.e. when today's forecast is not available

			LocalDate dateStart = getPeriod(0).getStartDate();
			for (Period p : periods) {
				if (p.getStartDate().compareTo(dateStart) == 0) {
					if (p.getRawMaxTemp() > todaysMaxTemp) {
						todaysMaxTemp = p.getRawMaxTemp();
					}
					if (p.getRawProbOfPrecip() > todaysMaxRainProb) {
						todaysMaxRainProb = p.getRawProbOfPrecip();
					}
				}
			}
		}
	}

    /**
     * @return the moonAge
     */
    public int getMoonPhaseNumber() {
        int test = (int)moonAge;
        int phaseNumber;
        // As the moonPhase images are not sceintifically distributed...
        if (test == 7){
            phaseNumber =  4;
        }
        
        if (test == 21){
            phaseNumber = 10;
        } else {
            phaseNumber = (int)moonAge/2;
            if (phaseNumber > 13){
                phaseNumber = 13;
            }
        }
        
        return phaseNumber;
    }

    public double getMoonAge(){
        return this.moonAge;
    }
    
    /**
     * @param moonAge the moonAge to set
     */
    public void setMoonAge(double moonAge) {
        this.moonAge = moonAge;
    }

}

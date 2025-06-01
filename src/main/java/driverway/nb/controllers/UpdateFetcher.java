package driverway.nb.controllers;

import driverway.nb.utils.PropertyLoader;
import driverway.nb.utils.PreferenceHelper;
import driverway.nb.weatherfinder.Forecast;
import driverway.nb.weatherfinder.WeatherReader;

import static java.lang.Thread.sleep;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Properties;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import driverway.sunandmoondata.SunAndMoonData;

/**
 *
 * @author john
 */
public class UpdateFetcher implements Runnable {

    private static final Logger LOGGER = LogManager.getLogger();
    private Forecast forecast;
    private int sunMoonDay = 0;
    private LocalDateTime lastForecast;
    private LocalDateTime lastAppointments;
    private LocalDateTime nextForecast;
    private LocalDateTime nextAppointments;
    private final String providerCode;
    private final String googleId;
    private Appointments apptsData;
    private final WeatherReader louiseLear;
    private final PreferenceHelper ph;
    private final Properties nbProperties;
    private final DateTimeFormatter formatDateAndTime = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private Properties samd;
    private double moonAge;
    
    //Minutes, not seconds
    private final int intervalForecast;
    private final int intervalAppointments;

    /**
     *
     * @param pl is a PropertyLoader
     */
    public UpdateFetcher(PropertyLoader pl) {
        ph = PreferenceHelper.getInstance();
        nbProperties = pl.load("noticeboard.properties");
        String forecastFrequency = nbProperties.getProperty("ForecastRequestInterval");
        String appointmentsFrequency = nbProperties.getProperty("GoogleRequestInterval");
        
        // intervals in minutes
        intervalForecast = Integer.parseInt(forecastFrequency, 10);
        intervalAppointments = Integer.parseInt(appointmentsFrequency, 10);
                
        louiseLear = new WeatherReader(nbProperties);
        providerCode = nbProperties.getProperty("WeatherProvider", "OW");
        ph.putItem("forecastProvider", providerCode);
        ph.putItem("ForecastRequestInterval", forecastFrequency);
        ph.putItem("GoogleRequestInterval", appointmentsFrequency);

        LOGGER.trace("WeatherProvider for Forecast :" + providerCode);
        googleId = nbProperties.getProperty("GoogleId");

        //MQTT properties for use in Admin page
        ph.putItem("mqttSwitches", nbProperties.getProperty("mqttSwitches"));
        ph.putItem("mqttServer", nbProperties.getProperty("mqttServer"));
        ph.putItem("topicLighting", nbProperties.getProperty("topicLighting"));
        ph.putItem("topicStatus", nbProperties.getProperty("topicStatus"));
        ph.putItem("username", nbProperties.getProperty("username"));
        ph.putItem("password", nbProperties.getProperty("password"));
        ph.putItem("lightingId", nbProperties.getProperty("lightingId"));
        ph.putItem("statusId", nbProperties.getProperty("statusId"));

    }

    @Override
    public void run() {
        apptsData = new Appointments(googleId);
        nextForecast = LocalDateTime.now().minusHours(1);
        nextAppointments = nextForecast;
        // stop them being used before we are ready
        lastForecast = LocalDateTime.now().plusHours(1);
        lastAppointments = lastForecast;
        
        
        while (true) {
            LocalDateTime rightNow = LocalDateTime.now();
            if (rightNow.isAfter(nextForecast)) {
                findNewForecast();
                lastForecast = rightNow;
                nextForecast = rightNow.plusMinutes(intervalForecast);        
                ph.putItem("lastForecast", rightNow.format(formatDateAndTime));
            }

            if (rightNow.isAfter(nextAppointments)) {
                findNewAppointments();
                lastAppointments = rightNow;
                nextAppointments = rightNow.plusMinutes(intervalAppointments);
                ph.putItem("lastAppointments", rightNow.format(formatDateAndTime));
            }
            try {
                sleep(60000);
            } catch (InterruptedException ex) {
            }

        }
    }

    public void findNewAppointments() {
        getApptsData().fetchAppointmentsGoogle();     //.fetchAppointmentsSamba(); 
        if (getApptsData().isOK()) {
        } else {
            LOGGER.error("failed to get Appointments");
        }
    }

    public void findNewForecast() {
        Forecast latest = louiseLear.readWeather();
        
        if (sunMoonDay != LocalDate.now().getDayOfMonth()){
            SunAndMoonData sunMoonParser = new SunAndMoonData();
            String sunMoonURL = nbProperties.getProperty("SunAndMoonURL");
            String timeZone = nbProperties.getProperty("LocalTimeZone");
            String latitude = nbProperties.getProperty("UkMetOfficeLatitude");
            String longitude = nbProperties.getProperty("UkMetOfficeLongitude");
            samd = sunMoonParser.collectData(sunMoonURL, timeZone, latitude, longitude);
            ph.putItem("sunrise", samd.getProperty("sunrise", "00:00"));
            ph.putItem("sunset", samd.getProperty("sunset", "00:00"));
            double moonAngle = Double.parseDouble(samd.getProperty("moonangle", "0.0"));
            moonAge = 30 * (moonAngle / 360);
            sunMoonDay = LocalDate.now().getDayOfMonth();
        } 
        
        latest.setMoonAge(moonAge);  
        // Not part of the weather, but displayed at the same time 
        // so stored in Forecast.
        
        if (latest.isOK()) {
            setForecast(latest);
        } else {
            LOGGER.error("failed to get Forecast");
        }
    }


    /**
     * @return the forecast
     */
    public Forecast getForecast() {
        return forecast;
    }

    /**
     * @param forecast the forecast to set
     */
    public void setForecast(Forecast forecast) {
        this.forecast = forecast;
    }

    /**
     * @return the apptsData
     */
    public Appointments getApptsData() {
        return apptsData;
    }

    /**
     * @param apptsData the apptsData to set
     */
    public void setApptsData(Appointments apptsData) {
        this.apptsData = apptsData;
    }

    /**
     * @return the lastForecast
     */
    public int getLastForecast() {
        return (lastForecast.getHour() * 100 + lastForecast.getMinute());
    }

    /**
     * @return the lastAppointments
     */
    public int getLastAppointments() {
        return (lastAppointments.getHour() * 100 + lastAppointments.getMinute());
    }

    private String timestamp(LocalDateTime t) {
        return t.format(formatDateAndTime);
    }

    @Override
    public String toString() {
        String thing = String.format("Updatefetcher- Appts- last :%s, next :%s Forecast- last :%s, next ;%s",
            timestamp(lastAppointments), timestamp(nextAppointments), timestamp(lastForecast), timestamp(nextForecast));
        return thing;
    }

}

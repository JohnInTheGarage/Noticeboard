package driverway.nb.controllers;

import driverway.nb.utils.PropertyLoader;
import driverway.nb.utils.PreferenceHelper;
import driverway.nb.weatherfinder.Forecast;
import driverway.nb.weatherfinder.WeatherReader;

import static java.lang.Thread.sleep;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author john
 */
public class UpdateFetcher implements Runnable {

    private static final Logger LOGGER = LogManager.getLogger();
    private Forecast forecast;
    private Appointments apptsData;
    private final WeatherReader louiseLear;
    private LocalTime lastForecast;
    private LocalTime lastAppointments;
    private LocalTime nextForecast;
    private LocalTime nextAppointments;
    private final String providerCode;
    private final String googleId;
    private final PreferenceHelper ph;

    //Minutes, not seconds
    private final int intervalForecast;
    private final int intervalAppointments;

    /**
     *
     * @param pl is a PropertyLoader
     */
    public UpdateFetcher(PropertyLoader pl) {
        ph = PreferenceHelper.getInstance();
        var nbProperties = pl.load("noticeboard.properties");
        String forecastFrequency = nbProperties.getProperty("ForecastRequestInterval");
        String appointmentsFrequency = nbProperties.getProperty("GoogleRequestInterval");

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
        nextForecast = LocalTime.now().minusHours(1);
        nextAppointments = nextForecast;
        // stop them being used before we are reqdy
        lastForecast = LocalTime.now().plusHours(1);
        lastAppointments = lastForecast;

        while (true) {
            LocalTime rightNow = LocalTime.now();
            if (rightNow.isAfter(nextForecast)) {
                findNewForecast();
                lastForecast = rightNow;
                nextForecast = rightNow.plusMinutes(intervalForecast);
                ph.putItem("lastForecast", rightNow.toString());
            }

            if (rightNow.isAfter(nextAppointments)) {
                findNewAppointments();
                lastAppointments = rightNow;
                nextAppointments = rightNow.plusMinutes(intervalAppointments);
                ph.putItem("lastAppointments", rightNow.toString());
            }
            try {
                //LOGGER.trace(toString());
                sleep(60000 * 1);
            } catch (InterruptedException ex) {
            }

        }
    }

    public void findNewAppointments() {
        getApptsData().fetchAppointmentsGoogle();     //.fetchAppointmentsSamba(); 
        if (getApptsData().isOK()) {
            //LOGGER.trace("Appointments OK:");
        } else {
            LOGGER.error("failed to get Appointments");
        }
    }

    public void findNewForecast() {

        Forecast latest = louiseLear.readWeather();
        if (latest != null && latest.isOK()) {
            setForecast(latest);
            //LOGGER.trace("Forecast generated :" + latest.getHumanReadableRunDate());
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

    private String hhmm(LocalTime t) {
        return t.format(DateTimeFormatter.ofPattern("HH:mm"));
    }

    @Override
    public String toString() {
        String thing = String.format("Updatefetcher- Appts- last :%s, next :%s Forecast- last :%s, next ;%s",
            hhmm(lastAppointments), hhmm(nextAppointments), hhmm(lastForecast), hhmm(nextForecast));
        return thing;
    }

}

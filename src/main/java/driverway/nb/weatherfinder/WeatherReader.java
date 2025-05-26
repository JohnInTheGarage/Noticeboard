package driverway.nb.weatherfinder;

import driverway.nb.externals.ESWeatherApi;
import driverway.nb.externals.OWWeatherApi;
import driverway.nb.externals.UKWeatherApi;
import driverway.nb.externals.WeatherApiCaller;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Properties;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author john
 */
public class WeatherReader {

    private static final Logger LOGGER = LogManager.getLogger();
    //private final XMLdecoder avisoDecoder;
    private final String provider;
    private final String timezone;

    private WeatherApiCaller apiCaller;
    private AbstractWeatherDecoder wd;
    private int avisosHour = -1;

    /**
     * Constructor expects the calling application to provide all the required
     * properties
     *
     * @param choices
     */
    public WeatherReader(Properties choices) {
        provider = choices.getProperty("WeatherProvider", "UK");
        timezone = choices.getProperty("LocalTimeZone", "Europe/London");
        apiCaller = serviceFactory(choices);

        switch (provider.toUpperCase()) {
            case "UK":
                wd = new WeatherDecoderUK(timezone);
                break;
            case "ES":
                wd = new WeatherDecoderES(timezone);
                break;
            case "OW":
                wd = new WeatherDecoderOW(timezone);
                break;
        }

    }

    public Forecast readWeather() {
        Forecast fc = null;
        String response = null;

        response = apiCaller.getForecastJSON();
        fc = wd.decodeJSON(response);

        // for Spain, once per hour look for weather alerts
        LocalTime rightNow = LocalTime.now();
        if (rightNow.getHour() != avisosHour) {
            avisosHour = rightNow.getHour();
            fc.setAlerts(apiCaller.getAlerts());
            LOGGER.trace("collected aviso xmls");
        }

        fc.setOK(true);
        return fc;
    }


    private WeatherApiCaller serviceFactory(Properties props) {
        switch (provider.toUpperCase()) {
            case "UK" -> {
                return new UKWeatherApi(props);
            }
            case "ES" -> {
                return new ESWeatherApi(props);
            }
            case "OW" -> {
                return new OWWeatherApi(props);
            }
        }
        return null;

    }

}

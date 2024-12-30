package driverway.nb.weatherfinder;

import driverway.nb.utils.ApiCaller;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Properties;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author john
 */
public class WeatherReader {

	private static final Logger LOGGER = LogManager.getLogger();
	private final XMLdecoder avisoDecoder;
    private final ApiCaller apiCaller = new ApiCaller();
	private final String provider;
	private final String timezone;

	private final String AeMetApiKey;
	private final String AeMetBaseURL;
	private final String AeMetMunicipio;
	private final String AeMetArea;
	private final String AePrediccionPath;
	private final String AeMetAvisosPath;

	private final String UkMetOfficeApiKey;
	private final String UkMetOfficeLatitude;
	private final String UkMetOfficeLongitude;
	private final String UkMetOfficeBaseURL;
    private final String UkMetOfficeParams;
	private AbstractWeatherDecoder wd;
	private int avisosHour = -1;
	private final String OpenWeatherApiKey;
	private final String OpenWeatherBaseURL;
    private final String ViewBitsApi;
    private LocalDate lastMoonCall = null;
    
	/**
	 * Constructor expects the calling application to provide all the required
	 * properties
	 *
	 * @param choices
	 */
	public WeatherReader(Properties choices) {
		provider = choices.getProperty("WeatherProvider", "UK");
		timezone = choices.getProperty("LocalTimeZone", "Europe/London");

		UkMetOfficeBaseURL = choices.getProperty("UkMetOfficeBaseURL");
        UkMetOfficeParams = choices.getProperty("UkMetOfficeParams");
		UkMetOfficeApiKey = choices.getProperty("UkMetOfficeApiKey");
		UkMetOfficeLatitude = choices.getProperty("UkMetOfficeLatitude", "52.0");
		UkMetOfficeLongitude = choices.getProperty("UkMetOfficeLongitude", "0.0");

		AeMetBaseURL = choices.getProperty("AeMetBaseURL");
		AeMetApiKey = choices.getProperty("AeMetApiKey");
		AeMetMunicipio = choices.getProperty("AeMunicipio");
		AeMetArea = choices.getProperty("AeMetArea");
		AePrediccionPath = choices.getProperty("AePrediccionPath");
		AeMetAvisosPath = choices.getProperty("AeMetAvisosPath");
		avisoDecoder = new XMLdecoder();

		OpenWeatherBaseURL = choices.getProperty("OpenWeatherBaseURL");
		OpenWeatherApiKey = choices.getProperty("OpenWeatherApiKey");
        ViewBitsApi = choices.getProperty("ViewBitsApi");
        
        
        
		switch (provider.toUpperCase()) {
			case "UK" -> wd = new WeatherDecoderUK(timezone);
			case "ES" -> wd = new WeatherDecoderES(timezone);
			case "OW" -> wd = new WeatherDecoderOW(timezone);
		}
        

	}

	public Forecast readWeather(boolean callMoonApi) {
		Forecast fc = null;
        String response = null;
		try {
			response = getWeatherJSON();
			if (provider.equalsIgnoreCase("ES")) {
				fc = wd.decodeJSON(response);

				// Once per hour look for weather alerts
				LocalTime rightNow = LocalTime.now();
				if (rightNow.getHour() != avisosHour) {
					avisosHour = rightNow.getHour();
					ArrayList<String> allAlerts = getSpainAdvisories();
					fc.setAlerts(getOurAlerts(allAlerts));
					LOGGER.trace("collected aviso xmls");
				}

			} else {               
				fc = wd.decodeJSON(response);
			}
			fc.setOK(true);
            if (callMoonApi){
                fc.setMoonAge( findMoonAge(LocalDate.now()) );    
            }
		} catch (Exception ex) {
			LOGGER.error("Failed decoding ", ex);
            LOGGER.info(response);
		}
		return fc;
	}

	public String getWeatherJSON() {

		String apiURL;
		String json = null;

		while (json == null) {
			try {
				switch (provider.toUpperCase()) {
					case ("ES"):
						apiURL = AeMetBaseURL + AePrediccionPath + AeMetMunicipio + AeMetApiKey;
						LOGGER.trace("calling ES weather api :" + apiURL);
						json = apiCaller.callSpainAPI(apiURL);
						break;
					case ("UK"):
						String params = String.format(UkMetOfficeParams, UkMetOfficeLatitude, UkMetOfficeLongitude);
                        //Grrr I'm sure they somewhere said "encode the query string". But no. String encodedParams = URLEncoder.encode(params, StandardCharsets.UTF_8.toString());
                        apiURL = UkMetOfficeBaseURL + "?" + params ; // encodedParams;
						LOGGER.trace("calling UK weather api:" + apiURL);
						json = apiCaller.callUKAPI(apiURL, UkMetOfficeApiKey);
						break;
					case ("OW"):
						apiURL = String.format(OpenWeatherBaseURL, OpenWeatherApiKey);
						LOGGER.trace("calling OW weather api:" + apiURL);
						json = apiCaller.callOWAPI(apiURL);
						break;

				}
			} catch (Exception e) {
				LOGGER.error("Exception fetching weather data ", e.getMessage());
				try {
					Thread.sleep(10000);
				} catch (InterruptedException ex) {

				}
			}
		}
        
		return json;

	}

	/*
	* Calls the avisos URL to get warnings of bad conditions
	 */
	public ArrayList<String> getSpainAdvisories() {
		String apiURL;
		ArrayList<String> xmls = new ArrayList<>();

		try {
			if (provider.equalsIgnoreCase("es")) {
				apiURL = AeMetBaseURL + AeMetAvisosPath + AeMetArea + AeMetApiKey;
				xmls = apiCaller.callSpainAvisosAPI(apiURL);
			} else {
				//apiURL = String.format(UkMetOfficeBaseURL, UkMetOfficeLatitude, UkMetOfficeLongitude);
				//LOGGER.trace("calling UK api:" + apiURL);
				//xmls = "not done yet"; //callUKAPI(apiURL, client);
			}
		} catch (Exception ex) {
			LOGGER.error("Error calling Advisories URL ", ex);
		}
		return xmls;

	}



	/**
	 * @param avisos the alerts to set Only take local and non-Minor alerts
     * @return an array of alerts
	 */
	public ArrayList<WeatherAlert> getOurAlerts(ArrayList<String> avisos) {
		ArrayList<WeatherAlert> newAlerts = new ArrayList<>();
		for (String aviso : avisos) {
			WeatherAlert a = avisoDecoder.scanAviso(aviso);
			if (a.getArea().toLowerCase().contains("campo de cartagena")) {
				if (!a.getLevel().equalsIgnoreCase("minor")) {
					newAlerts.add(a);
					//LOGGER.trace(a.getArea() + " - " + a.getLevel() + " - " + a.getText());
				}
			}
		}

		LOGGER.trace("Alerts for us above Minor :" + newAlerts.size());
		return newAlerts;
	}


    private double findMoonAge(LocalDate today) {
        double days = 0.0;
        try {
            String moonInfo = apiCaller.callOWAPI(ViewBitsApi);   // not really OpenWeather,, just a convenience call.
            
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            String thisDate = today.format(formatter);
            int pos1 = moonInfo.indexOf(thisDate);
            if (pos1 < 0){
                return pos1;
            }

            int pos2 = moonInfo.indexOf("moon_age", pos1);
            if (pos2 < 0){
                return pos2;
            }

            pos1 = moonInfo.indexOf('"', pos2+9); // leading quote
            if (pos1 < 0){
                return pos1;
            }

            pos2 = moonInfo.indexOf(' ', pos1+1);  //trailing quote
            days = Double.parseDouble(moonInfo.substring(pos1+1, pos2));
        } catch (Exception e) {
            LOGGER.error("Exception fetching Moon data ", e.getMessage());
        }
        
        return days;
    
    /* Response is an array of these 
        {
        "date": "2024-12-23",
        "timestamp": 1734912000,
        "phase": "Last Quarter",
        "illumination": "47.6%",
        "moon_age": "22.37 days",
        "moon_image": "https://api.viewbits.com/img/moon/phase-22.webp",
        "moon_angle": 272.76,
        "moon_distance": "334,258.03 km",
        "sun_distance": "148,975,897.48 km",
        "moon_sign": "Libra",
        "moon_zodiac": "♎"
        },
    */

    }

}

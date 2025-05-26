/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package driverway.nb.externals;

import driverway.nb.weatherfinder.WeatherAlert;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.Properties;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author john
 */
public class UKWeatherApi implements WeatherApiCaller {

    private static final Logger LOGGER = LogManager.getLogger();
    private final HttpClient client = HttpClient.newHttpClient();
    private final String UkMetOfficeApiKey;
    private final String UkMetOfficeLatitude;
    private final String UkMetOfficeLongitude;
    private final String UkMetOfficeBaseURL;
    private final String UkMetOfficeParams;

    public UKWeatherApi(Properties choices) {
        UkMetOfficeBaseURL = choices.getProperty("UkMetOfficeBaseURL");
        UkMetOfficeParams = choices.getProperty("UkMetOfficeParams");
        UkMetOfficeApiKey = choices.getProperty("UkMetOfficeApiKey");
        UkMetOfficeLatitude = choices.getProperty("UkMetOfficeLatitude", "52.0");
        UkMetOfficeLongitude = choices.getProperty("UkMetOfficeLongitude", "0.0");
    }

    @Override
    public String getForecastJSON() {

        try {
			String params = String.format(UkMetOfficeParams, UkMetOfficeLatitude, UkMetOfficeLongitude);
            //Grrr I'm sure they somewhere said "encode the query string". But no. String encodedParams = URLEncoder.encode(params, StandardCharsets.UTF_8.toString());
            String apiURL = UkMetOfficeBaseURL + "?" + params ; // encodedParams;
        	LOGGER.trace("calling UK weather api:" + apiURL);

            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(apiURL))
                .headers("apikey", UkMetOfficeApiKey,
                    "accept", "application/json")
                .GET()
                .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            return response.body();
        } catch (IOException | InterruptedException e) {
            return "";
        }

    }

    @Override
    public ArrayList<WeatherAlert> getAlerts(){
        return new ArrayList<WeatherAlert>();
    }

}

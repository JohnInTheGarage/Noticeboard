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
public class OWWeatherApi implements WeatherApiCaller {

    private static final Logger LOGGER = LogManager.getLogger();
    private final HttpClient client = HttpClient.newHttpClient();

    public OWWeatherApi(Properties choices) {
    }

    @Override
    public String getForecastJSON() {

        try {
            String apiURL = "";
        	LOGGER.trace("calling OpenWeather api:" + apiURL);

            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(apiURL))
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

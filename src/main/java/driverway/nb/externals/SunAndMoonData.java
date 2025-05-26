/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package driverway.nb.externals;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Properties;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;


/** https://api.met.no/weatherapi/
 *  https://api.met.no/weatherapi/sunrise/3.0/sun?lat=59.933333&lon=10.716667&date=2025-05-24&offset=+01:00
 * 
 *
 *
 * @author john
 */
public class SunAndMoonData {

    private Properties samd;
    private final HttpClient client = HttpClient.newHttpClient();
    private static final Logger LOGGER = LogManager.getLogger();   
    private static final double DEGREES_TO_RADIANS = 0.017453292519943295;    
    
    /* 
    * Return times of sunrise and sunset plus moon illumination %age
     */
    public Properties collectData(Properties nbProps) {

        samd = new Properties();
        ZoneId zoneId = ZoneId.of(nbProps.getProperty("LocalTimeZone"));

        ZoneOffset rulesOffset = zoneId.getRules().getOffset(Instant.now());
        int hoursOffset = rulesOffset.getTotalSeconds() / 3600;
        String offsetUTC = String.format("%+03d:00", hoursOffset);
        String baseURL = nbProps.getProperty("SunAndMoonURL");

        String sunJson = callAPI(baseURL, 
            "sun",
            nbProps.getProperty("UkMetOfficeLatitude"), 
            nbProps.getProperty("UkMetOfficeLongitude"),
            offsetUTC);
        
        String moonJson = callAPI(baseURL, 
            "moon",
            nbProps.getProperty("UkMetOfficeLatitude"), 
            nbProps.getProperty("UkMetOfficeLongitude"),
            offsetUTC);
        
        return extractFields(sunJson, moonJson);
    }


    private Properties extractFields(String sunJson, String moonJson) {
       
        JSONObject jo = new JSONObject(sunJson);
        JSONObject sun = jo.getJSONObject("properties");
        JSONObject rise = sun.getJSONObject("sunrise");
        JSONObject set = sun.getJSONObject("sunset");
        String time = (String)rise.get("time");
        samd.put("sunrise", time.substring(11, 16));
        
        time = (String)set.get("time");
        samd.put("sunset", time.substring(11, 16));
        
        jo = new JSONObject(moonJson);
        JSONObject moon = jo.getJSONObject("properties");
        BigDecimal angle = (BigDecimal)moon.getBigDecimal("moonphase");  
        // For angle it is (angle of rotation around the earth), even though named as phase.
        //double moonAge = 0.5 * (1 - Math.cos(DEGREES_TO_RADIANS * notPhase.doubleValue() ));
        
        samd.put("moonangle", String.valueOf(angle));
        return samd;
    }

    private String callAPI(String baseURL, String body, String latitude, String longitude, String offsetUTC) {
        try {
            
            String sunURL = baseURL.formatted(body, latitude, longitude, offsetUTC);
            LOGGER.trace("calling :" + sunURL);

            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(sunURL))
                .GET()
                .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            return response.body();
            
        } catch (IOException | InterruptedException e) {
            return "";
        }
        

    }
}

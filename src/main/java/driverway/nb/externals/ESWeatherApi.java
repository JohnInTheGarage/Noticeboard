/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package driverway.nb.externals;

import driverway.nb.weatherfinder.WeatherAlert;
import driverway.nb.weatherfinder.XMLdecoder;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.Properties;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;

/**
 *
 * @author john
 */
public class ESWeatherApi implements WeatherApiCaller {

    private static final Logger LOGGER = LogManager.getLogger();
    private final HttpClient client = HttpClient.newHttpClient();
    private final String AeMetApiKey;
    private final String AeMetBaseURL;
    private final String AeMetMunicipio;
    private final String AeMetArea;
    private final String AePrediccionPath;
    private final String AeMetAvisosPath;
    private final XMLdecoder avisoDecoder;

    public ESWeatherApi(Properties choices) {

        AeMetBaseURL = choices.getProperty("AeMetBaseURL");
        AeMetApiKey = choices.getProperty("AeMetApiKey");
        AeMetMunicipio = choices.getProperty("AeMunicipio");
        AeMetArea = choices.getProperty("AeMetArea");
        AePrediccionPath = choices.getProperty("AePrediccionPath");
        AeMetAvisosPath = choices.getProperty("AeMetAvisosPath");
        avisoDecoder = new XMLdecoder();

    }

    public String getForecastJSON() {
        try {
            String apiURL = AeMetBaseURL + AePrediccionPath + AeMetMunicipio + AeMetApiKey;
            LOGGER.trace("calling ES weather api :" + apiURL);
            HttpRequest request = locateSpainURL(apiURL);
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            return response.body();
        } catch (IOException | InterruptedException e) {
            return "";
        }

    }

    /*
    * call this to find the URL for spanish advisories of weather warnings 
    * (separate to normal API call)
     */
    private HttpRequest locateSpainURL(String URL) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(URL))
            .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200 || response.body().contains("exito") == false) {
            throw new IOException("Status not 200 or 'exito' missing");
        }
        int pos1 = response.body().indexOf("datos");
        int pos2 = response.body().indexOf(":", pos1);
        pos1 = response.body().indexOf("\"", pos2);
        pos1++;
        pos2 = response.body().indexOf("\"", pos1);
        URL = response.body().substring(pos1, pos2);
        request = HttpRequest.newBuilder()
            .uri(URI.create(URL))
            .build();
        return request;
    }


    /*
     * @return an array of alerts
     */
    @Override
    public ArrayList<WeatherAlert> getAlerts() {
        ArrayList<WeatherAlert> newAlerts = new ArrayList<>();

        String apiURL;
        ArrayList<String> xmls = new ArrayList<>();

        apiURL = AeMetBaseURL + AeMetAvisosPath + AeMetArea + AeMetApiKey;

        var avisos = callAvisosAPI(apiURL);

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

    /*
	* Gets the Set of XML files for weather warnings
     */
    private ArrayList<String> callAvisosAPI(String URL) {

        try {
            HttpRequest request = locateSpainURL(URL);

            HttpResponse<byte[]> responseBytes = client.send(request, HttpResponse.BodyHandlers.ofByteArray());
            ByteArrayInputStream bais = new ByteArrayInputStream(responseBytes.body());
            ArrayList<String> xmls = unTarFile(bais);
            return xmls;
        } catch (Exception e) {
            return new ArrayList<String>();
        }

    }

    /**
     *
     * @param tarFile
     * @param xmls
     * @throws IOException
     */
    private ArrayList<String> unTarFile(ByteArrayInputStream tarFile) throws IOException {
        TarArchiveInputStream tis = new TarArchiveInputStream((InputStream) tarFile);
        TarArchiveEntry tarEntry = null;
        ArrayList<String> xmls = new ArrayList<>();

        while ((tarEntry = tis.getNextTarEntry()) != null) {
            byte[] btis = tis.readAllBytes();
            xmls.add(new String(btis));
        }
        tis.close();
        return xmls;
    }

}

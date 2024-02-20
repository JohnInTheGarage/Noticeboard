package driverway.nb.weatherfinder;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Properties;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author john
 */
public class WeatherReader {

	private static final Logger LOGGER = LogManager.getLogger();
	private final XMLdecoder avisoDecoder;
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
	private final HttpClient client = HttpClient.newHttpClient();
	private AbstractWeatherDecoder wd;
	private int avisosHour = -1;
	private final String OpenWeatherApiKey;
	private final String OpenWeatherBaseURL;

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
						json = callSpainAPI(apiURL, client);
						break;
					case ("UK"):
						String params = String.format(UkMetOfficeParams, UkMetOfficeLatitude, UkMetOfficeLongitude);
                        //Grrr I'm sure they somewhere said "encode the query string". But no. String encodedParams = URLEncoder.encode(params, StandardCharsets.UTF_8.toString());
                        apiURL = UkMetOfficeBaseURL + "?" + params ; // encodedParams;
						LOGGER.trace("calling UK weather api:" + apiURL);
						json = callUKAPI(apiURL, client);
						break;
					case ("OW"):
						apiURL = String.format(OpenWeatherBaseURL, OpenWeatherApiKey);
						LOGGER.trace("calling OW weather api:" + apiURL);
						json = callOWAPI(apiURL, client);
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
				xmls = callSpainAvisosAPI(apiURL, client);
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

	/*
	* Gets the normal weather JSON response
	 */
	private String callSpainAPI(String URL, HttpClient client) throws IOException, InterruptedException {
		HttpRequest request = locateSpainURL(URL, client);
		HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
		return response.body();

	}

	/*
	* Gets the Set of XML files for weather warings
	 */
	private ArrayList<String> callSpainAvisosAPI(String URL, HttpClient client) throws IOException, InterruptedException {
		HttpRequest request = locateSpainURL(URL, client);
		//HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

		HttpResponse<byte[]> responseBytes = client.send(request, HttpResponse.BodyHandlers.ofByteArray());
		ByteArrayInputStream bais = new ByteArrayInputStream(responseBytes.body());
		ArrayList<String> xmls = unTarFile(bais);
		return xmls;

	}

	private HttpRequest locateSpainURL(String URL, HttpClient client1) throws IOException, InterruptedException {
		HttpRequest request = HttpRequest.newBuilder()
				.uri(URI.create(URL))
				.build();
		HttpResponse<String> response = client1.send(request, HttpResponse.BodyHandlers.ofString());
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

	private String callUKAPI(String URL, HttpClient client) throws IOException, InterruptedException {
		HttpRequest request = HttpRequest.newBuilder()
				.uri(URI.create(URL))
				.headers("apikey", UkMetOfficeApiKey,
						"accept", "application/json")
				.GET()
				.build();

		HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

		return response.body();
	}

	private String callOWAPI(String URL, HttpClient client) throws IOException, InterruptedException {
		HttpRequest request = HttpRequest.newBuilder()
				.uri(URI.create(URL))
				.GET()
				.build();

		HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

		return response.body();

	}

}

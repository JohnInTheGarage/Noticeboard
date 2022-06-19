package driverway.nb.weatherfinder;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpClient.*;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.Properties;
import javax.imageio.ImageIO;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;
import driverway.nb.utils.PreferenceHelper;

/**
 *
 * @author john
 */
public class EUmetViewer {

	private static final Logger LOGGER = LogManager.getLogger();
	private final String consumerKey;
	private final String consumerSecret;
	private final String EUmetAuthenticationURL;
	private final String EUmetWMSURL;
	private String EUmetQuery;
	private String accessToken = "";
	private String tokenTimestamp;
	// sending image to "disk" instead of keeping in memory  private byte[] imageBytes;
	//private String imagePath;
	private int statusCode;
	private HttpClient httpClient;
	private final String imagePath;
	private PreferenceHelper ph;

	public EUmetViewer(Properties choices) {
		consumerKey = choices.getProperty("consumerKey");
		consumerSecret = choices.getProperty("consumerSecret");
		EUmetAuthenticationURL = choices.getProperty("EUmetAuthenticationURL");
		EUmetWMSURL = choices.getProperty("EUmetWMSURL");
		EUmetQuery = choices.getProperty("EUmetQuery");
		imagePath = choices.getProperty("ImagePath");

		httpClient = HttpClient.newBuilder()
				.version(Version.HTTP_2)
				.followRedirects(Redirect.ALWAYS)
				.build();
		ph = PreferenceHelper.getInstance();
		restorePrefs();
	}

	public void callAPI() {

		HttpResponse<?> response = null;
		HttpRequest request;
		byte[] bytes = null;

		try {
			request = HttpRequest.newBuilder()
					.uri(URI.create(EUmetWMSURL + EUmetQuery))
					.headers("Authorization", "Bearer " + accessToken)
					.GET()
					.build();

			response = httpClient.send(request, BodyHandlers.ofByteArray());
			setStatusCode(response.statusCode());
			LOGGER.trace("EUMet API call StatusCode :" + getStatusCode());
			if (getStatusCode() == 200) {
				if (response != null) {
					//use disk, not memory    imageBytes = (byte[]) response.body();
					bytes = (byte[]) response.body();
					ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
					BufferedImage bImage2 = ImageIO.read(bis);
					File imageLocation = new File(imagePath);
					ImageIO.write(bImage2, "png", imageLocation);
					//Copy with timestamp
					LocalDateTime rightNow = LocalDateTime.now();
					ph.putItem("lastSatellite", rightNow.toString());
					DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd-HHmm");
					String timestamp = rightNow.format(formatter);
					ImageIO.write(bImage2, "png", new File(imageLocation.getParent() + File.separator + timestamp + ".png"));
					bytes = null;
				}
			} else {
				bytes = (byte[]) response.body();
				LOGGER.debug( "Non-image response :" +new String(bytes));
			}

		} catch (Exception e) {
			LOGGER.error("status code " + response.statusCode());
			LOGGER.error("bytes returned " + bytes.length);
			LOGGER.error("path " + imagePath);
			LOGGER.error(e.getMessage(), e);
		}

	}

	private void restorePrefs() {
		ph.getItem("accessToken");
		tokenTimestamp = ph.getItem("tokenTimestamp");
		try {
			LocalDateTime lastTime = LocalDateTime.parse(tokenTimestamp, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
			LocalDateTime oneHourAgo = LocalDateTime.now().minusHours(1L);
			if (accessToken == null || lastTime.isBefore(oneHourAgo)) {
				getNewToken();
			}
		} catch (Exception e) { // date was garbage
			getNewToken();
		}

	}

	private void getNewToken() {

		HttpResponse<String> response = null;
		HttpRequest request;

		String consumer = consumerKey + ":" + consumerSecret;
		String base64Product = Base64.getEncoder().withoutPadding().encodeToString(consumer.getBytes());

		request = HttpRequest.newBuilder().uri(URI.create(EUmetAuthenticationURL))
				.headers("Content-Type", "application/x-www-form-urlencoded")
				.headers("Authorization", "Basic " + base64Product)
				.POST(BodyPublishers.ofString("grant_type=client_credentials")).build();

		try {
			response = httpClient.send(request, BodyHandlers.ofString());
			JSONObject jo = new JSONObject(response.body());
			accessToken = jo.getString("access_token");
			tokenTimestamp = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
			ph.putItem("accessToken", accessToken);
			ph.putItem("tokenTimestamp", tokenTimestamp);

		} catch (IOException | InterruptedException ex) {
			LOGGER.error("response code :" + response.statusCode());
			LOGGER.error("body :" + response.body());
			LOGGER.error("Unable to authenticate for EUMet - network problems?");
		}
	}

	/**
	 * @return the EUmetQuery
	 */
	public String getEUmetQuery() {
		return EUmetQuery;
	}

	/**
	 * @param EUmetQuery the EUmetQuery to set
	 */
	public void setEUmetQuery(String EUmetQuery) {
		this.EUmetQuery = EUmetQuery;
	}

	/**
	 * @return the imageBytes No, find it on disk not memory public byte[]
	 * getImageBytes() { return imageBytes; }
	 */
	/**
	 * @return the statusCode
	 */
	public int getStatusCode() {
		return statusCode;
	}

	/**
	 * @param statusCode the statusCode to set
	 */
	private void setStatusCode(int statusCode) {
		this.statusCode = statusCode;
	}

}

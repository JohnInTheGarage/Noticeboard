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
import org.json.JSONArray;
import driverway.nb.utils.PreferenceHelper;
import java.util.logging.Level;

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
    private final String ApodURL;
    private final String NasaApiKey;
    private String EUmetQuery;
    private String accessToken = "";
    private String tokenTimestamp;
    private int statusCode;
    private final HttpClient httpClient;
    private PreferenceHelper ph;

    public EUmetViewer(Properties choices) {
        consumerKey = choices.getProperty("consumerKey");
        consumerSecret = choices.getProperty("consumerSecret");
        EUmetAuthenticationURL = choices.getProperty("EUmetAuthenticationURL");
        EUmetWMSURL = choices.getProperty("EUmetWMSURL");
        EUmetQuery = choices.getProperty("EUmetQuery");
        ApodURL = choices.getProperty("ApodURL");
        NasaApiKey = choices.getProperty("NasaApiKey");

        httpClient = HttpClient.newBuilder()
            .version(Version.HTTP_2)
            .followRedirects(Redirect.ALWAYS)
            .build();
        ph = PreferenceHelper.getInstance();
        restorePrefs();
    }

    public void callEUMetAPI(String imagePath) {

        HttpResponse<?> response = null;
        HttpRequest request;
        byte[] bytes = null;

        try {
            request = HttpRequest.newBuilder()
                .uri(URI.create(EUmetWMSURL + EUmetQuery))
                .headers("Authorization", "Bearer " + accessToken)
                .GET()
                .build();

            BufferedImage bImage = collectImage(request);

            if (bImage != null) {
                saveImage(bImage, imagePath);
                //Copy with timestamp
                LocalDateTime rightNow = LocalDateTime.now();
                ph.putItem("lastSatellite", rightNow.toString());
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd-HHmm");
                String timestamp = rightNow.format(formatter);
                saveImage(bImage, imagePath + File.separator + timestamp + ".png");
                bytes = null;
            }

        } catch (Exception e) {
            LOGGER.error("status code " + response.statusCode());
            LOGGER.error("bytes returned " + bytes.length);
            LOGGER.error("path " + imagePath);
            LOGGER.error(e.getMessage(), e);
        }

    }

    /**
     * 
     * @param imagePath 
     * A two-stage process; first get some JSON with image details, 
     * then get the image from the Url held in it.
     * In case of failure, try this https://api.nasa.gov/planetary/apod?api_key=DEMO_KEY 
     * to see if its your problem or someone else's
     */
    public void callApodAPI(String imagePath) {
        HttpResponse<?> response = null;
        HttpRequest request;
        String imageUrl;
        int retryDelayMS = 1000;

        try {

            while (getStatusCode() != 200) {
                // Request the JSON that has the image URL
                request = HttpRequest.newBuilder()
                    //The 'count' parameter selects that many random images
                    .uri(URI.create(ApodURL + NasaApiKey))
                    .headers("Content-Type", "application/x-www-form-urlencoded")
                    .GET()
                    .build();

                response = httpClient.send(request, BodyHandlers.ofString());
                if (response != null) {
                    setStatusCode(response.statusCode());
                    LOGGER.trace("APOD API call StatusCode :" + getStatusCode());
                    //decode JSON
                    if (getStatusCode() != 200) {
                        Thread.sleep(retryDelayMS);
                        retryDelayMS += 2000;
                        LOGGER.error("bad status code from APOD call :" + getStatusCode());
                    }

                }
            }
            
            // got the JSON apparently
            JSONArray ja = new JSONArray((String) response.body());
            JSONObject jo = ja.getJSONObject(0);
            imageUrl = jo.getString("url");
            LOGGER.trace("new image at :" + imageUrl);
            
            // Now request the image
            request = HttpRequest.newBuilder()
                .uri(URI.create(imageUrl))
                .GET()
                .build();

            BufferedImage bImage = collectImage(request);
            if (bImage != null) {
                saveImage(bImage,  imagePath + File.separator + "apod.png");
            } else {
                LOGGER.debug("Non-image response :" + response.body().toString());
            }

        } catch (Exception e) {
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

    private BufferedImage collectImage(HttpRequest imageRequest) throws IOException, InterruptedException {
        byte[] bytes = null;
        HttpResponse<?> response = httpClient.send(imageRequest, BodyHandlers.ofByteArray());
        bytes = (byte[]) response.body();
        setStatusCode(response.statusCode());
        LOGGER.trace("Image collection StatusCode :" + getStatusCode() + ", image size :" + bytes.length);
        if (getStatusCode() == 200) {
            ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
            return ImageIO.read(bis);
        } else {
            LOGGER.debug("Non-image response :" + new String(bytes));
            return null;
        }

    }

    private void saveImage(BufferedImage what, String where){
        try {
            File imageLocation = new File(where);
            ImageIO.write(what, "png", imageLocation);
        } catch (IOException ex) {
            LOGGER.error("Unable to save image "+ ex.getMessage());
        }
    }
}

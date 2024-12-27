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
    private int statusCode;
    private final HttpClient httpClient;
    private final PreferenceHelper ph;
    private int retryDelayMS = 1000;
    private File imageLocation;
    private final boolean EUmetRetainImages;
    private JSONArray ja;

    public EUmetViewer(Properties choices) {
        consumerKey = choices.getProperty("consumerKey");
        consumerSecret = choices.getProperty("consumerSecret");
        EUmetAuthenticationURL = choices.getProperty("EUmetAuthenticationURL");
        EUmetWMSURL = choices.getProperty("EUmetWMSURL");
        EUmetQuery = choices.getProperty("EUmetQuery");
        EUmetRetainImages = Boolean.parseBoolean(choices.getProperty("EUmetRetainImages"));

        httpClient = HttpClient.newBuilder()
            .version(Version.HTTP_2)
            .followRedirects(Redirect.ALWAYS)
            .build();
        ph = PreferenceHelper.getInstance();
        restorePrefs();
        ja = new JSONArray("[]");
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
                LocalDateTime rightNow = LocalDateTime.now();
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
                ph.putItem("lastSatellite", rightNow.format(formatter));

                //Copy with timestamp
                if (EUmetRetainImages) {
                    formatter = DateTimeFormatter.ofPattern("yyyyMMdd-HHmm");
                    String timestamp = rightNow.format(formatter);
                    saveImage(bImage, imageLocation.getParent() + File.separator + timestamp + ".png");
                }
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
     * 
    @param jsonURL
    @param apiKey
    @param imagePath 
    @param imageField 
    @param epic
     * For NASA images, get some JSON, and find the image URL from it.
     * Add the API key to the base URL using ? or & as appropriate (as APOD has "?count=n")
     * For APOD, the image URL in the JSON is complete.
     * For EPIC, the image URL is just a filename, plus we need to get the date as 3 separate fields and
     * slot those 4 things into a subtly different URL.  
     * 2023-12-21 Json fetched is assumed to have links to several images (previously just one).
     * 
     */
    public void callNasaImageApi(String jsonURL, String apiKey, String imagePath, String imageField, boolean epic) {
        try {
            if (ja.isEmpty()){
                ja = new JSONArray(fetchNasaJson(jsonURL, apiKey));
            }
            // get the first image and then remove that from the array so next time we
            // get the second image from the original list provided.
            JSONObject jo = ja.getJSONObject(0);
            String imageUrl = jo.getString(imageField);
            ja.remove(0);

            if (imageUrl.contains("www.youtube.com")) {
                LOGGER.error("FFS Nasa! Bloody YT links are no good to me");
                return;
            }

            if (epic) {
                // Grrr. The image path differs from the base url path. 
                String temp = jo.getString("date");
                String imageName = imageUrl;
                String yyyy = temp.substring(0, 4);
                String mm = temp.substring(5, 7);
                String dd = temp.substring(8, 10);
                imageUrl = String.format(
                    "https://epic.gsfc.nasa.gov/archive/natural/%s/%s/%s/png/%s.png",
                    yyyy, mm, dd, imageName);
            }
            LOGGER.trace("new image at :" + imageUrl);

            // Now request the image
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(imageUrl))
                .GET()
                .build();
            BufferedImage bImage = collectImage(request);

            if (bImage != null) {
                saveImage(bImage, imagePath);
            }

        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }

    }

    public String fetchNasaJson(String nasaURL, String apiKey) {

        HttpRequest request;
        retryDelayMS = -5000;
        String json = "";
        String joiner = nasaURL.contains("?") ? "&api_key=" : "?api_key=";
        
        try {
            while (retryDelayMS < 30000) {
                retryDelayMS += 5000;
                Thread.sleep(retryDelayMS);

                // Request the JSON that has the image URL
                request = HttpRequest.newBuilder()
                    .uri(URI.create(nasaURL+joiner+apiKey))
                    .headers("Content-Type", "application/x-www-form-urlencoded")
                    .GET()
                    .build();

                HttpResponse<?> response = httpClient.send(request, BodyHandlers.ofString());
                if (response != null) {
                    setStatusCode(response.statusCode());
                    if (getStatusCode() != 200) {
                        apodError("bad status code from APOD call, adding 5 sec pause:" + getStatusCode());
                        continue;
                    }
                    json = (String) response.body();
                    break;
                } else {
                    apodError("APOD response is null, adding 5 sec pause");
                }
            }
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }

        return json;
    }
/*
    public void callNasaPhotoJournalRSS(String rssURL, String imagePath) {
        //https://photojournal.jpl.nasa.gov/rss/targetFamily/Mars
        // parse for "<hiresJpeg><![CDATA["
        if (ja.isEmpty()){
            ja = buildFakeJson(rssURL, "<hiresJpeg><![CDATA[");
        }
        
        JSONObject jo = ja.getJSONObject(0);
        String imageUrl = jo.getString("url");
        ja.remove(0);

        LOGGER.trace("new image at :" + imageUrl);

        // Now request the image
        try {
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(imageUrl))
                .GET()
                .build();
            BufferedImage bImage = collectImage(request);

            if (bImage != null) {
                saveImage(bImage, imagePath);
            }
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }

    }
    
    private JSONArray buildFakeJson(String PhotoJournalURL, String imageField) {
        String notJson = fetchNasaJson(PhotoJournalURL, "");
        JSONArray imageList = new JSONArray();
        int pos = notJson.indexOf(imageField);
        int pos2 = 0;
        int imageIx = 0;
        while (pos > -1) {
            pos += imageField.length();
            pos2 = notJson.indexOf("]", pos);
            String url = notJson.substring(pos, pos2).trim();
            JSONObject j = new JSONObject();
            j.put("url", url);
            imageList.put(j);
            imageIx++;
            pos = notJson.indexOf(imageField, pos2);
        }
        return imageList;
    }

    private JSONArray buildHiRISEJson(String PhotoJournalURL, String imageField) {
        String notJson = fetchNasaJson(PhotoJournalURL, "");
        JSONArray imageList = new JSONArray();
        int pos = notJson.indexOf(imageField);
        int pos2 = 0;
        int imageIx = 0;
        while (pos > -1) {
            pos += imageField.length();
            pos2 = notJson.indexOf("]", pos);
            String url = notJson.substring(pos, pos2).trim();
            JSONObject j = new JSONObject();
            j.put("url", url);
            imageList.put(j);
            imageIx++;
            pos = notJson.indexOf(imageField, pos2);
        }
        return imageList;
    
    }
*/
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

    private String saveImage(BufferedImage what, String where) {

        try {
            imageLocation = new File(where);
            if (imageLocation.canWrite()){
                ImageIO.write(what, "png", imageLocation);
            }

        } catch (IOException ex) {
            LOGGER.error("Unable to save image (" + where + ") " + ex.getMessage());
        }
        return imageLocation.getParent();
    }

    private void apodError(String text) {
        try {
            LOGGER.error(text);
            retryDelayMS += 5000;
            Thread.sleep(retryDelayMS);
        } catch (InterruptedException ex) {
            //oh well, never mind
        }
    }
}

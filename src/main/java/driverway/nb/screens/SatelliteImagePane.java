package driverway.nb.screens;

import driverway.nb.utils.PreferenceHelper;
import driverway.nb.utils.PropertyLoader;
import driverway.nb.weatherfinder.EUmetViewer;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import javafx.geometry.Rectangle2D;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author john Fetches and displays the Satellite image then pans the ImageView
 * across the image to show different parts and thereby act as a screensaver.
 * Aug 2022 - added display of NASA's APOD image as alternative for evening
 * hours when we already know what the day was like.
 */
public class SatelliteImagePane extends Pane {

	private ImageView screenSaverView;
	private int x, y, dx, dy;
	private int delta = 5;
	private final EUmetViewer ev;
	private final PreferenceHelper ph;
	private LocalDateTime imageTime;
	private final int requestInterval;
	private final int imageRetentionDays;
	private static final Logger LOGGER = LogManager.getLogger();
	private int tidyDate;
	private boolean showingEUMet;
    private final String imagePath;
    private final int expectedWidth = 1600;
    private final int expectedHeight = 960;

	public SatelliteImagePane(PropertyLoader pl) {

		var EUmetProperties = pl.load("EUmet.properties");
		ph = PreferenceHelper.getInstance();
		String value = EUmetProperties.getProperty("RequestInterval");
		ph.putItem("EUmetRequestInterval", value);
		requestInterval = Integer.parseInt(value, 10);
		value = EUmetProperties.getProperty("imageRetentionDays");
		ph.putItem("SatelliteImageRetention", value);
		imageRetentionDays = Integer.parseInt(value, 10);

		ev = new EUmetViewer(EUmetProperties);
		imagePath = EUmetProperties.getProperty("ImagePath");
		LOGGER.trace("ImagePath " + imagePath);

		fetchImage(LocalDateTime.now());
		if (screenSaverView != null) {
			//Started when no image available e.g. overnight
			getChildren().add(screenSaverView);
		}
		showingEUMet = true;
		dx = 5;
		dy = 20;
	}

	private void fetchImage(LocalDateTime time) {

		String imageLocation;
		
		if (time.getDayOfMonth() != tidyDate) {
			tidyFiles(imagePath);
			tidyDate = time.getDayOfMonth();
		}

		//satellite gets no images after dark
		int fred = time.getHour();
		if (fred > 7 && fred < 18) {
			imageLocation = imagePath +"satellite.png";
			ev.callEUMetAPI(imageLocation);
		} else {
			imageLocation = imagePath+"apod.png";
			ev.callApodAPI(imageLocation);
		}
		handleImageUpdate(imageLocation);

	}

	private void handleImageUpdate(String imageLocation) {
		screenSaverView = new ImageView();
		showingEUMet = true;
		LOGGER.trace("fetching satellite image");
		imageTime = LocalDateTime.now();
		try {
			if (ev.getStatusCode() == 200) {
				loadScreenSaverImage(imageLocation, screenSaverView);
			} else {
				LOGGER.trace("cannot get satellite image, status :" + ev.getStatusCode());
			}
		} catch (Exception e) {
			LOGGER.trace("Exception loading satellite image", e);
		}
	}


    /*
    remove old files -anything in the images directory
    */
	private void tidyFiles(String directoryName) {
        Instant oldest = Instant.now().minus(imageRetentionDays, ChronoUnit.DAYS);
		LOGGER.debug("Oldest image to keep "+oldest.toString());
        File imageDir = new File(directoryName);
        
        if (imageDir.isDirectory()) {
            File[] candidates = imageDir.listFiles();
            for (File f:candidates){
                try {
                    Path p = f.toPath();
                    BasicFileAttributes attrs = Files.readAttributes(p, BasicFileAttributes.class);
                    if (attrs.creationTime().toInstant().isBefore(oldest)) {
                        LOGGER.info("deleting " + f.getAbsolutePath());
                        f.delete();
                    }
                } catch (IOException ex) {
                    LOGGER.error("Unable to delete images ",ex.getMessage());
                }
                
            }
		}

	}


	/*
	* left to right pans going down 1600 by 960 image
	 */
	public void slideImage1(LocalDateTime time) {
		if (!showingEUMet) {	// only slide for Satellite image
			return;
		}
		x += delta;
		if (x > 800) {
			x = 0;
			y += delta;
			if (y > 480) {
				y = 0;
				x = 0;
			}
		}
		Rectangle2D viewportRect = new Rectangle2D(x, y, x + 800, y + 480);
		screenSaverView.setViewport(viewportRect);

		if (time.isAfter(imageTime.plusMinutes(requestInterval))) {
			fetchImage(time);
		}
		getChildren().remove(0);
		getChildren().add(screenSaverView);

	}

	/*
	* diagonal pans around 1600 by 960 image
	 */
	public void slideImage2(LocalDateTime time) {
		if (!showingEUMet) {	// only slide for Satellite image
			return;
		}

		x += dx;
		y += dy;
		if (x > 400 || x < 0) {
			dx = -dx;
		}
		if (y > 240 || y < 0) {
			dy = -dy;
		}
		Rectangle2D viewportRect = new Rectangle2D(x, y, x + 1200, y + 720);
		screenSaverView.setViewport(viewportRect);

		if (time.isAfter(imageTime.plusMinutes(requestInterval))) {
			fetchImage(time);
		}
		getChildren().remove(0);
		getChildren().add(screenSaverView);

	}

	/*
	* Vertical pan down 800 by 960 image
	 */
	public void slideImage3(LocalDateTime time) {
		if (!showingEUMet) {	// only slide for Satellite image
			return;
		}

		x = 0;
		y += dy;
		if (y > 475 || y < 10) {
			dy = -dy;
		}
		Rectangle2D viewportRect = new Rectangle2D(x, y, 800, y + 480);
		screenSaverView.setViewport(viewportRect);

		if (time.isAfter(imageTime.plusMinutes(requestInterval))) {
			fetchImage(time);
		}
		getChildren().remove(0);
		getChildren().add(screenSaverView);

	}

	private void loadScreenSaverImage(String path, ImageView view) {
		File file = new File(path);
		Image satImage = new Image(file.toURI().toString(), expectedWidth, expectedHeight, true, true );
		view.isCache();
		view.setImage(satImage);
	}

}

package driverway.nb.screens;

import driverway.nb.utils.PreferenceHelper;
import driverway.nb.utils.PropertyLoader;
import driverway.nb.utils.SatImageRemover;
import driverway.nb.weatherfinder.EUmetViewer;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import javafx.geometry.Rectangle2D;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author john Fetches and displays the Satellite image then pans the ImageView
 * across the image to show different parts and thereby act as a screensaver.
 */
public class SatelliteImagePane extends Pane {

	private ImageView satImageView;
	private WebView issWebView;
	private String imagePath;
	private int x, y, dx, dy;
	private int delta = 5;
	private EUmetViewer ev;
	private PreferenceHelper ph;
	private LocalDateTime imageTime;
	private int requestInterval = 40;
	private final int imageRetentionDays;
	private static final Logger LOGGER = LogManager.getLogger();
	private int tidyDate;
	private boolean showingEUMet;

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
		if (satImageView != null){
			//Started when no image available e.g. overnight
			getChildren().add(satImageView);
		}
		showingEUMet = true;
		dx = 5;
		dy = 20;
	}

	private void fetchImage(LocalDateTime time) {

		if (time.getDayOfMonth() != tidyDate) {
			tidyFiles();
			tidyDate = time.getDayOfMonth();
		}

		//Node content = getChildren().get(0);
		//satellite gets no images after dark
		int fred = time.getHour();
		if (fred > 6 && fred < 22) {
			handleImageUpdate();
		} else {
			// first time switching to ISS Finder ?
//			if (showingEUMet){  
//				issWebView = new WebView();
//				handleISSFinder(); <<<<<<<<<<<<<<<<<<<<<<<< unfinished
//				showingEUMet = false;
//			}
		}
		
	}

	private void handleImageUpdate() {
//		if (content instanceof ImageView == false){
//			getChildren().remove(0);
//			getChildren().add(satImageView);
//		}
		satImageView = new ImageView();
		showingEUMet = true;
		LOGGER.trace("fetching satellite image");
		imageTime = LocalDateTime.now();
		ev.callAPI();
		try {
			if (ev.getStatusCode() == 200) {
				/*  Find it on disk, its not kept in memory now
				ByteArrayInputStream bis = new ByteArrayInputStream(ev.getImageBytes());
				Image satImage = new Image(bis, 1600, 960, true, true);
				*/
				File file = new File(imagePath);
				Image satImage = new Image(file.toURI().toString());
				satImage.isSmooth();
				satImageView.isCache();
				satImageView.setImage(satImage);
				//imageTime = LocalDateTime.now();
			} else {
				LOGGER.trace("cannot get satellite image, status :" + ev.getStatusCode());
			}
		} catch (Exception e) {
			LOGGER.trace("Exception loading satellite image", e);
		}
	}

	private void handleISSFinder() {
//		if (content instanceof WebView == false){
//			getChildren().remove(0);
//			getChildren().add(issWebView);
//		}
		getChildren().remove(0);
		getChildren().add(issWebView);
		WebEngine webEngine = issWebView.getEngine();
		//webEngine.load("https://isstracker.spaceflight.esa.int/");
		webEngine.load("https://mkas.org.uk/");
	}
	
	private void tidyFiles() {
		SatImageRemover remover = new SatImageRemover(imageRetentionDays);
		try {
			Files.walkFileTree(Paths.get(imagePath).getParent(), remover);
		} catch (IOException ex) {
		}

	}


	/*
	* left to right pans going down 1600 by 960 image
	 */
	public void slideImage1(LocalDateTime time) {
		if (!showingEUMet){	// only slide for Satellite image
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
		satImageView.setViewport(viewportRect);

		if (time.isAfter(imageTime.plusMinutes(requestInterval))) {
			fetchImage(time);
		}
		getChildren().remove(0);
		getChildren().add(satImageView);

	}

	/*
	* diagonal pans around 1600 by 960 image
	 */
	public void slideImage2(LocalDateTime time) {
		if (!showingEUMet){	// only slide for Satellite image
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
		satImageView.setViewport(viewportRect);

		if (time.isAfter(imageTime.plusMinutes(requestInterval))) {
			fetchImage(time);
		}
		getChildren().remove(0);
		getChildren().add(satImageView);

	}

	/*
	* Vertical pan down 800 by 960 image
	 */
	public void slideImage3(LocalDateTime time) {
		if (!showingEUMet){	// only slide for Satellite image
			return;
		}

		x = 0;
		y += dy;
		if (y > 475 || y < 10) {
			dy = -dy;
		}
		Rectangle2D viewportRect = new Rectangle2D(x, y, 800, y + 480);
		satImageView.setViewport(viewportRect);

		if (time.isAfter(imageTime.plusMinutes(requestInterval))) {
			fetchImage(time);
		}
		getChildren().remove(0);
		getChildren().add(satImageView);

	}

}

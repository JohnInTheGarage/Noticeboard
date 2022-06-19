package driverway.nb.screens;

import driverway.nb.weatherfinder.Forecast;
import driverway.nb.weatherfinder.Period;
import java.io.IOException;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;
import javafx.scene.text.Font;
import javafx.stage.Stage;

/**
 *
 * @author john
 * Dynamic pane of VBox columns in an HBox.
 * Provides the detail view of the periods of the weather forecast, one column per period.  
 * First column shows data type and Back button, last column has buttons to navigate through periods.
 * The duration of the periods depends on which API is providing the data.
 */
public class PeriodsPane extends HBox {

	private Scene callerScene;
	/*private final String pathBackButto = """
                                       M66.6,108.91c1.55,1.63,2.31,3.74,2.28,5.85c-0.03,2.11-0.84,4.2-2.44,5.79l-0.12,0.12c-1.58,1.5-3.6,2.23-5.61,2.2 c-2.01-0.03-4.02-0.82-5.55-2.37C37.5,102.85,20.03,84.9,2.48,67.11c-0.07-0.05-0.13-0.1-0.19-0.16C0.73,65.32-0.03,63.19,0,61.08 c0.03-2.11,0.85-4.21,2.45-5.8l0.27-0.26C20.21,37.47,37.65,19.87,55.17,2.36C56.71,0.82,58.7,0.03,60.71,0 c2.01-0.03,4.03,0.7,5.61,2.21l0.15,0.15c1.57,1.58,2.38,3.66,2.41,5.76c0.03,2.1-0.73,4.22-2.28,5.85L19.38,61.23L66.6,108.91 L66.6,108.91z M118.37,106.91c1.54,1.62,2.29,3.73,2.26,5.83c-0.03,2.11-0.84,4.2-2.44,5.79l-0.12,0.12 c-1.57,1.5-3.6,2.23-5.61,2.21c-2.01-0.03-4.02-0.82-5.55-2.37C89.63,101.2,71.76,84.2,54.24,67.12c-0.07-0.05-0.14-0.11-0.21-0.17 c-1.55-1.63-2.31-3.76-2.28-5.87c0.03-2.11,0.85-4.21,2.45-5.8C71.7,38.33,89.27,21.44,106.8,4.51l0.12-0.13 c1.53-1.54,3.53-2.32,5.54-2.35c2.01-0.03,4.03,0.7,5.61,2.21l0.15,0.15c1.57,1.58,2.38,3.66,2.41,5.76 c0.03,2.1-0.73,4.22-2.28,5.85L71.17,61.23L118.37,106.91L118.37,106.91z
                                       """;
	*/
	private final String pathBackButton = "M66.6,108.91c1.55,1.63,2.31,3.74,2.28,5.85c-0.03,2.11-0.84,4.2-2.44,5.79l-0.12,0.12c-1.58,1.5-3.6,2.23-5.61,2.2 c-2.01-0.03-4.02-0.82-5.55-2.37C37.5,102.85,20.03,84.9,2.48,67.11c-0.07-0.05-0.13-0.1-0.19-0.16C0.73,65.32-0.03,63.19,0,61.08 c0.03-2.11,0.85-4.21,2.45-5.8l0.27-0.26C20.21,37.47,37.65,19.87,55.17,2.36C56.71,0.82,58.7,0.03,60.71,0 c2.01-0.03,4.03,0.7,5.61,2.21l0.15,0.15c1.57,1.58,2.38,3.66,2.41,5.76c0.03,2.1-0.73,4.22-2.28,5.85L19.38,61.23L66.6,108.91 L66.6,108.91z M118.37,106.91c1.54,1.62,2.29,3.73,2.26,5.83c-0.03,2.11-0.84,4.2-2.44,5.79l-0.12,0.12 c-1.57,1.5-3.6,2.23-5.61,2.21c-2.01-0.03-4.02-0.82-5.55-2.37C89.63,101.2,71.76,84.2,54.24,67.12c-0.07-0.05-0.14-0.11-0.21-0.17 c-1.55-1.63-2.31-3.76-2.28-5.87c0.03-2.11,0.85-4.21,2.45-5.8C71.7,38.33,89.27,21.44,106.8,4.51l0.12-0.13 c1.53-1.54,3.53-2.32,5.54-2.35c2.01-0.03,4.03,0.7,5.61,2.21l0.15,0.15c1.57,1.58,2.38,3.66,2.41,5.76 c0.03,2.1-0.73,4.22-2.28,5.85L71.17,61.23L118.37,106.91L118.37,106.91z";
	private final String pathTemp = "M61.47,94.05c0,7.95-3.02,14.74-9.05,20.38c-6,5.64-13.25,8.45-21.73,8.45c-8.48,0-15.71-2.82-21.7-8.45 C3.02,108.8,0,102,0,94.05c0-9.15,3.81-16.57,11.37-22.33l0-52.6c0-5.3,1.92-9.81,5.73-13.55C20.91,1.86,25.51,0,30.88,0 c5.3,0,9.84,1.89,13.59,5.63c3.74,3.75,5.63,8.29,5.63,13.59v52.5C57.69,77.48,61.47,84.91,61.47,94.05L61.47,94.05z M46.42,93.06 c0-4.64-1.96-8.48-5.83-11.43l0-62.9c0-2.72-0.96-5.04-2.85-6.99c-1.92-1.92-4.24-2.88-6.96-2.88c-2.72,0-5.07,0.93-7.02,2.85 c-1.99,1.92-2.98,4.21-2.98,6.93v63c-3.88,2.92-5.8,6.73-5.8,11.43c0,4.08,1.52,7.56,4.61,10.41c3.05,2.85,6.76,4.27,11.1,4.27 c4.34,0,8.05-1.43,11.13-4.27C44.87,100.61,46.42,97.13,46.42,93.06L46.42,93.06L46.42,93.06z M37.67,46.92v37.89 c2.98,2.29,4.47,5.04,4.47,8.25c0,2.98-1.13,5.54-3.35,7.62c-2.25,2.12-4.94,3.18-8.12,3.18c-3.18,0-5.86-1.06-8.08-3.18 c-2.25-2.09-3.38-4.64-3.38-7.62c0-3.21,1.53-5.96,4.57-8.25l0-37.89L37.67,46.92L37.67,46.92L37.67,46.92z";
	private final String pathRain = "M56.62,3.95c0-1.09,0.44-2.08,1.16-2.79C58.5,0.44,59.48,0,60.57,0c1.09,0,2.07,0.44,2.79,1.16l0,0 c0.71,0.71,1.16,1.7,1.16,2.79v5.5c6.89,0.39,13.48,1.77,19.55,3.99c6.73,2.46,12.83,5.93,18.05,10.2 c5.6,4.58,10.21,10.09,13.49,16.25c3.18,5.97,5.11,12.54,5.52,19.47l0,0.04c0.05,1.07-0.34,2.06-1,2.8 c-0.67,0.75-1.63,1.25-2.72,1.31l-0.01,0c-0.07,0.01-0.14,0.02-0.21,0.02H64.51v39.12c-0.01,0.1-0.01,0.21-0.02,0.31 c-0.01,4.36-1.03,8.03-2.73,10.99c-1.93,3.35-4.72,5.78-7.9,7.26c-1.22,0.57-2.5,0.99-3.82,1.27c-1.31,0.28-2.67,0.42-4.04,0.41 c-1.36-0.01-2.72-0.16-4.05-0.46c-1.31-0.3-2.61-0.74-3.85-1.32c-3.43-1.61-6.47-4.28-8.58-8.02c-1.85-3.29-2.97-7.4-2.97-12.32 c0-1.08,0.44-2.07,1.15-2.78v-0.01c0.71-0.71,1.7-1.15,2.78-1.15s2.07,0.44,2.78,1.15l0.09,0.1c0.66,0.7,1.06,1.65,1.06,2.69 c0,3.36,0.7,6.11,1.86,8.26c1.27,2.36,3.1,4.01,5.14,4.97c0.73,0.34,1.48,0.6,2.25,0.77c0.78,0.18,1.58,0.27,2.36,0.27 c0.77,0,1.55-0.08,2.32-0.24c0.75-0.16,1.49-0.41,2.18-0.73c1.81-0.84,3.39-2.25,4.49-4.23c1.01-1.82,1.62-4.14,1.62-6.97 c0-0.07-0.01-0.15-0.01-0.23V63.52H3.95c-1.09,0-2.08-0.44-2.79-1.16C0.44,61.65,0,60.66,0,59.57c0-0.07,0-0.16,0.01-0.27 l0.02-0.18c0.43-6.88,2.38-13.4,5.55-19.33c3.28-6.12,7.86-11.6,13.44-16.16c5.22-4.27,11.33-7.74,18.05-10.2 c6.07-2.22,12.66-3.61,19.55-3.99V3.95L56.62,3.95z M97.13,29.74c-4.83-3.95-10.54-7.12-16.85-9.29 c-6.09-2.09-12.74-3.23-19.71-3.23c-6.97,0-13.62,1.15-19.71,3.23c-6.31,2.16-12.02,5.34-16.85,9.29 c-4.43,3.62-8.12,7.89-10.85,12.61v0.01c-2.37,4.11-4.02,8.57-4.82,13.26h104.44c-0.79-4.69-2.44-9.16-4.82-13.26l-0.04-0.08 C105.22,37.59,101.54,33.35,97.13,29.74L97.13,29.74z";
	private final String pathWind = "M28.69,53.38c-1.61,0-2.91-1.3-2.91-2.91c0-1.61,1.3-2.91,2.91-2.91h51.37c0.21,0,0.42,0.02,0.62,0.07 c1.84,0.28,3.56,0.8,5.1,1.63c1.7,0.92,3.15,2.19,4.27,3.89c3.85,5.83,3.28,11.24,0.56,15.24c-1.77,2.61-4.47,4.55-7.45,5.57 c-3,1.03-6.32,1.13-9.32,0.03c-4.54-1.66-8.22-5.89-8.76-13.55c-0.11-1.6,1.1-2.98,2.7-3.09c1.6-0.11,2.98,1.1,3.09,2.7 c0.35,4.94,2.41,7.56,4.94,8.48c1.71,0.62,3.67,0.54,5.48-0.08c1.84-0.63,3.48-1.79,4.52-3.32c1.49-2.19,1.71-5.28-0.61-8.79 c-0.57-0.86-1.31-1.51-2.18-1.98c-0.91-0.49-1.97-0.81-3.13-0.99H28.69L28.69,53.38z M15.41,27.21c-1.61,0-2.91-1.3-2.91-2.91 c0-1.61,1.3-2.91,2.91-2.91h51.21c1.17-0.18,2.23-0.5,3.14-0.99c0.87-0.47,1.61-1.12,2.18-1.98c2.32-3.51,2.09-6.6,0.61-8.79 c-1.04-1.53-2.68-2.69-4.52-3.32c-1.81-0.62-3.78-0.7-5.48-0.08c-2.52,0.92-4.59,3.54-4.94,8.48c-0.11,1.6-1.49,2.81-3.09,2.7 c-1.6-0.11-2.81-1.49-2.7-3.09c0.54-7.66,4.22-11.89,8.76-13.55c3-1.09,6.32-0.99,9.32,0.03c2.98,1.02,5.68,2.97,7.45,5.57 c2.72,4,3.29,9.41-0.56,15.24c-1.12,1.7-2.57,2.97-4.27,3.89c-1.54,0.83-3.26,1.35-5.1,1.63c-0.2,0.04-0.41,0.07-0.62,0.07H15.41 L15.41,27.21z M2.91,40.3C1.3,40.3,0,38.99,0,37.39c0-1.61,1.3-2.91,2.91-2.91h107.07c1.17-0.18,2.23-0.5,3.13-0.99 c0.87-0.47,1.61-1.12,2.18-1.98c2.32-3.51,2.09-6.6,0.61-8.79c-1.04-1.53-2.68-2.69-4.52-3.32c-1.81-0.62-3.78-0.7-5.48-0.08 c-2.52,0.92-4.59,3.54-4.94,8.48c-0.11,1.6-1.49,2.81-3.09,2.7c-1.6-0.11-2.81-1.49-2.7-3.09c0.54-7.66,4.22-11.89,8.76-13.55 c3-1.09,6.32-0.99,9.32,0.03c2.98,1.02,5.68,2.97,7.45,5.57c2.72,4,3.29,9.41-0.56,15.24c-1.12,1.7-2.57,2.97-4.27,3.89 c-1.54,0.83-3.26,1.35-5.1,1.63c-0.2,0.04-0.41,0.07-0.62,0.07H2.91L2.91,40.3z";
	private final String pathCloud = "M86.35,29.93c-0.75,0.37-1.51,0.78-2.26,1.21c-2.25,1.32-4.47,2.93-6.74,4.78l-4.84-5.54c1.67-1.55,3.48-2.96,5.4-4.21 c1.53-1,3.13-1.89,4.78-2.65c0.66-0.33,1.32-0.64,2-0.93c-3.19-5.65-7.78-9.7-12.98-12.2c-5.2-2.49-11.02-3.45-16.69-2.9 c-5.63,0.54-11.1,2.59-15.62,6.1c-5.23,4.05-9.2,10.11-10.73,18.14l-0.48,2.51l-2.5,0.44c-2.45,0.43-4.64,1.02-6.56,1.77 c-1.86,0.72-3.52,1.61-4.97,2.66c-1.16,0.84-2.16,1.78-3.01,2.8c-2.63,3.15-3.85,7.1-3.82,11.1c0.03,4.06,1.35,8.16,3.79,11.53 c0.91,1.25,1.96,2.4,3.16,3.4c1.22,1.01,2.59,1.85,4.13,2.48c1.53,0.63,3.22,1.08,5.09,1.34l72.55,0c3.53-0.85,6.65-2,9.3-3.48 c2.63-1.47,4.78-3.26,6.39-5.41c2.5-3.33,3.73-8.04,3.78-12.87c0.06-5.07-1.18-10.16-3.59-13.86c-0.69-1.07-1.45-2.03-2.25-2.89 c-3.61-3.89-8.19-5.59-12.95-5.62C93.3,27.6,89.73,28.43,86.35,29.93L86.35,29.93L86.35,29.93z M91.99,20.65 c1.6-0.25,3.2-0.38,4.79-0.36c6.72,0.05,13.2,2.45,18.3,7.95c1.07,1.15,2.08,2.45,3.03,3.9c3.2,4.92,4.84,11.49,4.77,17.92 c-0.07,6.31-1.77,12.59-5.25,17.21c-2.27,3.01-5.18,5.47-8.67,7.42c-3.36,1.88-7.28,3.31-11.68,4.33l-0.82,0.1l-73.08,0l-0.46-0.04 c-2.67-0.34-5.09-0.97-7.29-1.88c-2.27-0.94-4.28-2.15-6.05-3.63c-1.68-1.4-3.15-2.99-4.4-4.72C1.84,64.25,0.04,58.63,0,53.03 c-0.04-5.66,1.72-11.29,5.52-15.85c1.23-1.48,2.68-2.84,4.34-4.04c1.93-1.4,4.14-2.58,6.64-3.55c1.72-0.67,3.56-1.23,5.5-1.68 c2.2-8.74,6.89-15.47,12.92-20.14c5.64-4.37,12.43-6.92,19.42-7.59c6.96-0.67,14.12,0.51,20.55,3.6 C81.9,7.15,88.02,12.76,91.99,20.65L91.99,20.65L91.99,20.65z";
	private final String pathHumid = "M40.95,0c2.64,11.28,5.67,20.29,9.04,26.98c3.37,6.69,8.53,13.94,15.45,21.76l2.98,3.32 c9.04,10.06,13.55,20.15,13.55,30.21c0,11.14-4.02,20.69-12.07,28.65c-8.08,7.96-17.71,11.96-28.93,11.96 c-11.19,0-20.8-4-28.88-11.96C4.03,102.96,0,93.41,0,82.27c0-10.06,4.51-20.15,13.55-30.21l2.98-3.32 c6.91-7.82,12.07-15.07,15.45-21.76C35.34,20.29,38.35,11.28,40.95,0L40.95,0z M13.81,76.29c-0.21-2.28,1.48-4.3,3.76-4.51 c2.29-0.21,4.31,1.48,4.51,3.76c0.52,5.52,1.73,10.61,4.04,15c2.23,4.24,5.55,7.89,10.35,10.7c1.98,1.16,2.64,3.71,1.48,5.68 c-1.16,1.98-3.71,2.64-5.68,1.48c-6.25-3.66-10.58-8.42-13.51-13.99C15.91,88.99,14.43,82.86,13.81,76.29L13.81,76.29L13.81,76.29z";
	private final String pathNightClear = "M49.06,1.27c2.17-0.45,4.34-0.77,6.48-0.98c2.2-0.21,4.38-0.31,6.53-0.29c1.21,0.01,2.18,1,2.17,2.21 c-0.01,0.93-0.6,1.72-1.42,2.03c-9.15,3.6-16.47,10.31-20.96,18.62c-4.42,8.17-6.1,17.88-4.09,27.68l0.01,0.07 c2.29,11.06,8.83,20.15,17.58,25.91c8.74,5.76,19.67,8.18,30.73,5.92l0.07-0.01c7.96-1.65,14.89-5.49,20.3-10.78 c5.6-5.47,9.56-12.48,11.33-20.16c0.27-1.18,1.45-1.91,2.62-1.64c0.89,0.21,1.53,0.93,1.67,1.78c2.64,16.2-1.35,32.07-10.06,44.71 c-8.67,12.58-22.03,21.97-38.18,25.29c-16.62,3.42-33.05-0.22-46.18-8.86C14.52,104.1,4.69,90.45,1.27,73.83 C-2.07,57.6,1.32,41.55,9.53,28.58C17.78,15.57,30.88,5.64,46.91,1.75c0.31-0.08,0.67-0.16,1.06-0.25l0.01,0l0,0L49.06,1.27 L49.06,1.27z M51.86,5.2c-0.64,0.11-1.28,0.23-1.91,0.36l-1.01,0.22l0,0c-0.29,0.07-0.63,0.14-1,0.23 c-14.88,3.61-27.05,12.83-34.7,24.92C5.61,42.98,2.46,57.88,5.56,72.94c3.18,15.43,12.31,28.11,24.51,36.15 c12.19,8.03,27.45,11.41,42.88,8.23c15-3.09,27.41-11.81,35.46-23.49c6.27-9.09,9.9-19.98,10.09-31.41 c-2.27,4.58-5.3,8.76-8.96,12.34c-6,5.86-13.69,10.13-22.51,11.95l-0.01,0c-12.26,2.52-24.38-0.16-34.07-6.54 c-9.68-6.38-16.93-16.45-19.45-28.7l0-0.01C31.25,40.58,33.1,29.82,38,20.77C41.32,14.63,46.05,9.27,51.86,5.2L51.86,5.2z";
	private final String pathDayClear = "M58.57,25.81c-2.13-3.67-0.87-8.38,2.8-10.51c3.67-2.13,8.38-0.88,10.51,2.8l9.88,17.1c2.13,3.67,0.87,8.38-2.8,10.51 c-3.67,2.13-8.38,0.88-10.51-2.8L58.57,25.81L58.57,25.81z M120,51.17c19.01,0,36.21,7.7,48.67,20.16 C181.12,83.79,188.83,101,188.83,120c0,19.01-7.7,36.21-20.16,48.67c-12.46,12.46-29.66,20.16-48.67,20.16 c-19.01,0-36.21-7.7-48.67-20.16C58.88,156.21,51.17,139.01,51.17,120c0-19.01,7.7-36.21,20.16-48.67 C83.79,58.88,101,51.17,120,51.17L120,51.17z M158.27,81.73c-9.79-9.79-23.32-15.85-38.27-15.85c-14.95,0-28.48,6.06-38.27,15.85 c-9.79,9.79-15.85,23.32-15.85,38.27c0,14.95,6.06,28.48,15.85,38.27c9.79,9.79,23.32,15.85,38.27,15.85 c14.95,0,28.48-6.06,38.27-15.85c9.79-9.79,15.85-23.32,15.85-38.27C174.12,105.05,168.06,91.52,158.27,81.73L158.27,81.73z M113.88,7.71c0-4.26,3.45-7.71,7.71-7.71c4.26,0,7.71,3.45,7.71,7.71v19.75c0,4.26-3.45,7.71-7.71,7.71 c-4.26,0-7.71-3.45-7.71-7.71V7.71L113.88,7.71z M170.87,19.72c2.11-3.67,6.8-4.94,10.48-2.83c3.67,2.11,4.94,6.8,2.83,10.48 l-9.88,17.1c-2.11,3.67-6.8,4.94-10.48,2.83c-3.67-2.11-4.94-6.8-2.83-10.48L170.87,19.72L170.87,19.72z M214.19,58.57 c3.67-2.13,8.38-0.87,10.51,2.8c2.13,3.67,0.88,8.38-2.8,10.51l-17.1,9.88c-3.67,2.13-8.38,0.87-10.51-2.8 c-2.13-3.67-0.88-8.38,2.8-10.51L214.19,58.57L214.19,58.57z M232.29,113.88c4.26,0,7.71,3.45,7.71,7.71 c0,4.26-3.45,7.71-7.71,7.71h-19.75c-4.26,0-7.71-3.45-7.71-7.71c0-4.26,3.45-7.71,7.71-7.71H232.29L232.29,113.88z M220.28,170.87 c3.67,2.11,4.94,6.8,2.83,10.48c-2.11,3.67-6.8,4.94-10.48,2.83l-17.1-9.88c-3.67-2.11-4.94-6.8-2.83-10.48 c2.11-3.67,6.8-4.94,10.48-2.83L220.28,170.87L220.28,170.87z M181.43,214.19c2.13,3.67,0.87,8.38-2.8,10.51 c-3.67,2.13-8.38,0.88-10.51-2.8l-9.88-17.1c-2.13-3.67-0.87-8.38,2.8-10.51c3.67-2.13,8.38-0.88,10.51,2.8L181.43,214.19 L181.43,214.19z M126.12,232.29c0,4.26-3.45,7.71-7.71,7.71c-4.26,0-7.71-3.45-7.71-7.71v-19.75c0-4.26,3.45-7.71,7.71-7.71 c4.26,0,7.71,3.45,7.71,7.71V232.29L126.12,232.29z M69.13,220.28c-2.11,3.67-6.8,4.94-10.48,2.83c-3.67-2.11-4.94-6.8-2.83-10.48 l9.88-17.1c2.11-3.67,6.8-4.94,10.48-2.83c3.67,2.11,4.94,6.8,2.83,10.48L69.13,220.28L69.13,220.28z M25.81,181.43 c-3.67,2.13-8.38,0.87-10.51-2.8c-2.13-3.67-0.88-8.38,2.8-10.51l17.1-9.88c3.67-2.13,8.38-0.87,10.51,2.8 c2.13,3.67,0.88,8.38-2.8,10.51L25.81,181.43L25.81,181.43z M7.71,126.12c-4.26,0-7.71-3.45-7.71-7.71c0-4.26,3.45-7.71,7.71-7.71 h19.75c4.26,0,7.71,3.45,7.71,7.71c0,4.26-3.45,7.71-7.71,7.71H7.71L7.71,126.12z M19.72,69.13c-3.67-2.11-4.94-6.8-2.83-10.48 c2.11-3.67,6.8-4.94,10.48-2.83l17.1,9.88c3.67,2.11,4.94,6.8,2.83,10.48c-2.11,3.67-6.8,4.94-10.48,2.83L19.72,69.13L19.72,69.13z";
	private final String pathNightPartCloud = "M70.71,53.66c-0.62,0.3-1.23,0.63-1.85,0.99c-1.85,1.08-3.66,2.4-5.52,3.92l-3.96-4.54c1.37-1.27,2.85-2.43,4.42-3.45 c1.26-0.82,2.57-1.55,3.91-2.17c0.54-0.27,1.08-0.52,1.63-0.76c-2.61-4.63-6.37-7.95-10.63-9.99c-4.26-2.04-9.03-2.83-13.67-2.38 c-4.61,0.45-9.09,2.12-12.79,4.99c-4.28,3.32-7.54,8.28-8.78,14.86l-0.39,2.06l-2.05,0.36c-2.01,0.35-3.8,0.83-5.37,1.45 c-1.52,0.59-2.88,1.32-4.07,2.18c-0.95,0.69-1.77,1.46-2.47,2.29c-2.16,2.58-3.15,5.82-3.13,9.09c0.02,3.32,1.11,6.68,3.11,9.45 c0.74,1.02,1.61,1.96,2.59,2.78c1,0.83,2.12,1.51,3.38,2.03c1.25,0.52,2.64,0.89,4.17,1.1h59.41c2.89-0.7,5.45-1.64,7.61-2.85 c2.15-1.21,3.91-2.67,5.23-4.43c2.05-2.72,3.05-6.58,3.1-10.54c0.05-4.15-0.96-8.32-2.94-11.35c-0.57-0.87-1.18-1.66-1.84-2.37 c-2.96-3.18-6.71-4.57-10.6-4.6C76.4,51.75,73.49,52.43,70.71,53.66L70.71,53.66z M62.46,32.1c0,0.23-0.03,0.45-0.1,0.66 c5.31,2.8,9.92,7.22,12.98,13.3c1.31-0.2,2.62-0.31,3.92-0.3c5.5,0.04,10.81,2.01,14.99,6.51c0.88,0.94,1.71,2.01,2.48,3.19 c0.81,1.25,1.5,2.62,2.06,4.08c4.43-1.26,8.42-3.56,11.69-6.62c2.19-2.05,4.06-4.45,5.52-7.1c-0.78,0.35-1.58,0.67-2.4,0.95 c-2.96,1.02-6.14,1.57-9.44,1.57c-8.03,0-15.3-3.26-20.57-8.52c-5.26-5.26-8.52-12.54-8.52-20.57c0-3.43,0.6-6.72,1.69-9.78 c0.33-0.93,0.71-1.83,1.13-2.72c-3.69,1.91-6.9,4.6-9.44,7.86C64.69,19.44,62.46,25.51,62.46,32.1L62.46,32.1z M57.96,30.86 c0.26-7.16,2.8-13.73,6.92-19.03C69.32,6.12,75.61,1.9,82.85,0.07c1.21-0.3,2.44,0.43,2.74,1.64c0.19,0.77-0.04,1.55-0.54,2.09 c-1.72,2.12-3.09,4.54-4.04,7.19c-0.92,2.58-1.42,5.36-1.42,8.26c0,6.78,2.75,12.92,7.19,17.37c4.44,4.44,10.59,7.19,17.37,7.19 c2.8,0,5.48-0.47,7.98-1.32c2.61-0.89,5-2.2,7.11-3.85c0.98-0.77,2.4-0.59,3.16,0.39c0.46,0.59,0.58,1.33,0.39,2l0,0 c-1.65,5.89-4.89,11.12-9.23,15.18c-3.78,3.54-8.4,6.21-13.53,7.67c0.41,2.04,0.61,4.16,0.59,6.26c-0.06,5.17-1.45,10.31-4.3,14.1 c-1.86,2.47-4.24,4.48-7.1,6.08c-2.75,1.54-5.96,2.71-9.57,3.55L79,93.95H19.15l-0.38-0.04c-2.19-0.28-4.17-0.79-5.97-1.54 c-1.86-0.77-3.51-1.76-4.96-2.97c-1.37-1.15-2.58-2.45-3.6-3.87C1.51,81.77,0.03,77.16,0,72.58c-0.03-4.63,1.41-9.25,4.52-12.98 c1.01-1.21,2.19-2.32,3.55-3.31c1.58-1.15,3.39-2.12,5.43-2.91c1.41-0.55,2.91-1.01,4.51-1.37c1.8-7.15,5.65-12.67,10.58-16.5 c4.62-3.58,10.18-5.66,15.9-6.22C49,28.86,53.62,29.38,57.96,30.86L57.96,30.86z";
	private final String pathDayPartCloud = "M101.8,57.81c-0.99-0.97-1-2.56-0.03-3.55c0.97-0.99,2.56-1,3.55-0.03l6.91,6.82c0.99,0.97,1,2.56,0.03,3.55 c-0.97,0.99-2.56,1-3.55,0.03L101.8,57.81L101.8,57.81z M66.03,46.16c-0.58,0.28-1.15,0.59-1.73,0.93 c-1.72,1.01-3.42,2.24-5.15,3.66l-3.7-4.24c1.28-1.19,2.66-2.27,4.13-3.22c1.17-0.76,2.39-1.44,3.65-2.02 c0.5-0.25,1.02-0.49,1.53-0.71c-2.44-4.32-5.95-7.42-9.93-9.33c-3.98-1.91-8.43-2.64-12.76-2.22c-4.31,0.42-8.49,1.98-11.95,4.66 c-4,3.1-7.04,7.73-8.2,13.87l-0.36,1.92l-1.91,0.34c-1.87,0.33-3.55,0.78-5.02,1.35c-1.42,0.55-2.69,1.23-3.8,2.03 c-0.89,0.64-1.65,1.36-2.3,2.14c-2.01,2.41-2.95,5.43-2.92,8.49c0.02,3.1,1.03,6.24,2.9,8.82c0.69,0.96,1.5,1.83,2.42,2.6l0,0.01 c0.92,0.77,1.97,1.4,3.16,1.89c1.17,0.48,2.46,0.83,3.9,1.03h55.48c2.7-0.65,5.09-1.53,7.11-2.66c2.01-1.13,3.65-2.5,4.89-4.14 c1.91-2.54,2.85-6.15,2.89-9.84c0.04-3.88-0.9-7.77-2.74-10.6c-0.53-0.81-1.11-1.55-1.72-2.21c-2.76-2.97-6.27-4.27-9.9-4.3 C71.35,44.38,68.62,45.02,66.03,46.16L66.03,46.16z M70.35,39.07c1.22-0.19,2.45-0.29,3.66-0.28c5.14,0.03,10.09,1.87,14,6.08 c0.82,0.88,1.59,1.87,2.31,2.98c0.31,0.48,0.61,0.98,0.88,1.51c0.36-0.66,0.68-1.34,0.94-2.06c0.61-1.64,0.94-3.43,0.94-5.3 c0-4.2-1.7-8.01-4.46-10.76c-2.75-2.75-6.56-4.46-10.76-4.46c-2.66,0-5.15,0.67-7.3,1.85c-1.8,0.99-3.39,2.33-4.66,3.95 C67.61,34.45,69.12,36.62,70.35,39.07L70.35,39.07z M93.44,55.83c0.37,1.87,0.55,3.8,0.53,5.72c-0.05,4.82-1.36,9.63-4.01,13.16 c-1.73,2.31-3.96,4.18-6.63,5.68c-2.57,1.44-5.57,2.53-8.93,3.31l-0.63,0.08H17.88l-0.35-0.03c-2.04-0.26-3.9-0.74-5.57-1.43 c-1.72-0.71-3.26-1.64-4.62-2.78H7.32c-1.28-1.07-2.41-2.29-3.36-3.61C1.41,72.41,0.03,68.11,0,63.83 c-0.03-4.33,1.32-8.64,4.22-12.12c0.94-1.13,2.05-2.17,3.32-3.09c1.48-1.07,3.17-1.98,5.07-2.72c1.32-0.51,2.72-0.94,4.21-1.28 c1.68-6.68,5.27-11.83,9.88-15.41c4.31-3.34,9.51-5.29,14.85-5.81c5.32-0.51,10.8,0.39,15.71,2.75c1.55,0.74,3.04,1.63,4.45,2.66 c1.7-2.07,3.79-3.82,6.17-5.12c2.98-1.63,6.38-2.55,9.99-2.55c5.76,0,10.97,2.33,14.75,6.11c3.77,3.77,6.11,8.99,6.11,14.75 c0,2.54-0.46,4.97-1.29,7.23c-0.87,2.34-2.14,4.49-3.74,6.34C93.61,55.67,93.53,55.75,93.44,55.83L93.44,55.83z M51.38,14.53 c-0.99-0.97-1-2.56-0.03-3.55c0.97-0.99,2.56-1,3.55-0.03l6.91,6.82c0.99,0.97,1,2.56,0.03,3.55c-0.97,0.99-2.56,1-3.55,0.03 L51.38,14.53L51.38,14.53z M78.54,2.52c-0.01-1.38,1.11-2.51,2.5-2.52c1.38-0.01,2.51,1.11,2.52,2.5l0.06,9.71 c0.01,1.38-1.11,2.51-2.5,2.52c-1.38,0.01-2.51-1.11-2.52-2.5L78.54,2.52L78.54,2.52z M106.52,12.04c0.99-0.97,2.58-0.96,3.55,0.03 c0.97,0.99,0.96,2.58-0.03,3.55l-6.91,6.82c-0.99,0.97-2.58,0.96-3.55-0.03c-0.97-0.99-0.96-2.58,0.03-3.55L106.52,12.04 L106.52,12.04z M120.36,38.66c1.38-0.01,2.51,1.11,2.52,2.5c0.01,1.38-1.11,2.51-2.5,2.52l-9.71,0.06 c-1.38,0.01-2.51-1.11-2.52-2.5c-0.01-1.38,1.11-2.51,2.5-2.52L120.36,38.66L120.36,38.66z";
	private final String pathMistFogOvercast = "M86.35,29.93c-0.75,0.37-1.51,0.78-2.26,1.21c-2.25,1.32-4.47,2.93-6.74,4.78l-4.84-5.54c1.67-1.55,3.48-2.96,5.4-4.21 c1.53-1,3.13-1.89,4.78-2.65c0.66-0.33,1.32-0.64,2-0.93c-3.19-5.65-7.78-9.7-12.98-12.2c-5.2-2.49-11.02-3.45-16.69-2.9 c-5.63,0.54-11.1,2.59-15.62,6.1c-5.23,4.05-9.2,10.11-10.73,18.14l-0.48,2.51l-2.5,0.44c-2.45,0.43-4.64,1.02-6.56,1.77 c-1.86,0.72-3.52,1.61-4.97,2.66c-1.16,0.84-2.16,1.78-3.01,2.8c-2.63,3.15-3.85,7.1-3.82,11.1c0.03,4.06,1.35,8.16,3.79,11.53 c0.91,1.25,1.96,2.4,3.16,3.4c1.22,1.01,2.59,1.85,4.13,2.48c1.53,0.63,3.22,1.08,5.09,1.34l72.55,0c3.53-0.85,6.65-2,9.3-3.48 c2.63-1.47,4.78-3.26,6.39-5.41c2.5-3.33,3.73-8.04,3.78-12.87c0.06-5.07-1.18-10.16-3.59-13.86c-0.69-1.07-1.45-2.03-2.25-2.89 c-3.61-3.89-8.19-5.59-12.95-5.62C93.3,27.6,89.73,28.43,86.35,29.93L86.35,29.93L86.35,29.93z M91.99,20.65 c1.6-0.25,3.2-0.38,4.79-0.36c6.72,0.05,13.2,2.45,18.3,7.95c1.07,1.15,2.08,2.45,3.03,3.9c3.2,4.92,4.84,11.49,4.77,17.92 c-0.07,6.31-1.77,12.59-5.25,17.21c-2.27,3.01-5.18,5.47-8.67,7.42c-3.36,1.88-7.28,3.31-11.68,4.33l-0.82,0.1l-73.08,0l-0.46-0.04 c-2.67-0.34-5.09-0.97-7.29-1.88c-2.27-0.94-4.28-2.15-6.05-3.63c-1.68-1.4-3.15-2.99-4.4-4.72C1.84,64.25,0.04,58.63,0,53.03 c-0.04-5.66,1.72-11.29,5.52-15.85c1.23-1.48,2.68-2.84,4.34-4.04c1.93-1.4,4.14-2.58,6.64-3.55c1.72-0.67,3.56-1.23,5.5-1.68 c2.2-8.74,6.89-15.47,12.92-20.14c5.64-4.37,12.43-6.92,19.42-7.59c6.96-0.67,14.12,0.51,20.55,3.6 C81.9,7.15,88.02,12.76,91.99,20.65L91.99,20.65L91.99,20.65z";
	private final String pathNightRainSnow = "M19.99,90.56c1.69,0.21,2.88,1.75,2.67,3.44c-0.21,1.69-1.75,2.88-3.44,2.67c-2.24-0.28-4.27-0.81-6.11-1.57 c-1.89-0.78-3.57-1.8-5.07-3.05H8.03c-1.41-1.17-2.64-2.51-3.69-3.96C1.55,84.22,0.04,79.51,0,74.82 c-0.03-4.75,1.44-9.47,4.63-13.29c1.03-1.24,2.25-2.38,3.64-3.39c1.62-1.17,3.48-2.17,5.57-2.98c1.44-0.56,2.98-1.03,4.62-1.41 c1.84-7.33,5.78-12.97,10.84-16.9c4.73-3.67,10.42-5.8,16.29-6.37c3.6-0.35,7.28-0.1,10.83,0.75c0.34-7.18,2.93-13.78,7.07-19.11 C68.02,6.27,74.46,1.95,81.88,0.07c1.24-0.31,2.5,0.44,2.81,1.68c0.2,0.79-0.04,1.59-0.55,2.14c-1.76,2.17-3.17,4.65-4.13,7.36 c-0.94,2.64-1.46,5.49-1.46,8.46c0,6.95,2.82,13.24,7.37,17.79c4.55,4.55,10.84,7.37,17.79,7.37c2.86,0,5.61-0.48,8.17-1.35 c2.67-0.92,5.12-2.26,7.28-3.94c1-0.78,2.46-0.61,3.24,0.4c0.47,0.6,0.59,1.36,0.4,2.04l0,0c-1.69,6.03-5.01,11.39-9.46,15.55 c-3.21,3.01-7,5.39-11.19,6.97c0.63,2.5,0.94,5.15,0.91,7.77c-0.06,5.29-1.49,10.56-4.4,14.44c-1.9,2.53-4.34,4.59-7.27,6.23 c-2.82,1.58-6.11,2.78-9.8,3.63c-1.66,0.38-3.31-0.66-3.69-2.32c-0.38-1.66,0.66-3.31,2.32-3.69c3.11-0.72,5.85-1.71,8.16-3.01 c2.21-1.23,4.01-2.74,5.36-4.54c2.1-2.79,3.13-6.74,3.17-10.79c0.02-2.15-0.23-4.3-0.74-6.3l-1.3-3.62 c-0.29-0.61-0.61-1.17-0.97-1.71c-0.58-0.89-1.21-1.7-1.89-2.43c-3.03-3.26-6.87-4.69-10.86-4.71c-2.9-0.02-5.89,0.68-8.73,1.93 c-0.63,0.31-1.26,0.65-1.89,1.02c-1.89,1.11-3.75,2.46-5.65,4.01l-4.06-4.65c1.4-1.3,2.92-2.48,4.53-3.53 c1.29-0.84,2.62-1.58,4-2.22c0.55-0.28,1.11-0.54,1.68-0.78c-2.68-4.74-6.53-8.14-10.89-10.23c-4.36-2.09-9.24-2.9-14-2.44 c-4.72,0.46-9.31,2.17-13.1,5.11c-4.38,3.4-7.72,8.48-9,15.22l-0.4,2.11l-2.1,0.37c-2.05,0.36-3.89,0.85-5.5,1.48 c-1.56,0.61-2.95,1.35-4.17,2.23c-0.97,0.7-1.81,1.49-2.53,2.35c-2.21,2.65-3.23,5.96-3.21,9.31c0.02,3.4,1.13,6.84,3.18,9.67 c0.76,1.05,1.65,2.01,2.65,2.85l0,0.01c1.01,0.84,2.16,1.54,3.47,2.07C16.8,89.95,18.32,90.35,19.99,90.56L19.99,90.56z M46.66,97.24c1.84,8.53,7.36,12.79,7.36,17.06c0,4.27-1.84,8.53-7.36,8.53c-5.52,0-7.35-4.26-7.35-8.53 C39.3,110.04,44.82,105.77,46.66,97.24L46.66,97.24z M38.36,73.81c1.32,6.14,5.29,9.2,5.29,12.27c0,3.07-1.32,6.14-5.29,6.14 c-3.97,0-5.29-3.07-5.29-6.14C33.07,83.01,37.04,79.95,38.36,73.81L38.36,73.81z M62.98,74.71c1.84,9.26,7.36,13.89,7.36,18.52 c0,4.63-1.84,9.26-7.36,9.26c-5.52,0-7.35-4.63-7.35-9.26C55.63,88.6,61.15,83.97,62.98,74.71L62.98,74.71z M61,32.7 c0.61,0.25,1.21,0.51,1.81,0.8c5.89,2.83,11.02,7.53,14.35,14.15c1.34-0.21,2.68-0.31,4.02-0.31c5.63,0.04,11.07,2.05,15.35,6.67 c0.9,0.97,1.75,2.05,2.54,3.27c0.59,0.9,1.11,1.87,1.57,2.89c3.57-1.36,6.8-3.41,9.54-5.97c2.25-2.1,4.16-4.56,5.65-7.28 c-0.8,0.36-1.62,0.68-2.46,0.97c-3.04,1.04-6.29,1.61-9.67,1.61c-8.23,0-15.67-3.33-21.07-8.73c-5.39-5.39-8.73-12.84-8.73-21.07 c0-3.51,0.61-6.89,1.73-10.02c0.34-0.95,0.73-1.88,1.16-2.78c-3.78,1.96-7.07,4.71-9.66,8.05C63.32,19.87,61.04,26.02,61,32.7 L61,32.7z";
	private final String pathDayRainSnow = "M18.23,78.18c1.54,0.19,2.63,1.6,2.43,3.14c-0.19,1.54-1.6,2.63-3.14,2.43c-2.04-0.26-3.9-0.74-5.57-1.43 c-1.72-0.71-3.26-1.64-4.62-2.78H7.32c-1.28-1.07-2.41-2.29-3.36-3.61C1.41,72.41,0.03,68.11,0,63.83 c-0.03-4.33,1.32-8.64,4.22-12.12c0.94-1.13,2.05-2.17,3.32-3.09c1.48-1.07,3.17-1.98,5.07-2.72c1.32-0.51,2.72-0.94,4.21-1.28 c1.68-6.68,5.27-11.83,9.88-15.41c4.31-3.34,9.51-5.29,14.85-5.81c5.32-0.51,10.8,0.39,15.71,2.75c1.55,0.74,3.04,1.63,4.45,2.66 c1.7-2.07,3.79-3.82,6.17-5.12c2.98-1.63,6.38-2.55,9.99-2.55c5.76,0,10.97,2.33,14.75,6.11c3.77,3.77,6.11,8.99,6.11,14.75 c0,2.54-0.46,4.97-1.29,7.23c-0.87,2.34-2.14,4.49-3.74,6.34c-0.08,0.09-0.16,0.17-0.25,0.25c0.37,1.87,0.55,3.8,0.53,5.72 c-0.05,4.82-1.36,9.63-4.01,13.16c-1.73,2.31-3.96,4.18-6.63,5.68c-2.57,1.44-5.57,2.53-8.93,3.31c-1.51,0.35-3.02-0.6-3.37-2.11 c-0.35-1.51,0.6-3.02,2.11-3.37c2.84-0.66,5.33-1.56,7.44-2.74c2.01-1.13,3.65-2.5,4.89-4.14c1.91-2.54,2.85-6.15,2.89-9.84 c0.04-3.88-0.9-7.77-2.74-10.6c-0.53-0.81-1.11-1.55-1.72-2.21c-2.76-2.97-6.27-4.27-9.9-4.3c-2.64-0.02-5.37,0.62-7.96,1.76 c-0.58,0.28-1.15,0.59-1.72,0.93c-1.72,1.01-3.42,2.24-5.15,3.66l-3.7-4.24c1.28-1.19,2.66-2.27,4.13-3.22 c1.17-0.76,2.39-1.44,3.65-2.02c0.5-0.25,1.02-0.49,1.53-0.71c-2.44-4.32-5.95-7.42-9.93-9.33c-3.98-1.91-8.43-2.64-12.76-2.22 c-4.31,0.42-8.49,1.98-11.95,4.66c-4,3.1-7.04,7.73-8.2,13.87l-0.36,1.92l-1.91,0.34c-1.87,0.33-3.55,0.78-5.02,1.35 c-1.42,0.55-2.69,1.23-3.8,2.03c-0.89,0.64-1.65,1.36-2.3,2.14c-2.01,2.41-2.95,5.43-2.92,8.49c0.02,3.1,1.03,6.24,2.9,8.82 c0.69,0.96,1.5,1.83,2.42,2.6l0,0.01c0.92,0.77,1.97,1.4,3.16,1.89C15.32,77.63,16.7,77.99,18.23,78.18L18.23,78.18z M42.54,84.28 c1.68,7.78,6.71,11.66,6.71,15.55c0,3.89-1.68,7.78-6.71,7.78c-5.03,0-6.71-3.89-6.71-7.78C35.83,95.94,40.86,92.05,42.54,84.28 L42.54,84.28z M57.43,63.73c1.68,8.44,6.71,12.66,6.71,16.89c0,4.22-1.68,8.44-6.71,8.44c-5.03,0-6.71-4.22-6.71-8.44 C50.72,76.4,55.75,72.18,57.43,63.73L57.43,63.73z M34.97,62.91c1.21,5.59,4.82,8.39,4.82,11.19c0,2.8-1.21,5.59-4.82,5.59 c-3.62,0-4.82-2.8-4.82-5.59C30.15,71.3,33.77,68.51,34.97,62.91L34.97,62.91z M65.9,32.58c1.71,1.88,3.21,4.04,4.45,6.49 c1.22-0.19,2.45-0.29,3.66-0.28c5.14,0.03,10.09,1.87,14,6.08c0.82,0.88,1.59,1.87,2.31,2.98c0.31,0.48,0.61,0.98,0.88,1.5 c0.36-0.66,0.68-1.34,0.94-2.06c0.61-1.64,0.94-3.43,0.94-5.3c0-4.2-1.7-8.01-4.46-10.76c-2.75-2.75-6.56-4.46-10.76-4.46 c-2.66,0-5.15,0.67-7.3,1.85C68.76,29.62,67.18,30.97,65.9,32.58L65.9,32.58z M101.8,57.81c-0.99-0.97-1-2.56-0.03-3.55 c0.97-0.99,2.56-1,3.55-0.03l6.91,6.82c0.99,0.97,1,2.56,0.03,3.55c-0.97,0.99-2.56,1-3.55,0.03L101.8,57.81L101.8,57.81z M120.36,38.66c1.38-0.01,2.51,1.11,2.52,2.5c0.01,1.38-1.11,2.51-2.5,2.52l-9.71,0.06c-1.38,0.01-2.51-1.11-2.52-2.5 c-0.01-1.38,1.11-2.51,2.5-2.52L120.36,38.66L120.36,38.66z M106.52,12.04c0.99-0.97,2.58-0.96,3.55,0.03 c0.97,0.99,0.96,2.58-0.03,3.55l-6.91,6.82c-0.99,0.97-2.58,0.96-3.55-0.03c-0.97-0.99-0.96-2.58,0.03-3.55L106.52,12.04 L106.52,12.04z M78.54,2.51c-0.01-1.38,1.11-2.51,2.5-2.51c1.38-0.01,2.51,1.11,2.52,2.5l0.06,9.71c0.01,1.38-1.11,2.51-2.5,2.51 c-1.38,0.01-2.51-1.11-2.52-2.5L78.54,2.51L78.54,2.51z M51.38,14.53c-0.99-0.97-1-2.56-0.03-3.55c0.97-0.99,2.56-1,3.55-0.03 l6.91,6.82c0.99,0.97,1,2.56,0.03,3.55c-0.97,0.99-2.56,1-3.55,0.03L51.38,14.53L51.38,14.53z";
	
	private Forecast fc;
	private VBox colFirst;
	private VBox colLast;
	private int period1 = 0;
	private String[] direction = new String[]{"C","N","NE","E","SE","S","SW","W","NW"};
	
	@SuppressWarnings("unchecked")
	public PeriodsPane(Forecast forecast, Scene thisScene) throws IOException {
		callerScene = thisScene;
		fc = forecast;
		SVGPath svgCloud = makeSVGPath(-25, 0, 0.4, pathCloud);
		Pane next8Pane = makeDataPane("Next");
		Pane prev8Pane = makeDataPane("Prev");
		Pane backPane = makeIconPane( makeSVGPath(-20, -25, 0.4, pathBackButton));
		Pane tempPane = makeIconPane( makeSVGPath(10, -20, 0.4, pathTemp));
		Pane rainPane = makeIconPane( makeSVGPath(-25, -20, 0.4, pathRain));
		Pane windPane = makeIconPane( makeSVGPath(-25, 0, 0.4, pathWind));
		Pane cloudPane = makeIconPane( svgCloud);
		Pane humidityPane = makeIconPane( makeSVGPath(0, -20, 0.4, pathHumid));
		colFirst = new VBox(backPane, tempPane, rainPane, windPane, cloudPane, humidityPane);
		colLast = new VBox(makeEmptyPane(), makeEmptyPane(), next8Pane, prev8Pane, makeEmptyPane(), makeEmptyPane());

		this.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());
		fillPeriods();
		
		EventHandler<MouseEvent> eventBackHandler = new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				Scene thisScene = ((Node)event.getSource()).getScene();
				Stage window = (Stage) thisScene.getWindow();
				window.setScene(callerScene);
				window.setFullScreen(true);
				window.show();
			}
		};
		
		EventHandler<MouseEvent> eventNextHandler = new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				int max = fc.getPeriodMax() - 8;
				period1 = (period1 >= max) ? max : period1 + 8;
				fillPeriods();
			}
		};
		
		EventHandler<MouseEvent> eventPrevHandler = new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				period1 = period1 < 8 ? 0 : period1 - 8;
				fillPeriods();
			}
		};
		next8Pane.addEventFilter(MouseEvent.MOUSE_CLICKED, eventNextHandler);
		prev8Pane.addEventFilter(MouseEvent.MOUSE_CLICKED, eventPrevHandler);
		backPane.addEventFilter(MouseEvent.MOUSE_CLICKED, eventBackHandler);
	}
	
	private void fillPeriods(){
		this.getChildren().clear();
		this.getChildren().add(colFirst);
		
		int px = period1;
		for (int col = 0; col < 8; col++) {
			if (px > fc.getPeriodMax()){
				break;
			}
			Period p = fc.getPeriod(px);
			VBox column = new VBox();

			BorderPane bp = makeEmptyPane();
			setPaneText(bp, p.getStartTimeText());
			column.getChildren().add(bp);
			
			bp = makeEmptyPane();
			setPaneText(bp, p.getMaxTemp());
			column.getChildren().add(bp);
			
			bp = makeEmptyPane();
			setPaneText(bp, p.getProbOfPrecip() + "\n" + p.getTotalPrecip());
			column.getChildren().add(bp);
			
			bp = makeEmptyPane();
			setPaneText(bp, p.getWindSpeed() + "\n" +direction[p.getWindDirection()]);
			
			column.getChildren().add(bp);
		
			SVGPath svg = getSkySVG(p.getWeatherCode());
			column.getChildren().add( makeIconPane(svg) );
			
			bp = makeEmptyPane();
			setPaneText(bp, p.getHumidity());
			column.getChildren().add(bp);
			var pds = this.getChildren();
			this.getChildren().add( column );
			px++;
		}
		this.getChildren().add(colLast);
	}

	private SVGPath makeSVGPath(int xOffset, int yOffset, double scale, String content){
		SVGPath svgPath = new SVGPath();
		svgPath.setContent(content);
		svgPath.setScaleX(scale);
		svgPath.setScaleY(scale);
		svgPath.setLayoutX(xOffset);
		svgPath.setLayoutY(yOffset);
		svgPath.setFill(Color.web("#FFFFFF"));
		return svgPath;
	}
	
	/*
	* Create pane for SVG icons 
	*/
	private Pane makeIconPane(SVGPath svgPath) {
		Pane pane = makeEmptyPane();
		pane.getChildren().add(svgPath);
		return pane;
	}

	private void setPaneText(BorderPane bp, String num) {
		Label l = new Label(num);
		l.setFont(new Font("Arial", 20));
		l.setMaxHeight(100);
		l.setMaxWidth(100);
		l.setAlignment(Pos.CENTER);
		bp.setCenter(l);
	}
	
	private Pane makeDataPane(String num) {
		Label l = new Label(num);
		l.setFont(new Font("Arial", 20));
		l.setMaxHeight(100);
		l.setMaxWidth(100);
		l.setAlignment(Pos.CENTER);

		BorderPane pane = makeEmptyPane();
		pane.setCenter(l);
		pane.setMaxSize(80, 80);
		pane.setMinSize(80, 80);
		//pane.setStyle("-fx-background-color:teal");
		return pane;
	}

	private BorderPane makeEmptyPane() {
		BorderPane pane = new BorderPane();
		pane.setMaxSize(80, 80);
		pane.setMinSize(80, 80);
		return pane;
	}

	private SVGPath getSkySVG(int weatherCode) {
		SVGPath item;
		
		switch (weatherCode ){
			case 0 :item = makeSVGPath(-20, -20, 0.35, pathNightClear); 
			break;
			case 1 :item = makeSVGPath(-80, -80, 0.25, pathDayClear);
			break;
			case 2 :item = makeSVGPath(-25, -10, 0.4, pathNightPartCloud);
			break;
			case 3 :item = makeSVGPath(-25, -5, 0.4, pathDayPartCloud);
			break;
			case 5: case 6: case 7: case 8: item = makeSVGPath(-25, 0, 0.4, pathMistFogOvercast);
			break;
			case 9: case 13: case 16: case 19: case 22: case 25: case 28 :item = makeSVGPath(-25, -20, 0.4, pathNightRainSnow);
			break;
			case 10: case 11: case 12: case 14: case 15: case 17: case 18: case 20: case 21: case 23: case 24: case 26: case 27: case 29: case 30: item = makeSVGPath(-25, -15, 0.4, pathDayRainSnow); 
			break;
			default : item = null;
		}
		
		return item;
	}

	
}

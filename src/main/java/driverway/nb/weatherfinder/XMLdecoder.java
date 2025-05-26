package driverway.nb.weatherfinder;

/**
 *
 * @author john
 */
import java.io.StringReader;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.apache.logging.log4j.LogManager;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class XMLdecoder {

	private SAXParserFactory factory;
	private SAXParser saxParser;
	private static final org.apache.logging.log4j.Logger LOGGER = LogManager.getLogger();

	private String severity = "";
	private String certainty = "";
	private String value = "";
	private String onsetText = "";
	private String expiresText = "";
	private String headline = "";
	private String description = "";
	private String areaDesc = "";

	public XMLdecoder() {
		try {
			factory = SAXParserFactory.newInstance();
			saxParser = factory.newSAXParser();
		} catch (ParserConfigurationException | SAXException ex) {
			LOGGER.error(ex);
		}
	}

	public WeatherAlert scanAviso(String aviso) {

		WeatherAlert alert = new WeatherAlert();
		try {

			DefaultHandler handler = new DefaultHandler() {

				boolean gotSeverity = false;
				boolean gotCertainty = false;
				boolean gotValue = false;
				boolean gotOnset = false;
				boolean gotExpires = false;
				boolean gotHeadline = false;
				boolean gotDescription = false;
				boolean gotAreaDesc = false;

				public void startElement(String uri, String localName, String qName,
						Attributes attributes) throws SAXException {

					if (qName.equalsIgnoreCase("severity")) {
						gotSeverity = true;
					}

					if (qName.equalsIgnoreCase("certainty")) {
						gotCertainty = true;
					}

					if (qName.equalsIgnoreCase("value")) {
						gotValue = true;
					}
					if (qName.equalsIgnoreCase("onset")) {
						gotOnset = true;
					}
					if (qName.equalsIgnoreCase("expires")) {
						gotExpires = true;
					}
					if (qName.equalsIgnoreCase("headline")) {
						gotHeadline = true;
					}
					if (qName.equalsIgnoreCase("description")) {
						gotDescription = true;
					}
					if (qName.equalsIgnoreCase("AreaDesc")) {
						gotAreaDesc = true;
					}

				}

				public void endElement(String uri, String localName,
						String qName) throws SAXException {
				}

				public void characters(char ch[], int start, int length) throws SAXException {

					String item = new String(ch, start, length);
					
					if (gotSeverity) {
						severity = item;
						gotSeverity = false;
					}

					if (gotCertainty) {
						certainty = item;
						gotCertainty = false;
					}

					if (gotValue) {
						value = item;
						gotValue = false;
					}

					if (gotOnset) {
						String from = item;
						onsetText = dateNormalise(from);
						gotOnset = false;
					}

					if (gotExpires) {
						String until = item;
						expiresText = dateNormalise(until);
						gotExpires = false;
					}

					if (gotHeadline) {
						headline = item;
						gotHeadline = false;
					}

					if (gotDescription) {
						description = item;
						gotDescription = false;
					}
					if (gotAreaDesc) {
						areaDesc = item;
						gotAreaDesc = false;
					}

				}

				private String dateNormalise(String when) {
					LocalDateTime ldt = LocalDateTime.parse(when, DateTimeFormatter.ISO_OFFSET_DATE_TIME);
					Enum<DayOfWeek> dow = ldt.getDayOfWeek();
					String hhmm= ldt.format(DateTimeFormatter.ofPattern("HH:mm"));
					return dow.toString() + " " + hhmm;
				}

			};

			StringReader sr = new StringReader(aviso);
			InputSource input = new InputSource(sr);
			saxParser.parse(input, handler);

		} catch (Exception e) {
			LOGGER.error(e);
		}
		alert.setLevel(severity);
		alert.setText(String.format("%s\n %s - %s ", headline, onsetText, expiresText));  //,  description));
		alert.setArea(areaDesc);

		return alert;
	}

}

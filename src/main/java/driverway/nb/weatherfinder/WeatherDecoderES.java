package driverway.nb.weatherfinder;

import java.time.LocalDate;
import java.time.LocalDateTime;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 *
 * @author john
 */
public class WeatherDecoderES extends AbstractWeatherDecoder {

	private static final Logger LOGGER = LogManager.getLogger();

	private Forecast forecast;

	private LocalDate day1 = null;

	private String[] precipitacion = new String[24];
	private String[] velocidad = new String[24];
	private String[] direccion = new String[24];
	private String[] temperatura = new String[24];
	private String[] estadoCielo = new String[24];
	private String[] probPrecipitacion = new String[24];
	//private String[] probTormenta = new String[24];
	private String[] humedadRelativa = new String[24];

	public WeatherDecoderES(String tzName) {
		super(tzName);
	}


	@Override
	public Forecast decodeJSON(String json) {

		if (json == null) {
			return null;
		}
		int pos = json.indexOf("{");
		json = json.substring(pos);

		JSONObject jo = new JSONObject(json);
		forecast = new Forecast();
		forecast.setModelRunDate(parseDate((String) jo.getString("elaborado")));
		//System.out.println("forecast from :" +jo.getString("elaborado"));
		JSONObject prediction = (JSONObject) jo.get("prediccion");
		JSONArray dias = (JSONArray) prediction.get("dia");

		try {
			for (int d = 0; d < dias.length(); d++) {
				JSONObject oneday = (JSONObject) dias.get(d);

				collectByTypeES(oneday, "estadoCielo", estadoCielo);
				collectByTypeES(oneday, "precipitacion", precipitacion);
				collectByTypeES(oneday, "probPrecipitacion", probPrecipitacion);
				collectByTypeES(oneday, "temperatura", temperatura);
				collectByTypeES(oneday, "humedadRelativa", humedadRelativa);
				collectByTypeES(oneday, "vientoAndRachaMax", null);
				String fecha = (String) oneday.get("fecha");

				LocalDateTime ldt = LocalDateTime.parse(fecha);
				LocalDate ld = ldt.toLocalDate();
				if (ld.isBefore(LocalDate.now())) {
					// Because the Spanish forecast often has a partial set of data for yesterday
					// during the early part of today.  I might have known...
					continue;
				}
				for (int h = 0; h < 24; h++) {

					Period period = new Period();
					try {
						period.setStartTime(ldt.plusHours(h));
						period.setWeatherCode(estadoCielo[h]);
						period.setTotalPrecip(precipitacion[h]);
						period.setProbOfPrecip(probPrecipitacion[h]);
						//period.set probTormenta[h]);
						period.setMaxTemp(temperatura[h]);
						period.setHumidity(humedadRelativa[h]);
						period.setWindSpeedKPH(velocidad[h]);
						period.setWindDirection(direccion[h]);
						forecast.storePeriod(period);
					} catch (Exception e) {
						LOGGER.error("bad period data " + period.toString());
						/*
						* Another example of partial data from the Spanish forecast -
						* In the early part of the day sometimes other values are present but Temp isn't.
						* Strewth.
						*/
					}

				}

			}
		} catch (Exception e) {
			LOGGER.error(e);
			e.printStackTrace();
		}

		forecast.setTodaysNumbers();
		forecast.setOK(true);

		return forecast;

	}

	private void showJsonText(String jsonIn) {

	}

	private void collectByTypeES(JSONObject oneday, String activeItem, String[] valuesArray) {
		int periodIndexMin;
		int periodIndexMax;
		String value;
		String hours;
		String velocity;
		String direction;

		JSONArray itemByHours = (JSONArray) oneday.get(activeItem);
		for (int n = 0; n < itemByHours.length(); n++) {
			JSONObject item = (JSONObject) itemByHours.get(n);
			//System.out.println(activeItem + " parts :" + item);
			hours = (String) item.get("periodo");
			if (item.has("value")) {
				value = (String) item.get("value");			// will be null when the 2 below are not null
				velocity = null;
				direction = null;
			} else {
				value = null;
				JSONArray temp;
				temp = (JSONArray) item.get("velocidad");	// will be null most of the time
				velocity = (String) temp.get(0);
				temp = (JSONArray) item.get("direccion");	// will be null most of the time
				direction = (String) temp.get(0);
			}
			//Some periods are nn-nn, not just one hour, so code as if all are a range of hours
			if (hours.length() == 4) {
				periodIndexMin = Integer.parseInt(hours.substring(0, 2), 10);
				periodIndexMax = Integer.parseInt(hours.substring(2), 10);
				if (periodIndexMax < periodIndexMin) { // 1901 is 19h..01h next day!!! Why FFS??? 
					periodIndexMax = 23;
				}
			} else {
				periodIndexMin = Integer.parseInt(hours.substring(0, 2), 10);
				periodIndexMax = periodIndexMin;
			}

			do {
				if (velocity != null && direction != null) {
					storeByTimeES("velocidad", periodIndexMin, velocity, velocidad);
					storeByTimeES("direccion", periodIndexMin, direction, direccion);
				}
				if (activeItem != "vientoAndRachaMax") {
					storeByTimeES(activeItem, periodIndexMin, value, valuesArray);
				}
				periodIndexMin++;
			} while (periodIndexMin <= periodIndexMax);

		}
	}

	private void storeByTimeES(String activeItem, int pIndex, String value, String[] tempArray) {
		//System.out.println(String.format("storeByTime %s hour:%d, value: %s", activeItem, pIndex, value));
		//If doing sky-codes, reduce 60+ spanish values to the more limited set for our purposes
		if (activeItem.equals("estadoCielo")) {
			int spainCode = 0;
			String code = "0";
			if (value.endsWith("n")) { // noche
				spainCode = Integer.parseInt(value.substring(0, 2));
				switch (spainCode) {
					case 11:
						code = "0";
						break;           //clear
					case 12:
					case 13:
					case 17:
						code = "2";
						break;	//partly cloudy
					case 14:
					case 15:
					case 16:
						code = "7";
						break;	//Cloudy
					case 43:
					case 44:
					case 45:
					case 46:
						code = "12";
						break;  // light rain
					case 23:
					case 24:
					case 25:
					case 26:
						code = "13";
						break;
					case 51:
					case 52:
					case 53:
					case 54:
					case 61:
					case 62:
					case 63:
					case 64:
						code = "28";
						break;
				}

			} else {
				spainCode = Integer.parseInt(value);
				switch (spainCode) {
					case 11:
						code = "1";
						break;           //clear
					case 12:
					case 13:
					case 17:
						code = "3";
						break;	//partly cloudy
					case 14:
					case 15:
					case 16:
						code = "7";
						break;	//Cloudy
					case 43:
					case 44:
					case 45:
					case 46:
						code = "12";
						break;  // light rain
					case 23:
					case 24:
					case 25:
					case 26:
						code = "14";
						break;
					case 51:
					case 52:
					case 53:
					case 54:
					case 61:
					case 62:
					case 63:
					case 64:
						code = "29";
						break;
				}

			}
			//System.out.println(String.format("%s at %s =%s", activeItem, pIndex, code));
			tempArray[pIndex] = code;
			return;
		}

		if (value.trim().equals("Ip")) {		//AEMet - wtf is this about???
			value = "0";
		}
		tempArray[pIndex] = value;

	}

}

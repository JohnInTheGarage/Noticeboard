package driverway.nb.weatherfinder;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 *
 * @author john
 */
public class WeatherDecoderOW extends AbstractWeatherDecoder {

	private static final Logger LOGGER = LogManager.getLogger();
	private Forecast forecast;

	public WeatherDecoderOW(String tzName) {
		super(tzName);
	}

	@Override
	public Forecast decodeJSON(String json) {

		JSONObject jo = new JSONObject(json);
		JSONObject current = jo.getJSONObject("current");
		
		forecast = new Forecast();
		forecast.setModelRunDate(fixUnixDate(current.getInt("dt")));  // timestamp of forecast not available with OW?

		JSONArray timeSeries = (JSONArray) jo.get("hourly");
		for (int index = 0; index < timeSeries.length(); index++) {
			storeByTimeOW((JSONObject) timeSeries.get(index));
		}

		forecast.setTodaysNumbers();
		return forecast;
	}

	private void showJsonText(String jsonIn) {

	}

	private void storeByTimeOW(JSONObject item) {
		Period period = new Period();

		period.setStartTime(fixUnixDate(item.getInt("dt")));
		JSONArray weatherArray = (JSONArray) item.getJSONArray("weather");
		JSONObject weather = (JSONObject) weatherArray.get(0);

		period.setWeatherCode(convertCode(weather.getInt("id"),  weather.getString("icon")));
		period.setTotalPrecip("0.0");
		period.setProbOfPrecip(item.getInt("pop"));
		period.setMaxTemp(item.getDouble("temp"));
		period.setHumidity(item.getInt("humidity"));
		period.setWindSpeed(item.getDouble("wind_speed"));
		period.setWindDirection(item.getInt("wind_deg"));
		forecast.storePeriod(period);

	}

	/*
	* Convert OpenWeather codes to match our requirements
	 */
	private String convertCode(int owCode, String icon) {
		String code = "0";
		if (icon.endsWith("n")) { // night
			switch (owCode) {
				case 800:
					code = "0";
					break;           //clear
				case 801:
				case 802:
				case 803:
					code = "2";
					break;	//partly cloudy
				case 804:
					code = "7";
					break;	//Cloudy
				case 500:
				case 501:
				case 520:
				case 521:
					code = "12";
					break;  // light rain
				case 502:
				case 503:
				case 504:
				case 511:
				case 522:
				case 531: // Ragged!
					code = "13"; //heavy rain
					break;
				case 200:
				case 201:
				case 202:
				case 210:
				case 211:
				case 212:
				case 221:
				case 230:
				case 231:
				case 232:
					code = "28"; //thunder shower
					break;
			}

		} else {
			switch (owCode) {
				case 800:
					code = "1";
					break;           //clear
				case 801:
				case 802:
				case 803:
					code = "3";
					break;	//partly cloudy
				case 804:
					code = "7";
					break;	//Cloudy
				case 500:
				case 501:
				case 520:
				case 521:
					code = "12";
					break;  // light rain
				case 502:
				case 503:
				case 504:
				case 511:
				case 522:
				case 531: // Ragged!
					code = "14"; // heavy rain
					break;
				case 200:
				case 201:
				case 202:
				case 210:
				case 211:
				case 212:
				case 221:
				case 230:
				case 231:
				case 232:
					code = "29"; // thunder shower
					break;
			}

		}
		return code;
	}
}

package driverway.nb.weatherfinder;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 *
 * @author john
 */
public class WeatherDecoderUK extends AbstractWeatherDecoder{

	private static final Logger LOGGER = LogManager.getLogger();
	private Forecast forecast;
	
	public WeatherDecoderUK(String tzName) {
		super(tzName);
	}


	@Override
	public Forecast decodeJSON(String json) {
		//showJsonText(json);
		forecast = new Forecast();
		JSONObject jo = new JSONObject(json);
		JSONArray features = (JSONArray) jo.get("features");
		JSONObject thing = (JSONObject) features.get(0);
		JSONObject properties = (JSONObject) thing.get("properties");

		forecast.setModelRunDate(parseDate((String) properties.getString("modelRunDate")));

		JSONArray timeSeries = (JSONArray) properties.get("timeSeries");
		for (int index = 0; index < timeSeries.length(); index++) {
			storeByTimeUK((JSONObject) timeSeries.get(index));
		}

		forecast.setTodaysNumbers();
		return forecast;
	}

	private void showJsonText(String jsonIn) {

	}


	private void storeByTimeUK(JSONObject item) {
		Period period = new Period();

		period.setStartTime( parseDate(item.getString("time")) );
		period.setWeatherCode( item.getInt("significantWeatherCode") );
		period.setTotalPrecip(item.getDouble("totalPrecipAmount") );
		period.setProbOfPrecip(item.getInt("probOfPrecipitation") );
		period.setMaxTemp(item.getDouble("maxScreenAirTemp") );
		period.setHumidity(item.getInt("screenRelativeHumidity") );
		period.setWindSpeed(item.getDouble("windSpeed10m") );
		period.setWindDirection(item.getInt("windDirectionFrom10m") );
		forecast.storePeriod(period);

	}
}

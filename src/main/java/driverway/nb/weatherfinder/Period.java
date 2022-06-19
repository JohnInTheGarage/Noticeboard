package driverway.nb.weatherfinder;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoField;

/**
 *
 * @author john
 */
public class Period {

	private LocalDateTime startTime;
	private double maxTemp;
	private double minTemp;
	private double feelsLikeTemp;
	private double windSpeed;
	private double totalPrecip;
	private int windDirection;
	private int weatherCode;
	private int probOfPrecip;

	private int humidity;

	@Override
	public String toString() {

		var d1=	 getStartTime();
				 var d2=getMaxTemp();
				 var d3 = getWindSpeed();
				 var d4 = getWindDirection();
				 var d5 = getWeatherCode();
				 var d6 = getTotalPrecip();
				 var d7 = getProbOfPrecip();
				 var d8 = getHumidity();

		String me = String.format("%s  temp=%s, wind =%s from %s, code =%d, rain = %s, rain prob=%s, humidity =%s",
				 getStartTime().format(DateTimeFormatter.ofPattern("MM-dd HH:mm")),
				 getMaxTemp(),
				 getWindSpeed(),
				 getWindDirection(),
				 getWeatherCode(),
				 getTotalPrecip(),
				 getProbOfPrecip(),
				 getHumidity()
		);
		return me;
	}

	/**
	 * @return the startTime
	 */
	public LocalDateTime getStartTime() {
		return startTime;
	}

	public LocalDate getStartDate() {
		return startTime.toLocalDate();
	}

	public String getStartTimeText() {
		String dow = startTime.getDayOfWeek().name();
		int trunc = 3;
		if (dow.startsWith("T")) {
			trunc = 4;
		}

		return dow.substring(0, trunc) + "\n" + startTime.get(ChronoField.HOUR_OF_DAY) + ":00";
	}

	/**
	 * @param _startTime the startTime to set
	 */
	public void setStartTime(LocalDateTime _startTime) {
		this.startTime = _startTime;
	}

	/**
	 * @return the maxTemp
	 */
	public String getMaxTemp() {
		return String.format("%.1f", maxTemp);
	}

	/**
	 * @return the maxTemp
	 */
	public double getRawMaxTemp() {
		return maxTemp;
	}

	/**
	 * @param _maxTemp the maxTemp to set
	 */
	public void setMaxTemp(String _maxTemp) {
		maxTemp = Double.valueOf(_maxTemp);
	}

	/**
	 * @param _maxTemp the maxTemp to set
	 */
	public void setMaxTemp(Double _maxTemp) {
		maxTemp = _maxTemp;
	}

	/**
	 * @return the minTemp
	 */
	public String getMinTemp() {
		return String.valueOf(minTemp);
	}

	/**
	 * @param _minTemp the minTemp to set
	 */
	public void setMinTemp(double _minTemp) {
		this.minTemp = _minTemp;
	}

	/**
	 * @return the feelsLikTemp
	 */
	public String getFeelsLikeTemp() {
		return String.format("%.1f", feelsLikeTemp);
	}

	/**
	 * @param _feelsLikeTemp the feelsLikTemp to set
	 */
	public void setFeelsLikeTemp(double _feelsLikeTemp) {
		this.feelsLikeTemp = _feelsLikeTemp;
	}

	/**
	 * @return the windSpeed
	 */
	public String getWindSpeed() {
		return String.format("%.1f", windSpeed);
	}

	/**
	 * @param _wind_M_S the windSpeed to set changed from Meters / sec to Km /
	 * hour when stored
	 */
	public void setWindSpeed(String _wind_M_S) {
		windSpeed = (Double.valueOf(_wind_M_S)  / 1000) * 3600;
	}

	/**
	 * @param _wind_M_S the windSpeed to set changed from Meters / sec to Km /
	 * hour when stored
	 */
	public void setWindSpeed(Double _wind_M_S) {
		windSpeed = (_wind_M_S / 1000) * 3600;
	}

	public void setWindSpeedKPH(String _wind_KPH) {
		if (_wind_KPH != null){
		windSpeed = Double.valueOf(_wind_KPH);
		}
	}

	/**
	 * @return the windDirection
	 */
	public int getWindDirection() {
		return windDirection;
	}

	/**
	 * @param _windDirection the windDirection to set UK data gives compass
	 * degrees, Span gives compass point names, Convert both to 0..8 for calm,
	 * N, NE, E, SE, S, SW, W, NW. 0..8 used to select SVG string to make
	 * background Icon of wind direction.
	 */
	public void setWindDirection(int _windDirection) {

		if (_windDirection >= 338 || _windDirection <= 23) {  // Note : "OR" not "AND"
			windDirection = 1;
		}
		if (_windDirection > 23 && _windDirection <= 68) {
			windDirection = 2;
		}
		if (_windDirection > 68 && _windDirection <= 113) {
			windDirection = 3;
		}
		if (_windDirection > 113 && _windDirection <= 158) {
			windDirection = 4;
		}
		if (_windDirection > 158 && _windDirection <= 203) {
			windDirection = 5;
		}
		if (_windDirection > 203 && _windDirection <= 248) {
			windDirection = 6;
		}
		if (_windDirection > 248 && _windDirection <= 293) {
			windDirection = 7;
		}
		if (_windDirection > 293 && _windDirection <= 338) {
			windDirection = 8;
		}
	}

	/*
	* N/Norte, NE/Nordeste, E/Este, SE/Sudeste, S/Sur, SO/Suroeste, O/Oeste, NO/Noroeste, C/Calma",
	 */
	public void setWindDirection(String directionES) {
		windDirection = 0;
		if (directionES != null) {
			switch (directionES) {
				case ("N"):
					windDirection = 1;
					break;
				case ("NE"):
					windDirection = 2;
					break;
				case ("E"):
					windDirection = 3;
					break;
				case ("SE"):
					windDirection = 4;
					break;
				case ("S"):
					windDirection = 5;
					break;
				case ("SO"):
					windDirection = 6;
					break;
				case ("O"):
					windDirection = 7;
					break;
				case ("NO"):
					windDirection = 8;
					break;
				default:
					windDirection = 0;
			}
		}

	}

	/**
	 * @return the weatherCode
	 */
	public int getWeatherCode() {
		return weatherCode;
	}

	/**
	 * @param _weatherCode the weatherCode to set
	 */
	public void setWeatherCode(String _weatherCode) {
		if (_weatherCode != null){
			weatherCode = Integer.parseInt(_weatherCode,10);
		}
	}
	public void setWeatherCode(int _weatherCode) {
		weatherCode = _weatherCode;
	}

	/**
	 * @return the probOfPrecip
	 */
	public String getProbOfPrecip() {
		return String.valueOf(probOfPrecip) + "%";
	}

	/**
	 * @return the probOfPrecip
	 */
	public int getRawProbOfPrecip() {
		return probOfPrecip;
	}

	/**
	 * @param _probOfPrecip the probOfPrecip to set
	 */
	public void setProbOfPrecip(String _probOfPrecip) {
		if (_probOfPrecip == null || _probOfPrecip.isEmpty() ) {
			_probOfPrecip = "0";
		}
		probOfPrecip = Integer.parseInt(_probOfPrecip,10);
	}

	/**
	 * @param _probOfPrecip the probOfPrecip to set
	 */
	public void setProbOfPrecip(int _probOfPrecip) {
		probOfPrecip = _probOfPrecip;
	}

	/**
	 * @return the totalPrecip
	 */
	public String getTotalPrecip() {
		if (totalPrecip > 0.0) {
			return String.valueOf(totalPrecip);  // + "mm";
		} else {
			return "_";
		}

	}

	/**
	 * @param _totalPrecip the totalPrecip to set
	 */
	public void setTotalPrecip(String _totalPrecip) {
		totalPrecip = Double.valueOf(_totalPrecip);
	}
	/**
	 * @param _totalPrecip the totalPrecip to set
	 */
	public void setTotalPrecip(Double _totalPrecip) {
		totalPrecip = _totalPrecip;
	}

	/**
	 * @return the humidity
	 */
	public String getHumidity() {
		return String.valueOf(humidity + "%");
	}

	/**
	 * @param _humidity the humidity to set
	 */
	public void setHumidity(String _humidity) {
		humidity = Integer.parseInt(_humidity,10);
	}

	/**
	 * @param _humidity the humidity to set
	 */
	public void setHumidity(int _humidity) {
		humidity = _humidity;
	}

}

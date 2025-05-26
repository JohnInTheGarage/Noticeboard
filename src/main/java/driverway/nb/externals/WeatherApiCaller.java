/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package driverway.nb.externals;

import driverway.nb.weatherfinder.WeatherAlert;
import java.util.ArrayList;


/**
 *
 * @author john
 */
public interface WeatherApiCaller {
    
    public String getForecastJSON();
    
    public ArrayList<WeatherAlert> getAlerts();
    
}

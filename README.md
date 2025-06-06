# Noticeboard

June 2025 - Extracted Sun & Moon data to separate project for use elsewhere. https://github.com/JohnInTheGarage/SunAndMoon.

May 2025 - Add sunrise & sunset times, re-work moonphase, refactor external apis.

Dec 2024 - Add Moon Phase simulation.

Dec 2023 - Fixed bugs and added Nasa "BlueMarble" images.

Dec 2022 - Changed Xmas lights to use MQTT instead of HTTP requests as its a lighter workload for the Pi 2 that controls
the relays for the lights.
Also adjusted the calendar's list of events to include next month's events.  Previously, in the last days of this month we 
have no idea what's coming up soon.

Sept 2022 - Updated to include showing Astronomy Picture of the Day images (APOD) during evening hours.

This is a project for a Raspberry Pi (with Arm7 cpu or better) and the official 7-inch touch-screen display.  It hangs on the wall in my hallway so it can be seen regularly.

It shows weather forecast details from either the UK Met office API (https://www.metoffice.gov.uk/services/data/datapoint) 
or the Spanish equivalent (http://www.aemet.es/es/datos_abiertos/AEMET_OpenData), as it was developed partly during my time in Spain.  
The OpenWeathermap.org api is begun but unfinished atm (June 2022).

The APIs return data in JSON format but they have different structures, so the program uses an internal concept of forecast period to try and create a single approach to storing the information.  The forecast periods are displayed by a button ">>" on the main page.

To avoid screen burn-in, the fixed-layout data on the main screen is overlaid with a downloaded satellite image from the EUMet satellite which scrolls up and down slowly.  Timestamped copies of the last downloaded image are kept so additional storeage is sensible.  This allows animations to be generated with whatever software you prefer. There is also a calendar feature which displays data from my Google calendar for the current month.

The display is done with JavaFX and I build with Netbeans 12.4 on a separate PC (not on the Pi) so on the PC I use the Bellsoft full JDK which unusually, and conveniently, includes JavaFX.  On the Pi I use the Bellsoft JRE for Arm which also includes JavaFX.  Both on V15 at the moment. 


To use this properly, you will need to have or set up, your own accounts with the API providers that you use.  The api credentials are then put in text files for the program to use at runtime.  To locate those text files a system property must be defined :

Probably easiest to update /etc/environment with:

<pre>
NBPROPERTIES=&lt;your preferred location&gt;
</pre>


The properties files are split by function and have these names (in your preferred location) 
<pre>
Calendar - GoogleServiceCredentials.json
Satellite image - EUmet.properties
Weather and overall control - noticeboard.properties
</pre>
The various APIs are called at intervals defined in the properties files.

The project uses Maven for the build so even if you don't use Netbeans, other IDEs should understand it.  I struggled for a long time with re-structuring the original projects to include all the dependencies in one jar for easy deployment, but eventually gave up.  
Deployment now means copying these things from NoticeBoard/target/ to your pi (assuming that's where it will run)
<pre>
a) the NoticeBoard-2.0-SNAPSHOT.jar and 
b) the dependency-jars directory.
plus the three config files updated with your own credentials for the various acccounts as mentioned above.
</pre>

The program is set up to auto-run when the Pi boots with these 4 lines in /etc/xdg/lxsession/LXDE-pi/autostart :

<pre>
@lxpanel --profile LXDE-pi
@pcmanfm --desktop --profile LXDE-pi
@xscreensaver -no-splash
@/usr/bin/java -jar &lt;path to NoticeBoard.jar&gt; 
</pre>

  
To turn off the backlight between 23:59 and 06:00, set up "crontab -e" with these 2 lines 
<pre>  
59 23 * * * /usr/bin/sudo sh -c "echo 1 > /sys/class/backlight/rpi_backlight/bl_power"
00 06 * * * /usr/bin/sudo sh -c "echo 0 > /sys/class/backlight/rpi_backlight/bl_power"
</pre>

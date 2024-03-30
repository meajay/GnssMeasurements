**GNSS Logger :**

This Android project provides a GNSS (Global Navigation Satellite System) logger that records GNSS measurements and navigation messages. 
It allows you to capture real-time GNSS data and share it as a text file containing recorded logs.
<br>
##

**Features :**

**GNSS Measurements:** Capture raw GNSS measurements (pseudoranges, carrier phases, etc.).

**Navigation Messages:** Record navigation messages (ephemeris, almanac, etc.).

**Customizable Logging:** Enable or disable logging of measurements and navigation messages.

**Shareable Logs:** Export recorded data as a text file for analysis or sharing.

##

**Usage :** 

**Initialize GNSS Container:** Create an instance of GnssContainer with appropriate loggers (e.g., file logger, console logger).

**Start GNSS Logging:** Register the locationListener and gnssMeasurementsEventListener to start capturing GNSS data.

**Stop Logging:** Unregister the listeners when logging is complete.

</br>

**Example :**

Initialize GNSS Container
```
GnssContainer gnssContainer = new GnssContainer(context, new FileLogger("gnss_logs.txt"));
```
</br>

Start GNSS Logging
```
locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 0, locationListener);
locationManager.registerGnssMeasurementsCallback(gnssMeasurementsEventListener);
```
</br>

Stop Logging
```
locationManager.removeUpdates(locationListener);
locationManager.unregisterGnssMeasurementsCallback(gnssMeasurementsEventListener);
```
</br>
</br>


Feel free to explore the code and adapt it to your specific use case. Happy GNSS logging! 🛰️📊

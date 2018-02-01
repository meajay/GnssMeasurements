package testme.java.com.gpsdatalogger;

import android.Manifest;

import java.util.concurrent.TimeUnit;

/**
 * Created by achau on 30-01-2018.
 */

public  class Constants {

    public static String LOG_TAG = "LOG";
    public static final int LOCATION_REQUEST_ID = 1000;
    public static final String[] REQUIRED_PERMISSIONS = {
            Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.WRITE_EXTERNAL_STORAGE , Manifest.permission.READ_EXTERNAL_STORAGE
    };

    public static final long LOCATION_RATE_GPS_MS = TimeUnit.SECONDS.toMillis(1L);
    public static final long LOCATION_RATE_NETWORK_MS = TimeUnit.SECONDS.toMillis(60L);
    public static final String GNSS_LOGGER = "GNSS_LOGGER";
    public static final String PROVIDER_ENABLED = "PROVIDER_ENABLED : " ;
    public static final String PROVIDER_DISABLED = "PROVIDER_DISABLED : " ;
    public static final String LOCATION_CHANGED = "LOCATION_CHANGED : " ;
    public static final String LOCATION_STATUS_CHANGED = "LOCATION_STATUS_CHANGED : " ;
    public static final String GNSS_MEASUREMENT_EVENT = "GNSS_MEASUREMENT_EVENT : " ;
    public static final String GNSS_CLOCK_INFORMATION = "GNSS_CLOCK_INFORMATION : " ;
    public static final  String GNSS_INFO = "GNSS_INFO :" ;
    public static final String GNSS_DELTA_ACCUMULATED_LAST_CHANNEL = "GNSS_DELTA_ACCUMULATED_LAST_CHANNEL_RESET : " ;
    public static final String GNSS_ACCUMULATED_CHANNEL = "GNSS_DELTA_ACCUMULATED_LAST_CHANNEL : ";
    public static final String GNSS_DELTA_ACCUMULATED_UNCERTAINITY = "GNSS_DELTA_ACCUMULATED_UNCERTAINITY : " ;
    public static final String GNSS_AUTO_CONTROL_LEVEL_DB = "GNSS_AUTO_CONTROL_LEVEL_DB : " ;
    public static final String GNSS_NUMBER_OF_CARRIER_CYCLES = "GNSS_NUMBER_OF_CARRIER_CYCLES : " ;
    public static final String GNSS_CARRIER_PHASE = "GNSS_CARRIER_PHASE" ;
    public static final String GNSS_CARRIER_PHASE_UNCERTAINITY = "GNSS_CARRIER_PHASE_UNCERTAINITY : " ;
    public static final String GNSS_CARRIER_TO_NOISE_DENSITY = "GNSS_CARRIER_TO_NOISE_DENSITY : " ;
    public static final String GNSS_CONST = "GNSS_CARRIER_TO_NOISE_DENSITY : " ;
    public static final String GNSS_PSEUDO_RANGE_RATE = "GNSS_PSEUDO_RANGE_RATE : " ;
    public static final String GNSS_ESTIMATED_TIME_ERROR = "GNSS_ESTIMATED_TIME_ERROR : " ;
    public static final String GNSS_STATUS_CHANGED = "GNSS_STATUS_CHANGED : " ;
    public static final String GNSS_NAVAIGATION_MESSAGE = "GNSS_NAVAIGATION_MESSAGE : " ;
    public static final String GNSS_NAVAIGATION_MESSAGE_CHANGED = "GNSS_NAVAIGATION_MESSAGE_CHANGED : " ;
    public static final String GNSS_MEASUREMENT_STATUS_CHANGED = "GNSS_MEASUREMENT_STATUS_CHANGED : " ;
    public static final String REGISTRATION = "REGISTRATION :" ;
    public static final String FILE_NAME = "Gnss_logs" ;
    public static final int MINIMUM_USABLE_BYTES = 1500 ;
    public static final int MAX_FILES_STORED = 100;

}

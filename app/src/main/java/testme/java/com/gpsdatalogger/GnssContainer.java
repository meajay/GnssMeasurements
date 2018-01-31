package testme.java.com.gpsdatalogger;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.GnssMeasurement;
import android.location.GnssMeasurementsEvent;
import android.location.GnssNavigationMessage;
import android.location.GnssStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.OnNmeaMessageListener;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.SystemClock;
import android.support.v4.app.ActivityCompat;

import java.util.Arrays;
import java.util.List;

import testme.java.com.gpsdatalogger.interfaces.GnssListener;

/**
 * Created by achau on 30-01-2018.
 */

public class GnssContainer {

    private final List<Logger> mLoggers;
    private LocationManager locationManager;
    private boolean mLogMeasurements = true;
    private boolean mLogNavigationMessages = true;
    private Context context;

    public GnssContainer(Context context, Logger... loggers) {
        this.mLoggers = Arrays.asList(loggers);
        locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        this.context = context;
    }

    public final LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            for (Logger logger : mLoggers) {
                logger.onLocationChanged(location);
            }
        }

        @Override
        public void onProviderDisabled(String provider) {
            for (Logger logger : mLoggers) {
                logger.onProviderDisabled(provider);
            }
        }

        @Override
        public void onProviderEnabled(String provider) {
            for (Logger logger : mLoggers) {
                logger.onProviderEnabled(provider);
            }
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            for (Logger logger : mLoggers) {
                logger.onLocationStatusChanged(provider, status, extras);
            }
        }
    };

    private final GnssMeasurementsEvent.Callback gnssMeasurementsEventListener =
            new GnssMeasurementsEvent.Callback() {
                @Override
                public void onGnssMeasurementsReceived(GnssMeasurementsEvent event) {

                    for (Logger logger : mLoggers) {
                        logger.onGnssMeasurementsReceived(event);
                    }
                }

                @Override
                public void onStatusChanged(int status) {
                    for (Logger logger : mLoggers) {
                        logger.onGnssMeasurementsStatusChanged(status);
                    }
                }
            };

    private final GnssNavigationMessage.Callback gnssNavigationMessageListener =
            new GnssNavigationMessage.Callback() {
                @Override
                public void onGnssNavigationMessageReceived(GnssNavigationMessage event) {
                    for (Logger logger : mLoggers) {
                        logger.onGnssNavigationMessageReceived(event);
                    }
                }

                @Override
                public void onStatusChanged(int status) {
                    for (Logger logger : mLoggers) {
                        logger.onGnssNavigationMessageStatusChanged(status);
                    }
                }
            };

    private final GnssStatus.Callback gnssStatusListener =
            new GnssStatus.Callback() {
                @Override
                public void onStarted() {
                }

                @Override
                public void onStopped() {
                }

                @Override
                public void onSatelliteStatusChanged(GnssStatus status) {
                    for (Logger logger : mLoggers) {
                        logger.onGnssStatusChanged(status);
                    }
                }
            };

    public void setLogMeasurements(boolean value) {
        mLogMeasurements = value;
    }


    public void setLogNavigationMessages(boolean value) {
        mLogNavigationMessages = value;
    }

    private final OnNmeaMessageListener nmeaListener =
            new OnNmeaMessageListener() {
                @Override
                public void onNmeaMessage(String s, long l) {
                    for (GnssListener logger : mLoggers) {
                        logger.onNmeaReceived(l, s);
                    }
                }
            };

    @SuppressLint("MissingPermission")
    public void registerLocation() {
        boolean isGpsProviderEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        if (isGpsProviderEnabled) {
            locationManager.requestLocationUpdates(
                    LocationManager.NETWORK_PROVIDER,
                    Constants.LOCATION_RATE_NETWORK_MS,
                    0.0f /* minDistance */,
                    locationListener);
            locationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,
                    Constants.LOCATION_RATE_GPS_MS,
                    0.0f /* minDistance */,
                    locationListener);
        }
        logRegistration("LOCATION_UPDATES", isGpsProviderEnabled);
    }

    private void logRegistration(String listener, boolean result) {
        for (Logger logger : mLoggers) {
            logger.onListenerRegistration(listener, result);
        }
    }

    public void registerAll() {
        registerLocation();
        registerMeasurements();
        registerNavigation();
        registerGnssStatus();
        registerNmea();
    }

    public void unregisterAll() {
        unregisterLocation();
        unregisterMeasurements();
        unregisterNavigation();
        unregisterGpsStatus();
        unregisterNmea();
    }

    public void registerSingleNetworkLocation() {
        boolean isNetworkProviderEnabled =
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        if (isNetworkProviderEnabled) {
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            locationManager.requestSingleUpdate(
                    LocationManager.NETWORK_PROVIDER, locationListener, null);
        }
        logRegistration("LOCATION_UPDATES", isNetworkProviderEnabled);
    }

    public void registerSingleGpsLocation() {
        boolean isGpsProviderEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        if (isGpsProviderEnabled) {
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            locationManager.requestSingleUpdate(LocationManager.GPS_PROVIDER, locationListener, null);
        }
        logRegistration("LOCATION_UPDATES", isGpsProviderEnabled);
    }

    public void unregisterLocation() {
        locationManager.removeUpdates(locationListener);
    }

    public void registerMeasurements() {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        logRegistration(
                "GNSS_MEASUREMENTS",
                locationManager.registerGnssMeasurementsCallback(gnssMeasurementsEventListener));
    }

    public void unregisterMeasurements() {
        locationManager.unregisterGnssMeasurementsCallback(gnssMeasurementsEventListener);
    }

    public void registerNavigation() {
        logRegistration(
                "GpsNavigationMessage",
                locationManager.registerGnssNavigationMessageCallback(gnssNavigationMessageListener));
    }

    public void unregisterNavigation() {
        locationManager.unregisterGnssNavigationMessageCallback(gnssNavigationMessageListener);
    }

    public void registerGnssStatus() {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        logRegistration("GnssStatus", locationManager.registerGnssStatusCallback(gnssStatusListener));
    }

    public void unregisterGpsStatus() {
        locationManager.unregisterGnssStatusCallback(gnssStatusListener);
    }

    public void registerNmea() {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        logRegistration("Nmea", locationManager.addNmeaListener(nmeaListener));
    }

    public void unregisterNmea() {
        locationManager.removeNmeaListener(nmeaListener);
    }

    
}

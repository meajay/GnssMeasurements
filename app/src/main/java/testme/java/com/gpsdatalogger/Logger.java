package testme.java.com.gpsdatalogger;

import android.content.Context;
import android.location.GnssClock;
import android.location.GnssMeasurement;
import android.location.GnssMeasurementsEvent;
import android.location.GnssNavigationMessage;
import android.location.GnssStatus;
import android.location.Location;
import android.location.LocationProvider;
import android.os.Build;
import android.os.Bundle;
import android.provider.SyncStateContract;
import android.support.annotation.RequiresApi;
import android.util.Log;

import com.google.protobuf.MessageLite;

import java.text.DecimalFormat;
import java.util.ArrayList;

import testme.java.com.gpsdatalogger.interfaces.GnssListener;
import tutorial.Gnss;

import static tutorial.Gnss.*;

/**
 * Created by achau on 30-01-2018.
 */

public class Logger implements GnssListener {

    private GpsLoggerActivity.UIComponent component;

    interface LoggerDataChanged{
        void onDataChanged(String s);
    }

    private LoggerDataChanged lDataChanged;

   private Gnss.Location  gnssLocation ;
   private Gnss.GnsClock  gnsClock ;
   private Gnss.GnsMeasurements gnssMeasurements ;
   private Gnss.GnsStatus gnsStatus ;
   private Gnss.GnsEvents gnsEvents ;
   private Gnss.GnsNmeaEvent gnsNmea ;

   public Logger(LoggerDataChanged loggerDataChanged){
       lDataChanged = loggerDataChanged ;
   }

    public synchronized GpsLoggerActivity.UIComponent getComponent() {
        return component;
    }

    public synchronized void setComponent(GpsLoggerActivity.UIComponent component) {
        this.component = component;
    }

    @Override
    public void onProviderEnabled(String provider) {
        logLocationEvent(Constants.PROVIDER_ENABLED + provider+"\n");
        gnssLocation = Gnss.Location.newBuilder()
                        .setIsProviderEnabled(true).build();
        lDataChanged.onDataChanged(gnssLocation.toString());
    }

    @Override
    public void onProviderDisabled(String provider) {
        logLocationEvent(Constants.PROVIDER_DISABLED  + provider+"\n");
        gnssLocation = Gnss.Location.newBuilder()
                .setIsProviderEnabled(false).build();
        lDataChanged.onDataChanged(gnssLocation.toString());
     //   gnssLocation.setIsProviderEnabled(false);
    }

    @Override
    public void onLocationChanged(Location location) {
       String event = Constants.LOCATION_CHANGED +"\n"+ "LATITUDE " +location.getLatitude() + "\n" + "LATITUDE " + location.getLatitude()
        +"\n"+ "ALTITUDE " +location.getAltitude() + "\n" + "SPEED " + location.getSpeed()
        +"\n"+ "ACCURACY " +location.getAccuracy() + "\n" + "TIME " + location.getTime() + "\n";
       logLocationEvent(event);

        gnssLocation = Gnss.Location.newBuilder()
                .setIsLocationChanged(true)
                .setLatitude(location.getLatitude())
                .setLongitude(location.getLongitude())
                .setAltitude(location.getAltitude())
                .setSpeed(location.getSpeed())
                .setAccuracy(location.getAccuracy())
                .setTime(location.getTime())
                .build();
        lDataChanged.onDataChanged(gnssLocation.toString());
    }

    @Override
    public void onLocationStatusChanged(String provider, int status, Bundle extras) {
        String message =
                String.format(
                        Constants.LOCATION_STATUS_CHANGED ,
                    //    "onStatusChanged: provider=%s, status=%s, extras=%s",
                        provider, locationStatusToString(status), extras +"\n");
        logLocationEvent(message);
        gnssLocation = Gnss.Location.newBuilder()
                .setIsLocationStatusChanged(true)
                .build();
        lDataChanged.onDataChanged(gnssLocation.toString());
    }

    @Override
    public void onGnssMeasurementsReceived(GnssMeasurementsEvent event) {
        StringBuilder builder = new StringBuilder(Constants.GNSS_MEASUREMENT_EVENT + "\n");

        builder.append(Constants.GNSS_CLOCK_INFORMATION +toStringClock(event.getClock()) + "\n");
        builder.append(Constants.GNSS_INFO + "\n") ;

        for (GnssMeasurement measurement : event.getMeasurements()) {
            builder.append(Constants.GNSS_DELTA_ACCUMULATED_LAST_CHANNEL + measurement.getAccumulatedDeltaRangeMeters() + "/n");
            builder.append(Constants.GNSS_ACCUMULATED_CHANNEL + measurement.getAccumulatedDeltaRangeState() + "/n");
            builder.append(Constants.GNSS_DELTA_ACCUMULATED_UNCERTAINITY + measurement.getAccumulatedDeltaRangeUncertaintyMeters() + "/n");
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                builder.append(Constants.GNSS_AUTO_CONTROL_LEVEL_DB + measurement.getAutomaticGainControlLevelDb() + "/n");
            }
            builder.append( Constants.GNSS_NUMBER_OF_CARRIER_CYCLES + measurement.getCarrierCycles() + "/n");
            builder.append(Constants.GNSS_CARRIER_PHASE + measurement.getCarrierPhase() + "/n");
            builder.append(Constants.GNSS_CARRIER_PHASE_UNCERTAINITY + measurement.getCarrierPhaseUncertainty() + "/n");
            builder.append(Constants.GNSS_CARRIER_TO_NOISE_DENSITY + measurement.getCn0DbHz() + "/n");
            builder.append(Constants.GNSS_PSEUDO_RANGE_RATE + measurement.getPseudorangeRateMetersPerSecond() + "/n");
            builder.append(Constants.GNSS_ESTIMATED_TIME_ERROR + measurement.getReceivedSvTimeUncertaintyNanos() + "/n");

            gnssMeasurements = Gnss.GnsMeasurements.newBuilder()
                    .setAccumulatedDeltaRangeLastReset(measurement.getAccumulatedDeltaRangeMeters())
                    .setAccumulatedDeltaRange( measurement.getAccumulatedDeltaRangeState())
                    .setCarrierCycles(measurement.getCarrierCycles())
                    .setCarrierPhaseUncertainity(measurement.getCarrierPhaseUncertainty())
                    .setDeltaAccumulatedUncertainity(measurement.getAccumulatedDeltaRangeUncertaintyMeters())
                    .setCarrierPhase(measurement.getCarrierPhase())
                    .setPseudoRangeRate(measurement.getPseudorangeRateMetersPerSecond())
                    .setTimeErrorEstimate(measurement.getReceivedSvTimeUncertaintyNanos())
                    .setCarrierToNoiseDensity(measurement.getCn0DbHz())
                    .build();
            lDataChanged.onDataChanged(gnssLocation.toString());
        }
    }

    @Override
    public void onGnssMeasurementsStatusChanged(int status) {
        logMeasurementEvent(Constants.GNSS_MEASUREMENT_STATUS_CHANGED + gnssMeasurementsStatusToString(status) + "\n");
        gnsEvents = Gnss.GnsEvents.newBuilder()
                .setGnssMeasurementStatus(status).build();
        lDataChanged.onDataChanged(gnsEvents.toString());
    }

    @Override
    public void onGnssNavigationMessageReceived(GnssNavigationMessage event) {
        logNavigationMessageEvent(Constants.GNSS_NAVAIGATION_MESSAGE + event + "\n");
        gnsEvents = Gnss.GnsEvents.newBuilder()
                .setGnssMessageReceived(event.toString()).build();
        lDataChanged.onDataChanged(gnsEvents.toString());
    }

    @Override
    public void onGnssNavigationMessageStatusChanged(int status) {
        logNavigationMessageEvent(Constants.GNSS_NAVAIGATION_MESSAGE_CHANGED  + getGnssNavigationMessageStatus(status) + "\n");
        gnsEvents = Gnss.GnsEvents.newBuilder()
                .setGnssNavigationChanged(getGnssNavigationMessageStatus(status)).build();
        lDataChanged.onDataChanged(gnsEvents.toString());
    }

    @Override
    public void onGnssStatusChanged(GnssStatus gnssStatus) {
        logStatusEvent(Constants.GNSS_STATUS_CHANGED + gnssStatusToString(gnssStatus) + "\n");
        gnsEvents = Gnss.GnsEvents.newBuilder()
                    .setIsGnssStatusChanged(true).build();
        lDataChanged.onDataChanged(gnsEvents.toString());
    }

    @Override
    public void onListenerRegistration(String listener, boolean result) {
        logEvent(Constants.REGISTRATION , result + "\n");
    }

    @Override
    public void onNmeaReceived(long l, String s) {
        logNmeaEvent(String.format( "ON_NMEA_RECEIVED TIMESTAMP : " + l + "\n"));
        gnsNmea = Gnss.GnsNmeaEvent.newBuilder()
                .setNmeaReceivedTimestamp(l).build();
        lDataChanged.onDataChanged(gnsNmea.toString());
    }

    @Override
    public void onTTFFReceived(long l) {
        logNmeaEvent(String.format( "ON TTFF_RECEIVED TIMESTAMP : " +l) + "\n");
        gnsEvents = Gnss.GnsEvents.newBuilder()
                .setTtffReceivedTimestamp(l).build();
        lDataChanged.onDataChanged(gnsEvents.toString());
    }

    private void logLocationEvent(String event) {
        logEvent("LOCATION_EVENT : \n", event + "\n");
    }

    private void logEvent(String tag, String message) {
        String composedTag = Constants.GNSS_LOGGER + tag;
        Log.d(composedTag, message);

        GpsLoggerActivity.UIComponent component = getComponent();
        if (component != null) {
            component.logTextFragment(tag, message);
        }
    }

    private String locationStatusToString(int status) {
        switch (status) {
            case LocationProvider.AVAILABLE:
                return "AVAILABLE";
            case LocationProvider.OUT_OF_SERVICE:
                return "OUT_OF_SERVICE";
            case LocationProvider.TEMPORARILY_UNAVAILABLE:
                return "TEMPORARILY_UNAVAILABLE";
            default:
                return "<UNKNOWN>";
        }
    }

    private String toStringClock(GnssClock gnssClock) {
        final String format = "   %-4s = %s\n";
        StringBuilder builder = new StringBuilder("GNSS_CLOCK :\n");
        DecimalFormat numberFormat = new DecimalFormat("#0.000");

        if (gnssClock.hasLeapSecond()) {
            builder.append(String.format(format, "CLOCK_LEAP_SECOND : ", gnssClock.getLeapSecond()) + "\n");
        }

        builder.append(String.format(format, "CLOCK_TIME_NANOS : ", gnssClock.getTimeNanos()) + "\n");
        if (gnssClock.hasTimeUncertaintyNanos()) {
            builder.append(
                    String.format(format, "CLOCK_UNCERTAINITY_NANOS : ", gnssClock.getTimeUncertaintyNanos())+ "\n");
        }

        if (gnssClock.hasFullBiasNanos()) {
            builder.append(String.format(format, "CLOCK_FULL_BIAS_NANOS : ", gnssClock.getFullBiasNanos()) + "\n");
        }

        if (gnssClock.hasBiasNanos()) {
            builder.append(String.format(format, "CLOCK_BIAS_NANOS : ", gnssClock.getBiasNanos()) + "\n");
        }
        if (gnssClock.hasBiasUncertaintyNanos()) {
            builder.append(
                    String.format(
                            format,
                            "BIAS_UNCERTAINITY_NANOS :",
                            numberFormat.format(gnssClock.getBiasUncertaintyNanos())) + "\n");
        }

        if (gnssClock.hasDriftNanosPerSecond()) {
            builder.append(
                    String.format(
                            format,
                            "DRIFT_NANOS_PER_SECOND : ",
                            numberFormat.format(gnssClock.getDriftNanosPerSecond())) + "\n");
        }

        if (gnssClock.hasDriftUncertaintyNanosPerSecond()) {
            builder.append(
                    String.format(
                            format,
                            "DRIFT_UNCERTAINITY_NANOS : ",
                            numberFormat.format(gnssClock.getDriftUncertaintyNanosPerSecond())) + "\n");
        }

        builder.append(
                String.format(
                        format,
                        "HARDWARE_CLOCK_DISCONTINUITY_COUNT : ",
                        gnssClock.getHardwareClockDiscontinuityCount()) + "\n");

        gnsClock = GnsClock.newBuilder()
                .setClockLeapSecond(gnssClock.getLeapSecond())
                .setClockTimeNanos(gnssClock.getTimeNanos())
                .setClockUncertainity(gnssClock.getTimeUncertaintyNanos())
                .setHarwareClockCount(gnssClock.getHardwareClockDiscontinuityCount())
                .setBiasNanos(gnssClock.getBiasNanos())
                .setBiasUncertainity(gnssClock.getBiasUncertaintyNanos())
                .setDriftNanos(gnssClock.getDriftNanosPerSecond())
                .setDriftUncertainity(gnssClock.getDriftUncertaintyNanosPerSecond())
                .setFullBiasNanos(gnssClock.getFullBiasNanos()).build();
        lDataChanged.onDataChanged(gnssClock.toString());


        return builder.toString();
    }

//    private String toStringMeasurement(GnssMeasurement measurement) {
//        final String format = "   %-4s = %s\n";
//        StringBuilder builder = new StringBuilder("GnssMeasurement:\n");
//        DecimalFormat numberFormat = new DecimalFormat("#0.000");
//        DecimalFormat numberFormat1 = new DecimalFormat("#0.000E00");
//        builder.append(String.format(format, "Svid", measurement.getSvid()));
//        builder.append(String.format(format, "ConstellationType", measurement.getConstellationType()));
//        builder.append(String.format(format, "TimeOffsetNanos", measurement.getTimeOffsetNanos()));
//
//        builder.append(String.format(format, "State", measurement.getState()));
//
//        builder.append(
//                String.format(format, "ReceivedSvTimeNanos", measurement.getReceivedSvTimeNanos()));
//        builder.append(
//                String.format(
//                        format,
//                        "ReceivedSvTimeUncertaintyNanos",
//                        measurement.getReceivedSvTimeUncertaintyNanos()));
//
//        builder.append(String.format(format, "Cn0DbHz", numberFormat.format(measurement.getCn0DbHz())));
//
//        builder.append(
//                String.format(
//                        format,
//                        "PseudorangeRateMetersPerSecond",
//                        numberFormat.format(measurement.getPseudorangeRateMetersPerSecond())));
//        builder.append(
//                String.format(
//                        format,
//                        "PseudorangeRateUncertaintyMetersPerSeconds",
//                        numberFormat.format(measurement.getPseudorangeRateUncertaintyMetersPerSecond())));
//
//        if (measurement.getAccumulatedDeltaRangeState() != 0) {
//            builder.append(
//                    String.format(
//                            format, "AccumulatedDeltaRangeState", measurement.getAccumulatedDeltaRangeState()));
//
//            builder.append(
//                    String.format(
//                            format,
//                            "AccumulatedDeltaRangeMeters",
//                            numberFormat.format(measurement.getAccumulatedDeltaRangeMeters())));
//            builder.append(
//                    String.format(
//                            format,
//                            "AccumulatedDeltaRangeUncertaintyMeters",
//                            numberFormat1.format(measurement.getAccumulatedDeltaRangeUncertaintyMeters())));
//        }
//
//        if (measurement.hasCarrierFrequencyHz()) {
//            builder.append(
//                    String.format(format, "CarrierFrequencyHz", measurement.getCarrierFrequencyHz()));
//        }
//
//        if (measurement.hasCarrierCycles()) {
//            builder.append(String.format(format, "CarrierCycles", measurement.getCarrierCycles()));
//        }
//
//        if (measurement.hasCarrierPhase()) {
//            builder.append(String.format(format, "CarrierPhase", measurement.getCarrierPhase()));
//        }
//
//        if (measurement.hasCarrierPhaseUncertainty()) {
//            builder.append(
//                    String.format(
//                            format, "CarrierPhaseUncertainty", measurement.getCarrierPhaseUncertainty()));
//        }
//
//        builder.append(
//                String.format(format, "MultipathIndicator", measurement.getMultipathIndicator()));
//
//        if (measurement.hasSnrInDb()) {
//            builder.append(String.format(format, "SnrInDb", measurement.getSnrInDb()));
//        }
//
//        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
//            if (measurement.hasAutomaticGainControlLevelDb()) {
//                builder.append(
//                        String.format(format, "AgcDb", measurement.getAutomaticGainControlLevelDb()));
//            }
//            if (measurement.hasCarrierFrequencyHz()) {
//                builder.append(String.format(format, "CarrierFreqHz", measurement.getCarrierFrequencyHz()));
//            }
//        }
//
//        return builder.toString();
//    }

    private void logMeasurementEvent(String event) {
        logEvent("MEASUREMENT : ", event +  "\n");
    }

    private String gnssMeasurementsStatusToString(int status) {
        switch (status) {
            case GnssMeasurementsEvent.Callback.STATUS_NOT_SUPPORTED:
                logEvent("MEASUREMENT : ", "---------GNSS_NOT_SUPPORTED-------" +  "\n");
                return "GNSS_NOT_SUPPORTED";
            case GnssMeasurementsEvent.Callback.STATUS_READY:
                return "READY";
            case GnssMeasurementsEvent.Callback.STATUS_LOCATION_DISABLED:
                logEvent("MEASUREMENT : ", "---------GNSS_LOCATION_DISABLED-------" +  "\n");
                return "GNSS_LOCATION_DISABLED";
            default:
                return "<UKNOWN>";
        }
    }

    private String getGnssNavigationMessageStatus(int status) {
        switch (status) {
            case GnssNavigationMessage.STATUS_UNKNOWN:
                return "STATUS_UNKNOWN";
            case GnssNavigationMessage.STATUS_PARITY_PASSED:
                return "READY";
            case GnssNavigationMessage.STATUS_PARITY_REBUILT:
                return "STATUS_PARITY_REBUILT";
            default:
                return "<UNKNOWN>";
        }
    }

    private void logNavigationMessageEvent(String event) {
        logEvent("NAVIGATION_NESSAGE_EVENT", event + "\n");
    }

    private void logStatusEvent(String event) {
        logEvent("STATUS_EVENT", event  + "\n");
    }

    private String gnssStatusToString(GnssStatus gnssStatus) {

        StringBuilder builder = new StringBuilder("SATELLITE_STATUS | [SATELLITES:\n");
        for (int i = 0; i < gnssStatus.getSatelliteCount(); i++) {
            builder.append("GNSS_STATUS_CONSTELLATION_NAME : ").append(getConstellationName(gnssStatus.getConstellationType(i))).append("\n") ;
            builder.append("GNSS_STATUS_SVID :  ").append(gnssStatus.getSvid(i)).append("\n");
            builder.append("GNSS_STATUS_NOISE_DENSITY : ").append(gnssStatus.getCn0DbHz(i)).append("\n");
            builder.append("GNSS_STATUS_ELEVATION : ").append(gnssStatus.getElevationDegrees(i)).append("\n");
            builder.append("GNSS_STATUS_AZIMUTH : ").append(gnssStatus.getAzimuthDegrees(i)).append("\n");
            builder.append("GNSS_STAUTS_EPEMERIC : ").append(gnssStatus.hasEphemerisData(i)).append("\n");
            builder.append("GNSS_STATUS_ALMANC_DATA : ").append(gnssStatus.hasAlmanacData(i)).append("\n");
            builder.append("GNSS_STATUS_USED_IN_FIX : ").append(gnssStatus.usedInFix(i)).append("\n");

            gnsStatus = Gnss.GnsStatus.newBuilder()
                    .setCarrierToNoiseDensity(gnssStatus.getCn0DbHz(i))
                    .setConstellationName(getConstellationName(gnssStatus.getConstellationType(i)))
                    .setHasAlmanacPresent(gnssStatus.hasAlmanacData(i))
                    .setHasEphemerisPresent(gnssStatus.hasEphemerisData(i))
                    .setCarrierToNoiseDensity(gnssStatus.getCn0DbHz(i))
                    .setSatelliteAzimuth(gnssStatus.getAzimuthDegrees(i))
                    .setSatelliteCount(gnssStatus.getSatelliteCount())
                    .setSatelliteIdNumber(gnssStatus.getSvid(i)).build();

            lDataChanged.onDataChanged(gnsStatus.toString());
        }
        return builder.toString();
    }

    private String getConstellationName(int id) {
        switch (id) {
            case 1:
                return "GPS";
            case 2:
                return "SBAS";
            case 3:
                return "GLONASS";
            case 4:
                return "QZSS";
            case 5:
                return "BEIDOU";
            case 6:
                return "GALILEO";
            default:
                return "UNKNOWN";
        }
    }
    private void logNmeaEvent(String event) {
        logEvent("NMEA_EVENT", event);
    }



}

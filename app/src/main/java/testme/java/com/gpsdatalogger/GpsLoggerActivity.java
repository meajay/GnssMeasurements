package testme.java.com.gpsdatalogger;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.widget.ScrollView;
import android.widget.Scroller;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.ActivityRecognition;

public class GpsLoggerActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, View.OnClickListener , Logger.LoggerDataChanged {

    private GoogleApiClient mGoogleApiClient;
    private boolean isPermissionEnabled;
    private Logger logger;
    public TextView logText, start, end;
    public ScrollView scrollView;
    private boolean isStartPressed = false, isEndPressed = false , isDataLogged = false , isDataLoggedStarted = false ;
    private GnssContainer gnssContainer;
    private final UIComponent uiComponent = new UIComponent();
    private  SaveFile fileLog = null ;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gps_logger);
        logText = findViewById(R.id.agl_log);
        start = findViewById(R.id.agl_start);
        end = findViewById(R.id.agl_end);
        scrollView = findViewById(R.id.agl_scroll);
        start.setOnClickListener(this);
        end.setOnClickListener(this);

        end.setBackground(getDrawable(R.drawable.btn_fade_background));

    }

    @Override
    protected void onResume() {
        super.onResume();

        isPermissionEnabled = hasPermissions(this);
        if (isPermissionEnabled) {
            setUpLogger();
        } else {
            requestPermissions(Constants.REQUIRED_PERMISSIONS, Constants.LOCATION_REQUEST_ID);
        }


    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onDestroy() {
        if (gnssContainer != null) {
            gnssContainer.unregisterAll();
        }
        super.onDestroy();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.agl_start:
                if (isPermissionEnabled) {
                    if (isStartPressed) {
                        Toast.makeText(this, getString(R.string.already_logging), Toast.LENGTH_SHORT).show();

                    } else {
                        isStartPressed = true;
                        isEndPressed = false;
                        isDataLoggedStarted = true ;
                        if (gnssContainer != null) {
                            gnssContainer.registerAll();
                            if(fileLog!=null){
                                fileLog.saveFileLog();
                            }
                        }
                        start.setBackground(getDrawable(R.drawable.btn_fade_background));
                        end.setBackground(getDrawable(R.drawable.btn_background));
                        Toast.makeText(this, getString(R.string.logging_start), Toast.LENGTH_SHORT).show();
                    }
                } else
                    Toast.makeText(this, getString(R.string.allow_permissions), Toast.LENGTH_SHORT).show();
                break;

            case R.id.agl_end:
                if (isPermissionEnabled) {
                    if (isEndPressed) {
                        Toast.makeText(this, getString(R.string.first_start), Toast.LENGTH_SHORT).show();
                    } else {
                        isEndPressed = true;
                        isStartPressed = false;
                        isDataLogged = true ;
                        if (gnssContainer != null) {
                            gnssContainer.unregisterAll();
                        }
                        if(fileLog!=null){
                            fileLog.closeStreams();
                        }
                        start.setBackground(getDrawable(R.drawable.btn_background));
                        end.setBackground(getDrawable(R.drawable.btn_fade_background));
                        scrollView.fullScroll(View.FOCUS_UP);
                        Toast.makeText(this, getString(R.string.logging_end), Toast.LENGTH_SHORT).show();
                    }
                } else
                    Toast.makeText(this, getString(R.string.allow_permissions), Toast.LENGTH_SHORT).show();
                break;


        }

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.i(Constants.LOG_TAG, "Connection failed: ErrorCode = " + connectionResult.getErrorCode());
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.i(Constants.LOG_TAG, "Connected to GoogleApiClient");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.share_setting, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_share:
                if (isDataLoggedStarted) {
                    if(isDataLogged)
                    fileLog.send();
                    else
                        Toast.makeText(this,"FINISH LOGGING BEFORE SENDING DATA",Toast.LENGTH_SHORT).show() ;
                } else {
                    Toast.makeText(GpsLoggerActivity.this, "NOTHING TO SHARE , PLEASE LOG SOME DATA", Toast.LENGTH_SHORT).show();
                }
                return true;
        }
        return true;
    }


    @Override
    public void onConnectionSuspended(int i) {
        Log.i(Constants.LOG_TAG, "Connected Suspended");
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        boolean flag = false;
        for (int i = 0; i < grantResults.length; i++) {
            if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                flag = true;
            }
        }
        if (flag) {
            setUpLogger();
        } else {
            Toast.makeText(this, getString(R.string.permission_disable), Toast.LENGTH_SHORT).show();
            logText.setText(R.string.permission_disable);
        }
    }

    private synchronized void buildGoogleApiClient() {
        if(mGoogleApiClient == null) {
            mGoogleApiClient =
                    new GoogleApiClient.Builder(this)
                            .enableAutoManage(this, this)
                            .addConnectionCallbacks(this)
                            .addOnConnectionFailedListener(this)
                            .addApi(ActivityRecognition.API)
                            .build();
        }
    }

    private boolean hasPermissions(Activity activity) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            // Permissions granted at install time.
            return true;
        }
        for (String p : Constants.REQUIRED_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(activity, p) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }


    private void  setUpLogger() {
        buildGoogleApiClient();
        Logger logger = new Logger(this);
        if(fileLog == null)
         fileLog = new SaveFile(GpsLoggerActivity.this) ;
        if(gnssContainer == null)
        gnssContainer = new GnssContainer(this, logger);
        if (logger != null && fileLog!=null) {
            logger.setComponent(uiComponent);
            fileLog.setComponent(uiComponent);
        }
    }


    public class UIComponent {

        private static final int MAX_LENGTH = 42000;
        private static final int LOWER_THRESHOLD = (int) (MAX_LENGTH * 0.5);

        public synchronized void logTextFragment(final String tag, final String text) {
            final SpannableStringBuilder builder = new SpannableStringBuilder();
            builder.append(tag).append(" | ").append(text).append("\n");
//            builder.setSpan(
//                    new ForegroundColorSpan(color),
//                    0 /* start */,
//                    builder.length(),
//                    SpannableStringBuilder.SPAN_INCLUSIVE_EXCLUSIVE);

            Activity activity = GpsLoggerActivity.this;
            if (activity == null) {
                return;
            }
            activity.runOnUiThread(
                    new Runnable() {
                        @Override
                        public void run() {
                            logText.append(builder);
                            SharedPreferences sharedPreferences = PreferenceManager.
                                    getDefaultSharedPreferences(GpsLoggerActivity.this);
                            Editable editable = logText.getEditableText();
                            int length = editable.length();
                            if (length > MAX_LENGTH) {
                                editable.delete(0, length - LOWER_THRESHOLD);
                            }
                            scrollView.fullScroll(View.FOCUS_DOWN);
                        }
                    });
        }

        public void startActivity(Intent intent) {
            GpsLoggerActivity.this.startActivity(intent);
        }
    }

    @Override
    public void onDataChanged(String s) {
        if(fileLog!=null){
            fileLog.writeData(s);
            }
        }
}

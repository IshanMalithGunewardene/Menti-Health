package com.s23010494.mentihealth;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import androidx.fragment.app.FragmentActivity;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class StatsActivity extends FragmentActivity
        implements OnMapReadyCallback, SensorEventListener {

    private SensorManager sensorManager;
    private Sensor stepCounterSensor;
    private TextView tvStepCount;
    private TextView tvProgressText;
    private HorizontalStepProgressView horizontalProgress;
    private DBHelper dbHelper;
    private String userEmail;
    private String currentDate;
    private int dailyStepCount = 0;
    private int baselineSteps = 0;
    private boolean isFirstReading = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stats);

        // Initialize database and get user email
        dbHelper = new DBHelper(this);
        userEmail = getIntent().getStringExtra("EMAIL");
        if (userEmail == null) {
            userEmail = "test@example.com"; // Default for testing
        }

        // Get current date in YYYY-MM-DD format
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        currentDate = sdf.format(new Date());

        tvStepCount = findViewById(R.id.tv_step_count);
        tvProgressText = findViewById(R.id.tv_progress_text);
        horizontalProgress = findViewById(R.id.horizontal_progress);

        // Initialize step counter sensor
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        if (sensorManager != null) {
            stepCounterSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
        }

        // Setup debug buttons (Remove in production)
        setupDebugButtons();
        
        // Load existing daily step count from database
        loadDailyStepCount();

        // Google Map
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        // Polgolla Walking Lane, Kandy
        LatLng polgolla = new LatLng(7.3070, 80.6480);
        googleMap.addMarker(new MarkerOptions()
                .position(polgolla)
                .title("Polgolla Walking Lane"));
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(polgolla, 16));
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (stepCounterSensor != null) {
            sensorManager.registerListener(this, stepCounterSensor, SensorManager.SENSOR_DELAY_NORMAL);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (sensorManager != null) {
            sensorManager.unregisterListener(this);
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_STEP_COUNTER) {
            int totalStepsSinceReboot = (int) event.values[0];
            
            if (isFirstReading) {
                // First reading - establish baseline for today
                baselineSteps = dbHelper.getBaselineSteps(userEmail, currentDate);
                if (baselineSteps == 0) {
                    // No baseline set for today - set it now
                    baselineSteps = totalStepsSinceReboot;
                    dbHelper.saveDailySteps(userEmail, currentDate, totalStepsSinceReboot, baselineSteps);
                }
                isFirstReading = false;
            }
            
            // Calculate today's steps
            dailyStepCount = totalStepsSinceReboot - baselineSteps;
            if (dailyStepCount < 0) {
                // Handle device reboot case - reset baseline
                baselineSteps = totalStepsSinceReboot;
                dailyStepCount = 0;
            }
            
            // Update UI with animation
            updateStepCountDisplay(dailyStepCount);
            
            // Save to database
            dbHelper.saveDailySteps(userEmail, currentDate, totalStepsSinceReboot, baselineSteps);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Not used
    }
    
    private void loadDailyStepCount() {
        // Load existing daily step count from database
        dailyStepCount = dbHelper.getDailySteps(userEmail, currentDate);
        updateStepCountDisplay(dailyStepCount);
    }
    
    private void updateStepCountDisplay(int stepCount) {
        // Update text display
        tvStepCount.setText(String.valueOf(stepCount));
        
        // Update progress text (show current steps out of goal)
        if (tvProgressText != null) {
            int goal = horizontalProgress != null ? horizontalProgress.getMaxStepsGoal() : 10000;
            tvProgressText.setText(String.format("%s / %s steps", 
                String.format("%,d", stepCount),
                String.format("%,d", goal)));
        }
        
        // Update horizontal progress with animation
        if (horizontalProgress != null) {
            horizontalProgress.setStepCount(stepCount);
        }
    }
    
    // Debug methods for testing (Remove in production)
    private void setupDebugButtons() {
        Button btnAdd100 = findViewById(R.id.btn_add_100_steps);
        Button btnAdd500 = findViewById(R.id.btn_add_500_steps);
        Button btnReset = findViewById(R.id.btn_reset_steps);
        
        btnAdd100.setOnClickListener(v -> {
            dailyStepCount += 100;
            updateStepCountDisplay(dailyStepCount);
            // Update database
            dbHelper.saveDailySteps(userEmail, currentDate, baselineSteps + dailyStepCount, baselineSteps);
        });
        
        btnAdd500.setOnClickListener(v -> {
            dailyStepCount += 500;
            updateStepCountDisplay(dailyStepCount);
            // Update database
            dbHelper.saveDailySteps(userEmail, currentDate, baselineSteps + dailyStepCount, baselineSteps);
        });
        
        btnReset.setOnClickListener(v -> {
            dailyStepCount = 0;
            updateStepCountDisplay(dailyStepCount);
            // Update database
            dbHelper.saveDailySteps(userEmail, currentDate, baselineSteps, baselineSteps);
        });
    }
}


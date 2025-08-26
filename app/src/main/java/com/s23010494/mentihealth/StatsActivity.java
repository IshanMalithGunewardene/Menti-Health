package com.s23010494.mentihealth;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import androidx.fragment.app.FragmentActivity;
import java.util.Map;
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
    private TextView tvGoalStatus;
    private SemicircularStepProgressView semicircularProgress;
    private MoodLineChartView moodLineChart;
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
        tvGoalStatus = findViewById(R.id.tv_goal_status);
        semicircularProgress = findViewById(R.id.semicircular_progress);
        moodLineChart = findViewById(R.id.mood_line_chart);

        // Initialize step counter sensor
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        if (sensorManager != null) {
            stepCounterSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
        }

        // Setup debug buttons (Remove in production)
        setupDebugButtons();
        
        // Setup mood line chart buttons
        setupMoodLineChartButtons();
        
        // Load existing daily step count from database
        loadDailyStepCount();
        
        // Load mood line chart data
        loadMoodLineChartData();

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
        tvStepCount.setText(String.format("%,d", stepCount));
        
        // Update progress text (show current steps out of goal)
        if (tvProgressText != null) {
            int goal = semicircularProgress != null ? semicircularProgress.getMaxStepsGoal() : 10000;
            tvProgressText.setText(String.format("%s / %s steps", 
                String.format("%,d", stepCount),
                String.format("%,d", goal)));
        }
        
        // Update semicircular progress with animation
        if (semicircularProgress != null) {
            semicircularProgress.setStepCount(stepCount);
        }
        
        // Update goal status message
        updateGoalStatusMessage(stepCount);
    }
    
    private void updateGoalStatusMessage(int stepCount) {
        if (tvGoalStatus == null) return;
        
        int goal = semicircularProgress != null ? semicircularProgress.getMaxStepsGoal() : 10000;
        float percentage = (float) stepCount / goal * 100;
        
        if (stepCount == 0) {
            tvGoalStatus.setVisibility(TextView.GONE);
        } else if (stepCount >= goal) {
            tvGoalStatus.setText("🎉 Goal Achieved! Amazing! 🎉");
            tvGoalStatus.setTextColor(0xFF31E3BD); // Brand color
            tvGoalStatus.setVisibility(TextView.VISIBLE);
        } else if (percentage >= 75) {
            tvGoalStatus.setText("🔥 Almost there! Keep going! 🔥");
            tvGoalStatus.setTextColor(0xFF31E3BD); // Brand color
            tvGoalStatus.setVisibility(TextView.VISIBLE);
        } else if (percentage >= 50) {
            tvGoalStatus.setText("💪 Halfway there! Great job! 💪");
            tvGoalStatus.setTextColor(0xFF31E3BD); // Brand color
            tvGoalStatus.setVisibility(TextView.VISIBLE);
        } else if (percentage >= 25) {
            tvGoalStatus.setText("🚀 Good progress! Keep it up! 🚀");
            tvGoalStatus.setTextColor(0xFF31E3BD); // Brand color
            tvGoalStatus.setVisibility(TextView.VISIBLE);
        } else {
            tvGoalStatus.setText("🏃 Let's get moving! You got this! 🏃");
            tvGoalStatus.setTextColor(0xFF31E3BD); // Brand color
            tvGoalStatus.setVisibility(TextView.VISIBLE);
        }
    }
    
    // Debug methods for testing (Remove in production)
    private void setupDebugButtons() {
        Button btnAdd100 = findViewById(R.id.btn_add_100_steps);
        Button btnAdd500 = findViewById(R.id.btn_add_500_steps);
        Button btnReset = findViewById(R.id.btn_reset_steps);
        Button btnGenerateSample = findViewById(R.id.btn_generate_sample_data);
        Button btnClearSample = findViewById(R.id.btn_clear_sample_data);
        
        // Step counter debug buttons
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
        
        // Sample data generation buttons
        btnGenerateSample.setOnClickListener(v -> {
            generateSampleMoodData();
        });
        
        btnClearSample.setOnClickListener(v -> {
            clearSampleMoodData();
        });
    }
    
    private void generateSampleMoodData() {
        // Show progress immediately
        showSampleDataDialog("Generating Data...", "Please wait while we create sample mood data for testing.");
        
        new Thread(() -> {
            try {
                SampleDataGenerator generator = new SampleDataGenerator(this);
                boolean success = generator.generateSampleMoodData(userEmail);
                
                runOnUiThread(() -> {
                    if (success) {
                        // Refresh mood charts for the current user
                        loadMoodLineChartData();
                        
                        showSampleDataDialog("Sample Data Generated!", 
                            "Generated 30 days of sample mood and journal entries for your account!\n\n" +
                            "The data includes realistic mood patterns and journal entries that you can see in your calendar and journal pages.");
                    } else {
                        showSampleDataDialog("Error", "Failed to generate sample data. Make sure you're logged in with a valid account.");
                    }
                });
            } catch (Exception e) {
                runOnUiThread(() -> {
                    showSampleDataDialog("Error", "An error occurred while generating sample data: " + e.getMessage());
                });
            }
        }).start();
    }
    
    private void clearSampleMoodData() {
        new Thread(() -> {
            try {
                DBHelper dbHelper = new DBHelper(this);
                boolean success = dbHelper.deleteAllJournalEntries(userEmail);
                
                runOnUiThread(() -> {
                    if (success) {
                        // Refresh mood charts for the current user
                        loadMoodLineChartData();
                        
                        showSampleDataDialog("Data Cleared!", 
                            "All mood and journal entries for your account have been removed.");
                    } else {
                        showSampleDataDialog("Error", "Failed to clear data. Check logs for details.");
                    }
                });
            } catch (Exception e) {
                runOnUiThread(() -> {
                    showSampleDataDialog("Error", "An error occurred while clearing data: " + e.getMessage());
                });
            }
        }).start();
    }
    
    private void showSampleDataDialog(String title, String message) {
        new android.app.AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("OK", null)
                .show();
    }
    
    
    // Mood line chart functionality
    private void setupMoodLineChartButtons() {
        Button btnRefreshMoodChart = findViewById(R.id.btn_refresh_mood_chart);
        Button btnAnimateMoodChart = findViewById(R.id.btn_animate_mood_chart);
        
        btnRefreshMoodChart.setOnClickListener(v -> {
            loadMoodLineChartData();
        });
        
        btnAnimateMoodChart.setOnClickListener(v -> {
            if (moodLineChart != null) {
                moodLineChart.startAnimation();
            }
        });
    }
    
    private void loadMoodLineChartData() {
        new Thread(() -> {
            // Get mood data for trend analysis from database
            Map<String, String> moodData = dbHelper.getMoodsForTrendAnalysis(userEmail);
            
            runOnUiThread(() -> {
                if (moodLineChart != null) {
                    // Update the line chart with mood data
                    moodLineChart.setMoodData(moodData);
                    
                    // Auto-animate the lines
                    if (moodLineChart.hasData()) {
                        moodLineChart.startAnimation();
                    }
                }
            });
        }).start();
    }
}


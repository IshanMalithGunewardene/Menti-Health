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
    private MoodTrendGraphView moodTrendGraph;
    private MoodBarChartView moodBarChart;
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
        moodTrendGraph = findViewById(R.id.mood_trend_graph);
        moodBarChart = findViewById(R.id.mood_bar_chart);

        // Initialize step counter sensor
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        if (sensorManager != null) {
            stepCounterSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
        }

        // Setup debug buttons (Remove in production)
        setupDebugButtons();
        
        // Setup mood graph buttons
        setupMoodGraphButtons();
        
        // Setup mood bar chart buttons
        setupMoodBarChartButtons();
        
        // Load existing daily step count from database
        loadDailyStepCount();
        
        // Load mood trend data
        loadMoodTrendData();
        
        // Load mood bar chart data
        loadMoodBarChartData();

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
        new Thread(() -> {
            SampleDataGenerator generator = new SampleDataGenerator(this);
            boolean success = generator.generateSampleMoodData();
            
            runOnUiThread(() -> {
                if (success) {
                    // Refresh mood charts if we're on the sample user's account
                    if (userEmail.equals(generator.getSampleEmail())) {
                        loadMoodTrendData();
                        loadMoodBarChartData();
                    }
                    
                    showSampleDataDialog("Sample Data Generated!", 
                        "Generated mood data for " + generator.getSampleEmail() + " (" + generator.getSampleName() + ")\n\n" +
                        "Login with:\nEmail: " + generator.getSampleEmail() + "\nPassword: password123\n\n" +
                        "This account now has 30 days of sample mood entries with realistic patterns!");
                } else {
                    showSampleDataDialog("Error", "Failed to generate sample data. Check logs for details.");
                }
            });
        }).start();
    }
    
    private void clearSampleMoodData() {
        new Thread(() -> {
            SampleDataGenerator generator = new SampleDataGenerator(this);
            boolean success = generator.clearSampleUserData();
            
            runOnUiThread(() -> {
                if (success) {
                    // Refresh mood charts if we're on the sample user's account
                    if (userEmail.equals(generator.getSampleEmail())) {
                        loadMoodTrendData();
                        loadMoodBarChartData();
                    }
                    
                    showSampleDataDialog("Sample Data Cleared!", 
                        "All mood data for " + generator.getSampleEmail() + " has been removed.");
                } else {
                    showSampleDataDialog("Error", "Failed to clear sample data. Check logs for details.");
                }
            });
        }).start();
    }
    
    private void showSampleDataDialog(String title, String message) {
        new android.app.AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("OK", null)
                .show();
    }
    
    // Mood graph functionality
    private void setupMoodGraphButtons() {
        Button btnRefreshGraph = findViewById(R.id.btn_refresh_graph);
        Button btnAnimateGraph = findViewById(R.id.btn_animate_graph);
        
        btnRefreshGraph.setOnClickListener(v -> {
            loadMoodTrendData();
        });
        
        btnAnimateGraph.setOnClickListener(v -> {
            if (moodTrendGraph != null) {
                moodTrendGraph.startAnimation();
            }
        });
    }
    
    private void loadMoodTrendData() {
        new Thread(() -> {
            Map<String, String> moodData = dbHelper.getMoodsForTrendAnalysis(userEmail);
            
            runOnUiThread(() -> {
                if (moodTrendGraph != null) {
                    moodTrendGraph.setMoodData(moodData);
                    
                    // Auto-animate if there's data
                    if (moodTrendGraph.hasData()) {
                        moodTrendGraph.startAnimation();
                    }
                }
            });
        }).start();
    }
    
    // Mood bar chart functionality
    private void setupMoodBarChartButtons() {
        Button btnRefreshMoodChart = findViewById(R.id.btn_refresh_mood_chart);
        Button btnAnimateMoodChart = findViewById(R.id.btn_animate_mood_chart);
        
        btnRefreshMoodChart.setOnClickListener(v -> {
            loadMoodBarChartData();
        });
        
        btnAnimateMoodChart.setOnClickListener(v -> {
            if (moodBarChart != null) {
                moodBarChart.startAnimation();
            }
        });
    }
    
    private void loadMoodBarChartData() {
        new Thread(() -> {
            // Get mood statistics from database
            Map<String, Integer> moodCounts = dbHelper.getMoodStatistics(userEmail);
            
            runOnUiThread(() -> {
                if (moodBarChart != null) {
                    // Update the bar chart with mood data
                    moodBarChart.setMoodData(moodCounts);
                    
                    // Auto-animate the bars
                    if (moodBarChart.hasData()) {
                        moodBarChart.startAnimation();
                    }
                }
            });
        }).start();
    }
}


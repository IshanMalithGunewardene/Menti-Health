package com.s23010494.mentihealth;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class MoodLineChartView extends View {
    private static final int ANIMATION_DURATION = 2000; // 2 seconds
    private static final int DAYS_TO_SHOW = 30;
    
    // Colors for different moods (updated for new mood system)
    private static final int COLOR_EXCELLENT = Color.parseColor("#31E3BD"); // Brand color - bright green
    private static final int COLOR_GOOD = Color.parseColor("#FF9800");      // Orange
    private static final int COLOR_MEH = Color.parseColor("#2196F3");       // Blue
    private static final int COLOR_NOT_GREAT = Color.parseColor("#9C27B0"); // Purple
    private static final int COLOR_TERRIBLE = Color.parseColor("#F44336");  // Red
    private static final int BACKGROUND_COLOR = Color.parseColor("#232325");
    private static final int TEXT_COLOR = Color.parseColor("#FFFFFF");
    private static final int GRID_COLOR = Color.parseColor("#404040");
    private static final int LABEL_COLOR = Color.parseColor("#AAAAAA");
    
    private Paint linePaint;
    private Paint pointPaint;
    private Paint textPaint;
    private Paint labelPaint;
    private Paint backgroundPaint;
    private Paint gridPaint;
    
    private Map<String, Map<String, Integer>> moodDataByDate;
    private List<String> dateLabels;
    private Map<String, Path> moodPaths;
    private Map<String, Path> animatedPaths;
    private float animationProgress = 1f;
    
    // Chart dimensions
    private float chartLeft = 80f;
    private float chartRight;
    private float chartTop = 80f;
    private float chartBottom;
    private float chartWidth;
    private float chartHeight;
    
    public MoodLineChartView(Context context) {
        super(context);
        init();
    }
    
    public MoodLineChartView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }
    
    public MoodLineChartView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }
    
    private void init() {
        // Line paint
        linePaint = new Paint();
        linePaint.setStyle(Paint.Style.STROKE);
        linePaint.setStrokeWidth(4f);
        linePaint.setAntiAlias(true);
        linePaint.setStrokeCap(Paint.Cap.ROUND);
        linePaint.setStrokeJoin(Paint.Join.ROUND);
        
        // Point paint
        pointPaint = new Paint();
        pointPaint.setStyle(Paint.Style.FILL);
        pointPaint.setAntiAlias(true);
        
        // Text paint
        textPaint = new Paint();
        textPaint.setColor(TEXT_COLOR);
        textPaint.setTextSize(28f);
        textPaint.setAntiAlias(true);
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setFakeBoldText(true);
        
        // Label paint
        labelPaint = new Paint();
        labelPaint.setColor(LABEL_COLOR);
        labelPaint.setTextSize(20f);
        labelPaint.setAntiAlias(true);
        labelPaint.setTextAlign(Paint.Align.CENTER);
        
        // Background paint
        backgroundPaint = new Paint();
        backgroundPaint.setColor(BACKGROUND_COLOR);
        backgroundPaint.setStyle(Paint.Style.FILL);
        
        // Grid paint
        gridPaint = new Paint();
        gridPaint.setColor(GRID_COLOR);
        gridPaint.setStrokeWidth(1f);
        gridPaint.setAntiAlias(true);
        
        moodDataByDate = new HashMap<>();
        dateLabels = new ArrayList<>();
        moodPaths = new HashMap<>();
        animatedPaths = new HashMap<>();
        
        initializeDateLabels();
        initializeEmptyData();
    }
    
    private void initializeDateLabels() {
        dateLabels.clear();
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        SimpleDateFormat labelFormat = new SimpleDateFormat("MM/dd", Locale.getDefault());
        
        // Generate last 30 days
        for (int i = DAYS_TO_SHOW - 1; i >= 0; i--) {
            calendar.setTimeInMillis(System.currentTimeMillis());
            calendar.add(Calendar.DAY_OF_MONTH, -i);
            String dateKey = dateFormat.format(calendar.getTime());
            dateLabels.add(dateKey);
        }
    }
    
    private void initializeEmptyData() {
        moodDataByDate.clear();
        for (String date : dateLabels) {
            Map<String, Integer> dailyMoods = new HashMap<>();
            dailyMoods.put("Excellent!", 0);
            dailyMoods.put("Good!", 0);
            dailyMoods.put("Meh", 0);
            dailyMoods.put("Not great", 0);
            dailyMoods.put("Terrible", 0);
            moodDataByDate.put(date, dailyMoods);
        }
    }
    
    public void setMoodData(Map<String, String> moodsByDate) {
        // Clear existing data and create single trend line data structure
        moodDataByDate.clear();
        
        if (moodsByDate != null) {
            // Convert mood entries to single mood score per day (take first entry per day)
            for (Map.Entry<String, String> entry : moodsByDate.entrySet()) {
                String date = entry.getKey();
                String mood = entry.getValue();
                
                if (!moodDataByDate.containsKey(date) && mood != null) {
                    // Only take first mood entry per day for trend line
                    int moodScore = getMoodScore(mood);
                    Map<String, Integer> dailyData = new HashMap<>();
                    dailyData.put("moodScore", moodScore);
                    moodDataByDate.put(date, dailyData);
                }
            }
        }
        
        calculateSingleTrendPath();
        invalidate();
    }
    
    private int getMoodScore(String mood) {
        switch (mood) {
            case "Excellent!": return 5;
            case "Good!": return 4;
            case "Meh": return 3;
            case "Not great": return 2;
            case "Terrible": return 1;
            // Old mood values for backward compatibility
            case "excited": return 5;
            case "happy": return 4;
            case "calm": return 3;
            case "sad": return 2;
            case "anxious": return 1;
            default: return 3;
        }
    }
    
    private String mapMoodValue(String mood) {
        switch (mood) {
            // New mood values
            case "Excellent!": return "Excellent!";
            case "Good!": return "Good!";
            case "Meh": return "Meh";
            case "Not great": return "Not great";
            case "Terrible": return "Terrible";
            // Old mood values for backward compatibility
            case "happy": return "Excellent!";
            case "excited": return "Good!";
            case "calm": return "Meh";
            case "sad": return "Not great";
            case "anxious": return "Terrible";
            default: return "Meh";
        }
    }
    
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        
        chartRight = w - 40f;
        chartBottom = h - 60f;
        chartWidth = chartRight - chartLeft;
        chartHeight = chartBottom - chartTop;
        
        calculateSingleTrendPath();
    }
    
    private void calculateSingleTrendPath() {
        if (chartWidth <= 0 || chartHeight <= 0) return;
        
        moodPaths.clear();
        animatedPaths.clear();
        
        // Create single trend line path
        Path trendPath = new Path();
        List<Float> xPoints = new ArrayList<>();
        List<Float> yPoints = new ArrayList<>();
        
        // Collect data points for trend line
        for (int i = 0; i < dateLabels.size(); i++) {
            String date = dateLabels.get(i);
            Map<String, Integer> dailyData = moodDataByDate.get(date);
            int moodScore = dailyData != null ? dailyData.getOrDefault("moodScore", 3) : 3;
            
            float x = chartLeft + (i * chartWidth / (dateLabels.size() - 1));
            // Map mood score (1-5) to chart coordinates
            float y = chartBottom - ((moodScore - 1) * chartHeight / 4f);
            
            xPoints.add(x);
            yPoints.add(y);
        }
        
        // Create smooth curve using cubic Bezier curves
        if (xPoints.size() > 0) {
            trendPath.moveTo(xPoints.get(0), yPoints.get(0));
            
            if (xPoints.size() > 1) {
                for (int i = 1; i < xPoints.size(); i++) {
                    if (i == 1) {
                        // First curve segment
                        float cp1x = xPoints.get(0) + (xPoints.get(1) - xPoints.get(0)) / 3;
                        float cp1y = yPoints.get(0);
                        float cp2x = xPoints.get(1) - (xPoints.get(1) - xPoints.get(0)) / 3;
                        float cp2y = yPoints.get(1);
                        trendPath.cubicTo(cp1x, cp1y, cp2x, cp2y, xPoints.get(1), yPoints.get(1));
                    } else {
                        // Calculate control points for smooth curves
                        float prevX = xPoints.get(i - 1);
                        float prevY = yPoints.get(i - 1);
                        float currX = xPoints.get(i);
                        float currY = yPoints.get(i);
                        
                        float cp1x = prevX + (currX - prevX) / 3;
                        float cp1y = prevY;
                        float cp2x = currX - (currX - prevX) / 3;
                        float cp2y = currY;
                        
                        // Add smoothing based on neighboring points
                        if (i > 1) {
                            float prevPrevY = yPoints.get(i - 2);
                            cp1y = prevY + (currY - prevPrevY) / 6;
                        }
                        if (i < xPoints.size() - 1) {
                            float nextY = yPoints.get(i + 1);
                            cp2y = currY + (prevY - nextY) / 6;
                        }
                        
                        trendPath.cubicTo(cp1x, cp1y, cp2x, cp2y, currX, currY);
                    }
                }
            }
        }
        
        // Store single trend path
        moodPaths.put("trendLine", trendPath);
        animatedPaths.put("trendLine", new Path());
    }
    
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        
        // Draw background
        canvas.drawRoundRect(0, 0, getWidth(), getHeight(), 16f, 16f, backgroundPaint);
        
        // Draw title
        canvas.drawText("Mood Trends (30 Days)", getWidth() / 2f, 35f, textPaint);
        
        if (hasData()) {
            drawGrid(canvas);
            drawMoodLines(canvas);
            drawMoodScaleLabels(canvas);
            drawDateLabels(canvas);
        } else {
            drawEmptyState(canvas);
        }
    }
    
    private void drawGrid(Canvas canvas) {
        // Horizontal grid lines
        for (int i = 0; i <= 4; i++) {
            float y = chartTop + (i * chartHeight / 4);
            canvas.drawLine(chartLeft, y, chartRight, y, gridPaint);
        }
        
        // Vertical grid lines (weekly)
        for (int i = 0; i <= 4; i++) {
            float x = chartLeft + (i * chartWidth / 4);
            canvas.drawLine(x, chartTop, x, chartBottom, gridPaint);
        }
    }
    
    private void drawMoodLines(Canvas canvas) {
        // Draw single trend line
        Path fullPath = moodPaths.get("trendLine");
        Path animPath = animatedPaths.get("trendLine");
        
        if (fullPath != null && animPath != null) {
            // Create animated path
            animPath.rewind();
            PathMeasure pathMeasure = new PathMeasure(fullPath, false);
            float pathLength = pathMeasure.getLength();
            float animatedLength = pathLength * animationProgress;
            
            if (animatedLength > 0) {
                pathMeasure.getSegment(0, animatedLength, animPath, true);
                
                // Draw line with brand color
                linePaint.setColor(COLOR_EXCELLENT); // Use brand color for trend line
                linePaint.setStrokeWidth(6f); // Make line thicker
                canvas.drawPath(animPath, linePaint);
                
                // Draw data points
                drawSingleTrendPoints(canvas);
            }
        }
    }
    
    private void drawSingleTrendPoints(Canvas canvas) {
        pointPaint.setColor(COLOR_EXCELLENT); // Use brand color for points
        
        for (int i = 0; i < dateLabels.size(); i++) {
            String date = dateLabels.get(i);
            Map<String, Integer> dailyData = moodDataByDate.get(date);
            int moodScore = dailyData != null ? dailyData.getOrDefault("moodScore", 3) : 3;
            
            // Only draw points where we have actual data
            if (dailyData != null) {
                float x = chartLeft + (i * chartWidth / (dateLabels.size() - 1));
                float y = chartBottom - ((moodScore - 1) * chartHeight / 4f);
                
                // Only draw point if it's within animated range
                float totalDistance = i * chartWidth / (dateLabels.size() - 1);
                float animatedDistance = chartWidth * animationProgress;
                
                if (totalDistance <= animatedDistance) {
                    canvas.drawCircle(x, y, 8f, pointPaint);
                    
                    // Draw inner white circle for better visibility
                    Paint innerPaint = new Paint(pointPaint);
                    innerPaint.setColor(Color.WHITE);
                    canvas.drawCircle(x, y, 4f, innerPaint);
                }
            }
        }
    }
    
    private void drawMoodScaleLabels(Canvas canvas) {
        // Draw Y-axis mood labels
        String[] moodLabels = {"Terrible", "Not Great", "Meh", "Good", "Excellent"};
        labelPaint.setTextAlign(Paint.Align.RIGHT);
        labelPaint.setTextSize(16f);
        
        for (int i = 0; i < moodLabels.length; i++) {
            float y = chartBottom - (i * chartHeight / 4f) + 5f;
            canvas.drawText(moodLabels[i], chartLeft - 10f, y, labelPaint);
        }
        
        labelPaint.setTextAlign(Paint.Align.CENTER);
        labelPaint.setTextSize(20f);
    }
    
    private void drawDateLabels(Canvas canvas) {
        SimpleDateFormat labelFormat = new SimpleDateFormat("MM/dd", Locale.getDefault());
        
        // Show date labels at weekly intervals
        for (int i = 0; i < dateLabels.size(); i += 7) {
            try {
                SimpleDateFormat parseFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                String formattedDate = labelFormat.format(parseFormat.parse(dateLabels.get(i)));
                
                float x = chartLeft + (i * chartWidth / (dateLabels.size() - 1));
                labelPaint.setTextSize(16f);
                canvas.drawText(formattedDate, x, chartBottom + 25f, labelPaint);
                labelPaint.setTextSize(20f);
            } catch (Exception e) {
                // Skip invalid dates
            }
        }
    }
    
    private void drawEmptyState(Canvas canvas) {
        textPaint.setTextSize(24f);
        canvas.drawText("No mood data available", getWidth() / 2f, getHeight() / 2f - 20f, textPaint);
        
        labelPaint.setTextSize(18f);
        canvas.drawText("Track your moods to see trends!", getWidth() / 2f, getHeight() / 2f + 20f, labelPaint);
        
        textPaint.setTextSize(28f);
        labelPaint.setTextSize(20f);
    }
    
    public void startAnimation() {
        animationProgress = 0f;
        
        ValueAnimator animator = ValueAnimator.ofFloat(0f, 1f);
        animator.setDuration(ANIMATION_DURATION);
        animator.setInterpolator(new AccelerateDecelerateInterpolator());
        animator.addUpdateListener(animation -> {
            animationProgress = (Float) animation.getAnimatedValue();
            invalidate();
        });
        animator.start();
    }
    
    public void resetAnimation() {
        animationProgress = 0f;
        invalidate();
    }
    
    public boolean hasData() {
        for (Map<String, Integer> dailyMoods : moodDataByDate.values()) {
            for (int count : dailyMoods.values()) {
                if (count > 0) return true;
            }
        }
        return false;
    }
    
    public int getTotalEntries() {
        int total = 0;
        for (Map<String, Integer> dailyMoods : moodDataByDate.values()) {
            for (int count : dailyMoods.values()) {
                total += count;
            }
        }
        return total;
    }
}

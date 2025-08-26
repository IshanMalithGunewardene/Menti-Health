package com.s23010494.mentihealth;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class MoodTrendGraphView extends View {
    private static final int ANIMATION_DURATION = 2000; // 2 seconds
    
    // Colors
    private static final int BACKGROUND_COLOR = Color.parseColor("#232325");
    private static final int GRID_COLOR = Color.parseColor("#444444");
    private static final int TEXT_COLOR = Color.parseColor("#AAAAAA");
    private static final int TREND_LINE_COLOR = Color.parseColor("#31E3BD"); // Your brand color
    private static final int POINT_COLOR = Color.parseColor("#31E3BD");
    private static final int FILL_COLOR = Color.parseColor("#1A31E3BD"); // Semi-transparent brand color
    
    // Mood scoring system (updated for new mood tracker values)
    private static final int MOOD_TERRIBLE = 1;
    private static final int MOOD_NOT_GREAT = 2;
    private static final int MOOD_MEH = 3;
    private static final int MOOD_GOOD = 4;
    private static final int MOOD_EXCELLENT = 5;
    
    private Paint linePaint;
    private Paint pointPaint;
    private Paint gridPaint;
    private Paint textPaint;
    private Paint fillPaint;
    private Paint backgroundPaint;
    
    private List<MoodDataPoint> moodData;
    private float animationProgress = 1f;
    private RectF chartArea;
    
    public MoodTrendGraphView(Context context) {
        super(context);
        init();
    }
    
    public MoodTrendGraphView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }
    
    public MoodTrendGraphView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }
    
    private void init() {
        // Line paint for trend line
        linePaint = new Paint();
        linePaint.setColor(TREND_LINE_COLOR);
        linePaint.setStrokeWidth(6f);
        linePaint.setStyle(Paint.Style.STROKE);
        linePaint.setAntiAlias(true);
        linePaint.setStrokeCap(Paint.Cap.ROUND);
        linePaint.setStrokeJoin(Paint.Join.ROUND);
        
        // Point paint for data points
        pointPaint = new Paint();
        pointPaint.setColor(POINT_COLOR);
        pointPaint.setStyle(Paint.Style.FILL);
        pointPaint.setAntiAlias(true);
        
        // Grid paint
        gridPaint = new Paint();
        gridPaint.setColor(GRID_COLOR);
        gridPaint.setStrokeWidth(1f);
        gridPaint.setStyle(Paint.Style.STROKE);
        gridPaint.setAntiAlias(true);
        gridPaint.setPathEffect(new DashPathEffect(new float[]{10, 10}, 0));
        
        // Text paint
        textPaint = new Paint();
        textPaint.setColor(TEXT_COLOR);
        textPaint.setTextSize(24f);
        textPaint.setAntiAlias(true);
        textPaint.setTextAlign(Paint.Align.CENTER);
        
        // Fill paint for area under curve
        fillPaint = new Paint();
        fillPaint.setColor(FILL_COLOR);
        fillPaint.setStyle(Paint.Style.FILL);
        fillPaint.setAntiAlias(true);
        
        // Background paint
        backgroundPaint = new Paint();
        backgroundPaint.setColor(BACKGROUND_COLOR);
        backgroundPaint.setStyle(Paint.Style.FILL);
        
        moodData = new ArrayList<>();
        chartArea = new RectF();
    }
    
    public void setMoodData(Map<String, String> moodsByDate) {
        moodData.clear();
        
        // Convert mood data to data points - only take the first mood entry per day for trend
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        
        for (Map.Entry<String, String> entry : moodsByDate.entrySet()) {
            try {
                Date date = dateFormat.parse(entry.getKey());
                int moodScore = getMoodScore(entry.getValue());
                moodData.add(new MoodDataPoint(date, moodScore, entry.getValue()));
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        
        // Sort by date to create chronological order
        Collections.sort(moodData, (a, b) -> a.date.compareTo(b.date));
        
        // Trigger redraw
        invalidate();
    }
    
    private int getMoodScore(String mood) {
        if (mood == null) return MOOD_MEH;
        
        switch (mood) {
            case "Terrible": return MOOD_TERRIBLE;
            case "Not great": return MOOD_NOT_GREAT;
            case "Meh": return MOOD_MEH;
            case "Good!": return MOOD_GOOD;
            case "Excellent!": return MOOD_EXCELLENT;
            // Support old values for backward compatibility
            case "anxious": return MOOD_TERRIBLE;
            case "sad": return MOOD_NOT_GREAT;
            case "calm": return MOOD_MEH;
            case "happy": return MOOD_GOOD;
            case "excited": return MOOD_EXCELLENT;
            default: return MOOD_MEH;
        }
    }
    
    private String getMoodLabel(int score) {
        switch (score) {
            case MOOD_TERRIBLE: return "Terrible";
            case MOOD_NOT_GREAT: return "Not Great";
            case MOOD_MEH: return "Meh";
            case MOOD_GOOD: return "Good";
            case MOOD_EXCELLENT: return "Excellent";
            default: return "Meh";
        }
    }
    
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        
        // Define chart area with padding
        float padding = 60f;
        chartArea.set(
            padding,
            padding,
            w - padding,
            h - padding - 40f // Extra bottom padding for labels
        );
    }
    
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        
        // Draw background
        canvas.drawRoundRect(0, 0, getWidth(), getHeight(), 16f, 16f, backgroundPaint);
        
        if (moodData.isEmpty()) {
            drawEmptyState(canvas);
            return;
        }
        
        drawGrid(canvas);
        drawMoodLabels(canvas);
        drawTrendLine(canvas);
        drawDataPoints(canvas);
        drawTitle(canvas);
    }
    
    private void drawEmptyState(Canvas canvas) {
        textPaint.setTextSize(32f);
        textPaint.setColor(TEXT_COLOR);
        canvas.drawText("No mood data available", 
            getWidth() / 2f, getHeight() / 2f, textPaint);
        
        textPaint.setTextSize(24f);
        canvas.drawText("Start tracking your moods to see trends!", 
            getWidth() / 2f, getHeight() / 2f + 50f, textPaint);
        textPaint.setTextSize(24f); // Reset size
    }
    
    private void drawGrid(Canvas canvas) {
        // Horizontal grid lines for mood levels
        for (int i = 1; i <= 5; i++) {
            float y = chartArea.bottom - (i - 1) * (chartArea.height() / 4f);
            canvas.drawLine(chartArea.left, y, chartArea.right, y, gridPaint);
        }
        
        // Vertical grid lines for dates (if we have data)
        if (moodData.size() > 1) {
            int gridLines = Math.min(moodData.size(), 7); // Max 7 vertical lines
            for (int i = 0; i <= gridLines; i++) {
                float x = chartArea.left + (i * chartArea.width() / gridLines);
                canvas.drawLine(x, chartArea.top, x, chartArea.bottom, gridPaint);
            }
        }
    }
    
    private void drawMoodLabels(Canvas canvas) {
        textPaint.setTextAlign(Paint.Align.LEFT);
        textPaint.setTextSize(20f);
        
        // Draw mood labels on the left
        String[] labels = {"Terrible", "Not Great", "Meh", "Good", "Excellent"};
        for (int i = 0; i < labels.length; i++) {
            float y = chartArea.bottom - (i * (chartArea.height() / 4f)) + 8f;
            canvas.drawText(labels[i], 10f, y, textPaint);
        }
        
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setTextSize(24f);
    }
    
    private void drawTrendLine(Canvas canvas) {
        if (moodData.size() < 2) return;
        
        Path trendPath = new Path();
        Path fillPath = new Path();
        boolean firstPoint = true;
        
        // Create smooth curve using cubic Bezier interpolation
        for (int i = 0; i < moodData.size(); i++) {
            float progress = Math.min(animationProgress * moodData.size(), i + 1) - i;
            if (progress <= 0) break;
            
            float x = chartArea.left + (i * chartArea.width() / Math.max(1, moodData.size() - 1));
            float targetY = chartArea.bottom - ((moodData.get(i).moodScore - 1) * (chartArea.height() / 4f));
            float y = targetY;
            
            if (progress < 1f && i > 0) {
                // Smooth interpolation between previous and current point
                float prevY = chartArea.bottom - ((moodData.get(i - 1).moodScore - 1) * (chartArea.height() / 4f));
                y = prevY + (targetY - prevY) * progress;
            }
            
            if (firstPoint) {
                trendPath.moveTo(x, y);
                fillPath.moveTo(x, y);
                firstPoint = false;
            } else if (i == 1) {
                // For the first line segment, use straight line
                trendPath.lineTo(x, y);
                fillPath.lineTo(x, y);
            } else {
                // Use quadratic Bezier curves for smooth transitions
                float prevX = chartArea.left + ((i - 1) * chartArea.width() / Math.max(1, moodData.size() - 1));
                float prevY = chartArea.bottom - ((moodData.get(i - 1).moodScore - 1) * (chartArea.height() / 4f));
                
                // Control point for smooth curve
                float cpX = (prevX + x) / 2f;
                float cpY = (prevY + y) / 2f;
                
                trendPath.quadTo(cpX, cpY, x, y);
                fillPath.quadTo(cpX, cpY, x, y);
            }
        }
        
        // Complete fill path
        if (!firstPoint && moodData.size() > 0) {
            float lastX = chartArea.left + ((moodData.size() - 1) * chartArea.width() / Math.max(1, moodData.size() - 1));
            fillPath.lineTo(lastX, chartArea.bottom);
            fillPath.lineTo(chartArea.left, chartArea.bottom);
            fillPath.close();
            
            // Draw fill first, then line
            canvas.drawPath(fillPath, fillPaint);
            canvas.drawPath(trendPath, linePaint);
        }
    }
    
    private void drawDataPoints(Canvas canvas) {
        for (int i = 0; i < moodData.size(); i++) {
            float progress = Math.min(animationProgress * moodData.size(), i + 1) - i;
            if (progress <= 0) continue;
            
            float x = chartArea.left + (i * chartArea.width() / Math.max(1, moodData.size() - 1));
            float y = chartArea.bottom - ((moodData.get(i).moodScore - 1) * (chartArea.height() / 4f));
            
            // Draw point with scaling animation
            float radius = 6f * Math.min(progress, 1f);
            canvas.drawCircle(x, y, radius, pointPaint);
            
            // Draw inner circle for better visibility
            Paint innerPaint = new Paint(pointPaint);
            innerPaint.setColor(Color.WHITE);
            canvas.drawCircle(x, y, radius * 0.5f, innerPaint);
        }
    }
    
    private void drawTitle(Canvas canvas) {
        textPaint.setTextSize(28f);
        textPaint.setColor(Color.WHITE);
        canvas.drawText("Mood Trends (30 Days)", getWidth() / 2f, 35f, textPaint);
        textPaint.setTextSize(24f);
        textPaint.setColor(TEXT_COLOR);
    }
    
    // Animation methods
    public void startAnimation() {
        animationProgress = 0f;
        post(new Runnable() {
            @Override
            public void run() {
                animationProgress += 0.02f; // 2% per frame
                if (animationProgress < 1f) {
                    invalidate();
                    postDelayed(this, 16); // 60 FPS
                } else {
                    animationProgress = 1f;
                    invalidate();
                }
            }
        });
    }
    
    public void resetAnimation() {
        animationProgress = 0f;
        invalidate();
    }
    
    // Data class for mood points
    private static class MoodDataPoint {
        Date date;
        int moodScore;
        String moodName;
        
        MoodDataPoint(Date date, int moodScore, String moodName) {
            this.date = date;
            this.moodScore = moodScore;
            this.moodName = moodName;
        }
    }
    
    public boolean hasData() {
        return !moodData.isEmpty();
    }
    
    public int getDataPointCount() {
        return moodData.size();
    }
}

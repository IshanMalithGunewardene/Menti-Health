package com.s23010494.mentihealth;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import java.util.HashMap;
import java.util.Map;

public class MoodBarChartView extends View {
    private static final int ANIMATION_DURATION = 1500; // 1.5 seconds
    
    // Colors for different moods
    private static final int COLOR_HAPPY = Color.parseColor("#31E3BD");    // Brand color - bright green
    private static final int COLOR_EXCITED = Color.parseColor("#FF9800");  // Orange
    private static final int COLOR_CALM = Color.parseColor("#2196F3");     // Blue
    private static final int COLOR_SAD = Color.parseColor("#9C27B0");      // Purple
    private static final int COLOR_ANXIOUS = Color.parseColor("#F44336");  // Red
    private static final int BACKGROUND_COLOR = Color.parseColor("#232325");
    private static final int TEXT_COLOR = Color.parseColor("#FFFFFF");
    private static final int LABEL_COLOR = Color.parseColor("#AAAAAA");
    
    private Paint barPaint;
    private Paint textPaint;
    private Paint labelPaint;
    private Paint backgroundPaint;
    
    private Map<String, Integer> moodCounts;
    private Map<String, Float> animatedHeights;
    private float animationProgress = 1f;
    private int maxCount = 0;
    
    // Bar configuration
    private float barWidth;
    private float barSpacing;
    private float chartHeight;
    private float chartTopMargin = 60f;
    private float chartBottomMargin = 80f;
    
    public MoodBarChartView(Context context) {
        super(context);
        init();
    }
    
    public MoodBarChartView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }
    
    public MoodBarChartView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }
    
    private void init() {
        // Bar paint
        barPaint = new Paint();
        barPaint.setStyle(Paint.Style.FILL);
        barPaint.setAntiAlias(true);
        
        // Text paint for numbers
        textPaint = new Paint();
        textPaint.setColor(TEXT_COLOR);
        textPaint.setTextSize(32f);
        textPaint.setAntiAlias(true);
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setFakeBoldText(true);
        
        // Label paint for mood names
        labelPaint = new Paint();
        labelPaint.setColor(LABEL_COLOR);
        labelPaint.setTextSize(24f);
        labelPaint.setAntiAlias(true);
        labelPaint.setTextAlign(Paint.Align.CENTER);
        
        // Background paint
        backgroundPaint = new Paint();
        backgroundPaint.setColor(BACKGROUND_COLOR);
        backgroundPaint.setStyle(Paint.Style.FILL);
        
        moodCounts = new HashMap<>();
        animatedHeights = new HashMap<>();
        
        // Initialize with empty data
        initializeEmptyData();
    }
    
    private void initializeEmptyData() {
        moodCounts.put("Happy", 0);
        moodCounts.put("Excited", 0);
        moodCounts.put("Calm", 0);
        moodCounts.put("Sad", 0);
        moodCounts.put("Anxious", 0);
        
        for (String mood : moodCounts.keySet()) {
            animatedHeights.put(mood, 0f);
        }
    }
    
    public void setMoodData(Map<String, Integer> moodData) {
        if (moodData == null) {
            initializeEmptyData();
            invalidate();
            return;
        }
        
        // Update mood counts
        moodCounts.clear();
        moodCounts.put("Happy", moodData.getOrDefault("Happy", 0));
        moodCounts.put("Excited", moodData.getOrDefault("Excited", 0));
        moodCounts.put("Calm", moodData.getOrDefault("Calm", 0));
        moodCounts.put("Sad", moodData.getOrDefault("Sad", 0));
        moodCounts.put("Anxious", moodData.getOrDefault("Anxious", 0));
        
        // Calculate max count for scaling
        maxCount = 0;
        for (int count : moodCounts.values()) {
            maxCount = Math.max(maxCount, count);
        }
        
        // Ensure at least height of 1 for scaling
        if (maxCount == 0) {
            maxCount = 1;
        }
        
        // Reset animation heights
        for (String mood : moodCounts.keySet()) {
            animatedHeights.put(mood, 0f);
        }
        
        invalidate();
    }
    
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        
        chartHeight = h - chartTopMargin - chartBottomMargin;
        
        // Calculate bar dimensions
        int barCount = 5; // 5 moods
        float totalSpacing = w * 0.1f; // 10% for spacing
        float totalBarWidth = w - totalSpacing;
        
        barWidth = totalBarWidth / barCount;
        barSpacing = totalSpacing / (barCount + 1);
    }
    
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        
        // Draw background
        canvas.drawRoundRect(0, 0, getWidth(), getHeight(), 16f, 16f, backgroundPaint);
        
        // Draw title
        textPaint.setTextSize(28f);
        canvas.drawText("Mood Statistics", getWidth() / 2f, 35f, textPaint);
        textPaint.setTextSize(32f);
        
        if (maxCount == 0 || (getTotalMoodCount() == 0)) {
            drawEmptyState(canvas);
            return;
        }
        
        drawBars(canvas);
    }
    
    private void drawEmptyState(Canvas canvas) {
        labelPaint.setTextSize(20f);
        canvas.drawText("No mood data available", getWidth() / 2f, getHeight() / 2f - 20f, labelPaint);
        canvas.drawText("Track your moods to see statistics!", getWidth() / 2f, getHeight() / 2f + 20f, labelPaint);
        labelPaint.setTextSize(24f);
    }
    
    private void drawBars(Canvas canvas) {
        String[] moods = {"Happy", "Excited", "Calm", "Sad", "Anxious"};
        int[] colors = {COLOR_HAPPY, COLOR_EXCITED, COLOR_CALM, COLOR_SAD, COLOR_ANXIOUS};
        
        for (int i = 0; i < moods.length; i++) {
            String mood = moods[i];
            int count = moodCounts.getOrDefault(mood, 0);
            float animatedHeight = animatedHeights.getOrDefault(mood, 0f);
            
            // Calculate bar position
            float barLeft = barSpacing + i * (barWidth + barSpacing);
            float barRight = barLeft + barWidth;
            
            // Calculate bar height based on count and animation
            float maxBarHeight = chartHeight;
            float targetBarHeight = maxBarHeight * (count / (float) maxCount);
            float currentBarHeight = targetBarHeight * animatedHeight;
            
            float barTop = chartTopMargin + (maxBarHeight - currentBarHeight);
            float barBottom = chartTopMargin + maxBarHeight;
            
            // Draw bar with rounded corners
            barPaint.setColor(colors[i]);
            RectF barRect = new RectF(barLeft, barTop, barRight, barBottom);
            canvas.drawRoundRect(barRect, 8f, 8f, barPaint);
            
            // Draw count text on top of bar (if bar is visible)
            if (currentBarHeight > 20f && count > 0) {
                textPaint.setTextSize(24f);
                canvas.drawText(String.valueOf(count), 
                    barLeft + barWidth / 2f, 
                    barTop - 10f, 
                    textPaint);
                textPaint.setTextSize(32f);
            }
            
            // Draw mood label below bar
            float labelY = barBottom + 30f;
            labelPaint.setTextSize(18f);
            canvas.drawText(mood, barLeft + barWidth / 2f, labelY, labelPaint);
            labelPaint.setTextSize(24f);
        }
    }
    
    private int getTotalMoodCount() {
        int total = 0;
        for (int count : moodCounts.values()) {
            total += count;
        }
        return total;
    }
    
    public void startAnimation() {
        animationProgress = 0f;
        
        ValueAnimator animator = ValueAnimator.ofFloat(0f, 1f);
        animator.setDuration(ANIMATION_DURATION);
        animator.setInterpolator(new AccelerateDecelerateInterpolator());
        animator.addUpdateListener(animation -> {
            animationProgress = (Float) animation.getAnimatedValue();
            
            // Update animated heights for each mood
            for (String mood : moodCounts.keySet()) {
                animatedHeights.put(mood, animationProgress);
            }
            
            invalidate();
        });
        animator.start();
    }
    
    public void resetAnimation() {
        animationProgress = 0f;
        for (String mood : moodCounts.keySet()) {
            animatedHeights.put(mood, 0f);
        }
        invalidate();
    }
    
    public boolean hasData() {
        return getTotalMoodCount() > 0;
    }
    
    public int getTotalEntries() {
        return getTotalMoodCount();
    }
    
    public String getMostFrequentMood() {
        if (getTotalMoodCount() == 0) return "None";
        
        String mostFrequent = "None";
        int maxCount = 0;
        
        for (Map.Entry<String, Integer> entry : moodCounts.entrySet()) {
            if (entry.getValue() > maxCount) {
                maxCount = entry.getValue();
                mostFrequent = entry.getKey();
            }
        }
        
        return mostFrequent;
    }
}

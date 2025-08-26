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

public class HorizontalStepProgressView extends View {
    private static final int MAX_STEPS_GOAL = 10000; // Daily step goal
    private static final int ANIMATION_DURATION = 1000; // 1 second
    
    private Paint backgroundPaint;
    private Paint progressPaint;
    private RectF backgroundRect;
    private RectF progressRect;
    
    private float currentProgress = 0f; // 0 to 1
    private float targetProgress = 0f;
    private int stepCount = 0;
    private ValueAnimator progressAnimator;
    
    // Colors using your brand color
    private static final int BACKGROUND_COLOR = Color.parseColor("#4A4A4A"); // Dark gray background
    private static final int PROGRESS_COLOR = Color.parseColor("#31E3BD"); // Your brand color
    
    private static final float BAR_HEIGHT = 12f; // Height of progress bar
    private static final float CORNER_RADIUS = 6f; // Rounded corners
    
    public HorizontalStepProgressView(Context context) {
        super(context);
        init();
    }
    
    public HorizontalStepProgressView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }
    
    public HorizontalStepProgressView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }
    
    private void init() {
        // Background bar paint
        backgroundPaint = new Paint();
        backgroundPaint.setColor(BACKGROUND_COLOR);
        backgroundPaint.setStyle(Paint.Style.FILL);
        backgroundPaint.setAntiAlias(true);
        
        // Progress bar paint
        progressPaint = new Paint();
        progressPaint.setColor(PROGRESS_COLOR);
        progressPaint.setStyle(Paint.Style.FILL);
        progressPaint.setAntiAlias(true);
        
        backgroundRect = new RectF();
        progressRect = new RectF();
    }
    
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        
        // Center the progress bar vertically
        float centerY = h / 2f;
        float top = centerY - BAR_HEIGHT / 2f;
        float bottom = centerY + BAR_HEIGHT / 2f;
        
        // Background bar spans full width with some padding
        float padding = 16f;
        backgroundRect.set(padding, top, w - padding, bottom);
        
        // Progress bar starts from left, width depends on progress
        updateProgressRect();
    }
    
    private void updateProgressRect() {
        if (backgroundRect.isEmpty()) return;
        
        float progressWidth = (backgroundRect.width()) * currentProgress;
        progressRect.set(
            backgroundRect.left,
            backgroundRect.top,
            backgroundRect.left + progressWidth,
            backgroundRect.bottom
        );
    }
    
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        
        // Draw background bar
        canvas.drawRoundRect(backgroundRect, CORNER_RADIUS, CORNER_RADIUS, backgroundPaint);
        
        // Draw progress bar (only if there's progress)
        if (currentProgress > 0) {
            canvas.drawRoundRect(progressRect, CORNER_RADIUS, CORNER_RADIUS, progressPaint);
        }
    }
    
    public void setStepCount(int steps) {
        if (this.stepCount == steps) return; // No change needed
        
        this.stepCount = steps;
        targetProgress = Math.min(1f, Math.max(0f, (float) steps / MAX_STEPS_GOAL));
        
        animateToProgress();
    }
    
    private void animateToProgress() {
        if (progressAnimator != null && progressAnimator.isRunning()) {
            progressAnimator.cancel();
        }
        
        progressAnimator = ValueAnimator.ofFloat(currentProgress, targetProgress);
        progressAnimator.setDuration(ANIMATION_DURATION);
        progressAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        progressAnimator.addUpdateListener(animation -> {
            currentProgress = (Float) animation.getAnimatedValue();
            updateProgressRect();
            invalidate(); // Trigger redraw
        });
        progressAnimator.start();
    }
    
    public int getStepCount() {
        return stepCount;
    }
    
    public int getMaxStepsGoal() {
        return MAX_STEPS_GOAL;
    }
    
    public float getProgress() {
        return currentProgress;
    }
    
    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (progressAnimator != null && progressAnimator.isRunning()) {
            progressAnimator.cancel();
        }
    }
}

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

public class SemicircularStepProgressView extends View {
    private static final int MAX_STEPS_GOAL = 10000; // Daily step goal
    private static final int ANIMATION_DURATION = 1200; // 1.2 seconds
    
    private Paint backgroundPaint;
    private Paint progressPaint;
    private RectF arcRect;
    
    private float currentProgress = 0f; // 0 to 1
    private float targetProgress = 0f;
    private int stepCount = 0;
    private ValueAnimator progressAnimator;
    
    // Colors using your brand color
    private static final int BACKGROUND_COLOR = Color.parseColor("#4A4A4A"); // Dark gray background
    private static final int PROGRESS_COLOR = Color.parseColor("#31E3BD"); // Your brand color
    
    private static final float STROKE_WIDTH = 16f; // Thickness of the arc
    private static final float START_ANGLE = 180f; // Start from left side
    private static final float SWEEP_ANGLE = 180f; // Half circle (semicircle)
    
    public SemicircularStepProgressView(Context context) {
        super(context);
        init();
    }
    
    public SemicircularStepProgressView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }
    
    public SemicircularStepProgressView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }
    
    private void init() {
        // Background arc paint
        backgroundPaint = new Paint();
        backgroundPaint.setColor(BACKGROUND_COLOR);
        backgroundPaint.setStyle(Paint.Style.STROKE);
        backgroundPaint.setStrokeWidth(STROKE_WIDTH);
        backgroundPaint.setAntiAlias(true);
        backgroundPaint.setStrokeCap(Paint.Cap.ROUND);
        
        // Progress arc paint
        progressPaint = new Paint();
        progressPaint.setColor(PROGRESS_COLOR);
        progressPaint.setStyle(Paint.Style.STROKE);
        progressPaint.setStrokeWidth(STROKE_WIDTH);
        progressPaint.setAntiAlias(true);
        progressPaint.setStrokeCap(Paint.Cap.ROUND);
        
        arcRect = new RectF();
    }
    
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        
        // Create arc bounds - semicircle that fits the view
        float padding = STROKE_WIDTH / 2f + 8f; // Padding to prevent clipping
        float diameter = Math.min(w - padding * 2, h * 2 - padding); // Make it fit as semicircle
        float centerX = w / 2f;
        float top = padding;
        
        arcRect.set(
            centerX - diameter / 2f,
            top,
            centerX + diameter / 2f,
            top + diameter
        );
    }
    
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        
        // Draw background semicircle
        canvas.drawArc(arcRect, START_ANGLE, SWEEP_ANGLE, false, backgroundPaint);
        
        // Draw progress arc (only if there's actual progress)
        if (currentProgress > 0) {
            float progressSweepAngle = SWEEP_ANGLE * currentProgress;
            canvas.drawArc(arcRect, START_ANGLE, progressSweepAngle, false, progressPaint);
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

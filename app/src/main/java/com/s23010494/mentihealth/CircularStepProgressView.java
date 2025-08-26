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

public class CircularStepProgressView extends View {
    private static final int MAX_STEPS_GOAL = 10000; // Daily step goal
    private static final int ANIMATION_DURATION = 1500; // 1.5 seconds
    
    private Paint backgroundPaint;
    private Paint progressPaint;
    private RectF arcRect;
    
    private float currentProgress = 0f; // 0 to 1
    private float targetProgress = 0f;
    private int stepCount = 0;
    private ValueAnimator progressAnimator;
    
    // Colors using your brand color
    private static final int BACKGROUND_COLOR = Color.parseColor("#4A4A4A"); // Gray background arc
    private static final int PROGRESS_COLOR = Color.parseColor("#31E3BD"); // Your brand color
    
    public CircularStepProgressView(Context context) {
        super(context);
        init();
    }
    
    public CircularStepProgressView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }
    
    public CircularStepProgressView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }
    
    private void init() {
        // Background arc paint
        backgroundPaint = new Paint();
        backgroundPaint.setColor(BACKGROUND_COLOR);
        backgroundPaint.setStyle(Paint.Style.STROKE);
        backgroundPaint.setStrokeWidth(20f);
        backgroundPaint.setAntiAlias(true);
        backgroundPaint.setStrokeCap(Paint.Cap.ROUND);
        
        // Progress arc paint
        progressPaint = new Paint();
        progressPaint.setColor(PROGRESS_COLOR);
        progressPaint.setStyle(Paint.Style.STROKE);
        progressPaint.setStrokeWidth(20f);
        progressPaint.setAntiAlias(true);
        progressPaint.setStrokeCap(Paint.Cap.ROUND);
        
        arcRect = new RectF();
    }
    
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        
        // Create arc bounds with some padding
        float padding = 40f;
        float size = Math.min(w, h) - padding * 2;
        float centerX = w / 2f;
        float centerY = h / 2f;
        
        arcRect.set(
            centerX - size / 2f,
            centerY - size / 2f,
            centerX + size / 2f,
            centerY + size / 2f
        );
    }
    
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        
        // Draw background arc (semicircle from -90 to 180 degrees)
        canvas.drawArc(arcRect, -90f, 180f, false, backgroundPaint);
        
        // Draw progress arc (only if there's actual progress)
        if (currentProgress > 0) {
            float progressAngle = 180f * currentProgress;
            canvas.drawArc(arcRect, -90f, progressAngle, false, progressPaint);
        }
    }
    
    public void setStepCount(int steps) {
        if (this.stepCount == steps) return; // No change needed
        
        this.stepCount = steps;
        targetProgress = Math.min(1f, (float) steps / MAX_STEPS_GOAL);
        
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
    
    public void setMaxStepsGoal(int goal) {
        // Could add this functionality if needed
    }
    
    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (progressAnimator != null && progressAnimator.isRunning()) {
            progressAnimator.cancel();
        }
    }
}

package com.example.provapdm;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.location.GnssStatus;
import android.util.AttributeSet;
import android.view.View;

public class RumoView extends View {
    private Paint paint;
    private Paint textPaint;
    private Paint needlePaint;
    private float direction = 0;

    public RumoView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public RumoView(Context context) {
        super(context);
        init();
    }

    private void init() {
        paint = new Paint();
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(8);
        paint.setAntiAlias(true);
        paint.setColor(Color.WHITE);
        textPaint = new Paint();
        textPaint.setColor(Color.WHITE);
        textPaint.setTextSize(50);
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setAntiAlias(true);
        needlePaint = new Paint();
        needlePaint.setColor(Color.CYAN);
        needlePaint.setStyle(Paint.Style.FILL);
        needlePaint.setAntiAlias(true);
    }

    public void setDirection(float direction) {
        this.direction = direction;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int width = getWidth();
        int height = getHeight();
        float centerX = width / 2;
        float centerY = height / 2;
        float radius = Math.min(centerX, centerY) - 20;
        textPaint.setTextSize(30);
        canvas.drawText("N", centerX, centerY - radius + 50, textPaint);
        canvas.drawText("S", centerX, centerY + radius - 20, textPaint);
        canvas.drawText("E", centerX + radius - 20, centerY + 5, textPaint);
        canvas.drawText("W", centerX - radius + 20, centerY + 5, textPaint);
        paint.setColor(Color.WHITE);
        paint.setStrokeWidth(10);
        canvas.drawCircle(centerX, centerY, radius, paint);
        drawNeedle(canvas, centerX, centerY, radius);
    }

    private void drawNeedle(Canvas canvas, float centerX, float centerY, float radius) {
        float needleLength = radius - 40;
        float needleBaseRadius = 30;
        float endX = (float) (centerX + Math.cos(Math.toRadians(direction)) * needleLength);
        float endY = (float) (centerY + Math.sin(Math.toRadians(direction)) * needleLength);
        canvas.drawLine(centerX, centerY, endX, endY, needlePaint);
        needlePaint.setStyle(Paint.Style.FILL);
        canvas.drawCircle(centerX, centerY, needleBaseRadius, needlePaint);
        Path needleTip = new Path();
        needleTip.moveTo(endX, endY);
        needleTip.lineTo((float) (centerX + Math.cos(Math.toRadians(direction - 150)) * 50),
                (float) (centerY + Math.sin(Math.toRadians(direction - 150)) * 50));
        needleTip.lineTo((float) (centerX + Math.cos(Math.toRadians(direction + 150)) * 50),
                (float) (centerY + Math.sin(Math.toRadians(direction + 150)) * 50));
        needleTip.close();
        canvas.drawPath(needleTip, needlePaint);
    }
}

package com.example.provapdm;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.View;

import java.util.ArrayList;

public class QualidadeSateliteView extends View {
    private Paint paint;
    private ArrayList<String> satelliteNames;
    private ArrayList<Float> signalQualityData;


    public QualidadeSateliteView(Context context, AttributeSet attrs) {
        super(context, attrs);
        paint = new Paint();
        satelliteNames = new ArrayList<>();
        signalQualityData = new ArrayList<>();
    }

    public void setSignalQualityData(ArrayList<String> names, ArrayList<Float> qualities) {
        this.satelliteNames = names;
        this.signalQualityData = qualities;

        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        float width = getWidth();
        float height = getHeight();

        float barWidth = width / (satelliteNames.size() * 1.5f);
        float maxBarHeight = height - 100;
        float startX = (width - (barWidth * satelliteNames.size() * 1.5f)) / 2;


        paint.setColor(Color.BLACK);
        canvas.drawRect(0, 0, width, height, paint);


        paint.setColor(Color.BLUE);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(5); // Largura da borda
        canvas.drawRect(0, 0, width, height, paint);


        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.WHITE);

        paint.setTextSize(50);
        canvas.drawText("Qualidade do Sinal GNSS", width / 2 - paint.measureText("Qualidade do Sinal GNSS") / 2, 50, paint);


        for (int i = 0; i < satelliteNames.size(); i++) {
            float signalQuality = signalQualityData.get(i);

            int color;
            if (signalQuality < 20) {
                color = Color.RED;
            } else if (signalQuality < 40) {
                color = Color.YELLOW;
            } else {
                color = Color.GREEN;
            }


            paint.setShader(new LinearGradient(
                    startX + i * barWidth * 1.5f, maxBarHeight,
                    startX + (i * barWidth * 1.5f) + barWidth, maxBarHeight - (signalQuality / 60) * maxBarHeight,
                    color, Color.DKGRAY, Shader.TileMode.CLAMP));


            canvas.drawRoundRect(
                    startX + i * barWidth * 1.5f, maxBarHeight - (signalQuality / 60) * maxBarHeight,
                    startX + (i * barWidth * 1.5f) + barWidth, maxBarHeight,
                    20, 20, paint);


            paint.setShader(null);


            paint.setStyle(Paint.Style.STROKE);
            paint.setColor(Color.BLACK);
            canvas.drawRoundRect(
                    startX + i * barWidth * 1.5f, maxBarHeight - (signalQuality / 60) * maxBarHeight,
                    startX + (i * barWidth * 1.5f) + barWidth, maxBarHeight,
                    20, 20, paint);


            paint.setStyle(Paint.Style.FILL);
            paint.setColor(Color.WHITE);
            paint.setTextSize(30);


            String svidText = satelliteNames.get(i);
            canvas.drawText(svidText,
                    startX + (i * barWidth * 1.5f) + barWidth / 2 - paint.measureText(svidText) / 2,
                    maxBarHeight + 30, paint);

            String signalQualityText = String.valueOf((int) signalQuality);
            canvas.drawText(signalQualityText,
                    startX + (i * barWidth * 1.5f) + barWidth / 2 - paint.measureText(signalQualityText) / 2,
                    maxBarHeight - (signalQuality / 60) * maxBarHeight - 10, paint);
        }
    }

}

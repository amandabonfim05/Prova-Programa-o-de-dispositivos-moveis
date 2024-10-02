package com.example.provapdm;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.View;

public class RumoView extends View {
    private Paint paint;
    private Paint backgroundPaint;
    private Paint textPaint;
    private float rumo = 0;

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
        paint.setStrokeWidth(10);
        paint.setStyle(Paint.Style.STROKE);
        paint.setAntiAlias(true); //suavizar as bordas

        paint.setShader(new LinearGradient(0, 0, 0, getHeight(),
                Color.BLUE, Color.CYAN, Shader.TileMode.CLAMP));

        backgroundPaint = new Paint();
        backgroundPaint.setColor(Color.LTGRAY);
        backgroundPaint.setStyle(Paint.Style.FILL);
        backgroundPaint.setShadowLayer(15, 0, 0, Color.BLACK);

        textPaint = new Paint();
        textPaint.setColor(Color.WHITE);
        textPaint.setTextSize(50);
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setAntiAlias(true);
    }

    public void setRumo(float rumo) {
        this.rumo = rumo;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int width = getWidth();
        int height = getHeight();

        float centroX = width / 2;
        float centroY = height / 2;

        float tamanho = Math.min(centroX, centroY) * 2 - 20;
        float esquerda = centroX - tamanho / 2;
        float cima = centroY - tamanho / 2;
        float direita = centroX + tamanho / 2;
        float baixo = centroY + tamanho / 2;

        canvas.drawRect(esquerda, cima, direita, baixo, backgroundPaint);

        float fimX = (float) (centroX + (Math.cos(Math.toRadians(rumo)) * (tamanho / 2)));
        float fimY = (float) (centroY + (Math.sin(Math.toRadians(rumo)) * (tamanho / 2)));

        desenharSeta(canvas, centroX, centroY, fimX, fimY);

        canvas.drawText(String.format("%.0f°", rumo), centroX, centroY + textPaint.getTextSize() / 4, textPaint);
    }

    private void desenharSeta(Canvas canvas, float inicioX, float inicioY, float fimX, float fimY) {
        canvas.drawLine(inicioX, inicioY, fimX, fimY, paint);

        float anguloSeta = (float) Math.atan2(fimY - inicioY, fimX - inicioX);
        float tamanhoSeta = 40;
        float larguraSeta = 20;

        float setaX1 = fimX - tamanhoSeta * (float) Math.cos( - Math.PI / 6);
        float setaY1 = fimY - tamanhoSeta * (float) Math.sin(anguloSeta - Math.PI / 6);
        float setaX2 = fimX - tamanhoSeta * (float) Math.cos(anguloSeta + Math.PI / 6);
        float setaY2 = fimY - tamanhoSeta * (float) Math.sin(anguloSeta + Math.PI / 6);

        Path caminhoSeta = new Path();
        caminhoSeta.moveTo(fimX, fimY);
        caminhoSeta.lineTo(setaX1, setaY1);
        caminhoSeta.lineTo(setaX2, setaY2);
        caminhoSeta.close();

        canvas.drawPath(caminhoSeta, paint);
    }
}

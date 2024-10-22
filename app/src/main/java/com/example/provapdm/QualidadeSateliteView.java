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
    private ArrayList<String> nomesSatelites;
    private ArrayList<Float> dadosQualidadeSatelites;


    public QualidadeSateliteView(Context context, AttributeSet attrs) { // construtor
        super(context, attrs);
        paint = new Paint();
        nomesSatelites = new ArrayList<>();
        dadosQualidadeSatelites = new ArrayList<>();
    }


    public void setSignalQualityData(ArrayList<String> nomes, ArrayList<Float> qualidade) {  // define os dados de qualidade dos satelites e solicita o redesenho
        this.nomesSatelites = nomes;
        this.dadosQualidadeSatelites = qualidade;
        invalidate();  // solicita que a view seja redesenhada
    }

    @Override
    protected void onDraw(Canvas canvas) { //desenha a interface
        super.onDraw(canvas);
        float width = getWidth();
        float height = getHeight();
        float barraWidth = width / (nomesSatelites.size() * 1.5f);  // largura das barras
        float maxBarraHeight = height - 100;  // altura max das barras
        float comecaX = (width - (barraWidth * nomesSatelites.size() * 1.5f)) / 2;  // posição inicial da primeira barra

        // fundo de cor preta
        paint.setColor(Color.BLACK);
        canvas.drawRect(0, 0, width, height, paint);

        // desenha uma borda azul
        paint.setColor(Color.BLUE);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(5);
        canvas.drawRect(0, 0, width, height, paint);

        // desenha o título "Qualidade do Sinal GNSS" no topo
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.WHITE);
        paint.setTextSize(50);
        canvas.drawText("Qualidade do Sinal GNSS", width / 2 - paint.measureText("Qualidade do Sinal GNSS") / 2, 50, paint);


        for (int i = 0; i < nomesSatelites.size(); i++) { // loop para desenhar as barras de cada satélite
            float qualidadeSinal = dadosQualidadeSatelites.get(i);  // pega a qualidade do sinal


            int cor;
            if (qualidadeSinal < 20) {  // define a cor com base na qualidade do sinal
                cor = Color.RED;
            } else if (qualidadeSinal < 40) {
                cor = Color.YELLOW;
            } else {
                cor = Color.GREEN;
            }


            paint.setShader(new LinearGradient( // gradiente nas barras
                    comecaX + i * barraWidth * 1.5f, maxBarraHeight,
                    comecaX + (i * barraWidth * 1.5f) + barraWidth, maxBarraHeight - (qualidadeSinal / 60) * maxBarraHeight,
                    cor, Color.DKGRAY, Shader.TileMode.CLAMP));


            canvas.drawRoundRect( // barra arrendondada
                    comecaX + i * barraWidth * 1.5f, maxBarraHeight - (qualidadeSinal / 60) * maxBarraHeight,
                    comecaX + (i * barraWidth * 1.5f) + barraWidth, maxBarraHeight,
                    20, 20, paint);


            paint.setShader(null);
            paint.setStyle(Paint.Style.STROKE);
            paint.setColor(Color.BLACK);


            canvas.drawRoundRect(  // desenha a borda da barra
                    comecaX + i * barraWidth * 1.5f, maxBarraHeight - (qualidadeSinal / 60) * maxBarraHeight,
                    comecaX + (i * barraWidth * 1.5f) + barraWidth, maxBarraHeight,
                    20, 20, paint);


            paint.setStyle(Paint.Style.FILL); // desenha o nome do satélite abaixo da barra
            paint.setColor(Color.WHITE);
            paint.setTextSize(30);
            String svidTexto = nomesSatelites.get(i);
            canvas.drawText(svidTexto,
                    comecaX + (i * barraWidth * 1.5f) + barraWidth / 2 - paint.measureText(svidTexto) / 2,
                    maxBarraHeight + 30, paint);


            String textoQualidadeSinal = String.valueOf((int) qualidadeSinal);  // desenha o valor da qualidade do sinal dentro da barra
            canvas.drawText(textoQualidadeSinal,
                    comecaX + (i * barraWidth * 1.5f) + barraWidth / 2 - paint.measureText(textoQualidadeSinal) / 2,
                    maxBarraHeight - (qualidadeSinal / 60) * maxBarraHeight - 10, paint);
        }
    }

}
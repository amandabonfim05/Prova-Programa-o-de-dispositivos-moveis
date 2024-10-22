package com.example.provapdm;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.location.GnssStatus;
import android.util.AttributeSet;
import android.view.View;
import android.widget.CheckBox;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;

import java.util.ArrayList;
import java.util.Random;

public class EsferaCelesteView extends View {
    private GnssStatus newStatus; // guarda o status dos satelites
    private Paint paint;
    private int r;
    private int height, width;
    private Random random;
    private ArrayList<estrela> estrelas;

    // controlar quais satelites devem ser desenhados e se estão sendo usados
    private boolean filtroGPS = true;
    private boolean filtroGalileo = true;
    private boolean filtroGlonass = true;
    private boolean filtroUsado = true;

    private class estrela {
        float x, y, velocidade;

        estrela(float x, float y, float speed) {
            this.x = x;
            this.y = y;
            this.velocidade = speed;
        }

        // atualiza a posição da estrela e simula movimento
        void updatePosition() {
            y += velocidade;  // a posicao é incrementada com a  velocidade e a estrela move em direcao ao fundo
            if (y > r) { // quando a estrela atinge o fundo
                y = -r; // ela reaparece no topo
                x = random.nextFloat() * 2 * r - r; // posicao horizontal aleatoria
                velocidade = 0.5f + random.nextFloat(); // velocidade aleatoria
            }
        }
    }

    public EsferaCelesteView(Context context, @Nullable AttributeSet attrs) { // contrutor gera 300 estrelas
        super(context, attrs);
        paint = new Paint();
        random = new Random();
        estrelas = new ArrayList<>();

        for (int i = 0; i < 300; i++) {
            float angle = random.nextFloat() * 2 * (float) Math.PI;
            float radius = random.nextFloat() * r;
            float x = radius * (float) Math.cos(angle);
            float y = radius * (float) Math.sin(angle);
            float speed = 0.5f + random.nextFloat();
            estrelas.add(new estrela(x, y, speed));
        }

        // abre um listener onclick que abre um dialogo para configurar os satelites
        setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mostrarDialog(context);
            }
        });
    }

    private boolean deveDesenharSatelite(int sateliteIndex) { //verifica se o satelite deve ser desenhado com base na constelacao
        // e se ele esta sendo usado no calculo da posição

        int tipoConstelacao = newStatus.getConstellationType(sateliteIndex);
        boolean usandoFix = newStatus.usedInFix(sateliteIndex);
        boolean ChecandoConstelacao = (tipoConstelacao == GnssStatus.CONSTELLATION_GPS && filtroGPS) ||
                (tipoConstelacao == GnssStatus.CONSTELLATION_GALILEO && filtroGalileo) ||
                (tipoConstelacao == GnssStatus.CONSTELLATION_GLONASS && filtroGlonass);
        boolean checandoFix = filtroUsado ? usandoFix : true;
        return ChecandoConstelacao && checandoFix;
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) { //desenhar a esfera celeste e os satelites
        super.onDraw(canvas);
        width = getMeasuredWidth();
        height = getMeasuredHeight();
        if (width < height)
            r = (int) (width / 2 * 0.9);
        else
            r = (int) (height / 2 * 0.9);
        desenharEstrelas(canvas);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(5);
        paint.setColor(Color.BLUE);
        int raio = r;
        canvas.drawCircle(computarXc(0), computarYc(0), raio, paint);
        raio = (int) (raio * Math.cos(Math.toRadians(45)));
        canvas.drawCircle(computarXc(0), computarYc(0), raio, paint);
        raio = (int) (raio * Math.cos(Math.toRadians(60)));
        canvas.drawCircle(computarXc(0), computarYc(0), raio, paint);
        canvas.drawLine(computarXc(0), computarYc(-r), computarXc(0), computarYc(r), paint);
        canvas.drawLine(computarXc(-r), computarYc(0), computarXc(r), computarYc(0), paint);
        paint.setStyle(Paint.Style.FILL);
        if (newStatus != null) {
            for (int i = 0; i < newStatus.getSatelliteCount(); i++) {
                float az = newStatus.getAzimuthDegrees(i);
                float el = newStatus.getElevationDegrees(i);
                float x = (float) (r * Math.cos(Math.toRadians(el)) * Math.sin(Math.toRadians(az)));
                float y = (float) (r * Math.cos(Math.toRadians(el)) * Math.cos(Math.toRadians(az)));
                if (deveDesenharSatelite(i)) {
                    desenhaSatelite(canvas, computarXc(x), computarYc(y), newStatus.getConstellationType(i));
                    desenhaSateliteInfo(canvas, x, y, i);
                }
            }
        }
        postInvalidateDelayed(30);
    }

    private void desenharEstrelas(Canvas canvas) { // desenha cada estrela na tela e se estiver fora do raio a posição e randomziada
        paint.setColor(Color.WHITE);
        for (EsferaCelesteView.estrela estrela : estrelas) {
            if (Math.sqrt(estrela.x * estrela.x + estrela.y * estrela.y) <= r) {
                canvas.drawCircle(computarXc(estrela.x), computarYc(estrela.y), 2, paint);
            } else {
                float angulo = random.nextFloat() * 2 * (float) Math.PI;
                float raio = random.nextFloat() * r;
                estrela.x = raio * (float) Math.cos(angulo);
                estrela.y = raio * (float) Math.sin(angulo);
            }
            estrela.updatePosition();
        }
    }

    private void desenhaSatelite(Canvas canvas, float cx, float cy, int tipoConstelacao) {
        switch (tipoConstelacao) {
            case GnssStatus.CONSTELLATION_GPS: // desenha um octaedro amarelo
                paint.setColor(Color.YELLOW);
                desenharOctaedro(canvas, cx, cy);
                break;

            case GnssStatus.CONSTELLATION_GALILEO: //desenha um círculo azul
                paint.setColor(Color.BLUE);
                canvas.drawCircle(cx, cy, 15, paint);
                break;

            case GnssStatus.CONSTELLATION_GLONASS: // desenha uma forma de cruz vermelha
                paint.setColor(Color.RED);
                float[] caminhoX = {cx, cx - 10, cx, cx + 10};
                float[] caminhoY = {cy - 10, cy, cy + 10, cy};
                canvas.drawPath(criarCaminho(caminhoX, caminhoY), paint);
                break;

            default: //  desenha um retângulo cinza
                paint.setColor(Color.GRAY);
                canvas.drawRect(cx - 5, cy - 15, cx + 5, cy + 5, paint);
                break;
        }
    }

    private void desenharOctaedro(Canvas canvas, float cx, float cy) {
        float size = 15;
        Path path = new Path();
        path.moveTo(cx, cy - size);
        path.lineTo(cx - size, cy);
        path.lineTo(cx + size, cy);
        path.close();
        paint.setStyle(Paint.Style.STROKE);
        canvas.drawPath(path, paint);
        paint.setStyle(Paint.Style.FILL);
        canvas.drawCircle(cx, cy, size / 3, paint);
    }

    private Path criarCaminho(float[] xPoints, float[] yPoints) {
        Path path = new Path();
        path.moveTo(xPoints[0], yPoints[0]);
        path.lineTo(xPoints[1], yPoints[1]);
        path.lineTo(xPoints[2], yPoints[2]);
        path.lineTo(xPoints[3], yPoints[3]);
        path.close();
        return path;
    }

    private void desenhaSateliteInfo(Canvas canvas, float x, float y, int satelliteIndex) { //exibe informações sobre o satelite
        paint.setTextAlign(Paint.Align.LEFT);
        paint.setTextSize(30);
        paint.setColor(Color.RED);
        String satID = newStatus.getSvid(satelliteIndex) + "";
        String constelacao = getConstelacao(newStatus.getConstellationType(satelliteIndex));
        String usadoFix;
        if (newStatus.usedInFix(satelliteIndex)) {
            usadoFix = "Usado";
        } else {
            usadoFix = "Não usado";
        }
        float infoX = computarXc(x) + 20;
        float infoY = computarYc(y) + 10;
        float backgroundWidth = 250;
        float backgroundHeight = 100;
        paint.setColor(Color.argb(150, 0, 0, 0));
        canvas.drawRect(infoX, infoY - 10, infoX + backgroundWidth, infoY + backgroundHeight, paint); // desenha um retangulo para melhorar a legibilidade
        paint.setColor(Color.RED);
        canvas.drawText("ID: " + satID, infoX + 5, infoY + 25, paint);
        canvas.drawText("Constelação: " + constelacao, infoX + 5, infoY + 55, paint);
        canvas.drawText("Usado: " + usadoFix, infoX + 5, infoY + 85, paint);
    }

    private String getConstelacao(int tipoConstelacao) {
        switch (tipoConstelacao) {
            case GnssStatus.CONSTELLATION_GPS:
                return "GPS";
            case GnssStatus.CONSTELLATION_GALILEO:
                return "Galileo";
            case GnssStatus.CONSTELLATION_GLONASS:
                return "GLONASS";
            default:
                return "Desconhecido";
        }
    }

    private void mostrarDialog(Context context) {  // cria e mostra uma caixa de dialogo que permite o usuario configurar filtros de quais satelites devem ser exibidos
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Filtros de Satélites");
        builder.setMessage("Selecione os tipos de satélites que deseja visualizar:");
        LinearLayout layout = new LinearLayout(context);
        layout.setOrientation(LinearLayout.VERTICAL);

        CheckBox checkBoxGPS = new CheckBox(context);
        checkBoxGPS.setText("GPS");
        checkBoxGPS.setChecked(filtroGPS);
        layout.addView(checkBoxGPS);

        CheckBox checkBoxGalileo = new CheckBox(context);
        checkBoxGalileo.setText("Galileo");
        checkBoxGalileo.setChecked(filtroGalileo);
        layout.addView(checkBoxGalileo);

        CheckBox checkBoxGlonass = new CheckBox(context);
        checkBoxGlonass.setText("GLONASS");
        checkBoxGlonass.setChecked(filtroGlonass);
        layout.addView(checkBoxGlonass);

        CheckBox checkBoxUsado = new CheckBox(context);
        checkBoxUsado.setText("Usado em Fix");
        checkBoxUsado.setChecked(filtroUsado);
        layout.addView(checkBoxUsado);

        builder.setView(layout);

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                filtroGPS = checkBoxGPS.isChecked();
                filtroGalileo = checkBoxGalileo.isChecked();
                filtroGlonass = checkBoxGlonass.isChecked();
                filtroUsado = checkBoxUsado.isChecked();
                invalidate();
            }
        });

        builder.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.show();
    }

    private Bitmap desenharSateliteBitmap(int tipoConstelacao) {
        Bitmap bitmap = Bitmap.createBitmap(50, 50, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        Paint paint = new Paint();
        paint.setAntiAlias(true);

        switch (tipoConstelacao) {
            case GnssStatus.CONSTELLATION_GPS:
                paint.setColor(Color.YELLOW);
                canvas.drawCircle(25, 25, 20, paint);
                break;

            case GnssStatus.CONSTELLATION_GALILEO:
                paint.setColor(Color.BLUE);
                canvas.drawCircle(25, 25, 20, paint);
                break;

            case GnssStatus.CONSTELLATION_GLONASS:
                paint.setColor(Color.RED);
                canvas.drawRect(5, 5, 45, 45, paint);
                break;

            default:
                paint.setColor(Color.GRAY);
                canvas.drawRect(5, 5, 45, 45, paint);
                break;
        }

        return bitmap;
    }

    public void setGnssStatus(GnssStatus status) {
        this.newStatus = status;
        invalidate();
    }

    //convertem coordenadas cartesianas centradas na origem da esfera para a tela, onde o centro da esfera é o centro da tela
    private float computarXc(float x) {
        return width / 2 + x;
    }

    private float computarYc(float y) {
        return height / 2 - y;
    }
}
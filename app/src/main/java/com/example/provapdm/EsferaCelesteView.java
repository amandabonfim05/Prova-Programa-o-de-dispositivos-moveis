package com.example.provapdm;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.location.GnssStatus;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;

import java.util.Random;

public class EsferaCelesteView extends View {
    private GnssStatus newStatus;
    private Paint paint;
    private int r;
    private int height, width;
    private Random random;


    private boolean filtroGPS = true;
    private boolean filtroGalileo = true;
    private boolean filtroGlonass = true;
    private boolean filtroUsado = true;

    public EsferaCelesteView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        paint = new Paint();
        random = new Random();
        setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mostrarDialog(context);
            }
        });
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
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
                    desenhaSatelite(canvas, computarXc(x), computarYc(y));
                    desenhaSateliteInfo(canvas, x, y, i);
                }
            }
        }
    }

    private boolean deveDesenharSatelite(int satelliteIndex) {
        int tipoConstelacao = newStatus.getConstellationType(satelliteIndex);
        boolean usandoFix = newStatus.usedInFix(satelliteIndex);


        boolean ChecandoConstelacao = (tipoConstelacao == GnssStatus.CONSTELLATION_GPS && filtroGPS) ||
                (tipoConstelacao == GnssStatus.CONSTELLATION_GALILEO && filtroGalileo) ||
                (tipoConstelacao == GnssStatus.CONSTELLATION_GLONASS && filtroGlonass);


        boolean checandoFix = filtroUsado ? usandoFix : true;

        return ChecandoConstelacao && checandoFix;
    }

    private void desenharEstrelas(Canvas canvas) {
        paint.setColor(Color.WHITE);
        int numEstrelas = 100;
        for (int i = 0; i < numEstrelas; i++) {
            float x, y;
            do {
                x = random.nextFloat() * 2 * r - r;
                y = random.nextFloat() * 2 * r - r;
            } while (x * x + y * y > r * r);

            canvas.drawCircle(computarXc(x), computarYc(y), 2, paint);
        }
    }

    private void desenhaSatelite(Canvas canvas, float cx, float cy) {
        paint.setColor(Color.GRAY);
        canvas.drawRect(cx - 5, cy - 15, cx + 5, cy + 5, paint);

        paint.setColor(Color.YELLOW);
        canvas.drawRect(cx - 15, cy - 5, cx - 5, cy + 5, paint);
        canvas.drawRect(cx + 5, cy - 5, cx + 15, cy + 5, paint);

        paint.setColor(Color.BLUE);
        canvas.drawLine(cx, cy - 15, cx, cy - 30, paint);
    }

    private void desenhaSateliteInfo(Canvas canvas, float x, float y, int satelliteIndex) {
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
        canvas.drawRect(infoX, infoY - 10, infoX + backgroundWidth, infoY + backgroundHeight, paint);

        paint.setColor(Color.RED); // Cor do texto
        canvas.drawText("ID: " + satID, infoX + 5, infoY + 25, paint);
        canvas.drawText("Constelação: " + constelacao, infoX + 5, infoY + 55, paint);
        canvas.drawText("Usado: " + usadoFix, infoX + 5, infoY + 85, paint);
    }

    private String getConstelacao(int TipoConstelacao) {
        switch (TipoConstelacao) {
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

    private int computarXc(double x) {
        return (int) (x + width / 2);
    }

    private int computarYc(double y) {
        return (int) (-y + height / 2 - 400);
    }

    public void setNovoStatus(GnssStatus novoStatus) {
        this.newStatus = novoStatus;
        invalidate();
    }

    private void mostrarDialog(Context context) {
        String[] constelacoes = {"GPS", "Galileo", "GLONASS"};
        boolean[] checaItens = {filtroGPS, filtroGalileo, filtroGlonass};

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Filtrar Satélites")
                .setMultiChoiceItems(constelacoes, checaItens, new AlertDialog.OnMultiChoiceClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which, boolean clicado) {
                        switch (which) {
                            case 0:
                                filtroGPS = clicado;
                                break;
                            case 1:
                                filtroGalileo = clicado;
                                break;
                            case 2:
                                filtroGlonass = clicado;
                                break;
                        }
                    }
                })
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        filtroUsado  = true;
                        invalidate();
                    }
                })
                .setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .show();
    }
}

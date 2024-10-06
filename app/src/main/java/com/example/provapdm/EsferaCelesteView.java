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
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;

import java.util.ArrayList;
import java.util.Random;

public class EsferaCelesteView extends View {
    private GnssStatus newStatus;
    private Paint paint;
    private int r;
    private int height, width;
    private Random random;
    private ArrayList<Star> stars;

    private boolean filtroGPS = true;
    private boolean filtroGalileo = true;
    private boolean filtroGlonass = true;
    private boolean filtroUsado = true;

    private class Star {
        float x, y, speed;

        Star(float x, float y, float speed) {
            this.x = x;
            this.y = y;
            this.speed = speed;
        }

        void updatePosition() {
            y += speed;
            if (y > r) {
                // Reinicia a estrela na parte superior com nova velocidade
                y = -r;
                x = random.nextFloat() * 2 * r - r;
                speed = 0.5f + random.nextFloat();
            }
        }
    }

    public EsferaCelesteView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        paint = new Paint();
        random = new Random();
        stars = new ArrayList<>();

        for (int i = 0; i < 300; i++) {
            float angle = random.nextFloat() * 2 * (float) Math.PI;
            float radius = random.nextFloat() * r;
            float x = radius * (float) Math.cos(angle);
            float y = radius * (float) Math.sin(angle);
            float speed = 0.5f + random.nextFloat();
            stars.add(new Star(x, y, speed));
        }

        setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mostrarDialog(context);
            }
        });
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
                    desenhaSatelite(canvas, computarXc(x), computarYc(y), newStatus.getConstellationType(i));
                    desenhaSateliteInfo(canvas, x, y, i);
                }
            }
        }
        postInvalidateDelayed(30);
    }

    private void desenharEstrelas(Canvas canvas) {
        paint.setColor(Color.WHITE);
        for (Star star : stars) {
            if (Math.sqrt(star.x * star.x + star.y * star.y) <= r) {
                canvas.drawCircle(computarXc(star.x), computarYc(star.y), 2, paint);
            } else {
                float angle = random.nextFloat() * 2 * (float) Math.PI;
                float radius = random.nextFloat() * r;
                star.x = radius * (float) Math.cos(angle);
                star.y = radius * (float) Math.sin(angle);
            }
            star.updatePosition();
        }
    }

    private void desenhaSatelite(Canvas canvas, float cx, float cy, int tipoConstelacao) {
        switch (tipoConstelacao) {
            case GnssStatus.CONSTELLATION_GPS:
                paint.setColor(Color.YELLOW);
                desenharOctaedro(canvas, cx, cy);
                break;

            case GnssStatus.CONSTELLATION_GALILEO:
                paint.setColor(Color.BLUE);
                canvas.drawCircle(cx, cy, 15, paint);
                break;

            case GnssStatus.CONSTELLATION_GLONASS:
                paint.setColor(Color.RED);
                float[] diamondX = {cx, cx - 10, cx, cx + 10};
                float[] diamondY = {cy - 10, cy, cy + 10, cy};
                canvas.drawPath(createDiamondPath(diamondX, diamondY), paint);
                break;

            default:
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

    private Path createDiamondPath(float[] xPoints, float[] yPoints) {
        Path path = new Path();
        path.moveTo(xPoints[0], yPoints[0]);
        path.lineTo(xPoints[1], yPoints[1]);
        path.lineTo(xPoints[2], yPoints[2]);
        path.lineTo(xPoints[3], yPoints[3]);
        path.close();
        return path;
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

    private void mostrarDialog(Context context) {
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
                invalidate(); // Redesenhar a view
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

    private float computarXc(float x) {
        return width / 2 + x;
    }

    private float computarYc(float y) {
        return height / 2 - y;
    }
}

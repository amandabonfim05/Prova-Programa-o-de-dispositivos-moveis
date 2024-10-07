package com.example.provapdm;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.location.GnssStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.Manifest;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class GNSSA extends AppCompatActivity {
    private LocationManager locationManager;
    private LocationProvider locationProvider;
    private static final int REQUEST_LOCATION = 1;
    private int latitudeFormato = Location.FORMAT_SECONDS;
    private int longitudeFormato = Location.FORMAT_SECONDS;
    private TextView textViewLocalizacao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.esfera_celeste_layout);
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        textViewLocalizacao = findViewById(R.id.textviewLocation_id);

        textViewLocalizacao.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mostrarDialogoDeEscolha();
            }
        });
        obtemLocationProvider_Permission();
    }

    public void mostrarDialogoDeEscolha() {
        String[] formatos = {"Graus [+/-DDD.DDDDD]",
                "Graus-Minutos [+/-DDD:MM.MMMMM]",
                "Graus-Minutos-Segundos [+/-DDD:MM:SS.SSSSS]"};

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Escolha o formato para coordenadas:")
                .setSingleChoiceItems(formatos, -1, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int escolha) {
                        switch (escolha) {
                            case 0:
                                latitudeFormato = Location.FORMAT_DEGREES;
                                longitudeFormato = Location.FORMAT_DEGREES;
                                break;
                            case 1:
                                latitudeFormato = Location.FORMAT_MINUTES;
                                longitudeFormato = Location.FORMAT_MINUTES;
                                break;
                            case 2:
                                latitudeFormato = Location.FORMAT_SECONDS;
                                longitudeFormato = Location.FORMAT_SECONDS;
                                break;
                        }
                        dialog.dismiss();
                        if (ActivityCompat.checkSelfPermission(GNSSA.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                                ActivityCompat.checkSelfPermission(GNSSA.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                            return;
                        }

                        Location ultimaLocalizacao = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                        mostraLocation(ultimaLocalizacao);
                    }
                });
        builder.create().show();
    }

    public void obtemLocationProvider_Permission() {
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED) {
            locationProvider = locationManager.getProvider(LocationManager.GPS_PROVIDER);
            startLocationAndGNSSUpdates();
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_LOCATION);
        }
    }

    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_LOCATION) {
            if (grantResults.length == 1 && grantResults[0] ==
                    PackageManager.PERMISSION_GRANTED) {
                obtemLocationProvider_Permission();
            } else {
                Toast.makeText(this, "Sem permissão para acessar o sistema de posicionamento",
                        Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    public void startLocationAndGNSSUpdates() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        locationManager.requestLocationUpdates(locationProvider.getName(), 1000, 0.1f, new LocationListener() {
            @Override
            public void onLocationChanged(@NonNull Location location) {
                mostraLocation(location);
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {
                LocationListener.super.onStatusChanged(provider, status, extras);
            }
        });
        locationManager.registerGnssStatusCallback(new GnssStatus.Callback() {
            @Override
            public void onSatelliteStatusChanged(@NonNull GnssStatus status) {
                super.onSatelliteStatusChanged(status);
                EsferaCelesteView esferaCelesteView = findViewById(R.id.esferacelesteview_id);
                esferaCelesteView.setGnssStatus(status);
                mostraGNSSGrafico(status);
            }
        });

    }

    public void mostraGNSSGrafico(GnssStatus status) {
        ArrayList<String> sateliteIds = new ArrayList<>();
        ArrayList<Float> dadosQualidadeSinal = new ArrayList<>();
        int contagemSatelites = status.getSatelliteCount();
        for (int i = 0; i < contagemSatelites; i++) {
            int svid = status.getSvid(i);
            float qaulidadeSinal = status.getCn0DbHz(i);
            sateliteIds.add(String.valueOf(svid));
            dadosQualidadeSinal.add(qaulidadeSinal);
        }
        QualidadeSateliteView qualidadeSateliteView = findViewById(R.id.QualidadeSateliteView);
        qualidadeSateliteView.setSignalQualityData(sateliteIds, dadosQualidadeSinal);
    }

    public void mostraLocation(Location localizacao) {
        String dados = "Dados da Última posição\n";
        if (localizacao != null) {
            String latitudeSatelite = Location.convert(localizacao.getLatitude(), latitudeFormato);
            String longitudeSatelite = Location.convert(localizacao.getLongitude(), longitudeFormato);

            dados += "Latitude: " + latitudeSatelite + "\n"
                    + "Longitude: " + longitudeSatelite + "\n"
                    + "Velocidade (m/s): " + localizacao.getSpeed();

            Log.d("RumoView", "Direção definida: " + localizacao.getBearing());
            RumoView rumoView = findViewById(R.id.rumoView_id);
            rumoView.setDirecao(localizacao.getBearing());
        } else {
            dados += "Localização Não disponível";
        }
        textViewLocalizacao.setText(dados);
    }
}

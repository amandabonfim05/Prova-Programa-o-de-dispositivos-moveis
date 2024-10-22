package com.example.provapdm;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState); // ciclo de vida iniciado e configurado
        setContentView(R.layout.activity_main); // define o layout
        Button button_gnss=findViewById(R.id.button_gnss); // loc o botao e conecta o botao na interfacee grafica
        button_gnss.setOnClickListener(new View.OnClickListener() { // define o listener para o botao
            public void onClick(View view) {
                Intent i=new Intent(getApplicationContext(), GNSSA.class); // intent que obtem o contexto da aplicação e indica que a ativ a ser iniciada é a classe GNSSA
                startActivity(i);
            }
        });
    }
}
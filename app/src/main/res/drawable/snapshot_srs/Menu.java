package com.example.pull_system;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

 public class Menu extends Activity {
    private String cadenaConexion;

    private Button butPallet, butEmbarque, butRecibo, getButRetornar;
    private TextView textUsuario;
    private Usuario usuario ;

    @SuppressLint({"MissingInflatedId", "SetTextI18n"})
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.menu);
        iniciarElementos();
        Intent intent =  getIntent();

        usuario =(Usuario) intent.getSerializableExtra("Usuario");
       cadenaConexion = intent.getStringExtra("cadenaCon");
        textUsuario.setText("Bienvenido:" + usuario.getNombre());
       butPallet.setOnClickListener(view -> {
           try {
               Intent intent2 = new Intent(Menu.this, Pallet.class);
               intent2.putExtra("Usuario", usuario);
               intent2.putExtra("cadenaCon",cadenaConexion);
               startActivity(intent2);
           }catch (Exception ex){
               Log.e("Error", ex.getMessage(), ex);
             mensaje("Error" + ex.getMessage());
           }
       });
        butEmbarque.setOnClickListener(view -> {
            try {
                Intent intent2 = new Intent(Menu.this, Embarque.class);
                intent2.putExtra("Usuario", usuario);
                intent2.putExtra("cadenaCon",cadenaConexion);
                startActivity(intent2);
            }catch (Exception ex){
                Log.e("Error", ex.getMessage(), ex);
                mensaje("Error" + ex.getMessage());
            }
        });
        butRecibo.setOnClickListener(view -> {
            try {
                Intent intent2 = new Intent(Menu.this, Recibo.class);
                intent2.putExtra("Usuario", usuario);
                intent2.putExtra("cadenaCon",cadenaConexion);
                startActivity(intent2);
            }catch (Exception ex){
                Log.e("Error", ex.getMessage(), ex);
                mensaje("Error" + ex.getMessage());
            }
        });
        getButRetornar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent2 = new Intent(Menu.this, Retornar.class);
                intent2.putExtra("Usuario", usuario);
                intent2.putExtra("cadenaCon",cadenaConexion);
                startActivity(intent2);
            }
        });

    }

    public void mensaje(String mensaje) {


        AlertDialog.Builder dlgAlert = new AlertDialog.Builder(this);
        dlgAlert.setMessage(mensaje);
        dlgAlert.setTitle("Snapshot SRS");
        dlgAlert.create().show();

    }


    private void iniciarElementos(){
        butPallet = findViewById(R.id.btnEnvio);
        textUsuario = findViewById(R.id.textUsuario2);
        butEmbarque = findViewById(R.id.btnEmbarque);
        butRecibo =findViewById(R.id.btnRecibo);
        getButRetornar = findViewById(R.id.btnRetorno);
    }
}

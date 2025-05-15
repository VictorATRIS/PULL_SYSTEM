package com.example.pull_system;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class Menu extends Activity {
private  Usuario usuario;
public String cadenaConexion;
Button btnRecibo, btnEnvio, btnInventario;
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.menu);
        Intent intent =  getIntent();
       iniciarElementos();
        usuario =(Usuario) intent.getSerializableExtra("Usuario");
        cadenaConexion = intent.getStringExtra("cadenaCon");
       bloqueaBotones();

        assert usuario != null;





        btnRecibo.setOnClickListener(new View.OnClickListener() {
            Intent intent2 = null;
            public void onClick(View view) {
                intent2 = new Intent(Menu.this, Recibo.class);
                intent2.putExtra("Usuario", usuario);
                intent2.putExtra("cadenaCon",cadenaConexion);
                startActivity(intent2);
            }
        });
        btnEnvio.setOnClickListener(new View.OnClickListener() {
            Intent intent2 = null;
            public void onClick(View view) {
                intent2 = new Intent(Menu.this, MenuNoPart.class);
                intent2.putExtra("Usuario", usuario);
                intent2.putExtra("cadenaCon",cadenaConexion);
                startActivity(intent2);
            }
        });
        btnInventario.setOnClickListener(new View.OnClickListener() {
            Intent intent2 = null;
            public void onClick(View view) {
                intent2 = new Intent(Menu.this, Inventario.class);
                intent2.putExtra("Usuario", usuario);
                intent2.putExtra("cadenaCon",cadenaConexion);
                startActivity(intent2);
            }
        });

    }
    public void mensaje(String mensaje) {


        AlertDialog.Builder dlgAlert = new AlertDialog.Builder(this);
        dlgAlert.setMessage(mensaje);
        dlgAlert.setTitle("Pull System");
        dlgAlert.create().show();

    }
    private void iniciarElementos(){
        btnRecibo = findViewById(R.id.btnEnvio);
        btnEnvio = findViewById(R.id.btnEmbarque);
        btnInventario = findViewById(R.id.btnRetorno);

    }

    private void bloqueaBotones (){
        switch (usuario.getUsuarioGrupo()) {
            case "1":
                btnRecibo.setEnabled(true);
                btnEnvio.setEnabled(true);
                btnInventario.setEnabled(true);
                break;
            case "3":
                btnRecibo.setEnabled(false);
                btnEnvio.setEnabled(true);
                btnInventario.setEnabled(false);
                break;
            case "2":
                btnRecibo.setEnabled(true);
                btnEnvio.setEnabled(false);
                btnInventario.setEnabled(true);
                break;
        }

    }

}

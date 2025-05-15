package com.example.pull_system;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.provider.Settings;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.SQLException;

import java.io.OutputStream;
import java.io.OutputStream;
import java.net.Socket;



public class MainLogin extends Activity {
    private Button btn1 , btnImprimir;
    private Conexion conexion ;
    private Usuario usuario;
    private TextView textUsuario;
    private TextView textContrasena;
    private String cadenaConexion;
    String impresoraIP = "172.30.74.49";
    String comandosIPL =
            "<STX><ESC>C<ETX>\n" +
                    "<STX><ESC>P<ETX>\n" +
                    "<STX>E4;F4<ETX>\n" +
                    "<STX>H0;o200,100;f0;c25;h40;w40;d0,30<ETX>\n" +
                    "<STX>L1;o200,200;f0;l800;w5<ETX>\n" +
                    "<STX>L2;o200,250;f0;l800;w5<ETX>\n" +
                    "<STX>H3;o200,300;f0;c25;h15;w20;d3,SET:<ETX>\n" +
                    "<STX>H4;o600,300;f0;c25;h15;w20;d0,30<ETX>\n" +
                    "<STX>H5;o200,350;f0;c25;h15;w20;d3,TOTAL BOX:<ETX>\n" +
                    "<STX>H6;o600,350;f0;c25;h15;w20;d0,30<ETX>\n" +
                    "<STX>H7;o200,400;f0;c25;h15;w20;d3,TOTAL PALLET:<ETX>\n" +
                    "<STX>H8;o600,400;f0;c25;h15;w20;d0,30<ETX>\n" +
                    "<STX>L9;o200,450;f0;l800;w5<ETX>\n" +
                    "<STX>L10;o200,500;f0;l800;w5<ETX>\n" +
                    "<STX>H11;o200,550;f0;c25;h15;w20;d3,Folio:<ETX>\n" +
                    "<STX>H12;o600,550;f0;c25;h15;w20;d0,30<ETX>\n" +
                    "<STX>H13;o200,600;f0;c25;h15;w20;d3,ETD ATR:<ETX>\n" +
                    "<STX>H14;o600,600;f0;c25;h15;w20;d0,30<ETX>\n" +
                    "<STX>H15;o200,650;f0;c25;h15;w20;d3,ETD TRC:<ETX>\n" +
                    "<STX>H16;o600,650;f0;c25;h15;w20;d0,30<ETX>\n" +
                    "<STX>H17;o200,700;f0;c25;h15;w20;d3,ETA DCSC:<ETX>\n" +
                    "<STX>H18;o600,700;f0;c25;h15;w20;d0,30<ETX>\n" +
                    "<STX>R<ETX>\n" +
                    "<STX><ESC>E4<ETX>\n" +
                    "<STX><CAN><ETX>\n" +
                    "<STX>@part_no<CR><ETX>\n" +
                    "<STX>@qty<CR><ETX>\n" +
                    "<STX>@artesas<CR><ETX>\n" +
                    "<STX>@pallet<CR><ETX>\n" +
                    "<STX>@folio<CR><ETX>\n" +
                    "<STX>@etd_atr<CR><ETX>\n" +
                    "<STX>@etd_tdc<CR><ETX>\n" +
                    "<STX>@eta_dcsc<CR><ETX>\n" +
                    "<STX><ETB><ETX>" ;

    ImpresoraHoneywell impresora = new ImpresoraHoneywell(impresoraIP);

  /*  private void imprime() {
        String formato;

        formato = comandosIPL.replace("@NOPART", )
        formato = comandosIPL.replace("@QTY", )
        formato = comandosIPL.replace("@", )
        formato = comandosIPL.replace("@NOPART", )
        formato = comandosIPL.replace("@NOPART", )
        formato = comandosIPL.replace("@NOPART", )
        formato = comandosIPL.replace("@NOPART", )
    }
*/
    @SuppressLint("MissingInflatedId")
    protected void onCreate(Bundle savedInstanceState) {
     super.onCreate(savedInstanceState);
        setContentView(R.layout.login);
        btn1 = findViewById(R.id.butIngresar); //Hacemos referencia al boton del activity
        usuario = new Usuario();
        btnImprimir = findViewById(R.id.butPrint);
        btnImprimir.setVisibility(View.GONE);
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        /*
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (!Environment.isExternalStorageManager()) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION,
                        Uri.parse("package:" + getPackageName()));
                startActivity(intent);
            }
        }else {

        }*/
        //En esta variable esta la cadena que usaremos para la conexion a la base de datos
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        } else {
            checkConfig();
        }




        conexion = new Conexion(cadenaConexion,this);
        btnImprimir.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        boolean resultado = impresora.imprimirIPL(comandosIPL);
                        if (resultado) {
                            // La impresión fue exitosa (puedes actualizar la UI aquí)
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                   conexion.mensaje("Se imprimio con exito", "Jjajaja");
                                }
                            });
                        } else {
                            // Hubo un error en la impresión (puedes actualizar la UI aquí)
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    conexion.mensaje("No imprimio con exito", "Jjajaja");
                                }
                            });
                        }
                    }
                }).start();

            }
        });


        //Hacemos la referencia a los campos de texto del activity
        textUsuario = findViewById(R.id.textUsuario2);
        textContrasena = findViewById(R.id.textContrasena);
        //textContrasena.setText("0");
       // textUsuario.setText("Victor.avalos");
        btn1.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //Checamos que ingrese usuario y contrasena
                if(!textUsuario.getText().toString().trim().isEmpty() && !textUsuario.getText().toString().trim().isEmpty() ){
                    //Seteamos lo que hay en los campos de texto al objeto usuario
                    usuario.setUsuario(textUsuario.getText().toString().trim());
                    usuario.setPassword(textContrasena.getText().toString());
                    //Validamos que el usuario  y contrasena sean correctos
                    try {
                        if(conexion.validaUsuario(usuario)){



                                Intent intent = null;
                                try {
                                    //Ya cuando el usuario sea correcto abrimos el siguiente activity y le enviamos el usuario y nombre del usuario
                                    try {
                                        intent = new Intent(MainLogin.this, Menu.class);
                                    }catch (Exception ex){
                                        ex.printStackTrace();
                                    }
                                    textUsuario.setText("");
                                    textContrasena.setText("");
                                    textUsuario.requestFocus();
                                    intent.putExtra("Usuario", usuario);
                                    intent.putExtra("cadenaCon",cadenaConexion);

                                    startActivity(intent);
                                } catch (Exception ex) {
                                    ex.printStackTrace();
                                }

                        }else {
                            conexion.mensaje("Usuario incorrecto","Pull System");
                        }
                    } catch (SQLException e) {
                        conexion.mensaje(e.getMessage(),"Pull System");
                    }
                }else {
                    conexion.mensaje("Ingrese usuario y contrasena ","Pull System");
                }
            }
        });





    }
    public void hideKeyboard(View view) {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        View view = getCurrentFocus();
        if (view != null ) {
            hideKeyboard(view);
        }
        return super.dispatchTouchEvent(ev);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResult) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResult);
        checkConfig();
    }
    public void checkConfig() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && !Environment.isExternalStorageManager()) {

            Uri uri = Uri.parse("package:" + "com.example.snapshot_srs");
            startActivity(new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION, uri));
        }
        String dir = Environment.getExternalStorageDirectory() + "/ATR-APPS/";
        File path = new File(dir);

        try {
            if (!path.exists()) {
                if (!path.mkdirs()) {
                    showToastMsg("Error al crear el directorio");
                }
            }
            File file = new File(path.getAbsolutePath(), "configPullSystem.txt");
            if (!file.exists()) {
                AlertDialog mBuilder = new AlertDialog.Builder(this)
                        .setTitle("Pull System")
                        .setMessage("Ingrese el archivo de configuracion antes de continuar.")
                        .setPositiveButton("Ok", null)
                        .show();

                Button mPositiveButton = mBuilder.getButton(AlertDialog.BUTTON_POSITIVE);
                mPositiveButton.setOnClickListener(v -> finish());
            }
        } catch (Exception e) {
            showToastMsg(e.getMessage());
        }

        File configFile = new File(Environment.getExternalStorageDirectory() + "/ATR-APPS/configPullSystem.txt");
        try {
            BufferedReader br = new BufferedReader(new FileReader(configFile));
            String line;

            while ((line = br.readLine()) != null) {
                if (line.equals("CadenaConexion")) {
                    cadenaConexion = br.readLine();

                }


            }
        } catch (IOException e) {
            showToastMsg(e.getMessage());
        }
    }
    private void showToastMsg(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }




}




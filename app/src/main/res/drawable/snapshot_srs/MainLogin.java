package com.example.snapshot_srs;

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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.SQLException;

public class MainLogin extends Activity {
    //Creamos los objetos a los que vamos hacer referencia
   private Button btn1 ;
   private Conexion conexion ;
    private Usuario usuario;
    private TextView textUsuario;
    private TextView textContrasena;
    private String cadenaConexion,cadenaConexionFGSS;
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);
        btn1 = findViewById(R.id.butIngresar); //Hacemos referencia al boton del activity
        usuario = new Usuario();
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
       // cadenaConexion = "jdbc:jtds:sqlserver://172.30.75.22/ERP;user=localapps;password=L0c@lapp;";
        conexion = new Conexion(cadenaConexion,this);



        //Hacemos la referencia a los campos de texto del activity
        textUsuario = findViewById(R.id.textUsuario2);
        textContrasena = findViewById(R.id.textContrasena);

        //Hacemos el evento on click para cuando presione el boton del activity
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
                         if(conexion.validateUserAccess(usuario.getUsuarioGrupo())){


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
                             intent.putExtra("cadenaConFGSS",cadenaConexionFGSS);


                            startActivity(intent);
                         } catch (Exception ex) {
                             ex.printStackTrace();
                         }
                         }else
                         {
                             conexion.mensaje("No tienes Acceso a esta aplicacion","SNAPSHOT SRS");
                         }
                     }else {
                         conexion.mensaje("Usuario incorrecto","SNAPSHOT SRS");
                     }
                 } catch (SQLException e) {
                     conexion.mensaje(e.getMessage(),"SNAPSHOT SRS");
                 }
             }else {
                 conexion.mensaje("Ingrese usuario y contrasena ","SNAPSHOT SRS");
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
    // Este metodo lo estamos usando para leer el archivo de configuracion
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
            File file = new File(path.getAbsolutePath(), "configEMB.txt");
            if (!file.exists()) {
                AlertDialog mBuilder = new AlertDialog.Builder(this)
                        .setTitle("SNAPSHOT SRS")
                        .setMessage("Ingrese el archivo de configuracion antes de continuar.")
                        .setPositiveButton("Ok", null)
                        .show();

                Button mPositiveButton = mBuilder.getButton(AlertDialog.BUTTON_POSITIVE);
                mPositiveButton.setOnClickListener(v -> finish());
            }
        } catch (Exception e) {
            showToastMsg(e.getMessage());
        }

        File configFile = new File(Environment.getExternalStorageDirectory() + "/ATR-APPS/configEMB.txt");
        try {
            BufferedReader br = new BufferedReader(new FileReader(configFile));
            String line;

            while ((line = br.readLine()) != null) {
                if (line.equals("CadenaConexion")) {
                    cadenaConexion = br.readLine();

                }
                if (line.equals("CadenaConexionFGSS")) {
                    cadenaConexionFGSS = br.readLine();
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

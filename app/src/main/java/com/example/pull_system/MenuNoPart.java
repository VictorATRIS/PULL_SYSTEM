package com.example.pull_system;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import java.sql.SQLException;
import java.util.ArrayList;

public class MenuNoPart extends Activity {
    public String cadenaConexion;
   public  Usuario usuario;
   Conexion conexion;
   TextView textFecha ;
     TableLayout tableLayout  ;
    ArrayList<DailyOrder> orders = new ArrayList<>();
    ImpresoraHoneywell impresora;
    String ip;
    int puerto;
    @SuppressLint("SetTextI18n")
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.menunopart);
        Intent intent =  getIntent();
        usuario =(Usuario) intent.getSerializableExtra("Usuario");
        cadenaConexion = intent.getStringExtra("cadenaCon");
        conexion = new Conexion(cadenaConexion);
        iniciarElementos();


        try {
            textFecha.setText("Date: " + conexion.GetFechaActual());
            orders = conexion.getDailyOrder();
            vaciarDatosTabla();
            ArrayList <String> datosImpresora =   conexion.getInfoPrinter();
            ip = datosImpresora.get(0);
            puerto = Integer.parseInt( datosImpresora.get(1));

        } catch (SQLException e) {
            mensaje(e.getMessage());
        }
        impresora = new ImpresoraHoneywell(ip,puerto);
    }
    private void iniciarElementos(){

        textFecha = findViewById(R.id.tvFechaHoy);
        tableLayout = findViewById(R.id.tableNoParts);


    }

    private void vaciarDatosTabla() {
        tableLayout.removeAllViews();
        TableRow headerRow = new TableRow(this);

        TextView headerPartNo = new TextView(this);
        headerPartNo.setText("Part No");
        headerPartNo.setTypeface(Typeface.DEFAULT_BOLD);
        headerPartNo.setPadding(12, 12, 12, 12);
        headerPartNo.setLayoutParams(new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 2));

        TextView headerArtesas = new TextView(this);
        headerArtesas.setText("Progress");
        headerArtesas.setTypeface(Typeface.DEFAULT_BOLD);
        headerArtesas.setPadding(12, 12, 12, 12);
        headerArtesas.setLayoutParams(new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1));

        TextView headerPrint = new TextView(this);
        headerPrint.setText("Print");
        headerPrint.setTypeface(Typeface.DEFAULT_BOLD);
        headerPrint.setPadding(12, 12, 12, 12);
        headerPrint.setLayoutParams(new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1));

        headerRow.addView(headerPartNo);
        headerRow.addView(headerArtesas);
        headerRow.addView(headerPrint);

        tableLayout.addView(headerRow);

        for (DailyOrder order : orders) {
            TableRow row = new TableRow(this);

            TextView partNo = new TextView(this);
            partNo.setText(order.getPartNo());
            partNo.setPadding(12, 12, 12, 12);
            partNo.setLayoutParams(new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 2));

            TextView artesas = new TextView(this);
            artesas.setText(order.getArtesas());
            artesas.setPadding(12, 12, 12, 12);
            artesas.setLayoutParams(new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1));

            row.addView(partNo);
            row.addView(artesas);


            row.setOnClickListener(v -> {
               // mensaje(order.getPartNo());

                Intent intent = new Intent(this, Scaneeo.class);
                intent.putExtra("Usuario", usuario);
                intent.putExtra("cadenaCon",cadenaConexion);
                intent.putExtra("part_no", order.getPartNo());
                startActivity(intent);


            });


            if (Integer.parseInt(order.getArtesas().split("/")[0]) >= Integer.parseInt(order.getArtesas().split("/")[1])) {
                Button printButton = new Button(this);
                printButton.setPadding(12, 12, 12, 12);
                printButton.setLayoutParams(new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1));

                printButton.setText("Print");
                printButton.setOnClickListener(v -> {
                    imprime(order.getPartNo());
                });
                row.addView(printButton);

                //row.addView(printButton);
                row.setOnClickListener(null);
            } else {
                TextView emptyCell = new TextView(this);
                emptyCell.setLayoutParams(new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1));
                row.addView(emptyCell);
            }

            tableLayout.addView(row);
        }
    }
    public void mensaje(String mensaje) {


        AlertDialog.Builder dlgAlert = new AlertDialog.Builder(this);
        dlgAlert.setMessage(mensaje);
        dlgAlert.setTitle("Pull System");
        dlgAlert.create().show();

    }
    protected void onResume() {
        super.onResume();
        try {
            textFecha.setText("Date: " + conexion.GetFechaActual());
            orders = conexion.getDailyOrder();
            vaciarDatosTabla();

        } catch (SQLException e) {
            mensaje(e.getMessage());
        }
    }
    private void imprime(String part_no) {
        try {
            ArrayList<String> datos = conexion.setInfoPrinter(part_no);
            if (datos.size() > 0) {
                for (int i = 0; i < datos.size(); i++) { // Corrección en la sintaxis del bucle
                    impresora.imprimirIPL(datos.get(i));
                }
            }

        }catch (Exception ex) {

            mensaje(ex.getMessage());
        }


    }

}

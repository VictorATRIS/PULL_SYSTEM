package com.example.snapshot_srs;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.text.InputType;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.symbol.emdk.EMDKManager;
import com.symbol.emdk.EMDKResults;
import com.symbol.emdk.barcode.BarcodeManager;
import com.symbol.emdk.barcode.ScanDataCollection;
import com.symbol.emdk.barcode.Scanner;
import com.symbol.emdk.barcode.ScannerException;
import com.symbol.emdk.barcode.ScannerResults;
import com.symbol.emdk.barcode.StatusData;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class Retornar extends AppCompatActivity implements EMDKManager.EMDKListener, Scanner.StatusListener, Scanner.DataListener{
    private EMDKManager emdkManager = null;
    private BarcodeManager barcodeManager = null;
    private Scanner scanner = null;
    private EditText editPallet, editTroka;
    private TableLayout tableLayout  ;
    private Conexion conexion;
    private Usuario usuario;
    private String cadenaConexion ;
    private TextView textTotal;
    // Inicializamos un Set para almacenar las cajas que ya se han agregado
    private Set<String> cajasAgregadas = new HashSet<>();
    private Button butCerrarPallet;
    private static final String TAG = "IntentApiSample";
    protected void onCreate(Bundle savedInstanceState) {
  super.onCreate(savedInstanceState);
        setContentView(R.layout.retornar);
        iniciarElementos();
        Intent intent =  getIntent();
        usuario =(Usuario) intent.getSerializableExtra("Usuario");
        cadenaConexion = intent.getStringExtra("cadenaCon");
        conexion = new Conexion(cadenaConexion);
        try {
            EMDKResults results = EMDKManager.getEMDKManager(getApplicationContext(), this);
            if (results.statusCode != EMDKResults.STATUS_CODE.SUCCESS) {
                return;
            }
        } catch (Exception ex) {
            mensaje(ex.getMessage());
        }
        editPallet.setInputType(InputType.TYPE_NULL);
        butCerrarPallet.setEnabled(false);
        butCerrarPallet.setBackgroundColor(Color.rgb(198, 197, 196));


        butCerrarPallet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (editPallet.getText().toString().isEmpty() || editTroka.getText().toString().isEmpty()){
                    mensaje("Ingresa el pallet y la troka para poder retornar");
                    return;
                }
                if (tableLayout.getChildCount() < 2) {
                    mensaje("No puedes retornar un pallet vacio");
                    return;
                }
                // Mostrar un cuadro de diálogo para confirmar la acción
                new AlertDialog.Builder(view.getContext())
                        .setTitle("Confirmación")
                        .setMessage("¿Estás seguro de que quieres retornar este Pallet?")
                        .setPositiveButton("Sí", (dialog, which) -> {
                            try {
                                if (conexion.cerrarPalletRetorno(editPallet.getText().toString().trim())) {
                                    limpiaTabla();
                                    editPallet.setText("");
                                    butCerrarPallet.setEnabled(false);
                                    butCerrarPallet.setBackgroundColor(Color.rgb(198, 197, 196));
                                    editPallet.invalidate();
                                    mensaje("Pallet Cerrado con éxito");
                                }
                            } catch (SQLException e) {
                                mensaje(e.getMessage());
                            }
                        })
                        .setNegativeButton("No", (dialog, which) -> {
                            dialog.dismiss();
                        })
                        .show();
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
    private void iniciarElementos(){
        editPallet = findViewById(R.id.etScanPallet);
        tableLayout = findViewById(R.id.tableArtesas);
        textTotal = findViewById(R.id.tvTotal);
        butCerrarPallet = findViewById(R.id.btnRetornar);
        editTroka = findViewById(R.id.etScanTroka);

    }
    public void mensaje(String mensaje) {


        AlertDialog.Builder dlgAlert = new AlertDialog.Builder(this);
        dlgAlert.setMessage(mensaje);
        dlgAlert.setTitle("Snapshot SRS");
        dlgAlert.create().show();

    }
    public void onClosed() {
        if (this.emdkManager != null) {
            this.emdkManager.release();
            this.emdkManager = null;
        }
    }

    @Override
    public void onOpened(EMDKManager emdkManager) {
        this.emdkManager = emdkManager;
        initBarcodeManager();
        initScanner();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (emdkManager != null) {
            emdkManager.release();
            emdkManager = null;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (emdkManager != null) {
            barcodeManager = (BarcodeManager) emdkManager.getInstance(EMDKManager.FEATURE_TYPE.BARCODE);
            initScanner();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onData(ScanDataCollection scanDataCollection) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    scanner.disable();


                    if ((scanDataCollection != null) && (scanDataCollection.getResult() == ScannerResults.SUCCESS)) {
                        ArrayList<ScanDataCollection.ScanData> scanData = scanDataCollection.getScanData();
                        for (ScanDataCollection.ScanData data : scanData) {
                            String scaneado = data.getData();
                            validaEscaneo(scaneado);

                        }
                    }
                    scanner.enable();
                } catch (Exception ex) {
                    mensaje(ex.getMessage());
                }
            }
        });


    }

    private void generaRow(String caja, CajaInfo cajaInfo){
        try {

            TableRow newRow = new TableRow(Retornar.this);

            TextView textView = new TextView(Retornar.this);
            textView.setText(caja);
            textView.setPadding(3, 16, 16, 16);

            TextView textView2 = new TextView(Retornar.this);
            textView2.setText(cajaInfo.product_no);
            textView2.setPadding(6, 16, 16, 16);

            TextView textView3 = new TextView(Retornar.this);
            textView3.setText(cajaInfo.qty + "");
            textView3.setPadding(9, 16, 16, 16);

            // Crear un botón de eliminación
            Button deleteButton = new Button(Retornar.this);
            deleteButton.setText("Eliminar");
            deleteButton.setOnClickListener(v -> {
                tableLayout.removeView(newRow); // Eliminar la fila
                cajasAgregadas.remove(caja); // Eliminar el ID del conjunto
                try {
                    if (conexion.eliminarCajaPorSerialRetorno(cajaInfo.caja)) {
                        mensaje("Caja eliminada con exito");
                    }
                } catch (SQLException e) {
                    mensaje(e.getMessage());
                }
                // Actualizar el total en tvTotal
                int total = Integer.parseInt(textTotal.getText().toString().split(":")[1].trim()); // Obtener el valor actual
                total -= cajaInfo.qty; // Sumar la cantidad actual
                textTotal.setText("Total:" + String.valueOf(total)); // Actualizar el valor en tvTotal
            });

            // Agregar las TextViews y el botón al TableRow
            newRow.addView(textView);
            newRow.addView(textView2);
            newRow.addView(textView3);
            newRow.addView(deleteButton);
            // Agregar el TableRow al TableLayout
            tableLayout.addView(newRow);

            // Actualizar el total en tvTotal

            int total = Integer.parseInt(textTotal.getText().toString().split(":")[1].trim()); // Obtener el valor actual
            total += cajaInfo.qty; // Sumar la cantidad actual
            textTotal.setText("Total:" + String.valueOf(total)); // Actualizar el valor en tvTotal
        } catch (Exception ex) {
            mensaje(ex.getMessage());
        }

    }

    private void getDatosPallet (String pallet) throws ScannerException {
        try {
            ArrayList<CajaInfo> cajas = conexion.getCajasPorPalletRetorno(pallet);
            for (CajaInfo cajaInfo : cajas){

                generaRow(cajaInfo.caja,cajaInfo);
            }
        }catch (Exception ex) {
            scanner.enable();
            mensaje(ex.getMessage());
        }
    }
    private void validaEscaneo(String scaneado) throws ScannerException {
        try{
            if (conexion.existeDato("select * from ERP_CTRL_SRS_PALLETS where pallet = '" + scaneado.trim() + "' and status = '-1' ")){
                mensaje("Pallet Cerrado.");
                limpiaTabla();
                return;
            }
            if (conexion.existeDato("select * from ERP_CTRL_SRS_PALLETS where pallet = '" + scaneado.trim() + "'")){
                editPallet.setText(scaneado);
                editPallet.setBackgroundColor(Color.GREEN);
                butCerrarPallet.setEnabled(true);
                butCerrarPallet.setBackgroundColor(Color.rgb(54, 71, 166));
                limpiaTabla();

                getDatosPallet(scaneado.trim());
                return;
            }

            String[] validValues = {"CUE", "SAU", "GPL", "SNP", "HBG", "HA", "CME", "FIM", "MAZ", "RIO"};

            String planta = scaneado.substring(0, 3);



            for (String validValue : validValues) {
                if (planta.equalsIgnoreCase(validValue)) {
                    // Coincidencia encontrada, establecer valor en editPallet
                    editPallet.setText(scaneado);
                    // Cambiar el fondo del EditText a verde
                    editPallet.setBackgroundColor(Color.GREEN);
                    limpiaTabla();
                    //Insertamos el Pallet
                    conexion.insertPallet(scaneado,"0",planta, usuario.getUsuarioNick());
                    butCerrarPallet.setEnabled(true);
                    butCerrarPallet.setBackgroundColor(Color.rgb(54, 71, 166));
                    return;
                }
            }

            if (editPallet.getText().toString().trim().equalsIgnoreCase("")){
                mensaje("Ingresa El No. Pallet");
                return;
            }
            if (conexion.existeDato("select * from ERP_CTRL_SRS_Artesas where box_serial = '" + scaneado.trim() + "' and status = '1'")){
                mensaje("Artesa ya registrada");
                return;
            }
            // Obtener la información de la caja
            CajaInfo cajaInfo = conexion.getCajaInfoRetorno(scaneado);
            if (cajaInfo.product_no.trim().isEmpty()){
                mensaje("No. de caja no existe. Nunca Ha Salido de Planta Origen");
                return;
            }

            conexion.insertArtesas(editPallet.getText().toString().trim(),scaneado.trim(),cajaInfo.product_no.trim(),cajaInfo.dl.trim(),cajaInfo.qty, "1", usuario.getUsuarioNick());
            cajasAgregadas.add(scaneado);

            generaRow(scaneado,cajaInfo);

        }catch (Exception ex){
            scanner.enable();
            mensaje(ex.getMessage());
        }

    }
    private void limpiaTabla (){

        int numeroDeTitulos = 1;

        while (tableLayout.getChildCount() > numeroDeTitulos) {
            tableLayout.removeViewAt(numeroDeTitulos);
        }
        textTotal.setText("Total:0");

    }
    public void onStatus(StatusData statusData) {
        StatusData.ScannerStates state = statusData.getState();
        if (state == StatusData.ScannerStates.IDLE) {
            try {
                scanner.read();
            } catch (ScannerException ignored) {
            }
        }
    }

    private void initBarcodeManager() {
        barcodeManager = (BarcodeManager) emdkManager.getInstance(EMDKManager.FEATURE_TYPE.BARCODE);
        if (barcodeManager == null) {
            Toast.makeText(this, "Barcode scanning is not supported.", Toast.LENGTH_LONG).show();
            finish();
        }
    }

    private void initScanner() {
        if (scanner == null) {
            scanner = barcodeManager.getDevice(BarcodeManager.DeviceIdentifier.DEFAULT);
            if (scanner != null) {
                scanner.addDataListener(this);
                scanner.addStatusListener(this);
                scanner.triggerType = com.symbol.emdk.barcode.Scanner.TriggerType.HARD;
                try {
                    scanner.enable();

                } catch (ScannerException e) {
                    deInitScanner();
                }
            }
        }
    }

    private void deInitScanner() {
        if (scanner != null) {
            try {
                // Release the scanner
                scanner.release();
            } catch (Exception ignored) {
            }
            scanner = null;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (emdkManager != null) {
            emdkManager.release();
            emdkManager = null;
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        try {
            if (scanner != null) {
                scanner.removeDataListener(this);
                scanner.removeStatusListener(this);
                scanner.disable();
                scanner = null;
            }
        } catch (ScannerException e) {
            e.printStackTrace();
        }
    }
}

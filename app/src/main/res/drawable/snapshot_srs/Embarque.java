package com.example.snapshot_srs;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.KeyEvent;
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

public class Embarque extends AppCompatActivity implements EMDKManager.EMDKListener, Scanner.StatusListener, Scanner.DataListener {
    private EMDKManager emdkManager = null;
    private BarcodeManager barcodeManager = null;
    private Scanner scanner = null;
    private EditText editTroka;
    private TableLayout tableLayout  ;
    private Conexion conexion;
    private Usuario usuario;
    private String cadenaConexion ;
    private TextView textTotal;


    private Set<String> cajasAgregadas = new HashSet<>();
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.embarque);
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
        editTroka.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_ENTER) {

                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(editTroka.getWindowToken(), 0);
                    try {
                        limpiaTabla();
                        getDatosPorTroka(editTroka.getText().toString().trim());
                    } catch (ScannerException e) {
                        mensaje(e.getMessage());
                    }
                    return true;
                }
                return false;
            }
        });
    }
   /* public void hideKeyboard(View view) {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            mensaje("Holaaaaaaa");
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        View view = getCurrentFocus();
        if (view != null ) {
            hideKeyboard(view);

        }
        return super.dispatchTouchEvent(ev);
    }*/


    private void getDatosPorTroka (String pallet) throws ScannerException {
        try {
            ArrayList<CajaInfo> cajas = conexion.getPalletsPorTroka(pallet);
            for (CajaInfo cajaInfo : cajas){

                generaRow(cajaInfo.pallet,cajaInfo);
            }
        }catch (Exception ex) {
            scanner.enable();
            mensaje(ex.getMessage());
        }
    }
    private void iniciarElementos(){
        editTroka = findViewById(R.id.etScanTroka);
        tableLayout = findViewById(R.id.tableArtesas);
        textTotal = findViewById(R.id.tvTotal);
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

    private void limpiaTabla (){

        int numeroDeTitulos = 1;

        while (tableLayout.getChildCount() > numeroDeTitulos) {
            tableLayout.removeViewAt(numeroDeTitulos);
        }
        textTotal.setText("Total:0");

    }
    private void validaEscaneo(String scaneado){
        try {


        if(editTroka.getText().toString().isEmpty()){
            mensaje("Ingresa el No. de Troka");
            return;
        }
            if ( !conexion.existeDato("select * from ERP_CTRL_SRS_PALLETS where pallet = '" + scaneado.trim() + "'")){
                mensaje("Este Pallet no existe");
                limpiaTabla();
                return;
            }
            if (conexion.existeDato("select * from ERP_CTRL_SRS_PALLETS where pallet = '" + scaneado.trim() + "' and status = '0'")){
                mensaje("Este Pallet no se ha Cerrado");
                limpiaTabla();
                return;
            }
            if (conexion.existeDato("select * from ERP_CTRL_SRS_PALLETS where pallet = '" + scaneado.trim() + "' and status >= 2")){
                mensaje("Este Pallet ya se Embarco");
                return;
            }
          CajaInfo cajaInfo = conexion.getPalletInfo(scaneado);
            conexion.embarcarPallet(cajaInfo.pallet, editTroka.getText().toString().trim());
            conexion.actualizaInventariosSRS(cajaInfo.pallet, "D");
           generaRow(scaneado, cajaInfo);

        }catch (Exception ex) {
            mensaje(ex.getMessage());
        }
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
    public void mensaje(String mensaje) {


        AlertDialog.Builder dlgAlert = new AlertDialog.Builder(this);
        dlgAlert.setMessage(mensaje);
        dlgAlert.setTitle("Snapshot SRS");
        dlgAlert.create().show();

    }
    private void generaRow(String pallet, CajaInfo cajaInfo){
        try {

            TableRow newRow = new TableRow(Embarque.this);

            TextView textView = new TextView(Embarque.this);
            textView.setText(pallet);
            textView.setPadding(3, 16, 16, 16);

            TextView textView3 = new TextView(Embarque.this);
            textView3.setText(cajaInfo.qty + "");
            textView3.setPadding(30, 16, 16, 16);

            // Crear un botón de eliminación
            Button deleteButton = new Button(Embarque.this);
            deleteButton.setText("Eliminar");
            deleteButton.setOnClickListener(v -> {
                tableLayout.removeView(newRow); // Eliminar la fila
                cajasAgregadas.remove(pallet); // Eliminar el ID del conjunto
                try {
                    if (conexion.eliminarPalletEmbarcado(cajaInfo.pallet)) {
                        conexion.actualizaInventariosSRS(cajaInfo.pallet, "R");
                        mensaje("El Pallet se elimino de la troca");
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
}

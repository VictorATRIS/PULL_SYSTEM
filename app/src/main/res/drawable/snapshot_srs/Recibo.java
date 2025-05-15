package com.example.snapshot_srs;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class Recibo  extends AppCompatActivity  implements EMDKManager.EMDKListener, Scanner.StatusListener, Scanner.DataListener{
    private EMDKManager emdkManager = null;
    private BarcodeManager barcodeManager = null;
    private Scanner scanner = null;
    private EditText editPallet;
    private TableLayout tableLayout  ;
    private Conexion conexion;
    private Usuario usuario;
    private String cadenaConexion ;
    private TextView textTotal, textArtesas;
    private Set<String> cajasAgregadas = new HashSet<>();
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.recibo);
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
        try {
            getDatosPorDia();
        } catch (ScannerException e) {
            mensaje(e.getMessage());
        }
        editPallet.setFocusable(true);
    }
    private void iniciarElementos(){
        editPallet = findViewById(R.id.etScanTroka);
        tableLayout = findViewById(R.id.tableArtesas);
        textTotal = findViewById(R.id.tvTotalPzas);
        textArtesas = findViewById(R.id.tvTotalArtesas);
    }


    private void getDatosPorDia() throws ScannerException {
        try {
            ArrayList<CajaInfo> cajas = conexion.getDatosPorDia();
            for (CajaInfo cajaInfo : cajas){

                generaRow(cajaInfo.pallet,cajaInfo);
            }
        }catch (Exception ex) {
            scanner.enable();
            mensaje(ex.getMessage());
        }
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
                            editPallet.setFocusable(true);
                            editPallet.setText(scaneado);
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

            if ( !conexion.existeDato("select * from ERP_CTRL_SRS_PALLETS where pallet = '" + scaneado.trim() + "'")){
                mensaje("No existe informacion de este Pallet");
                return;
            }
            if ( conexion.existeDato("select * from ERP_CTRL_SRS_PALLETS where pallet = '" + scaneado.trim() + "' AND STATUS IN ('3','10')")){
                mensaje("Ya se recibio este Pallet");
                return;
            }

            CajaInfo cajaInfo = conexion.getPalletInfo(scaneado);
            conexion.recibirPallet(cajaInfo.pallet);
            editPallet.setBackgroundColor(Color.GREEN);
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

            TableRow newRow = new TableRow(Recibo.this);

            TextView textView = new TextView(Recibo.this);
            textView.setText(pallet);
            textView.setPadding(3, 16, 16, 16);

            TextView textView3 = new TextView(Recibo.this);
            textView3.setText(cajaInfo.qty + "");
            textView3.setPadding(30, 16, 16, 16);

            TextView textView4 = new TextView(Recibo.this);
            textView4.setText(cajaInfo.caja);
            textView4.setPadding(60, 16, 16, 16);



            // Agregar las TextViews y el botón al TableRow
            newRow.addView(textView);
            newRow.addView(textView3);
            newRow.addView(textView4);
            // Agregar el TableRow al TableLayout
            tableLayout.addView(newRow);


            int total = Integer.parseInt(textTotal.getText().toString().split(":")[1].trim());
            total += cajaInfo.qty;
            textTotal.setText("Total Artesas:" + String.valueOf(total));
            int total2 = Integer.parseInt(textArtesas.getText().toString().split(":")[1].trim());
            total2 += Integer.parseInt(cajaInfo.caja.trim());
            textArtesas.setText("Total Pzas:" + String.valueOf(total2));
        } catch (Exception ex) {
            mensaje(ex.getMessage());
        }

    }

}

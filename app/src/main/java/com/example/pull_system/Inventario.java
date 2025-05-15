package com.example.pull_system;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.symbol.emdk.EMDKManager;
import com.symbol.emdk.barcode.BarcodeManager;
import com.symbol.emdk.barcode.ScanDataCollection;
import com.symbol.emdk.barcode.Scanner;
import com.symbol.emdk.barcode.ScannerException;
import com.symbol.emdk.barcode.ScannerResults;
import com.symbol.emdk.barcode.StatusData;

import java.util.ArrayList;

public class Inventario extends Activity implements EMDKManager.EMDKListener, Scanner.StatusListener, Scanner.DataListener {
    private Conexion conexion;
    private Usuario usuario;
    private String cadenaConexion ;
    private Button butCerrarInv ;
    public EditText editTextSerial, editTextPartNo, editTextContainers, editTextPieces ;
    public TextView txtFecha, lbl_Error;
    private EMDKManager emdkManager = null;
    private BarcodeManager barcodeManager = null;
    private Scanner scanner = null;
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.inventario);
        initElementos();
        Intent intent =  getIntent();
        usuario =(Usuario) intent.getSerializableExtra("Usuario");
        cadenaConexion = intent.getStringExtra("cadenaCon");
        conexion = new Conexion(cadenaConexion);
        getFecha();

        EMDKManager.getEMDKManager(getApplicationContext(), this);
        butCerrarInv.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(Inventario.this);
                builder.setTitle("Attention");
                builder.setMessage("Are you sure you want to close the inventory?");
                builder.setPositiveButton("Accept", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        try {
                            conexion.cerrarInventario(usuario, lbl_Error, butCerrarInv);
                        } catch (Exception ex) {
                            MessageBox(ex.getMessage());
                        }
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                AlertDialog dialog = builder.create();
                dialog.show();

            }
        });
    }
    private void getFecha(){
        try {
             String fecha = conexion.GetFechaActual();
            txtFecha.setText(fecha);
        }catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    private void initElementos() {
        editTextSerial = findViewById(R.id.editTextSerial);
        editTextPartNo = findViewById(R.id.editTextPartNo);
        editTextContainers = findViewById(R.id.editTextContainers);
        editTextPieces = findViewById(R.id.editTextPieces);
        txtFecha = findViewById(R.id.txtFecha);
        lbl_Error = findViewById(R.id.lbl_Error);
        butCerrarInv = findViewById(R.id.butCerrarInv);
    }

    public void BuscarSerial(String Serial) {
        try {
          conexion.ValidarInventarioSerial(editTextSerial, editTextPartNo, editTextContainers, editTextPieces, Serial, usuario, lbl_Error );

        } catch (Exception ex) {
            MessageBox(ex.getMessage());
        }
    }


    private void MessageBox(String msg) {
        new AlertDialog.Builder(this)
                .setTitle("SCR")
                .setMessage(msg)
                .setPositiveButton(android.R.string.ok, (dialogInterface, i) -> {
                })
                .show();
    }

    /* Metodos del escaner */
    @Override
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
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (this.emdkManager != null) {
            barcodeManager = (BarcodeManager) this.emdkManager.getInstance(EMDKManager.FEATURE_TYPE.BARCODE);
            initScanner();
        }
    }

    @Override
    public void onData(ScanDataCollection scanDataCollection) {
        String dataStr = "";
        if ((scanDataCollection != null) && (scanDataCollection.getResult() == ScannerResults.SUCCESS)) {
            ArrayList<ScanDataCollection.ScanData> scanData = scanDataCollection.getScanData();
            for (ScanDataCollection.ScanData data : scanData) {
                dataStr = data.getData();
            }
            updateData(dataStr);
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
                scanner.triggerType = Scanner.TriggerType.HARD;
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

    public void updateData(final String result) {
        runOnUiThread(() -> {
            try {
                BuscarSerial(result);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

}
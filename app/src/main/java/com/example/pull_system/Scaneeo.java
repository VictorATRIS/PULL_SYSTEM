package com.example.pull_system;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
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

import java.sql.SQLException;
import java.util.ArrayList;

public class Scaneeo extends Activity implements EMDKManager.EMDKListener, Scanner.StatusListener, Scanner.DataListener  {
    Usuario usuario;
    String cadenaConexion;
    TextView textNumber, textQtyActual,textQtyOrder,textStdPack,textContainer, textMfgShip, textTdc,textDcs,textMfgPlant;
    EditText textScanner;
    String part_no;
    private EMDKManager emdkManager = null;
    private BarcodeManager barcodeManager = null;
    private Scanner scanner = null;
    Conexion conexion ;
    String qty_total,  std_pack,  container,  mfg_ship,  etd_tdc,  etdDcsc,  mfg_plant, qty_real;
    String ip;
    int puerto;
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
                    "<STX>890C00403000<CR><ETX>\n" +
                    "<STX>1120<CR><ETX>\n" +
                    "<STX>32<CR><ETX>\n" +
                    "<STX>2<CR><ETX>\n" +
                    "<STX>L2025051234<CR><ETX>\n" +
                    "<STX>2025-05-13<CR><ETX>\n" +
                    "<STX>2025-05-14<CR><ETX>\n" +
                    "<STX>2025-05-14<CR><ETX>\n" +
                    "<STX><ETB><ETX>";
    ImpresoraHoneywell impresora ;
    @SuppressLint("SetTextI18n")
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.scanneo);
        Intent intent =  getIntent();
        usuario =(Usuario) intent.getSerializableExtra("Usuario");
        cadenaConexion = intent.getStringExtra("cadenaCon");
        part_no = intent.getStringExtra("part_no");
        iniciarElementos();
        textScanner.setEnabled(false);
        textNumber.setText( part_no);
        EMDKManager.getEMDKManager(getApplicationContext(), this);
        conexion = new Conexion(cadenaConexion);

        updateInfo();

        impresora = new ImpresoraHoneywell(ip,puerto);
    }
    private void updateInfo() {
        try {
            NoPartInfo info = new NoPartInfo();
            conexion.getInfoNoPart(part_no, info);
          ArrayList <String> datosImpresora =   conexion.getInfoPrinter();
          ip = datosImpresora.get(0);
          puerto = Integer.parseInt( datosImpresora.get(1));
            // Ahora podemos usar los valores de `info`
            textQtyActual.setText(info.getQtyReal());
            textQtyOrder.setText(info.getQtyTotal());
            textStdPack.setText(info.getStdPack());
            textContainer.setText(info.getContainer());
            textMfgShip.setText(info.getMfgShip());
            textTdc.setText(info.getEtdTdc());
            textDcs.setText(info.getEtdDcsc());
            textMfgPlant.setText(info.getMfgPlant());

        } catch (SQLException e) {
            mensaje(e.getMessage());
        }
    }

    private void iniciarElementos(){
        textNumber = findViewById(R.id.tvPartNo);
        textQtyActual = findViewById(R.id.tvQty1);
        textQtyOrder = findViewById(R.id.tvQty2);
        textStdPack = findViewById(R.id.tvStdPack1);
        textContainer = findViewById(R.id.tvContainer1);
        textMfgShip = findViewById(R.id.tvMFGSHIP1);
        textTdc = findViewById(R.id.tvEtdTdc1);
        textDcs = findViewById(R.id.tvEtdCsc1);
        textMfgPlant = findViewById(R.id.tvMfgPlant1);
        textScanner = findViewById(R.id.etScanData);




    }
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

    @SuppressLint("SetTextI18n")
    public void updateData(final String result) {
        runOnUiThread(() -> {
            try {
                guardaRegistros(result);
                textScanner.setText("🔍" + result);

                int qtyActual = Integer.parseInt(textQtyActual.getText().toString());
                int qtyOrder = Integer.parseInt(textQtyOrder.getText().toString());

                if (qtyActual >= qtyOrder) {
                   imprime(part_no);
                    deInitScanner(); // Desactiva el escáner si se cumple la condición
                    mensaje("Part_no Completed");
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    public void guardaRegistros (String data) {
        try {
            conexion.validaSerialParaEmbarcar(data, usuario, this, part_no);
            updateInfo();
        }catch (Exception ex) {
            mensaje(ex.getMessage());
        }

    }
    public void mensaje(String mensaje) {


        AlertDialog.Builder dlgAlert = new AlertDialog.Builder(this);
        dlgAlert.setMessage(mensaje);
        dlgAlert.setTitle("Pull System");
        dlgAlert.create().show();

    }
    public void imprimir(){
       /* String comandosIPL =
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
                        "<STX>@pallets<CR><ETX>\n" +
                        "<STX>@folio<CR><ETX>\n" +
                        "<STX>@atr_date<CR><ETX>\n" +
                        "<STX>@etd_tdc<CR><ETX>\n" +
                        "<STX>eta_dsc<CR><ETX>\n" +
                        "<STX><ETB><ETX>";
*/

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

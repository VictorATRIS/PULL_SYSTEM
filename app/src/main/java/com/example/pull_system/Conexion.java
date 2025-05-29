package com.example.pull_system;

import android.app.Activity;
import android.app.AlertDialog;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import android.graphics.Color;
import android.widget.TextView;

public class Conexion {
    private Connection conn;
    private Statement comm;
    private String cadenaConexion;
    private Activity activiti;
    private String usuarioo;
    private String password, dom;
    private Usuario usuario;
    public Conexion(String cadenaConexion) {
        this.cadenaConexion = cadenaConexion;
    }

    public Conexion(String cadenaConexion, Activity activiti) {
        this.cadenaConexion = cadenaConexion;
        this.activiti = activiti;

    }


    public Conexion(Connection conn, Statement comm, String cadenaConexion, Activity activiti, String usuarioo, String password, String dom) {
        this.conn = conn;
        this.comm = comm;
        this.cadenaConexion = cadenaConexion;
        this.activiti = activiti;


        this.usuarioo = usuarioo;
        this.password = password;
        this.dom = dom;
    }
    public Conexion(String cadenaConexion, Activity activiti, Usuario usuario) {
        this.cadenaConexion = cadenaConexion;
        this.activiti = activiti;

        this.usuario = usuario;
    }
    public ResultSet ConsultaDatos(String query) {
        try {
            initConexion();
            return comm.executeQuery(query);

        } catch (Exception ex) {
            return null;
        }
    }

    public Connection getConn() {
        return conn;
    }

    public Connection initConexion() {
        try {

            Class.forName("net.sourceforge.jtds.jdbc.Driver").newInstance();
            conn = DriverManager.getConnection(cadenaConexion);
            comm = conn.createStatement();
        } catch (Exception e) {
            mensaje(e.getMessage(),"Pull System");
        }
        return conn;

    }
    public void mensaje(String mensaje, String titulo) {
        AlertDialog.Builder dlgAlert = new AlertDialog.Builder(this.activiti);
        dlgAlert.setMessage(mensaje);
        dlgAlert.setTitle(titulo);
        dlgAlert.create().show();

    }
    public void mensaje2(String mensaje, String titulo, Scaneeo scaneo) {
        AlertDialog.Builder dlgAlert = new AlertDialog.Builder(scaneo);
        dlgAlert.setMessage(mensaje);
        dlgAlert.setTitle(titulo);
        dlgAlert.create().show();

    }
    public boolean validaUsuario(Usuario usuario) throws SQLException {
        boolean correcto = false;
        try {
            initConexion(); // Iniciamos la conexion de la base da datos
            String query = "SP_CTRL_VALIDA_USUARIO_PULL_SYSTEM '" + usuario.getUsuario() + "' , '" + usuario.getPassword() + "'";
            ResultSet resultSet = comm.executeQuery(query);//Obtenemos los datos que traemos de la consulta y lo guardamos en un resultSet
            if (resultSet.next()) {
                usuario.setNombre(resultSet.getString("Usr_name")); // Obtenemos el nombre del usuario
                usuario.setUsuario(resultSet.getString("USR_ID"));
                usuario.setUsuarioNick(resultSet.getString("Usr_nick"));
                usuario.setUsuarioGrupo(resultSet.getString("GRP_id"));

                correcto = true;
            }


        } catch (SQLException ex) {
            mensaje(ex.getMessage(),"Pull System");
        } finally {
            if (!conn.isClosed()) {
                conn.close();
            }

        }
        return correcto;

    }
    public String GetFechaActual() throws SQLException {
        String fecha = "";
        try {
            initConexion(); // Iniciamos la conexion de la base da datos
            String query = "SELECT FORMAT(GETDATE(), 'MM/dd/yyyy') AS fecha";
            ResultSet resultSet = comm.executeQuery(query);//Obtenemos los datos que traemos de la consulta y lo guardamos en un resultSet
            if (resultSet.next()) {
                fecha = (resultSet.getString("fecha"));
            }

        } catch (SQLException ex) {
            mensaje(ex.getMessage(),"Pull System");
        } finally {
            if (!conn.isClosed()) {
                conn.close();
            }

        }
        return fecha;
    }
    public void ValidarReciboSerial(EditText SerialEditTxt, EditText NoPartEditTxt, EditText Containers, EditText Pieces, String Serial, Usuario usuario, TextView Error) throws SQLException {

        String NoPart, qty ;
        try {
            initConexion(); // Iniciamos la conexion de la base da datos
            String query = "SP_SRS_CONSULTA_DATOS_CAJA '"+ Serial + "'"; //te valida que exista el serial en el gacs
            ResultSet resultSet = comm.executeQuery(query);//Obtenemos los datos que traemos de la consulta y lo guardamos en un resultSet
            if (resultSet.next()) {
                NoPart = (resultSet.getString("PRODUCT_NO"));
                qty = (resultSet.getString("CASE_QTY"));
                query = "SP_CTRL_PULL_SYSTEM_VALIDA_NUMBERS_SERIAL_RECIBO '"+ NoPart + "'"; //valida que se encuentren el no Parte dentro de los aceptados en pull system
                ResultSet resultSet2 = comm.executeQuery(query);
                if (resultSet2.next()) {
                    query = "SP_CTRL_PULL_SYSTEM_VERIFICA_SERIAL_RECIBO '"+ Serial + "'" ; //valida que no exista en receiving
                    ResultSet resultSet3 = comm.executeQuery(query);
                    if (resultSet3.next()) {
                        Error.setText("ERROR: The serial number is already exist");
                        Error.setTextColor(Color.RED);
                        SerialEditTxt.setText(Serial);
                        SerialEditTxt.setTextColor(Color.RED);
                        NoPartEditTxt.setText("");
                        Containers.setText("");
                        Pieces.setText("");
                    }else{
                        query = "SP_CTRL_PULL_SYSTEM_INSERTA_SERIAL_RECIBO '"+ Serial + "', '"+NoPart +"','"+ qty + "', '" + usuario.getUsuarioNick() + "'" ; // inserta en tabla de receiving y retorna el total de cajas y piezas del dia
                        ResultSet resultSet4 = comm.executeQuery(query);
                        if (resultSet4.next()) {
                            Error.setText("");
                            SerialEditTxt.setTextColor(Color.BLACK);
                            SerialEditTxt.setText(resultSet4.getString("Serial"));
                            NoPartEditTxt.setText(resultSet4.getString("NoPart"));
                            Containers.setText(resultSet4.getString("Containers"));
                            Pieces.setText(resultSet4.getString("QTY"));
                        }
                    }
                }else{
                    Error.setText("ERROR: The serial number doesn't correspond to the pull system");
                    Error.setTextColor(Color.RED);
                    SerialEditTxt.setText(Serial);
                    SerialEditTxt.setTextColor(Color.RED);
                    NoPartEditTxt.setText("");
                    Containers.setText("");
                    Pieces.setText("");
                }
            }else{
                Error.setText("ERROR: The serial number doesn't exist");
                Error.setTextColor(Color.RED);
                SerialEditTxt.setText(Serial);
                SerialEditTxt.setTextColor(Color.RED);
                NoPartEditTxt.setText("");
                Containers.setText("");
                Pieces.setText("");
            }
        } catch (SQLException ex) {
            mensaje(ex.getMessage(),"Pull System");
        } finally {
            if (!conn.isClosed()) {
                conn.close();
            }

        }
    }
    public void ValidarInventarioSerial(EditText SerialEditTxt, EditText NoPartEditTxt, EditText Containers, EditText Pieces, String Serial, Usuario usuario, TextView Error) throws SQLException {

        String NoPart, qty,serial ;
        Integer ActivInvent;
        try {
            initConexion(); // Iniciamos la conexion de la base da datos
            String query = "SP_CTRL_PULL_SYSTEM_VALIDA_INVENTARIO_ACTIVO "; //te valida que el inventario este activo
            ResultSet resultSet = comm.executeQuery(query);//Obtenemos los datos que traemos de la consulta y lo guardamos en un resultSet
            if (resultSet.next()) {
                ActivInvent = (resultSet.getInt("Inv_status"));
                if (ActivInvent == 1) {
                    query = "SP_SRS_CONSULTA_DATOS_CAJA '" + Serial + "'"; //valida que exista el serial en el gacs
                    ResultSet resultSet2 = comm.executeQuery(query);
                    if (resultSet2.next()) {
                        NoPart = (resultSet2.getString("PRODUCT_NO"));
                        qty = (resultSet2.getString("CASE_QTY"));
                        query = "SP_CTRL_PULL_SYSTEM_VALIDA_NUMBERS_SERIAL_RECIBO '"+ NoPart + "'"; //valida que se encuentren el no Parte dentro de los aceptados en pull system
                        ResultSet resultSet3 = comm.executeQuery(query);
                        if (resultSet3.next()) {
                            query = "SP_CTRL_PULL_SYSTEM_VERIFICA_SERIAL_INVENTARIO '"+ Serial + "'" ; //valida que no exista en el inventario del día de hoy
                            ResultSet resultSet4 = comm.executeQuery(query);
                            if (resultSet4.next()) {
                                Error.setText("ERROR: The serial number is already exist in the inventory");
                                Error.setTextColor(Color.RED);
                                SerialEditTxt.setText(Serial);
                                SerialEditTxt.setTextColor(Color.RED);
                                NoPartEditTxt.setText("");
                                Containers.setText("");
                                Pieces.setText("");
                            }else{
                                query = "SP_CTRL_PULL_SYSTEM_INSERTA_SERIAL_INVENTARIO '"+ Serial + "', '"+ qty +"','"+ NoPart + "', '" + usuario.getUsuarioNick() + "'" ; // inserta en tabla de receiving y retorna el total de cajas y piezas del dia
                                ResultSet resultSet5 = comm.executeQuery(query);
                                if (resultSet5.next()) {
                                    Error.setText("");
                                    SerialEditTxt.setTextColor(Color.BLACK);
                                    SerialEditTxt.setText(resultSet5.getString("Serial"));
                                    NoPartEditTxt.setText(resultSet5.getString("NoPart"));
                                    Containers.setText(resultSet5.getString("Containers"));
                                    Pieces.setText(resultSet5.getString("QTY"));
                                }
                            }
                        }else{
                            Error.setText("ERROR: The serial number doesn't correspond to the pull system");
                            Error.setTextColor(Color.RED);
                            SerialEditTxt.setText(Serial);
                            SerialEditTxt.setTextColor(Color.RED);
                            NoPartEditTxt.setText("");
                            Containers.setText("");
                            Pieces.setText("");
                        }
                    } else {
                        Error.setText("ERROR: The serial number doesn't exist");
                        Error.setTextColor(Color.RED);
                        SerialEditTxt.setText(Serial);
                        SerialEditTxt.setTextColor(Color.RED);
                        NoPartEditTxt.setText("");
                        Containers.setText("");
                        Pieces.setText("");
                    }
                }else{
                    Error.setText("ERROR: The inventory has been completed");
                    Error.setTextColor(Color.RED);
                    SerialEditTxt.setText(Serial);
                    SerialEditTxt.setTextColor(Color.RED);
                    NoPartEditTxt.setText("");
                    Containers.setText("");
                    Pieces.setText("");
                }
            }
        } catch (SQLException ex) {
            mensaje(ex.getMessage(),"Pull System");
        } finally {
            if (!conn.isClosed()) {
                conn.close();
            }

        }
    }
    public void cerrarInventario(Usuario usuario, TextView Error, Button btnCerrar) throws SQLException {
        Integer ActivInvent;
        try {
            initConexion(); // Iniciamos la conexion de la base da datos
            String query = "SP_CTRL_PULL_SYSTEM_VALIDA_INVENTARIO_ACTIVO "; //te valida que el inventario este activo
            ResultSet resultSet = comm.executeQuery(query);//Obtenemos los datos que traemos de la consulta y lo guardamos en un resultSet
            if (resultSet.next()) {
                ActivInvent = (resultSet.getInt("Inv_status"));
                if (ActivInvent == 1) {
                    query = "SP_CTRL_PULL_SYSTEM_CERRAR_INVENTARIO '" + usuario.getUsuarioNick() + "'" ; // CERRAR INVENTARIO
                    PreparedStatement preparedStatement = conn.prepareStatement(query);

                    int guardado = preparedStatement.executeUpdate();

                    // Verificar si la inserción fue exitosa
                    if (guardado > 0) {
                        Error.setText("¡Success! The inventory has been closed.");
                        Error.setTextColor(Color.parseColor("#388E3C"));
                        btnCerrar.setVisibility(View.INVISIBLE);
                    }
                }else{
                    Error.setText("ERROR: The inventory has been completed");
                    Error.setTextColor(Color.RED);
                    btnCerrar.setVisibility(View.INVISIBLE);
                }
            }
        } catch (SQLException ex) {
            mensaje(ex.getMessage(),"Pull System");
        } finally {
            if (!conn.isClosed()) {
                conn.close();
            }

        }
    }
    public ArrayList<DailyOrder> getDailyOrder() throws SQLException {
        ArrayList<DailyOrder> orders = new ArrayList<>();
        try {
            initConexion(); // Inicia la conexión con la base de datos
            String query = "SP_CTRL_PULL_SYSTEM_GET_DAILY_ORDER";
            ResultSet resultSet = comm.executeQuery(query); // Ejecuta la consulta

            while (resultSet.next()) {
                DailyOrder info = new DailyOrder();
                info.setPartNo(resultSet.getString("PART_NO"));
                info.setCustomer(resultSet.getString("CUSTOMER_ID"));
                info.setOrderQty(resultSet.getInt("ORDER_QTY"));
                info.setRealQty(resultSet.getInt("ORDER_QTY"));
                info.setArtesas(resultSet.getString("ARTESAS"));
                orders.add(info);
            }
        } catch (SQLException ex) {
            mensaje(ex.getMessage(),"Pull System");
        } finally {
            if (!conn.isClosed()) {
                conn.close(); // Cierra la conexión con la base de datos
            }
        }
        return orders;
    }
    public void getInfoNoPart(String part_no, NoPartInfo info) throws SQLException {
        try {
            initConexion(); // Inicia la conexión con la base de datos
            String query = "SP_CTRL_PULL_SYSTEM_GET_NO_PART_INFO '" + part_no + "'";
            ResultSet resultSet = comm.executeQuery(query); // Ejecuta la consulta

            if (resultSet.next()) { // Si hay resultados, llenamos el objeto
                info.setQtyTotal(resultSet.getString("Qty_Total"));
                info.setQtyReal(resultSet.getString("Qty_Real"));
                info.setStdPack(resultSet.getString("Std_pack"));
                info.setContainer(resultSet.getString("Container"));
                info.setMfgShip(resultSet.getString("Mfg_ship"));
                info.setEtdTdc(resultSet.getString("Etd_tdc"));
                info.setEtdDcsc(resultSet.getString("Etd_Dcsc"));
                info.setMfgPlant(resultSet.getString("Mfg_plant"));
            }
        } catch (SQLException ex) {
            mensaje(ex.getMessage(), "Pull System");
        } finally {
            if (!conn.isClosed()) {
                conn.close(); // Cierra la conexión con la base de datos
            }
        }
    }
    public boolean  validaSerialParaEmbarcar( String Serial, Usuario usuario, Scaneeo scaneeo, String part_no) throws SQLException {
        boolean bandera = false;
        String NoPart, qty ;
        try {
            initConexion(); // Iniciamos la conexion de la base da datos

            String query = "SP_CTRL_PULL_SYSTEM_EXISTE_IN_SHIPPING '"+ Serial + "'"; //te valida que exista el serial en el gacs
            ResultSet resultSet = comm.executeQuery(query);//Obtenemos los datos que traemos de la consulta y lo guardamos en un resultSet
            if(resultSet.next()) {
                mensaje2("This serial has already been scanned" , "Pull System", scaneeo);
                return false;
            }

             query = "SP_CTRL_PULL_SYSTEM_VALIDA_EXISTA_SERIAL '"+ Serial + "' , '" + part_no + "'"; //te valida que exista el serial en el gacs
             resultSet = comm.executeQuery(query);//Obtenemos los datos que traemos de la consulta y lo guardamos en un resultSet
            if (resultSet.next()) {
                query = "SP_CTRL_PULL_SYSTEM_SAVE_SHIPPING '" + Serial + "', '" + usuario.getUsuarioNick() + "'";
                int filasAfectadas = comm.executeUpdate(query); // Cambia executeQuery() por executeUpdate()
               if (filasAfectadas > 0) { // Verifica si se ejecutó correctamente
                    bandera = true;
                }
             }else{
                mensaje2("ERROR: The serial number is not a pull system number" , "Pull System",scaneeo);
            }
        } catch (SQLException ex) {
            mensaje(ex.getMessage(),"Pull System");
        } finally {
            if (!conn.isClosed()) {
                conn.close();
            }

        }
        return  bandera;
    }


    public ArrayList<String> getInfoPrinter() throws SQLException {
        ArrayList <String> info = new ArrayList<String>();
        try {
            initConexion(); // Inicia la conexión con la base de datos
            String query = "Select * from ERP_CTRL_PULL_SYSTEM_PRINTER";
            ResultSet resultSet = comm.executeQuery(query); // Ejecuta la consulta

            if (resultSet.next()) { // Si hay resultados, llenamos el objeto
                info.add(resultSet.getString("Ip"));
                info.add(resultSet.getString("Puerto"));

            }
            return info;
        } catch (SQLException ex) {
            mensaje(ex.getMessage(), "Pull System");
        } finally {
            if (!conn.isClosed()) {
                conn.close(); // Cierra la conexión con la base de datos
            }
        }
        return info;
    }

    public ArrayList<String> setInfoPrinter( String part_no) throws SQLException {
        ArrayList <String> info = new ArrayList<String>();
        try {
            initConexion(); // Inicia la conexión con la base de datos
            String query = "SP_CTRL_PULL_SYSTEM_GET_PRINTER_DATA '" + part_no + "'";
            ResultSet resultSet = comm.executeQuery(query); // Ejecuta la consulta

            while (resultSet.next()) { // Iteramos sobre todos los resultados
                info.add(resultSet.getString("Printer"));
            }

            return info;
        } catch (SQLException ex) {
            mensaje(ex.getMessage(), "Pull System");
        } finally {
            if (!conn.isClosed()) {
                conn.close(); // Cierra la conexión con la base de datos
            }
        }
        return info;
    }
}

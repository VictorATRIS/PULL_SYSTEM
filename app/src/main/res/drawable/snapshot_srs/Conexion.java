package com.example.snapshot_srs;

import android.app.Activity;
import android.app.AlertDialog;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

public class Conexion {
    private Connection conn;
    private Statement comm;
    private String cadenaConexion;
    private Activity activiti;

    private boolean banderaInicioEscaneo = true;
    private Usuario usuario;
    private String usuarioo;
    private String password, dom;

    public Conexion(String cadenaConexion) {
        this.cadenaConexion = cadenaConexion;
    }

    public Conexion(String cadenaConexion, Activity activiti) {
        this.cadenaConexion = cadenaConexion;
        this.activiti = activiti;

    }


    public Conexion(Connection conn, Statement comm, String cadenaConexion, Activity activiti, boolean banderaInicioEscaneo, Usuario usuario, String usuarioo, String password, String dom) {
        this.conn = conn;
        this.comm = comm;
        this.cadenaConexion = cadenaConexion;
        this.activiti = activiti;

        this.banderaInicioEscaneo = banderaInicioEscaneo;
        this.usuario = usuario;
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

    public void setConn(Connection conn) {
        this.conn = conn;
    }

    public String getUsuarioo() {
        return usuarioo;
    }

    public void setUsuarioo(String usuarioo) {
        this.usuarioo = usuarioo;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getDom() {
        return dom;
    }

    public void setDom(String dom) {
        this.dom = dom;
    }


    //En este metodo iniciamos la conexion con la base de datos ----------------------------------------------------------------------------------------------------------
    public Connection initConexion() {
        try {

            Class.forName("net.sourceforge.jtds.jdbc.Driver").newInstance();
            conn = DriverManager.getConnection(cadenaConexion);
            comm = conn.createStatement();
        } catch (Exception e) {
            mensaje(e.getMessage(),"Snapshot SRS");
        }
        return conn;

    }

    //Metodo para ejecutar metodos como delete, insert, update -------------------------------------------------------------------------------------------------------------
    public void executaQuery(String query) {
              initConexion();
        try {
            comm.execute(query);
            comm.close();
        } catch (SQLException throwables) {
            mensaje(throwables.getMessage(),"Control Embarques");
        }


    }
    public String consultaDato(String sql) throws SQLException {
        String result = "";
        ResultSet rs = null;
        try {

            Statement stmt = initConexion().createStatement();
            rs = stmt.executeQuery(sql);
            if (rs.next()) {
                result = rs.getString(1);
            }
            return result;
        } catch (SQLException ex) {
            mensaje(ex.getMessage(),"Embarques");
        } finally {
            getConn().close();
        }

        return result;
    }

    public boolean existeDato(String sql) throws SQLException {
        boolean bandera = false;
        ResultSet rs = null;
        try {

            Statement stmt = initConexion().createStatement();
            rs = stmt.executeQuery(sql);
            if (rs.next()) {
                bandera = true;
            }
            return bandera;
        } catch (SQLException ex) {
            mensaje(ex.getMessage(),"Embarques");
        } finally {
            getConn().close();
        }
        return bandera;


    }





    //Esta funcion nos sirve mara arrojar mensajes en la pantalla------------------------------------------------------------------------------------------------------------
    public void mensaje(String mensaje, String titulo) {
        AlertDialog.Builder dlgAlert = new AlertDialog.Builder(this.activiti);
        dlgAlert.setMessage(mensaje);
        dlgAlert.setTitle(titulo);
        dlgAlert.create().show();

    }

    //Esta funcion la usaremos para validar que el usuario y contrasena sean correctos y asi poder iniciar sesion-----------------------------------------------------------
    public boolean validaUsuario(Usuario usuario) throws SQLException {
        boolean correcto = false;
        try {
            initConexion(); // Iniciamos la conexion de la base da datos
            String query = "SELECT * FROM ERP_ADM_User WHERE Usr_nick = '" + usuario.getUsuario() + "' AND USR_PWD = '" + usuario.getPassword() + "'";
            ResultSet resultSet = comm.executeQuery(query);//Obtenemos los datos que traemos de la consulta y lo guardamos en un resultSet
            if (resultSet.next()) {
                usuario.setNombre(resultSet.getString("Usr_name")); // Obtenemos el nombre del usuario
                usuario.setUsuario(resultSet.getString("USR_ID"));
                usuario.setUsuarioNick(resultSet.getString("Usr_nick"));
                usuario.setUsuarioGrupo(resultSet.getString("GRP_id"));

                correcto = true;
            }


        } catch (SQLException ex) {
            mensaje(ex.getMessage(),"Snapshot SRS");
        } finally {
            if (!conn.isClosed()) {
                conn.close();
            }

        }
        return correcto;

    }





    public boolean existeDatos(String sql) throws SQLException {
        try {
            initConexion();
            PreparedStatement statement = conn.prepareStatement(sql);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return true;
            } else {
                return false;
            }
        } catch (Exception ex) {
            mensaje(ex.getMessage(), "Embarques");
        } finally {
            conn.close();
        }
        return false;
    }



    public  boolean validateUserAccess(String group) throws SQLException {

        if ("1".equals(group)) {
            return true; // Grant access for group 1
        } else {
            initConexion();
            try (
                 PreparedStatement stmt = conn.prepareStatement("SELECT * FROM ERP_ADM_ACCESS WHERE GRP_ID = ? AND FRM_ID = ? AND FRM_AX = ?")) {
                stmt.setString(1, group);
                stmt.setString(2, "erp_ctrl_verificaja");
                stmt.setString(3, "1");
                try (ResultSet rs = stmt.executeQuery()) {
                    return rs.next(); // Check if any rows exist
                }
            } catch (SQLException ex) {
                mensaje(ex.getMessage(),"Snapshot SRS");
            } finally {
                if (!conn.isClosed()) {
                    conn.close();
                }

            }
        }
        return false;
    }
    public CajaInfo getCajaInfo(String caja) throws SQLException {
        CajaInfo info = new CajaInfo(caja,"", 0,"","");
        try {
            initConexion(); // Iniciamos la conexión de la base de datos
            String query = "SP_OTHERS_SRS_SEARCH_BOX'" + caja.trim() + "'";
            ResultSet resultSet = comm.executeQuery(query); // Obtenemos los datos de la consulta
            if (resultSet.next()) {
                info.product_no = resultSet.getString("product_no");
                info.qty = resultSet.getInt("case_qty");
                info.dl = resultSet.getString("cusdesch_c1").trim() + resultSet.getString("cusdesch_c2").trim() + resultSet.getString("intdesch_c").trim();
            }
        } catch (SQLException ex) {
            mensaje(ex.getMessage(),"Snapshot SRS");
        } finally {
            if (!conn.isClosed()) {
                conn.close();
            }
        }
        return info;
    }

    public CajaInfo getCajaInfoRetorno(String caja) throws SQLException {
        CajaInfo info = new CajaInfo(caja,"", 0,"","");
        try {
            initConexion(); // Iniciamos la conexión de la base de datos
            String query = "select Product_no, Dl, Qty from ERP_CTRL_SRS_ARTESAS where Status = '0' and box_serial = '" + caja.trim() + "'";
            ResultSet resultSet = comm.executeQuery(query); // Obtenemos los datos de la consulta
            if (resultSet.next()) {
                info.product_no = resultSet.getString("Product_no");
                info.qty = resultSet.getInt("Qty");
                info.dl = resultSet.getString("Dl").trim() ;
            }
        } catch (SQLException ex) {
            mensaje(ex.getMessage(),"Snapshot SRS");
        } finally {
            if (!conn.isClosed()) {
                conn.close();
            }
        }
        return info;
    }
    public boolean insertPallet(String pallet, String status, String plantaDestino,String usuReg) throws SQLException {
        boolean seGuardo = false;

        try {
            initConexion(); // Iniciamos la conexión de la base de datos

            String query = "INSERT INTO ERP_CTRL_SRS_PALLETS (Pallet, Status, Planta_Destino, Usu_reg, Fecha_reg) " +
                    "VALUES (?, ?, ?, ?, getDate())";

            PreparedStatement preparedStatement = conn.prepareStatement(query);
            preparedStatement.setString(1, pallet);
            preparedStatement.setString(2, status);
            preparedStatement.setString(3, plantaDestino);
            preparedStatement.setString(4, usuReg);

            // Ejecutar la consulta
            int guardado = preparedStatement.executeUpdate();

            // Verificar si la inserción fue exitosa
            if (guardado > 0) {
                seGuardo = true;

            }
        } catch (SQLException ex) {
            mensaje("No se puedo registrar el Pallet;" + ex.getMessage() ,"Snapshot SRS");
        } finally {
            if (!conn.isClosed()) {
                conn.close(); // Cerrar la conexión
            }
        }

        return seGuardo;
    }

    public boolean insertArtesas(String pallet,String caja, String product_no,String dl,int qty, String status,String usuReg) throws SQLException {
        boolean seGuardo = false;

        try {
            initConexion(); // Iniciamos la conexión de la base de datos

            String query = "INSERT INTO ERP_CTRL_SRS_artesas (Pallet, Box_serial, Product_no, Dl, Qty,Status,Usu_reg,fecha_reg) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, getDate())";

            PreparedStatement preparedStatement = conn.prepareStatement(query);
            preparedStatement.setString(1, pallet);
            preparedStatement.setString(2, caja);
            preparedStatement.setString(3, product_no);
            preparedStatement.setString(4, dl);
            preparedStatement.setInt(5, qty);
            preparedStatement.setString(6, status);
            preparedStatement.setString(7, usuReg);

            // Ejecutar la consulta
            int guardado = preparedStatement.executeUpdate();

            // Verificar si la inserción fue exitosa
            if (guardado > 0) {
                seGuardo = true;

            }
        } catch (SQLException ex) {
            mensaje("No se puedo registrar la artesa;" + ex.getMessage() ,"Snapshot SRS");
        } finally {
            if (!conn.isClosed()) {
                conn.close(); // Cerrar la conexión
            }
        }

        return seGuardo;
    }
    public ArrayList<CajaInfo> getCajasPorPallet(String pallet) throws SQLException {
        ArrayList<CajaInfo> cajas = new ArrayList<>();
        try {
            initConexion(); // Inicia la conexión con la base de datos
            String query = "SELECT Box_Serial,Product_no,Qty FROM ERP_CTRL_SRS_artesas WHERE pallet = '" + pallet.trim() + "' AND STATUS = '0'";
            ResultSet resultSet = comm.executeQuery(query); // Ejecuta la consulta

            while (resultSet.next()) {
                CajaInfo info = new CajaInfo();
                info.caja = resultSet.getString("Box_Serial");
                info.product_no = resultSet.getString("Product_no");
                info.qty = resultSet.getInt("Qty");
                cajas.add(info);
            }
        } catch (SQLException ex) {
            mensaje(ex.getMessage(),"Snapshot SRS");
        } finally {
            if (!conn.isClosed()) {
                conn.close(); // Cierra la conexión con la base de datos
            }
        }
        return cajas;
    }

    public ArrayList<CajaInfo> getCajasPorPalletRetorno(String pallet) throws SQLException {
        ArrayList<CajaInfo> cajas = new ArrayList<>();
        try {
            initConexion(); // Inicia la conexión con la base de datos
            String query = "SELECT Box_Serial,Product_no,Qty FROM ERP_CTRL_SRS_artesas WHERE pallet = '" + pallet.trim() + "' AND STATUS = '1'";
            ResultSet resultSet = comm.executeQuery(query); // Ejecuta la consulta

            while (resultSet.next()) {
                CajaInfo info = new CajaInfo();
                info.caja = resultSet.getString("Box_Serial");
                info.product_no = resultSet.getString("Product_no");
                info.qty = resultSet.getInt("Qty");
                cajas.add(info);
            }
        } catch (SQLException ex) {
            mensaje(ex.getMessage(),"Snapshot SRS");
        } finally {
            if (!conn.isClosed()) {
                conn.close(); // Cierra la conexión con la base de datos
            }
        }
        return cajas;
    }
    public boolean eliminarCajaPorSerial(String boxSerial) throws SQLException {
          boolean delete = false;
        try {
            initConexion(); // Inicia la conexión con la base de datos
            String query = "DELETE FROM ERP_CTRL_SRS_artesas WHERE Box_Serial = ? AND STATUS = '0'";
            PreparedStatement preparedStatement = conn.prepareStatement(query);
            preparedStatement.setString(1, boxSerial.trim());
            int filasAfectadas = preparedStatement.executeUpdate();

            if (filasAfectadas > 0) {
               delete = true;
            }
        } catch (SQLException ex) {
           mensaje(ex.getMessage(),"Snapshot SRS");
        } finally {
            if (!conn.isClosed()) {
                conn.close();
            }
        }
        return delete;
    }
    public boolean eliminarCajaPorSerialRetorno(String boxSerial) throws SQLException {
        boolean delete = false;
        try {
            initConexion(); // Inicia la conexión con la base de datos
            String query = "DELETE FROM ERP_CTRL_SRS_artesas WHERE Box_Serial = ? AND STATUS = '1'";
            PreparedStatement preparedStatement = conn.prepareStatement(query);
            preparedStatement.setString(1, boxSerial.trim());
            int filasAfectadas = preparedStatement.executeUpdate();

            if (filasAfectadas > 0) {
                delete = true;
            }
        } catch (SQLException ex) {
            mensaje(ex.getMessage(),"Snapshot SRS");
        } finally {
            if (!conn.isClosed()) {
                conn.close();
            }
        }
        return delete;
    }
    public boolean getPlantasBolsaDeAire(String productNo, String planta) throws SQLException {
        boolean tieneDatos = false;
        try {
            initConexion();
            String query = "SP_CTRL_GET_PLANTAS_BOLSA_DE_AIRE '" + productNo.trim() + "', '" + planta.trim() + "'";
            ResultSet resultSet = comm.executeQuery(query);
            if (resultSet.next()) {
                tieneDatos = true;
            }
        } catch (SQLException ex) {
            mensaje(ex.getMessage(),"Snapshot SRS");
        } finally {
            if (!conn.isClosed()) {
                conn.close();
            }
        }
        return tieneDatos;
    }
    public boolean cerrarPallet(String pallet) throws SQLException {
        boolean palletCerrado = false;
        try {
            initConexion();
            String query = "UPDATE ERP_CTRL_SRS_PALLETS SET status = '1' WHERE pallet = ?";
            PreparedStatement preparedStatement = conn.prepareStatement(query);
            preparedStatement.setString(1, pallet.trim());
            int filasAfectadas = preparedStatement.executeUpdate();

            if (filasAfectadas > 0) {
                palletCerrado = true;
            }
        } catch (SQLException ex) {
            mensaje(ex.getMessage(),"Snapshot SRS");
        } finally {
            if (!conn.isClosed()) {
                conn.close(); // Cierra la conexión con la base de datos
            }
        }
        return palletCerrado;
    }
    public boolean cerrarPalletRetorno(String pallet) throws SQLException {
        boolean palletCerrado = false;
        try {
            initConexion();
            String query = "UPDATE ERP_CTRL_SRS_PALLETS SET status = -1 AND FECHA_ENVIO = GETDATE() WHERE pallet = ?";
            PreparedStatement preparedStatement = conn.prepareStatement(query);
            preparedStatement.setString(1, pallet.trim());
            int filasAfectadas = preparedStatement.executeUpdate();

            if (filasAfectadas > 0) {
                palletCerrado = true;
            }
        } catch (SQLException ex) {
            mensaje(ex.getMessage(),"Snapshot SRS");
        } finally {
            if (!conn.isClosed()) {
                conn.close(); // Cierra la conexión con la base de datos
            }
        }
        return palletCerrado;
    }



    public boolean embarcarPallet(String pallet, String troka) throws SQLException {
        boolean palletEmbarcado = false;
        try {
            initConexion();
            String query = "UPDATE ERP_CTRL_SRS_PALLETS SET status = 2 , troka = '" + troka.trim() + "', fecha_envio = getDate() WHERE pallet = ?";
            PreparedStatement preparedStatement = conn.prepareStatement(query);
            preparedStatement.setString(1, pallet.trim());
            int filasAfectadas = preparedStatement.executeUpdate();

            if (filasAfectadas > 0) {
                palletEmbarcado = true;
            }
        } catch (SQLException ex) {
          mensaje(ex.getMessage(),"Snapshot SRS");
        } finally {
            if (!conn.isClosed()) {
                conn.close(); // Cierra la conexión con la base de datos
            }
        }
        return palletEmbarcado;
    }
    public boolean actualizaInventariosSRS(String pallet, String tipo) throws SQLException {
        boolean palletEmbarcado = false;
        try {
            initConexion();
            String query = "SP_CTRL_ACTUALIZA_INVENTRARIOS_SRS '" + pallet.trim() + "','" + tipo.trim() + "'"   ;
            PreparedStatement preparedStatement = conn.prepareStatement(query);

            int filasAfectadas = preparedStatement.executeUpdate();

            if (filasAfectadas > 0) {
                palletEmbarcado = true;
            }
        } catch (SQLException ex) {
            mensaje(ex.getMessage(),"Snapshot SRS");
        } finally {
            if (!conn.isClosed()) {
                conn.close(); // Cierra la conexión con la base de datos
            }
        }
        return palletEmbarcado;
    }
    public boolean recibirPallet(String pallet) throws SQLException {
        boolean palletEmbarcado = false;
        try {
            initConexion();
            String query = "UPDATE ERP_CTRL_SRS_PALLETS SET status = iif(Status = 2, '3', '10') , Fecha_Recibo = getDate() WHERE pallet = ? and Status in ('1','2')";
            PreparedStatement preparedStatement = conn.prepareStatement(query);
            preparedStatement.setString(1, pallet.trim());
            int filasAfectadas = preparedStatement.executeUpdate();

            if (filasAfectadas > 0) {
                palletEmbarcado = true;
            }
        } catch (SQLException ex) {
            mensaje(ex.getMessage(),"Snapshot SRS");
        } finally {
            if (!conn.isClosed()) {
                conn.close(); // Cierra la conexión con la base de datos
            }
        }
        return palletEmbarcado;
    }
    public boolean eliminarPalletEmbarcado(String pallet) throws SQLException {
        boolean update = false;
        try {
            initConexion(); // Inicia la conexión con la base de datos
            String query = "update  ERP_CTRL_SRS_pallets set status = '1' WHERE pallet = ?";
            PreparedStatement preparedStatement = conn.prepareStatement(query);
            preparedStatement.setString(1, pallet.trim());
            int filasAfectadas = preparedStatement.executeUpdate();

            if (filasAfectadas > 0) {
                update = true;
            }
        } catch (SQLException ex) {
            mensaje(ex.getMessage(),"Snapshot SRS");
        } finally {
            if (!conn.isClosed()) {
                conn.close();
            }
        }
        return update;
    }
    public ArrayList<CajaInfo> getPalletsPorTroka(String troka) throws SQLException {
        ArrayList<CajaInfo> cajas = new ArrayList<>();
        try {
            initConexion(); // Inicia la conexión con la base de datos
            String query = "select p.Pallet Pallet,COUNT(a.Pallet) Qty from ERP_CTRL_SRS_PALLETS p\n" +
                    "join ERP_CTRL_SRS_ARTESAS a on a.Pallet = p.Pallet\n" +
                    "where p.Troka = '" + troka.trim() + "' and convert(date,p.fecha_envio,101) = convert(date,getdate(),101) AND A.STATUS = '0'" +
                    " group by p.Pallet";
            ResultSet resultSet = comm.executeQuery(query); // Ejecuta la consulta

            while (resultSet.next()) {
                CajaInfo info = new CajaInfo();
                info.pallet = resultSet.getString("Pallet");
                info.qty = resultSet.getInt("Qty");
                cajas.add(info);
            }
        } catch (SQLException ex) {
            mensaje(ex.getMessage(),"Snapshot SRS");
        } finally {
            if (!conn.isClosed()) {
                conn.close(); // Cierra la conexión con la base de datos
            }
        }
        return cajas;
    }
    public ArrayList<CajaInfo> getDatosPorDia() throws SQLException {
        ArrayList<CajaInfo> cajas = new ArrayList<>();
        try {
            initConexion(); // Inicia la conexión con la base de datos
            String query = "SELECT p.Pallet , count(p.pallet) Qty, convert(varchar,SUM(a.qty)) Qty_SRS FROM ERP_CTRL_SRS_artesas a\n" +
                    "                     join ERP_CTRL_SRS_PALLETS p on p.Pallet = a.Pallet\n" +
                    "                    WHERE convert(date,p.fecha_recibo,101) = convert(date, getDate(),101)\n AND a.STATUS = '0'" +
                    "                    Group by p.pallet";
            ResultSet resultSet = comm.executeQuery(query); // Ejecuta la consulta

            while (resultSet.next()) {
                CajaInfo info = new CajaInfo();
                info.pallet = resultSet.getString("Pallet");
                info.qty = resultSet.getInt("Qty");
                info.caja = resultSet.getString("Qty_SRS");
                cajas.add(info);
            }
        } catch (SQLException ex) {
            mensaje(ex.getMessage(),"Snapshot SRS");
        } finally {
            if (!conn.isClosed()) {
                conn.close(); // Cierra la conexión con la base de datos
            }
        }
        return cajas;
    }
    public CajaInfo getPalletInfo(String pallet) throws SQLException {
        CajaInfo info = new CajaInfo("","", 0,"","");
        try {
            initConexion(); // Inicia la conexión con la base de datos
            String query = "SELECT Pallet , count(pallet) Qty, convert(varchar,SUM(qty)) Qty_SRS FROM ERP_CTRL_SRS_artesas " +
                    "WHERE pallet = '" + pallet.trim() + "' AND STATUS = '0'" +
                    "Group by pallet";
            ResultSet resultSet = comm.executeQuery(query);
            if (resultSet.next()) {
                info.pallet = resultSet.getString("Pallet");
                info.qty = resultSet.getInt("Qty");
                info.caja = resultSet.getString("Qty_SRS");
            }
        } catch (SQLException ex) {
            mensaje(ex.getMessage(),"Snapshot SRS");
        } finally {
            if (!conn.isClosed()) {
                conn.close();
            }
        }
        return info;
    }

}



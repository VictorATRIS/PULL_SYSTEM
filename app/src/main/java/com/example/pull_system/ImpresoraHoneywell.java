package com.example.pull_system;

import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class ImpresoraHoneywell {
    private String ipAddress;
    private int port; // El puerto predeterminado para impresión suele ser el 9100

    public ImpresoraHoneywell(String ipAddress) {
        this(ipAddress, 9100);
    }

    public ImpresoraHoneywell(String ipAddress, int port) {
        this.ipAddress = ipAddress;
        this.port = port;
    }

    public boolean imprimirIPL(String iplCommands) {
        Socket socket = null;
        OutputStream outputStream = null;
        boolean success = false;

        try {

            socket = new Socket();
            socket.connect(new InetSocketAddress(ipAddress, port), 5000); // Timeout de conexión de 5 segundos

            outputStream = socket.getOutputStream();
            outputStream.write(iplCommands.getBytes());
            outputStream.flush();
            success = true;

        } catch (IOException e) {
            e.printStackTrace();
            // Manejar la excepción (por ejemplo, mostrar un mensaje al usuario)
        } finally {
            try {
                if (outputStream != null) {
                    outputStream.close();
                }
                if (socket != null && socket.isConnected()) {
                    socket.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return success;
    }
    public String obtenerEstadoImpresora() {
        Socket socket = null;
        OutputStream outputStream = null;
        InputStream inputStream = null;
        String estado = null;

        try {
            socket = new Socket();
            socket.connect(new InetSocketAddress(ipAddress, port), 5000);

            outputStream = socket.getOutputStream();
            // Comando de consulta de estado (¡VERIFICA LA DOCUMENTACIÓN DE TU PX45!)
            byte[] statusCommand = {(byte) 15, (byte) 'S'}; // <SI>S
            outputStream.write(statusCommand);
            outputStream.flush();

            inputStream = socket.getInputStream();
            byte[] buffer = new byte[1024];
            int bytesRead = inputStream.read(buffer);
            if (bytesRead > 0) {
                estado = new String(buffer, 0, bytesRead, StandardCharsets.UTF_8).trim();
                Log.d("ImpresoraHoneywell", "Respuesta de estado: " + estado);
            } else {
                Log.d("ImpresoraHoneywell", "No se recibió respuesta de estado.");
            }

        } catch (IOException e) {
            Log.e("ImpresoraHoneywell", "Error al obtener el estado: " + e.getMessage());
        } finally {
            try {
                if (inputStream != null) inputStream.close();
                if (outputStream != null) outputStream.close();
                if (socket != null && socket.isConnected()) socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return estado;
    }
}

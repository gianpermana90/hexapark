/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ServerParkir;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.StringWriter;
import java.net.Socket;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.simple.JSONObject;

/**
 *
 * @author Hades
 */
public class Client {

    private static final int PORT = 9090;
    private static final String HOST = "localhost";
    private static Socket sock;
    private static DataInputStream dataInput;
    private static DataOutputStream dataOutput;

    public static void main(String[] args) {
        try {
            //Membuat saluran data
            sock = new Socket(HOST, PORT);
            System.out.println("Connected with server");
            System.out.println("with socket " + sock);
            dataInput = new DataInputStream(sock.getInputStream());
            dataOutput = new DataOutputStream(sock.getOutputStream());

            //Membuat data dalam JSON
            JSONObject obj = new JSONObject();
            obj.put("barcode", "GI31509009755000");
            obj.put("license", "BG 0805 NV");
            obj.put("date", "2017-11-13 16:40:38");
            
            //Send Data
            dataOutput.writeUTF(obj.toString());
            
            System.out.print(obj.toString());

        } catch (IOException ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ServerParkir;

import db.queryTicket;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/**
 *
 * @author Hades
 */
public class Server {

    private static final int PORT = 9090;
    private static ServerSocket sSock;
    private static DataInputStream input;
    private static DataOutputStream output;

    public static void main(String[] args) {
        JSONParser parser = new JSONParser();
        try {
            sSock = new ServerSocket(PORT);
            while (true) {
                System.out.println("Waiting for a connection ...");
                Socket sock = sSock.accept();
                System.out.println("Client with Local PORT " + sock.getPort() + " is connected");
                input = new DataInputStream(sock.getInputStream());
                output = new DataOutputStream(sock.getOutputStream());
                String receivedData = input.readUTF();
                try {
                    JSONObject jsonObject = (JSONObject) parser.parse(receivedData);
                    //Example
                    String barcode = (String) jsonObject.get("barcode");
                    String license = (String) jsonObject.get("license");
                    String date = (String) jsonObject.get("date");
                    System.out.println("Barcode: " + barcode);
                    System.out.println("License Number: " + license);
                    System.out.println("Date: " + date);
                    int update = new queryTicket().insertExitDate(barcode,date);
                    if(update == 1){
                        System.out.println("Vehicle with license number "+(String) jsonObject.get("license")+" left the parking area");
                    }else{
                        System.out.println("Failed to update data");
                    }
                } catch (ParseException ex) {
                    Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
                }

            }
        } catch (IOException ex) {
            Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ServerParkir;

//import cls.User;
//import db.ConnectionManager;
//import db.executeData;
import db.queryTicket;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.Socket;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/**
 *
 * @author Hades
 */
public class ServerThread extends Thread {

    private DataInputStream dataInput;
    private DataOutputStream dataOutput;
    private Socket sock;
    private MainServer ms = new MainServer();

    public ServerThread(Socket sock) {
        this.sock = sock;
    }

    @Override
    public void run() {
        try {
            //Membuat saluran data
            dataInput = new DataInputStream(sock.getInputStream());
            dataOutput = new DataOutputStream(sock.getOutputStream());
            System.out.println("Client with Local PORT " + sock.getPort() + " is connected");
            
            //Termia data
            String receivedData = null;
//            receivedData = dataInput.readLine();  //Python
            receivedData = dataInput.readUTF(); //Java
            
            //Parsing data to JSON
            JSONParser parser = new JSONParser();
            JSONObject jsonObject = (JSONObject) parser.parse(receivedData);
            String barcode = (String) jsonObject.get("barcode");
            String license = (String) jsonObject.get("license");
            String date = (String) jsonObject.get("date");
            
            System.out.println("Barcode: " + barcode);
            System.out.println("License Number: " + license);
            System.out.println("Date: " + date);
            
            //Save information ==> Update parkingtrx table 
            int update = new queryTicket().insertExitDate(barcode, date);
            if (update == 1) {
                System.out.println("[" + sock.getPort() + "] Vehicle with license number " + license + " left the parking area");
            } else {
                System.out.println("[" + sock.getPort() + "] Failed to update data");
            }
            
        } catch (IOException ex) {
            Logger.getLogger(ServerThread.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ParseException ex) {
            Logger.getLogger(ServerThread.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}

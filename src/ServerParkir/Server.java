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
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
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
    private static Socket sock;
    
    public static void main(String[] args) {
        
        try {
            sSock = new ServerSocket(PORT);
            while (true) {
                System.out.println("Waiting for a connection ...");
                sock = sSock.accept();
                ServerThread st = new ServerThread(sock);
                st.start();
            }
        } catch (IOException ex) {
            Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}

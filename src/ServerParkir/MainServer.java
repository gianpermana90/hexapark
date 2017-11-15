/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ServerParkir;

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
import javax.swing.JOptionPane;

/**
 *
 * @author Hades
 */
public class MainServer {

    private static final int PORT = 9090;
    private static ServerSocket sSock;
    private static Socket sock;
    public static List<String> listUser = new ArrayList<String>();
    public static List<Socket> listSock = new ArrayList<Socket>();
    public static Hashtable<String, Socket> hashClient;

    public static void main(String[] args) {
        hashClient = new Hashtable<>();
        try {
            sSock = new ServerSocket(PORT);
            System.out.println("Server is listening . . .");
            while (true) {
                sock = sSock.accept();
                ServerThread st = new ServerThread(sock);
                st.start();
            }
        } catch (IOException ex) {
            Logger.getLogger(MainServer.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}

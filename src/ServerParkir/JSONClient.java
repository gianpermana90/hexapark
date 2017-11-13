///*
// * To change this license header, choose License Headers in Project Properties.
// * To change this template file, choose Tools | Templates
// * and open the template in the editor.
// */
//package ServerParkir;
//
///**
// *
// * @author Hades
// */
////import org.json.JSONObject;
//
//import java.io.*;
//import java.net.ConnectException;
//import java.net.NoRouteToHostException;
//import java.net.Socket;
//import java.net.UnknownHostException;
//import java.util.Scanner;
//
////import model.*;
////import view.*;
//
///**
//
// */
//public class JSONClient {
//
//    private String host;
//    private int port;
//    private Socket socket;
//    private final String DEFAULT_HOST = "localhost";
//
//
//    public void connect(String host, int port) throws IOException {
//        this.host = host;
//        this.port = port;
//        socket = new Socket(host, port);
//        System.out.println("Client has been connected..");
//    }
//
//
//    /**
//     * use the JSON Protocol to receive a json object as
//     *  from the client and reconstructs that object
//     *
//     * @return JSONObejct with the same state (data) as
//     * the JSONObject the client sent as a String msg.
//     * @throws IOException
//     */
//    public JSONObject receiveJSON() throws IOException {
//        InputStream in = socket.getInputStream();
//        ObjectInputStream i = new ObjectInputStream(in);
//        JSONObject line = null;
//        try {
//            line = (JSONObject) i.readObject();
//
//        } catch (ClassNotFoundException e) {
//            // TODO Auto-generated catch block
//             e.printStackTrace();
//
//        }
//
//
//        return line;
//
//    }
//
//
//    public void sendJSON(JSONObject jsonObject) throws IOException {
//           JSONObject jsonObject2 = new JSONObject();
//        jsonObject2.put("key", new Paper(250,333));
//
//
//         OutputStream out = socket.getOutputStream();
//        ObjectOutputStream o = new ObjectOutputStream(out);
//         o.writeObject(jsonObject2);
//        out.flush();
//        System.out.println("Sent to server: " + " " + jsonObject2.get("key").toString());
//    }
//
//
//    public static void main(String[] args) {
//        JSONClient client = new JSONClient();
//        try{
//
//            client.connect("localhost", 7777);
//            // For JSON call sendJSON(JSON json) & receiveJSON();
//            JSONObject jsonObject2 = new JSONObject();
//            jsonObject2.put("key", new Paper(250,333));
//
//            client.sendJSON(jsonObject2);
//            client.receiveJSON();
//        }
//
//        catch (ConnectException e) {
//            System.err.println(client.host + " connect refused");
//            return;
//        }
//
//        catch(UnknownHostException e){
//            System.err.println(client.host + " Unknown host");
//            client.host = client.DEFAULT_HOST;
//            return;
//        }
//
//        catch (NoRouteToHostException e) {
//            System.err.println(client.host + " Unreachable");
//            return;
//
//        }
//
//        catch (IllegalArgumentException e){
//            System.err.println(client.host + " wrong port");
//            return;
//        }
//
//        catch(IOException e){
//            System.err.println(client.host + ' ' + e.getMessage());
//            System.err.println(e);
//        }
//        finally {
//            try {
//                client.socket.close();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
//
//    }
//}

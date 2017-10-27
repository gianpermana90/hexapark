/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package db;

import cls.Ticket;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Hades
 */
public class queryTicket {
    
    SimpleDateFormat entranceTime = new SimpleDateFormat("HH:mm:ss dd/MM/yyyy");
    SimpleDateFormat exitTime = new SimpleDateFormat("HH:mm:ss dd/MM/yyyy");

    public Ticket getData(String barcode) {
        Ticket res = new Ticket();
        Koneksi connect = new Koneksi();
        Connection con = connect.logOn();
        String query = "select * from parkingtrx where trxid = '" +barcode+ "'";
        try{
            Statement stm = con.createStatement();
            ResultSet rs = stm.executeQuery(query);
            while (rs.next()) {
                res.setBarcode(barcode);
                res.setDateEntrance(entranceTime.format(rs.getTimestamp("entrancetime")).toString());
//                res.setDateExit(rs.getString(""));
                res.setGate(rs.getInt("entrancegate"));
                res.setLicenseNumber(rs.getString("numberplate"));
            }
        }catch(SQLException e){
            Logger.getLogger(queryTicket.class.getName()).log(Level.SEVERE, null, e);
        }
        connect.logOff();
        return res;
    }
    
    public int updatePolNum(String idTicket, String number){
        int res = 0;
        Koneksi conn = new Koneksi();
        Connection con = conn.logOn();
        String query = "update parkingtrx set numberplate = '"+number+"' where trxid = '"+idTicket+"'";
        try {
            Statement stm = con.createStatement();
            res = stm.executeUpdate(query);
            System.out.println("Number Plate successfully updated");
        } catch (SQLException ex) {
            Logger.getLogger(queryTicket.class.getName()).log(Level.SEVERE, null, ex);
        }
        conn.logOff();
        return res;
    }

}

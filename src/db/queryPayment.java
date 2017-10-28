/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package db;

import cls.Member;
import cls.Ticket;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Hades
 */
public class queryPayment {

    SimpleDateFormat entranceTime = new SimpleDateFormat("HH:mm:ss dd/MM/yyyy");
    SimpleDateFormat exitTime = new SimpleDateFormat("HH:mm:ss dd/MM/yyyy");

    private int getPrice(Ticket pym) {
        int res = 0;
        String query = "select * from tariff where types = '" + pym.getTarifTypes()+ "';";
        Koneksi connect = new Koneksi();
        Connection con = connect.logOn();
        int initTime;
        int perHourPrice;
        int nextHourPrice;
        int oneDayPrice;
        try {
            Statement stm = con.createStatement();
            ResultSet rs = stm.executeQuery(query);
            while (rs.next()) {
                initTime = rs.getInt(3);
                perHourPrice = rs.getInt(4);
                nextHourPrice = rs.getInt(5);
                oneDayPrice = rs.getInt(6);
            }
        } catch (SQLException ex) {
            Logger.getLogger(queryPayment.class.getName()).log(Level.SEVERE, null, ex);
        }
        return res;
    }
    
    private Member getMemberDetails(String licenseNum, Connection con) {
        Member res = new Member();
        //Get Data Member
        String queryGetMember = "SELECT b.nopol, a.mdalamat, a.mdnama, a.mdid FROM memberdetails a, membernopol b WHERE b.nopol = '" + licenseNum + "' AND b.Memberid = a.mdid; ";
        try {
            Statement stm = con.createStatement();
            ResultSet rs = stm.executeQuery(queryGetMember);
            res.setLicenseNumber(rs.getString(1));
            res.setInstansi(rs.getString(2));
            res.setName(rs.getString(3));
            res.setMemberID(rs.getString(4));

            //Cek Status dan Tanggal Masa Berlaku Akun Member
            Date validDate;
            String queryMasaBerlaku = "select validuntil from memberdetails where mdid = '" + res.getMemberID() + "'";
            Statement stm2 = con.createStatement();
            ResultSet rs2 = stm2.executeQuery(queryMasaBerlaku);
            if (rs2.next()) {
                validDate = rs2.getDate(1);
                System.out.println("Masa Berlaku Hingga : " + validDate);

                Calendar dateNow = Calendar.getInstance();
                Calendar dateValid = Calendar.getInstance();
                Date skr = new Date();
                dateNow.setTime(skr);
                dateValid.setTime(validDate);
                if (dateNow.before(dateValid)) {
                    res.setStatus("Aktif");
                    //System.out.println(res.getStatus());
                } else {
                    res.setStatus("Tidak Aktif");
                }
            }
            System.out.println("Status Member : " + res.getStatus());

        } catch (SQLException ex) {
            Logger.getLogger(queryPayment.class.getName()).log(Level.SEVERE, null, ex);
        }
        return res;
    }
    
    private String cekMember(String licenseNum){
        String res = "";
        Koneksi connect = new Koneksi();
        Connection con = connect.logOn();
        String query = "select * from membernopol where nopol = '" + licenseNum + "'";
        try {
            Statement stm = con.createStatement();
            ResultSet rs = stm.executeQuery(query);
            if(rs.next()){
                res = "YES";
            }else{
                res = "NO";
            }
        } catch (SQLException ex) {
            Logger.getLogger(queryPayment.class.getName()).log(Level.SEVERE, null, ex);
        }
        return res;
    }
}

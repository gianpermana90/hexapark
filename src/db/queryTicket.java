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
public class queryTicket {
    
    //SimpleDateFormat entranceTime = new SimpleDateFormat("HH:mm:ss dd/MM/yyyy");
    SimpleDateFormat entranceTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    SimpleDateFormat exitTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    //SimpleDateFormat exitTime = new SimpleDateFormat("HH:mm:ss dd/MM/yyyy");

    public Ticket getData(String barcode) {
        Ticket res = new Ticket();
        Koneksi connect = new Koneksi();
        Connection con = connect.logOn();
        String query = "select * from parkingtrx where trxid = '" +barcode+ "'";
        try{
            Statement stm = con.createStatement();
            ResultSet rs = stm.executeQuery(query);
            while (rs.next()) {
                //Keseluruhan atribut yang tersedia di database untuk tabel parkingtrx
                res.setBarcode(barcode);
                res.setEntranceGate(rs.getInt("entrancegate"));
                res.setEntranceTime(entranceTime.format(rs.getTimestamp("entrancetime")).toString());
                res.setExitGate(rs.getInt("exitgate"));
                res.setExitTime(exitTime.format(rs.getTimestamp("exittime")).toString());
                res.setPrice(rs.getInt("amounttopay"));
//                res.setPaymentTime(exitTime.format(rs.getTimestamp("paymenttime")).toString());
                res.setPaymentMethods(rs.getString("paymentmethods"));
                res.setLicenseNumber(rs.getString("numberplate"));
                res.setVehicleTypes(rs.getString("vehicletypes"));
                res.setTarifTypes(rs.getString("tarifftypes"));
                res.setOverNightParking(rs.getString("overnightparking"));
//                res.setGateInPicture1(rs.getString("gateinpicone"));
//                res.setGateInPicture2(rs.getString("gateinpictwo"));
//                res.setGateOutPicture1(rs.getString("gateoutpic"));
//                res.setGateOutPicture2(rs.getString("gateoutpictwo"));
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
    
    public int insertExitDate(String barcode, String date){
        int res = 0;
        Koneksi conn = new Koneksi();
        Connection con = conn.logOn();
        String query = "UPDATE parkingtrx set exittime = '"+date+"' where trxid = '"+barcode+"'";
        try {
            Statement stm = con.createStatement();
            res = stm.executeUpdate(query);
        } catch (SQLException ex) {
            Logger.getLogger(queryTicket.class.getName()).log(Level.SEVERE, null, ex);
        }
        conn.logOff();
        return res;
    }
    
    public Member getMemberDetails(int memberID) {
        Member res = new Member();
        Koneksi connect = new Koneksi();
        Connection con = connect.logOn();
        //Get Data Member
        String queryGetMember = "select b.nopol, a.mdalamat, a.mdnama, a.mdid FROM memberdetails a, membernopol b where b.memberid = " + memberID + " and a.mdid = b.memberid";
        try {
            Statement stm = con.createStatement();
            ResultSet rs = stm.executeQuery(queryGetMember);
            while (rs.next()) {
                res.setLicenseNumber(rs.getString(1));
                res.setAddress(rs.getString(2));
                res.setName(rs.getString(3));
                res.setMemberID(Integer.toString(rs.getInt(4)));
            }

            //Cek Status dan Tanggal Masa Berlaku Akun Member
            Date validDate;
            Date validFrom;
            String queryMasaBerlaku = "select validuntil, validfrom from memberdetails where mdid = '" + res.getMemberID() + "'";
            Statement stm2 = con.createStatement();
            ResultSet rs2 = stm2.executeQuery(queryMasaBerlaku);
            if (rs2.next()) {
                validDate = rs2.getDate(1);
                validFrom = rs2.getDate(2);
                System.out.println("Masa Berlaku Hingga : " + validDate);
                res.setValidFrom(validFrom.toString());
                res.setValidUntil(validDate.toString());
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
        connect.logOff();
        return res;
    }

}

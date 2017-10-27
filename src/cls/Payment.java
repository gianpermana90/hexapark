/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cls;

/**
 *
 * @author Hades
 */
public class Payment {

    private String kode;
    private int gate;
    private String LicenceNum;
    private String vehicleType;
    private String tarifType;
    private String dateEntrance;
    private String dateExit;
    private String memberStatus;
    private int price;

    public Payment() {
    }

    public Payment(String kode) {
        this.kode = kode;
    }

    public String getMemberStatus() {
        return memberStatus;
    }

    public void setMemberStatus(String memberStatus) {
        this.memberStatus = memberStatus;
    }

    public String getKode() {
        return kode;
    }

    public void setKode(String kode) {
        this.kode = kode;
    }

    public int getGate() {
        return gate;
    }

    public void setGate(int gate) {
        this.gate = gate;
    }

    public String getLicenseNum() {
        return LicenceNum;
    }

    public void setLicenseNum(String LicenceNum) {
        this.LicenceNum = LicenceNum;
    }

    public String getVehicleType() {
        return vehicleType;
    }

    public void setVehivleType(String vehicleType) {
        this.vehicleType = vehicleType;
    }

    public String getDateEntrance() {
        return dateEntrance;
    }

    public void setDateEntrance(String dateEntrance) {
        this.dateEntrance = dateEntrance;
    }

    public String getDateExit() {
        return dateExit;
    }

    public void setDateExit(String dateExit) {
        this.dateExit = dateExit;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }
    
    public void setPrice(int price, int pri) {
        this.price = price;
    }

    public String getTarifType() {
        return tarifType;
    }

    public void setTarifType(String tarifType) {
        this.tarifType = tarifType;
    }

    @Override
    public String toString() {
        return "Payment{" + "kode=" + kode + ", gate=" + gate + ", LicenceNum=" + LicenceNum + ", vehicleType=" + vehicleType + ", tarifType=" + tarifType + ", dateEntrance=" + dateEntrance + ", dateExit=" + dateExit + ", memberStatus=" + memberStatus + ", price=" + price + '}';
    }

}

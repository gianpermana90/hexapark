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
public class Ticket {

    private String barcode;
    private String dateEntrance;
    private String dateExit;
    private String licenseNumber;
    private int gate;

    public String getLicenseNumber() {
        return licenseNumber;
    }

    public void setLicenseNumber(String licenseNumber) {
        this.licenseNumber = licenseNumber;
    }

    public int getGate() {
        return gate;
    }

    public void setGate(int gate) {
        this.gate = gate;
    }

    public String getBarcode() {
        return barcode;
    }

    public void setBarcode(String barcode) {
        this.barcode = barcode;
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

    public Ticket() {
    }

    @Override
    public String toString() {
        return "Ticket{" + "barcode=" + barcode + ", dateEntrance=" + dateEntrance + ", dateExit=" + dateExit + ", licenseNumber=" + licenseNumber + ", gate=" + gate + '}';
    }

}

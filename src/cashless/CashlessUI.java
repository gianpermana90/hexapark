/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cashless;

import cls.Member;
import tools.WebcamCapture;
import tools.Camera;
import cls.Ticket;
import config.Params;
import db.queryPayment;
import db.queryTicket;
import java.awt.AWTException;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import static java.awt.print.Printable.NO_SUCH_PAGE;
import static java.awt.print.Printable.PAGE_EXISTS;
import java.awt.print.PrinterException;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import org.apache.commons.codec.binary.Base64;
import rfid.RFIDcommand;

/**
 *
 * @author Hades
 */
public class CashlessUI extends javax.swing.JFrame {

    private Ticket tkt = new Ticket();
    private Member mbr = new Member();
    private Robot rbt;

    private CardLayout clMainPanel;
    private CardLayout clTiketMain;
    private CardLayout clTiketStep;
    private CardLayout clUpdateLicenseBtn;
    private CardLayout clMemberMain;
    private CardLayout clMemberRenewalMain;

    SimpleDateFormat entranceTime = new SimpleDateFormat("HH:mm:ss dd/MM/yyyy");
    SimpleDateFormat exitTime = new SimpleDateFormat("HH:mm:ss dd/MM/yyyy");
    //initial value for barcode program
    private static final long THRESHOLD = 100;
    private static final int MIN_BARCODE_LENGTH = 16;
    private final StringBuffer barcode = new StringBuffer();
    private long lastEventTimeStamp = 0L;
    //initial config
    private static final String fileConfig = "config.txt";
    //initial variable for Get Pic
    private static final int BUFFER_SIZE = 4096;
    private String saveFilePath;
    //initial RFID command
    private RFIDcommand card = new RFIDcommand();
    private String expired;
    private int[] newExpired = {0x00, 0x00, 0x2d, 0x00, 0x00, 0x2d, 0x00, 0x00, 0x00, 0x00, 0x20, 0x20, 0x20, 0x20, 0x20, 0x20};

    /**
     * Creates new form Main
     */
    public CashlessUI() {
        initComponents();
        //Set initial final variable that would be used by program
        initialConfig();
        //setLocationRelativeTo(this);
        //Set Fullscreen
        makeFrameFullSize(this);
        //Initialize all Cardlayout
        clMainPanel = (CardLayout) mainPanel.getLayout();
        clTiketMain = (CardLayout) panelTiketMain.getLayout();
        clTiketStep = (CardLayout) panelTiketStep.getLayout();
        clUpdateLicenseBtn = (CardLayout) panelUpdateNoPol.getLayout();
        clMemberMain = (CardLayout) panelMemberMain.getLayout();
        clMemberRenewalMain = (CardLayout) panelMemberRenewalMain.getLayout();

        try {
            this.rbt = new Robot();
        } catch (AWTException ex) {
            Logger.getLogger(CashlessUI.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    private void makeFrameFullSize(JFrame aFrame) {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        aFrame.setSize(screenSize.width, screenSize.height);
    }

    private void checkRenewalDate() {
        // Get Current Time
        Calendar dateNow = Calendar.getInstance();
        Date now = new Date();
        dateNow.setTime(now);
        SimpleDateFormat df = new SimpleDateFormat("dd-MM-yyyy");
        //Get Last Date from current Month
        Date lastDate = new Date((dateNow.get(Calendar.MONTH) + 1) + "/" + dateNow.getActualMaximum(Calendar.DATE) + "/" + dateNow.get(Calendar.YEAR));
        //Set Begin renewal Time (-7 = last week on current month / 7 days before the end of current month)
        Date startDate = manipulateDays(lastDate, -7);
        if ((now.after(startDate)) && (now.before(lastDate))) {
            clMemberRenewalMain.show(panelMemberRenewalMain, "cardMemberRenewalActive");
        } else {
            clMemberRenewalMain.show(panelMemberRenewalMain, "cardMemberRenewalInactive");
        }
    }

    public static Date manipulateDays(Date date, int days) {
        GregorianCalendar cal = new GregorianCalendar();
        cal.setTime(date);
        cal.add(Calendar.DATE, days);
        return cal.getTime();
    }

    private void initialConfig() {
        BufferedReader br = null;
        FileReader fr = null;
        try {
            fr = new FileReader(fileConfig);
            br = new BufferedReader(fr);
            Params.DBurl = br.readLine();
            Params.username = br.readLine();
            Params.password = br.readLine();
            Params.pathProgram = br.readLine();
            Params.ipCam1 = br.readLine();
            Params.ipCam2 = br.readLine();
            Params.ScannerMode = Integer.parseInt(br.readLine());
            System.out.println("Configuration");
            System.out.println("===================");
            System.out.println("Database Server : " + Params.DBurl);
            System.out.println("Database Username : " + Params.username);
            System.out.println("Database Password : " + Params.password);
            System.out.println("Path file : " + Params.pathProgram);
            System.out.println("IP Cam 1 : " + Params.ipCam1);
            System.out.println("IP Cam 2 : " + Params.ipCam2);
            System.out.println("Scanner Mode : " + Params.ScannerMode);
            System.out.println("===================");
        } catch (FileNotFoundException ex) {
//            Logger.getLogger(GateOut.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println("File config.txt tidak ditemukan");
        } catch (IOException ex) {
//            Logger.getLogger(GateOut.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println("Error membaca file config.txt");
        } finally {
            try {
                if (br != null) {
                    br.close();
                }
                if (fr != null) {
                    fr.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    private void barcodeListener() {
        KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(new KeyEventDispatcher() {
            @Override
            public boolean dispatchKeyEvent(KeyEvent e) {
                if (e.getID() != KeyEvent.KEY_RELEASED) {
                    return false;
                }
                if (e.getWhen() - lastEventTimeStamp > THRESHOLD) {
                    barcode.delete(0, barcode.length());
                }
                lastEventTimeStamp = e.getWhen();

                if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    KeyboardFocusManager.getCurrentKeyboardFocusManager().removeKeyEventDispatcher(this);
                    clMainPanel.show(mainPanel, "mainCardMenu");
                } else {
                    barcode.append(e.getKeyChar());
                    if (barcode.length() == MIN_BARCODE_LENGTH) {
                        String code = barcode.toString();
                        if (!code.equals("")) {
                            //Isi Objek Tiket dengan data dari database
                            getDataTicket(code);
                            //Tampil Info Parkir
                            clTiketMain.show(panelTiketMain, "card_parkingInfo");
                            clTiketStep.show(panelTiketStep, "cardInfo");
                            try {
                                showParkingPhotos();
                            } catch (Exception ex) {
                                Logger.getLogger(CashlessUI.class.getName()).log(Level.SEVERE, null, ex);
                            }
                            KeyboardFocusManager.getCurrentKeyboardFocusManager().removeKeyEventDispatcher(this);
                        } else {
                            System.out.println("Barcode tidak ditemukan");
                        }
                        barcode.delete(0, barcode.length());
                    }
                }
                return false;
            }
        });
    }

    private void showParkingPhotos() {
        try {
            getPic("image1.jpg");
            showImage(saveFilePath, labelCam1);
            getPic("image2.jpg");
            showImage(saveFilePath, labelCam2);
        } catch (Exception ex) {
            Logger.getLogger(CashlessUI.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void showImage(String filePath, JLabel labelCam1) {
        BufferedImage image1 = null;
        Image cam1 = null;
        String errorCode = "";
        if (!filePath.equals("")) {
            try {
                image1 = ImageIO.read(new File(filePath));
                cam1 = image1.getScaledInstance(labelCam1.getWidth(), labelCam1.getHeight(), Image.SCALE_SMOOTH);
                labelCam1.setIcon(new ImageIcon(cam1));
            } catch (IOException exp) {
                errorCode = "Gambar Tidak Ditemukan";
            }
            if (errorCode.equalsIgnoreCase("gambar tidak ditemukan")) {
                labelCam1.setText(errorCode);
                labelCam1.setIcon(null);
            } else {
                labelCam1.setText("");
            }
        } else {
            labelCam1.setText("Gambar Tidak Ditemukan");
            labelCam1.setIcon(null);
        }

    }

    private void getDataTicket(String code) {
        tkt = new queryTicket().getData(code);
        tkt_barcode.setText(code);
        tkt_gate.setText(Integer.toString(tkt.getEntranceGate()));
        tkt_entrance.setText(tkt.getEntranceTime());
        tkt_noPol.setText(tkt.getLicenseNumber());

        //Testing Cek Harga
//        queryPayment qp = new queryPayment();
//        int[] tarifInfo = qp.getPriceInfo(tkt);
//        System.out.println("Tarif Info");
//        System.out.print(tarifInfo[0] + " ");
//        System.out.print(tarifInfo[1] + " ");
//        System.out.print(tarifInfo[2] + " ");
//        System.out.println(tarifInfo[3]);
//        int harga = 0;
//        if (qp.cekMember(tkt.getBarcode()).equalsIgnoreCase("YES")) {
//            System.out.println("Member Mah Gratis");
//        } else if (tkt.getTarifTypes().equalsIgnoreCase("I A")) {
//            System.out.println(tkt.getTarifTypes());
//        } else if (tkt.getTarifTypes().equalsIgnoreCase("I B")) {
//            System.out.println(tkt.getTarifTypes());
//        } else if (tkt.getTarifTypes().equalsIgnoreCase("I C")) {
//            System.out.println(tkt.getTarifTypes());
//        } else if (tkt.getTarifTypes().equalsIgnoreCase("III A")) {
//            System.out.println(tkt.getTarifTypes());
//            harga = qp.determinePriceInap(tkt.getEntranceTime(), tkt.getExitTime(), tarifInfo[1], tarifInfo[2], tarifInfo[0]);
//        } else if (tkt.getTarifTypes().equalsIgnoreCase("IV")) {
//            System.out.println(tkt.getTarifTypes());
//            harga = qp.determinePriceInap(tkt.getEntranceTime(), tkt.getExitTime(), tarifInfo[1], tarifInfo[2], tarifInfo[0]);
//        }
//        System.out.println(harga);
    }

    private void getDataPayment(String code) {
        txtTrx_Gate.setText(Integer.toString(tkt.getEntranceGate()));
        txtTrx_PolNum.setText(tkt.getLicenseNumber());
        txtTrx_type.setText(tkt.getVehicleTypes());
        txtTrx_entrance.setText(tkt.getEntranceTime());
        //Set Date Exit
        Calendar dateNow = Calendar.getInstance();
        Date skr = new Date();
        dateNow.setTime(skr);
        txtTrx_Exit.setText(exitTime.format(skr));
        txtTrx_Price.setText("RP." + tkt.getPrice() + ".-");
    }

    public void getDataMember(int kode) {
        mbr = new queryTicket().getMemberDetails(kode);
//        System.out.println(mbr.toString());
    }

    public void renewalMember() {
        int[] renewalDate = {0x00, 0x00, 0x2d, 0x00, 0x00, 0x2d, 0x00, 0x00, 0x00, 0x00, 0x20, 0x20, 0x20, 0x20, 0x20, 0x20};

        card.writeTAG(0x10, renewalDate);
    }

    public String convertStringToHex(String str) {
        char[] chars = str.toCharArray();
        StringBuffer hex = new StringBuffer();
        for (int i = 0; i < chars.length; i++) {
            hex.append(Integer.toHexString((int) chars[i]));
        }
        return hex.toString();
    }

    private int[] ascii2hexDATE(int date, int month, int year) {
        int[] res = {0x00, 0x00, 0x2d, 0x00, 0x00, 0x2d, 0x00, 0x00, 0x00, 0x00, 0x20, 0x20, 0x20, 0x20, 0x20, 0x20};
        //parsing date
        res[0] = Integer.parseInt(Integer.toHexString(date));
        res[1] = 0x00;
        //parsing month
        //parsing year
        return res;
    }

    private void getPic(String ip) throws Exception {
        //String urlip = "http://" + ip + "/cgi-bin/snapshot.cgi";
        String urlip = "http://192.168.43.149/giantlab/aaa/" + ip;
        URL url = new URL(urlip);
        HttpURLConnection httpConn = (HttpURLConnection) url.openConnection();
        String basicAuth = "Basic " + new String(Base64.encodeBase64("admin:admin".getBytes())); //jangan kesini buat ambil dari server
        httpConn.setRequestProperty("Authorization", basicAuth); //jangan kesini buat ambil dari server
        //System.out.println("Basic Out "+basicAuth); //jangan kesini buat ambil dari server
        //httpConn.setRequestMethod("GET");
        //httpConn.setDoOutput(true);
        int responseCode = httpConn.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK) {
            String fileName = "";
            String disposition = httpConn.getHeaderField("Content-Disposition");
            String contentType = httpConn.getContentType();
            int contentLength = httpConn.getContentLength();
            fileName = ip + ".jpg";
            //System.out.println("Content-Type = " + contentType);
            //System.out.println("Content-Disposition = " + disposition);
            //System.out.println("Content-Length = " + contentLength);
            //System.out.println("fileName = " + fileName);
            InputStream inputStream = httpConn.getInputStream();
            saveFilePath = Params.pathProgram + File.separator + fileName;
//            System.out.println(saveFilePath);
            FileOutputStream outputStream = new FileOutputStream(saveFilePath);
            int bytesRead = -1;
            byte[] buffer = new byte[BUFFER_SIZE];
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
            outputStream.close();
            inputStream.close();
            System.out.println("File downloaded");
        } else {
            System.out.println("No file to download. Server replied HTTP code: " + responseCode);
            saveFilePath = "";
        }
        httpConn.disconnect();
    }

    public class BillPrintable implements Printable {

        public int print(Graphics graphics, PageFormat pageFormat, int pageIndex)
                throws PrinterException {

            int result = NO_SUCH_PAGE;
            if (pageIndex == 0) {

                Graphics2D g2d = (Graphics2D) graphics;

                double width = pageFormat.getImageableWidth();

                g2d.translate((int) pageFormat.getImageableX(), (int) pageFormat.getImageableY());

                ////////// code by alqama//////////////
                FontMetrics metrics = g2d.getFontMetrics(new Font("Arial", Font.BOLD, 7));
                //    int idLength=metrics.stringWidth("000000");
                //int idLength=metrics.stringWidth("00");
                int idLength = metrics.stringWidth("000");
                int amtLength = metrics.stringWidth("000000");
                int qtyLength = metrics.stringWidth("00000");
                int priceLength = metrics.stringWidth("000000");
                int prodLength = (int) width - idLength - amtLength - qtyLength - priceLength - 17;

                //    int idPosition=0;
                //    int productPosition=idPosition + idLength + 2;
                //    int pricePosition=productPosition + prodLength +10;
                //    int qtyPosition=pricePosition + priceLength + 2;
                //    int amtPosition=qtyPosition + qtyLength + 2;
                int productPosition = 0;
                int discountPosition = prodLength + 5;
                int pricePosition = discountPosition + idLength + 10;
                int qtyPosition = pricePosition + priceLength + 4;
                int amtPosition = qtyPosition + qtyLength;

                try {
                    /*Draw Header*/
                    int y = 20;
                    int yShift = 10;
                    int headerRectHeight = 15;
                    int headerRectHeighta = 40;

                    ///////////////// Product names Get ///////////
                    String nokarcis = tkt.getBarcode();
                    String jPark = tkt.getVehicleTypes();
                    String nopol = tkt.getLicenseNumber();
                    String tgl_masuk = tkt.getEntranceTime();
                    String tgl_keluar = tkt.getExitTime();
                    String pos_masuk = Integer.toString(tkt.getEntranceGate());
                    String pos_keluar = Integer.toString(tkt.getExitGate());
                    String lama = tkt.getEntranceTime();
                    String[] lamaSplit = lama.split(" :");
                    String total = Integer.toString(tkt.getPrice());
                    ///////////////// Product names Get ///////////

                    ///////////////// SET LAYOUT /////////////////
                    g2d.setFont(new Font("Monospaced", Font.PLAIN, 9));
                    g2d.drawString("-------------------------------------", 12, y);
                    y += yShift;
                    g2d.drawString("TANDA BUKTI PEMBAYARAN PARKIR", 12, y);
                    y += yShift;
                    g2d.drawString("       PRODEXA PARKING     ", 12, y);
                    y += yShift;
                    g2d.drawString("        BANDARA SMB II     ", 12, y);
                    y += yShift;
                    g2d.drawString("-------------------------------------", 12, y);
                    y += headerRectHeight;

                    g2d.drawString("-------------------------------------", 9, y);
                    y += yShift;
                    g2d.drawString(" Nomor Polisi: " + nopol + "/" + jPark + "   ", 9, y);
                    y += yShift;
                    g2d.drawString(" POS Masuk   : " + pos_masuk + "   ", 9, y);
                    y += yShift;
                    g2d.drawString(" Waktu Masuk : " + tgl_masuk + " ", 9, y);
                    y += yShift;
                    g2d.drawString(" POS Keluar  : " + pos_keluar + "   ", 9, y);
                    y += yShift;
                    g2d.drawString(" Waktu Keluar: " + tgl_keluar + " ", 9, y);
                    y += yShift;

                    if (lamaSplit.length == 4) {
                        g2d.drawString(" Lama        : " + lamaSplit[0] + "" + lamaSplit[1] + "   ", 9, y);
                        y += yShift;
                        g2d.drawString("                  " + lamaSplit[2] + "" + lamaSplit[3] + "   ", 9, y);
                        y += yShift;
                        System.out.print("" + lamaSplit[0]);
                        System.out.print("" + lamaSplit[1]);
                        System.out.print("" + lamaSplit[2]);
                        System.out.print("" + lamaSplit[3]);
                    } else if (lamaSplit.length == 3) {
                        g2d.drawString(" Lama        : " + lamaSplit[0] + "" + lamaSplit[1] + "   ", 9, y);
                        y += yShift;
                        g2d.drawString("                  " + lamaSplit[2] + "                   ", 9, y);
                        y += yShift;
                        System.out.print("" + lamaSplit[0]);
                        System.out.print("" + lamaSplit[1]);
                        System.out.print("" + lamaSplit[2]);
                    } else if (lamaSplit.length == 2) {
                        g2d.drawString(" Lama        : " + lamaSplit[0] + "" + lamaSplit[1] + "   ", 9, y);
                        y += yShift;
                        System.out.print("" + lamaSplit[0]);
                        System.out.print("" + lamaSplit[1]);
                    } else if (lamaSplit.length == 1) {
                        g2d.drawString(" Lama        : " + lamaSplit[0] + "                   ", 9, y);
                        y += yShift;
                        System.out.print("" + lamaSplit[0]);
                    }

                    g2d.drawString(" Total       : " + total + "   ", 9, y);
                    y += yShift;
                    g2d.drawString("-------------------------------------", 10, y);
                    y += headerRectHeight;
                    g2d.drawString("-------------------------------------", 10, y);
                    y += yShift;
                    g2d.drawString("      HATI HATI DIJALAN          ", 10, y);
                    y += yShift;
                    g2d.drawString("         TERIMA KASIH             ", 10, y);
                    y += yShift;
                    g2d.drawString("-------------------------------------", 10, y);
                    y += yShift;
                    g2d.drawString("                                     ", 10, y);
                    y += yShift;
                    g2d.drawString("                                     ", 10, y);
                    y += yShift;
                    g2d.drawString("                                     ", 10, y);
                    y += yShift;

                    g2d.setFont(new Font("Monospaced", Font.BOLD, 10));
                    g2d.drawString("", 30, y);
                    y += yShift;

                } catch (Exception r) {
                    r.printStackTrace();
                }

                result = PAGE_EXISTS;
            }
            return result;
        }
    }

    private void updateExpireDate() {
//        expired = card.readTAG(0x0A);
//        expired = expired.replaceAll("\\s+","");
//        String[] arrExp = expired.split("-");
        //Char 0-1 -> date
        newExpired[0] = Integer.parseInt(convertStringToHex("0"));
        newExpired[1] = Integer.parseInt(convertStringToHex("8"));
        //Char 3-4 -> month
        newExpired[3] = Integer.parseInt(convertStringToHex("0"));
        newExpired[4] = Integer.parseInt(convertStringToHex("5"));
        //Char 6-9 -> year
        newExpired[6] = Integer.parseInt(convertStringToHex("2"));
        newExpired[7] = Integer.parseInt(convertStringToHex("0"));
        newExpired[8] = Integer.parseInt(convertStringToHex("1"));
        newExpired[9] = Integer.parseInt(convertStringToHex("8"));
        //Write to card
        try{
            card.writeTAG(0x10, newExpired);
        }catch(Exception e){
            System.out.println("Error Update Expired Date !! ");
        }
    }

    /**
     * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The content of this method is always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jComboBox1 = new javax.swing.JComboBox();
        mainPanel = new javax.swing.JPanel();
        panelMenu = new javax.swing.JPanel();
        panelMenuMain = new javax.swing.JPanel();
        panelMenuTittle = new javax.swing.JPanel();
        jPanel9 = new javax.swing.JPanel();
        jLabel43 = new javax.swing.JLabel();
        jLabel44 = new javax.swing.JLabel();
        jLabel45 = new javax.swing.JLabel();
        panelMenuButton = new javax.swing.JPanel();
        jPanel11 = new javax.swing.JPanel();
        btnMenuPayment = new javax.swing.JButton();
        btnMenuMember = new javax.swing.JButton();
        panelTiket = new javax.swing.JPanel();
        panelTiketStep = new javax.swing.JPanel();
        panelStep1 = new javax.swing.JPanel();
        img_stepScan = new javax.swing.JLabel();
        panelStep2 = new javax.swing.JPanel();
        img_stepInfo = new javax.swing.JLabel();
        panelStep3 = new javax.swing.JPanel();
        img_stepPayment = new javax.swing.JLabel();
        panelStep4 = new javax.swing.JPanel();
        img_stepFinish = new javax.swing.JLabel();
        panelTiketMain = new javax.swing.JPanel();
        panelScanBarcode = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jLabel27 = new javax.swing.JLabel();
        btnTiketCancelScan = new javax.swing.JButton();
        panelParkingInfo = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        panelInfo = new javax.swing.JPanel();
        jPanel1 = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        tkt_barcode = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        tkt_gate = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        tkt_entrance = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        tkt_noPol = new javax.swing.JTextField();
        panelImage1 = new javax.swing.JPanel();
        labelCam1 = new javax.swing.JLabel();
        panelImage2 = new javax.swing.JPanel();
        labelCam2 = new javax.swing.JLabel();
        panelUpdateNoPol = new javax.swing.JPanel();
        panelMenuParkingInfo = new javax.swing.JPanel();
        btnChangePolNum = new javax.swing.JButton();
        btnPaymentProcess = new javax.swing.JButton();
        btnCancelInfo = new javax.swing.JButton();
        panelKonfirmUpdate = new javax.swing.JPanel();
        jButton3 = new javax.swing.JButton();
        jButton4 = new javax.swing.JButton();
        panelPayment = new javax.swing.JPanel();
        panelInfoPayment = new javax.swing.JPanel();
        jPanel7 = new javax.swing.JPanel();
        jLabel8 = new javax.swing.JLabel();
        txtTrx_Gate = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        txtTrx_PolNum = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();
        txtTrx_type = new javax.swing.JLabel();
        jLabel11 = new javax.swing.JLabel();
        txtTrx_entrance = new javax.swing.JLabel();
        jLabel12 = new javax.swing.JLabel();
        txtTrx_Exit = new javax.swing.JLabel();
        jLabel13 = new javax.swing.JLabel();
        txtTrx_Price = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        panelMenuPayment = new javax.swing.JPanel();
        jPanel5 = new javax.swing.JPanel();
        jButton1 = new javax.swing.JButton();
        btnCancelPayment = new javax.swing.JButton();
        jLabel14 = new javax.swing.JLabel();
        panelFinish = new javax.swing.JPanel();
        jLabel15 = new javax.swing.JLabel();
        jLabel18 = new javax.swing.JLabel();
        panelAccountBalanceInfo = new javax.swing.JPanel();
        jPanel12 = new javax.swing.JPanel();
        jLabel16 = new javax.swing.JLabel();
        txtFin_Price = new javax.swing.JLabel();
        jLabel17 = new javax.swing.JLabel();
        txtFin_balance = new javax.swing.JLabel();
        btnTiketFinish = new javax.swing.JButton();
        panelMember = new javax.swing.JPanel();
        panelMemberMain = new javax.swing.JPanel();
        panelMemberFirst = new javax.swing.JPanel();
        jPanel16 = new javax.swing.JPanel();
        jPanel17 = new javax.swing.JPanel();
        jLabel25 = new javax.swing.JLabel();
        jLabel26 = new javax.swing.JLabel();
        jLabel28 = new javax.swing.JLabel();
        panelMemberCheck = new javax.swing.JPanel();
        jPanel8 = new javax.swing.JPanel();
        jLabel20 = new javax.swing.JLabel();
        jPanel10 = new javax.swing.JPanel();
        jPanel13 = new javax.swing.JPanel();
        jLabel29 = new javax.swing.JLabel();
        lblMemberName = new javax.swing.JLabel();
        jLabel31 = new javax.swing.JLabel();
        lblMemberInstansi = new javax.swing.JLabel();
        jLabel33 = new javax.swing.JLabel();
        lblMemberLicense = new javax.swing.JLabel();
        jLabel35 = new javax.swing.JLabel();
        lblMemberValidFrom = new javax.swing.JLabel();
        jLabel37 = new javax.swing.JLabel();
        lblMemberValidUntil = new javax.swing.JLabel();
        jLabel39 = new javax.swing.JLabel();
        lblMemberStatus = new javax.swing.JLabel();
        jButton2 = new javax.swing.JButton();
        jPanel14 = new javax.swing.JPanel();
        lblMemberCheckInfo = new javax.swing.JLabel();
        panelMemberRenewal = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();
        jLabel19 = new javax.swing.JLabel();
        panelMemberRenewalMain = new javax.swing.JPanel();
        panelMemberRenewalInactive = new javax.swing.JPanel();
        jLabel23 = new javax.swing.JLabel();
        jLabel21 = new javax.swing.JLabel();
        jLabel22 = new javax.swing.JLabel();
        panelMemberRenewalActive = new javax.swing.JPanel();
        jButton5 = new javax.swing.JButton();
        panelMemberButton = new javax.swing.JPanel();
        gridMemberbutton = new javax.swing.JPanel();
        btnMemberCekInfo = new javax.swing.JButton();
        btnMemberRenewal = new javax.swing.JButton();
        btnMemberExit = new javax.swing.JButton();

        jComboBox1.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setBackground(new java.awt.Color(0, 0, 0));
        setMinimumSize(new java.awt.Dimension(1366, 768));
        setUndecorated(true);
        setResizable(false);

        mainPanel.setLayout(new java.awt.CardLayout());

        jPanel9.setLayout(new java.awt.GridLayout(3, 0, 0, 25));

        jLabel43.setFont(new java.awt.Font("Tahoma", 1, 36)); // NOI18N
        jLabel43.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel43.setText("PRODEXA PARKING");
        jPanel9.add(jLabel43);

        jLabel44.setFont(new java.awt.Font("Tahoma", 1, 36)); // NOI18N
        jLabel44.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel44.setText("BANDARA SULTAN MAHMUD BADARUDIN 2");
        jPanel9.add(jLabel44);

        jLabel45.setFont(new java.awt.Font("Tahoma", 1, 36)); // NOI18N
        jLabel45.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel45.setText("PALEMBANG, SUMATERA SELATAN");
        jPanel9.add(jLabel45);

        javax.swing.GroupLayout panelMenuTittleLayout = new javax.swing.GroupLayout(panelMenuTittle);
        panelMenuTittle.setLayout(panelMenuTittleLayout);
        panelMenuTittleLayout.setHorizontalGroup(
            panelMenuTittleLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 1330, Short.MAX_VALUE)
            .addGroup(panelMenuTittleLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(panelMenuTittleLayout.createSequentialGroup()
                    .addGap(0, 0, Short.MAX_VALUE)
                    .addComponent(jPanel9, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGap(0, 0, Short.MAX_VALUE)))
        );
        panelMenuTittleLayout.setVerticalGroup(
            panelMenuTittleLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 211, Short.MAX_VALUE)
            .addGroup(panelMenuTittleLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(panelMenuTittleLayout.createSequentialGroup()
                    .addGap(0, 0, Short.MAX_VALUE)
                    .addComponent(jPanel9, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGap(0, 0, Short.MAX_VALUE)))
        );

        jPanel11.setLayout(new java.awt.GridLayout(1, 5, 50, 50));

        btnMenuPayment.setFont(new java.awt.Font("Tahoma", 1, 36)); // NOI18N
        btnMenuPayment.setText("PEMBAYARAN PARKIR");
        btnMenuPayment.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnMenuPaymentActionPerformed(evt);
            }
        });
        jPanel11.add(btnMenuPayment);

        btnMenuMember.setFont(new java.awt.Font("Tahoma", 1, 36)); // NOI18N
        btnMenuMember.setText("MEMBER");
        btnMenuMember.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnMenuMemberActionPerformed(evt);
            }
        });
        jPanel11.add(btnMenuMember);

        javax.swing.GroupLayout panelMenuButtonLayout = new javax.swing.GroupLayout(panelMenuButton);
        panelMenuButton.setLayout(panelMenuButtonLayout);
        panelMenuButtonLayout.setHorizontalGroup(
            panelMenuButtonLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelMenuButtonLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel11, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        panelMenuButtonLayout.setVerticalGroup(
            panelMenuButtonLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelMenuButtonLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel11, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );

        javax.swing.GroupLayout panelMenuMainLayout = new javax.swing.GroupLayout(panelMenuMain);
        panelMenuMain.setLayout(panelMenuMainLayout);
        panelMenuMainLayout.setHorizontalGroup(
            panelMenuMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelMenuMainLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panelMenuMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(panelMenuTittle, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(panelMenuButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        panelMenuMainLayout.setVerticalGroup(
            panelMenuMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelMenuMainLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(panelMenuTittle, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(panelMenuButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );

        javax.swing.GroupLayout panelMenuLayout = new javax.swing.GroupLayout(panelMenu);
        panelMenu.setLayout(panelMenuLayout);
        panelMenuLayout.setHorizontalGroup(
            panelMenuLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelMenuLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(panelMenuMain, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        panelMenuLayout.setVerticalGroup(
            panelMenuLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelMenuLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(panelMenuMain, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );

        mainPanel.add(panelMenu, "mainCardMenu");

        panelTiket.setBackground(new java.awt.Color(51, 51, 51));

        panelTiketStep.setLayout(new java.awt.CardLayout());

        img_stepScan.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icon/Scan Barcode.png"))); // NOI18N

        javax.swing.GroupLayout panelStep1Layout = new javax.swing.GroupLayout(panelStep1);
        panelStep1.setLayout(panelStep1Layout);
        panelStep1Layout.setHorizontalGroup(
            panelStep1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 278, Short.MAX_VALUE)
            .addGroup(panelStep1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(panelStep1Layout.createSequentialGroup()
                    .addContainerGap()
                    .addComponent(img_stepScan, javax.swing.GroupLayout.DEFAULT_SIZE, 258, Short.MAX_VALUE)
                    .addContainerGap()))
        );
        panelStep1Layout.setVerticalGroup(
            panelStep1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 737, Short.MAX_VALUE)
            .addGroup(panelStep1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panelStep1Layout.createSequentialGroup()
                    .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(img_stepScan, javax.swing.GroupLayout.PREFERRED_SIZE, 460, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addContainerGap(265, Short.MAX_VALUE)))
        );

        panelTiketStep.add(panelStep1, "cardScan");

        img_stepInfo.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icon/Info Parkir.png"))); // NOI18N

        javax.swing.GroupLayout panelStep2Layout = new javax.swing.GroupLayout(panelStep2);
        panelStep2.setLayout(panelStep2Layout);
        panelStep2Layout.setHorizontalGroup(
            panelStep2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 278, Short.MAX_VALUE)
            .addGroup(panelStep2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(panelStep2Layout.createSequentialGroup()
                    .addContainerGap()
                    .addComponent(img_stepInfo, javax.swing.GroupLayout.DEFAULT_SIZE, 258, Short.MAX_VALUE)
                    .addContainerGap()))
        );
        panelStep2Layout.setVerticalGroup(
            panelStep2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 737, Short.MAX_VALUE)
            .addGroup(panelStep2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(panelStep2Layout.createSequentialGroup()
                    .addContainerGap()
                    .addComponent(img_stepInfo, javax.swing.GroupLayout.PREFERRED_SIZE, 460, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addContainerGap(266, Short.MAX_VALUE)))
        );

        panelTiketStep.add(panelStep2, "cardInfo");

        img_stepPayment.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icon/Pembayaran.png"))); // NOI18N

        javax.swing.GroupLayout panelStep3Layout = new javax.swing.GroupLayout(panelStep3);
        panelStep3.setLayout(panelStep3Layout);
        panelStep3Layout.setHorizontalGroup(
            panelStep3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 278, Short.MAX_VALUE)
            .addGroup(panelStep3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(panelStep3Layout.createSequentialGroup()
                    .addContainerGap()
                    .addComponent(img_stepPayment, javax.swing.GroupLayout.DEFAULT_SIZE, 258, Short.MAX_VALUE)
                    .addContainerGap()))
        );
        panelStep3Layout.setVerticalGroup(
            panelStep3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 737, Short.MAX_VALUE)
            .addGroup(panelStep3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(panelStep3Layout.createSequentialGroup()
                    .addContainerGap()
                    .addComponent(img_stepPayment, javax.swing.GroupLayout.PREFERRED_SIZE, 460, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addContainerGap(266, Short.MAX_VALUE)))
        );

        panelTiketStep.add(panelStep3, "cardPayment");

        img_stepFinish.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icon/Selesai.png"))); // NOI18N

        javax.swing.GroupLayout panelStep4Layout = new javax.swing.GroupLayout(panelStep4);
        panelStep4.setLayout(panelStep4Layout);
        panelStep4Layout.setHorizontalGroup(
            panelStep4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 278, Short.MAX_VALUE)
            .addGroup(panelStep4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(panelStep4Layout.createSequentialGroup()
                    .addContainerGap()
                    .addComponent(img_stepFinish, javax.swing.GroupLayout.DEFAULT_SIZE, 258, Short.MAX_VALUE)
                    .addContainerGap()))
        );
        panelStep4Layout.setVerticalGroup(
            panelStep4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 737, Short.MAX_VALUE)
            .addGroup(panelStep4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(panelStep4Layout.createSequentialGroup()
                    .addContainerGap()
                    .addComponent(img_stepFinish, javax.swing.GroupLayout.PREFERRED_SIZE, 460, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addContainerGap(266, Short.MAX_VALUE)))
        );

        panelTiketStep.add(panelStep4, "cardFinish");

        panelTiketMain.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        panelTiketMain.setLayout(new java.awt.CardLayout());

        jLabel1.setFont(new java.awt.Font("Tahoma", 1, 36)); // NOI18N
        jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel1.setText("SCAN TIKET ANDA");

        jLabel27.setFont(new java.awt.Font("Tahoma", 0, 24)); // NOI18N
        jLabel27.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel27.setText("How to scan barcode (Image Unavailable)");

        btnTiketCancelScan.setText("Batal Scan");
        btnTiketCancelScan.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnTiketCancelScanActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout panelScanBarcodeLayout = new javax.swing.GroupLayout(panelScanBarcode);
        panelScanBarcode.setLayout(panelScanBarcodeLayout);
        panelScanBarcodeLayout.setHorizontalGroup(
            panelScanBarcodeLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelScanBarcodeLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panelScanBarcodeLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, 1030, Short.MAX_VALUE)
                    .addComponent(jLabel27, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panelScanBarcodeLayout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(btnTiketCancelScan, javax.swing.GroupLayout.PREFERRED_SIZE, 176, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        panelScanBarcodeLayout.setVerticalGroup(
            panelScanBarcodeLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelScanBarcodeLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1)
                .addGap(18, 18, 18)
                .addComponent(jLabel27, javax.swing.GroupLayout.DEFAULT_SIZE, 582, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnTiketCancelScan, javax.swing.GroupLayout.PREFERRED_SIZE, 61, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        panelTiketMain.add(panelScanBarcode, "card_scan");

        jLabel2.setFont(new java.awt.Font("Tahoma", 1, 36)); // NOI18N
        jLabel2.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel2.setText("INFO KENDARAAN ANDA");

        jPanel1.setLayout(new java.awt.GridLayout(4, 2, 20, 50));

        jLabel3.setFont(new java.awt.Font("Tahoma", 1, 30)); // NOI18N
        jLabel3.setText("KODE TIKET");
        jPanel1.add(jLabel3);

        tkt_barcode.setFont(new java.awt.Font("Tahoma", 0, 30)); // NOI18N
        tkt_barcode.setText("029387429834090215");
        jPanel1.add(tkt_barcode);

        jLabel4.setFont(new java.awt.Font("Tahoma", 1, 30)); // NOI18N
        jLabel4.setText("PINTU MASUK");
        jPanel1.add(jLabel4);

        tkt_gate.setFont(new java.awt.Font("Tahoma", 0, 30)); // NOI18N
        tkt_gate.setText("0");
        jPanel1.add(tkt_gate);

        jLabel5.setFont(new java.awt.Font("Tahoma", 1, 30)); // NOI18N
        jLabel5.setText("JAM MASUK");
        jPanel1.add(jLabel5);

        tkt_entrance.setFont(new java.awt.Font("Tahoma", 0, 30)); // NOI18N
        tkt_entrance.setText("06:30:32 21/10/2017");
        jPanel1.add(tkt_entrance);

        jLabel6.setFont(new java.awt.Font("Tahoma", 1, 30)); // NOI18N
        jLabel6.setText("NOMOR POLISI");
        jPanel1.add(jLabel6);

        tkt_noPol.setFont(new java.awt.Font("Tahoma", 0, 30)); // NOI18N
        tkt_noPol.setText("BG 2509 QO");
        tkt_noPol.setFocusable(false);
        jPanel1.add(tkt_noPol);

        javax.swing.GroupLayout panelInfoLayout = new javax.swing.GroupLayout(panelInfo);
        panelInfo.setLayout(panelInfoLayout);
        panelInfoLayout.setHorizontalGroup(
            panelInfoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 616, Short.MAX_VALUE)
            .addGroup(panelInfoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(panelInfoLayout.createSequentialGroup()
                    .addContainerGap()
                    .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
        );
        panelInfoLayout.setVerticalGroup(
            panelInfoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 348, Short.MAX_VALUE)
            .addGroup(panelInfoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(panelInfoLayout.createSequentialGroup()
                    .addContainerGap()
                    .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
        );

        panelImage1.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        labelCam1.setFont(new java.awt.Font("Tahoma", 1, 24)); // NOI18N
        labelCam1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        labelCam1.setText("Image Unavailable");

        javax.swing.GroupLayout panelImage1Layout = new javax.swing.GroupLayout(panelImage1);
        panelImage1.setLayout(panelImage1Layout);
        panelImage1Layout.setHorizontalGroup(
            panelImage1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelImage1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(labelCam1, javax.swing.GroupLayout.DEFAULT_SIZE, 296, Short.MAX_VALUE)
                .addContainerGap())
        );
        panelImage1Layout.setVerticalGroup(
            panelImage1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelImage1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(labelCam1, javax.swing.GroupLayout.DEFAULT_SIZE, 214, Short.MAX_VALUE)
                .addContainerGap())
        );

        panelImage2.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        labelCam2.setFont(new java.awt.Font("Tahoma", 1, 24)); // NOI18N
        labelCam2.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        labelCam2.setText("Image Unavailable");

        javax.swing.GroupLayout panelImage2Layout = new javax.swing.GroupLayout(panelImage2);
        panelImage2.setLayout(panelImage2Layout);
        panelImage2Layout.setHorizontalGroup(
            panelImage2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelImage2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(labelCam2, javax.swing.GroupLayout.DEFAULT_SIZE, 296, Short.MAX_VALUE)
                .addContainerGap())
        );
        panelImage2Layout.setVerticalGroup(
            panelImage2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelImage2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(labelCam2, javax.swing.GroupLayout.DEFAULT_SIZE, 214, Short.MAX_VALUE)
                .addContainerGap())
        );

        panelUpdateNoPol.setLayout(new java.awt.CardLayout());

        panelMenuParkingInfo.setLayout(new java.awt.GridLayout(1, 3, 30, 0));

        btnChangePolNum.setFont(new java.awt.Font("Tahoma", 1, 18)); // NOI18N
        btnChangePolNum.setText("UBAH NO. POLISI");
        btnChangePolNum.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnChangePolNumActionPerformed(evt);
            }
        });
        panelMenuParkingInfo.add(btnChangePolNum);

        btnPaymentProcess.setFont(new java.awt.Font("Tahoma", 1, 18)); // NOI18N
        btnPaymentProcess.setText("PROSES PEMBAYARAN");
        btnPaymentProcess.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnPaymentProcessActionPerformed(evt);
            }
        });
        panelMenuParkingInfo.add(btnPaymentProcess);

        btnCancelInfo.setFont(new java.awt.Font("Tahoma", 1, 18)); // NOI18N
        btnCancelInfo.setText("BATAL");
        btnCancelInfo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCancelInfoActionPerformed(evt);
            }
        });
        panelMenuParkingInfo.add(btnCancelInfo);

        panelUpdateNoPol.add(panelMenuParkingInfo, "card1");

        panelKonfirmUpdate.setLayout(new java.awt.GridLayout(1, 2, 100, 0));

        jButton3.setFont(new java.awt.Font("Tahoma", 1, 24)); // NOI18N
        jButton3.setText("Selesai");
        jButton3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton3ActionPerformed(evt);
            }
        });
        panelKonfirmUpdate.add(jButton3);

        jButton4.setFont(new java.awt.Font("Tahoma", 1, 24)); // NOI18N
        jButton4.setText("Batal");
        jButton4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton4ActionPerformed(evt);
            }
        });
        panelKonfirmUpdate.add(jButton4);

        panelUpdateNoPol.add(panelKonfirmUpdate, "card2");

        javax.swing.GroupLayout panelParkingInfoLayout = new javax.swing.GroupLayout(panelParkingInfo);
        panelParkingInfo.setLayout(panelParkingInfoLayout);
        panelParkingInfoLayout.setHorizontalGroup(
            panelParkingInfoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelParkingInfoLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panelParkingInfoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(panelUpdateNoPol, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panelParkingInfoLayout.createSequentialGroup()
                        .addComponent(panelInfo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 94, Short.MAX_VALUE)
                        .addComponent(panelImage1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panelParkingInfoLayout.createSequentialGroup()
                        .addGap(0, 710, Short.MAX_VALUE)
                        .addComponent(panelImage2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        panelParkingInfoLayout.setVerticalGroup(
            panelParkingInfoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelParkingInfoLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addGroup(panelParkingInfoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(panelParkingInfoLayout.createSequentialGroup()
                        .addComponent(panelImage1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(panelImage2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(panelInfo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addComponent(panelUpdateNoPol, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );

        panelTiketMain.add(panelParkingInfo, "card_parkingInfo");

        jPanel7.setLayout(new java.awt.GridLayout(6, 2));

        jLabel8.setFont(new java.awt.Font("Tahoma", 1, 30)); // NOI18N
        jLabel8.setText("PINTU MASUK");
        jPanel7.add(jLabel8);

        txtTrx_Gate.setFont(new java.awt.Font("Tahoma", 0, 30)); // NOI18N
        txtTrx_Gate.setText("1");
        jPanel7.add(txtTrx_Gate);

        jLabel9.setFont(new java.awt.Font("Tahoma", 1, 30)); // NOI18N
        jLabel9.setText("NOMOR POLISI");
        jPanel7.add(jLabel9);

        txtTrx_PolNum.setFont(new java.awt.Font("Tahoma", 0, 30)); // NOI18N
        txtTrx_PolNum.setText("BG 2509 QO");
        jPanel7.add(txtTrx_PolNum);

        jLabel10.setFont(new java.awt.Font("Tahoma", 1, 30)); // NOI18N
        jLabel10.setText("JENIS TARIF");
        jPanel7.add(jLabel10);

        txtTrx_type.setFont(new java.awt.Font("Tahoma", 0, 30)); // NOI18N
        txtTrx_type.setText("Mobil");
        jPanel7.add(txtTrx_type);

        jLabel11.setFont(new java.awt.Font("Tahoma", 1, 30)); // NOI18N
        jLabel11.setText("JAM MASUK");
        jPanel7.add(jLabel11);

        txtTrx_entrance.setFont(new java.awt.Font("Tahoma", 0, 30)); // NOI18N
        txtTrx_entrance.setText("06:30:15 21/10/2017");
        jPanel7.add(txtTrx_entrance);

        jLabel12.setFont(new java.awt.Font("Tahoma", 1, 30)); // NOI18N
        jLabel12.setText("JAM KELUAR");
        jPanel7.add(jLabel12);

        txtTrx_Exit.setFont(new java.awt.Font("Tahoma", 0, 30)); // NOI18N
        txtTrx_Exit.setText("06:30:15 21/10/2017");
        jPanel7.add(txtTrx_Exit);

        jLabel13.setFont(new java.awt.Font("Tahoma", 1, 30)); // NOI18N
        jLabel13.setText("TOTAL BIAYA");
        jPanel7.add(jLabel13);

        txtTrx_Price.setFont(new java.awt.Font("Tahoma", 0, 30)); // NOI18N
        txtTrx_Price.setText("Rp. 11.000.-");
        jPanel7.add(txtTrx_Price);

        javax.swing.GroupLayout panelInfoPaymentLayout = new javax.swing.GroupLayout(panelInfoPayment);
        panelInfoPayment.setLayout(panelInfoPaymentLayout);
        panelInfoPaymentLayout.setHorizontalGroup(
            panelInfoPaymentLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelInfoPaymentLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel7, javax.swing.GroupLayout.DEFAULT_SIZE, 831, Short.MAX_VALUE)
                .addContainerGap())
        );
        panelInfoPaymentLayout.setVerticalGroup(
            panelInfoPaymentLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelInfoPaymentLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel7, javax.swing.GroupLayout.DEFAULT_SIZE, 396, Short.MAX_VALUE)
                .addContainerGap())
        );

        jLabel7.setFont(new java.awt.Font("Tahoma", 1, 36)); // NOI18N
        jLabel7.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel7.setText("TRANSAKSI KENDARAAN");

        jPanel5.setLayout(new java.awt.GridLayout(1, 2, 300, 0));

        jButton1.setFont(new java.awt.Font("Tahoma", 1, 24)); // NOI18N
        jButton1.setText("(Dummy) Tap Card");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });
        jPanel5.add(jButton1);

        btnCancelPayment.setFont(new java.awt.Font("Tahoma", 1, 24)); // NOI18N
        btnCancelPayment.setText("BATAL");
        btnCancelPayment.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCancelPaymentActionPerformed(evt);
            }
        });
        jPanel5.add(btnCancelPayment);

        javax.swing.GroupLayout panelMenuPaymentLayout = new javax.swing.GroupLayout(panelMenuPayment);
        panelMenuPayment.setLayout(panelMenuPaymentLayout);
        panelMenuPaymentLayout.setHorizontalGroup(
            panelMenuPaymentLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
            .addGroup(panelMenuPaymentLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(panelMenuPaymentLayout.createSequentialGroup()
                    .addContainerGap()
                    .addComponent(jPanel5, javax.swing.GroupLayout.DEFAULT_SIZE, 1018, Short.MAX_VALUE)
                    .addContainerGap()))
        );
        panelMenuPaymentLayout.setVerticalGroup(
            panelMenuPaymentLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 84, Short.MAX_VALUE)
            .addGroup(panelMenuPaymentLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(jPanel5, javax.swing.GroupLayout.DEFAULT_SIZE, 84, Short.MAX_VALUE))
        );

        jLabel14.setFont(new java.awt.Font("Tahoma", 1, 36)); // NOI18N
        jLabel14.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel14.setText("SILAKAN TAP KARTU PEMBAYARAN ANDA !");

        javax.swing.GroupLayout panelPaymentLayout = new javax.swing.GroupLayout(panelPayment);
        panelPayment.setLayout(panelPaymentLayout);
        panelPaymentLayout.setHorizontalGroup(
            panelPaymentLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelPaymentLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panelPaymentLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel7, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(panelMenuPayment, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel14, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panelPaymentLayout.createSequentialGroup()
                        .addGap(0, 179, Short.MAX_VALUE)
                        .addComponent(panelInfoPayment, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        panelPaymentLayout.setVerticalGroup(
            panelPaymentLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelPaymentLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel7)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(panelInfoPayment, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jLabel14, javax.swing.GroupLayout.DEFAULT_SIZE, 123, Short.MAX_VALUE)
                .addGap(18, 18, 18)
                .addComponent(panelMenuPayment, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        panelTiketMain.add(panelPayment, "card_payment");

        jLabel15.setFont(new java.awt.Font("Tahoma", 1, 36)); // NOI18N
        jLabel15.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel15.setText("TERIMA KASIH");

        jLabel18.setFont(new java.awt.Font("Tahoma", 1, 24)); // NOI18N
        jLabel18.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel18.setText("WAKTU ANDA 15 MENIT UNTUK KELUAR DARI GERBANG !!!");

        jPanel12.setLayout(new java.awt.GridLayout(2, 2));

        jLabel16.setFont(new java.awt.Font("Tahoma", 1, 30)); // NOI18N
        jLabel16.setText("TOTAL BIAYA");
        jPanel12.add(jLabel16);

        txtFin_Price.setFont(new java.awt.Font("Tahoma", 0, 30)); // NOI18N
        txtFin_Price.setText("Rp. 11.000.-");
        jPanel12.add(txtFin_Price);

        jLabel17.setFont(new java.awt.Font("Tahoma", 1, 30)); // NOI18N
        jLabel17.setText("SISA SALDO");
        jPanel12.add(jLabel17);

        txtFin_balance.setFont(new java.awt.Font("Tahoma", 0, 30)); // NOI18N
        txtFin_balance.setText("Rp. 154.000.-");
        jPanel12.add(txtFin_balance);

        javax.swing.GroupLayout panelAccountBalanceInfoLayout = new javax.swing.GroupLayout(panelAccountBalanceInfo);
        panelAccountBalanceInfo.setLayout(panelAccountBalanceInfoLayout);
        panelAccountBalanceInfoLayout.setHorizontalGroup(
            panelAccountBalanceInfoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
            .addGroup(panelAccountBalanceInfoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panelAccountBalanceInfoLayout.createSequentialGroup()
                    .addContainerGap(217, Short.MAX_VALUE)
                    .addComponent(jPanel12, javax.swing.GroupLayout.PREFERRED_SIZE, 811, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addContainerGap()))
        );
        panelAccountBalanceInfoLayout.setVerticalGroup(
            panelAccountBalanceInfoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 243, Short.MAX_VALUE)
            .addGroup(panelAccountBalanceInfoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(panelAccountBalanceInfoLayout.createSequentialGroup()
                    .addContainerGap()
                    .addComponent(jPanel12, javax.swing.GroupLayout.DEFAULT_SIZE, 221, Short.MAX_VALUE)
                    .addContainerGap()))
        );

        btnTiketFinish.setText("OK");
        btnTiketFinish.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnTiketFinishActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout panelFinishLayout = new javax.swing.GroupLayout(panelFinish);
        panelFinish.setLayout(panelFinishLayout);
        panelFinishLayout.setHorizontalGroup(
            panelFinishLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelFinishLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panelFinishLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel15, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(panelAccountBalanceInfo, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel18, javax.swing.GroupLayout.DEFAULT_SIZE, 1030, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panelFinishLayout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(btnTiketFinish, javax.swing.GroupLayout.PREFERRED_SIZE, 181, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        panelFinishLayout.setVerticalGroup(
            panelFinishLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelFinishLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel15, javax.swing.GroupLayout.PREFERRED_SIZE, 88, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(panelAccountBalanceInfo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 142, Short.MAX_VALUE)
                .addComponent(jLabel18, javax.swing.GroupLayout.PREFERRED_SIZE, 146, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(btnTiketFinish, javax.swing.GroupLayout.PREFERRED_SIZE, 56, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        panelTiketMain.add(panelFinish, "card_finish");

        javax.swing.GroupLayout panelTiketLayout = new javax.swing.GroupLayout(panelTiket);
        panelTiket.setLayout(panelTiketLayout);
        panelTiketLayout.setHorizontalGroup(
            panelTiketLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panelTiketLayout.createSequentialGroup()
                .addContainerGap(298, Short.MAX_VALUE)
                .addComponent(panelTiketMain, javax.swing.GroupLayout.PREFERRED_SIZE, 1054, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18))
            .addGroup(panelTiketLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(panelTiketLayout.createSequentialGroup()
                    .addContainerGap()
                    .addComponent(panelTiketStep, javax.swing.GroupLayout.PREFERRED_SIZE, 278, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addContainerGap(1082, Short.MAX_VALUE)))
        );
        panelTiketLayout.setVerticalGroup(
            panelTiketLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelTiketLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(panelTiketMain, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
            .addGroup(panelTiketLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(panelTiketLayout.createSequentialGroup()
                    .addContainerGap()
                    .addComponent(panelTiketStep, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addContainerGap()))
        );

        mainPanel.add(panelTiket, "mainCardTiket");

        panelMember.setBackground(new java.awt.Color(51, 51, 51));

        panelMemberMain.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        panelMemberMain.setLayout(new java.awt.CardLayout());

        jPanel16.setBackground(new java.awt.Color(204, 204, 204));

        jPanel17.setLayout(new java.awt.GridLayout(3, 0));

        jLabel25.setFont(new java.awt.Font("Tahoma", 1, 24)); // NOI18N
        jLabel25.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel25.setText("Menu Member");
        jPanel17.add(jLabel25);

        jLabel26.setFont(new java.awt.Font("Tahoma", 1, 24)); // NOI18N
        jLabel26.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel26.setText("Bandara Sultan Mahmud Badarudin 2");
        jPanel17.add(jLabel26);

        jLabel28.setFont(new java.awt.Font("Tahoma", 1, 24)); // NOI18N
        jLabel28.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel28.setText("Palembang, Sumatera Barat");
        jPanel17.add(jLabel28);

        javax.swing.GroupLayout jPanel16Layout = new javax.swing.GroupLayout(jPanel16);
        jPanel16.setLayout(jPanel16Layout);
        jPanel16Layout.setHorizontalGroup(
            jPanel16Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel16Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel17, javax.swing.GroupLayout.DEFAULT_SIZE, 1010, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel16Layout.setVerticalGroup(
            jPanel16Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel16Layout.createSequentialGroup()
                .addGap(210, 210, 210)
                .addComponent(jPanel17, javax.swing.GroupLayout.PREFERRED_SIZE, 219, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(282, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout panelMemberFirstLayout = new javax.swing.GroupLayout(panelMemberFirst);
        panelMemberFirst.setLayout(panelMemberFirstLayout);
        panelMemberFirstLayout.setHorizontalGroup(
            panelMemberFirstLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelMemberFirstLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel16, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        panelMemberFirstLayout.setVerticalGroup(
            panelMemberFirstLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelMemberFirstLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel16, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );

        panelMemberMain.add(panelMemberFirst, "cardMemberFirst");

        jLabel20.setFont(new java.awt.Font("Tahoma", 1, 36)); // NOI18N
        jLabel20.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel20.setText("Informasi Member");

        javax.swing.GroupLayout jPanel8Layout = new javax.swing.GroupLayout(jPanel8);
        jPanel8.setLayout(jPanel8Layout);
        jPanel8Layout.setHorizontalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel8Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel20, javax.swing.GroupLayout.DEFAULT_SIZE, 1010, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel8Layout.setVerticalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel8Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel20, javax.swing.GroupLayout.DEFAULT_SIZE, 78, Short.MAX_VALUE)
                .addContainerGap())
        );

        jPanel10.setBackground(new java.awt.Color(204, 204, 204));

        jPanel13.setLayout(new java.awt.GridLayout(6, 2));

        jLabel29.setFont(new java.awt.Font("Tahoma", 1, 24)); // NOI18N
        jLabel29.setText("Nama");
        jPanel13.add(jLabel29);

        lblMemberName.setFont(new java.awt.Font("Tahoma", 1, 24)); // NOI18N
        lblMemberName.setText("-");
        jPanel13.add(lblMemberName);

        jLabel31.setFont(new java.awt.Font("Tahoma", 1, 24)); // NOI18N
        jLabel31.setText("Alamat");
        jPanel13.add(jLabel31);

        lblMemberInstansi.setFont(new java.awt.Font("Tahoma", 1, 24)); // NOI18N
        lblMemberInstansi.setText("-");
        jPanel13.add(lblMemberInstansi);

        jLabel33.setFont(new java.awt.Font("Tahoma", 1, 24)); // NOI18N
        jLabel33.setText("Nomor Polisi");
        jPanel13.add(jLabel33);

        lblMemberLicense.setFont(new java.awt.Font("Tahoma", 1, 24)); // NOI18N
        lblMemberLicense.setText("-");
        jPanel13.add(lblMemberLicense);

        jLabel35.setFont(new java.awt.Font("Tahoma", 1, 24)); // NOI18N
        jLabel35.setText("Berlaku Sejak");
        jPanel13.add(jLabel35);

        lblMemberValidFrom.setFont(new java.awt.Font("Tahoma", 1, 24)); // NOI18N
        lblMemberValidFrom.setText("-");
        jPanel13.add(lblMemberValidFrom);

        jLabel37.setFont(new java.awt.Font("Tahoma", 1, 24)); // NOI18N
        jLabel37.setText("Berlaku Hingga");
        jPanel13.add(jLabel37);

        lblMemberValidUntil.setFont(new java.awt.Font("Tahoma", 1, 24)); // NOI18N
        lblMemberValidUntil.setText("-");
        jPanel13.add(lblMemberValidUntil);

        jLabel39.setFont(new java.awt.Font("Tahoma", 1, 24)); // NOI18N
        jLabel39.setText("Status");
        jPanel13.add(jLabel39);

        lblMemberStatus.setFont(new java.awt.Font("Tahoma", 1, 24)); // NOI18N
        lblMemberStatus.setText("-");
        jPanel13.add(lblMemberStatus);

        jButton2.setText("(Dummy) Tap Card");
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel10Layout = new javax.swing.GroupLayout(jPanel10);
        jPanel10.setLayout(jPanel10Layout);
        jPanel10Layout.setHorizontalGroup(
            jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel10Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel13, javax.swing.GroupLayout.DEFAULT_SIZE, 1010, Short.MAX_VALUE)
                    .addGroup(jPanel10Layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(jButton2, javax.swing.GroupLayout.PREFERRED_SIZE, 156, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        jPanel10Layout.setVerticalGroup(
            jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel10Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel13, javax.swing.GroupLayout.DEFAULT_SIZE, 468, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jButton2)
                .addContainerGap())
        );

        lblMemberCheckInfo.setFont(new java.awt.Font("Tahoma", 1, 24)); // NOI18N
        lblMemberCheckInfo.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblMemberCheckInfo.setText("Silakan Tap Kartu Member Anda Pada Perangkat yang Telah Disediakan !");

        javax.swing.GroupLayout jPanel14Layout = new javax.swing.GroupLayout(jPanel14);
        jPanel14.setLayout(jPanel14Layout);
        jPanel14Layout.setHorizontalGroup(
            jPanel14Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel14Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(lblMemberCheckInfo, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel14Layout.setVerticalGroup(
            jPanel14Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel14Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(lblMemberCheckInfo, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );

        javax.swing.GroupLayout panelMemberCheckLayout = new javax.swing.GroupLayout(panelMemberCheck);
        panelMemberCheck.setLayout(panelMemberCheckLayout);
        panelMemberCheckLayout.setHorizontalGroup(
            panelMemberCheckLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelMemberCheckLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panelMemberCheckLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel8, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel10, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel14, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        panelMemberCheckLayout.setVerticalGroup(
            panelMemberCheckLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelMemberCheckLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel8, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jPanel10, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGap(18, 18, 18)
                .addComponent(jPanel14, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        panelMemberMain.add(panelMemberCheck, "cardMemberCheck");

        jLabel19.setFont(new java.awt.Font("Tahoma", 1, 36)); // NOI18N
        jLabel19.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel19.setText("Perpanjangan Member");

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel19, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel19, javax.swing.GroupLayout.DEFAULT_SIZE, 78, Short.MAX_VALUE)
                .addContainerGap())
        );

        panelMemberRenewalMain.setLayout(new java.awt.CardLayout());

        jLabel23.setFont(new java.awt.Font("Tahoma", 1, 18)); // NOI18N
        jLabel23.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel23.setText("Proses perpanjangan kartu dapat dilakukan pada setiap 7 hari sebelum bulan berakhir.");

        jLabel21.setFont(new java.awt.Font("Tahoma", 1, 18)); // NOI18N
        jLabel21.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel21.setText("Silakan datang kembali untuk melakukan prepanjangan pada waktu yang telah ditentukan.");

        jLabel22.setFont(new java.awt.Font("Tahoma", 1, 18)); // NOI18N
        jLabel22.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel22.setText("Proses perpanjangan kartu member hanya dapat dilakukan pada waktu tertentu.");

        javax.swing.GroupLayout panelMemberRenewalInactiveLayout = new javax.swing.GroupLayout(panelMemberRenewalInactive);
        panelMemberRenewalInactive.setLayout(panelMemberRenewalInactiveLayout);
        panelMemberRenewalInactiveLayout.setHorizontalGroup(
            panelMemberRenewalInactiveLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panelMemberRenewalInactiveLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panelMemberRenewalInactiveLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel22, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel21, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel23, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        panelMemberRenewalInactiveLayout.setVerticalGroup(
            panelMemberRenewalInactiveLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelMemberRenewalInactiveLayout.createSequentialGroup()
                .addGap(234, 234, 234)
                .addComponent(jLabel22)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel21)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel23)
                .addContainerGap(281, Short.MAX_VALUE))
        );

        panelMemberRenewalMain.add(panelMemberRenewalInactive, "cardMemberRenewalInactive");

        jButton5.setFont(new java.awt.Font("Tahoma", 1, 24)); // NOI18N
        jButton5.setText("(Dummy) Renewal");
        jButton5.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton5ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout panelMemberRenewalActiveLayout = new javax.swing.GroupLayout(panelMemberRenewalActive);
        panelMemberRenewalActive.setLayout(panelMemberRenewalActiveLayout);
        panelMemberRenewalActiveLayout.setHorizontalGroup(
            panelMemberRenewalActiveLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panelMemberRenewalActiveLayout.createSequentialGroup()
                .addContainerGap(739, Short.MAX_VALUE)
                .addComponent(jButton5, javax.swing.GroupLayout.PREFERRED_SIZE, 281, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        panelMemberRenewalActiveLayout.setVerticalGroup(
            panelMemberRenewalActiveLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panelMemberRenewalActiveLayout.createSequentialGroup()
                .addContainerGap(466, Short.MAX_VALUE)
                .addComponent(jButton5, javax.swing.GroupLayout.PREFERRED_SIZE, 116, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        panelMemberRenewalMain.add(panelMemberRenewalActive, "cardMemberRenewalActive");

        javax.swing.GroupLayout panelMemberRenewalLayout = new javax.swing.GroupLayout(panelMemberRenewal);
        panelMemberRenewal.setLayout(panelMemberRenewalLayout);
        panelMemberRenewalLayout.setHorizontalGroup(
            panelMemberRenewalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelMemberRenewalLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panelMemberRenewalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(panelMemberRenewalMain, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        panelMemberRenewalLayout.setVerticalGroup(
            panelMemberRenewalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelMemberRenewalLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(panelMemberRenewalMain, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );

        panelMemberMain.add(panelMemberRenewal, "cardMemberRenewal");

        gridMemberbutton.setLayout(new java.awt.GridLayout(0, 1, 0, 10));

        btnMemberCekInfo.setFont(new java.awt.Font("Tahoma", 1, 24)); // NOI18N
        btnMemberCekInfo.setText("Cek Info Kartu");
        btnMemberCekInfo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnMemberCekInfoActionPerformed(evt);
            }
        });
        gridMemberbutton.add(btnMemberCekInfo);

        btnMemberRenewal.setFont(new java.awt.Font("Tahoma", 1, 24)); // NOI18N
        btnMemberRenewal.setText("Perpanjangan");
        btnMemberRenewal.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnMemberRenewalActionPerformed(evt);
            }
        });
        gridMemberbutton.add(btnMemberRenewal);

        btnMemberExit.setFont(new java.awt.Font("Tahoma", 1, 24)); // NOI18N
        btnMemberExit.setText("Keluar");
        btnMemberExit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnMemberExitActionPerformed(evt);
            }
        });
        gridMemberbutton.add(btnMemberExit);

        javax.swing.GroupLayout panelMemberButtonLayout = new javax.swing.GroupLayout(panelMemberButton);
        panelMemberButton.setLayout(panelMemberButtonLayout);
        panelMemberButtonLayout.setHorizontalGroup(
            panelMemberButtonLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelMemberButtonLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(gridMemberbutton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        panelMemberButtonLayout.setVerticalGroup(
            panelMemberButtonLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelMemberButtonLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(gridMemberbutton, javax.swing.GroupLayout.PREFERRED_SIZE, 379, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout panelMemberLayout = new javax.swing.GroupLayout(panelMember);
        panelMember.setLayout(panelMemberLayout);
        panelMemberLayout.setHorizontalGroup(
            panelMemberLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panelMemberLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(panelMemberButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(panelMemberMain, javax.swing.GroupLayout.PREFERRED_SIZE, 1054, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18))
        );
        panelMemberLayout.setVerticalGroup(
            panelMemberLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelMemberLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panelMemberLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(panelMemberMain, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(panelMemberButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );

        mainPanel.add(panelMember, "mainCardMember");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(mainPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(mainPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnCancelInfoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCancelInfoActionPerformed
        // TODO add your handling code here:
        clTiketMain.show(panelTiketMain, "card_scan");
        clTiketStep.show(panelTiketStep, "cardScan");
        barcodeListener();
    }//GEN-LAST:event_btnCancelInfoActionPerformed

    private void btnPaymentProcessActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnPaymentProcessActionPerformed
        // TODO add your handling code here:
        //Proses data tiket menjadi informasi pembayaran
        getDataPayment(tkt.getBarcode());
        clTiketMain.show(panelTiketMain, "card_payment");
        clTiketStep.show(panelTiketStep, "cardPayment");
    }//GEN-LAST:event_btnPaymentProcessActionPerformed

    private void btnCancelPaymentActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCancelPaymentActionPerformed
        // TODO add your handling code here:
        clTiketMain.show(panelTiketMain, "card_parkingInfo");
        clTiketStep.show(panelTiketStep, "cardInfo");
    }//GEN-LAST:event_btnCancelPaymentActionPerformed

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        // TODO add your handling code here:
        //Dummy Tap Card
        clTiketMain.show(panelTiketMain, "card_finish");
        clTiketStep.show(panelTiketStep, "cardFinish");
        try {
            //Capture Webcam (2 Option with different Library)
            //Pilihan 1
            //boolean c = new Camera().capture(tkt.getBarcode());
            //Pilihan 2
            new WebcamCapture().capture(tkt.getBarcode());
        } catch (IOException ex) {
            Logger.getLogger(CashlessUI.class.getName()).log(Level.SEVERE, null, ex);
        }
        txtFin_Price.setText("Rp. " + "45.000" + ".-");
    }//GEN-LAST:event_jButton1ActionPerformed

    private void btnChangePolNumActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnChangePolNumActionPerformed
        // TODO add your handling code here:
        tkt_noPol.setFocusable(true);
        clUpdateLicenseBtn.show(panelUpdateNoPol, "card2");
    }//GEN-LAST:event_btnChangePolNumActionPerformed

    private void btnTiketFinishActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnTiketFinishActionPerformed
        clMainPanel.show(mainPanel, "mainCardMenu");
    }//GEN-LAST:event_btnTiketFinishActionPerformed

    private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton3ActionPerformed
        // TODO add your handling code here:
        //Update Nomor Polisi Kendaraan
        int updatePolNum = new queryTicket().updatePolNum(tkt.getBarcode(), tkt_noPol.getText());
        if (updatePolNum != 0) {
            tkt.setLicenseNumber(tkt_noPol.getText());
            JOptionPane.showMessageDialog(null, "Nomor Polisi Berhasil Diperbarui");
        } else {
            JOptionPane.showMessageDialog(null, "Nomor Polisi Gagal Diperbarui, Silakan Hubungi Pengelola Layanan");
        }
        clUpdateLicenseBtn.show(panelUpdateNoPol, "card1");
        tkt_noPol.setFocusable(false);
    }//GEN-LAST:event_jButton3ActionPerformed

    private void jButton4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton4ActionPerformed
        // TODO add your handling code here:
        tkt_noPol.setFocusable(false);
        tkt_noPol.setText(tkt.getLicenseNumber());
        JOptionPane.showMessageDialog(null, "Update Nomor Polisi Dibatalkan");
        clUpdateLicenseBtn.show(panelUpdateNoPol, "card1");
    }//GEN-LAST:event_jButton4ActionPerformed

    private void btnMenuPaymentActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnMenuPaymentActionPerformed
        clMainPanel.show(mainPanel, "mainCardTiket");
        barcodeListener();
    }//GEN-LAST:event_btnMenuPaymentActionPerformed

    private void btnMenuMemberActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnMenuMemberActionPerformed
        clMainPanel.show(mainPanel, "mainCardMember");
    }//GEN-LAST:event_btnMenuMemberActionPerformed

    private void btnTiketCancelScanActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnTiketCancelScanActionPerformed
        rbt.keyRelease(KeyEvent.VK_ESCAPE);
    }//GEN-LAST:event_btnTiketCancelScanActionPerformed

    private void btnMemberCekInfoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnMemberCekInfoActionPerformed
        clMemberMain.show(panelMemberMain, "cardMemberCheck");
    }//GEN-LAST:event_btnMemberCekInfoActionPerformed

    private void btnMemberRenewalActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnMemberRenewalActionPerformed
        clMemberMain.show(panelMemberMain, "cardMemberRenewal");
        checkRenewalDate();
    }//GEN-LAST:event_btnMemberRenewalActionPerformed

    private void btnMemberExitActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnMemberExitActionPerformed
        clMainPanel.show(mainPanel, "mainCardMenu");
    }//GEN-LAST:event_btnMemberExitActionPerformed

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        int kodemember = Integer.parseInt(JOptionPane.showInputDialog("Masukkan kode member"));
        getDataMember(kodemember);
        lblMemberName.setText(mbr.getName());
        lblMemberInstansi.setText(mbr.getAddress());
        lblMemberLicense.setText(mbr.getLicenseNumber());
        lblMemberValidFrom.setText(mbr.getValidFrom());
        lblMemberValidUntil.setText(mbr.getValidUntil());
        lblMemberStatus.setText(mbr.getStatus());
    }//GEN-LAST:event_jButton2ActionPerformed

    private void jButton5ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton5ActionPerformed

    }//GEN-LAST:event_jButton5ActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Windows".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(CashlessUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(CashlessUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(CashlessUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(CashlessUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new CashlessUI().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnCancelInfo;
    private javax.swing.JButton btnCancelPayment;
    private javax.swing.JButton btnChangePolNum;
    private javax.swing.JButton btnMemberCekInfo;
    private javax.swing.JButton btnMemberExit;
    private javax.swing.JButton btnMemberRenewal;
    private javax.swing.JButton btnMenuMember;
    private javax.swing.JButton btnMenuPayment;
    private javax.swing.JButton btnPaymentProcess;
    private javax.swing.JButton btnTiketCancelScan;
    private javax.swing.JButton btnTiketFinish;
    private javax.swing.JPanel gridMemberbutton;
    private javax.swing.JLabel img_stepFinish;
    private javax.swing.JLabel img_stepInfo;
    private javax.swing.JLabel img_stepPayment;
    private javax.swing.JLabel img_stepScan;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JButton jButton4;
    private javax.swing.JButton jButton5;
    private javax.swing.JComboBox jComboBox1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel17;
    private javax.swing.JLabel jLabel18;
    private javax.swing.JLabel jLabel19;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel20;
    private javax.swing.JLabel jLabel21;
    private javax.swing.JLabel jLabel22;
    private javax.swing.JLabel jLabel23;
    private javax.swing.JLabel jLabel25;
    private javax.swing.JLabel jLabel26;
    private javax.swing.JLabel jLabel27;
    private javax.swing.JLabel jLabel28;
    private javax.swing.JLabel jLabel29;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel31;
    private javax.swing.JLabel jLabel33;
    private javax.swing.JLabel jLabel35;
    private javax.swing.JLabel jLabel37;
    private javax.swing.JLabel jLabel39;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel43;
    private javax.swing.JLabel jLabel44;
    private javax.swing.JLabel jLabel45;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel10;
    private javax.swing.JPanel jPanel11;
    private javax.swing.JPanel jPanel12;
    private javax.swing.JPanel jPanel13;
    private javax.swing.JPanel jPanel14;
    private javax.swing.JPanel jPanel16;
    private javax.swing.JPanel jPanel17;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JPanel jPanel8;
    private javax.swing.JPanel jPanel9;
    private javax.swing.JLabel labelCam1;
    private javax.swing.JLabel labelCam2;
    private javax.swing.JLabel lblMemberCheckInfo;
    private javax.swing.JLabel lblMemberInstansi;
    private javax.swing.JLabel lblMemberLicense;
    private javax.swing.JLabel lblMemberName;
    private javax.swing.JLabel lblMemberStatus;
    private javax.swing.JLabel lblMemberValidFrom;
    private javax.swing.JLabel lblMemberValidUntil;
    private javax.swing.JPanel mainPanel;
    private javax.swing.JPanel panelAccountBalanceInfo;
    private javax.swing.JPanel panelFinish;
    private javax.swing.JPanel panelImage1;
    private javax.swing.JPanel panelImage2;
    private javax.swing.JPanel panelInfo;
    private javax.swing.JPanel panelInfoPayment;
    private javax.swing.JPanel panelKonfirmUpdate;
    private javax.swing.JPanel panelMember;
    private javax.swing.JPanel panelMemberButton;
    private javax.swing.JPanel panelMemberCheck;
    private javax.swing.JPanel panelMemberFirst;
    private javax.swing.JPanel panelMemberMain;
    private javax.swing.JPanel panelMemberRenewal;
    private javax.swing.JPanel panelMemberRenewalActive;
    private javax.swing.JPanel panelMemberRenewalInactive;
    private javax.swing.JPanel panelMemberRenewalMain;
    private javax.swing.JPanel panelMenu;
    private javax.swing.JPanel panelMenuButton;
    private javax.swing.JPanel panelMenuMain;
    private javax.swing.JPanel panelMenuParkingInfo;
    private javax.swing.JPanel panelMenuPayment;
    private javax.swing.JPanel panelMenuTittle;
    private javax.swing.JPanel panelParkingInfo;
    private javax.swing.JPanel panelPayment;
    private javax.swing.JPanel panelScanBarcode;
    private javax.swing.JPanel panelStep1;
    private javax.swing.JPanel panelStep2;
    private javax.swing.JPanel panelStep3;
    private javax.swing.JPanel panelStep4;
    private javax.swing.JPanel panelTiket;
    private javax.swing.JPanel panelTiketMain;
    private javax.swing.JPanel panelTiketStep;
    private javax.swing.JPanel panelUpdateNoPol;
    private javax.swing.JLabel tkt_barcode;
    private javax.swing.JLabel tkt_entrance;
    private javax.swing.JLabel tkt_gate;
    private javax.swing.JTextField tkt_noPol;
    private javax.swing.JLabel txtFin_Price;
    private javax.swing.JLabel txtFin_balance;
    private javax.swing.JLabel txtTrx_Exit;
    private javax.swing.JLabel txtTrx_Gate;
    private javax.swing.JLabel txtTrx_PolNum;
    private javax.swing.JLabel txtTrx_Price;
    private javax.swing.JLabel txtTrx_entrance;
    private javax.swing.JLabel txtTrx_type;
    // End of variables declaration//GEN-END:variables
}

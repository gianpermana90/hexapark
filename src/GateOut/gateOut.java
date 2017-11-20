/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package GateOut;

import cls.Ticket;
import db.queryPayment;
import db.queryTicket;
import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;
import java.awt.event.KeyEvent;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import javax.swing.JFrame;

/**
 *
 * @author Hades
 */
public class gateOut extends javax.swing.JFrame {

    /**
     * Creates new form gateOut
     */
    private Ticket tkt = new Ticket();

    private static final long THRESHOLD = 100;
    private static final int MIN_BARCODE_LENGTH = 8;
    private final StringBuffer barcode = new StringBuffer();
    private final List<BarcodeListener> listeners = new CopyOnWriteArrayList<BarcodeListener>();
    private long lastEventTimeStamp = 0L;

    public gateOut() {
        initComponents();
        setLocationRelativeTo(this);

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

                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    if (barcode.length() >= MIN_BARCODE_LENGTH) {
                        fireBarcode(barcode.toString());
                        System.out.println(barcode.toString());
                        txtbarcode.setText(barcode.toString());
                        getDataTicket(barcode.toString());
                        txtOutput.setText("Terima Kasih... \nHati-hati di jalan...");
                    }
                    barcode.delete(0, barcode.length());
                } else if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    txtbarcode.setText("-");
                    txtGate.setText("-");
                    txtNoPol.setText("-");
                    txtJenisTrf.setText("-");
                    txtJamMasuk.setText("-");
                    txtJamKeluar.setText("-");
                    txtBiaya.setText("-");
                    txtOutput.setText("Silakan scan barcode terlebih dahulu ...");
                } else {
                    barcode.append(e.getKeyChar());
                }
                return false;
            }
        });
    }

    SimpleDateFormat exitTime = new SimpleDateFormat("HH:mm:ss dd/MM/yyyy");

    private void getDataTicket(String code) {
        tkt = new queryTicket().getData(code);
        txtGate.setText(Integer.toString(tkt.getEntranceGate()));
        txtNoPol.setText(tkt.getLicenseNumber());
        txtJenisTrf.setText(tkt.getVehicleTypes());
        txtJamMasuk.setText(tkt.getEntranceTime());
        //Set Date Exit
        Calendar dateNow = Calendar.getInstance();
        Date skr = new Date();
        dateNow.setTime(skr);
        txtJamKeluar.setText(exitTime.format(skr));
        txtBiaya.setText("RP." + tkt.getPrice() + ".-");
    }

    /**
     * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The content of this method is always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel2 = new javax.swing.JPanel();
        panelBase = new javax.swing.JPanel();
        panelMain = new javax.swing.JPanel();
        panelScan = new javax.swing.JPanel();
        panelFront = new javax.swing.JPanel();
        txtbarcode = new javax.swing.JLabel();
        panelInfo = new javax.swing.JPanel();
        panelInfoGrid = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        txtGate = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        txtNoPol = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        txtJenisTrf = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        txtJamMasuk = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();
        txtJamKeluar = new javax.swing.JLabel();
        jLabel12 = new javax.swing.JLabel();
        txtBiaya = new javax.swing.JLabel();
        panelOutput = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        txtOutput = new javax.swing.JTextArea();

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 100, Short.MAX_VALUE)
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 100, Short.MAX_VALUE)
        );

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setMaximumSize(new java.awt.Dimension(850, 480));
        setUndecorated(true);
        setResizable(false);
        getContentPane().setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        panelBase.setBackground(new java.awt.Color(102, 102, 102));

        panelScan.setBackground(new java.awt.Color(102, 102, 102));
        panelScan.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));
        panelScan.setLayout(new java.awt.CardLayout());

        panelFront.setBackground(new java.awt.Color(102, 102, 102));

        txtbarcode.setFont(new java.awt.Font("Tahoma", 1, 18)); // NOI18N
        txtbarcode.setForeground(new java.awt.Color(255, 255, 255));
        txtbarcode.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        txtbarcode.setText("-");

        javax.swing.GroupLayout panelFrontLayout = new javax.swing.GroupLayout(panelFront);
        panelFront.setLayout(panelFrontLayout);
        panelFrontLayout.setHorizontalGroup(
            panelFrontLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelFrontLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(txtbarcode, javax.swing.GroupLayout.DEFAULT_SIZE, 784, Short.MAX_VALUE)
                .addContainerGap())
        );
        panelFrontLayout.setVerticalGroup(
            panelFrontLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelFrontLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(txtbarcode, javax.swing.GroupLayout.DEFAULT_SIZE, 48, Short.MAX_VALUE)
                .addContainerGap())
        );

        panelScan.add(panelFront, "card3");

        panelInfo.setBackground(new java.awt.Color(102, 102, 102));
        panelInfo.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));

        panelInfoGrid.setBackground(new java.awt.Color(102, 102, 102));
        panelInfoGrid.setLayout(new java.awt.GridLayout(6, 2));

        jLabel2.setFont(new java.awt.Font("Tahoma", 1, 18)); // NOI18N
        jLabel2.setForeground(new java.awt.Color(255, 255, 255));
        jLabel2.setText("Pintu Masuk");
        panelInfoGrid.add(jLabel2);

        txtGate.setBackground(new java.awt.Color(102, 102, 102));
        txtGate.setFont(new java.awt.Font("Tahoma", 1, 18)); // NOI18N
        txtGate.setForeground(new java.awt.Color(255, 255, 255));
        txtGate.setText("-");
        panelInfoGrid.add(txtGate);

        jLabel4.setFont(new java.awt.Font("Tahoma", 1, 18)); // NOI18N
        jLabel4.setForeground(new java.awt.Color(255, 255, 255));
        jLabel4.setText("Nomor Polisi");
        panelInfoGrid.add(jLabel4);

        txtNoPol.setFont(new java.awt.Font("Tahoma", 1, 18)); // NOI18N
        txtNoPol.setForeground(new java.awt.Color(255, 255, 255));
        txtNoPol.setText("-");
        panelInfoGrid.add(txtNoPol);

        jLabel6.setFont(new java.awt.Font("Tahoma", 1, 18)); // NOI18N
        jLabel6.setForeground(new java.awt.Color(255, 255, 255));
        jLabel6.setText("Jenis Tarif");
        panelInfoGrid.add(jLabel6);

        txtJenisTrf.setFont(new java.awt.Font("Tahoma", 1, 18)); // NOI18N
        txtJenisTrf.setForeground(new java.awt.Color(255, 255, 255));
        txtJenisTrf.setText("-");
        panelInfoGrid.add(txtJenisTrf);

        jLabel8.setFont(new java.awt.Font("Tahoma", 1, 18)); // NOI18N
        jLabel8.setForeground(new java.awt.Color(255, 255, 255));
        jLabel8.setText("Jam Masuk");
        panelInfoGrid.add(jLabel8);

        txtJamMasuk.setFont(new java.awt.Font("Tahoma", 1, 18)); // NOI18N
        txtJamMasuk.setForeground(new java.awt.Color(255, 255, 255));
        txtJamMasuk.setText("-");
        panelInfoGrid.add(txtJamMasuk);

        jLabel10.setFont(new java.awt.Font("Tahoma", 1, 18)); // NOI18N
        jLabel10.setForeground(new java.awt.Color(255, 255, 255));
        jLabel10.setText("Jam Keluar");
        panelInfoGrid.add(jLabel10);

        txtJamKeluar.setFont(new java.awt.Font("Tahoma", 1, 18)); // NOI18N
        txtJamKeluar.setForeground(new java.awt.Color(255, 255, 255));
        txtJamKeluar.setText("-");
        panelInfoGrid.add(txtJamKeluar);

        jLabel12.setFont(new java.awt.Font("Tahoma", 1, 18)); // NOI18N
        jLabel12.setForeground(new java.awt.Color(255, 255, 255));
        jLabel12.setText("Total Biaya");
        panelInfoGrid.add(jLabel12);

        txtBiaya.setFont(new java.awt.Font("Tahoma", 1, 18)); // NOI18N
        txtBiaya.setForeground(new java.awt.Color(255, 255, 255));
        txtBiaya.setText("-");
        panelInfoGrid.add(txtBiaya);

        javax.swing.GroupLayout panelInfoLayout = new javax.swing.GroupLayout(panelInfo);
        panelInfo.setLayout(panelInfoLayout);
        panelInfoLayout.setHorizontalGroup(
            panelInfoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelInfoLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(panelInfoGrid, javax.swing.GroupLayout.DEFAULT_SIZE, 466, Short.MAX_VALUE)
                .addContainerGap())
        );
        panelInfoLayout.setVerticalGroup(
            panelInfoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelInfoLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(panelInfoGrid, javax.swing.GroupLayout.DEFAULT_SIZE, 323, Short.MAX_VALUE)
                .addContainerGap())
        );

        panelOutput.setBackground(new java.awt.Color(0, 0, 0));

        txtOutput.setEditable(false);
        txtOutput.setBackground(new java.awt.Color(0, 0, 0));
        txtOutput.setColumns(20);
        txtOutput.setFont(new java.awt.Font("Monospaced", 1, 24)); // NOI18N
        txtOutput.setForeground(new java.awt.Color(255, 255, 255));
        txtOutput.setLineWrap(true);
        txtOutput.setRows(5);
        txtOutput.setText("Silakan scan barcode terlebih dahulu ...");
        txtOutput.setWrapStyleWord(true);
        jScrollPane2.setViewportView(txtOutput);

        javax.swing.GroupLayout panelOutputLayout = new javax.swing.GroupLayout(panelOutput);
        panelOutput.setLayout(panelOutputLayout);
        panelOutputLayout.setHorizontalGroup(
            panelOutputLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelOutputLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane2)
                .addContainerGap())
        );
        panelOutputLayout.setVerticalGroup(
            panelOutputLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelOutputLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane2)
                .addContainerGap())
        );

        javax.swing.GroupLayout panelMainLayout = new javax.swing.GroupLayout(panelMain);
        panelMain.setLayout(panelMainLayout);
        panelMainLayout.setHorizontalGroup(
            panelMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelMainLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panelMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(panelScan, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(panelMainLayout.createSequentialGroup()
                        .addComponent(panelInfo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(panelOutput, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addContainerGap())
        );
        panelMainLayout.setVerticalGroup(
            panelMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelMainLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(panelScan, javax.swing.GroupLayout.PREFERRED_SIZE, 76, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(panelMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(panelInfo, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(panelOutput, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );

        javax.swing.GroupLayout panelBaseLayout = new javax.swing.GroupLayout(panelBase);
        panelBase.setLayout(panelBaseLayout);
        panelBaseLayout.setHorizontalGroup(
            panelBaseLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelBaseLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(panelMain, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        panelBaseLayout.setVerticalGroup(
            panelBaseLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelBaseLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(panelMain, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );

        getContentPane().add(panelBase, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 850, 480));

        pack();
    }// </editor-fold>//GEN-END:initComponents

    public interface BarcodeListener {

        void onBarcodeRead(String barcode);
    }

    protected void fireBarcode(String barcode) {
        for (BarcodeListener listener : listeners) {
            listener.onBarcodeRead(barcode);
        }
    }

    public void addBarcodeListener(BarcodeListener listener) {
        listeners.add(listener);
    }

    public void removeBarcodeListener(BarcodeListener listener) {
        listeners.remove(listener);
    }

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
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(gateOut.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(gateOut.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(gateOut.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(gateOut.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new gateOut().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JPanel panelBase;
    private javax.swing.JPanel panelFront;
    private javax.swing.JPanel panelInfo;
    private javax.swing.JPanel panelInfoGrid;
    private javax.swing.JPanel panelMain;
    private javax.swing.JPanel panelOutput;
    private javax.swing.JPanel panelScan;
    private javax.swing.JLabel txtBiaya;
    private javax.swing.JLabel txtGate;
    private javax.swing.JLabel txtJamKeluar;
    private javax.swing.JLabel txtJamMasuk;
    private javax.swing.JLabel txtJenisTrf;
    private javax.swing.JLabel txtNoPol;
    private javax.swing.JTextArea txtOutput;
    private javax.swing.JLabel txtbarcode;
    // End of variables declaration//GEN-END:variables
}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tools;

import acs.jni.ACR120U;

/**
 *
 * @author Hades
 */
public class CardReader {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here
        
        short i = 34;
        ACR120U obj = new ACR120U();
        obj.open(i);
    }
    
}

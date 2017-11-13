/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tools;

import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.WebcamResolution;
import java.awt.Dimension;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

/**
 *
 * @author Hades
 */
public class WebcamCapture {
    
    public void capture(String filename) throws IOException{
        Webcam webc = Webcam.getDefault();   
        webc.setViewSize(WebcamResolution.VGA.getSize());     
        webc.open();
        ImageIO.write(webc.getImage(), "JPG", new File(filename+".jpg"));
        webc.close();
    }

}

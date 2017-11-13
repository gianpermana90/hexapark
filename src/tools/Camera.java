/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tools;

import com.googlecode.javacv.FrameGrabber;
import com.googlecode.javacv.OpenCVFrameGrabber;
import com.googlecode.javacv.VideoInputFrameGrabber;
import com.googlecode.javacv.cpp.opencv_core.IplImage;
import static com.googlecode.javacv.cpp.opencv_core.cvFlip;
import static com.googlecode.javacv.cpp.opencv_highgui.*;

public class Camera {

    public boolean capture(String filename) {
        boolean status = false;
        // 0-default camera, 1 - next...so on
        final OpenCVFrameGrabber grabber = new OpenCVFrameGrabber(0);
        try {
            grabber.start();
            IplImage img = null;
            for (int i = 0; i < 10; i++) {
                img = grabber.grab();
            }           
            if (img != null) {
                cvSaveImage(filename+".jpg", img);
                status = true;
            }
            grabber.stop();
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return status;
    }

    
    
}

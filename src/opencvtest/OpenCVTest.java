/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package opencvtest;

import com.googlecode.javacv.*;
import com.googlecode.javacv.cpp.opencv_core;
import static com.googlecode.javacv.cpp.opencv_core.*;
import static com.googlecode.javacv.cpp.opencv_core.cvSize;
import static com.googlecode.javacv.cpp.opencv_highgui.*;
import static com.googlecode.javacv.cpp.opencv_highgui.cvLoadImage;
import static com.googlecode.javacv.cpp.opencv_imgproc.CV_BGR2HSV;
import static com.googlecode.javacv.cpp.opencv_imgproc.CV_CHAIN_APPROX_TC89_KCOS;
import static com.googlecode.javacv.cpp.opencv_imgproc.CV_SHAPE_RECT;
import static com.googlecode.javacv.cpp.opencv_imgproc.CV_MOP_CLOSE;
import static com.googlecode.javacv.cpp.opencv_imgproc.CV_RETR_LIST;
import static com.googlecode.javacv.cpp.opencv_imgproc.CV_THRESH_BINARY;
import static com.googlecode.javacv.cpp.opencv_imgproc.CV_THRESH_BINARY_INV;
import static com.googlecode.javacv.cpp.opencv_imgproc.IplConvKernel;
import static com.googlecode.javacv.cpp.opencv_imgproc.cvCvtColor;
import static com.googlecode.javacv.cpp.opencv_imgproc.cvFindContours;
import static com.googlecode.javacv.cpp.opencv_imgproc.cvMorphologyEx;
import static com.googlecode.javacv.cpp.opencv_imgproc.cvThreshold;



/**
 *
 * @author Administrator
 */
public class OpenCVTest {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args)
        throws Exception
    {
        CanvasFrame canvas = new CanvasFrame("Out");
        OpenCVFrameGrabber grabber = new OpenCVFrameGrabber(0);
        grabber.setImageHeight(480);
        grabber.setImageWidth(640);
        grabber.start();
        //while(true)
        {
            IplImage img = grabber.grab();
            if(img == null) return;
            canvas.setCanvasSize(img.width(), img.height());
            canvas.showImage(img);
            //Thread.sleep(5000);
            //canvas.dispose();
            //cvReleaseImage(img);
        }
        grabber.stop();
        
        IplImage img = cvLoadImage(
            "C:\\WindRiver\\workspace\\VisionSample2013\\VisionImages\\First Choice Green Images\\HybridLine_SmallGreen2.jpg");
        if(img == null) return;
        
        CvSize size = cvSize(img.width(),img.height());
        IplImage bin = IplImage.create(size, 8, 1);
        IplImage hsv = IplImage.create(size, 8, 3);
        IplImage hue = IplImage.create(size, 8, 1);
        IplImage sat = IplImage.create(size, 8, 1);
        IplImage val = IplImage.create(size, 8, 1);
        
        canvas.setCanvasSize(img.width(), img.height());
        canvas.showImage(img);
        
        Thread.sleep(3000);
        
        cvCvtColor(img, hsv, CV_BGR2HSV);
        cvSplit(hsv, hue, sat, val, null);
        
        // red
        /*
        cvThreshold(hue, bin, 60-15, 255, CV_THRESH_BINARY);
        cvThreshold(hue, hue, 60+15, 255, CV_THRESH_BINARY_INV);
        cvThreshold(sat, sat, 200, 255, CV_THRESH_BINARY);
        cvThreshold(val, val, 55, 255, CV_THRESH_BINARY);
        */
   
        // green
        cvThreshold(hue, bin, 60, 100, CV_THRESH_BINARY);
        //cvThreshold(hue, hue, 60+15, 255, CV_THRESH_BINARY_INV);
        cvThreshold(sat, sat, 90, 255, CV_THRESH_BINARY);
        cvThreshold(val, val, 20, 255, CV_THRESH_BINARY);
        cvAnd(hue, bin, bin, null);
        cvAnd(bin, sat, bin, null);
        cvAnd(bin, val, bin, null);
        
        canvas.showImage(bin);
        Thread.sleep(3000);
        
        IplConvKernel morphKernel = IplConvKernel.create(3, 3, 1, 1, CV_SHAPE_RECT, null);
        cvMorphologyEx(bin, bin, null, morphKernel, CV_MOP_CLOSE, 5);
        
        //cvInRangeS(hsv, CvScalar(60, 200, 55), CvScalar(255, 255, 255), bin);
        //cvInRange(hsv, CvArr(60, 200, 55), CvArr(255, 255, 255), bin);

        canvas.showImage(bin);
        Thread.sleep(3000);
        
        IplImage tempImage = IplImage.create(bin.cvSize(), bin.depth(), 1);
        CvSeq contours = new CvSeq();
        CvMemStorage storage = CvMemStorage.create();
        cvFindContours(tempImage, storage, contours, 256, CV_RETR_LIST, CV_CHAIN_APPROX_TC89_KCOS);
        while(contours != null)
        {
            contours = contours.h_next();
        }

        canvas.dispose();
    }
}

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
//package opencvtest;
package edu.wpi.first.wpijavacv;

import com.googlecode.javacv.*;
import com.googlecode.javacv.cpp.opencv_core;
import static com.googlecode.javacv.cpp.opencv_core.*;
import static com.googlecode.javacv.cpp.opencv_core.cvSize;
import static com.googlecode.javacv.cpp.opencv_core.cvDrawContours;
import static com.googlecode.javacv.cpp.opencv_highgui.*;
import static com.googlecode.javacv.cpp.opencv_highgui.cvLoadImage;
import static com.googlecode.javacv.cpp.opencv_imgproc.CV_BGR2HSV;
import static com.googlecode.javacv.cpp.opencv_imgproc.CV_CHAIN_APPROX_TC89_KCOS;
import static com.googlecode.javacv.cpp.opencv_imgproc.CV_CLOCKWISE;
import static com.googlecode.javacv.cpp.opencv_imgproc.CV_SHAPE_RECT;
import static com.googlecode.javacv.cpp.opencv_imgproc.CV_MOP_CLOSE;
import static com.googlecode.javacv.cpp.opencv_imgproc.CV_RETR_LIST;
import static com.googlecode.javacv.cpp.opencv_imgproc.CV_THRESH_BINARY;
import static com.googlecode.javacv.cpp.opencv_imgproc.CV_THRESH_BINARY_INV;
import static com.googlecode.javacv.cpp.opencv_imgproc.IplConvKernel;
import static com.googlecode.javacv.cpp.opencv_imgproc.cvConvexHull2;
import static com.googlecode.javacv.cpp.opencv_imgproc.cvCvtColor;
import static com.googlecode.javacv.cpp.opencv_imgproc.cvFindContours;
import static com.googlecode.javacv.cpp.opencv_imgproc.cvMorphologyEx;
import static com.googlecode.javacv.cpp.opencv_imgproc.cvThreshold;
import edu.wpi.first.wpijavacv.WPIContour;
import edu.wpi.first.wpijavacv.WPIPolygon;



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
        CanvasFrame canvas = new CanvasFrame("Vision Tracking", CV_WINDOW_AUTOSIZE);
        canvas.setDefaultCloseOperation(javax.swing.JFrame.EXIT_ON_CLOSE);
        
        // Frame grabber
        /*
        OpenCVFrameGrabber grabber = new OpenCVFrameGrabber(0);
        grabber.setImageHeight(480);
        grabber.setImageWidth(640);
        grabber.start();
        */
        
        // CvCapture
        CvCapture capture = cvCreateCameraCapture(0);
        cvSetCaptureProperty(capture, CV_CAP_PROP_FRAME_WIDTH, 1280);
        cvSetCaptureProperty(capture, CV_CAP_PROP_FRAME_HEIGHT, 1024);
        
        while(true)
        {
            //IplImage img = grabber.grab();
            IplImage img = cvQueryFrame(capture);
            if(img == null) return;
            canvas.setCanvasSize(img.width(), img.height());
            //canvas.showImage(img);
            //Thread.sleep(5000);
            //canvas.dispose();
            //cvReleaseImage(img);
            //}
            //grabber.stop();

            //IplImage img = cvLoadImage(
             //   "C:\\WindRiver\\workspace\\VisionSample2013\\VisionImages\\First Choice Green Images\\HybridLine_SmallGreen2.jpg");
                //"C:\\WindRiver\\workspace\\VisionSample2013\\VisionImages\\First Choice Green Images\\Midfield_SmallGreen2.jpg");

            if(img == null) return;

            CvSize size = cvSize(img.width(),img.height());
            IplImage bin = IplImage.create(size, 8, 1);
            IplImage hsv = IplImage.create(size, 8, 3);
            IplImage hue = IplImage.create(size, 8, 1);
            IplImage sat = IplImage.create(size, 8, 1);
            IplImage val = IplImage.create(size, 8, 1);

            canvas.setCanvasSize(img.width(), img.height());
            //canvas.showImage(img);

            //Thread.sleep(3000);

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
            cvThreshold(hue, hue, 60+15, 100, CV_THRESH_BINARY_INV);
            cvThreshold(sat, sat, 90, 255, CV_THRESH_BINARY);
            cvThreshold(val, val, 20, 255, CV_THRESH_BINARY);
            cvAnd(hue, bin, bin, null);
            cvAnd(bin, sat, bin, null);
            cvAnd(bin, val, bin, null);

            //canvas.showImage(bin);
            //Thread.sleep(3000);

            IplConvKernel morphKernel = IplConvKernel.create(3, 3, 1, 1, CV_SHAPE_RECT, null);
            cvMorphologyEx(bin, bin, null, morphKernel, CV_MOP_CLOSE, 2);

            //cvInRangeS(hsv, CvScalar(60, 200, 55), CvScalar(255, 255, 255), bin);
            //cvInRange(hsv, CvArr(60, 200, 55), CvArr(255, 255, 255), bin);

            //canvas.showImage(bin);
            //Thread.sleep(3000);

            IplImage tempImage = IplImage.create(bin.cvSize(), bin.depth(), 1);
            cvCopy(bin, tempImage);

            WPIColorImage ci = new WPIColorImage(img);

            WPIPolygon top = null;
            int rectCount = 0;

            // first pass, find polygons to fill
            CvSeq contours = new CvSeq();
            CvMemStorage storage = CvMemStorage.create();
            cvFindContours(tempImage, storage, contours, 256, CV_RETR_LIST, CV_CHAIN_APPROX_TC89_KCOS);
            while(!WPIDisposable.isNull(contours))
            {
                CvSeq convexContour = cvConvexHull2(contours, storage, CV_CLOCKWISE, 1);
                cvDrawContours(tempImage, convexContour, CvScalar.WHITE, CvScalar.WHITE, -1, CV_FILLED, 8);
                //canvas.showImage(tempImage);
                //Thread.sleep(500);
                contours = contours.h_next();
            }

            // second pass filled in
            contours = new CvSeq();
            cvFindContours(tempImage, storage, contours, 256, CV_RETR_LIST, CV_CHAIN_APPROX_TC89_KCOS);
            while(!WPIDisposable.isNull(contours))
            {
                CvSeq convexContour = cvConvexHull2(contours, storage, CV_CLOCKWISE, 1);
                WPIContour contour = new WPIContour(cvCloneSeq(convexContour, storage));
                if(contour.getWidth() != contour.getHeight())
                {
                    /*
                    System.out.println("width = " + contour.getWidth() + 
                            ", height = " + contour.getHeight());
                    */
                    WPIPolygon p = contour.approxPolygon(20);
                    System.out.println("width = " + p.getWidth() + 
                            ", height = " + p.getHeight() + " x = " + p.getX() +
                            ", y = " + p.getY());
                    if(p.isConvex() && p.getNumVertices() == 4)
                    {
                        System.out.println("Looks like a rect.");
                        ci.drawPolygon(p, WPIColor.RED, 2);
                        WPIPoint pt = new WPIPoint(p.getX() + (p.getWidth()) / 2,
                            p.getY() + (p.getHeight()) / 2);
                        ci.drawPoint(pt, WPIColor.YELLOW, 5);
                        if(top == null || p.getY() < top.getY())
                        {
                            top = p;
                            System.out.println("Top!");
                        }
                        rectCount++;
                    }
                }
                contours = contours.h_next();
            }

            if(rectCount >= 3 && top != null)
            {
                WPIPoint pt = new WPIPoint(top.getX() + (top.getWidth()) / 2,
                    top.getY() + (top.getHeight()) / 2);
                ci.drawPoint(pt, WPIColor.BLUE, 5);
            }

            canvas.showImage(ci.image);
            //Thread.sleep(10000);
            
            /*
            int k = cvWaitKey(0);
            System.out.println(k);
            if(k == 27) break;
            */
        }

        //canvas.dispose();
    }
}

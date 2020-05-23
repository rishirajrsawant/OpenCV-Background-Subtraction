package com.example.opencvbgsubtraction;

import androidx.appcompat.app.AppCompatActivity;
import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.JavaCameraView;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfKeyPoint;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.features2d.Feature2D;
import org.opencv.features2d.FeatureDetector;
import org.opencv.features2d.Features2d;
import org.opencv.features2d.SimpleBlobDetector;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.video.*;

import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static org.opencv.imgproc.Imgproc.CHAIN_APPROX_SIMPLE;
import static org.opencv.imgproc.Imgproc.HISTCMP_BHATTACHARYYA;
import static org.opencv.imgproc.Imgproc.MORPH_CLOSE;
import static org.opencv.imgproc.Imgproc.MORPH_OPEN;
import static org.opencv.imgproc.Imgproc.RETR_EXTERNAL;
import static org.opencv.imgproc.Imgproc.RETR_FLOODFILL;
import static org.opencv.imgproc.Imgproc.RETR_LIST;
import static org.opencv.imgproc.Imgproc.RETR_TREE;
import static org.opencv.imgproc.Imgproc.THRESH_BINARY;
import static org.opencv.imgproc.Imgproc.boundingRect;
import static org.opencv.imgproc.Imgproc.contourArea;
import static org.opencv.imgproc.Imgproc.getStructuringElement;


public class MainActivity extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener2 {

    CameraBridgeViewBase cameraBridgeViewBase;
    BaseLoaderCallback baseLoaderCallback;
    private Mat mRgb;
    private Mat mFGMask;
    private Mat frame, blur, thresh, dilate, frame2, diff;
    private BackgroundSubtractorMOG2 mog2;
    private MatOfKeyPoint keypoints;
    private Mat descriptors;
    private Features2d features2d;
    private SimpleBlobDetector detector;
    //private List<MatOfPoint> contours;
    private Mat hierarchy;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        cameraBridgeViewBase = (JavaCameraView)findViewById(R.id.CameraView);
        cameraBridgeViewBase.setVisibility(SurfaceView.VISIBLE);
        cameraBridgeViewBase.setCvCameraViewListener(this);



        //System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        baseLoaderCallback = new BaseLoaderCallback(this) {
            @Override
            public void onManagerConnected(int status) {
                super.onManagerConnected(status);

                switch(status){

                    case BaseLoaderCallback.SUCCESS:
                        cameraBridgeViewBase.enableView();
                        break;
                    default:
                        super.onManagerConnected(status);
                        break;
                }


            }

        };

    }




    @Override
    public void onCameraViewStarted(int width, int height) {
        mRgb = new Mat();
        mFGMask = new Mat();
        mog2 = Video.createBackgroundSubtractorMOG2(0, 10, true);
        keypoints = new MatOfKeyPoint();
        descriptors = new Mat();
        features2d = new Features2d();
        hierarchy = new Mat();
        blur = new Mat();
        thresh = new Mat();
        dilate = new Mat();
        diff = new Mat();
        frame2 = new Mat();
    }

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        frame = inputFrame.rgba();
        List<MatOfPoint> contours = new ArrayList<MatOfPoint>();


        // detector = SimpleBlobDetector.create();

        Imgproc.cvtColor(frame, mRgb, Imgproc.COLOR_RGBA2GRAY);
        mog2.apply(frame, mFGMask, 0.4); //apply() exports a gray image by definition
        Imgproc.cvtColor(mFGMask, frame, Imgproc.COLOR_GRAY2RGBA);

//        detector.detect(mFGMask, keypoints, frame);
//        int flags = features2d.DRAW_RICH_KEYPOINTS;
//        features2d.drawKeypoints(mFGMask, keypoints, frame, new Scalar(0,0,255), flags);

        //detector.compute(frame, keypoints, descriptors);



          Imgproc.GaussianBlur(mFGMask, blur, new Size(5, 5), 0);
//          Imgproc.Canny(blur, blur, 80, 100);
          Imgproc.threshold(blur, thresh, 20, 255, THRESH_BINARY);
          Imgproc.dilate(thresh, dilate, Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(2, 2)));
          Imgproc.findContours(dilate, contours, hierarchy, RETR_TREE, CHAIN_APPROX_SIMPLE);

          double maxVal = 0;
          int maxValIdx = 0;
        for ( int contourIdx=0; contourIdx < contours.size(); contourIdx++ )
        {
            // Minimum size allowed for consideration
            MatOfPoint2f approxCurve = new MatOfPoint2f();
            MatOfPoint2f contour2f = new MatOfPoint2f( contours.get(contourIdx).toArray() );
            //Processing on mMOP2f1 which is in type MatOfPoint2f
            double approxDistance = Imgproc.arcLength(contour2f, true)*0.02;
            Imgproc.approxPolyDP(contour2f, approxCurve, approxDistance, true);

            //Convert back to MatOfPoint
            MatOfPoint points = new MatOfPoint( approxCurve.toArray() );

            // Get bounding rect of contour
            Rect rect = Imgproc.boundingRect(points);

            double contourArea = Imgproc.contourArea(contours.get(contourIdx));

            Log.i("area798", "a:"+contourArea);

            if (contourArea < 3000){}
            else
                Imgproc.rectangle(frame, new Point(rect.x, rect.y), new Point(rect.x + rect.width, rect.y + rect.height), new Scalar(0, 255, 0), 3);

        }

//          for(int idx = 0; idx >= 0; idx = (int) hierarchy.get(0, idx)[0]) {
//            MatOfPoint matOfPoint = contours.get(idx);
//            Rect rect = Imgproc.boundingRect(matOfPoint);
//            if(maxArea < 500000)
//            {
//                //
//            }
//            else
//                Imgproc.rectangle(frame, rect.tl(), rect.br(), new Scalar(0, 255, 0));
//
//          }
          //Imgproc.drawContours(mRgb, contours, -1, new Scalar(0, 255, 0), 2);


//        for (int contourIdx = 0; contourIdx < contours.size(); contourIdx++) {
//            if(maxArea < 900000)
//            {
//                //
//            }
//            else
//                Imgproc.drawContours(frame, contours, contourIdx, new Scalar(0, 255, 0), 2);
//
//
//        }
        //Imgproc.erode(mFGMask, frame, Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(2,2)));
//        Imgproc.dilate(mFGMask, frame, Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(2, 2)));
//        Imgproc.morphologyEx(mFGMask, frame, MORPH_OPEN, Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(2, 2)));
//        Imgproc.morphologyEx(mFGMask, frame, MORPH_CLOSE, Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(2, 2)));


//        //System.gc();
        contours.clear();
        return frame;
    }


    @Override
    public void onCameraViewStopped() {
        frame.release();
    }


    @Override
    protected void onResume() {
        super.onResume();

        if (!OpenCVLoader.initDebug()){
            Toast.makeText(getApplicationContext(),"OpenCV not loaded properly!", Toast.LENGTH_SHORT).show();
        }

        else
        {
            baseLoaderCallback.onManagerConnected(baseLoaderCallback.SUCCESS);
        }



    }

    @Override
    protected void onPause() {
        super.onPause();
        if(cameraBridgeViewBase!=null){

            cameraBridgeViewBase.disableView();
        }

    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (cameraBridgeViewBase!=null){
            cameraBridgeViewBase.disableView();
        }
    }
}

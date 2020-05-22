package com.example.opencvbgsubtraction;

import androidx.appcompat.app.AppCompatActivity;
import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.JavaCameraView;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.features2d.SimpleBlobDetector;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.video.*;

import android.os.Bundle;
import android.view.SurfaceView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener2 {

    CameraBridgeViewBase cameraBridgeViewBase;
    BaseLoaderCallback baseLoaderCallback;
    private Mat mRgb;
    private Mat mFGMask;
    private Mat frame;
    private BackgroundSubtractorMOG2 mog2;
    private SimpleBlobDetector simpleBlobDetector;
    private List<MatOfPoint> contours;
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
        hierarchy = new Mat();
        contours = new ArrayList<>();
    }

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {

        frame = inputFrame.rgba();


        Imgproc.cvtColor(frame, mRgb, Imgproc.COLOR_RGBA2RGB); //the apply function will throw the above error if you don't feed it an RGB image
        mog2.apply(mRgb, mFGMask, 0.1); //apply() exports a gray image by definition
        Imgproc.cvtColor(mFGMask, frame, Imgproc.COLOR_GRAY2RGBA);

//        Imgproc.findContours(mFGMask, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);
//        Imgproc.drawContours(frame, contours, -1, new Scalar(100, 125, 230));
// now iterate over all top level contours
//        for(int idx = 0; idx >= 0; idx = (int) hierarchy.get(0, idx)[0]) {
//            MatOfPoint matOfPoint = contours.get(idx);
//            Rect rect = Imgproc.boundingRect(matOfPoint);
//            Imgproc.rectangle(frame, rect.tl(), rect.br(), new Scalar(0, 0, 255));
//
//        }
//        //System.gc();
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

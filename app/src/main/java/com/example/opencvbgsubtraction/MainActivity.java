package com.example.opencvbgsubtraction;

import androidx.appcompat.app.AppCompatActivity;
import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.JavaCameraView;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.Mat;

import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;
import org.opencv.video.*;

import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceView;
import android.widget.Toast;



public class MainActivity extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener2 {

    CameraBridgeViewBase cameraBridgeViewBase;
    BaseLoaderCallback baseLoaderCallback;
    private Mat mRgb;
    private Mat mFGMask;
    private Mat frame, whiteFrame, threshold;
    private BackgroundSubtractorMOG2 mog2;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        cameraBridgeViewBase = (JavaCameraView)findViewById(R.id.CameraView);
        cameraBridgeViewBase.setVisibility(SurfaceView.VISIBLE);
        cameraBridgeViewBase.setCvCameraViewListener(this);
        cameraBridgeViewBase.enableFpsMeter();
        cameraBridgeViewBase.setMaxFrameSize(640, 480);



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
        whiteFrame = new Mat();
        threshold = new Mat();
        mog2 = Video.createBackgroundSubtractorMOG2(0, 10, true);

    }

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        frame = inputFrame.rgba();

        Imgproc.cvtColor(frame, mRgb, Imgproc.COLOR_RGBA2GRAY);
        mog2.apply(frame, mFGMask, 0.4); //apply() exports a gray image by definition
        Imgproc.cvtColor(mFGMask, frame, Imgproc.COLOR_GRAY2RGBA);

        //converting into gray image
        Imgproc.cvtColor(frame, frame, Imgproc.COLOR_RGBA2GRAY);

        //calculating threshold
        Imgproc.threshold(frame, threshold, 127, 255, Imgproc.THRESH_BINARY);

        //applying bitwise-xor
        Core.bitwise_xor(frame, threshold, whiteFrame);

        //getting the count of white pixels
        int n = Core.countNonZero(whiteFrame);
        float qom = ((float) n)/(307200);

        String result = String.format("%.4f", qom);
        Log.i("whitePixels", "white:"+n);
        Log.i("whitePixels", "QoM:"+result);

        Imgproc.cvtColor(frame,frame,Imgproc.COLOR_GRAY2RGBA);

        Imgproc.putText(frame, "Quantity of Motion:"+result, new Point(5,30), 0, 0.7, new Scalar(255,0,0), 2);
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

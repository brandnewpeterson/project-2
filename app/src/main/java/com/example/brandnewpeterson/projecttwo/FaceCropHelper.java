package com.example.brandnewpeterson.projecttwo;

/**
 * Created by brandnewpeterson on 1/31/16.
 */

import android.content.Context;
import android.graphics.Bitmap;
import android.util.SparseArray;

import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.face.Face;
import com.google.android.gms.vision.face.FaceDetector;
import com.google.android.gms.vision.face.Landmark;


public class FaceCropHelper {
    private Context context;
    private Bitmap srcImg;
    private int firstLeftEyeY = 0;
    private FaceDetector detector;

    public FaceCropHelper(Context context, Bitmap srcImg) {
        this.context = context;
        this.srcImg = srcImg;
    }

    public void setupFaceDetector(){
        detector = new FaceDetector.Builder(context)
                .setTrackingEnabled(false)
                .setLandmarkType(FaceDetector.ALL_LANDMARKS)
                .build();
    }

    public void releaseFaceDetector(){
        detector.release();
    }

    public int getEyeY(){

        if (detector.isOperational()) {

            Frame frame = new Frame.Builder().setBitmap(srcImg).build();
            SparseArray<Face> faces = detector.detect(frame);

            for (int i = 0; i < faces.size(); ++i) {
                Face face = faces.valueAt(i);
                for (Landmark landmark : face.getLandmarks()) {
                    if (landmark.getType() == Landmark.LEFT_EYE) {
                        System.out.println(
                                "FD Left eye at: " + landmark.getPosition().x + "," + landmark.getPosition().y
                        );
                        firstLeftEyeY = (int) landmark.getPosition().y;
                        break;
                    }
                }
            }
        }else{
            //System.out.println("FaceDetector is unoperational now. It will likely work after library downloads from Google Svcs.");
        }

        return firstLeftEyeY;

    }

    //Will crop src image with vertically centered eyes and same aspect ratio as parent view.
    public Bitmap getFaceCroppedBitmap(int viewW, int viewH){

        int srcImgW = srcImg.getWidth();
        int aspectR = viewW/viewH;
        int h = srcImgW/aspectR;
        int y = Math.max(firstLeftEyeY - h / 2, 0); //Top border will be eye minus offset, or 0 if the first is neg.
        Bitmap faceCroppedBitmap = Bitmap.createBitmap(srcImg,
                0,
                y,
                srcImgW,
                h
        );
        return faceCroppedBitmap;
    }

}

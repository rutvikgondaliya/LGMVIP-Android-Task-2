package com.rutvik.facedetection;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PointF;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.face.Face;
import com.google.mlkit.vision.face.FaceDetection;
import com.google.mlkit.vision.face.FaceDetector;
import com.google.mlkit.vision.face.FaceDetectorOptions;
import com.google.mlkit.vision.face.FaceLandmark;

import java.util.List;


public class MainActivity extends AppCompatActivity {

    private static final int REQ_CODE=100;
    Dialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void faceDetect(View view) {
        Intent i=new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(i,REQ_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode==REQ_CODE){
            if(resultCode == RESULT_OK){
                Bitmap photo=(Bitmap)data.getExtras().get("data");
                faceDetection(photo);
            }else if(resultCode==RESULT_CANCELED){
                Toast.makeText(this,"Operation cancelled by you!",Toast.LENGTH_LONG).show();
            }else{
                Toast.makeText(this,"Failed to capture image !",Toast.LENGTH_LONG).show();
            }
        }
    }

    private void faceDetection(Bitmap photo) {
        FaceDetectorOptions highAccuracyOpts =
                new FaceDetectorOptions.Builder()
                        .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE)
                        .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_ALL)
                        .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
                        .build();

        InputImage image = InputImage.fromBitmap(photo,0);
        FaceDetector detector = FaceDetection.getClient(highAccuracyOpts);
        Task<List<Face>> result =
                detector.process(image)
                        .addOnSuccessListener(
                                new OnSuccessListener<List<Face>>() {
                                    @Override
                                    public void onSuccess(List<Face> faces) {
                                        Toast.makeText(getApplicationContext(),"To close dialog click anywhere outside the box!",Toast.LENGTH_LONG).show();
                                        for (Face face : faces) {

                                            // If landmark detection was enabled (mouth, ears, eyes, cheeks, and
                                            // nose available):
                                            String eyes="",ears="",nose="",mouth="",smile="",righteyeopen="",lefteyeopen="";
                                            FaceLandmark leftEye = face.getLandmark(FaceLandmark.LEFT_EYE);
                                            if (leftEye != null) {
                                                PointF leftEyepos = leftEye.getPosition();
                                                eyes="(L: "+leftEyepos.toString();
                                            }
                                            FaceLandmark rightEye = face.getLandmark(FaceLandmark.RIGHT_EYE);
                                            if (rightEye != null) {
                                                PointF RightEyepos = rightEye.getPosition();
                                                eyes=eyes+",\nR:"+RightEyepos.toString()+")";
                                            }

                                            FaceLandmark leftEar = face.getLandmark(FaceLandmark.LEFT_EAR);
                                            if (leftEar != null) {
                                                PointF leftEarpos = leftEar.getPosition();
                                                ears="(L: "+leftEarpos.toString();
                                            }
                                            FaceLandmark rightEar = face.getLandmark(FaceLandmark.RIGHT_EAR);
                                            if (rightEar != null) {
                                                PointF RightEarpos = rightEar.getPosition();
                                                ears=ears+",\nR:"+RightEarpos.toString()+")";
                                            }

                                            FaceLandmark noset = face.getLandmark(FaceLandmark.NOSE_BASE);
                                            if (noset != null) {
                                                PointF nosepos = noset.getPosition();
                                                nose=nosepos.toString();
                                            }
                                            FaceLandmark mouthb = face.getLandmark(FaceLandmark.MOUTH_LEFT);
                                            if (mouthb != null) {
                                                PointF mouthpos = mouthb.getPosition();
                                                mouth="(L: "+mouthpos.toString();
                                            }
                                            FaceLandmark mouthr = face.getLandmark(FaceLandmark.MOUTH_RIGHT);
                                            if (mouthr != null) {
                                                PointF mouthpos = mouthr.getPosition();
                                                mouth=mouth+"\nR: "+mouthpos.toString();
                                            }
                                            FaceLandmark mouthl = face.getLandmark(FaceLandmark.MOUTH_BOTTOM);
                                            if (mouthl != null) {
                                                PointF mouthpos = mouthl.getPosition();
                                                mouth=mouth+"\nB: "+mouthpos.toString()+")";
                                            }

                                            // If classification was enabled:
                                            if (face.getSmilingProbability() != null) {
                                                float smileProb = face.getSmilingProbability();
                                                smile=smileProb+" ";

                                            }
                                            if (face.getRightEyeOpenProbability() != null) {
                                                float rightEyeOpenProb = face.getRightEyeOpenProbability();
                                                righteyeopen=rightEyeOpenProb+" ";
                                            }
                                            if (face.getLeftEyeOpenProbability() != null) {
                                                float leftEyeOpenProb = face.getLeftEyeOpenProbability();
                                                lefteyeopen=leftEyeOpenProb+" ";
                                            }

                                            openDialog(eyes,ears,nose,mouth,smile,lefteyeopen,righteyeopen);


                                        }
                                    }
                                })
                        .addOnFailureListener(
                                new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(getApplicationContext(),"Failed to detect face !",Toast.LENGTH_LONG).show();
                                    }
                                });

    }

    private void openDialog(String eyes,String ears,String noset,String moutht, String smile, String lefteyeopen, String righteyeopen) {
        dialog=new Dialog(MainActivity.this);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setContentView(R.layout.fragment_resultdialog);
        //eyes
        TextView eye=dialog.findViewById(R.id.eye);
        eye.setText(eyes);

        //ears
        TextView ear=dialog.findViewById(R.id.ear);
        ear.setText(ears);

        //nose
        TextView noSE=dialog.findViewById(R.id.nose);
        noSE.setText(noset);

        //mouth
        TextView mouTH=dialog.findViewById(R.id.mouth);
        mouTH.setText(moutht);

        //smile
        TextView smiling=dialog.findViewById(R.id.smiling);
        smiling.setText(smile);

        //eye open
        TextView lopen=dialog.findViewById(R.id.lopen);
        lopen.setText(lefteyeopen);
        TextView ropen=dialog.findViewById(R.id.ropen);
        ropen.setText(lefteyeopen);
        dialog.show();
    }
    
}
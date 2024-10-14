package com.example.virtualwear;
import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.example.virtualwear.ml.LiteModelMovenetSingleposeLightning3;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import org.tensorflow.lite.DataType;
import org.tensorflow.lite.support.image.ImageProcessor;
import org.tensorflow.lite.support.image.TensorImage;
import org.tensorflow.lite.support.image.ops.ResizeOp;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;
import java.io.File;
import java.io.IOException;
import java.util.List;

public class LiveFeeding extends AppCompatActivity {
    ImageProcessor imageProcessor;
    ImageView imageView,imgView_shirt;
    Bitmap bitmap,bitmap_cloths;
    TextureView textureView;
    TextView txt_detect;
    CameraManager cameraManager;
    CameraDevice camDevice;
    Handler handler ;
    HandlerThread handlerThread;
    LiteModelMovenetSingleposeLightning3 model;
    Paint paint= new Paint();
    int cam_id =0;
    float width_aspect_Ratio = 1.60f;
    float img_height_width_ratio = 581/440;
    int widthOfShirt;
    String fileName;
    StorageReference storageReference;
    File localfile;
    ProgressDialog loading;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_live_feed);
        getPermission();


        imageView = findViewById(R.id.imageView);
        textureView = findViewById(R.id.textureView);
        imgView_shirt = findViewById(R.id.imgview_cloths);
        txt_detect = findViewById(R.id.txt_detect);

        Intent intent = getIntent();
        fileName =intent.getStringExtra("file_name");
        storageReference = FirebaseStorage.getInstance().getReference().child(fileName);
        try {
            localfile = File.createTempFile("newshirt","png",this.getCacheDir());
            storageReference.getFile(localfile);
            bitmap_cloths = BitmapFactory.decodeFile(localfile.getAbsolutePath());
        } catch (IOException e) {
            Toast.makeText(this, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
            e.printStackTrace();
            throw new RuntimeException(e);
        }


        imageProcessor = new ImageProcessor.Builder().add(new ResizeOp(192,192, ResizeOp.ResizeMethod.BILINEAR)).build();
        cameraManager =(CameraManager) getSystemService(CAMERA_SERVICE);
        handlerThread = new HandlerThread("videoThread");
        handlerThread.start();
        handler = new Handler(handlerThread.getLooper());

        paint.setColor(Color.RED);

        textureView.setSurfaceTextureListener(new TextureView.SurfaceTextureListener() {
            @Override
            public void onSurfaceTextureAvailable(@NonNull SurfaceTexture surfaceTexture, int i, int i1) {
                try {
                    open_Camera();
                } catch (CameraAccessException e) {
                    throw new RuntimeException(e);
                }
            }

            @Override
            public void onSurfaceTextureSizeChanged(@NonNull SurfaceTexture surfaceTexture, int i, int i1) {

            }

            @Override
            public boolean onSurfaceTextureDestroyed(@NonNull SurfaceTexture surfaceTexture) {
                return false;
            }

            @Override
            public void onSurfaceTextureUpdated(@NonNull SurfaceTexture surfaceTexture) {
                bitmap = textureView.getBitmap();
                float[] outputFeature0;
                try {
                    model = LiteModelMovenetSingleposeLightning3.newInstance(LiveFeeding.this);
                    TensorImage tensorImage = new TensorImage(DataType.FLOAT32);
                    tensorImage.load(bitmap);
                    tensorImage = imageProcessor.process(tensorImage);

                    // Creates inputs for reference.
                    TensorBuffer inputFeature0 = TensorBuffer.createFixedSize(new int[]{1, 192, 192, 3}, DataType.FLOAT32);
                    inputFeature0.loadBuffer(tensorImage.getBuffer());

                    // Runs model inference and gets result.
                    LiteModelMovenetSingleposeLightning3.Outputs outputs = model.process(inputFeature0);
                     outputFeature0 = outputs.getOutputFeature0AsTensorBuffer().getFloatArray();

                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                Bitmap mutableBitmap = bitmap.copy(Bitmap.Config.ARGB_8888,true);
                int h= bitmap.getHeight();
                int w = bitmap.getWidth();

                if(outputFeature0[20] > 0.45)
                {
                    bitmap_cloths = BitmapFactory.decodeFile(localfile.getAbsolutePath());
                    if(bitmap_cloths != null)
                    {
                        widthOfShirt = (int) ((int)((outputFeature0[16]*w) -(outputFeature0[19]*w)) * width_aspect_Ratio);
                        bitmap_cloths = Bitmap.createScaledBitmap(bitmap_cloths,widthOfShirt,(int)(widthOfShirt*img_height_width_ratio+60),true);
                        float curr_Scale = ((outputFeature0[16]*w)-(outputFeature0[19]*w)) /140;
                        int offset_x = (int)(44*curr_Scale);
                        int offset_y = (int)(48*curr_Scale);
                        imgView_shirt.setImageBitmap(bitmap_cloths);
                        if(((outputFeature0[18]*h-offset_y+20)+(widthOfShirt*img_height_width_ratio+60)) > imageView.getHeight())
                        {
                            txt_detect.setText("Shirt Size out of the Range");
                            txt_detect.setTextColor(getResources().getColor(R.color.Red));
                            imgView_shirt.setVisibility(View.GONE);
                        }
                        else
                        {
                            txt_detect.setText("Human Detected");
                            txt_detect.setTextColor(getResources().getColor(R.color.Green));
                            imgView_shirt.setVisibility(View.VISIBLE);
                        }
                        imgView_shirt.setX(outputFeature0[19]*w-offset_x+20);
                        imgView_shirt.setY(outputFeature0[18]*h-offset_y+20);
                    }
                }
                else {
                    txt_detect.setTextColor(getResources().getColor(R.color.Red));
                    txt_detect.setText("Human Not Detected");
                    imgView_shirt.setVisibility(View.GONE);
                }
                imageView.setImageBitmap(mutableBitmap);
            }
        });
    }

    @SuppressLint("MissingPermission")
    private void open_Camera() throws CameraAccessException {
        cameraManager.openCamera(cameraManager.getCameraIdList()[cam_id],new CameraDevice.StateCallback() {
            @Override
            public void onOpened(@NonNull CameraDevice cameraDevice) {
                camDevice = cameraDevice;
                CaptureRequest.Builder captureRequestBuild = null;
                try {
                    captureRequestBuild  = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
                    Surface surface = new Surface(textureView.getSurfaceTexture());
                    captureRequestBuild.addTarget(surface);
                    CaptureRequest captureRequest = captureRequestBuild.build();
                    cameraDevice.createCaptureSession(List.of(surface), new CameraCaptureSession.StateCallback() {
                        @Override
                        public void onConfigured(@NonNull CameraCaptureSession cameraCaptureSession) {
                            try {
                                cameraCaptureSession.setRepeatingRequest(captureRequest,null,null);
                            } catch (CameraAccessException e) {
                                throw new RuntimeException(e);
                            }
                        }

                        @Override
                        public void onConfigureFailed(@NonNull CameraCaptureSession cameraCaptureSession) {

                        }
                    },handler);
                } catch (CameraAccessException e) {
                    throw new RuntimeException(e);
                }
            }

            @Override
            public void onDisconnected(@NonNull CameraDevice cameraDevice) {
            }

            @Override
            public void onError(@NonNull CameraDevice cameraDevice, int i) {
            }
        },handler);
    }

    private void getPermission() {
        if(checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.CAMERA},101);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(grantResults[0] != PackageManager.PERMISSION_GRANTED)
            getPermission();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.option_menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.cam_flip:
                if (cam_id == 0)
                    cam_id = 1;
                else
                    cam_id = 0;

                try {
                    camDevice.close();
                    open_Camera();
                } catch (CameraAccessException e) {
                throw new RuntimeException(e);
                }
                break;
            case R.id.close_app:
                finish();
                break;
        }
        return true;
    }
    public static boolean deleteDir(File dir) {
        if (dir != null && dir.isDirectory()) {
            String[] children = dir.list();
            for (int i = 0; i < children.length; i++) {
                boolean success = deleteDir(new File(dir, children[i]));
                if (!success) {
                    return false;
                }
            }
            return dir.delete();
        } else if(dir!= null && dir.isFile()) {
            return dir.delete();
        } else {
            return false;
        }
    }
    @Override
    protected void onDestroy() {
        try {
            File dir = getCacheDir();
            deleteDir(dir);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        super.onDestroy();
        model.close();
    }
}



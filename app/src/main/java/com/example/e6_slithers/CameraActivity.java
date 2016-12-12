/*********************************
 * 開啟相機步驟：
 * (1)要有 Surface, Camera2, ImageReader
 * (2)打開相機
 ********************************/
package com.example.e6_slithers;

import android.Manifest;
import android.content.Context;
import android.graphics.ImageFormat;
import android.hardware.SensorManager;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureRequest.Builder;
import android.media.ImageReader;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.FrameLayout;
import android.widget.TextView;

import java.util.Arrays;

public class CameraActivity extends AppCompatActivity {

    private SurfaceHolder surfaceHolder;
    private SurfaceView cameraView;
    private CameraManager cameraManager;
    private CameraDevice cameraDevice;
    private Handler cameraHandler;
    private ImageReader imageReader;
    private Builder previewBuilder;
    private final static int CAMERA_REQUEST_CODE = 2222;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        // Camera Surface View
        cameraManager = (CameraManager) this.getSystemService(Context.CAMERA_SERVICE);
        cameraView = (SurfaceView) findViewById(R.id.cameraView);
        surfaceHolder = cameraView.getHolder();
        surfaceHolder.addCallback(surfaceCallBack);

        // Draw in Layout
        FrameLayout drawLayout = (FrameLayout) findViewById(R.id.frame);
        TextView info = (TextView)findViewById(R.id.info);
        drawLayout.addView(new DrawCircle(this, info,
                (SensorManager)getSystemService(Context.SENSOR_SERVICE)));
    }

    // SurfaceHolder Callback => Callback2???
    private SurfaceHolder.Callback surfaceCallBack = new SurfaceHolder.Callback() {
        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            // initialize camera
            initCameraAndPreview();
        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            // set parameters, start preview
        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {
            // release camera
            cameraDevice.close();
        }
    };

    // 開啟相機並且建立取得畫面相關流程
    private void initCameraAndPreview() {
        HandlerThread handlerThread = new HandlerThread("Camera2");
        handlerThread.start();
        cameraHandler = new Handler(handlerThread.getLooper());
        openCamera();
    }

    // 詢問是否能取得權限
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        // If request is cancelled, the result arrays are empty.
        if (requestCode == CAMERA_REQUEST_CODE && grantResults.length > 0) {
            openCamera();
        } else {
            finish();
        }
    }

    // 打開相機
    private void openCamera() {
        try {
            // 給ImageReader格式
            imageReader = ImageReader.newInstance(cameraView.getWidth(), cameraView.getHeight(),
                    ImageFormat.JPEG,/*maxImages*/7);
            // 打開相機 取得權限
            String cameraId = cameraManager.getCameraIdList()[0];
            cameraManager.openCamera(cameraId, DeviceStateCallback, null);

        } catch (CameraAccessException e) {
            finish();
        } catch (SecurityException e) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA}, CAMERA_REQUEST_CODE);
        }
    }

    // 監聽device是否開啟，關閉，硬體錯誤
    private CameraDevice.StateCallback DeviceStateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(CameraDevice camera) {
            cameraDevice = camera;
            try {
                createCameraCaptureSession();
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onDisconnected(CameraDevice camera) {
            camera.close();
            cameraDevice = null;
        }

        @Override
        public void onError(CameraDevice camera, int error) {

        }
    };

    // 建立預覽畫面
    private void createCameraCaptureSession() throws CameraAccessException {
        // 寫入 captureRequest 的 field 設定和輸出的 target surface
        previewBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
        previewBuilder.addTarget(surfaceHolder.getSurface());
        // 一個 request 對應一個 image data
        cameraDevice.createCaptureSession(
                Arrays.asList(surfaceHolder.getSurface(), imageReader.getSurface()),
                SessionPreviewStateCallback, cameraHandler);
    }
    
    // 監控 capture session 狀況
    private CameraCaptureSession.StateCallback SessionPreviewStateCallback
            = new CameraCaptureSession.StateCallback() {
        @Override
        public void onConfigured(CameraCaptureSession session) {
            //mSession = session;
            try {
                // Auto focus should be continuous for camera preview.
                previewBuilder.set(CaptureRequest.CONTROL_AF_MODE,
                        CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);

                // Flash is automatically enabled when necessary.
                previewBuilder.set(CaptureRequest.CONTROL_AE_MODE,
                        CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH);

                // 將捕獲的畫面持續顯示在preview上 *如果要新增拍照功能 null 改成 SessionCaptureCallback
                session.setRepeatingRequest(previewBuilder.build(), null, cameraHandler);
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onConfigureFailed(CameraCaptureSession session) {

        }
    };
}

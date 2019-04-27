package tk.hongbo.car.camera;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.ImageFormat;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.ImageReader;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.util.Size;

import java.util.Collections;

import static android.content.Context.CAMERA_SERVICE;

public class DoorbellCamera {

    private static final String TAG = DoorbellCamera.class.getSimpleName();

    private static final int IMAGE_WIDTH = 1280;
    private static final int IMAGE_HEIGHT = 720;
    private static final int MAX_IMAGES = 1;

    private CameraDevice mCameraDevice;

    private CameraCaptureSession mCaptureSession;

    private CaptureRequest.Builder mPreviewBuilder;
    private Handler backgroundHandler;
    private ImageReader.OnImageAvailableListener imageAvailableListener;
    /**
     * An {@link ImageReader} that handles still image capture.
     */
    private ImageReader mImageReader;

    // Lazy-loaded singleton, so only one instance of the camera is created.
    private DoorbellCamera() {
    }

    private static class InstanceHolder {
        private static DoorbellCamera mCamera = new DoorbellCamera();
    }

    public static DoorbellCamera getInstance() {
        return InstanceHolder.mCamera;
    }

    /**
     * Initialize the camera device
     */
    public void initializeCamera(Context context,
                                 Handler backgroundHandler,
                                 ImageReader.OnImageAvailableListener imageAvailableListener) {
        this.backgroundHandler = backgroundHandler;
        this.imageAvailableListener = imageAvailableListener;
        // Discover the camera instance
        CameraManager manager = (CameraManager) context.getSystemService(CAMERA_SERVICE);
        String[] camIds = {};
        try {
            camIds = manager.getCameraIdList();
        } catch (CameraAccessException e) {
            Log.d(TAG, "Cam access exception getting IDs", e);
        }
        if (camIds.length < 1) {
            Log.d(TAG, "No cameras found");
            return;
        }
        String id = camIds[0];
        Log.d(TAG, "Using camera id " + id);

        // Initialize the image processor
//        mImageReader = ImageReader.newInstance(IMAGE_WIDTH, IMAGE_HEIGHT,
//                ImageFormat.JPEG, MAX_IMAGES); // 创建一个ImageReader对象，用于获取摄像头的图像数据.最后一个参数 用户想要读图像的最大数量
//        mImageReader.setOnImageAvailableListener(
//                imageAvailableListener, backgroundHandler);

        // Open the camera resource
        try {
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            manager.openCamera(id, mStateCallback, backgroundHandler);
        } catch (CameraAccessException cae) {
            Log.d(TAG, "Camera access exception", cae);
        }
    }

    /**
     * Callback handling device state changes
     */
    private final CameraDevice.StateCallback mStateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(CameraDevice cameraDevice) {
            Log.d(TAG, "Opened camera.");
            mCameraDevice = cameraDevice;
            try {
                startPreview();
                Log.d(TAG, "开启预览");
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onDisconnected(CameraDevice cameraDevice) {
            Log.d(TAG, "Camera disconnected, closing.");
            cameraDevice.close();
        }

        @Override
        public void onError(CameraDevice cameraDevice, int i) {
            Log.d(TAG, "Camera device error, closing.");
            cameraDevice.close();
        }

        @Override
        public void onClosed(CameraDevice cameraDevice) {
            Log.d(TAG, "Closed camera, releasing");
            mCameraDevice = null;
        }
    };

    // 开始预览，主要是camera.createCaptureSession这段代码很重要，创建会话
    private void startPreview() throws CameraAccessException {
//        SurfaceTexture texture = mPreviewView.getSurfaceTexture();
//
////      这里设置的就是预览大小
//        texture.setDefaultBufferSize(mPreviewSize.getWidth(), mPreviewSize.getHeight());
//                Surface surface = new Surface(texture);
        try {
            // 设置捕获请求为预览，这里还有拍照啊，录像等
            mPreviewBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }

//      就是在这里，通过这个set(key,value)方法，设置曝光啊，自动聚焦等参数！！ 如下举例：
//      mPreviewBuilder.set(CaptureRequest.CONTROL_AE_MODE,CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH);


//        mImageReader = ImageReader.newInstance(mSurfaceView.getWidth(), mSurfaceView.getHeight(), ImageFormat.JPEG/*此处还有很多格式，比如我所用到YUV等*/, 2/*最大的图片数，mImageReader里能获取到图片数，但是实际中是2+1张图片，就是多一张*/);
//        mImageReader.setOnImageAvailableListener(mOnImageAvailableListener, mHandler);
        mImageReader = ImageReader.newInstance(IMAGE_WIDTH, IMAGE_HEIGHT,
                ImageFormat.JPEG, MAX_IMAGES); // 创建一个ImageReader对象，用于获取摄像头的图像数据.最后一个参数 用户想要读图像的最大数量
        mImageReader.setOnImageAvailableListener(
                imageAvailableListener, backgroundHandler);
        // 这里一定分别add两个surface，一个Textureview的，一个ImageReader的，如果没add，会造成没摄像头预览，或者没有ImageReader的那个回调！！
//        mPreviewBuilder.addTarget(surface);
        mPreviewBuilder.addTarget(mImageReader.getSurface());
//        mCameraDevice.createCaptureSession(Arrays.asList(surface, mImageReader.getSurface()),mSessionStateCallback, mHandler);
        mCameraDevice.createCaptureSession(
                Collections.singletonList(mImageReader.getSurface()),//getSurface获取一个Surface，通过这个函数获取一个Surface用来为ImageReader产生Images即视频流帧数据。
                mSessionCallback,
                null);
    }


    /**
     * Begin a still image capture
     */
    public void takePicture() {
        if (mCameraDevice == null) {
            Log.w(TAG, "Cannot capture image. Camera not initialized.");
            return;
        }

        // Here, we create a CameraCaptureSession for capturing still images.
        try {
            mCameraDevice.createCaptureSession(
                    Collections.singletonList(mImageReader.getSurface()),//getSurface获取一个Surface，通过这个函数获取一个Surface用来为ImageReader产生Images即视频流帧数据。
                    mSessionCallback,
                    null);
        } catch (CameraAccessException cae) {
            Log.d(TAG, "access exception while preparing pic", cae);
        }
    }

    /**
     * Callback handling session state changes
     */
    private CameraCaptureSession.StateCallback mSessionCallback =
            new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(CameraCaptureSession cameraCaptureSession) {
//                    // The camera is already closed
//                    if (mCameraDevice == null) {
//                        return;
//                    }
//
//                    // When the session is ready, we start capture.
//                    mCaptureSession = cameraCaptureSession;
//                    triggerImageCapture();
                    try {
                        updatePreview(cameraCaptureSession);
                    } catch (CameraAccessException e) {
                        e.printStackTrace();
                    }

                }

                @Override
                public void onConfigureFailed(CameraCaptureSession cameraCaptureSession) {
                    Log.w(TAG, "Failed to configure camera");
                }
            };

    private void updatePreview(CameraCaptureSession session) throws CameraAccessException {
        session.setRepeatingRequest(mPreviewBuilder.build(), null, backgroundHandler);
    }

    /**
     * Execute a new capture request within the active session
     */
    private void triggerImageCapture() {
        try {
            final CaptureRequest.Builder captureBuilder =
                    mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            captureBuilder.addTarget(mImageReader.getSurface());
            captureBuilder.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_ON);
//            Log.d(TAG, "Session initialized.");
//            mCaptureSession.capture(captureBuilder.build(), mCaptureCallback, null);
        } catch (CameraAccessException cae) {
            Log.d(TAG, "camera capture exception");
        }
    }

    /**
     * Callback handling capture session events
     */
    private final CameraCaptureSession.CaptureCallback mCaptureCallback =
            new CameraCaptureSession.CaptureCallback() {

                @Override
                public void onCaptureProgressed(CameraCaptureSession session,
                                                CaptureRequest request,
                                                CaptureResult partialResult) {
                    Log.d(TAG, "Partial result");
                }

                @Override
                public void onCaptureCompleted(CameraCaptureSession session,
                                               CaptureRequest request,
                                               TotalCaptureResult result) {
                    if (session != null) {
                        session.close();
                        mCaptureSession = null;
                        Log.d(TAG, "CaptureSession closed");
//                        MainActivity.subThread.sendMessage();
                    }
                }
            };


    /**
     * Close the camera resources
     */
    public void shutDown() {
        if (mCameraDevice != null) {
            mCameraDevice.close();
        }
    }

    /**
     * Helpful debugging method:  Dump all supported camera formats to log.  You don't need to run
     * this for normal operation, but it's very helpful when porting this code to different
     * hardware.
     */
    public static void dumpFormatInfo(Context context) {
        CameraManager manager = (CameraManager) context.getSystemService(CAMERA_SERVICE);
        String[] camIds = {};
        try {
            camIds = manager.getCameraIdList();
        } catch (CameraAccessException e) {
            Log.d(TAG, "Cam access exception getting IDs");
        }
        if (camIds.length < 1) {
            Log.d(TAG, "No cameras found");
        } else {
            String id = camIds[0];
            Log.d(TAG, "Using camera id " + id);
            try {
                CameraCharacteristics characteristics = manager.getCameraCharacteristics(id);
                StreamConfigurationMap configs = characteristics.get(
                        CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
                for (int format : configs.getOutputFormats()) {
                    Log.d(TAG, "Getting sizes for format: " + format);
                    for (Size s : configs.getOutputSizes(format)) {
                        Log.d(TAG, "\t" + s.toString());
                    }
                }
                int[] effects = characteristics.get(CameraCharacteristics.CONTROL_AVAILABLE_EFFECTS);
                for (int effect : effects) {
                    Log.d(TAG, "Effect available: " + effect);
                }
            } catch (CameraAccessException e) {
                Log.d(TAG, "Cam access exception getting characteristics.");
            }
        }
    }
}
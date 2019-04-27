package tk.hongbo.car;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.media.ImageReader;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;

import com.google.android.things.contrib.driver.pwmservo.Servo;
import com.google.android.things.pio.Gpio;
import com.google.android.things.pio.Pwm;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Random;

import tk.hongbo.car.camera.DoorbellCamera;
import tk.hongbo.car.utils.Server;
import tk.hongbo.car.utils.ServerClient;
import tk.hongbo.publicdata.Direction;
import tk.hongbo.publicdata.MoveEntity;
import tk.hongbo.publicdata.Power;

public class MainActivity extends Activity {
    private static final String TAG = MainActivity.class.getSimpleName();

    Gpio motorEn1; //输入1针脚
    Gpio motorEn2; //输入2针脚
    Pwm motorENA; //使能端A
    Servo mServo;

    private Server server;
    private DoorbellCamera mCamera;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initGpio(); //初始化电机

        new Thread(new Runnable() {
            @Override
            public void run() {
                server = new Server();
                server.createSocket(new ServerClient.OnMessageListener() {
                    @Override
                    public void onReceive(ServerClient client, String str) {
                        showMessage(str);
                    }
                });
            }
        }).start();

        handler.post(runnable);

        initCamera();
    }

    private void showMessage(final String str) {
        new Handler(getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(MainActivity.this, str, Toast.LENGTH_SHORT).show();
            }
        });
    }

    Handler handler = new Handler();
    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            server.sendMsg("随机数字是" + new Random().nextInt(100) + "【服务端发送】");
            handler.postDelayed(runnable, 5000);
        }
    };

    /**
     * A {@link Handler} for running Camera tasks in the background.
     */
    private Handler mCameraHandler;

    /**
     * Listener for new camera images.
     */
    private ImageReader.OnImageAvailableListener mOnImageAvailableListener =
            new ImageReader.OnImageAvailableListener() {
                @Override
                public void onImageAvailable(ImageReader reader) {
                    Log.d("123", "PhotoCamera OnImageAvailableListener");

                    Image image = reader.acquireLatestImage();
                    // get image bytes
                    ByteBuffer imageBuf = image.getPlanes()[0].getBuffer();
                    final byte[] imageBytes = new byte[imageBuf.remaining()];
                    imageBuf.get(imageBytes);
                    image.close();
                    onPictureTaken(imageBytes);
                }
            };

    /**
     * Handle image processing in Firebase and Cloud Vision.
     */
    private void onPictureTaken(final byte[] imageBytes) {
        Log.d("123", "PhotoCamera onPictureTaken");
        if (imageBytes != null) {
            String imageStr = Base64.encodeToString(imageBytes, Base64.NO_WRAP | Base64.URL_SAFE);
            Log.d("123", "imageBase64:" + imageStr);

            final Bitmap[] bitmap = {BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length)};
            if (bitmap[0] != null) {
                try {
                    BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(getExternalFilesDir(null) + "pic.jpg"));// /sdcard/Android/data/com.things.thingssocket/filespic.jpg
                    bitmap[0].compress(Bitmap.CompressFormat.JPEG, 100, bos);
                    bos.flush();
                    bos.close();
                    bitmap[0].recycle();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                final Bitmap bitmaps = BitmapFactory.decodeFile(getExternalFilesDir(null) + "pic.jpg");
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        server.sendImage(bitmaps);
                    }
                }).start();
            }
        }
    }

    private HandlerThread mCameraThread;

    public void initCamera() {
        if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        DoorbellCamera.dumpFormatInfo(this);

        // Creates new handlers and associated threads for camera and networking operations.
        mCameraThread = new HandlerThread("CameraBackground");
        mCameraThread.start();
        mCameraHandler = new Handler(mCameraThread.getLooper());//在mCameraThread这个线程中创建handler对象

        // Camera code is complicated, so we've shoved it all in this closet class for you.
        mCamera = DoorbellCamera.getInstance();
        mCamera.initializeCamera(this, mCameraHandler, mOnImageAvailableListener);
    }

    /**
     * 初始化针脚1引用
     */
    private void initGpio() {
        try {
            mServo = BoardUtils.openPwnYun();
            mServo.setAngleRange(0f, 180f);
            mServo.setEnabled(true);
            mServo.setAngle(90f);

            motorEn1 = BoardUtils.openEn1();
            motorEn2 = BoardUtils.openEn2();
            motorENA = BoardUtils.openPwn();
            motorENA.setEnabled(true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        destoryGpio(); //销毁电机
    }

    /**
     * 根据控制器数据做出小车的反应
     *
     * @param entity
     */
    private void trans(MoveEntity entity) {
        try {
            transDirection(Direction.parse(entity.moveDirection));
            transPower(Power.parse(entity.movePower));
        } catch (IOException e) {
            Log.e(TAG, "Error setting the angle", e);
        }
    }

    /**
     * 控制前轮方向
     *
     * @param direction
     * @throws IOException
     */
    private void transDirection(Direction direction) throws IOException {
        if (mServo == null) {
            return;
        }
        switch (direction) {
            case DIRECTION_RUN:
                mServo.setAngle(90f);
                break;
            case DIRECTION_LEFT:
                mServo.setAngle(0f);
                break;
            case DIRECTION_RIGHT:
                mServo.setAngle(150f);
                break;
        }
    }

    /**
     * 控制马达速度
     *
     * @param power
     */
    private void transPower(Power power) {
        if (power == null) {
            return;
        }
        switch (power) {
            case POWER_STOP:
                runMotor(0);
                runMoto2(0);
                break;
            case POWER_FORWARD_HIGH:
                runMotor(1);
                runMoto2(100);
                break;
            case POWER_FORWARD_LOW:
                runMotor(1);
                runMoto2(80);
                break;
            case POWER_BACK_HIGH:
                runMotor(2);
                runMoto2(100);
                break;
            case POWER_BACK_LOW:
                runMotor(2);
                runMoto2(80);
                break;
        }
    }

    /**
     * 电机方向控制
     *
     * @param forwad
     */
    private void runMotor(int forwad) {
        if (motorEn1 == null || motorEn2 == null) {
            return;
        }
        try {
            switch (forwad) {
                case 1:
                    motorEn1.setValue(false);
                    motorEn2.setValue(true);
                    break;
                case 2:
                    motorEn1.setValue(true);
                    motorEn2.setValue(false);
                    break;
                default:
                    motorEn1.setValue(false);
                    motorEn2.setValue(false);
                    break;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 控制电机速度
     * 0-100
     *
     * @param su
     */
    private void runMoto2(int su) {
        try {
            if (motorENA == null) {
                return;
            }
            if (su == 0) {
                motorENA.setEnabled(false);
            } else {
                motorENA.setEnabled(true);
                motorENA.setPwmDutyCycle(su);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 初始化针脚2引用
     */
    private void destoryGpio() {
        try {
            if (mServo != null) {
                mServo.setEnabled(false);
                mServo.close();
            }
            if (motorEn1 != null) {
                motorEn1.close();
            }
            if (motorEn2 != null) {
                motorEn2.close();
            }
            if (motorENA != null) {
                motorENA.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            mServo = null;
            motorEn1 = null;
            motorEn2 = null;
            motorENA = null;
        }
    }
}

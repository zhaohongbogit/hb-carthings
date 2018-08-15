package tk.hongbo.ultrasonic;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import com.google.android.things.pio.Gpio;
import com.google.android.things.pio.GpioCallback;
import com.google.android.things.pio.PeripheralManager;

import java.io.IOException;

public class MainActivity extends Activity {
    private static final String TAG = MainActivity.class.getSimpleName();

    private static final String gpioTrigPinName = "BCM24"; //黄线
    private static final String gpioEchoPinName = "BCM23";

    private Gpio mTrigGpio;
    private Gpio mEchoGpio;

    Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupButton();
        startSend(); //发送信号
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        destroyButton();
    }

    private void setupButton() {
        try {
            PeripheralManager service = PeripheralManager.getInstance();
            mTrigGpio = service.openGpio(gpioTrigPinName);
            mTrigGpio.setDirection(Gpio.DIRECTION_OUT_INITIALLY_LOW); //将引脚初始化为低电平输出
            mTrigGpio.setActiveType(Gpio.ACTIVE_HIGH); //输出电压设置为高电压

            mEchoGpio = service.openGpio(gpioEchoPinName);
            mEchoGpio.setDirection(Gpio.DIRECTION_IN); //设置为输入引脚
            mEchoGpio.setActiveType(Gpio.ACTIVE_HIGH); //设置高电压为有效电压
            mEchoGpio.setEdgeTriggerType(Gpio.EDGE_BOTH); //注册状态更改监听
            mEchoGpio.registerGpioCallback(gpioCallback); //注册监听回调
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 开始发送超声波信号
     */
    private void startSend() {
        handler.post(runnable);
    }

    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            if (mTrigGpio == null) {
                return;
            }
            try {
                Log.d(TAG, "Send ultrasonic message");
                mTrigGpio.setValue(true); //输出高电平
//                Thread.sleep((long) 1);//高电平输出25US
                mTrigGpio.setValue(false); //恢复低电平
                handler.postDelayed(runnable, 1000);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    };

    private long startTime = 0; //收到一次超声波时间

    GpioCallback gpioCallback = new GpioCallback() {
        @Override
        public boolean onGpioEdge(Gpio gpio) {
            try {
                if (gpio.getValue()) {
                    //接收到高电平开始计时
                    startTime = System.currentTimeMillis();
                } else {
                    if (startTime == 0) {
                        return true;
                    }
                    long endTime = System.currentTimeMillis();
                    //接收到低电平结束计时
                    float time = (endTime - startTime) / 1000f; //间隔时间s
                    float distance = 340f * time / 2f;
                    Log.d(TAG, "本次测量，高电平：" + startTime + "，低电平：" + endTime
                            + "，距离：" + distance + "M，花费时间：" + time + "S");
                    startTime = 0;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return true;
        }
    };

    private void destroyButton() {
        if (mTrigGpio != null) {
            try {
                mTrigGpio.close();
            } catch (IOException e) {
                Log.e(TAG, "Error closing gpio", e);
            } finally {
                mTrigGpio = null;
            }
        }
        if (mEchoGpio != null) {
            try {
                mEchoGpio.close();
                mEchoGpio.unregisterGpioCallback(gpioCallback);
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                mEchoGpio = null;
            }
        }
    }

}

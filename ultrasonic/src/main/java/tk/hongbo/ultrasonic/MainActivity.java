package tk.hongbo.ultrasonic;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import com.google.android.things.pio.Gpio;
import com.google.android.things.pio.GpioCallback;
import com.google.android.things.pio.PeripheralManagerService;

import java.io.IOException;

public class MainActivity extends Activity {
    private static final String TAG = MainActivity.class.getSimpleName();

    private static final String gpioTrigPinName = "BCM23";
    private static final String gpioEchoPinName = "BCM24";

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
            PeripheralManagerService service = new PeripheralManagerService();
            mTrigGpio = service.openGpio(gpioTrigPinName);
            mTrigGpio.setDirection(Gpio.DIRECTION_OUT_INITIALLY_LOW); //将引脚初始化为低电平输出
            mTrigGpio.setActiveType(Gpio.ACTIVE_HIGH); //输出电压设置为高电压

            mEchoGpio = service.openGpio(gpioEchoPinName);
            mEchoGpio.setDirection(Gpio.DIRECTION_IN); //设置为输入引脚
            mEchoGpio.setActiveType(Gpio.ACTIVE_HIGH); //设置高电压为有效电压
            mEchoGpio.setEdgeTriggerType(Gpio.EDGE_FALLING); //注册状态更改监听
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
                Thread.sleep(1);//高电平输出25US
                mTrigGpio.setValue(false); //恢复低电平
                handler.postDelayed(runnable, 3000);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    };

    GpioCallback gpioCallback = new GpioCallback() {
        @Override
        public boolean onGpioEdge(Gpio gpio) {
            try {
                if (gpio.getValue()) {
                    //接收到高电平开始计时
                    Log.d(TAG, "===收到高电平===");
                } else {
                    //接收到低电平结束计时
                    Log.d(TAG, "===收到低电平===");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return true;
        }

        @Override
        public void onGpioError(Gpio gpio, int error) {
            Log.d(TAG, "onGpioError，" + error);
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

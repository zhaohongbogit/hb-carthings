package tk.hongbo.motor;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;

import com.google.android.things.pio.Gpio;
import com.google.android.things.pio.PeripheralManagerService;
import com.google.android.things.pio.Pwm;

import java.io.IOException;

public class MainActivity extends Activity {

    private static final String TAG = MainActivity.class.getSimpleName();

    private static final String EN1_PORT_NAME = "BCM22";
    private static final String EN2_PORT_NAME = "BCM23";
    private static final String ENA_PORT_NAME = "PWM0";

    Gpio motorEn1; //输入1针脚
    Gpio motorEn2; //输入2针脚
    Pwm motorENA; //使能端A

    Handler handler = new Handler();
    Handler handler2 = new Handler();

    private static int max_trove = 1; //最大反转次数

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initGpio();
        handler.post(runnable);
    }

    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            if (motorEn1 == null || motorEn2 == null || max_trove > 2) {
                handler2.post(runnable2);
                return;
            }
            try {
                if (motorEn1.getValue()) {
                    motorEn1.setValue(false);
                    motorEn2.setValue(true);
                } else {
                    motorEn1.setValue(true);
                    motorEn2.setValue(false);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            max_trove++;
            handler.postDelayed(runnable, 10000); //10s进行反方向转动
        }
    };

    Runnable runnable2 = new Runnable() {
        @Override
        public void run() {
            try {
                motorENA.setPwmDutyCycle(100 * Math.random());
            } catch (IOException e) {
                e.printStackTrace();
            }
            handler2.postDelayed(runnable2, 2000); //2s改变速度
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        destoryGpio();
    }

    /**
     * 初始化针脚1引用
     */
    private void initGpio() {
        PeripheralManagerService service = new PeripheralManagerService();
        try {
            motorEn1 = service.openGpio(EN1_PORT_NAME);
            motorEn1.setDirection(Gpio.DIRECTION_OUT_INITIALLY_LOW);
            motorEn1.setActiveType(Gpio.ACTIVE_HIGH);
            motorEn1.setValue(true);

            motorEn2 = service.openGpio(EN2_PORT_NAME);
            motorEn2.setDirection(Gpio.DIRECTION_OUT_INITIALLY_LOW);
            motorEn2.setActiveType(Gpio.ACTIVE_HIGH);
            motorEn2.setValue(false);

            motorENA = service.openPwm(ENA_PORT_NAME);
            motorENA.setPwmFrequencyHz(50);
            motorENA.setPwmDutyCycle(100);
            motorENA.setEnabled(true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 初始化针脚2引用
     */
    private void destoryGpio() {
        try {
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
            motorEn1 = null;
            motorEn2 = null;
            motorENA = null;
        }
    }
}

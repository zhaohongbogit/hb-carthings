package tk.hongbo.motor;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;

import com.google.android.things.pio.Gpio;
import com.google.android.things.pio.PeripheralManagerService;

import java.io.IOException;

public class MainActivity extends Activity {

    private static final String TAG = MainActivity.class.getSimpleName();
    private static final String EN1_PORT_NAME = "BCM22";
    private static final String EN2_PORT_NAME = "BCM23";

    Gpio motorEn1; //输入1针脚
    Gpio motorEn2; //输入2针脚

    Handler handler = new Handler();

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
            if (motorEn1 == null || motorEn2 == null || max_trove > 5) {
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
            handler.postDelayed(runnable, 3000); //10s进行反方向转动
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

            motorEn2 = service.openGpio(EN2_PORT_NAME);
            motorEn2.setDirection(Gpio.DIRECTION_OUT_INITIALLY_LOW);
            motorEn2.setActiveType(Gpio.ACTIVE_HIGH);
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
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            motorEn1 = null;
            motorEn2 = null;
        }
    }
}

package com.hongbo.car;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

import com.google.android.things.contrib.driver.pwmservo.Servo;
import com.google.android.things.pio.Gpio;
import com.google.android.things.pio.PeripheralManagerService;
import com.google.android.things.pio.Pwm;
import com.wilddog.client.ChildEventListener;
import com.wilddog.client.DataSnapshot;
import com.wilddog.client.SyncError;
import com.wilddog.client.SyncReference;
import com.wilddog.client.WilddogSync;

import java.io.IOException;

import tk.hongbo.publicdata.Constans;
import tk.hongbo.publicdata.Direction;
import tk.hongbo.publicdata.MoveEntity;
import tk.hongbo.publicdata.Power;

public class MainActivity extends Activity {
    private static final String TAG = MainActivity.class.getSimpleName();

    //转向舵机控制
    private static final String PWM_BUS = "PWM1";

    //点击控制部分
    private static final String EN1_PORT_NAME = "BCM17";
    private static final String EN2_PORT_NAME = "BCM27";
    private static final String ENA_PORT_NAME = "PWM0";

    Gpio motorEn1; //输入1针脚
    Gpio motorEn2; //输入2针脚
    Pwm motorENA; //使能端A

    private Servo mServo;
    SyncReference mWilddogRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupServo();
        initGpio(); //初始化电机
        mWilddogRef = WilddogSync.getInstance().getReference().child(Constans.WILDDOG_REF);
        mWilddogRef.addChildEventListener(listener);
    }

    ChildEventListener listener = new ChildEventListener() {
        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
            Log.d(TAG, "onChildAdded ，" + s);
            onMessage(dataSnapshot);
        }

        @Override
        public void onChildChanged(DataSnapshot dataSnapshot, String s) {
            Log.d(TAG, "onChildChanged ，" + s);
            onMessage(dataSnapshot);
        }

        @Override
        public void onChildRemoved(DataSnapshot dataSnapshot) {
            Log.d(TAG, "onChildRemoved");
        }

        @Override
        public void onChildMoved(DataSnapshot dataSnapshot, String s) {
            Log.d(TAG, "onChildMoved ，" + s);
        }

        @Override
        public void onCancelled(SyncError syncError) {
            Log.d(TAG, "onCancelled ，" + syncError.getDetails());
        }
    };

    public void onMessage(DataSnapshot dataSnapshot) {
        if (dataSnapshot.exists() && dataSnapshot.getKey().equals(MoveEntity.WILDDOG_REF_MOVE)) {
            MoveEntity entity = MoveEntity.parse(dataSnapshot.getValue());
            trans(entity);
        }
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
            motorEn1.setValue(false);

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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        destroyServo();
        destoryGpio(); //销毁电机
        mWilddogRef.startAt().removeEventListener(listener);
    }

    /**
     * 根据控制器数据做出小车的反应
     *
     * @param entity
     */
    private void trans(MoveEntity entity) {
        try {
            transDirection(entity.moveDirection);
            transPower(entity.movePower);
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

    private void setupServo() {
        try {
            mServo = new Servo(PWM_BUS);
            mServo.setAngleRange(0f, 180f);
            mServo.setEnabled(true);
            mServo.setAngle(90f);
        } catch (IOException e) {
            Log.e(TAG, "Error creating Servo", e);
        }
    }

    private void destroyServo() {
        if (mServo != null) {
            try {
                mServo.setEnabled(false);
                mServo.close();
            } catch (IOException e) {
                Log.e(TAG, "Error closing Servo");
            } finally {
                mServo = null;
            }
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

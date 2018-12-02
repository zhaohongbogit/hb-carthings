package tk.hongbo.car;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

import com.google.android.things.contrib.driver.pwmservo.Servo;
import com.google.android.things.pio.Gpio;
import com.google.android.things.pio.Pwm;
import com.wilddog.client.ChildEventListener;
import com.wilddog.client.DataSnapshot;
import com.wilddog.client.Query;
import com.wilddog.client.SyncError;
import com.wilddog.client.SyncReference;
import com.wilddog.client.ValueEventListener;
import com.wilddog.client.WilddogSync;

import java.io.IOException;
import java.util.Map;

import tk.hongbo.publicdata.Constans;
import tk.hongbo.publicdata.Direction;
import tk.hongbo.publicdata.MoveEntity;
import tk.hongbo.publicdata.Power;

public class MainActivity extends Activity {
    private static final String TAG = MainActivity.class.getSimpleName();

    Gpio motorEn1; //输入1针脚
    Gpio motorEn2; //输入2针脚
    Pwm motorENA; //使能端A
    Servo mServo;

    SyncReference mWilddogRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initGpio(); //初始化电机

        //监听数据裱花
        mWilddogRef = WilddogSync.getInstance().getReference();
        mWilddogRef.addValueEventListener(eventListener);
        Query query = mWilddogRef.child(Constans.WILDDOG_REF).startAt();
        query.addChildEventListener(listener);
    }

    ValueEventListener eventListener = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            Log.d(TAG, "onDataChange");
        }

        @Override
        public void onCancelled(SyncError syncError) {
            Log.d(TAG, "onCancelled");
        }
    };

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
            MoveEntity entity = MoveEntity.parseMap((Map<String, String>) dataSnapshot.getValue());
            trans(entity);
        }
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
        mWilddogRef.startAt().removeEventListener(listener);
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
        if(power==null){
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
                motorENA. setPwmDutyCycle(su);
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

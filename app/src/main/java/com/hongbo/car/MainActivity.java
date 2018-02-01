package com.hongbo.car;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

import com.google.android.things.contrib.driver.pwmservo.Servo;
import com.wilddog.client.ChildEventListener;
import com.wilddog.client.DataSnapshot;
import com.wilddog.client.Query;
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

    private static final String PWM_BUS = "PWM0";
    private Servo mServo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupServo();
        SyncReference mWilddogRef = WilddogSync.getInstance().getReference().child(Constans.WILDDOG_REF);
        Query query = mWilddogRef.startAt();
        query.addChildEventListener(listener);
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        destroyServo();
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
        switch (direction) {
            case DIRECTION_RUN:
                mServo.setAngle(90);
                break;
            case DIRECTION_LEFT:
                mServo.setAngle(40);
                break;
            case DIRECTION_RIGHT:
                mServo.setAngle(120);
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
                break;
            case POWER_FORWARD_HIGH:
                break;
            case POWER_FORWARD_LOW:
                break;
            case POWER_BACK_HIGH:
                break;
            case POWER_BACK_LOW:
                break;
        }
    }

    private void setupServo() {
        try {
            mServo = new Servo(PWM_BUS);
            mServo.setAngleRange(0f, 180f);
            mServo.setEnabled(true);
        } catch (IOException e) {
            Log.e(TAG, "Error creating Servo", e);
        }
    }

    private void destroyServo() {
        if (mServo != null) {
            try {
                mServo.close();
            } catch (IOException e) {
                Log.e(TAG, "Error closing Servo");
            } finally {
                mServo = null;
            }
        }
    }
}

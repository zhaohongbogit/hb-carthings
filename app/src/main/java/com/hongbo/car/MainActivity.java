package com.hongbo.car;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

import com.google.android.things.contrib.driver.pwmservo.Servo;
import com.hongbo.car.entity.MoveEntity;
import com.wilddog.client.ChildEventListener;
import com.wilddog.client.DataSnapshot;
import com.wilddog.client.Query;
import com.wilddog.client.SyncError;
import com.wilddog.client.SyncReference;
import com.wilddog.client.WilddogSync;

import java.io.IOException;

public class MainActivity extends Activity {
    private static final String TAG = MainActivity.class.getSimpleName();

    private static final String PWM_BUS = "PWM0";
    private Servo mServo;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupServo();
        trans(new MoveEntity(6));
        SyncReference mWilddogRef = WilddogSync.getInstance().getReference().child("move");
        Query query = mWilddogRef.limitToLast(1);
        query.addChildEventListener(listener);
    }

    ChildEventListener listener = new ChildEventListener() {
        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
            Log.d(TAG, "onChildAdded ，" + s);
            if (dataSnapshot.exists()) {
                MoveEntity entity = MoveEntity.parse(dataSnapshot.getValue());
                Log.d(TAG, "MoveEntiry," + entity.moveCode);
                trans(entity);
            }
        }

        @Override
        public void onChildChanged(DataSnapshot dataSnapshot, String s) {
            Log.d(TAG, "onChildChanged ，" + s);
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        destroyServo();
    }

    private void trans(MoveEntity entity) {
        MoveEntity.MoveType moveType = MoveEntity.MoveType.getType(entity.moveCode);
        try {
            switch (moveType) {
                case MOVE_TYPE_LEFT:
                    mServo.setAngle(40);
                    break;
                case MOVE_TYPE_RIGHT:
                    mServo.setAngle(120);
                    break;
                case MOVE_TYPE_RUN:
                    mServo.setAngle(90);
                    break;
            }
        } catch (IOException e) {
            Log.e(TAG, "Error setting the angle", e);
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

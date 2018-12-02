package tk.hongbo.wilddog;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;

import com.wilddog.client.ChildEventListener;
import com.wilddog.client.DataSnapshot;
import com.wilddog.client.Query;
import com.wilddog.client.SyncError;
import com.wilddog.client.SyncReference;
import com.wilddog.client.WilddogSync;

import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import tk.hongbo.car.widget.RockerView;
import tk.hongbo.publicdata.Constans;
import tk.hongbo.publicdata.Direction;
import tk.hongbo.publicdata.MoveEntity;
import tk.hongbo.publicdata.Power;

import static tk.hongbo.car.widget.RockerView.Direction.DIRECTION_CENTER;

public class MainActivity extends AppCompatActivity {

    public static final String TAG = MainActivity.class.getSimpleName();

    @BindView(R.id.msg)
    TextView msg;
    @BindView(R.id.rockerView)
    RockerView rockerView;
    @BindView(R.id.floatingActionButton)
    FloatingActionButton floatingActionButton;

    private SyncReference mWilddogRef; //野狗数据仓库

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        //野狗身份认证
//        WilddogAuth.getInstance().signInAnonymously().addOnCompleteListener(new OnCompleteListener<AuthResult>() {
//            @Override
//            public void onComplete(Task<AuthResult> var1) {
//                if (var1.isSuccessful()) {
//                    Log.d("success", "Login success!");
//                    Log.d("Anonymous", String.valueOf(var1.getResult().getWilddogUser().isAnonymous()) +
//                            "，UID：" + String.valueOf(var1.getResult().getWilddogUser().getUid()));
//                } else {
//                    Log.d("failure", "reason:" + var1.getException());
//                }
//            }
//        });

        //摇杆监听
        rockerView.setCallBackMode(RockerView.CallBackMode.CALL_BACK_MODE_STATE_CHANGE);
        rockerView.setOnShakeListener(RockerView.DirectionMode.DIRECTION_8, new RockerView.OnShakeListener() {
            @Override
            public void onStart() {
//                Log.d(TAG, "RockerView onStart");
            }

            @Override
            public void direction(RockerView.Direction direction) {
                shendMove(direction);
            }

            @Override
            public void onFinish() {
//                Log.d(TAG, "RockerView onFinish");
                shendMove(DIRECTION_CENTER);
            }
        });

        //数据变化监听
        mWilddogRef = WilddogSync.getInstance().getReference().child(Constans.WILDDOG_REF);
        Query query = mWilddogRef.startAt();
        query.addChildEventListener(listener);

        floatingActionButton.setOnClickListener(v -> {
            mWilddogRef.child(MoveEntity.WILDDOG_REF_MOVE).removeValue();
        });
    }

    /**
     * 相应角度
     *
     * @param direction
     */
    private void shendMove(RockerView.Direction direction) {
        switch (direction) {
            case DIRECTION_LEFT:
                Log.d(TAG, "DIRECTION_LEFT");
                sendMsg(Direction.DIRECTION_LEFT, Power.POWER_STOP);
                break;
            case DIRECTION_RIGHT:
                Log.d(TAG, "DIRECTION_RIGHT");
                sendMsg(Direction.DIRECTION_RIGHT, Power.POWER_STOP);
                break;
            case DIRECTION_UP:
                Log.d(TAG, "DIRECTION_UP");
                sendMsg(Direction.DIRECTION_RUN, Power.POWER_FORWARD_HIGH);
                break;
            case DIRECTION_DOWN:
                Log.d(TAG, "DIRECTION_DOWN");
                sendMsg(Direction.DIRECTION_RUN, Power.POWER_BACK_HIGH);
                break;
            case DIRECTION_UP_LEFT:
                Log.d(TAG, "DIRECTION_UP_LEFT");
                sendMsg(Direction.DIRECTION_LEFT, Power.POWER_FORWARD_LOW);
                break;
            case DIRECTION_UP_RIGHT:
                Log.d(TAG, "DIRECTION_UP_RIGHT");
                sendMsg(Direction.DIRECTION_RIGHT, Power.POWER_FORWARD_LOW);
                break;
            case DIRECTION_DOWN_LEFT:
                Log.d(TAG, "DIRECTION_DOWN_LEFT");
                sendMsg(Direction.DIRECTION_LEFT, Power.POWER_BACK_LOW);
                break;
            case DIRECTION_DOWN_RIGHT:
                Log.d(TAG, "DIRECTION_DOWN_RIGHT");
                sendMsg(Direction.DIRECTION_RIGHT, Power.POWER_BACK_LOW);
                break;
            case DIRECTION_CENTER:
                Log.d(TAG, "DIRECTION_CENTER");
                sendMsg(Direction.DIRECTION_RUN, Power.POWER_STOP);
                break;
        }
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
            MoveEntity entity = MoveEntity.parseMap((Map<String, String>) dataSnapshot.getValue());
            msg.setText("moveDirection：" + entity.moveDirection + "\r\nmovePower：" + entity.movePower);
        }
    }

    /**
     * 做出响应
     *
     * @param direction
     * @param power
     */
    private void sendMsg(Direction direction, Power power) {
        MoveEntity entity = new MoveEntity();
        entity.moveDirection = direction.direction;
        entity.movePower = power.power;
        pushMove(entity);
    }

    /**
     * 推送操作数据到云端
     *
     * @param moveEntity
     */
    private void pushMove(MoveEntity moveEntity) {
        if (moveEntity == null) {
            return;
        }
        mWilddogRef.child(MoveEntity.WILDDOG_REF_MOVE).setValue(moveEntity);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mWilddogRef.startAt().removeEventListener(listener);
    }
}

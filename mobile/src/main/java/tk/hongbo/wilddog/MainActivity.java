package tk.hongbo.wilddog;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.wilddog.client.ChildEventListener;
import com.wilddog.client.DataSnapshot;
import com.wilddog.client.Query;
import com.wilddog.client.SyncError;
import com.wilddog.client.SyncReference;
import com.wilddog.client.WilddogSync;
import com.wilddog.wilddogauth.WilddogAuth;
import com.wilddog.wilddogauth.core.Task;
import com.wilddog.wilddogauth.core.listener.OnCompleteListener;
import com.wilddog.wilddogauth.core.result.AuthResult;
import com.wilddog.wilddogauth.model.WilddogUser;

import butterknife.BindView;
import butterknife.ButterKnife;
import tk.hongbo.wilddog.entity.MoveEntity;

public class MainActivity extends AppCompatActivity {

    public static final String TAG = MainActivity.class.getSimpleName();

    @BindView(R.id.msg)
    TextView msg;

    private SyncReference mWilddogRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        WilddogAuth.getInstance().signInAnonymously().addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(Task<AuthResult> var1) {
                if (var1.isSuccessful()) {
                    Log.d("success", "Login success!");
                    Log.d("Anonymous", String.valueOf(var1.getResult().getWilddogUser().isAnonymous()) +
                            "，UID：" + String.valueOf(var1.getResult().getWilddogUser().getUid()));
                } else {
                    Log.d("failure", "reason:" + var1.getException());
                }
            }
        });

        mWilddogRef = WilddogSync.getInstance().getReference().child("move");
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
                msg.setText("新动作：" + entity.moveCode);
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

    /**
     * 推送操作数据到云端
     *
     * @param moveType
     */
    private void pushMove(MoveEntity.MoveType moveType) {
        if (moveType == null) {
            return;
        }
        MoveEntity entity = new MoveEntity();
        entity.moveCode = moveType.getCode();
        Log.d(TAG, "The random number is " + entity.moveCode);
        mWilddogRef.push().setValue(entity);
    }

    /**
     * 动力前进
     *
     * @param view
     */
    public void activityForwad(View view) {
        pushMove(MoveEntity.MoveType.MOVE_TYPE_FORWAD);
    }

    /**
     * 动力后退
     *
     * @param view
     */
    public void activityBack(View view) {
        pushMove(MoveEntity.MoveType.MOVE_TYPE_BACK);
    }

    /**
     * 动力停止
     *
     * @param view
     */
    public void activityStop(View view) {
        pushMove(MoveEntity.MoveType.MOVE_TYPE_STOP);
    }

    /**
     * 方向左转
     *
     * @param view
     */
    public void activityLeft(View view) {
        pushMove(MoveEntity.MoveType.MOVE_TYPE_LEFT);
    }

    /**
     * 方向右转
     *
     * @param view
     */
    public void activityRight(View view) {
        pushMove(MoveEntity.MoveType.MOVE_TYPE_RIGHT);
    }

    /**
     * 方向直行
     *
     * @param view
     */
    public void activityRun(View view) {
        pushMove(MoveEntity.MoveType.MOVE_TYPE_RUN);
    }
}

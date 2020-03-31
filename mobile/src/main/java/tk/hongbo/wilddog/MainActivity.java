package tk.hongbo.wilddog;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.Toast;

import com.google.gson.Gson;

import butterknife.BindView;
import butterknife.ButterKnife;
import tk.hongbo.car.widget.RockerView;
import tk.hongbo.publicdata.MoveEntity;
import tk.hongbo.wilddog.utils.Client;

import static tk.hongbo.car.widget.RockerView.Direction.DIRECTION_CENTER;

public class MainActivity extends AppCompatActivity {

    public static final String TAG = MainActivity.class.getSimpleName();

    @BindView(R.id.rockerView)
    RockerView rockerView;
    @BindView(R.id.imageView)
    SurfaceView bgImageView;

    Client client;
    SurfaceHolder holder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        //摇杆监听
        rockerView.setCallBackMode(RockerView.CallBackMode.CALL_BACK_MODE_STATE_CHANGE);
        rockerView.setOnShakeListener(RockerView.DirectionMode.DIRECTION_8, new RockerView.OnShakeListener() {
            @Override
            public void onStart() {
                Log.d(TAG, "RockerView onStart");
            }

            @Override
            public void direction(RockerView.Direction direction) {
                shendMove(direction);
            }

            @Override
            public void onFinish() {
                Log.d(TAG, "RockerView onFinish");
                shendMove(DIRECTION_CENTER);
            }
        });

        holder = bgImageView.getHolder();

        new Thread(() -> {
            client = new Client("192.168.188.8", 12345, new Client.OnMessageListener() {
                @Override
                public void onMessage(String str) {
                    showMessage(str);
                }

                @Override
                public void onImage(Bitmap bitmap) {
                    drawImg(bitmap);
                }
            });
            client.start();
        }).start();
    }

    protected void onDraw(Canvas canvas, Bitmap bitmap) {
        canvas.drawColor(Color.BLACK);
        canvas.drawBitmap(bitmap, 0, 0, null);
    }

    private void drawImg(Bitmap bitmap) {
        Canvas canvas = null;
        try {
            canvas = holder.lockCanvas();
            onDraw(canvas, bitmap);
        } finally {
            if (canvas != null) {
                holder.unlockCanvasAndPost(canvas);
            }
        }
    }

    private void showMessage(final String str) {
        new Handler(getMainLooper()).post(() -> Toast.makeText(MainActivity.this, str, Toast.LENGTH_SHORT).show());
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
                pushMove(-1, 0);
                break;
            case DIRECTION_RIGHT:
                Log.d(TAG, "DIRECTION_RIGHT");
                pushMove(1, 0);
                break;
            case DIRECTION_UP:
                Log.d(TAG, "DIRECTION_UP");
                pushMove(0, 1);
                break;
            case DIRECTION_DOWN:
                Log.d(TAG, "DIRECTION_DOWN");
                pushMove(0, 2);
                break;
            case DIRECTION_UP_LEFT:
                Log.d(TAG, "DIRECTION_UP_LEFT");
                pushMove(-1, 11);
                break;
            case DIRECTION_UP_RIGHT:
                Log.d(TAG, "DIRECTION_UP_RIGHT");
                pushMove(1, 11);
                break;
            case DIRECTION_DOWN_LEFT:
                Log.d(TAG, "DIRECTION_DOWN_LEFT");
                pushMove(-1, 22);
                break;
            case DIRECTION_DOWN_RIGHT:
                Log.d(TAG, "DIRECTION_DOWN_RIGHT");
                pushMove(1, 22);
                break;
            case DIRECTION_CENTER:
                Log.d(TAG, "DIRECTION_CENTER");
                pushMove(0, 0);
                break;
        }
    }

    /**
     * 推送操作数据到云端
     *
     * @param direction
     * @param power
     */
    private void pushMove(int direction, int power) {
        client.sendMessage(new Gson().toJson(new MoveEntity(direction, power)));
    }
}

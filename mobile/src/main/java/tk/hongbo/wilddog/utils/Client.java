package tk.hongbo.wilddog.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Base64;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class Client extends Thread {

    private String ip;
    private int port;
    private OnMessageListener onMessageListener;

    private Socket socket;
    private OutputStream out;

    public Client(String ip, int port, OnMessageListener onMessageListener) {
        this.ip = ip;
        this.port = port;
        this.onMessageListener = onMessageListener;
    }

    @Override
    public void run() {
        try {
            socket = new Socket(ip, port);
            if (socket.isConnected()) {
                InputStream inputStream = socket.getInputStream();
                out = socket.getOutputStream();
                while (true) {
                    byte[] srcType = new byte[4];
                    inputStream.read(srcType);
                    int type = BytesUtils.bytes2Int(srcType);

                    byte[] src = new byte[4];
                    inputStream.read(src);
                    int len = BytesUtils.bytes2Int(src);

                    byte[] srcData = new byte[len];
                    inputStream.read(srcData);

                    if (type == 1) {
                        String str = new String(srcData);
                        if (onMessageListener != null && !TextUtils.isEmpty(str)) {
                            onMessageListener.onMessage(str);
                        }
                    } else if (type == 2) {

                        Base64.decode(new String(srcData),Base64.NO_WRAP | Base64.URL_SAFE);

                        final Bitmap bitmap = BitmapFactory.decodeByteArray(srcData, 0, len);
                        if (onMessageListener != null) {
                            new Handler(Looper.getMainLooper()).post(new Runnable() {
                                @Override
                                public void run() {
                                    final Bitmap bitmap1 = Bitmap.createBitmap(bitmap);
                                    onMessageListener.onImage(bitmap1);
                                }
                            });

                        }
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendMessage(final String msg) {
        new Thread(() -> {
            try {
                byte[] bytes = msg.getBytes();
                byte[] lenByte = BytesUtils.int2ByteArray(bytes.length);
                if (out != null) {
                    out.write(BytesUtils.int2ByteArray(1));
                    out.write(lenByte);
                    out.write(bytes);
                    out.flush();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    public interface OnMessageListener {
        void onMessage(String str);

        void onImage(Bitmap bitmap);
    }
}

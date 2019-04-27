package tk.hongbo.car.utils;

import android.graphics.Bitmap;
import android.graphics.Matrix;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;

public class ServerClient extends Thread {

    private Socket socket;
    private OutputStream out;
    private OnMessageListener listener;

    public ServerClient(Socket socket, OnMessageListener listener) {
        this.socket = socket;
        this.listener = listener;
    }

    @Override
    public void run() {
        while (true) {
            try {
                InputStream inputStream = socket.getInputStream();
                out = socket.getOutputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                String str = reader.readLine();
                if (listener != null) {
                    listener.onReceive(this, str);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void sendMessage(final String msg) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    byte[] bytes = msg.getBytes();
                    byte[] lenByte = BytesUtils.int2ByteArray(bytes.length);
                    out.write(BytesUtils.int2ByteArray(1));
                    out.write(lenByte);
                    out.write(bytes);
                    out.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public void sendImage(final Bitmap bitmap) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    byte[] outdata = transImage(bitmap, 640, 480);
                    if (outdata == null || outdata.length == 0) {
                        return;
                    }
                    int datalen = outdata.length;
                    out.write(BytesUtils.int2ByteArray(2));
                    out.write(BytesUtils.int2ByteArray(datalen));
                    out.write(outdata);
                    out.flush();
                    if (!bitmap.isRecycled()) {
                        bitmap.recycle();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    /**
     * 数据转换，将bitmap转换为byte
     */
    private byte[] transImage(Bitmap bitmap, int width, int height) {
        try {
            int bitmapWidth = bitmap.getWidth();
            int bitmapHeight = bitmap.getHeight();
            //缩放图片的尺寸
            float scaleWidth = (float) width / bitmapWidth;
            float scaleHeight = (float) height / bitmapHeight;
            Matrix matrix = new Matrix();
            matrix.postScale(scaleWidth, scaleHeight);
            //产生缩放后的Bitmap对象
            Bitmap resizeBitemp = Bitmap.createBitmap(bitmap, 0, 0, bitmapWidth, bitmapHeight, matrix, false);
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            resizeBitemp.compress(Bitmap.CompressFormat.JPEG, 85, outputStream);
            byte[] byteArray = outputStream.toByteArray();
            outputStream.close();
            if (!bitmap.isRecycled()) {
                bitmap.recycle();
            }
            if (!resizeBitemp.isRecycled()) {
                resizeBitemp.recycle();
            }
            return byteArray;

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public interface OnMessageListener {
        void onReceive(ServerClient client, String str);
    }
}
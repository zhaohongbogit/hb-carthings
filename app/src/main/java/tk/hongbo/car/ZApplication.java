package tk.hongbo.car;


import android.app.Application;

import com.wilddog.wilddogcore.WilddogApp;
import com.wilddog.wilddogcore.WilddogOptions;

/**
 * Created by HONGBO on 2018/1/31 18:27.
 */
public class ZApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        WilddogOptions wilddogOptions = new WilddogOptions.Builder().setSyncUrl("https://wd1131873415xbudnd.wilddogio.com").build();
        WilddogApp.initializeApp(this, wilddogOptions);
    }
}

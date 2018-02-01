package tk.hongbo.publicdata;

import com.google.gson.annotations.SerializedName;

/**
 * Created by HONGBO on 2018/2/1 16:33.
 */
public enum Power {

    POWER_STOP(1000),
    POWER_FORWARD_HIGH(1011),
    POWER_FORWARD_LOW(1001),
    POWER_BACK_HIGH(1022),
    POWER_BACK_LOW(1002);

    private Integer power;

    /*
    动力
    1. 前进高速
    2. 前进慢速
    3. 后退高速
    4. 后退慢速
    5. 动力停止
    */
    Power(Integer power) {
        this.power = power;
    }
}

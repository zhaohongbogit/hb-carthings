package tk.hongbo.publicdata;

import com.google.gson.annotations.SerializedName;

/**
 * Created by HONGBO on 2018/2/1 16:33.
 */
public enum Power {

    POWER_STOP("POWER_STOP"),
    POWER_FORWARD_HIGH("POWER_FORWARD_HIGH"),
    POWER_FORWARD_LOW("POWER_FORWARD_LOW"),
    POWER_BACK_HIGH("POWER_BACK_HIGH"),
    POWER_BACK_LOW("POWER_BACK_LOW");

    public String power;

    public static Power parse(String code){
        switch (code){
            case "POWER_STOP":
                return POWER_STOP;
            case "POWER_FORWARD_HIGH":
                return POWER_FORWARD_HIGH;
            case "POWER_FORWARD_LOW":
                return POWER_FORWARD_LOW;
            case "POWER_BACK_HIGH":
                return POWER_BACK_HIGH;
            case "POWER_BACK_LOW":
                return POWER_BACK_LOW;
        }
        return null;
    }

    /*
    动力
    1. 前进高速
    2. 前进慢速
    3. 后退高速
    4. 后退慢速
    5. 动力停止
    */
    Power(String power) {
        this.power = power;
    }
}

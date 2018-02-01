package tk.hongbo.publicdata;

import com.google.gson.Gson;

/**
 * 摇杆方向
 * Created by HONGBO on 2018/1/31 18:34.
 */
public class MoveEntity {

    public Power movePower;
    public Direction moveDirection;

    public static MoveEntity parse(Object jsonObj) {
        return new Gson().fromJson(jsonObj.toString(), MoveEntity.class);
    }
}

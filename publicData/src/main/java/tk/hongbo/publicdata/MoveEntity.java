package tk.hongbo.publicdata;

import java.util.Map;

/**
 * 摇杆方向
 * Created by HONGBO on 2018/1/31 18:34.
 */
public class MoveEntity {

    private int power; //0:停止 1：高速前进 11：低速前进 2：高速后退 22：低速后退
    private int direction; //0:正前方 -1：左方向 1：右方向

    public MoveEntity(int power, int direction) {
        this.power = power;
        this.direction = direction;
    }

    public int getPower() {
        return power;
    }

    public void setPower(int power) {
        this.power = power;
    }

    public int getDirection() {
        return direction;
    }

    public void setDirection(int direction) {
        this.direction = direction;
    }
}

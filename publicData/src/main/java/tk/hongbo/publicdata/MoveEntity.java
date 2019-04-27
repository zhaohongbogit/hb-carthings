package tk.hongbo.publicdata;

import java.util.Map;

/**
 * 摇杆方向
 * Created by HONGBO on 2018/1/31 18:34.
 */
public class MoveEntity {

    private int direction; //0:正前方 -1：左方向 1：右方向
    private int power; //0:停止 1：高速前进 11：低速前进 2：高速后退 22：低速后退

    public MoveEntity(int direction, int power) {
        this.direction = direction;
        this.power = power;
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

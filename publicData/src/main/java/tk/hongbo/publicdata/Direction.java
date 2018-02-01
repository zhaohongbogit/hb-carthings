package tk.hongbo.publicdata;

/**
 * 小车方向
 */
public enum Direction {

    DIRECTION_RUN(2000),
    DIRECTION_LEFT(2001),
    DIRECTION_RIGHT(2002);

    private Integer direction;

    /*
    方向
    1. 左转极限
    2. 右转极限
    3. 正方向
     */
    Direction(Integer direction) {
        this.direction = direction;
    }
}

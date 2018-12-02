package tk.hongbo.publicdata;

/**
 * 小车方向
 */
public enum Direction {

    DIRECTION_RUN("DIRECTION_RUN"),
    DIRECTION_LEFT("DIRECTION_LEFT"),
    DIRECTION_RIGHT("DIRECTION_RIGHT");

    public String direction;

    public static Direction parse(String direction) {
        switch (direction) {
            case "DIRECTION_RUN":
                return DIRECTION_RUN;
            case "DIRECTION_LEFT":
                return DIRECTION_LEFT;
            case "DIRECTION_RIGHT":
                return DIRECTION_RIGHT;
            default:
                return null;
        }
    }

    /*
    方向
    1. 左转极限
    2. 右转极限
    3. 正方向
     */
    Direction(String direction) {
        this.direction = direction;
    }
}

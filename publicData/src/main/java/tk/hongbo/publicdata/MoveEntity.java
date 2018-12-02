package tk.hongbo.publicdata;

import java.util.Map;

/**
 * 摇杆方向
 * Created by HONGBO on 2018/1/31 18:34.
 */
public class MoveEntity {

    public static final String WILDDOG_REF_MOVE = "moveEntity";

    public String movePower;
    public String moveDirection;

    public static MoveEntity parseMap(Map<String, String> map) {
        MoveEntity moveEntity = new MoveEntity();
        moveEntity.movePower = map.get("movePower");
        moveEntity.moveDirection = map.get("moveDirection");
        return moveEntity;
    }
}

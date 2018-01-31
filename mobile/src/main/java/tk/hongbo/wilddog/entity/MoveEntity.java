package tk.hongbo.wilddog.entity;

import com.google.gson.Gson;

/**
 * Created by HONGBO on 2018/1/31 18:34.
 */
public class MoveEntity {
    public Integer moveCode; //动作编号 1:前进 2：后退 3：左转弯 4：后转弯 5：停止 6：前进

    public static MoveEntity parse(Object jsonObj) {
        return new Gson().fromJson(jsonObj.toString(), MoveEntity.class);
    }

    public enum MoveType {

        MOVE_TYPE_FORWAD(1),
        MOVE_TYPE_BACK(2),
        MOVE_TYPE_LEFT(3),
        MOVE_TYPE_RIGHT(4),
        MOVE_TYPE_STOP(5),
        MOVE_TYPE_RUN(6);

        int code;

        MoveType(int code) {
            this.code = code;
        }

        public int getCode() {
            return code;
        }

        public MoveType getType(int code) {
            switch (code) {
                case 1:
                    return MOVE_TYPE_FORWAD;
                case 2:
                    return MOVE_TYPE_BACK;
                case 3:
                    return MOVE_TYPE_LEFT;
                case 4:
                    return MOVE_TYPE_RIGHT;
                case 5:
                    return MOVE_TYPE_STOP;
                case 6:
                    return MOVE_TYPE_RUN;
                default:
                    return MOVE_TYPE_STOP;
            }
        }
    }
}

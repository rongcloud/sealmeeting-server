package cn.rongcloud.im.message;

import cn.rongcloud.im.BaseMessage;
import lombok.Getter;
import lombok.Setter;

/**
 * Created by weiqinxiao on 2019/3/13.
 */
public class TurnPageMessage extends BaseMessage {

    private @Setter @Getter String whiteboardId;

    private @Setter @Getter String userId;

    private @Setter @Getter int curPg;

    public TurnPageMessage(String whiteboardId, String userId, int curPg) {
        this.whiteboardId = whiteboardId;
        this.userId = userId;
        this.curPg = curPg;
    }

    @Override
    public String getObjectName() {
        return "SC:WBTPMsg";
    }
}

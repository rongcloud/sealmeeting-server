package cn.rongcloud.im.message;

import cn.rongcloud.im.BaseMessage;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.annotation.JSONField;
import lombok.Getter;
import lombok.Setter;

/**
 * Created by weiqinxiao on 2019/3/8.
 */
public class WhiteboardMessage extends BaseMessage {
    public final static int Create = 1, Delete = 2;

    private @Setter @Getter int action;

    private @Setter @Getter String whiteboardId;
    private @Setter @Getter String whiteboardName;

    public WhiteboardMessage(int action) {
        this.action = action;
    }

    @Override
    public String getObjectName() {
        return "SC:WBMsg";
    }
}

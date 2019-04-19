package cn.rongcloud.im.message;

import cn.rongcloud.im.BaseMessage;
import lombok.Getter;
import lombok.Setter;

/**
 * Created by weiqinxiao on 2019/3/6.
 */
public class AdminTransferMessage extends BaseMessage {

    private @Getter @Setter String opUserId;
    private @Getter @Setter String toUserId;

    @Override
    public String getObjectName() {
        return "SC:ATMsg";
    }
}

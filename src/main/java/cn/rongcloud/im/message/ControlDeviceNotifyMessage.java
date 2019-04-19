package cn.rongcloud.im.message;

import cn.rongcloud.im.BaseMessage;
import lombok.Getter;
import lombok.Setter;

/**
 * Created by weiqinxiao on 2019/3/19.
 */
public class ControlDeviceNotifyMessage extends BaseMessage {

    private  @Getter @Setter int action;
    private  @Getter @Setter String ticket;
    private @Getter @Setter int type;

    private @Getter @Setter String opUserId;
    private @Getter @Setter String opUserName;

    public ControlDeviceNotifyMessage(int action) {
        this.action = action;
    }

    @Override
    public String getObjectName() {
        return "SC:CDNMsg";
    }
}

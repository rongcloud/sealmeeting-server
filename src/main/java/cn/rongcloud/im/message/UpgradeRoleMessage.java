package cn.rongcloud.im.message;

import cn.rongcloud.im.BaseMessage;
import lombok.Getter;
import lombok.Setter;

/**
 * Created by weiqinxiao on 2019/3/18.
 */
public class UpgradeRoleMessage extends BaseMessage {

    private @Getter @Setter String ticket;
    private @Getter @Setter String opUserId;
    private @Getter @Setter String opUserName;

    private @Getter @Setter int action;
    private @Getter @Setter int role;

    public UpgradeRoleMessage(int action) {
        this.action = action;
    }

    @Override
    public String getObjectName() {
        return "SC:IURMsg";
    }
}

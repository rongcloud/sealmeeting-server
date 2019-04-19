package cn.rongcloud.im.message;

import cn.rongcloud.im.BaseMessage;
import com.alibaba.fastjson.annotation.JSONField;
import lombok.Getter;
import lombok.Setter;

/**
 * Created by weiqinxiao on 2019/3/7.
 */
public class DeviceStateChangedMessage extends BaseMessage {
    private @Setter @Getter boolean enable;

    private @Setter @Getter int type;

    private @Setter @Getter String userId;
    private @Setter @Getter String userName;

    public DeviceStateChangedMessage(int type, boolean enable) {
        this.type = type;
        this.enable = enable;
    }

    @Override
    public String getObjectName() {
        return "SC:DRMsg";
    }
}

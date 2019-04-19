package cn.rongcloud.im.message;

import cn.rongcloud.im.BaseMessage;
import lombok.Getter;
import lombok.Setter;

/**
 * Created by weiqinxiao on 2019/3/13.
 */
public class ApplyForSpeechMessage extends BaseMessage {
    private @Getter @Setter String reqUserId;
    private @Getter @Setter String reqUserName;
    private @Getter @Setter String ticket;

    @Override
    public String getObjectName() {
        return "SC:RSMsg";
    }
}

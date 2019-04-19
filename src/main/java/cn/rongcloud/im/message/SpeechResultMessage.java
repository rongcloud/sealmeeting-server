package cn.rongcloud.im.message;

import cn.rongcloud.im.BaseMessage;
import lombok.Getter;
import lombok.Setter;

/**
 * Created by weiqinxiao on 2019/3/13.
 */
public class SpeechResultMessage extends BaseMessage {
    public final static int Action_Approve = 1, Action_Reject = 2;
    private @Getter @Setter String opUserId;
    private @Getter @Setter String opUserName;
    private @Getter @Setter String reqUserId;
    private @Getter @Setter String reqUserName;
    private @Getter @Setter int role;
    private @Getter @Setter int action;

    public SpeechResultMessage(int action) {
        this.action = action;
    }

    @Override
    public String getObjectName() {
        return "SC:SRMsg";
    }
}

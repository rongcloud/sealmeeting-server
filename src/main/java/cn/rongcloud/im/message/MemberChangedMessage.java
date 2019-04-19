package cn.rongcloud.im.message;

import cn.rongcloud.im.BaseMessage;
import com.alibaba.fastjson.annotation.JSONField;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

/**
 * Created by weiqinxiao on 2019/3/6.
 */
public class MemberChangedMessage extends BaseMessage {
    public final static int Action_Join = 1;
    public final static int Action_Leave = 2;
    public final static int Action_Kick = 3;

    @Getter @Setter private int action;

    @Getter @Setter private String userId;
    @Getter @Setter private String userName;

    private @Getter @Setter int role;

    private @Getter @Setter Date timestamp;

    public MemberChangedMessage(int action, String userId, int role) {
        this.action = action;
        this.userId = userId;
        this.role = role;
    }

    @Override
    public String getObjectName() {
        return "SC:RMCMsg";
    }
}

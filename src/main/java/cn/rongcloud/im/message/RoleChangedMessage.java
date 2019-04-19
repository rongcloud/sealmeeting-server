package cn.rongcloud.im.message;

import cn.rongcloud.im.BaseMessage;
import cn.rongcloud.pojo.ReqChangeUserRoleData;
import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * Created by weiqinxiao on 2019/3/6.
 */
public class RoleChangedMessage extends BaseMessage {

    private @Getter @Setter String opUserId;

    private @Getter @Setter List<ChangedUser> users;


    public RoleChangedMessage(String opUserId) {
        this.opUserId = opUserId;
    }

    @Override
    public String getObjectName() {
        return "SC:RCMsg";
    }

    public static class ChangedUser {
        @Getter @Setter String userId;
        @Getter @Setter String userName;
        @Getter @Setter int role;

        public ChangedUser(String userId, int role) {
            this.userId = userId;
            this.role = role;
        }
    }
}

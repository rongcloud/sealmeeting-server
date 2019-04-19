package cn.rongcloud.im.message;

import cn.rongcloud.im.BaseMessage;
import lombok.Getter;
import lombok.Setter;

/**
 * Created by weiqinxiao on 2019/3/15.
 */
public class TicketExpiredMessage extends BaseMessage {
    private @Getter @Setter String ticket;
    private @Getter @Setter String fromUserId;//请求发言者
    private @Getter @Setter String toUserId;//处理请求者

    @Override
    public String getObjectName() {
        return "SC:TEMsg";
    }
}

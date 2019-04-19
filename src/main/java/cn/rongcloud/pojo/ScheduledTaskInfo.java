package cn.rongcloud.pojo;

import lombok.Data;

/**
 * Created by weiqinxiao on 2019/3/13.
 */
@Data
public class ScheduledTaskInfo {
    private String ticket;
    private String roomId;
    private String applyUserId;
    private String targetUserId;

    @Override
    public String toString() {
        return "{" +
                "ticket='" + ticket + '\'' +
                ", roomId='" + roomId + '\'' +
                ", applyUserId='" + applyUserId + '\'' +
                ", targetUserId='" + targetUserId + '\'' +
                '}';
    }
}

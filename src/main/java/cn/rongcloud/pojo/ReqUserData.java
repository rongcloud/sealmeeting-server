package cn.rongcloud.pojo;

import lombok.Data;

/**
 * Created by weiqinxiao on 2019/3/1.
 */
@Data
public class ReqUserData {
    private String userName;
    private String roomId;
    private String userId;
    private boolean observer;
    private boolean disableCamera;
}

package cn.rongcloud.pojo;

import lombok.Data;

/**
 * Created by weiqinxiao on 2019/3/13.
 */
@Data
public class ReqUpgradeRoleData {
    private String roomId;
    private String userId;
    private String ticket;
    private int role;
}

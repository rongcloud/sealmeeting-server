package cn.rongcloud.pojo;

import lombok.Data;

/**
 * Created by weiqinxiao on 2019/3/18.
 */
@Data
public class UpgradeRoleTaskInfo extends ScheduledTaskInfo {
    private RoleEnum role;

    @Override
    public String toString() {
        return "UpgradeRoleTaskInfo{" +
                "role=" + role +
                '}' + super.toString();
    }
}

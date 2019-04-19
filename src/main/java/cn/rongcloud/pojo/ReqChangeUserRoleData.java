package cn.rongcloud.pojo;

import lombok.Data;

import java.util.List;

/**
 * Created by weiqinxiao on 2019/3/4.
 */
@Data
public class ReqChangeUserRoleData {
    private String roomId;
    private List<ChangedUser> users;

    @Data
    public static class ChangedUser {
        String userId;
        int role;
    }
}

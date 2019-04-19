package cn.rongcloud.pojo;

import lombok.Data;

/**
 * Created by weiqinxiao on 2019/3/19.
 */
@Data
public class ReqChangeRole {
    String userId;
    int role;
    String roomId;
}

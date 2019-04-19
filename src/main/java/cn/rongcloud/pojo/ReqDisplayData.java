package cn.rongcloud.pojo;

import lombok.Data;

/**
 * Created by weiqinxiao on 2019/3/1.
 */
@Data
public class ReqDisplayData {
    private String roomId;
    private int type;
    private String userId;//即将展示的对应人的 userId
    private String uri;
}

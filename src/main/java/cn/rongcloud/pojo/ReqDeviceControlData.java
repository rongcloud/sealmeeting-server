package cn.rongcloud.pojo;

import lombok.Data;

/**
 * Created by weiqinxiao on 2019/3/7.
 */
@Data
public class ReqDeviceControlData {
    private Boolean cameraOn;
    private Boolean microphoneOn;
    private String roomId;
    private String userId;
    private String ticket;
}

package cn.rongcloud.pojo;

import lombok.Data;

/**
 * Created by weiqinxiao on 2019/3/13.
 */
@Data
public class ControlDeviceTaskInfo extends ScheduledTaskInfo {
    private DeviceTypeEnum typeEnum;
    private boolean onOff;

    @Override
    public String toString() {
        return "ControlDeviceTaskInfo{" +
                "typeEnum=" + typeEnum +
                ", onOff=" + onOff +
                '}' + super.toString();
    }
}

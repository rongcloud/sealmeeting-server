package cn.rongcloud.im.message;

import cn.rongcloud.im.BaseMessage;
import com.alibaba.fastjson.annotation.JSONField;
import lombok.Getter;
import lombok.Setter;

/**
 * Created by weiqinxiao on 2019/3/7.
 */
public class DisplayMessage extends BaseMessage {

    private @Setter @Getter String display;

    public DisplayMessage(String display) {
        this.display = display;
    }

    @Override
    public String getObjectName() {
        return "SC:DisplayMsg";
    }
}

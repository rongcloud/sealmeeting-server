package cn.rongcloud.job;

import cn.rongcloud.pojo.ScheduledTaskInfo;
import lombok.Getter;
import org.springframework.scheduling.config.FixedDelayTask;

/**
 * Created by weiqinxiao on 2019/3/15.
 */
public class ScheduledDelayTask extends FixedDelayTask {
    private @Getter
    ScheduledTaskInfo scheduledTaskInfo;

    public ScheduledDelayTask(Runnable runnable, long interval, long initialDelay, ScheduledTaskInfo scheduledTaskInfo) {
        super(runnable, interval, initialDelay);
        this.scheduledTaskInfo = scheduledTaskInfo;
    }
}

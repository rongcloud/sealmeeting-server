package cn.rongcloud.job;

import cn.rongcloud.common.ApiException;
import cn.rongcloud.common.ErrorEnum;
import cn.rongcloud.config.RoomProperties;
import cn.rongcloud.config.WhiteBoardProperties;
import cn.rongcloud.dao.RoomDao;
import cn.rongcloud.dao.RoomMemberDao;
import cn.rongcloud.im.IMHelper;
import cn.rongcloud.im.message.TicketExpiredMessage;
import cn.rongcloud.pojo.ScheduledTaskInfo;
import cn.rongcloud.service.RoomService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.config.ScheduledTask;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;
import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by weiqinxiao on 2019/3/15.
 */
@Slf4j
@Service
public class ScheduleManager implements SchedulingConfigurer {
    private ScheduledTaskRegistrar taskRegistrar;

    @Autowired
    RoomProperties roomProperties;

    @Autowired
    WhiteBoardProperties whiteBoardProperties;

    @Autowired
    IMHelper imHelper;

    @Autowired
    RoomMemberDao roomMemberDao;

    @Autowired
    RoomDao roomDao;

    private ConcurrentHashMap<String, ScheduledTask> schedulingTasks = new ConcurrentHashMap<>();
    private ConcurrentHashMap<String, ScheduledTask> roomCacheTasks = new ConcurrentHashMap<>();

    @Override
    public void configureTasks(ScheduledTaskRegistrar scheduledTaskRegistrar) {
        this.taskRegistrar = scheduledTaskRegistrar;
        log.info("config schedule: taskTtl = {}, roomTtl={}, roomMaxCount={}, ", roomProperties.getTaskTtl(), roomProperties.getRoomTtl(), roomProperties.getMaxCount());
        log.info("config whiteboard: host={}", whiteBoardProperties.getHost());
    }

    public void addExpiredTask(RoomService roomService, String roomId) {
        log.info("addExpiredTask destroyRoom: {}", roomId);
        ScheduledTask task = taskRegistrar.scheduleFixedDelayTask(new ScheduledDelayTask(new Runnable() {
            @Override
            public void run() {
                ScheduledTask task = roomCacheTasks.remove(roomId);
                task.cancel();
                roomService.destroyRoom(roomId);

            }
        }, roomProperties.getRoomTtl() * 10, roomProperties.getRoomTtl(), null));
        roomCacheTasks.put(roomId, task);
    }

    public void addTask(ScheduledTaskInfo task) {
        log.info("add speech task: {}", task);
        schedulingTasks.put(task.getTicket(), taskRegistrar.scheduleFixedDelayTask(new ScheduledDelayTask(new Runnable() {
            @Override
            public void run() {
                log.info("speech task expired, execute task: {}", task);
                TicketExpiredMessage msg = new TicketExpiredMessage();
                msg.setFromUserId(task.getApplyUserId());
                msg.setToUserId(task.getTargetUserId());
                msg.setTicket(task.getTicket());
                try {
                    imHelper.publishMessage(task.getTargetUserId(), task.getRoomId(), msg);
                } catch (Exception e) {
                    log.error("msg send error: {}", e.getMessage());
                }
                ScheduledTask scheduledTask = schedulingTasks.remove(task.getTicket());
                scheduledTask.cancel();
            }
        }, roomProperties.getTaskTtl() * 60, roomProperties.getTaskTtl(), task)));
    }

    public ScheduledTaskInfo executeTask(String key) {
        ScheduledTask scheduledTask = schedulingTasks.remove(key);
        if (scheduledTask == null) {
            log.error("task not exist: key={}", key);
            throw new ApiException(ErrorEnum.ERR_APPLY_TICKET_INVALID);
        }
        ScheduledDelayTask task = (ScheduledDelayTask)scheduledTask.getTask();
        ScheduledTaskInfo taskInfo = task.getScheduledTaskInfo();
        scheduledTask.cancel();
        log.info("execute speech task: {}", taskInfo);
        return taskInfo;
    }
}

package cn.rongcloud.dao;

import cn.rongcloud.pojo.ScheduledTaskInfo;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Created by weiqinxiao on 2019/3/13.
 */
@Repository
public interface SpeechDao extends CrudRepository<ScheduledTaskInfo, String> {

    List<ScheduledTaskInfo> findByTicket(String ticket);
}

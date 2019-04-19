package cn.rongcloud.dao;

import cn.rongcloud.pojo.Whiteboard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Created by weiqinxiao on 2019/2/25.
 */
@Repository
public interface WhiteboardDao extends JpaRepository<Whiteboard, Long> {
    public List<Whiteboard> findByRid(String rid);
    public List<Whiteboard> findByRidAndCreator(String rid, String creator);
    public List<Whiteboard> findByRidAndWbid(String rid, String wbid);

    public int deleteByRid(String rid);

    @Transactional
    @Modifying
    public int deleteByWbid(String wbid);

    @Transactional
    @Modifying
    @Query(value = "delete from t_whiteboard where rid=?1 and creator=?2", nativeQuery = true)
    public int deleteByRidAndCreator(String rid, String creator);

    @Transactional
    @Modifying
    @Query(value = "update t_whiteboard set cur_pg=?3 where wbid=?2 and rid=?1", nativeQuery = true)
    public int updatePageByRidAndWbid(String rid, String wbid, int page);
}
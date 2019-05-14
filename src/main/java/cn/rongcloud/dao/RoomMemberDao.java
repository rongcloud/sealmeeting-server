package cn.rongcloud.dao;

import cn.rongcloud.pojo.RoomMember;
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
public interface RoomMemberDao extends JpaRepository<RoomMember, Long> {
    public List<RoomMember> findByRid(String rid);

    public List<RoomMember> findByRidAndUid(String rid, String uid);

    public List<RoomMember> findByRidAndRole(String rid, int role);

    public List<RoomMember> findByUid(String uid);

    public int countByRidAndRole(String rid, int role);

    @Modifying
    @Transactional
    public int deleteByRid(String roomId);

    @Query(value = "select count(*) from t_room_member where rid=?1", nativeQuery = true)
    public int countByRid(String roomId);

    @Query(value = "select count(*) from t_room_member where rid=?1 and role!=?2", nativeQuery = true)
    public int countByRidAndExcludeRole(String roomId, int excludeRole);

    @Transactional
    @Modifying
    @Query(value = "delete from t_room_member where rid=?1 and uid=?2", nativeQuery = true)
    public int deleteUserByRidAndUid(String rid, String uid);

    @Transactional
    @Modifying
    @Query(value = "update t_room_member set role=?3 where rid=?1 and uid=?2", nativeQuery = true)
    public int updateRoleByRidAndUid(String rid, String uid, int role);

    @Transactional
    @Modifying
    @Query(value = "update t_room_member set role=?3 where rid=?1 and role=?2", nativeQuery = true)
    public int updateRoleByRidAndRole(String rid, int role);

    @Transactional
    @Modifying
    @Query(value = "update t_room_member set camera=?3 where rid=?1 and uid=?2", nativeQuery = true)
    public int updateCameraByRidAndUid(String rid, String uid, boolean camera);

    @Transactional
    @Modifying
    @Query(value = "update t_room_member set mic=?3 where rid=?1 and uid=?2", nativeQuery = true)
    public int updateMicByRidAndUid(String rid, String uid, boolean mic);

    public boolean existsByRidAndUid(String rid, String uid);

    public boolean existsByRidAndRole(String rid, int role);

}
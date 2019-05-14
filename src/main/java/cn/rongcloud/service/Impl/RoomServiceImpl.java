package cn.rongcloud.service.Impl;

import cn.rongcloud.common.*;
import cn.rongcloud.config.IMProperties;
import cn.rongcloud.config.RoomProperties;
import cn.rongcloud.dao.*;
import cn.rongcloud.im.IMHelper;
import cn.rongcloud.im.message.*;
import cn.rongcloud.job.ScheduleManager;
import cn.rongcloud.permission.DeclarePermissions;
import cn.rongcloud.pojo.*;
import cn.rongcloud.service.RoomService;
import cn.rongcloud.utils.CheckUtils;
import cn.rongcloud.utils.CodeUtil;
import cn.rongcloud.utils.DateTimeUtils;
import cn.rongcloud.utils.IdentifierUtils;
import cn.rongcloud.whiteboard.WhiteBoardHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by weiqinxiao on 2019/2/28.
 */
@Slf4j
@Service
public class RoomServiceImpl implements RoomService {
    @Autowired
    private IMHelper imHelper;

    @Autowired
    private RoomProperties roomProperties;

    @Autowired
    private RoomDao roomDao;

    @Autowired
    private RoomMemberDao roomMemberDao;

    @Autowired
    private JwtTokenHelper tokenHelper;

    @Autowired
    private WhiteBoardHelper whiteBoardHelper;

    @Autowired
    private WhiteboardDao whiteboardDao;

    @Autowired
    private ScheduleManager scheduleManager;

    @Autowired
    private UserDao userDao;

    @Autowired
    private IMProperties imProperties;

    @Transactional
    @Override
    public RoomResult joinRoom(String userName, String roomId, boolean isObserver, boolean isDisableCamera, JwtUser jwtUser) throws ApiException, Exception {
        CheckUtils.checkArgument(userName != null, "userName must't be null");
        CheckUtils.checkArgument(roomId != null, "roomId must't be null");

        log.info("joinRoom: jwtUser={}, roomId={}, userName={}, isObserver={}, isDisableCamera={}", jwtUser, roomId, userName, isObserver, isDisableCamera);
        String userId;
        if (jwtUser != null) {
            userId = jwtUser.getUserId();
            if (!jwtUser.getUserName().equals(userName) || !jwtUser.getRoomId().equals(roomId)) {
                userId = IdentifierUtils.uuid();
                log.info("generate new user: roomId={} , userId={}, userName={}", roomId, userId, userName);
            } else {
                log.info("join the old room: roomId={} , userId={}, userName={}", roomId, userId, userName);
            }
        } else {
            userId = IdentifierUtils.uuid();
        }

        String display = "";
        Date curTime = DateTimeUtils.currentUTC();
        List<Room> roomList = roomDao.findByRid(roomId);
        if (roomList.isEmpty()) {
            saveRoom(roomId, roomId, curTime, display);
            IMApiResultInfo resultInfo = imHelper.createGroup(new String[]{userId}, roomId, roomId);
            if (!resultInfo.isSuccess()) {
                log.error("joinRoom IM error: roomId={}, {}", roomId, resultInfo.getErrorMessage());
                throw new ApiException(ErrorEnum.ERR_CREATE_ROOM_ERROR, resultInfo.getErrorMessage());
            } else {
                scheduleManager.addExpiredTask(this, roomId);
            }
        } else {
            display = roomList.get(0).getDisplay();
        }

        RoleEnum roleEnum;
        RoomResult roomResult = new RoomResult();
        RoomResult.MemberResult userResult = new RoomResult.MemberResult();
        List<RoomMember> memberList = roomMemberDao.findByRidAndUid(roomId, userId);
        if (memberList.isEmpty()) {
            int count = roomMemberDao.countByRidAndExcludeRole(roomId, RoleEnum.RoleObserver.getValue());
            if (!isObserver && count == roomProperties.getMaxCount()) {
                log.info("join error: roomId = {}, userName = {}, isObserver = {}", roomId, userName, isObserver);
                throw new ApiException(ErrorEnum.ERR_OVER_MAX_COUNT);
            }
            if (!isObserver) {
                List<RoomMember> adminList = roomMemberDao.findByRidAndRole(roomId, RoleEnum.RoleAdmin.getValue());
                if (!adminList.isEmpty()) {
                    if (count == 1) {
                        roleEnum = RoleEnum.RoleSpeaker;
                    } else {
                        roleEnum = RoleEnum.RoleParticipant;
                    }
                } else {
                    roleEnum = RoleEnum.RoleAdmin;
                }
            } else {
                roleEnum = RoleEnum.RoleObserver;
            }
            saveRoomMember(userId, userName, roomId, roleEnum.getValue(), !isDisableCamera, curTime);
            IMApiResultInfo resultInfo = imHelper.joinGroup(new String[]{userId}, roomId, roomId);
            if (!resultInfo.isSuccess()) {
                throw new ApiException(ErrorEnum.ERR_CREATE_ROOM_ERROR, resultInfo.getErrorMessage());
            }
            userResult.setMicrophone(true);
            userResult.setCamera(!isDisableCamera);
            userResult.setJoinTime(curTime);
            log.info("user join the room: roomId={} , userId={}, roleEnum={}, memCount: {}", roomId, userId, roleEnum, count);
        } else {
            roleEnum = RoleEnum.getEnumByValue(memberList.get(0).getRole());
            roomMemberDao.updateCameraByRidAndUid(roomId, userId, !isDisableCamera);
            userResult.setCamera(!isDisableCamera);
            userResult.setJoinTime(memberList.get(0).getJoinDt());

            log.info("user exist in the room: roomId={} , userId={}, use the last role={}", roomId, userId, roleEnum);
        }

        MemberChangedMessage msg = new MemberChangedMessage(MemberChangedMessage.Action_Join, userId, roleEnum.getValue());
        msg.setTimestamp(curTime);
        msg.setUserName(userName);
        msg.setCamera(!isDisableCamera);
        imHelper.publishMessage(userId, roomId, msg);
        if (roleEnum == RoleEnum.RoleSpeaker) {
            display = "display://type=1?userId=" + userId + "?uri=";
            updateDisplay(roomId, userId, display, 0);
            log.info("joinRoom, display changed: roomId={}, {}, userId={}", roomId, display, userId);
        } else if (roleEnum == RoleEnum.RoleAdmin && display.isEmpty()) {
            display = "display://type=0?userId=" + userId + "?uri=";
            updateDisplay(roomId, userId, display, 0);
            log.info("joinRoom, display changed: roomId={}, {}, userId={}", roomId, display, userId);
        }

        List<UserInfo> userInfoList = userDao.findByUid(userId);
        if (userInfoList.isEmpty()) {
            UserInfo userInfo = new UserInfo();
            userInfo.setUid(userId);
            userInfo.setName(userName);
            userInfo.setCreateDt(curTime);
            userInfo.setUpdateDt(curTime);
            userDao.save(userInfo);
        } else {
            UserInfo user = userInfoList.get(0);
            user.setUpdateDt(curTime);
            userDao.save(user);
        }

        userResult.setUserName(userName);
        userResult.setUserId(userId);
        userResult.setRole(roleEnum.getValue());
        roomResult.setUserInfo(userResult);
        roomResult.setDisplay(display);

        //generate authorization
        jwtUser = new JwtUser();
        jwtUser.setUserId(userId);
        jwtUser.setUserName(userName);
        jwtUser.setRoomId(roomId);
        JwtToken jwtToken = tokenHelper.createJwtToken(jwtUser);
        IMTokenInfo tokenInfo = imHelper.getToken(userId, userId, "");
        if (tokenInfo.isSuccess()) {
            roomResult.setImToken(tokenInfo.getToken());
        } else {
            throw new ApiException(ErrorEnum.ERR_IM_TOKEN_ERROR, tokenInfo.getErrorMessage());
        }
        roomResult.setAuthorization(jwtToken.getToken());
        roomResult.setRoomId(roomId);
        List<RoomMember> roomMemberList = roomMemberDao.findByRid(roomId);
        roomResult.setMembers(roomMemberList);

        List<Whiteboard> whiteboardList = whiteboardDao.findByRid(roomId);
        roomResult.setWhiteboards(whiteboardList);
        log.info("join success: roomId = {}, userId = {}, userName={}, role = {}", roomId, userId, userName, roleEnum);
        return roomResult;
    }

    private void saveRoom(String roomId, String roomName, Date createTime, String display) {
        Room room = new Room();
        room.setRid(roomId);
        room.setName(roomName);
        room.setCreateDt(createTime);
        room.setDisplay(display);
        room.setWhiteboardNameIndex(0);
        roomDao.save(room);
    }

    private void saveRoomMember(String userId, String userName, String roomId, int role, boolean cameraOn, Date joinTime) {
        RoomMember roomMember = new RoomMember();
        roomMember.setUid(userId);
        roomMember.setName(userName);
        roomMember.setRid(roomId);
        roomMember.setRole(role);
        roomMember.setCamera(cameraOn);
        roomMember.setJoinDt(joinTime);
        roomMemberDao.save(roomMember);
    }

    @Transactional
    @Override
    public Boolean leaveRoom(JwtUser jwtUser, String roomId) throws Exception {
        CheckUtils.checkArgument(jwtUser.getUserId() != null, "userId must't be null");
        CheckUtils.checkArgument(roomId != null, "roomId must't be null");
        CheckUtils.checkArgument(roomId.equals(jwtUser.getRoomId()), "roomId not exist");

        List<Room> roomList = roomDao.findByRid(roomId);
        if (roomList.size() == 0) {
            log.error("room : {} not exist ", roomId);
            throw new ApiException(ErrorEnum.ERR_ROOM_NOT_EXIST);
        }

        List<RoomMember> roomMemberList = roomMemberDao.findByRidAndUid(roomId, jwtUser.getUserId());
        if (roomMemberList.size() == 0) {
            log.error("{} not exist in room: {}", jwtUser.getUserId(), roomId);
            throw new ApiException(ErrorEnum.ERR_USER_NOT_EXIST_IN_ROOM);
        }

        int userRole = roomMemberList.get(0).getRole();
        log.info("leaveRoom: roomId={}, {}, role={}", roomId, jwtUser, RoleEnum.getEnumByValue(userRole));

        if (userRole == RoleEnum.RoleSpeaker.getValue() || userRole == RoleEnum.RoleAdmin.getValue()) {
            if (isUserDisplay(roomList.get(0), jwtUser.getUserId())) {
                updateDisplay(roomId, jwtUser.getUserId(), "", 0);
                log.info("clear display cause speaker leave: roomId={}, {}", roomId, jwtUser);
            } else {
                log.info("don't update current display: room={}, role={}", roomList.get(0), RoleEnum.getEnumByValue(userRole));
            }
        } else {
            log.info("don't update current display: room={}, userRole={}", roomList.get(0), RoleEnum.getEnumByValue(userRole));
        }

        if (roomMemberDao.countByRid(roomId) == 1) {
            IMApiResultInfo apiResultInfo = null;
            try {
                apiResultInfo = imHelper.dismiss(jwtUser.getUserId(), roomId);
                if (apiResultInfo.getCode() == 200) {
                    roomMemberDao.deleteUserByRidAndUid(roomId, jwtUser.getUserId());
                    roomDao.deleteByRid(roomId);
                    deleteWhiteboardByUser(roomId, jwtUser.getUserId());
                    log.info("dismiss the room: {}", roomId);
                } else {
                    log.error("{} exit {} room error: {}", jwtUser.getUserId(), roomId, apiResultInfo.getErrorMessage());
                    throw new ApiException(ErrorEnum.ERR_EXIT_ROOM_ERROR, apiResultInfo.getErrorMessage());
                }
            } catch (Exception e) {
                log.error("{} exit {} room error: {}", jwtUser.getUserId(), roomId, e.getMessage());
                throw new ApiException(ErrorEnum.ERR_EXIT_ROOM_ERROR, e.getMessage());
            }
        } else {
            IMApiResultInfo apiResultInfo = null;
            try {
                apiResultInfo = imHelper.quit(new String[]{jwtUser.getUserId()}, roomId);
                if (apiResultInfo.isSuccess()) {
                    roomMemberDao.deleteUserByRidAndUid(roomId, jwtUser.getUserId());
                    MemberChangedMessage msg = new MemberChangedMessage(MemberChangedMessage.Action_Leave, jwtUser.getUserId(), userRole);
                    msg.setUserName(jwtUser.getUserName());
                    imHelper.publishMessage(jwtUser.getUserId(), roomId, msg);
                    imHelper.quit(new String[]{jwtUser.getUserId()}, roomId);
                    log.info("quit group: roomId={}, {}", roomId, jwtUser);
                } else {
                    throw new ApiException(ErrorEnum.ERR_EXIT_ROOM_ERROR, apiResultInfo.getErrorMessage());
                }
            } catch (Exception e) {
                log.error("leave room error: roomId={}, {}, {}", roomId, jwtUser, e.getMessage());
                throw new ApiException(ErrorEnum.ERR_EXIT_ROOM_ERROR);
            }
        }
        userDao.deleteByUid(jwtUser.getUserId());

        return true;
    }

    private void deleteWhiteboardByUser(String roomId, String userId) throws Exception {
        List<Whiteboard> whiteboardList = whiteboardDao.findByRidAndCreator(roomId, userId);
        if (!whiteboardList.isEmpty()) {
            whiteboardDao.deleteByRidAndCreator(roomId, userId);
            for (Whiteboard wb : whiteboardList) {
                whiteBoardHelper.destroy(wb.getWbRoom());
            }
        }
    }

    @Transactional
    @Override
    public void destroyRoom(String roomId) {
        roomDao.deleteByRid(roomId);
        whiteboardDao.deleteByRid(roomId);

        List<RoomMember> list = roomMemberDao.findByRid(roomId);
        if (!list.isEmpty()) {
            try {
                imHelper.dismiss(list.get(0).getUid(), roomId);
            } catch (Exception e) {
                log.error("destroyRoom: {}", e.getMessage());
                e.printStackTrace();
            }
        }
        roomMemberDao.deleteByRid(roomId);
        log.info("destroyRoom: {}", roomId);
    }

    @Transactional
    @DeclarePermissions(RoleEnum.RoleAdmin)
    @Override
    public Boolean downgrade(String roomId, JwtUser jwtUser, List<ReqChangeUserRoleData.ChangedUser> users) throws ApiException, Exception {
        CheckUtils.checkArgument(roomId != null, "roomId must't be null");
        CheckUtils.checkArgument(users.size() > 0, "the changed user list must't be null");

        List<Room> roomList = roomDao.findByRid(roomId);
        if (roomList.isEmpty()) {
            throw new ApiException(ErrorEnum.ERR_ROOM_NOT_EXIST);
        }

        boolean result = false;
        List<RoleChangedMessage.ChangedUser> changedUsers = new ArrayList<>();
        for (ReqChangeUserRoleData.ChangedUser user : users) {
            String changedUserId = user.getUserId();
            RoleEnum changedRole = RoleEnum.getEnumByValue(user.getRole());
            if (changedUserId.equals(jwtUser.getUserId())) {
                log.error("can not change self role: {}, {}, {}", roomId, jwtUser.getUserId(), changedRole);
                throw new ApiException(ErrorEnum.ERR_CHANGE_SELF_ROLE);
            } else {
                List<RoomMember> oldUsers = roomMemberDao.findByRidAndUid(roomId, changedUserId);
                if (oldUsers.size() > 0) {
                    if (changedRole.equals(RoleEnum.RoleObserver)) {
                        int r = roomMemberDao.updateRoleByRidAndUid(roomId, changedUserId, changedRole.getValue());
                        RoleChangedMessage.ChangedUser u = new RoleChangedMessage.ChangedUser(changedUserId, changedRole.getValue());
                        List<UserInfo> userInfoList = userDao.findByUid(changedUserId);
                        if (!userInfoList.isEmpty()) {
                            u.setUserName(userInfoList.get(0).getName());
                        }
                        changedUsers.add(u);
                        log.info("change the role: {}, {}, {}, result: {}", roomId, jwtUser.getUserId(), changedRole, r);
                        result = true;
                    }
                    if (oldUsers.get(0).getRole() == RoleEnum.RoleSpeaker.getValue() && isUserDisplay(roomList.get(0), oldUsers.get(0).getUid())) {
                        updateDisplay(roomId, jwtUser.getUserId(), "", 1);
                    } else {
                        log.info("don't update display: room={}, userRole={}", roomList.get(0), RoleEnum.getEnumByValue(oldUsers.get(0).getRole()));
                    }
                } else {
                    log.info("role changed fail, not exist: {} - {} - {}", roomId, jwtUser.getUserId(), changedRole);
                }
            }
        }
        if (result) {
            RoleChangedMessage msg = new RoleChangedMessage(jwtUser.getUserId());
            msg.setUsers(changedUsers);
            imHelper.publishMessage(jwtUser.getUserId(), roomId, msg, 1);
        }
        return result;
    }

    @DeclarePermissions(RoleEnum.RoleAdmin)
    @Override
    public Boolean kickMember(String roomId, String userId, JwtUser jwtUser) throws ApiException, Exception {
        CheckUtils.checkArgument(userId != null, "userId must't be null");
        CheckUtils.checkArgument(roomId != null, "roomId must't be null");

        List<RoomMember> kickedUsers = roomMemberDao.findByRidAndUid(roomId, userId);
        int result = roomMemberDao.deleteUserByRidAndUid(roomId, userId);
        log.info("kickMember: roomId={}, userId={}, result = {}, {}", roomId, userId, result, jwtUser);
        if (result == 0) {
            throw new ApiException(ErrorEnum.ERR_USER_NOT_EXIST_IN_ROOM);
        } else {
            MemberChangedMessage msg = new MemberChangedMessage(MemberChangedMessage.Action_Kick, userId, kickedUsers.get(0).getRole());
            List<UserInfo> userInfoList = userDao.findByUid(userId);
            if (!userInfoList.isEmpty()) {
                msg.setUserName(userInfoList.get(0).getName());
            }
            IMApiResultInfo apiResultInfo = imHelper.publishMessage(jwtUser.getUserId(), roomId, msg, 1);
            if (!apiResultInfo.isSuccess()) {
                throw new ApiException(ErrorEnum.ERR_MESSAGE_ERROR);
            }
            Thread.sleep(50);
            log.info("published msg: {}, msg={}", jwtUser, msg);
            List<Room> roomList = roomDao.findByRid(roomId);
            if (kickedUsers.get(0).getRole() == RoleEnum.RoleSpeaker.getValue() && isUserDisplay(roomList.get(0), userId)) {
                updateDisplay(roomId, jwtUser.getUserId(), "", 1);
            } else {
                log.info("don't update display: room={}, userRole={}", roomId, RoleEnum.getEnumByValue(kickedUsers.get(0).getRole()));
            }
        }
        userDao.deleteByUid(userId);
        IMApiResultInfo apiResultInfo = imHelper.quit(new String[]{userId}, roomId);
        if (!apiResultInfo.isSuccess()) {
            throw new ApiException(ErrorEnum.ERR_EXIT_ROOM_ERROR, apiResultInfo.getErrorMessage());
        }
        return true;
    }

    @DeclarePermissions({RoleEnum.RoleSpeaker, RoleEnum.RoleAdmin})
    @Override
    public Boolean display(String roomId, int type, String userId, String uri, JwtUser jwtUser) throws ApiException, Exception {
        log.info("display in room: {}, type = {}, uri = {}, {}", roomId, type, uri, jwtUser);
        CheckUtils.checkArgument(roomId != null, "roomId must't be null");
        CheckUtils.checkArgument(type >= 0 && type < DisplayEnum.values().length, "type not exist");
        DisplayEnum displayEnum = DisplayEnum.values()[type];

        if (displayEnum.equals(DisplayEnum.None)) {
            roomDao.updateDisplayByRid(roomId, "");
            DisplayMessage displayMessage = new DisplayMessage("");
            IMApiResultInfo apiResultInfo = imHelper.publishMessage(jwtUser.getUserId(), roomId, displayMessage);
            if (apiResultInfo.isSuccess()) {
                return true;
            } else {
                throw new ApiException(ErrorEnum.ERR_MESSAGE_ERROR, apiResultInfo.getErrorMessage());
            }
        }

        String display = "display://type=" + type;
        if (displayEnum.equals(DisplayEnum.Speaker)) {
            List<RoomMember> speakers = roomMemberDao.findByRidAndRole(roomId, RoleEnum.RoleSpeaker.getValue());
            if (speakers.isEmpty()) {
                throw new ApiException(ErrorEnum.ERR_SPEAKER_NOT_EXIST_IN_ROOM);
            } else {
                display += "?userId=" + speakers.get(0).getUid() + "?uri=";
                roomDao.updateDisplayByRid(roomId, display);
                DisplayMessage displayMessage = new DisplayMessage(display);
                imHelper.publishMessage(jwtUser.getUserId(), roomId, displayMessage);
                log.info("change display to speaker: roomId={}, {}, display={}", roomId, jwtUser, display);
            }
        } else if (displayEnum.equals(DisplayEnum.Admin)) {
            List<RoomMember> adminList = roomMemberDao.findByRidAndRole(roomId, RoleEnum.RoleAdmin.getValue());
            if (adminList.isEmpty()) {
                throw new ApiException(ErrorEnum.ERR_ADMIN_NOT_EXIST_IN_ROOM);
            } else {
                display += "?userId=" + adminList.get(0).getUid() + "?uri=";
                roomDao.updateDisplayByRid(roomId, display);
                DisplayMessage displayMessage = new DisplayMessage(display);
                imHelper.publishMessage(jwtUser.getUserId(), roomId, displayMessage);
                log.info("change display to admin: roomId={}, {}, display={}", roomId, jwtUser, display);
            }
        } else if (displayEnum.equals(DisplayEnum.Screen)) {
            display += "?userId=" + userId + "?uri=";
            roomDao.updateDisplayByRid(roomId, display);
            DisplayMessage displayMessage = new DisplayMessage(display);
            imHelper.publishMessage(jwtUser.getUserId(), roomId, displayMessage);
            log.info("change display to screen: roomId={}, {}, display={}", roomId, jwtUser, display);
        } else {
            display += "?userId=" + "?uri=" + uri;
            CheckUtils.checkArgument(uri != null, "uri must't be null");
            CheckUtils.checkArgument(whiteboardDao.findByRidAndWbid(roomId, uri).size() > 0, "whiteboard not exist");

            roomDao.updateDisplayByRid(roomId, display);
            DisplayMessage displayMessage = new DisplayMessage(display);
            imHelper.publishMessage(jwtUser.getUserId(), roomId, displayMessage);
        }
        log.info("result display in room: {}, type = {}, uri = {}, {}", roomId, type, uri, jwtUser);
        return true;
    }

    @DeclarePermissions({RoleEnum.RoleSpeaker, RoleEnum.RoleAdmin})
    @Override
    public String createWhiteBoard(String roomId, JwtUser jwtUser) throws ApiException, Exception {
        CheckUtils.checkArgument(roomId != null, "roomId must't be null");
        CheckUtils.checkArgument(roomDao.existsByRid(roomId), "room not exist");
        CheckUtils.checkArgument(roomMemberDao.existsByRidAndUid(roomId, jwtUser.getUserId()), "room member not exist");

        log.info("createWhiteBoard: roomId = {},  {}", roomId, jwtUser);

        String wbRoom = IdentifierUtils.uuid();
        WhiteBoardApiResultInfo resultInfo = whiteBoardHelper.create(wbRoom);
        if (resultInfo.isSuccess()) {
            String wbId = resultInfo.getData();
            Date date = DateTimeUtils.currentUTC();
            List<Room> roomList = roomDao.findByRid(roomId);
            int whiteboardNameIndex = roomList.get(0).getWhiteboardNameIndex() + 1;
            String name = "白板" + whiteboardNameIndex;
            roomDao.updateWhiteboardNameIndexByRid(roomId, whiteboardNameIndex);
            Whiteboard wb = new Whiteboard();
            wb.setRid(roomId);
            wb.setWbRoom(wbRoom);
            wb.setWbid(wbId);
            wb.setName(name);
            wb.setCreator(jwtUser.getUserId());
            wb.setCreateDt(date);
            wb.setCurPg(0);
            whiteboardDao.save(wb);
            WhiteboardMessage wbmsg = new WhiteboardMessage(WhiteboardMessage.Create);
            wbmsg.setWhiteboardId(wbId);
            wbmsg.setWhiteboardName(name);
            imHelper.publishMessage(jwtUser.getUserId(), roomId, wbmsg);
            String display = "display://type=2?userId=" + jwtUser.getUserId() + "?uri=" + wbId;
            roomDao.updateDisplayByRid(roomId, display);
            DisplayMessage displayMessage = new DisplayMessage(display);
            imHelper.publishMessage(jwtUser.getUserId(), roomId, displayMessage, 1);

            return wbId;
        } else {
            throw new ApiException(ErrorEnum.ERR_CREATE_WHITE_BOARD, resultInfo.getMsg());
        }
    }

    @DeclarePermissions({RoleEnum.RoleSpeaker, RoleEnum.RoleAdmin})
    @Override
    public Boolean deleteWhiteboard(String roomId, JwtUser jwtUser, String whiteBoardId) throws ApiException, Exception {
        CheckUtils.checkArgument(roomId != null, "roomId must't be null");
        CheckUtils.checkArgument(whiteBoardId != null, "whiteBoardId must't be null");

        List<Whiteboard> whiteboardList = whiteboardDao.findByRidAndWbid(roomId, whiteBoardId);
        CheckUtils.checkArgument(whiteboardList.size() > 0, "whiteboard not exist");

        List<Room> roomList = roomDao.findByRid(roomId);
        CheckUtils.checkArgument(!roomList.isEmpty(), "room not exist");

        log.info("deleteWhiteboard: room={}, whiteBoardId={}, {}", roomList.get(0), whiteBoardId, jwtUser);

        String display = roomList.get(0).getDisplay();
        if (display.contains("uri=" + whiteBoardId)) {
            int result = roomDao.updateDisplayByRid(roomId, "");
            log.info("clear room display, room: {}, result: {}", roomId, result);
            DisplayMessage displayMessage = new DisplayMessage("");
            imHelper.publishMessage(jwtUser.getUserId(), roomId, displayMessage, 1);
        } else {
            log.info("no display to clean: room={}", roomList.get(0));
        }

        String wbRoom = whiteboardList.get(0).getWbRoom();
        WhiteBoardApiResultInfo resultInfo = whiteBoardHelper.destroy(wbRoom);
        if (resultInfo.isSuccess()) {
            int result = whiteboardDao.deleteByWbid(whiteBoardId);
            log.info("delete whiteboard: roomId = {}, whiteBoardId = {}, result = {}", roomId, whiteBoardId, result);
            WhiteboardMessage wbmsg = new WhiteboardMessage(WhiteboardMessage.Delete);
            wbmsg.setWhiteboardId(whiteBoardId);
            imHelper.publishMessage(jwtUser.getUserId(), roomId, wbmsg, 1);
            return true;
        } else {
            throw new ApiException(ErrorEnum.ERR_DELETE_WHITE_BOARD, resultInfo.getMsg());
        }
    }

    @Override
    public List<RoomResult.WhiteboardResult> getWhiteboard(String roomId, JwtUser jwtUser) throws ApiException, Exception {
        CheckUtils.checkArgument(roomId != null, "roomId must't be null");
        CheckUtils.checkArgument(roomDao.existsByRid(roomId), "room not exist");

        List<Whiteboard> whiteboards = whiteboardDao.findByRid(roomId);
        List<RoomResult.WhiteboardResult> result = new ArrayList<>();
        for (Whiteboard wb : whiteboards) {
            RoomResult.WhiteboardResult r = new RoomResult.WhiteboardResult();
            r.setName(wb.getName());
            r.setCurPg(wb.getCurPg());
            r.setWhiteboardId(wb.getWbid());
            result.add(r);
        }
        return result;
    }

    @DeclarePermissions({RoleEnum.RoleSpeaker, RoleEnum.RoleAdmin})
    @Override
    public Boolean turnWhiteBoardPage(String roomId, String whiteBoardId, int page, JwtUser jwtUser) throws ApiException, Exception {
        CheckUtils.checkArgument(roomId != null, "roomId must't be null");
        CheckUtils.checkArgument(whiteBoardId != null, "whiteBoardId must't be null");
        List<Room> roomList = roomDao.findByRid(roomId);
        CheckUtils.checkArgument(!roomList.isEmpty(), "room not exist");

        int result = whiteboardDao.updatePageByRidAndWbid(roomId, whiteBoardId, page);
        log.info("turn page to: {}, room: {}, wb : {}; r: {}", page, roomId, whiteBoardId, result);

        TurnPageMessage turnPageMessage = new TurnPageMessage(whiteBoardId, jwtUser.getUserId(), page);
        imHelper.publishMessage(jwtUser.getUserId(), roomId, turnPageMessage);
        return true;
    }

    @DeclarePermissions(RoleEnum.RoleAdmin)
    @Override
    public Boolean controlDevice(String roomId, String userId, DeviceTypeEnum typeEnum, boolean enable, JwtUser jwtUser) throws ApiException, Exception {
        CheckUtils.checkArgument(roomId != null, "roomId must't be null");
        CheckUtils.checkArgument(userId != null, "userId must't be null");
        CheckUtils.checkArgument(roomDao.existsByRid(roomId), "room not exist");
        CheckUtils.checkArgument(roomMemberDao.existsByRidAndUid(roomId, userId), "room member not exist");

        log.info("controlDevice: {}, userId={}, typeEnum={}, onOff={}", jwtUser, userId, typeEnum, enable);

        if (enable) {
            String ticket = IdentifierUtils.uuid();
            ControlDeviceTaskInfo taskInfo = new ControlDeviceTaskInfo();
            taskInfo.setRoomId(roomId);
            taskInfo.setTypeEnum(typeEnum);
            taskInfo.setOnOff(true);
            taskInfo.setApplyUserId(jwtUser.getUserId());
            taskInfo.setTargetUserId(userId);
            taskInfo.setTicket(ticket);
            scheduleManager.addTask(taskInfo);
            ControlDeviceNotifyMessage msg = new ControlDeviceNotifyMessage(ActionEnum.Invite.ordinal());
            msg.setTicket(ticket);
            msg.setType(taskInfo.getTypeEnum().ordinal());
            msg.setOpUserId(jwtUser.getUserId());
            msg.setOpUserName(jwtUser.getUserName());
            imHelper.publishMessage(jwtUser.getUserId(), userId, roomId, msg);
        } else {
            if (typeEnum.equals(DeviceTypeEnum.Camera)) {
                roomMemberDao.updateCameraByRidAndUid(roomId, jwtUser.getUserId(), false);
            } else {
                roomMemberDao.updateMicByRidAndUid(roomId, jwtUser.getUserId(), false);
            }
            DeviceStateChangedMessage deviceResourceMessage = new DeviceStateChangedMessage(typeEnum.ordinal(), false);
            deviceResourceMessage.setUserId(userId);
            List<UserInfo> userInfoList = userDao.findByUid(userId);
            if (!userInfoList.isEmpty()) {
                deviceResourceMessage.setUserName(userInfoList.get(0).getName());
            }
            imHelper.publishMessage(jwtUser.getUserId(), roomId, deviceResourceMessage, 1);
        }
        return true;
    }

    @DeclarePermissions({RoleEnum.RoleSpeaker, RoleEnum.RoleParticipant})
    @Override
    public Boolean approveControlDevice(String roomId, String ticket, JwtUser jwtUser) throws ApiException, Exception {
        CheckUtils.checkArgument(ticket != null, "ticket must't be null");

        log.info("approveControlDevice: jwtUser={}, ticket={}", jwtUser, ticket);
        ControlDeviceTaskInfo taskInfo = (ControlDeviceTaskInfo) scheduleManager.executeTask(ticket);
        if (taskInfo.getTypeEnum().equals(DeviceTypeEnum.Camera)) {
            roomMemberDao.updateCameraByRidAndUid(roomId, jwtUser.getUserId(), taskInfo.isOnOff());
        } else {
            roomMemberDao.updateMicByRidAndUid(roomId, jwtUser.getUserId(), taskInfo.isOnOff());
        }
        ControlDeviceNotifyMessage msg = new ControlDeviceNotifyMessage(ActionEnum.Approve.ordinal());
        msg.setType(taskInfo.getTypeEnum().ordinal());
        msg.setOpUserId(jwtUser.getUserId());
        msg.setOpUserName(jwtUser.getUserName());
        imHelper.publishMessage(jwtUser.getUserId(), taskInfo.getApplyUserId(), roomId, msg);

        DeviceStateChangedMessage deviceResourceMessage = new DeviceStateChangedMessage(taskInfo.getTypeEnum().ordinal(), taskInfo.isOnOff());
        deviceResourceMessage.setUserId(jwtUser.getUserId());
        imHelper.publishMessage(jwtUser.getUserId(), roomId, deviceResourceMessage, 1);
        return true;
    }

    @DeclarePermissions({RoleEnum.RoleSpeaker, RoleEnum.RoleParticipant})
    @Override
    public Boolean rejectControlDevice(String roomId, String ticket, JwtUser jwtUser) throws ApiException, Exception {
        CheckUtils.checkArgument(ticket != null, "ticket must't be null");

        log.info("rejectControlDevice: jwtUser={}, ticket={}", jwtUser, ticket);
        ControlDeviceTaskInfo taskInfo = (ControlDeviceTaskInfo) scheduleManager.executeTask(ticket);
        ControlDeviceNotifyMessage msg = new ControlDeviceNotifyMessage(ActionEnum.Reject.ordinal());
        msg.setType(taskInfo.getTypeEnum().ordinal());
        msg.setOpUserId(jwtUser.getUserId());
        msg.setOpUserName(jwtUser.getUserName());
        imHelper.publishMessage(jwtUser.getUserId(), taskInfo.getApplyUserId(), roomId, msg);
        return true;
    }

    @DeclarePermissions({RoleEnum.RoleSpeaker, RoleEnum.RoleParticipant, RoleEnum.RoleAdmin})
    @Override
    public Boolean syncDeviceState(String roomId, DeviceTypeEnum type, boolean enable, JwtUser jwtUser) throws ApiException, Exception {
        CheckUtils.checkArgument(roomId != null, "roomId must't be null");
        CheckUtils.checkArgument(roomDao.existsByRid(roomId), "room not exist");

        int result;
        DeviceStateChangedMessage deviceResourceMessage;
        if (type.equals(DeviceTypeEnum.Camera)) {
            result = roomMemberDao.updateCameraByRidAndUid(roomId, jwtUser.getUserId(), enable);
            deviceResourceMessage = new DeviceStateChangedMessage(type.ordinal(), enable);
        } else {
            result = roomMemberDao.updateMicByRidAndUid(roomId, jwtUser.getUserId(), enable);
            deviceResourceMessage = new DeviceStateChangedMessage(type.ordinal(), enable);
        }
        deviceResourceMessage.setUserId(jwtUser.getUserId());
        imHelper.publishMessage(jwtUser.getUserId(), roomId, deviceResourceMessage, 1);
        log.info("syncDeviceState : {}, {}, result = {}, jwtUser={}", roomId, enable, result, jwtUser);
        return true;
    }

    @DeclarePermissions({RoleEnum.RoleAdmin, RoleEnum.RoleSpeaker, RoleEnum.RoleParticipant, RoleEnum.RoleObserver})
    @Override
    public List<RoomResult.MemberResult> getMembers(String roomId, JwtUser jwtUser) throws ApiException, Exception {
        CheckUtils.checkArgument(roomId != null, "roomId must't be null");

        List<RoomMember> roomMemberList = roomMemberDao.findByRid(roomId);
        RoomResult roomResult = new RoomResult();
        roomResult.setMembers(roomMemberList);
        return roomResult.getMembers();
    }

    @DeclarePermissions(RoleEnum.RoleObserver)
    @Override
    public Boolean applySpeech(String roomId, JwtUser jwtUser) throws ApiException, Exception {
        CheckUtils.checkArgument(roomId != null, "roomId must't be null");
        CheckUtils.checkArgument(roomDao.existsByRid(roomId), "room not exist");

        List<RoomMember> admins = roomMemberDao.findByRidAndRole(roomId, RoleEnum.RoleAdmin.getValue());
        if (admins.isEmpty()) {
            throw new ApiException(ErrorEnum.ERR_ADMIN_NOT_EXIST_IN_ROOM);
        }

        String ticket = IdentifierUtils.uuid();
        ScheduledTaskInfo scheduledTaskInfo = new ScheduledTaskInfo();
        scheduledTaskInfo.setTicket(ticket);
        scheduledTaskInfo.setRoomId(roomId);
        scheduledTaskInfo.setApplyUserId(jwtUser.getUserId());
        scheduledTaskInfo.setTargetUserId(admins.get(0).getUid());
        scheduleManager.addTask(scheduledTaskInfo);

        log.info("applySpeech: task = {}, jwtUser={}", scheduledTaskInfo, jwtUser);

        ApplyForSpeechMessage msg = new ApplyForSpeechMessage();
        msg.setTicket(ticket);
        msg.setReqUserId(jwtUser.getUserId());
        IMApiResultInfo resultInfo = imHelper.publishMessage(jwtUser.getUserId(), admins.get(0).getUid(), roomId, msg);

        log.info("apply for speech: {}, jwtUser = {}, task = {}", roomId, jwtUser, scheduledTaskInfo);
        if (resultInfo.isSuccess()) {
            return true;
        } else {
            throw new ApiException(ErrorEnum.ERR_MESSAGE_ERROR, resultInfo.getErrorMessage());
        }
    }

    @DeclarePermissions(RoleEnum.RoleAdmin)
    @Override
    public Boolean approveSpeech(String roomId, String ticket, JwtUser jwtUser) throws ApiException, Exception {
        CheckUtils.checkArgument(roomId != null, "roomId must't be null");
        CheckUtils.checkArgument(roomDao.existsByRid(roomId), "room not exist");

        int count = roomMemberDao.countByRidAndExcludeRole(roomId, RoleEnum.RoleObserver.getValue());
        if (count == roomProperties.getMaxCount()) {
            log.error("approveSpeech error: roomId = {}, jwtUser = {}, ticket={}", roomId, jwtUser, ticket);
            throw new ApiException(ErrorEnum.ERR_OVER_MAX_COUNT);
        }

        ScheduledTaskInfo taskInfo = scheduleManager.executeTask(ticket);
        log.info("approveSpeech: task = {}, jwtUser={}", taskInfo, jwtUser);
        roomMemberDao.updateRoleByRidAndUid(roomId, taskInfo.getApplyUserId(), RoleEnum.RoleParticipant.getValue());

        SpeechResultMessage msg = new SpeechResultMessage(SpeechResultMessage.Action_Approve);
        List<UserInfo> userInfoList = userDao.findByUid(taskInfo.getApplyUserId());
        msg.setOpUserId(jwtUser.getUserId());
        msg.setOpUserName(jwtUser.getUserName());
        msg.setReqUserId(taskInfo.getApplyUserId());
        if (!userInfoList.isEmpty()) {
            msg.setReqUserName(userInfoList.get(0).getName());
        }
        msg.setRole(RoleEnum.RoleParticipant.getValue());
        IMApiResultInfo resultInfo = imHelper.publishMessage(jwtUser.getUserId(), taskInfo.getApplyUserId(), roomId, msg);
        if (!resultInfo.isSuccess()) {
            throw new ApiException(ErrorEnum.ERR_MESSAGE_ERROR, resultInfo.getErrorMessage());
        }

        RoleChangedMessage rcMsg = new RoleChangedMessage(jwtUser.getUserId());
        List<RoleChangedMessage.ChangedUser> changedUserList = new ArrayList<>();
        RoleChangedMessage.ChangedUser user = new RoleChangedMessage.ChangedUser(taskInfo.getApplyUserId(), RoleEnum.RoleParticipant.getValue());
        if (!userInfoList.isEmpty()) {
            user.setUserName(userInfoList.get(0).getName());
        }
        changedUserList.add(user);
        rcMsg.setUsers(changedUserList);
        imHelper.publishMessage(jwtUser.getUserId(), roomId, rcMsg, 1);

        return true;
    }

    @DeclarePermissions(RoleEnum.RoleAdmin)
    @Override
    public Boolean rejectSpeech(String roomId, String ticket, JwtUser jwtUser) throws ApiException, Exception {
        CheckUtils.checkArgument(roomId != null, "roomId must't be null");
        CheckUtils.checkArgument(roomDao.existsByRid(roomId), "room not exist");

        ScheduledTaskInfo taskInfo = scheduleManager.executeTask(ticket);

        log.info("rejectSpeech: task = {}, jwtUser={}", taskInfo, jwtUser);
        SpeechResultMessage msg = new SpeechResultMessage(SpeechResultMessage.Action_Reject);
        msg.setOpUserId(jwtUser.getUserId());
        msg.setOpUserName(jwtUser.getUserName());
        msg.setRole(RoleEnum.RoleParticipant.getValue());
        IMApiResultInfo resultInfo = imHelper.publishMessage(jwtUser.getUserId(), taskInfo.getApplyUserId(), roomId, msg);
        if (resultInfo.isSuccess()) {
            return true;
        } else {
            throw new ApiException(ErrorEnum.ERR_MESSAGE_ERROR, resultInfo.getErrorMessage());
        }
    }

    private void checkOverMax(String roomId, RoomMember targetUser, int targetRole) {
        if (RoleEnum.getEnumByValue(targetUser.getRole()).equals(RoleEnum.RoleObserver)) {
            int count = roomMemberDao.countByRidAndExcludeRole(roomId, RoleEnum.RoleObserver.getValue());
            if (count == roomProperties.getMaxCount()) {
                log.error("assign error: roomId = {}, userId = {}, role = {}", roomId, targetUser.getRid(), targetUser.getRole());
                throw new ApiException(ErrorEnum.ERR_OVER_MAX_COUNT);
            }
        } else if (targetRole > targetUser.getRole()) {
            throw new ApiException(ErrorEnum.ERR_DOWNGRADE_ROLE);
        }
    }

    @DeclarePermissions(RoleEnum.RoleAdmin)
    @Override
    public Boolean transfer(String roomId, String userId, JwtUser jwtUser) throws ApiException, Exception {
        CheckUtils.checkArgument(roomId != null, "roomId must't be null");
        CheckUtils.checkArgument(userId != null, "userId must't be null");
        CheckUtils.checkArgument(!userId.equals(jwtUser.getUserId()), "can't set self role");

        log.info("transfer: roomId = {}, userId = {}, {}", roomId, userId, jwtUser);
        List<RoomMember> roomMemberList = roomMemberDao.findByRidAndUid(roomId, userId);
        if (roomMemberList.size() == 0) {
            log.error("admin transfer error: {} toUser = {}, opUser={}", roomId, userId, jwtUser.getUserId());
            throw new ApiException(ErrorEnum.ERR_USER_NOT_EXIST_IN_ROOM);
        }

        List<Room> roomList = roomDao.findByRid(roomId);
        if (roomList.size() == 0) {
            log.error("admin transfer error: {} toUser = {}, opUser={}", roomId, userId, jwtUser.getUserId());
            throw new ApiException(ErrorEnum.ERR_ROOM_NOT_EXIST);
        }

        if (isUserDisplay(roomList.get(0), jwtUser.getUserId()) || isUserDisplay(roomList.get(0), userId)) {
            updateDisplay(roomId, jwtUser.getUserId(), "", 1);
        } else {
            log.info("don't update display: room={}", roomList.get(0));
        }

        roomMemberDao.updateRoleByRidAndUid(roomId, jwtUser.getUserId(), RoleEnum.RoleParticipant.getValue());
        roomMemberDao.updateRoleByRidAndUid(roomId, userId, RoleEnum.RoleAdmin.getValue());

        AdminTransferMessage msg = new AdminTransferMessage();
        msg.setOpUserId(jwtUser.getUserId());
        msg.setToUserId(userId);
        IMApiResultInfo resultInfo = imHelper.publishMessage(jwtUser.getUserId(), roomId, msg, 1);
        if (resultInfo.isSuccess()) {
            return true;
        } else {
            throw new ApiException(ErrorEnum.ERR_MESSAGE_ERROR, resultInfo.getErrorMessage());
        }
    }

    @DeclarePermissions(RoleEnum.RoleAdmin)
    @Override
    public Boolean inviteUpgradeRole(String roomId, String targetUserId, int targetRole, JwtUser jwtUser) throws ApiException, Exception {
        CheckUtils.checkArgument(roomId != null, "roomId must't be null");
        CheckUtils.checkArgument(targetUserId != null, "userId must't be null");
        CheckUtils.checkArgument(!targetUserId.equals(jwtUser.getUserId()), "can't set self role");
        CheckUtils.checkArgument(roomMemberDao.existsByRidAndUid(roomId, targetUserId), "room member not exist");

        log.info("inviteUpgradeRole roomId = {}, targetUserId = {}, targetRole = {}, jwtUser={}", roomId, targetUserId, targetRole, jwtUser);

        List<RoomMember> targetUser = roomMemberDao.findByRidAndUid(roomId, targetUserId);
        if (targetUser.isEmpty()) {
            throw new ApiException(ErrorEnum.ERR_USER_NOT_EXIST_IN_ROOM);
        }

        checkOverMax(roomId, targetUser.get(0), targetRole);

        String ticket = IdentifierUtils.uuid();

        UpgradeRoleTaskInfo taskInfo = new UpgradeRoleTaskInfo();
        taskInfo.setTicket(ticket);
        taskInfo.setRoomId(roomId);
        taskInfo.setApplyUserId(jwtUser.getUserId());
        taskInfo.setTargetUserId(targetUserId);
        taskInfo.setRole(RoleEnum.getEnumByValue(targetRole));
        scheduleManager.addTask(taskInfo);

        UpgradeRoleMessage msg = new UpgradeRoleMessage(ActionEnum.Invite.ordinal());
        msg.setTicket(ticket);
        msg.setOpUserId(jwtUser.getUserId());
        msg.setOpUserName(jwtUser.getUserName());
        msg.setRole(targetRole);
        IMApiResultInfo resultInfo = imHelper.publishMessage(jwtUser.getUserId(), targetUserId, roomId, msg);
        if (resultInfo.isSuccess()) {
            return true;
        } else {
            throw new ApiException(ErrorEnum.ERR_MESSAGE_ERROR, resultInfo.getErrorMessage());
        }
    }

    @DeclarePermissions({RoleEnum.RoleObserver})
    @Override
    public Boolean approveUpgradeRole(String roomId, String ticket, JwtUser jwtUser) throws ApiException, Exception {
        CheckUtils.checkArgument(roomId != null, "roomId must't be null");
        CheckUtils.checkArgument(ticket != null, "ticket must't be null");
        CheckUtils.checkArgument(roomDao.existsByRid(roomId), "room not exist");
        CheckUtils.checkArgument(roomMemberDao.existsByRidAndUid(roomId, jwtUser.getUserId()), "room member not exist");

        UpgradeRoleTaskInfo taskInfo = (UpgradeRoleTaskInfo) scheduleManager.executeTask(ticket);
        log.info("approveUpgradeRole roomId = {}, task={}, jwtUser={}", roomId, taskInfo, jwtUser);

        List<RoomMember> targetUser = roomMemberDao.findByRidAndUid(roomId, jwtUser.getUserId());
        if (targetUser.isEmpty()) {
            throw new ApiException(ErrorEnum.ERR_USER_NOT_EXIST_IN_ROOM);
        }
        if (!taskInfo.getTargetUserId().equals(jwtUser.getUserId())) {
            throw new ApiException(ErrorEnum.ERR_APPLY_TICKET_INVALID);
        }

        checkOverMax(roomId, targetUser.get(0), taskInfo.getRole().getValue());
        roomMemberDao.updateRoleByRidAndUid(roomId, jwtUser.getUserId(), taskInfo.getRole().getValue());

        UpgradeRoleMessage msg = new UpgradeRoleMessage(ActionEnum.Approve.ordinal());
        msg.setOpUserName(jwtUser.getUserName());
        msg.setOpUserId(jwtUser.getUserId());
        msg.setRole(taskInfo.getRole().getValue());
        IMApiResultInfo resultInfo = imHelper.publishMessage(jwtUser.getUserId(), taskInfo.getApplyUserId(), roomId, msg);
        if (!resultInfo.isSuccess()) {
            throw new ApiException(ErrorEnum.ERR_MESSAGE_ERROR, resultInfo.getErrorMessage());
        }

        RoleChangedMessage rcMsg = new RoleChangedMessage(jwtUser.getUserId());
        List<RoleChangedMessage.ChangedUser> changedUserList = new ArrayList<>();
        RoleChangedMessage.ChangedUser user = new RoleChangedMessage.ChangedUser(jwtUser.getUserId(), taskInfo.getRole().getValue());
        user.setUserName(jwtUser.getUserName());
        changedUserList.add(user);
        rcMsg.setUsers(changedUserList);
        imHelper.publishMessage(jwtUser.getUserId(), roomId, rcMsg, 1);

        return true;
    }

    @DeclarePermissions({RoleEnum.RoleObserver, RoleEnum.RoleParticipant})
    @Override
    public Boolean rejectUpgradeRole(String roomId, String ticket, JwtUser jwtUser) throws ApiException, Exception {
        CheckUtils.checkArgument(roomId != null, "roomId must't be null");
        CheckUtils.checkArgument(ticket != null, "ticket must't be null");
        CheckUtils.checkArgument(roomDao.existsByRid(roomId), "room not exist");
        CheckUtils.checkArgument(roomMemberDao.existsByRidAndUid(roomId, jwtUser.getUserId()), "room member not exist");

        UpgradeRoleTaskInfo taskInfo = (UpgradeRoleTaskInfo) scheduleManager.executeTask(ticket);
        UpgradeRoleMessage msg = new UpgradeRoleMessage(ActionEnum.Reject.ordinal());
        msg.setOpUserName(jwtUser.getUserName());
        msg.setOpUserId(jwtUser.getUserId());
        msg.setRole(taskInfo.getRole().getValue());
        IMApiResultInfo resultInfo = imHelper.publishMessage(jwtUser.getUserId(), taskInfo.getApplyUserId(), roomId, msg);
        if (resultInfo.isSuccess()) {
            return true;
        } else {
            throw new ApiException(ErrorEnum.ERR_MESSAGE_ERROR, resultInfo.getErrorMessage());
        }
    }

    @DeclarePermissions(RoleEnum.RoleAdmin)
    @Override
    public Boolean changeRole(String roomId, String targetUserId, int targetRole, JwtUser jwtUser) throws ApiException, Exception {
        CheckUtils.checkArgument(roomId != null, "roomId must't be null");
        CheckUtils.checkArgument(targetUserId != null, "userId must't be null");
        CheckUtils.checkArgument(roomDao.existsByRid(roomId), "room not exist");
        CheckUtils.checkArgument(RoleEnum.getEnumByValue(targetRole).equals(RoleEnum.RoleSpeaker), "only set to speaker");
        CheckUtils.checkArgument(roomMemberDao.existsByRidAndUid(roomId, targetUserId), "room member not exist");

        List<RoomMember> targetUser = roomMemberDao.findByRidAndUid(roomId, targetUserId);
        if (targetUser.isEmpty()) {
            throw new ApiException(ErrorEnum.ERR_USER_NOT_EXIST_IN_ROOM);
        } else {
            if (!RoleEnum.getEnumByValue(targetUser.get(0).getRole()).equals(RoleEnum.RoleParticipant)) {
                log.error("change role error: {}, targetUserId={}, targetRole = {}", jwtUser, targetUser, RoleEnum.getEnumByValue(targetRole));
                throw new ApiException(ErrorEnum.ERR_CHANGE_ROLE);
            }
        }

        log.info("changeRole: roomId={}, {}, targetUserId={}", roomId, jwtUser, targetUserId);
        List<RoleChangedMessage.ChangedUser> changedUserList = new ArrayList<>();
        RoleChangedMessage msg = new RoleChangedMessage(jwtUser.getUserId());

        List<RoomMember> speakers = roomMemberDao.findByRidAndRole(roomId, RoleEnum.RoleSpeaker.getValue());
        if (!speakers.isEmpty()) {
            roomMemberDao.updateRoleByRidAndUid(roomId, speakers.get(0).getUid(), RoleEnum.RoleParticipant.getValue());
            RoleChangedMessage.ChangedUser user = new RoleChangedMessage.ChangedUser(speakers.get(0).getUid(), RoleEnum.RoleParticipant.getValue());
            List<UserInfo> userInfoList = userDao.findByUid(speakers.get(0).getUid());
            if (!userInfoList.isEmpty()) {
                user.setUserName(userInfoList.get(0).getName());
            }
            changedUserList.add(user);
        } else {
            log.info("change directly cause no speaker exist in room, roomId={}", roomId);
        }

        roomMemberDao.updateRoleByRidAndUid(roomId, targetUserId, targetRole);
        RoleChangedMessage.ChangedUser user = new RoleChangedMessage.ChangedUser(targetUserId, targetRole);
        List<UserInfo> userInfoList = userDao.findByUid(targetUserId);
        if (!userInfoList.isEmpty()) {
            user.setUserName(userInfoList.get(0).getName());
        }
        changedUserList.add(user);
        msg.setUsers(changedUserList);
        imHelper.publishMessage(jwtUser.getUserId(), roomId, msg, 1);

        String display = "display://type=1?userId=" + targetUserId + "?uri=";
        DisplayMessage displayMessage = new DisplayMessage(display);
        roomDao.updateDisplayByRid(roomId, display);
        imHelper.publishMessage(jwtUser.getUserId(), roomId, displayMessage, 1);
        log.info("changeRole, display changed: roomId={}, {}, targetUserId={}", roomId, display, targetUserId);

        return true;
    }

    @Override
    public Boolean memberOnlineStatus(List<ReqMemberOnlineStatus> statusList, String nonce, String timestamp, String signature) throws ApiException, Exception {
        String sign = imProperties.getSecret() + nonce + timestamp;
        String signSHA1 = CodeUtil.hexSHA1(sign);
        if (!signSHA1.equals(signature)) {
            log.info("memberOnlineStatus signature error");
            return true;
        }

        for (ReqMemberOnlineStatus status : statusList) {
            int s = Integer.parseInt(status.getStatus());
            String userId = status.getUserId();

            log.info("memberOnlineStatus, userId={}, status={}", userId, status);
            //1：offline 离线； 0: online 在线
            if (s == 1) {
                List<RoomMember> members = roomMemberDao.findByUid(userId);
                if (!members.isEmpty()) {
                    scheduleManager.userIMOffline(userId);
                }
            } else if (s == 0) {
                scheduleManager.userIMOnline(userId);
            }
        }

        return true;
    }

    @Override
    public void userIMOfflineKick(String userId) {
        List<RoomMember> members = roomMemberDao.findByUid(userId);
        for (RoomMember member : members) {
            int userRole = member.getRole();
            log.info("userIMOfflineKick: roomId={}, {}, role={}", member.getRid(), userId, RoleEnum.getEnumByValue(userRole));
            try {
                if (userRole == RoleEnum.RoleSpeaker.getValue() || userRole == RoleEnum.RoleAdmin.getValue()) {
                    List<Room> rooms = roomDao.findByRid(member.getRid());
                    if (rooms.isEmpty()) {
                        break;
                    }
                    if (isUserDisplay(rooms.get(0), member.getUid())) {
                        updateDisplay(member.getRid(), member.getUid(), "", 0);
                        log.info("memberOnlineStatus offline: roomId={}, {}", member.getRid(), member.getUid());
                    }
                }
                if (roomMemberDao.countByRid(member.getRid()) == 1) {
                    IMApiResultInfo apiResultInfo = null;
                    apiResultInfo = imHelper.dismiss(member.getUid(), member.getRid());
                    if (apiResultInfo.getCode() == 200) {
                        roomMemberDao.deleteUserByRidAndUid(member.getRid(), member.getUid());
                        roomDao.deleteByRid(member.getRid());
                        deleteWhiteboardByUser(member.getRid(), member.getUid());
                        log.info("dismiss the room: {}", member.getRid());
                    } else {
                        log.error("{} exit {} room error: {}", member.getUid(), member.getRid(), apiResultInfo.getErrorMessage());
                    }
                } else {
                    IMApiResultInfo apiResultInfo = null;
                    apiResultInfo = imHelper.quit(new String[]{member.getUid()}, member.getRid());
                    if (apiResultInfo.isSuccess()) {
                        roomMemberDao.deleteUserByRidAndUid(member.getRid(), member.getUid());
                        MemberChangedMessage msg = new MemberChangedMessage(MemberChangedMessage.Action_Leave, member.getUid(), userRole);
                        msg.setUserName(member.getName());
                        imHelper.publishMessage(member.getUid(), member.getRid(), msg);
                        imHelper.quit(new String[]{member.getUid()}, member.getRid());
                        log.info("quit group: roomId={}, {}", member.getRid(), member.getUid());
                    } else {
                        log.error("{} exit {} room error: {}", member.getUid(), member.getRid(), apiResultInfo.getErrorMessage());
                    }
                }
                userDao.deleteByUid(member.getUid());
            } catch (Exception e) {
                log.error("userIMOfflineKick error: userId={}", userId);
            }
        }
    }

    private void updateDisplay(String roomId, String senderId, String display, Integer isIncludeSender) throws ApiException, Exception {
        roomDao.updateDisplayByRid(roomId, display);
        DisplayMessage displayMessage = new DisplayMessage(display);
        imHelper.publishMessage(senderId, roomId, displayMessage, isIncludeSender);
    }

    private boolean isSpeakerDisplay(Room room, String userId) {
        return !room.getDisplay().isEmpty() && room.getDisplay().contains("userId=" + userId);
    }

    private boolean isSpeakerDisplayWhiteboard(Room room, String userId) {
        return !room.getDisplay().isEmpty() && room.getDisplay().contains("userId=" + userId) && room.getDisplay().contains("type=2");
    }

    private boolean isAdminDisplay(Room room, String userId) {
        return !room.getDisplay().isEmpty() && room.getDisplay().contains("userId=" + userId);
    }

    private boolean isUserDisplay(Room room, String userId) {
        boolean result = false;
        if (!room.getDisplay().isEmpty() && room.getDisplay().contains("userId=" + userId)) {
            if (room.getDisplay().contains("type=0") || room.getDisplay().contains("type=1") || room.getDisplay().contains("type=3")) {
                result = true;
            }
        }
        return result;
    }
}

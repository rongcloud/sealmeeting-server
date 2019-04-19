package cn.rongcloud.service;

import cn.rongcloud.common.ApiException;
import cn.rongcloud.common.JwtUser;
import cn.rongcloud.pojo.DeviceTypeEnum;
import cn.rongcloud.pojo.ReqChangeUserRoleData;
import cn.rongcloud.pojo.RoomResult;

import java.util.List;

/**
 * Created by weiqinxiao on 2019/2/28.
 */
public interface RoomService {
    //everyone
    public RoomResult joinRoom(String userName, String roomId, boolean isObserver, boolean isDisableCamera, JwtUser jwtUser) throws ApiException, Exception;

    public Boolean leaveRoom(JwtUser jwtUser, String roomId) throws ApiException, Exception;

    //only host
    public Boolean downgrade(String roomId, JwtUser jwtUser, List<ReqChangeUserRoleData.ChangedUser> users) throws ApiException, Exception;

    public Boolean kickMember(String roomId, String userId, JwtUser jwtUser) throws ApiException, Exception;


    //only speaker
    public Boolean display(String roomId, int type, String userId, String uri, JwtUser jwtUser) throws ApiException, Exception;

    public String createWhiteBoard(String roomId, JwtUser jwtUser) throws ApiException, Exception;

    public Boolean deleteWhiteboard(String roomId, JwtUser jwtUser, String whiteBoardId) throws ApiException, Exception;

    public List<RoomResult.WhiteboardResult> getWhiteboard(String roomId, JwtUser jwtUser) throws ApiException, Exception;

    public Boolean turnWhiteBoardPage(String roomId, String whiteBoardId, int page, JwtUser jwtUser) throws ApiException, Exception;

    public Boolean controlDevice(String roomId, String userId, DeviceTypeEnum type, boolean enable, JwtUser jwtUser) throws ApiException, Exception;

    public Boolean approveControlDevice(String roomId, String ticket, JwtUser jwtUser) throws ApiException, Exception;

    public Boolean rejectControlDevice(String roomId, String ticket, JwtUser jwtUser) throws ApiException, Exception;


    public List<RoomResult.MemberResult> getMembers(String roomId, JwtUser jwtUser) throws  ApiException, Exception;

    public Boolean applySpeech(String roomId, JwtUser jwtUser) throws  ApiException, Exception;

    public Boolean approveSpeech(String roomId, String requestId, JwtUser jwtUser) throws  ApiException, Exception;

    public Boolean rejectSpeech(String roomId, String requestId, JwtUser jwtUser) throws  ApiException, Exception;


    public Boolean transfer(String roomId, String userId, JwtUser jwtUser) throws ApiException, Exception;

    public Boolean inviteUpgradeRole(String roomId, String userId, int role, JwtUser jwtUser) throws  ApiException, Exception;
    public Boolean approveUpgradeRole(String roomId, String ticket, JwtUser jwtUser) throws ApiException, Exception;
    public Boolean rejectUpgradeRole(String roomId, String ticket, JwtUser jwtUser) throws ApiException, Exception;

    public Boolean syncDeviceState(String roomId, DeviceTypeEnum type, boolean enable, JwtUser jwtUser) throws ApiException, Exception;

    public Boolean changeRole(String roomId, String userId, int role, JwtUser jwtUser) throws ApiException, Exception;

    public void destroyRoom(String roomId);
}

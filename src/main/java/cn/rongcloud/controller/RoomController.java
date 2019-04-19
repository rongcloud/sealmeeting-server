package cn.rongcloud.controller;

import cn.rongcloud.common.ApiException;
import cn.rongcloud.common.BaseResponse;
import cn.rongcloud.common.ErrorEnum;
import cn.rongcloud.common.JwtUser;
import cn.rongcloud.filter.JwtFilter;
import cn.rongcloud.pojo.*;
import cn.rongcloud.service.RoomService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Created by weiqinxiao on 2019/2/25.
 */
@RestController
@RequestMapping("/room")
public class RoomController {
    @Autowired
    RoomService roomService;

    @RequestMapping(value = "/join", method = RequestMethod.POST)
    public BaseResponse<RoomResult> joinRoom(@RequestBody ReqUserData data,
                                             @RequestAttribute(value = JwtFilter.JWT_AUTH_DATA, required = false) JwtUser jwtUser)
            throws ApiException, Exception {
        RoomResult roomResult = roomService.joinRoom(data.getUserName(), data.getRoomId(), data.isObserver(), data.isDisableCamera(), jwtUser);
        return new BaseResponse<>(roomResult);
    }

    @RequestMapping(value = "/leave", method = RequestMethod.POST)
    public BaseResponse<Boolean> leaveRoom(@RequestBody ReqUserData data,
                                           @RequestAttribute(value = JwtFilter.JWT_AUTH_DATA, required = false) JwtUser jwtUser)
            throws ApiException, Exception {
        boolean result = roomService.leaveRoom(jwtUser, data.getRoomId());
        return new BaseResponse<>(result);
    }

    @RequestMapping(value = "/downgrade", method = RequestMethod.POST)
    public BaseResponse<Boolean> downRole(@RequestBody ReqChangeUserRoleData data,
                                          @RequestAttribute(value = JwtFilter.JWT_AUTH_DATA, required = false) JwtUser jwtUser)
            throws ApiException, Exception {
        boolean result = roomService.downgrade(data.getRoomId(), jwtUser, data.getUsers());
        return new BaseResponse<>(result);
    }

    @RequestMapping(value = "/kick", method = RequestMethod.POST)
    public BaseResponse<Boolean> kickMember(@RequestBody ReqUserData data,
                                            @RequestAttribute(value = JwtFilter.JWT_AUTH_DATA, required = false) JwtUser jwtUser)
            throws ApiException, Exception {
        boolean result = roomService.kickMember(data.getRoomId(), data.getUserId(), jwtUser);
        return new BaseResponse<>(result);
    }

    //only speaker
    @RequestMapping(value = "/display", method = RequestMethod.POST)
    public BaseResponse<Boolean> display(@RequestBody ReqDisplayData data,
                                         @RequestAttribute(value = JwtFilter.JWT_AUTH_DATA, required = false) JwtUser jwtUser)
            throws ApiException, Exception {
        boolean result = roomService.display(data.getRoomId(), data.getType(), data.getUserId(), data.getUri(), jwtUser);
        return new BaseResponse<>(result);
    }

    @RequestMapping(value = "/whiteboard/create", method = RequestMethod.POST)
    public BaseResponse<String> createWhiteBoard(@RequestBody ReqWhiteboardData data,
                                                 @RequestAttribute(value = JwtFilter.JWT_AUTH_DATA, required = false) JwtUser jwtUser)
            throws ApiException, Exception {
        String result = roomService.createWhiteBoard(data.getRoomId(), jwtUser);
        return new BaseResponse<>(result);
    }


    @RequestMapping(value = "/whiteboard/delete", method = RequestMethod.POST)
    public BaseResponse<Boolean> destroyWhiteBoard(@RequestBody ReqWhiteboardData data,
                                                   @RequestAttribute(value = JwtFilter.JWT_AUTH_DATA, required = false) JwtUser jwtUser)
            throws ApiException, Exception {
        boolean result = roomService.deleteWhiteboard(data.getRoomId(), jwtUser, data.getWhiteboardId());
        return new BaseResponse<>(result);
    }

    @RequestMapping(value = "/whiteboard/list", method = RequestMethod.GET)
    public BaseResponse<List<RoomResult.WhiteboardResult>> getWhiteBoard(@RequestParam String roomId,
                                                                         @RequestAttribute(value = JwtFilter.JWT_AUTH_DATA, required = false) JwtUser jwtUser)
            throws ApiException, Exception {
        List<RoomResult.WhiteboardResult> whiteboards = roomService.getWhiteboard(roomId, jwtUser);
        return new BaseResponse<>(whiteboards);
    }

    @RequestMapping(value = "/device/approve", method = RequestMethod.POST)
    public BaseResponse<Boolean> approveControlDevice(@RequestBody ReqDeviceControlData data,
                                                      @RequestAttribute(value = JwtFilter.JWT_AUTH_DATA, required = false) JwtUser jwtUser)
            throws ApiException, Exception {
        boolean result;
        result = roomService.approveControlDevice(data.getRoomId(), data.getTicket(), jwtUser);
        return new BaseResponse<>(result);
    }

    @RequestMapping(value = "/device/reject", method = RequestMethod.POST)
    public BaseResponse<Boolean> rejectControlDevice(@RequestBody ReqDeviceControlData data,
                                                      @RequestAttribute(value = JwtFilter.JWT_AUTH_DATA, required = false) JwtUser jwtUser)
            throws ApiException, Exception {
        boolean result;
        result = roomService.rejectControlDevice(data.getRoomId(), data.getTicket(), jwtUser);
        return new BaseResponse<>(result);
    }

    @RequestMapping(value = "/device/control", method = RequestMethod.POST)
    public BaseResponse<Boolean> controlDevice(@RequestBody ReqDeviceControlData data,
                                               @RequestAttribute(value = JwtFilter.JWT_AUTH_DATA, required = false) JwtUser jwtUser)
            throws ApiException, Exception {
        boolean result;
        if (data.getCameraOn() != null) {
            result = roomService.controlDevice(data.getRoomId(), data.getUserId(), DeviceTypeEnum.Camera, data.getCameraOn(), jwtUser);
        } else if (data.getMicrophoneOn() != null) {
            result = roomService.controlDevice(data.getRoomId(), data.getUserId(), DeviceTypeEnum.Microphone, data.getMicrophoneOn(), jwtUser);
        } else {
            throw new ApiException(ErrorEnum.ERR_REQUEST_PARA_ERR);
        }
        return new BaseResponse<>(result);
    }

    @RequestMapping(value = "/device/sync", method = RequestMethod.POST)
    public BaseResponse<Boolean> syncDeviceState(@RequestBody ReqDeviceControlData data,
                                                 @RequestAttribute(value = JwtFilter.JWT_AUTH_DATA, required = false) JwtUser jwtUser)
            throws ApiException, Exception {
        boolean result;
        if (data.getCameraOn() != null) {
            result = roomService.syncDeviceState(data.getRoomId(), DeviceTypeEnum.Camera, data.getCameraOn(), jwtUser);
        } else if (data.getMicrophoneOn() != null) {
            result = roomService.syncDeviceState(data.getRoomId(), DeviceTypeEnum.Microphone, data.getMicrophoneOn(), jwtUser);
        } else {
            throw new ApiException(ErrorEnum.ERR_REQUEST_PARA_ERR);
        }
        return new BaseResponse<>(result);
    }

    @RequestMapping(value = "/whiteboard/turn-page", method = RequestMethod.POST)
    public BaseResponse<Boolean> turnPage(@RequestBody ReqWhiteboardData data,
                                          @RequestAttribute(value = JwtFilter.JWT_AUTH_DATA, required = false) JwtUser jwtUser)
            throws ApiException, Exception {
        boolean result = roomService.turnWhiteBoardPage(data.getRoomId(), data.getWhiteboardId(), data.getPage(), jwtUser);
        return new BaseResponse<>(result);
    }

    @RequestMapping(value = "/members", method = RequestMethod.GET)
    public BaseResponse<List<RoomResult.MemberResult>> getMembers(@RequestParam String roomId,
                                                                  @RequestAttribute(value = JwtFilter.JWT_AUTH_DATA, required = false) JwtUser jwtUser)
            throws ApiException, Exception {
        List<RoomResult.MemberResult> whiteboards = roomService.getMembers(roomId, jwtUser);
        return new BaseResponse<>(whiteboards);
    }

    @RequestMapping(value = "/speech/apply", method = RequestMethod.POST)
    public BaseResponse<Boolean> apply(@RequestBody ReqSpeechData data,
                                       @RequestAttribute(value = JwtFilter.JWT_AUTH_DATA, required = false) JwtUser jwtUser)
            throws ApiException, Exception {
        Boolean result = roomService.applySpeech(data.getRoomId(), jwtUser);
        return new BaseResponse<>(result);
    }

    @RequestMapping(value = "/speech/approve", method = RequestMethod.POST)
    public BaseResponse<Boolean> approval(@RequestBody ReqSpeechData data,
                                          @RequestAttribute(value = JwtFilter.JWT_AUTH_DATA, required = false) JwtUser jwtUser)
            throws ApiException, Exception {
        Boolean result = roomService.approveSpeech(data.getRoomId(), data.getTicket(), jwtUser);
        return new BaseResponse<>(result);
    }

    @RequestMapping(value = "/speech/reject", method = RequestMethod.POST)
    public BaseResponse<Boolean> reject(@RequestBody ReqSpeechData data,
                                        @RequestAttribute(value = JwtFilter.JWT_AUTH_DATA, required = false) JwtUser jwtUser)
            throws ApiException, Exception {
        Boolean result = roomService.rejectSpeech(data.getRoomId(), data.getTicket(), jwtUser);
        return new BaseResponse<>(result);
    }

    @RequestMapping(value = "/transfer", method = RequestMethod.POST)
    public BaseResponse<Boolean> transfer(@RequestBody ReqUpgradeRoleData data,
                                          @RequestAttribute(value = JwtFilter.JWT_AUTH_DATA, required = false) JwtUser jwtUser)
            throws ApiException, Exception {
        Boolean result = roomService.transfer(data.getRoomId(), data.getUserId(), jwtUser);
        return new BaseResponse<>(result);
    }

    @RequestMapping(value = "/upgrade/invite", method = RequestMethod.POST)
    public BaseResponse<Boolean> inviteUpgradeRole(@RequestBody ReqUpgradeRoleData data,
                                                   @RequestAttribute(value = JwtFilter.JWT_AUTH_DATA, required = false) JwtUser jwtUser)
            throws ApiException, Exception {
        Boolean result = roomService.inviteUpgradeRole(data.getRoomId(), data.getUserId(), data.getRole(), jwtUser);
        return new BaseResponse<>(result);
    }

    @RequestMapping(value = "/upgrade/approve", method = RequestMethod.POST)
    public BaseResponse<Boolean> approveUpgradeRole(@RequestBody ReqUpgradeRoleData data,
                                                    @RequestAttribute(value = JwtFilter.JWT_AUTH_DATA, required = false) JwtUser jwtUser)
            throws ApiException, Exception {
        Boolean result = roomService.approveUpgradeRole(data.getRoomId(), data.getTicket(), jwtUser);
        return new BaseResponse<>(result);
    }

    @RequestMapping(value = "/upgrade/reject", method = RequestMethod.POST)
    public BaseResponse<Boolean> rejectUpgradeRole(@RequestBody ReqUpgradeRoleData data,
                                                   @RequestAttribute(value = JwtFilter.JWT_AUTH_DATA, required = false) JwtUser jwtUser)
            throws ApiException, Exception {
        Boolean result = roomService.rejectUpgradeRole(data.getRoomId(), data.getTicket(), jwtUser);
        return new BaseResponse<>(result);
    }

    @RequestMapping(value = "/change-role", method = RequestMethod.POST)
    public BaseResponse<Boolean> changeRole(@RequestBody ReqChangeRole data,
                                            @RequestAttribute(value = JwtFilter.JWT_AUTH_DATA, required = false) JwtUser jwtUser)
            throws ApiException, Exception {
        Boolean result = roomService.changeRole(data.getRoomId(), data.getUserId(), data.getRole(), jwtUser);
        return new BaseResponse<>(result);
    }
}

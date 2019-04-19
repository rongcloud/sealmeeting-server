package cn.rongcloud.pojo;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by weiqinxiao on 2019/2/28.
 */
public class RoomResult {
    private @Getter @Setter String roomId;
    private @Setter Date startTime;
    private @Getter @Setter String imToken;
    private @Getter @Setter String authorization;
    private @Getter List<MemberResult> members = new ArrayList<>();
    private @Getter @Setter String display;
    private @Getter @Setter List<WhiteboardResult> whiteboards = new ArrayList<>();
    private @Getter @Setter MemberResult userInfo;

    @Data
    public static class MemberResult {
        String userId;
        String userName;
        int role;
        Date joinTime;
        boolean camera;
        boolean microphone;
    }

    @Data
    public static class WhiteboardResult {
        String whiteboardId;
        String name;
        int curPg;
    }

    public void setMembers(List<RoomMember> roomMemberList) {
        for (RoomMember member : roomMemberList) {
            MemberResult result = new MemberResult();
            result.setUserId(member.getUid());
            result.setJoinTime(member.getJoinDt());
            result.setRole(member.getRole());
            result.setMicrophone(member.isMic());
            result.setCamera(member.isCamera());
            result.setUserName(member.getName());
            members.add(result);
        }
    }

    public void setWhiteboards(List<Whiteboard> whiteboardList) {
        for (Whiteboard wb : whiteboardList) {
            WhiteboardResult r = new WhiteboardResult();
            r.setName(wb.getName());
            r.setWhiteboardId(wb.getWbid());
            r.setCurPg(wb.getCurPg());
            whiteboards.add(r);
        }
    }
}

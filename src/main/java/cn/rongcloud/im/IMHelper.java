package cn.rongcloud.im;

import cn.rongcloud.http.HttpHelper;
import cn.rongcloud.pojo.IMApiResultInfo;
import cn.rongcloud.pojo.IMTokenInfo;
import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.net.HttpURLConnection;
import java.net.URLEncoder;

/**
 * Created by weiqinxiao on 2019/2/28.
 */
@Slf4j
@Component
public class IMHelper {
    private static final String UTF8 = "UTF-8";

    @Autowired
    HttpHelper httpHelper;

    /**
     * 获取 Token 方法
     *
     * @param userId:用户 Id，最大长度 64 字节.是用户在 App 中的唯一标识码，必须保证在同一个 App 内不重复，重复的用户 Id 将被当作是同一用户。（必传）
     * @param name:用户名称，最大长度 128 字节.用来在 Push 推送时显示用户的名称.用户名称，最大长度 128 字节.用来在 Push 推送时显示用户的名称。（必传）
     * @param portraitUri:用户头像 URI，最大长度 1024 字节.用来在 Push 推送时显示用户的头像。（必传）
     * @return TokenResult
     **/
    public IMTokenInfo getToken(String userId, String name, String portraitUri) throws Exception {
        if (userId == null) {
            throw new IllegalArgumentException("Paramer 'userId' is required");
        }

        if (name == null) {
            throw new IllegalArgumentException("Paramer 'name' is required");
        }

        if (portraitUri == null) {
            throw new IllegalArgumentException("Paramer 'portraitUri' is required");
        }

        StringBuilder sb = new StringBuilder();
        sb.append("&userId=").append(URLEncoder.encode(userId.toString(), UTF8));
        sb.append("&name=").append(URLEncoder.encode(name.toString(), UTF8));
        sb.append("&portraitUri=").append(URLEncoder.encode(portraitUri.toString(), UTF8));
        String body = sb.toString();
        if (body.indexOf("&") == 0) {
            body = body.substring(1, body.length());
        }

        HttpURLConnection conn = httpHelper.createIMPostHttpConnection("/user/getToken.json", "application/x-www-form-urlencoded");
        httpHelper.setBodyParameter(body, conn);

        return JSON.parseObject(httpHelper.returnResult(conn, body), IMTokenInfo.class);
    }

    /**
     * 创建群组方法（创建群组，并将用户加入该群组，用户将可以收到该群的消息，同一用户最多可加入 500 个群，每个群最大至 3000 人，App
     * 内的群组数量没有限制.注：其实本方法是加入群组方法 /group/join 的别名。）
     *
     * @param userId:要加入群的用户 Id。（必传）
     * @param groupId:创建群组 Id。（必传）
     * @param groupName:群组 Id 对应的名称。（必传）
     * @return CodeSuccessResult
     **/
    public IMApiResultInfo createGroup(String[] userId, String groupId, String groupName)
            throws Exception {
        if (userId == null) {
            throw new IllegalArgumentException("Paramer 'userId' is required");
        }

        if (groupId == null) {
            throw new IllegalArgumentException("Paramer 'groupId' is required");
        }

        if (groupName == null) {
            throw new IllegalArgumentException("Paramer 'groupName' is required");
        }

        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < userId.length; i++) {
            String child = userId[i];
            sb.append("&userId=").append(URLEncoder.encode(child, UTF8));
        }

        sb.append("&groupId=").append(URLEncoder.encode(groupId.toString(), UTF8));
        sb.append("&groupName=").append(URLEncoder.encode(groupName.toString(), UTF8));
        String body = sb.toString();
        if (body.indexOf("&") == 0) {
            body = body.substring(1, body.length());
        }

        HttpURLConnection conn = httpHelper
                .createIMPostHttpConnection("/group/create.json", "application/x-www-form-urlencoded");
        httpHelper.setBodyParameter(body, conn);

        return JSON.parseObject(httpHelper.returnResult(conn, body), IMApiResultInfo.class);
    }


    /**
     * 将用户加入指定群组，用户将可以收到该群的消息，同一用户最多可加入 500 个群，每个群最大至 3000 人。
     *
     * @param userId:要加入群的用户 Id，可提交多个，最多不超过 1000 个。（必传）
     * @param groupId:要加入的群 Id。（必传）
     * @param groupName:要加入的群 Id 对应的名称。（必传）
     * @return CodeSuccessResult
     **/
    public IMApiResultInfo joinGroup(String[] userId, String groupId, String groupName)
            throws Exception {
        if (userId == null) {
            throw new IllegalArgumentException("Paramer 'userId' is required");
        }

        if (groupId == null) {
            throw new IllegalArgumentException("Paramer 'groupId' is required");
        }

        if (groupName == null) {
            throw new IllegalArgumentException("Paramer 'groupName' is required");
        }

        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < userId.length; i++) {
            String child = userId[i];
            sb.append("&userId=").append(URLEncoder.encode(child, UTF8));
        }

        sb.append("&groupId=").append(URLEncoder.encode(groupId.toString(), UTF8));
        sb.append("&groupName=").append(URLEncoder.encode(groupName.toString(), UTF8));
        String body = sb.toString();
        if (body.indexOf("&") == 0) {
            body = body.substring(1, body.length());
        }

        HttpURLConnection conn = httpHelper
                .createIMPostHttpConnection("/group/join.json", "application/x-www-form-urlencoded");
        httpHelper.setBodyParameter(body, conn);

        return JSON.parseObject(httpHelper.returnResult(conn, body), IMApiResultInfo.class);
    }



    /**
     * 退出群组方法（将用户从群中移除，不再接收该群组的消息.）
     *
     * @param userId:要退出群的用户 Id.（必传）
     * @param groupId:要退出的群 Id.（必传）
     * @return CodeSuccessResult
     **/
    public IMApiResultInfo quit(String[] userId, String groupId) throws Exception {
        if (userId == null) {
            throw new IllegalArgumentException("Paramer 'userId' is required");
        }

        if (groupId == null) {
            throw new IllegalArgumentException("Paramer 'groupId' is required");
        }

        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < userId.length; i++) {
            String child = userId[i];
            sb.append("&userId=").append(URLEncoder.encode(child, UTF8));
        }

        sb.append("&groupId=").append(URLEncoder.encode(groupId.toString(), UTF8));
        String body = sb.toString();
        if (body.indexOf("&") == 0) {
            body = body.substring(1, body.length());
        }

        HttpURLConnection conn = httpHelper
                .createIMPostHttpConnection("/group/quit.json", "application/x-www-form-urlencoded");
        httpHelper.setBodyParameter(body, conn);

        return JSON.parseObject(httpHelper.returnResult(conn, body), IMApiResultInfo.class);
    }


    /**
     * 解散群组方法。（将该群解散，所有用户都无法再接收该群的消息。）
     *
     * @param userId:操作解散群的用户 Id。（必传）
     * @param groupId:要解散的群 Id。（必传）
     * @return CodeSuccessResult
     **/
    public IMApiResultInfo dismiss(String userId, String groupId) throws Exception {
        if (userId == null) {
            throw new IllegalArgumentException("Paramer 'userId' is required");
        }

        if (groupId == null) {
            throw new IllegalArgumentException("Paramer 'groupId' is required");
        }

        StringBuilder sb = new StringBuilder();
        sb.append("&userId=").append(URLEncoder.encode(userId.toString(), UTF8));
        sb.append("&groupId=").append(URLEncoder.encode(groupId.toString(), UTF8));
        String body = sb.toString();
        if (body.indexOf("&") == 0) {
            body = body.substring(1, body.length());
        }

        HttpURLConnection conn = httpHelper
                .createIMPostHttpConnection("/group/dismiss.json", "application/x-www-form-urlencoded");
        httpHelper.setBodyParameter(body, conn);

        return JSON.parseObject(httpHelper.returnResult(conn, body), IMApiResultInfo.class);
    }


    /**
     * 发送群组消息方法（以一个用户身份向群组发送消息，单条消息最大 128k.每秒钟最多发送 20 条消息，每次最多向 3 个群组发送，如：一次向 3 个群组发送消息，示为 3 条消息。）
     *
     * @param fromUserId:发送人用户 Id 。（必传）
     * @param toGroupId:接收群Id，提供多个本参数可以实现向多群发送消息，最多不超过 3 个群组。（必传）
     * @param pushContent:定义显示的 Push 内容，如果 objectName 为融云内置消息类型时，则发送后用户一定会收到 Push 信息. 如果为自定义消息，则
     * pushContent 为自定义消息显示的 Push 内容，如果不传则用户不会收到 Push 通知。（可选）
     * @param pushData:针对 iOS 平台为 Push 通知时附加到 payload 中，Android 客户端收到推送消息时对应字段名为 pushData。（可选）
     * @param isPersisted:当前版本有新的自定义消息，而老版本没有该自定义消息时，老版本客户端收到消息后是否进行存储，0 表示为不存储、 1 表示为存储，默认为 1
     * 存储消息。（可选）
     * @param isCounted:当前版本有新的自定义消息，而老版本没有该自定义消息时，老版本客户端收到消息后是否进行未读消息计数，0 表示为不计数、 1 表示为计数，默认为 1
     * 计数，未读消息数增加 1。（可选）
     * @return CodeSuccessResult
     **/
    public IMApiResultInfo publishMessage(String fromUserId, String toGroupId, BaseMessage message) throws Exception {
        String[] toGroupIds = new String[1];
        toGroupIds[0] = toGroupId;
        return publishMessage(fromUserId, null, toGroupIds, message, "", "", 0,
                0, 0, 0, 0);
    }

    public IMApiResultInfo publishMessage(String fromUserId, String toGroupId, BaseMessage message, Integer isIncludeSender) throws Exception {
        String[] toGroupIds = new String[1];
        toGroupIds[0] = toGroupId;
        return publishMessage(fromUserId, null, toGroupIds, message, "", "", 0,
                0, isIncludeSender, 0, 0);
    }

    //定向消息 toUserId 指向对应的人
    public IMApiResultInfo publishMessage(String fromUserId, String toUserId, String toGroupId, BaseMessage message) throws Exception {
        String[] toGroupIds = new String[1];
        toGroupIds[0] = toGroupId;
        return publishMessage(fromUserId, toUserId, toGroupIds, message, "", "", 0,
                0, 0, 0, 0);
    }

    public IMApiResultInfo publishMessage(String fromUserId, String toUserId, String[] toGroupId,
                                          BaseMessage message, String pushContent, String pushData, Integer isPersisted,
                                          Integer isCounted, Integer isIncludeSender, Integer isStatus, Integer isMentioned)
            throws Exception {
        if (fromUserId == null) {
            throw new IllegalArgumentException("Paramer 'fromUserId' is required");
        }

        if (toGroupId == null) {
            throw new IllegalArgumentException("Paramer 'toGroupId' is required");
        }

        if (message == null) {
            throw new IllegalArgumentException("Paramer 'message' is required");
        }

        StringBuilder sb = new StringBuilder();
        sb.append("&fromUserId=").append(URLEncoder.encode(fromUserId.toString(), UTF8));

        //定向消息
        if (toUserId != null) {
            sb.append("&toUserId=").append(URLEncoder.encode(toUserId.toString(), UTF8));
        }

        for (int i = 0; i < toGroupId.length; i++) {
            String child = toGroupId[i];
            sb.append("&toGroupId=").append(URLEncoder.encode(child, UTF8));
        }

        String msgStr = message.toString();
        log.info("publish msg: {}", msgStr);
        sb.append("&objectName=").append(URLEncoder.encode(message.getObjectName(), UTF8));
        sb.append("&content=").append(URLEncoder.encode(msgStr, UTF8));

        if (pushContent != null) {
            sb.append("&pushContent=").append(URLEncoder.encode(pushContent.toString(), UTF8));
        }

        if (pushData != null) {
            sb.append("&pushData=").append(URLEncoder.encode(pushData.toString(), UTF8));
        }

        if (isPersisted != null) {
            sb.append("&isPersisted=").append(URLEncoder.encode(isPersisted.toString(), UTF8));
        }

        if (isCounted != null) {
            sb.append("&isCounted=").append(URLEncoder.encode(isCounted.toString(), UTF8));
        }

        if (isIncludeSender != null) {
            sb.append("&isIncludeSender=")
                    .append(URLEncoder.encode(isIncludeSender.toString(), UTF8));
        }

        if (isMentioned != null) {
            sb.append("&isMentioned=").append(URLEncoder.encode(isMentioned.toString(), UTF8));
        }

        String body = sb.toString();
        if (body.indexOf("&") == 0) {
            body = body.substring(1, body.length());
        }

        String url;
        if (isStatus != null && isStatus.intValue() == 1) {
            url = "/statusmessage/group/publish.json";
        } else {
            url = "/message/group/publish.json";
        }

        HttpURLConnection conn = httpHelper
                .createIMPostHttpConnection(url, "application/x-www-form-urlencoded");
        httpHelper.setBodyParameter(body, conn);

        return JSON.parseObject(httpHelper.returnResult(conn, body), IMApiResultInfo.class);
    }
}

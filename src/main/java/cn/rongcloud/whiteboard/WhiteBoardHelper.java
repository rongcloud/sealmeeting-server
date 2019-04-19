package cn.rongcloud.whiteboard;

import cn.rongcloud.config.IMProperties;
import cn.rongcloud.config.WhiteBoardProperties;
import cn.rongcloud.http.HttpHelper;
import cn.rongcloud.pojo.WhiteBoardApiResultInfo;
import com.alibaba.fastjson.JSON;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.net.HttpURLConnection;
import java.net.URLEncoder;

/**
 * Created by weiqinxiao on 2019/3/7.
 */
@Component
public class WhiteBoardHelper {
    private static final String UTF8 = "UTF-8";

    @Autowired
    HttpHelper httpHelper;

    @Autowired
    WhiteBoardProperties whiteBoardProperties;
    @Autowired
    IMProperties imProperties;

    public WhiteBoardApiResultInfo create(String roomId) throws Exception {
        if (roomId == null) {
            throw new IllegalArgumentException("Paramer 'roomId' is required");
        }

        StringBuilder sb = new StringBuilder();
        sb.append("&appId=").append(URLEncoder.encode(imProperties.getAppKey(), UTF8));
        sb.append("&roomNr=").append(URLEncoder.encode(roomId, UTF8));
        HttpURLConnection connection= httpHelper.createWhiteBoardPostHttpConnection(whiteBoardProperties.getHost(), "/room/create", "application/x-www-form-urlencoded");
        httpHelper.setBodyParameter(sb, connection);

        return JSON.parseObject(httpHelper.returnResult(connection, sb.toString()), WhiteBoardApiResultInfo.class);
    }

    public WhiteBoardApiResultInfo destroy(String roomId) throws Exception {
        if (roomId == null) {
            throw new IllegalArgumentException("Paramer 'roomId' is required");
        }

        StringBuilder sb = new StringBuilder();
        sb.append("&appId=").append(URLEncoder.encode(imProperties.getAppKey(), UTF8));
        sb.append("&roomNr=").append(URLEncoder.encode(roomId, UTF8));
        HttpURLConnection connection= httpHelper.createWhiteBoardPostHttpConnection(whiteBoardProperties.getHost(), "/room/destroy", "application/x-www-form-urlencoded");
        httpHelper.setBodyParameter(sb, connection);

        return JSON.parseObject(httpHelper.returnResult(connection, sb.toString()), WhiteBoardApiResultInfo.class);
    }
}

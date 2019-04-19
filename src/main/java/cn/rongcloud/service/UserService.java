package cn.rongcloud.service;

import cn.rongcloud.common.ApiException;
import cn.rongcloud.pojo.UserInfo;

/**
 * Created by weiqinxiao on 2019/2/25.
 */
public interface UserService {

    public String refreshToken(String userId, String name) throws ApiException, Exception;
}

package cn.rongcloud.service.Impl;

import cn.rongcloud.common.ApiException;
import cn.rongcloud.common.ErrorEnum;
import cn.rongcloud.im.IMHelper;
import cn.rongcloud.pojo.IMTokenInfo;
import cn.rongcloud.service.UserService;
import cn.rongcloud.dao.UserDao;
import cn.rongcloud.pojo.UserInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Created by weiqinxiao on 2019/2/25.
 */
@Slf4j
@Service
public class UserServiceImpl implements UserService {
    @Autowired
    UserDao userDao;

    @Autowired
    IMHelper imHelper;

    @Override
    public String refreshToken(String userId, String name) throws ApiException, Exception  {
        log.info("request token: {}, {}", userId, name);
        IMTokenInfo tokenInfo = imHelper.getToken(userId, name, "");
        if (tokenInfo.isSuccess()) {
            return tokenInfo.getToken();
        } else {
            throw new ApiException(ErrorEnum.ERR_IM_TOKEN_ERROR, tokenInfo.getErrorMessage());
        }
    }
}

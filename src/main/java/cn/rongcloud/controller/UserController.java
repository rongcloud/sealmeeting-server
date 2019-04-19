package cn.rongcloud.controller;

import cn.rongcloud.common.ApiException;
import cn.rongcloud.common.BaseResponse;
import cn.rongcloud.common.JwtUser;
import cn.rongcloud.filter.JwtFilter;
import cn.rongcloud.pojo.ReqUserData;
import cn.rongcloud.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * Created by weiqinxiao on 2019/2/25.
 */
@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    UserService userService;

    @RequestMapping(value = "/refresh-token", method = RequestMethod.POST)
    public BaseResponse<String> refreshToken(@RequestBody ReqUserData reqUserData,
                                             @RequestAttribute(value = JwtFilter.JWT_AUTH_DATA, required = false) JwtUser jwtUser)
            throws ApiException, Exception {
        String token = userService.refreshToken(reqUserData.getUserId(), reqUserData.getUserId());
        BaseResponse<String> response = new BaseResponse<>();
        response.setData(token);
        return response;
    }
}

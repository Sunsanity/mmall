package com.mmall.controller.backend;

import com.mmall.common.Const;
import com.mmall.common.ResponseCode;
import com.mmall.common.ServerResponse;
import com.mmall.pojo.User;
import com.mmall.service.IUserService;
import com.mmall.util.CookieUtil;
import com.mmall.util.JsonUtil;
import com.mmall.util.RedisShardedPoolUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import redis.clients.jedis.ShardedJedisPool;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * Created by hasee on 2017/5/21.
 */
@Controller
@RequestMapping("/manage/user")
public class UserManageController {

    @Autowired
    private IUserService iUserService;

    /**
     * 后台管理员用户登陆
     * @param username
     * @param password
     * @param session
     * @return
     */
    @RequestMapping(value = "login.do",method = RequestMethod.GET)
    @ResponseBody
    public ServerResponse<User> login(String username, String password, HttpSession session,HttpServletResponse httpServletResponse){
        ServerResponse<User> response = iUserService.login(username,password);
        if (response.isSuccess()){
            User user = response.getData();
            if (user.getRole().equals(Const.Role.ROLE_ADMIN)){
                //session.setAttribute(Const.CURRENT_USER,user);

                CookieUtil.writeLoginToken(httpServletResponse,session.getId());
                RedisShardedPoolUtil.setEx(session.getId(), JsonUtil.obj2String(user),Const.RedisCacheExtime.REDIS_SESSION_EXTIME);
                return response;
            }else{
                return ServerResponse.createByErrorMessage("非管理员用户，登陆失败！");
            }
        }
        return response;
    }

}

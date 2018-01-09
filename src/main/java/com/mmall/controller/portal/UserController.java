package com.mmall.controller.portal;

import com.mmall.common.Const;
import com.mmall.common.RedisPool;
import com.mmall.common.ResponseCode;
import com.mmall.common.ServerResponse;
import com.mmall.pojo.User;
import com.mmall.service.IUserService;
import com.mmall.util.CookieUtil;
import com.mmall.util.JsonUtil;
import com.mmall.util.RedisPoolUtil;
import net.sf.jsqlparser.schema.Server;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * Created by hasee on 2017/5/18.
 */
@Controller
@RequestMapping("/user/")
public class UserController {

    @Autowired
    private IUserService iUserService;

    /**
     * 用户登录
     * @param username
     * @param password
     * @param session
     * @return
     */
    @RequestMapping(value = "login.do",method= RequestMethod.POST)
    @ResponseBody
    public ServerResponse<User> login(String username, String password, HttpSession session, HttpServletResponse httpServletResponse){
        ServerResponse<User> response = iUserService.login(username, password);
        if (response.isSuccess()){
            /*二期存储用户信息方式    start*/
            //向浏览器中写入cookie
            CookieUtil.writeLoginToken(httpServletResponse,session.getId());
            //用户信息改为存储在redis中
            RedisPoolUtil.setEx(session.getId(), JsonUtil.obj2String(response.getData()),Const.RedisCacheExtime.REDIS_SESSION_EXTIME);
            /*二期存储用户信息方式    end*/

            /*一期存储用户信息方式    start*/
            //session.setAttribute(Const.CURRENT_USER,response.getData());
            /*一期存储用户信息方式    end*/
        }
        return response;
    }

    /**
     * 用户退出登录
     * @param httpServletRequest
     * @param httpServletResponse
     * @return
     */
    @RequestMapping(value = "logout.do",method= RequestMethod.POST)
    @ResponseBody
    public ServerResponse<String> logout(HttpServletRequest httpServletRequest,HttpServletResponse httpServletResponse){
        //二期用redis
        String cookieValue = CookieUtil.readLoginToken(httpServletRequest);
        CookieUtil.delLoginToken(httpServletRequest,httpServletResponse);
        //退出登录就是把redis中用户的登录信息移除掉就可以了
        RedisPoolUtil.del(cookieValue);

        //一期用session
        //session.removeAttribute(Const.CURRENT_USER);
        return ServerResponse.createBySuccess();
    }

    /**
     * 用户注册
     * @param user
     * @return
     */
    @RequestMapping(value = "register.do",method= RequestMethod.POST)
    @ResponseBody
    public ServerResponse<String> register(User user){
        return iUserService.register(user);
    }

    /**
     * 前台校验用户名和email是否已经存在
     * @param str
     * @param type
     * @return
     */
    @RequestMapping(value = "check_valid.do",method= RequestMethod.POST)
    @ResponseBody
    public ServerResponse<String> checkValid(String str,String type){
        return iUserService.checkValid(str,type);
    }

    /**
     * 获取个人信息
     * @param httpServletRequest
     * @return
     */
    @RequestMapping(value = "get_user_info.do",method= RequestMethod.POST)
    @ResponseBody
    public ServerResponse<User> getUserInfo(HttpServletRequest httpServletRequest){
        String cookieValue = CookieUtil.readLoginToken(httpServletRequest);
        if (StringUtils.isEmpty(cookieValue)){
            return ServerResponse.createByErrorMessage("用户未登陆，无法获取用户信息！");
        }
        String userJsonStr = RedisPoolUtil.get(cookieValue);
        User user = JsonUtil.string2Obj(userJsonStr,User.class);
        if(user != null){
            return ServerResponse.createBySuccess(user);
        }
        return ServerResponse.createByErrorMessage("用户未登陆，无法获取用户信息！");
    }

    /**
     * 获取密码提示问题
     * @param username
     * @return
     */
    @RequestMapping(value = "forget_get_question.do",method= RequestMethod.POST)
    @ResponseBody
    public ServerResponse<String> forgetGetQuestion(String username){
        return iUserService.selectQuestion(username);
    }

    /**
     * 验证提示问题答案是否正确，正确返回一个token用户密码修改操作
     * @param username
     * @param question
     * @param answer
     * @return
     */
    @RequestMapping(value = "forget_check_answer.do",method= RequestMethod.POST)
    @ResponseBody
    public ServerResponse<String> forgetCheckAnswer(String username,String question,String answer){
        return iUserService.checkAnswer(username,question,answer);
    }

    /**
     * 用户忘记密码，通过提示问题答案的方式修改密码
     * @param username
     * @param passwordNew
     * @param forgetToken
     * @return
     */
    @RequestMapping(value = "forget_reset_password.do",method= RequestMethod.POST)
    @ResponseBody
    public ServerResponse<String> forgetResetPassword(String username,String passwordNew,String forgetToken){
        return iUserService.forgetResetPassword(username,passwordNew,forgetToken);
    }

    /**
     * 当前登陆用户修改密码
     * @param httpServletRequest
     * @param passwordOld
     * @param passwordNew
     * @return
     */
    @RequestMapping(value = "reset_password.do",method= RequestMethod.POST)
    @ResponseBody
    public ServerResponse<String> resetPassword(HttpServletRequest httpServletRequest,String passwordOld,String passwordNew){
        String cookieValue = CookieUtil.readLoginToken(httpServletRequest);
        if (StringUtils.isEmpty(cookieValue)){
            return ServerResponse.createByErrorMessage("用户未登陆，无法获取用户信息！");
        }
        String userJsonStr = RedisPoolUtil.get(cookieValue);
        User user = JsonUtil.string2Obj(userJsonStr,User.class);
        if(user == null){
            return ServerResponse.createByErrorMessage("用户未登陆，请先登录再修改密码！");
        }
        return iUserService.resetPassword(passwordOld,passwordNew,user);
    }

    /**
     * 个人信息修改
     * @param httpServletRequest
     * @param user
     * @return
     */
    @RequestMapping(value = "update_information.do",method= RequestMethod.POST)
    @ResponseBody
    public ServerResponse<User> updateInformation(HttpServletRequest httpServletRequest,User user){
        String cookieValue = CookieUtil.readLoginToken(httpServletRequest);
        if (StringUtils.isEmpty(cookieValue)){
            return ServerResponse.createByErrorMessage("用户未登陆，无法获取用户信息！");
        }
        String userJsonStr = RedisPoolUtil.get(cookieValue);
        User currentUser = JsonUtil.string2Obj(userJsonStr,User.class);
        if(currentUser == null){
            return ServerResponse.createByErrorMessage("用户未登陆，请先登录再更新个人信息！");
        }
        user.setId(currentUser.getId());
        user.setUsername(currentUser.getUsername());
        ServerResponse<User> response = iUserService.updateInformation(user);
        if (response.isSuccess()){
            response.getData().setUsername(currentUser.getUsername());
            RedisPoolUtil.setEx(cookieValue,JsonUtil.obj2String(response.getData()),Const.RedisCacheExtime.REDIS_SESSION_EXTIME);
        }
        return response;
    }

    /**
     * 获取当前登陆用户个人信息
     * @param httpServletRequest
     * @return
     */
    @RequestMapping(value = "get_information.do",method= RequestMethod.POST)
    @ResponseBody
    public ServerResponse<User> getInformation(HttpServletRequest httpServletRequest){
        String cookieValue = CookieUtil.readLoginToken(httpServletRequest);
        if (StringUtils.isEmpty(cookieValue)){
            return ServerResponse.createByErrorMessage("用户未登陆，无法获取用户信息！");
        }
        String userJsonStr = RedisPoolUtil.get(cookieValue);
        User user = JsonUtil.string2Obj(userJsonStr,User.class);
        if (user == null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),"用户需要先登陆，status=10！");
        }
        return iUserService.getInformation(user.getId());
    }
}

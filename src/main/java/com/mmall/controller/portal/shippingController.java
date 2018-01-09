package com.mmall.controller.portal;

import com.github.pagehelper.PageInfo;
import com.mmall.common.Const;
import com.mmall.common.ResponseCode;
import com.mmall.common.ServerResponse;
import com.mmall.pojo.Shipping;
import com.mmall.pojo.User;
import com.mmall.service.IShippingService;
import com.mmall.util.CookieUtil;
import com.mmall.util.JsonUtil;
import com.mmall.util.RedisPoolUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.jws.soap.SOAPBinding;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

/**
 * Created by hasee on 2017/6/4.
 */
@Controller
@RequestMapping("/shipping/")
public class ShippingController {

    @Autowired
    private IShippingService iShippingService;

    /**
     * 添加收货地址
     * @param httpServletRequest
     * @param shipping
     * @return
     */
    @RequestMapping("add.do")
    @ResponseBody
    public ServerResponse add(HttpServletRequest httpServletRequest, Shipping shipping){
        String cookieValue = CookieUtil.readLoginToken(httpServletRequest);
        if (StringUtils.isEmpty(cookieValue)){
            return ServerResponse.createByErrorMessage("用户未登陆，无法获取用户信息！");
        }
        String userJsonStr = RedisPoolUtil.get(cookieValue);
        User user = JsonUtil.string2Obj(userJsonStr,User.class);
        if (user == null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getDesc());
        }
        return iShippingService.add(user.getId(),shipping);
    }

    /**
     * 删除收货地址
     * @param httpServletRequest
     * @param shippingId
     * @return
     */
    @RequestMapping("del.do")
    @ResponseBody
    public ServerResponse del(HttpServletRequest httpServletRequest, Integer shippingId){
        String cookieValue = CookieUtil.readLoginToken(httpServletRequest);
        if (StringUtils.isEmpty(cookieValue)){
            return ServerResponse.createByErrorMessage("用户未登陆，无法获取用户信息！");
        }
        String userJsonStr = RedisPoolUtil.get(cookieValue);
        User user = JsonUtil.string2Obj(userJsonStr,User.class);
        if (user == null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getDesc());
        }
        //防止用户任意删除其他userid的收货地址(横向越权),传一个userid判断收货地址是否属于当前登录用户
        return iShippingService.del(user.getId(),shippingId);
    }

    /**
     * 更新收货地址
     * @param session
     * @param shipping
     * @return
     */
    @RequestMapping("update.do")
    @ResponseBody
    public ServerResponse update(HttpServletRequest httpServletRequest, Shipping shipping){
        String cookieValue = CookieUtil.readLoginToken(httpServletRequest);
        if (StringUtils.isEmpty(cookieValue)){
            return ServerResponse.createByErrorMessage("用户未登陆，无法获取用户信息！");
        }
        String userJsonStr = RedisPoolUtil.get(cookieValue);
        User user = JsonUtil.string2Obj(userJsonStr,User.class);
        if (user == null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getDesc());
        }
        //防止用户任意更改其他userid的收货地址(横向越权),传一个userid判断收货地址是否属于当前登录用户
        return iShippingService.update(user.getId(),shipping);
    }

    /**
     * 查看一个收货地址
     * @param httpServletRequest
     * @param shippingId
     * @return
     */
    @RequestMapping("select.do")
    @ResponseBody
    public ServerResponse<Shipping> select(HttpServletRequest httpServletRequest, Integer shippingId){
        String cookieValue = CookieUtil.readLoginToken(httpServletRequest);
        if (StringUtils.isEmpty(cookieValue)){
            return ServerResponse.createByErrorMessage("用户未登陆，无法获取用户信息！");
        }
        String userJsonStr = RedisPoolUtil.get(cookieValue);
        User user = JsonUtil.string2Obj(userJsonStr,User.class);
        if (user == null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getDesc());
        }
        //防止用户任意查看其他userid的收货地址(横向越权),传一个userid判断收货地址是否属于当前登录用户
        return iShippingService.select(user.getId(),shippingId);
    }

    /**
     * 分页查询收货地址
     * @param pageNum
     * @param pageSize
     * @param httpServletRequest
     * @return
     */
    @RequestMapping("list.do")
    @ResponseBody
    public ServerResponse<PageInfo> list(@RequestParam(value = "pageNum",defaultValue = "1")Integer pageNum,
                                         @RequestParam(value = "pageSize",defaultValue = "10")Integer pageSize,
                                         HttpServletRequest httpServletRequest){
        String cookieValue = CookieUtil.readLoginToken(httpServletRequest);
        if (StringUtils.isEmpty(cookieValue)){
            return ServerResponse.createByErrorMessage("用户未登陆，无法获取用户信息！");
        }
        String userJsonStr = RedisPoolUtil.get(cookieValue);
        User user = JsonUtil.string2Obj(userJsonStr,User.class);
        if (user == null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getDesc());
        }
        return iShippingService.list(user.getId(),pageNum,pageSize);
    }
}

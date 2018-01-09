package com.mmall.controller.portal;

import com.mmall.common.Const;
import com.mmall.common.ResponseCode;
import com.mmall.common.ServerResponse;
import com.mmall.pojo.User;
import com.mmall.service.ICartService;
import com.mmall.util.CookieUtil;
import com.mmall.util.JsonUtil;
import com.mmall.util.RedisPoolUtil;
import com.mmall.vo.CartVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.jws.soap.SOAPBinding;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

/**
 * Created by hasee on 2017/5/31.
 */
@Controller
@RequestMapping("/cart/")
public class CartController {

    @Autowired
    private ICartService iCartService;

    /**
     * 添加购物车信息
     * @param httpServletRequest
     * @param count
     * @param productId
     * @return
     */
    @RequestMapping("add.do")
    @ResponseBody
    public ServerResponse<CartVo> add(HttpServletRequest httpServletRequest, Integer count, Integer productId){
        String cookieValue = CookieUtil.readLoginToken(httpServletRequest);
        if (StringUtils.isEmpty(cookieValue)){
            return ServerResponse.createByErrorMessage("用户未登陆，无法获取用户信息！");
        }
        String userJsonStr = RedisPoolUtil.get(cookieValue);
        User user = JsonUtil.string2Obj(userJsonStr,User.class);
        if (user == null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getDesc());
        }
        return iCartService.add(user.getId(),productId,count);
    }

    /**
     * 更新购物车信息
     * @param httpServletRequest
     * @param count
     * @param productId
     * @return
     */
    @RequestMapping("update.do")
    @ResponseBody
    public ServerResponse<CartVo> update(HttpServletRequest httpServletRequest,Integer count,Integer productId){
        String cookieValue = CookieUtil.readLoginToken(httpServletRequest);
        if (StringUtils.isEmpty(cookieValue)){
            return ServerResponse.createByErrorMessage("用户未登陆，无法获取用户信息！");
        }
        String userJsonStr = RedisPoolUtil.get(cookieValue);
        User user = JsonUtil.string2Obj(userJsonStr,User.class);
        if (user == null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getDesc());
        }
        return iCartService.update(user.getId(),productId,count);
    }

    /**
     * 删除购物车信息
     * @param httpServletRequest
     * @param productIds
     * @return
     */
    @RequestMapping("delete_product.do")
    @ResponseBody
    public ServerResponse<CartVo> deleteProduct(HttpServletRequest httpServletRequest,String productIds){
        String cookieValue = CookieUtil.readLoginToken(httpServletRequest);
        if (StringUtils.isEmpty(cookieValue)){
            return ServerResponse.createByErrorMessage("用户未登陆，无法获取用户信息！");
        }
        String userJsonStr = RedisPoolUtil.get(cookieValue);
        User user = JsonUtil.string2Obj(userJsonStr,User.class);
        if (user == null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getDesc());
        }
        return iCartService.delete(user.getId(),productIds);
    }

    /**
     * 获取购物车列表
     * @param httpServletRequest
     * @return
     */
    @RequestMapping("list.do")
    @ResponseBody
    public ServerResponse<CartVo> list(HttpServletRequest httpServletRequest){
        String cookieValue = CookieUtil.readLoginToken(httpServletRequest);
        if (StringUtils.isEmpty(cookieValue)){
            return ServerResponse.createByErrorMessage("用户未登陆，无法获取用户信息！");
        }
        String userJsonStr = RedisPoolUtil.get(cookieValue);
        User user = JsonUtil.string2Obj(userJsonStr,User.class);
        if (user == null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getDesc());
        }
        return iCartService.list(user.getId());
    }

    /**
     * 全选购物车中商品
     * @param httpServletRequest
     * @return
     */
    @RequestMapping("select_all.do")
    @ResponseBody
    public ServerResponse<CartVo> selectAll(HttpServletRequest httpServletRequest){
        String cookieValue = CookieUtil.readLoginToken(httpServletRequest);
        if (StringUtils.isEmpty(cookieValue)){
            return ServerResponse.createByErrorMessage("用户未登陆，无法获取用户信息！");
        }
        String userJsonStr = RedisPoolUtil.get(cookieValue);
        User user = JsonUtil.string2Obj(userJsonStr,User.class);
        if (user == null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getDesc());
        }
        return iCartService.selectOrUnselect(user.getId(),null,Const.Cart.CHECKED);
    }

    /**
     * 全反选购物车中商品
     * @param httpServletRequest
     * @return
     */
    @RequestMapping("un_select_all.do")
    @ResponseBody
    public ServerResponse<CartVo> unselectAll(HttpServletRequest httpServletRequest){
        String cookieValue = CookieUtil.readLoginToken(httpServletRequest);
        if (StringUtils.isEmpty(cookieValue)){
            return ServerResponse.createByErrorMessage("用户未登陆，无法获取用户信息！");
        }
        String userJsonStr = RedisPoolUtil.get(cookieValue);
        User user = JsonUtil.string2Obj(userJsonStr,User.class);
        if (user == null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getDesc());
        }
        return iCartService.selectOrUnselect(user.getId(),null,Const.Cart.UN_CHECKED);
    }

    /**
     * 选中商品
     * @param httpServletRequest
     * @param productId
     * @return
     */
    @RequestMapping("select.do")
    @ResponseBody
    public ServerResponse<CartVo> select(HttpServletRequest httpServletRequest,Integer productId){
        String cookieValue = CookieUtil.readLoginToken(httpServletRequest);
        if (StringUtils.isEmpty(cookieValue)){
            return ServerResponse.createByErrorMessage("用户未登陆，无法获取用户信息！");
        }
        String userJsonStr = RedisPoolUtil.get(cookieValue);
        User user = JsonUtil.string2Obj(userJsonStr,User.class);
        if (user == null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getDesc());
        }
        return iCartService.selectOrUnselect(user.getId(),productId,Const.Cart.CHECKED);
    }

    /**
     * 取消选中商品
     * @param httpServletRequest
     * @param productId
     * @return
     */
    @RequestMapping("un_select.do")
    @ResponseBody
    public ServerResponse<CartVo> unselect(HttpServletRequest httpServletRequest,Integer productId){
        String cookieValue = CookieUtil.readLoginToken(httpServletRequest);
        if (StringUtils.isEmpty(cookieValue)){
            return ServerResponse.createByErrorMessage("用户未登陆，无法获取用户信息！");
        }
        String userJsonStr = RedisPoolUtil.get(cookieValue);
        User user = JsonUtil.string2Obj(userJsonStr,User.class);
        if (user == null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getDesc());
        }
        return iCartService.selectOrUnselect(user.getId(),productId,Const.Cart.UN_CHECKED);
    }

    /**
     * 获取当前用户购物车中商品数量
     * @param httpServletRequest
     * @return
     */
    @RequestMapping("get_cart_product_count.do")
    @ResponseBody
    public ServerResponse<Integer> getCartProductCount(HttpServletRequest httpServletRequest){
        String cookieValue = CookieUtil.readLoginToken(httpServletRequest);
        if (StringUtils.isEmpty(cookieValue)){
            return ServerResponse.createByErrorMessage("用户未登陆，无法获取用户信息！");
        }
        String userJsonStr = RedisPoolUtil.get(cookieValue);
        User user = JsonUtil.string2Obj(userJsonStr,User.class);
        if (user == null){
            return ServerResponse.createBySuccess(0);
        }
        return iCartService.getCartProductCount(user.getId());
    }
}

package com.mmall.controller.portal;

import com.alipay.api.AlipayApiException;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.demo.trade.config.Configs;
import com.google.common.collect.Maps;
import com.mmall.common.Const;
import com.mmall.common.ResponseCode;
import com.mmall.common.ServerResponse;
import com.mmall.pojo.User;
import com.mmall.service.IOrderService;
import com.mmall.util.CookieUtil;
import com.mmall.util.JsonUtil;
import com.mmall.util.RedisPoolUtil;
import com.mmall.util.RedisShardedPoolUtil;
import com.mmall.vo.OrderVo;
import org.apache.ibatis.annotations.Param;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by hasee on 2017/6/4.
 */
@Controller
@RequestMapping("/order/")
public class OrderController {

    Logger logger = LoggerFactory.getLogger(OrderController.class);

    @Autowired
    private IOrderService iOrderService;

    /**
     * 生成新订单
     * @param httpServletRequest
     * @param shippingId
     * @return
     */
    @RequestMapping("create.do")
    @ResponseBody
    public ServerResponse create(HttpServletRequest httpServletRequest,Integer shippingId){
        String cookieValue = CookieUtil.readLoginToken(httpServletRequest);
        if (StringUtils.isEmpty(cookieValue)){
            return ServerResponse.createByErrorMessage("用户未登陆，无法获取用户信息！");
        }
        String userJsonStr = RedisShardedPoolUtil.get(cookieValue);
        User user = JsonUtil.string2Obj(userJsonStr,User.class);
        if (user == null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getDesc());
        }

        return iOrderService.createOrder(user.getId(),shippingId);
    }

    /**
     * 取消订单
     * @param httpServletRequest
     * @param orderNo
     * @return
     */
    @RequestMapping("cancel.do")
    @ResponseBody
    public ServerResponse<String> cancel(HttpServletRequest httpServletRequest,long orderNo){
        String cookieValue = CookieUtil.readLoginToken(httpServletRequest);
        if (StringUtils.isEmpty(cookieValue)){
            return ServerResponse.createByErrorMessage("用户未登陆，无法获取用户信息！");
        }
        String userJsonStr = RedisShardedPoolUtil.get(cookieValue);
        User user = JsonUtil.string2Obj(userJsonStr,User.class);
        if (user == null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getDesc());
        }
        return iOrderService.cancel(user.getId(),orderNo);
    }

    /**
     * 获取生成订单前的商品列表详情
     * @param httpServletRequest
     * @return
     */
    @RequestMapping("get_order_cart_product.do")
    @ResponseBody
    public ServerResponse getOrderCartProduct(HttpServletRequest httpServletRequest){
        String cookieValue = CookieUtil.readLoginToken(httpServletRequest);
        if (StringUtils.isEmpty(cookieValue)){
            return ServerResponse.createByErrorMessage("用户未登陆，无法获取用户信息！");
        }
        String userJsonStr = RedisShardedPoolUtil.get(cookieValue);
        User user = JsonUtil.string2Obj(userJsonStr,User.class);
        if (user == null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getDesc());
        }
        return iOrderService.getOrderCartProduct(user.getId());
    }

    /**
     * 前台获取订单详情
     * @param httpServletRequest
     * @param orderNo
     * @return
     */
    @RequestMapping("detail.do")
    @ResponseBody
    public ServerResponse<OrderVo> detail(HttpServletRequest httpServletRequest,long orderNo){
        String cookieValue = CookieUtil.readLoginToken(httpServletRequest);
        if (StringUtils.isEmpty(cookieValue)){
            return ServerResponse.createByErrorMessage("用户未登陆，无法获取用户信息！");
        }
        String userJsonStr = RedisShardedPoolUtil.get(cookieValue);
        User user = JsonUtil.string2Obj(userJsonStr,User.class);
        if (user == null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getDesc());
        }
        return iOrderService.getOrderDetail(user.getId(),orderNo);
    }

    /**
     * 查询订单列表
     * @param httpServletRequest
     * @param pageNum
     * @param pageSize
     * @return
     */
    @RequestMapping("list.do")
    @ResponseBody
    public ServerResponse list(HttpServletRequest httpServletRequest, @RequestParam(value = "pageNum",defaultValue = "1") int pageNum, @RequestParam(value = "pageSize",defaultValue = "10") int pageSize){
        String cookieValue = CookieUtil.readLoginToken(httpServletRequest);
        if (StringUtils.isEmpty(cookieValue)){
            return ServerResponse.createByErrorMessage("用户未登陆，无法获取用户信息！");
        }
        String userJsonStr = RedisShardedPoolUtil.get(cookieValue);
        User user = JsonUtil.string2Obj(userJsonStr,User.class);
        if (user == null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getDesc());
        }
        return iOrderService.getOrderList(user.getId(),pageNum,pageSize);
    }

    /**
     * 支付订单
     * @param httpServletRequest
     * @param orderNo
     * @param request
     * @return
     */
    @RequestMapping("pay.do")
    @ResponseBody
    public ServerResponse pay(HttpServletRequest httpServletRequest, Long orderNo, HttpServletRequest request){
        String cookieValue = CookieUtil.readLoginToken(httpServletRequest);
        if (StringUtils.isEmpty(cookieValue)){
            return ServerResponse.createByErrorMessage("用户未登陆，无法获取用户信息！");
        }
        String userJsonStr = RedisShardedPoolUtil.get(cookieValue);
        User user = JsonUtil.string2Obj(userJsonStr,User.class);
        if (user == null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getDesc());
        }
        String path = request.getSession().getServletContext().getRealPath("upload");
        return iOrderService.pay(orderNo,user.getId(),path);
    }

    /**
     * 支付宝回调方法
     * @param request
     * @return
     */
    @RequestMapping("alipay_callback.do")
    @ResponseBody
    public Object alipayCallback(HttpServletRequest request){
        Map<String,String> params = Maps.newHashMap();

        Map requestParams = request.getParameterMap();
        for (Iterator iter = requestParams.keySet().iterator();iter.hasNext();){
            String name = (String) iter.next();
            String[] values = (String[]) requestParams.get(name);
            String valueStr = "";
            for (int i=0;i<values.length;i++){
                valueStr = (i==values.length-1) ? valueStr + values[i] : valueStr + values[i] +",";
            }
            params.put(name,valueStr);
        }
        logger.info("支付宝回调,sign:{},trade_status:{},参数:{}",params.get("sign"),params.get("trade_status"),params.toString());

        //验签,验证支付宝回调是否有效以及回调是否是支付宝发出的,还要避免重复通知
        params.remove("sign_type");
        try {
            boolean alipayRSACheckedV2 = AlipaySignature.rsaCheckV2(params, Configs.getAlipayPublicKey(),"utf-8",Configs.getSignType());

            if (!alipayRSACheckedV2){
                return ServerResponse.createByErrorMessage("非法请求，验证失败！！！");
            }
        } catch (AlipayApiException e) {
            logger.error("验证回调异常！",e);
        }

        // TODO: 2017/6/5 此处需要验证参数中的数据和我们业务逻辑中生成订单的数据是否一致

        ServerResponse serverResponse = iOrderService.aliCallback(params);
        if (serverResponse.isSuccess()){
            return Const.AlipayCallback.RESPONSE_SUCCESS;
        }
        return Const.AlipayCallback.RESPONSE_FALLED;
    }

    /**
     * 用户扫码付款后前台调用该方法查询订单是否支付成功，成功的话前台跳转订单详情页面
     * @param httpServletRequest
     * @param orderNo
     * @return
     */
    @RequestMapping("query_order_pay_status.do")
    @ResponseBody
    public ServerResponse<Boolean> queryOrderPayStatus(HttpServletRequest httpServletRequest, Long orderNo){
        String cookieValue = CookieUtil.readLoginToken(httpServletRequest);
        if (StringUtils.isEmpty(cookieValue)){
            return ServerResponse.createByErrorMessage("用户未登陆，无法获取用户信息！");
        }
        String userJsonStr = RedisShardedPoolUtil.get(cookieValue);
        User user = JsonUtil.string2Obj(userJsonStr,User.class);
        if(user ==null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getDesc());
        }

        ServerResponse serverResponse = iOrderService.queryOrderPayStatus(user.getId(),orderNo);
        if(serverResponse.isSuccess()){
            return ServerResponse.createBySuccess(true);
        }
        return ServerResponse.createBySuccess(false);
    }
}
